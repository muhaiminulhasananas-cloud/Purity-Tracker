package com.example

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*
import kotlin.random.Random

object AmbientSoundPlayer {
    private const val TAG = "AmbientSoundPlayer"
    private const val SAMPLE_RATE = 44100
    private var audioTrack: AudioTrack? = null
    
    private val _isPlayingFlow = MutableStateFlow(false)
    val isPlayingFlow: StateFlow<Boolean> = _isPlayingFlow.asStateFlow()

    private val _currentSoundFlow = MutableStateFlow("Rain")
    val currentSoundFlow: StateFlow<String> = _currentSoundFlow.asStateFlow()

    private var currentSound = "Rain"
    private var playJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Synchronized
    fun startSound(soundName: String) {
        currentSound = soundName
        _currentSoundFlow.value = soundName
        if (soundName == "Silence") {
            stopSound()
            return
        }

        if (_isPlayingFlow.value && audioTrack != null) {
            // Already running, will seamlessly transition in synthesis loop
            return
        }

        stopSound()
        _isPlayingFlow.value = true

        val bufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(SAMPLE_RATE / 2)

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.setVolume(1.0f)
            audioTrack?.play()

            playJob = scope.launch {
                synthesizeAndStream()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio track", e)
            _isPlayingFlow.value = false
        }
    }

    fun setSound(soundName: String) {
        currentSound = soundName
        _currentSoundFlow.value = soundName
        if (soundName == "Silence") {
            stopSound()
        } else {
            startSound(soundName)
        }
    }

    fun togglePlay(soundName: String = currentSound) {
        if (_isPlayingFlow.value) {
            stopSound()
        } else {
            val targetSound = if (soundName == "Silence") "Rain" else soundName
            startSound(targetSound)
        }
    }

    @Synchronized
    fun stopSound() {
        _isPlayingFlow.value = false
        playJob?.cancel()
        playJob = null
        try {
            audioTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping audio track", e)
        }
        audioTrack = null
    }

    fun isCurrentlyPlaying(): Boolean = _isPlayingFlow.value

