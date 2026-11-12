package com.example

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.util.Base64
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

enum class CallRole { CALLER, RECEIVER }
enum class CallStatus { IDLE, CALLING, RINGING, CONNECTED, ENDED }

data class ActiveCallSession(
    val callId: String,
    val callerId: String,
    val callerName: String,
    val callerPhoto: String,
    val receiverId: String,
    val receiverName: String,
    val receiverPhoto: String,
    val isVideo: Boolean = false,
    val role: CallRole,
    val status: CallStatus,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false
)

object VoiceCallManager {
    private const val TAG = "VoiceCallManager"
    // 8kHz standard telephony PCM for ultra-low latency & 100% reliable packet exchange
    private const val SAMPLE_RATE = 8000
    private const val CHUNK_DURATION_MS = 60L // Ultra-fast 60ms packets for instantaneous millisecond audio exchange

    private val _callSession = MutableStateFlow<ActiveCallSession?>(null)
    val callSession: StateFlow<ActiveCallSession?> = _callSession.asStateFlow()

    private val db by lazy { FirebaseFirestore.getInstance() }
    private var callListener: ListenerRegistration? = null
    private var audioStreamListener: ListenerRegistration? = null
    private var timerJob: Job? = null

    // Audio recording & playback
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var aec: AcousticEchoCanceler? = null
    private var ns: NoiseSuppressor? = null
    private var agc: AutomaticGainControl? = null
    private val isRecording = AtomicBoolean(false)
    private val isPlaying = AtomicBoolean(false)
    private var audioJob: Job? = null