    private suspend fun synthesizeAndStream() {
        val chunkSamples = 2048
        val pcmBuffer = ShortArray(chunkSamples)
        var sampleIndex = 0L

        // High quality pink & brown noise state variables
        var b0 = 0.0
        var b1 = 0.0
        var b2 = 0.0
        var b3 = 0.0
        var b4 = 0.0
        var b5 = 0.0
        var b6 = 0.0
        var brownNoise = 0.0

        val random = Random(System.currentTimeMillis())

        while (_isPlayingFlow.value && currentCoroutineContext().isActive) {
            val sound = currentSound
            if (sound == "Silence") {
                delay(100)
                continue
            }

            for (i in 0 until chunkSamples) {
                val t = (sampleIndex + i).toDouble() / SAMPLE_RATE
                val white = random.nextDouble(-1.0, 1.0)

                // Paul Kellet's refined Pink Noise filter (-3dB/octave) for rich natural ambient acoustics
                b0 = 0.99886 * b0 + white * 0.0555179
                b1 = 0.99332 * b1 + white * 0.0750759
                b2 = 0.96900 * b2 + white * 0.1538520
                b3 = 0.86650 * b3 + white * 0.3104856
                b4 = 0.55000 * b4 + white * 0.5329522
                b5 = -0.7616 * b5 - white * 0.0168980
                val pink = (b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362) * 0.14
                b6 = white * 0.115926

                val sampleVal: Double = when (sound) {
                    "Rain" -> {
                        // High fidelity soothing rainfall:
                        // 1. Soft atmospheric downpour (pink noise)
                        // 2. Low-frequency distant thunder rumble (brown noise)
                        // 3. Clear, randomized crisp water droplets and puddle splashes
                        brownNoise = (brownNoise + (0.015 * white)) / 1.015
                        val rainHiss = pink * 0.75
                        val rainRumble = brownNoise * 0.35
                        val droplet = if (random.nextDouble() < 0.012) {
                            val dropFreq = random.nextDouble(800.0, 2400.0)
                            sin(2.0 * PI * dropFreq * t) * 0.35 * exp(-random.nextDouble(10.0, 30.0) * (t % 0.1))
                        } else 0.0
                        (rainHiss + rainRumble + droplet).coerceIn(-1.0, 1.0)
                    }

                    "Ocean" -> {
                        // Deep rolling ocean waves with smooth swell envelope (7-second natural wave cycle)
                        brownNoise = (brownNoise + (0.035 * white)) / 1.035
                        val wavePeriod = 7.0
                        val phase = (t % wavePeriod) / wavePeriod
                        // Asymmetric wave shape (gradual rise, roaring crest, gentle foaming backwash)
                        val swell = sin(PI * phase).pow(1.8)
                        val foamWash = pink * 0.48 * swell
                        val deepSurge = brownNoise * 3.6 * swell
                        val spray = if (phase > 0.4 && phase < 0.7) pink * 0.25 else 0.0
                        (deepSurge + foamWash + spray).coerceIn(-1.0, 1.0)
                    }

                    "Breeze" -> {
                        // Soothing natural wind breeze with gentle gusts and tree leaf rustle
                        brownNoise = (brownNoise + (0.018 * white)) / 1.018
                        val gust = 0.5 + 0.5 * sin(2 * PI * 0.12 * t + 0.4 * sin(2 * PI * 0.03 * t))
                        val leavesRustle = pink * 0.32 * gust
                        val gentleWind = brownNoise * 2.8 * gust
                        val whistlingAir = sin(2 * PI * (320.0 + 40.0 * sin(2 * PI * 0.2 * t)) * t) * 0.05 * gust
                        (gentleWind + leavesRustle + whistlingAir).coerceIn(-1.0, 1.0)
                    }

                    "Bell" -> {
                        // Authentic Tibetan meditation singing bowl & 432 Hz Solfeggio healing chime
                        val bowlPeriod = 6.0 // Resonates every 6 seconds
                        val bowlTime = (t % bowlPeriod)
                        val resonanceDecay = exp(-0.85 * bowlTime)

                        // 432Hz fundamental + harmonic overtones (864Hz, 1296Hz, 1728Hz)
                        val f1 = sin(2 * PI * 432.0 * t) * 0.45
                        val f2 = sin(2 * PI * 864.0 * t) * 0.22
                        val f3 = sin(2 * PI * 1296.0 * t) * 0.12
                        val f4 = sin(2 * PI * 1728.0 * t) * 0.06
                        val shimmerDrone = sin(2 * PI * 216.0 * t) * 0.18 // Sub-harmonic warm drone
                        val strikeTransient = if (bowlTime < 0.02) random.nextDouble(-0.3, 0.3) else 0.0

                        ((f1 + f2 + f3 + f4 + strikeTransient) * resonanceDecay + shimmerDrone).coerceIn(-1.0, 1.0)
                    }

                    else -> {
                        // Calming mountain river stream with bubbling water harmonics
                        brownNoise = (brownNoise + (0.025 * white)) / 1.025
                        val bubble1 = sin(2 * PI * (650.0 + 200.0 * sin(2 * PI * 4.2 * t)) * t) * 0.15
                        val bubble2 = sin(2 * PI * (1100.0 + 350.0 * cos(2 * PI * 6.5 * t)) * t) * 0.10
                        (brownNoise * 1.5 + bubble1 + bubble2 + pink * 0.35).coerceIn(-1.0, 1.0)
                    }
                }

                // Output clean 16-bit PCM volume (31000 out of 32767 for loud & clear playback)
                pcmBuffer[i] = (sampleVal * 31000.0).toInt().coerceIn(-32767, 32767).toShort()
            }

            sampleIndex += chunkSamples
            try {
                val written = audioTrack?.write(pcmBuffer, 0, chunkSamples) ?: -1
                if (written < 0) {
                    break
                }
            } catch (e: Exception) {
                break
            }
        }
    }
}