    private var audioManager: AudioManager? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun initialize(context: Context) {
        audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    /**
     * Start an outgoing call to a friend
     */
    fun startCall(
        context: Context,
        callerId: String,
        callerName: String,
        callerPhoto: String,
        receiverId: String,
        receiverName: String,
        receiverPhoto: String,
        isVideo: Boolean = false
    ) {
        val callDoc = db.collection("voice_calls").document()
        val callId = callDoc.id

        val session = ActiveCallSession(
            callId = callId,
            callerId = callerId,
            callerName = callerName,
            callerPhoto = callerPhoto,
            receiverId = receiverId,
            receiverName = receiverName,
            receiverPhoto = receiverPhoto,
            isVideo = isVideo,
            role = CallRole.CALLER,
            status = CallStatus.CALLING
        )
        _callSession.value = session

        // Start ringing/dialing feedback tone for the caller
        CallSoundManager.startDialingTone()

        val data = hashMapOf<String, Any>(
            "callId" to callId,
            "callerId" to callerId,
            "callerName" to callerName,
            "callerPhoto" to callerPhoto,
            "receiverId" to receiverId,
            "receiverName" to receiverName,
            "receiverPhoto" to receiverPhoto,
            "isVideo" to isVideo,
            "status" to "calling",
            "createdAt" to System.currentTimeMillis()
        )

        callDoc.set(data).addOnSuccessListener {
            listenToCall(context, callDoc)
        }.addOnFailureListener {
            endCall()
        }
    }

    /**
     * Accept incoming call
     */
    fun acceptCall(context: Context) {
        CallSoundManager.stopAll()
        val current = _callSession.value ?: return
        val callDoc = db.collection("voice_calls").document(current.callId)

        callDoc.update(
            mapOf(
                "status" to "connected",
                "connectedAt" to System.currentTimeMillis()
            )
        )

        _callSession.value = current.copy(status = CallStatus.CONNECTED)
        startAudioStream(context, current.callId, current.role)
        startCallTimer()
    }

    /**
     * Reject or hang up call
     */
    fun endCall() {
        CallSoundManager.stopAll()
        val current = _callSession.value
        if (current != null) {
            try {
                db.collection("voice_calls").document(current.callId)
                    .update("status", "ended")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to update call status: ${e.message}")
            }
        }

        cleanupAudio()
        callListener?.remove()
        callListener = null
        audioStreamListener?.remove()
        audioStreamListener = null
        timerJob?.cancel()
        timerJob = null

        _callSession.value = null
    }

    /**
     * Listen for incoming calls on current user's profile
     */
    fun listenForIncomingCalls(context: Context, currentUserId: String): ListenerRegistration {
        return db.collection("voice_calls")
            .whereEqualTo("receiverId", currentUserId)
            .whereIn("status", listOf("calling", "ringing"))
            .limit(1)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null || snapshots.isEmpty) {
                    // Check if current incoming call was cancelled by caller
                    if (_callSession.value?.role == CallRole.RECEIVER && _callSession.value?.status == CallStatus.RINGING) {
                        endCall()
                    }
                    return@addSnapshotListener
                }

                // If already on a call, ignore
                if (_callSession.value != null && _callSession.value?.status != CallStatus.IDLE) {
                    return@addSnapshotListener
                }

                val doc = snapshots.documents.firstOrNull() ?: return@addSnapshotListener
                val callId = doc.getString("callId") ?: doc.id
                val callerId = doc.getString("callerId") ?: ""
                val callerName = doc.getString("callerName") ?: "Friend"
                val callerPhoto = doc.getString("callerPhoto") ?: ""
                val receiverName = doc.getString("receiverName") ?: "Me"
                val receiverPhoto = doc.getString("receiverPhoto") ?: ""
                val isVideo = doc.getBoolean("isVideo") ?: false

                val session = ActiveCallSession(
                    callId = callId,
                    callerId = callerId,
                    callerName = callerName,
                    callerPhoto = callerPhoto,
                    receiverId = currentUserId,
                    receiverName = receiverName,
                    receiverPhoto = receiverPhoto,
                    isVideo = isVideo,
                    role = CallRole.RECEIVER,
                    status = CallStatus.RINGING
                )
                _callSession.value = session

                // Start incoming ringtone and vibration
                CallSoundManager.startIncomingRingtone(context)

                // Mark call document as ringing so the caller sees "Ringing" and knows the receiver's phone is alerting
                doc.reference.update("status", "ringing")

                // Fetch latest caller profile to ensure up-to-date photo and name
                if (callerId.isNotEmpty()) {
                    db.collection("users").document(callerId).get().addOnSuccessListener { callerDoc ->
                        if (callerDoc != null && callerDoc.exists()) {
                            val liveCallerPhoto = callerDoc.getString("profilePicUrl") ?: callerPhoto
                            val liveCallerName = callerDoc.getString("name") ?: callerName
                            _callSession.value = _callSession.value?.takeIf { it.callId == callId }?.copy(
                                callerPhoto = liveCallerPhoto,
                                callerName = liveCallerName.ifBlank { callerName }
                            )
                        }
                    }
                }

                listenToCall(context, doc.reference)
            }
    }

    private fun listenToCall(context: Context, docRef: DocumentReference) {
        callListener?.remove()
        callListener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) {
                endCall()
                return@addSnapshotListener
            }

            val statusStr = snapshot.getString("status") ?: "ended"
            val updatedCallerPhoto = snapshot.getString("callerPhoto")
            val updatedReceiverPhoto = snapshot.getString("receiverPhoto")
            val updatedCallerName = snapshot.getString("callerName")
            val updatedReceiverName = snapshot.getString("receiverName")

            val current = _callSession.value ?: return@addSnapshotListener
            var sessionToUpdate = current
            if (updatedCallerPhoto != null && updatedCallerPhoto != current.callerPhoto) {
                sessionToUpdate = sessionToUpdate.copy(callerPhoto = updatedCallerPhoto)
            }
            if (updatedReceiverPhoto != null && updatedReceiverPhoto != current.receiverPhoto) {
                sessionToUpdate = sessionToUpdate.copy(receiverPhoto = updatedReceiverPhoto)
            }
            if (!updatedCallerName.isNullOrBlank() && updatedCallerName != current.callerName) {
                sessionToUpdate = sessionToUpdate.copy(callerName = updatedCallerName)
            }
            if (!updatedReceiverName.isNullOrBlank() && updatedReceiverName != current.receiverName) {
                sessionToUpdate = sessionToUpdate.copy(receiverName = updatedReceiverName)
            }

            when (statusStr) {
                "ringing" -> {
                    if (sessionToUpdate.status == CallStatus.CALLING) {
                        _callSession.value = sessionToUpdate.copy(status = CallStatus.RINGING)
                    } else if (sessionToUpdate != current) {
                        _callSession.value = sessionToUpdate
                    }
                }
                "connected" -> {
                    CallSoundManager.stopAll()
                    if (sessionToUpdate.status != CallStatus.CONNECTED) {
                        _callSession.value = sessionToUpdate.copy(status = CallStatus.CONNECTED)
                        startAudioStream(context, sessionToUpdate.callId, sessionToUpdate.role)
                        startCallTimer()
                    } else if (sessionToUpdate != current) {
                        _callSession.value = sessionToUpdate
                    }
                }
                "ended", "rejected" -> {
                    endCall()
                }
                else -> {
                    if (sessionToUpdate != current) {
                        _callSession.value = sessionToUpdate
                    }
                }
            }
        }
    }

    private fun startCallTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                delay(1000)
                _callSession.value = _callSession.value?.let { session ->
                    session.copy(durationSeconds = session.durationSeconds + 1)
                }
            }
        }
    }

    /**
     * Real-time audio stream recording & playback via Firestore real-time channel
     */
    private fun startAudioStream(context: Context, callId: String, role: CallRole) {
        cleanupAudio()
        initialize(context)

        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(SAMPLE_RATE / 4)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize * 2
            )

            // Hardware Audio Effects for clear VoIP talk
            val audioSessionId = audioRecord?.audioSessionId ?: 0
            if (audioSessionId != 0) {
                try {
                    if (AcousticEchoCanceler.isAvailable()) {
                        aec = AcousticEchoCanceler.create(audioSessionId)?.apply {
                            enabled = true
                        }
                    }
                    if (NoiseSuppressor.isAvailable()) {
                        ns = NoiseSuppressor.create(audioSessionId)?.apply {
                            enabled = true
                        }
                    }
                    if (AutomaticGainControl.isAvailable()) {
                        agc = AutomaticGainControl.create(audioSessionId)?.apply {
                            enabled = true
                        }
                    }
                } catch (e: Throwable) {
                    Log.w(TAG, "Audio effect init: ${e.message}")
                }
            }

            val trackBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(bufferSize * 2)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(trackBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
            isPlaying.set(true)
            audioRecord?.startRecording()
            isRecording.set(true)

            // Configure audio manager for communication
            audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager?.isSpeakerphoneOn = false

        } catch (e: SecurityException) {
            Log.e(TAG, "Audio permission error: ${e.message}")
            return
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio engine: ${e.message}")
            return
        }

        // 1. Microphone Sender Coroutine
        val sendChannel = if (role == CallRole.CALLER) "caller_audio" else "receiver_audio"
        val receiveChannel = if (role == CallRole.CALLER) "receiver_audio" else "caller_audio"
        val audioStreamDoc = db.collection("voice_calls").document(callId)
            .collection("audio").document("stream")

        audioJob = scope.launch {
            val audioBuffer = ByteArray(bufferSize)
            var chunkIndex = 0L

            while (isActive && isRecording.get()) {
                val readBytes = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: -1
                if (readBytes > 0 && (_callSession.value?.isMuted != true)) {
                    val encoded = Base64.encodeToString(audioBuffer, 0, readBytes, Base64.NO_WRAP)
                    chunkIndex++

                    val packet = mapOf(
                        "${sendChannel}_data" to encoded,
                        "${sendChannel}_seq" to chunkIndex,
                        "${sendChannel}_ts" to System.currentTimeMillis()
                    )

                    audioStreamDoc.set(packet, SetOptions.merge())
                }
                delay(CHUNK_DURATION_MS)
            }
        }

        // 2. Incoming Audio Listener
        var lastReceivedSeq = -1L
        audioStreamListener = audioStreamDoc.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

            val seq = snapshot.getLong("${receiveChannel}_seq") ?: -1L
            if (seq > lastReceivedSeq) {
                lastReceivedSeq = seq
                val rawData = snapshot.getString("${receiveChannel}_data")
                if (!rawData.isNullOrEmpty()) {
                    try {
                        val pcmBytes = Base64.decode(rawData, Base64.NO_WRAP)
                        if (isPlaying.get() && audioTrack != null) {
                            audioTrack?.write(pcmBytes, 0, pcmBytes.size, AudioTrack.WRITE_NON_BLOCKING)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to decode/play voice packet: ${e.message}")
                    }
                }
            }
        }
    }

    fun toggleMute() {
        val current = _callSession.value ?: return
        val newMute = !current.isMuted
        _callSession.value = current.copy(isMuted = newMute)
    }

    fun toggleSpeaker() {
        val current = _callSession.value ?: return
        val newSpeaker = !current.isSpeakerOn
        audioManager?.isSpeakerphoneOn = newSpeaker
        _callSession.value = current.copy(isSpeakerOn = newSpeaker)
    }

    private fun cleanupAudio() {
        isRecording.set(false)
        isPlaying.set(false)
        audioJob?.cancel()
        audioJob = null

        try {
            aec?.release()
        } catch (e: Exception) {}
        aec = null

        try {
            ns?.release()
        } catch (e: Exception) {}
        ns = null

        try {
            agc?.release()
        } catch (e: Exception) {}
        agc = null

        try {
            audioRecord?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "cleanupAudio record: ${e.message}")
        }
        audioRecord = null

        try {
            audioTrack?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "cleanupAudio track: ${e.message}")
        }
        audioTrack = null

        try {
            audioManager?.mode = AudioManager.MODE_NORMAL
            audioManager?.isSpeakerphoneOn = false
        } catch (e: Exception) {
            Log.w(TAG, "reset audioManager: ${e.message}")
        }
    }
}

