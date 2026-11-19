package com.example

import com.example.ui.theme.*

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.TextUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

val DeepBlue: Color
    get() = if (com.example.ui.theme.GlassTheme.isLightMode.value) Color(0xFFF1F5F9).copy(alpha = 0.80f) else Color.Black

val CardBlue: Color
    get() = if (com.example.ui.theme.GlassTheme.isLightMode.value) Color.White.copy(alpha = 0.82f) else Color(0xFF141414).copy(alpha = 0.92f)
val RedAlert = Color(0xFFEF4444)
val LightRed = Color(0xFFFCA5A5)
val GreenText = Color(0xFF10A37F)
val CyanAccent = Color(0xFF38BDF8)

object LanguageStore {
    var currentLanguage = mutableStateOf("en")
    
    val translations = mapOf(
        "en" to mapOf(
            "nav_home" to "Home",
            "nav_history" to "History",
            "nav_focus" to "Focus",
            "nav_ai" to "AI Chat",
            "nav_salah" to "Salah",
            "nav_community" to "Community",
            "nav_profile" to "Account",
            "dns_dialog_title" to "How to set DNS?",
            "dns_dialog_desc" to "To block adult sites, you need to configure 'Private DNS' in your phone's settings.",
            "dns_step1" to "1. We copied 'family.adguard-dns.com'.",
            "dns_step2" to "2. Go to settings button to open your phone settings.",
            "dns_step3" to "3. Search there for 'Private DNS'.",
            "dns_step4" to "4. Select 'Private DNS provider hostname', paste the copied line and save.",
            "dns_alternative" to "Alternative solution: If DNS does not work, use BlockerX, Bulldog or OpenDNS router settings.",
            "go_to_settings" to "Go to Settings",
            "dismiss" to "Dismiss",
            "pin_dialog_title_change" to "Security PIN",
            "pin_dialog_title_set" to "Set Permanent Security PIN",
            "pin_dialog_advice" to "⚠️ Warning: Once set, this PIN CANNOT be changed or reset! Enter a memorable 4-8 digit PIN or ask a trusted person to set it.",
            "pin_dialog_enter" to "Enter 4-digit PIN",
            "pin_dialog_confirm" to "Re-enter PIN to confirm",
            "pin_save_success" to "Security PIN permanently locked successfully!",
            "pin_too_short" to "PIN must be at least 4 digits",
            "pin_mismatch" to "PINs do not match! Please re-enter.",
            "pin_card_title" to "Security PIN",
            "pin_card_desc_set" to "PIN is permanently locked. Once set, this PIN cannot be changed.",
            "pin_card_desc_not_set" to "No PIN set. Set a permanent security PIN now to protect your settings.",
            "pin_card_btn_set" to "Permanently Locked",
            "pin_card_btn_not_set" to "Set PIN",
            "pin_already_set_alert" to "Security PIN is permanently locked. Once set, this PIN cannot be changed.",
            "vpn_card_title" to "Built-in VPN Shield (Block 18+)",
            "vpn_card_desc" to "Routes traffic locally to filter adult content with AdGuard DNS. Does not leak your data.",
            "vpn_card_btn_connected" to "VPN Shield Connected",
            "vpn_card_btn_activate" to "Activate VPN Shield",
            "guard_card_title" to "Settings Guard (Accessibility)",
            "guard_card_desc_active" to "Guard is active. Settings and bypass attempts are monitored and blocked.",
            "guard_card_desc_inactive" to "Your hard-earned clean streak can be bypassed! Enable settings guard now.",
            "guard_card_btn_active" to "Settings Guard Active",
            "guard_card_btn_inactive" to "Enable Settings Guard",
            "admin_card_title" to "Permanent Anti-Uninstall (Device Admin)",
            "admin_card_desc_active" to "OS-Level Unbreakable Lock active. The system completely blocks uninstallation.",
            "admin_card_desc_inactive" to "Lock uninstallation at Android OS level. Never turns off automatically after 1 day or reboot.",
            "admin_card_btn_active" to "Anti-Uninstall Active (OS Lock)",
            "admin_card_btn_inactive" to "Enable Anti-Uninstall",
            "battery_card_title" to "Keep Protection Active 24/7",
            "battery_card_desc_active" to "Battery optimization disabled. Android will not sleep or kill your protection.",
            "battery_card_desc_inactive" to "Avoid auto turn-off! Disable battery optimization so Android never stops your protection after 1 day.",
            "battery_card_btn_active" to "24/7 Active (Unrestricted)",
            "battery_card_btn_inactive" to "Disable Battery Restriction",
            "error_set_pin" to "Please set Security PIN first.",
            "error_enable_vpn" to "Please enable VPN Shield first.",
            "welcome_warrior" to "Welcome, Warrior",
            "quote_desc" to "Keep your character resolution unbroken.",
            "emergency" to "EMERGENCY BUTTON",
            "salah_tracker_btn" to "Salah Tracker",
            "streak_card_title" to "YOUR PURITY STREAK",
            "days" to "Days",
            "hours" to "Hours",
            "minutes" to "Minutes",
            "seconds" to "Seconds",
            "current_badge" to "Current Rank",
            "next_badge_in" to "Next: %s (only %d days left)",
            "relapsed_btn" to "Dismissed / Relapsed? Reset",
            "set_custom_date" to "Reset to Specific Date/Time",
            "reset_streak" to "Reset Purity Tracker",
            "confirm_reset_title" to "Have you relapsed?",
            "confirm_reset_desc" to "This will reset your current clean streak to 0 or another previous date. Be honest with yourself, warrior!",
            "reset_now" to "Yes, Reset Right Now",
            "choose_past_date" to "Select Previous Date & Time",
            "cancel" to "Cancel",
            "focus_mode_title" to "Focus & Protect Shield",
            "status_active" to "PROTECTION ACTIVE",
            "status_inactive" to "PROTECTION DEACTIVATED",
            "activate_shield" to "Activate Protection Shield",
            "guardian_shield" to "Guardian Shield Protection",
            "guardian_desc" to "Fill in the details below. We will soon activate automated alerts. For now, keep your guardian's email saved here!",
            "your_name" to "Your Name *",
            "guardian_name" to "Guardian/Teacher Name *",
            "guardian_email" to "Guardian Email *",
            "phone_optional" to "Phone Number (Optional)",
            "save_guardian" to "Save Guardian Status",
            "login_google" to "Log In with Google",
            "or" to "OR",
            "forgot_pw" to "Forgot Password? 🔑",
            "not_registered" to "New here? Create Account",
            "login_with_link" to "Get Passwordless Login Link",
            "get_started" to "Get Started",
            "register" to "Register",
            "email_hint" to "Email Address *",
            "password_hint" to "Password *",
            "name_hint" to "Full Name *",
            "logout" to "Log Out",
            "admin_badge" to "Admin: Manage Badges",
            "alert_warning" to "Defense Alert! Non-Blocked Device.",
            "alert_warning_desc" to "Please scroll down to enable VPN Shield and Settings Guard for 18+ content filters!",
            "ai_chat_title" to "Islamic AI Guide",
            "ai_welcome" to "Assalamu alaikum! Ask me anything about character development, self-control, Islamic teachings, or clean lifestyle habits.",
            "chat_hint" to "Write your message here...",
            "send" to "Send",
            "salah_tracker_title" to "Daily Prayers Tracker",
            "location_permission_desc" to "To construct the exact prayer timings for your region, please enable the location permission here, or you will be able to manually specify in future updates.",
            "activate_location" to "Enable Location",
            "time_until_prefix" to "Time remaining for",
            "current_waqt_prefix" to "Current Waqt:",
            "sunrise" to "Sunrise",
            "forbidden_sunrise" to "Forbidden (Sunrise)",
            "forbidden_zawaal" to "Forbidden (Zawaal)",
            "forbidden_sunset" to "Forbidden (Sunset)",
            "forbidden_time" to "Forbidden Time",
            "current_time_prefix" to "Current Time",
            "rakah_btn" to "Rakah",
            "next_waqt_prefix" to "Next Waqt:",
            "all_prayers" to "All Prayers",
            "location_label" to "Location",
            "fajr" to "Fajr",
            "dhuhr" to "Dhuhr",
            "asr" to "Asr",
            "maghrib" to "Maghrib",
            "isha" to "Isha",
            "salah_desc" to "Keep track of your 5 daily obligatory prayers. Achieving purity is built on spiritual discipline.",
            "community_title" to "Global Warrior Hub",
            "create_post_hint" to "Share your recovery journey, struggles, or tips with the community...",
            "post_btn" to "Post Status",
            "leaderboard_title" to "Top Warriors Leaderboard",
            "anonymous" to "Anonymous",
            "comments" to "Comments",
            "add_comment_hint" to "Write a comment of support...",
            "stop_timer" to "Stop Timer",
            "reset" to "Reset",
            "all_badges" to "All Badges >"
        ),
        "bn" to mapOf(
            "nav_home" to "হোম",
            "nav_history" to "ইতিহাস",
            "nav_focus" to "ফোকাস",
            "nav_ai" to "এআই চ্যাট",
            "nav_salah" to "সালাত",
            "nav_community" to "কমিউনিটি",
            "nav_profile" to "প্রোফাইল",
            "dns_dialog_title" to "কীভাবে DNS সেট করবেন?",
            "dns_dialog_desc" to "অ্যাডাল্ট সাইট ব্লক করার জন্য আপনাকে ফোনের সেটিংসে 'Private DNS' পরিবর্তন করতে হবে।",
            "dns_step1" to "১. আমরা 'family.adguard-dns.com' লেখাটি কপি করেছি।",
            "dns_step2" to "২. 'সেটিংসে যান' বাটনে ক্লিক করলে আপনার ফোনের সেটিংস খুলবে।",
            "dns_step3" to "৩. সেখানে 'Private DNS' বা 'প্রাইভেট ডিএনএস' লিখে সার্চ দিন।",
            "dns_step4" to "৪. 'Private DNS provider hostname' সিলেক্ট করে কপি করা লেখাটি পেস্ট করে সেভ করুন।",
            "dns_alternative" to "বিকল্প সমাধান: আপনি যদি DNS ব্যবহার করতে না চান বা কাজ না করে, তবে আপনি গুগল প্লে-স্টোর থেকে 'BlockerX', 'Bulldog' বা 'OpenDNS' রাউটার সেটিংস ব্যবহার করতে পারেন।",
            "go_to_settings" to "সেটিংসে যান",
            "dismiss" to "বাদ দিন",
            "pin_dialog_title_change" to "নিরাপত্তা পিন",
            "pin_dialog_title_set" to "স্থায়ী নিরাপত্তা পিন সেট করুন",
            "pin_dialog_advice" to "⚠️ সতর্কবার্তা: এই পিনটি একবার সেট করার পর আর কখনোই পরিবর্তন বা রিসেট করা যাবে না! তাই মনে রাখার মতো বা কোনো বিশ্বস্ত ব্যক্তির মাধ্যমে ৪-৮ সংখ্যার পিন সেট করুন।",
            "pin_dialog_enter" to "৪ ডিজিটের পিন লিখুন",
            "pin_dialog_confirm" to "পিনটি পুনরায় লিখুন (নিশ্চিতকরণ)",
            "pin_save_success" to "পিন সফলভাবে স্থায়ীভাবে লক করা হয়েছে!",
            "pin_too_short" to "পিন কমপক্ষে ৪ ডিজিটের হতে হবে",
            "pin_mismatch" to "পিন দুটি মেলেনি! সঠিকভাবে পুনরায় লিখুন।",
            "pin_card_title" to "নিরাপত্তা পিন",
            "pin_card_desc_set" to "পিন সুরক্ষিত আছে। পিন একা পরিবর্তন করা যাবে না, শুধুমাত্র অভিভাবকের কি (Key) দিয়ে পরিবর্তন সম্ভব।",
            "pin_card_desc_not_set" to "কোনো পিন সেট করা নেই। প্রটেকশন সুরক্ষিত রাখতে এখনই একটি স্থায়ী পিন সেট করুন।",
            "pin_card_btn_set" to "স্থায়ীভাবে লকড",
            "pin_card_btn_not_set" to "পিন সেট করুন",
            "pin_already_set_alert" to "নিরাপত্তা পিন স্থায়ীভাবে লকড। পিন পরিবর্তন করতে অভিভাবকের কি (Key) প্রয়োজন।",
            "vpn_card_title" to "বিল্ট-ইন VPN শিল্ড (১৮+ ব্লক)",
            "vpn_card_desc" to "অ্যাডাল্ট কন্টেন্ট ফিল্টার করতে AdGuard DNS ব্যবহার করে ট্রাফিক রিডাইরেক্ট করে। কোনো বাহ্যিক অ্যাপ লাগবে না।",
            "vpn_card_btn_connected" to "VPN কানেক্টেড",
            "vpn_card_btn_activate" to "VPN শিল্ড চালু করুন",
            "guard_card_title" to "সেটিংস গার্ড (অ্যাক্সেসিবিলিটি সুরক্ষা)",
            "guard_card_desc_active" to "গার্ড সচল আছে। সেটিংস ও বাইপাসের প্রচেষ্টা স্বয়ংক্রিয়ভাবে ব্লক করা হচ্ছে।",
            "guard_card_desc_inactive" to "আপনার কঠোর পরিশ্রমের স্ট্রিকটি নিরাপদ রাখতে সেটিংস গার্ড চালু করুন।",
            "guard_card_btn_active" to "সেটিংস গার্ড সচল",
            "guard_card_btn_inactive" to "সেটিংস গার্ড চালু করুন",
            "admin_card_title" to "স্থায়ী অ্যান্টি-আনইনস্টল (ডিভাইস অ্যাডমিন)",
            "admin_card_desc_active" to "অপারেটিং সিস্টেম লেভেলে আনইনস্টল সম্পূর্ণ লকড। ফোন থেকে কোনোভাবেই অ্যাপ আনইনস্টল করা যাবে না।",
            "admin_card_desc_inactive" to "ডিভাইস অ্যাডমিন সক্রিয় করুন যাতে ১ দিন পর বা রিস্টার্টের পরেও আনইনস্টল সুরক্ষা কখনো অটো অফ না হয়।",
            "admin_card_btn_active" to "অ্যান্টি-আনইনস্টল সক্রিয় (OS লক)",
            "admin_card_btn_inactive" to "অ্যান্টি-আনইনস্টল চালু করুন",
            "battery_card_title" to "২৪/৭ সর্বদা সচল রাখুন (অটো অফ রোধ)",
            "battery_card_desc_active" to "ব্যাটারি অপ্টিমাইজেশন বন্ধ করা আছে। অ্যান্ড্রয়েড বা ফোন ক্লিনার গার্ড কখনো বন্ধ করবে না।",
            "battery_card_desc_inactive" to "১ দিন পর অটো অফ হওয়া বন্ধ করতে ব্যাটারি অপ্টিমাইজেশন বন্ধ রাখুন, যাতে গার্ড ২৪/৭ আজীবন চালু থাকে।",
            "battery_card_btn_active" to "২৪/৭ সচল রয়েছে (সীমাহীন)",
            "battery_card_btn_inactive" to "ব্যাটারি সীমাবদ্ধতা বন্ধ করুন",
            "error_set_pin" to "দয়া করে আগে Security PIN সেট করুন।",
            "error_enable_vpn" to "দয়া করে আগে VPN শিল্ড চালু করুন।",
            "welcome_warrior" to "স্বাগতম, যোদ্ধা",
            "quote_desc" to "আপনার চারিত্রিক সঙ্কল্প অটুট রাখুন।",
            "emergency" to "জরুরী বাটন",
            "salah_tracker_btn" to "সালাহ ট্র্যাকার",
            "streak_card_title" to "আপনার নিষ্কলুষ দিনগুলো",
            "days" to "দিন",
            "hours" to "ঘণ্টা",
            "minutes" to "মিনিট",
            "seconds" to "সেকেন্ড",
            "current_badge" to "বর্তমান র‍্যাংক",
            "next_badge_in" to "পরবর্তী: %s (আর মাত্র %d দিন বাকি)",
            "relapsed_btn" to "ভুল ত্রুটি হয়েছে? রিসেট করুন",
            "set_custom_date" to "নির্দিষ্ট সময় থেকে শুরু করুন",
            "reset_streak" to "টাইমার রিসেট করুন",
            "confirm_reset_title" to "আপনি কি সত্যি হেরে গেছেন?",
            "confirm_reset_desc" to "এটি আপনার বর্তমান রেকর্ড রিসেট করে ০ বা পূর্ববর্তী কোনো সময়ে নিয়ে যাবে। নিজের সাথে সৎ থাকুন, যোদ্ধা!",
            "reset_now" to "হ্যাঁ, এখনই রিসেট করুন",
            "choose_past_date" to "আগের তারিখ ও সময় নির্বাচন করুন",
            "cancel" to "বাতিল",
            "focus_mode_title" to "ফোকাস এবং সুরক্ষা শিল্ড",
            "status_active" to "সুরক্ষা কবচ সক্রিয় রয়েছে",
            "status_inactive" to "সুরক্ষা কবচ বন্ধ রয়েছে",
            "activate_shield" to "সুরক্ষা কবচ সচল করুন",
            "guardian_shield" to "গার্ডিয়ান প্রটেকশন শিল্ড",
            "guardian_desc" to "আপনার অভিভাবক বা শিক্ষকের মেইলটি এখানে সেভ করুন। পরবর্তীতে অটোমেটিক ইমেইল এলার্ট সিস্টেম সচল করতে এটি প্রয়োজন হবে!",
            "your_name" to "আপনার নাম *",
            "guardian_name" to "অভিভাবকের নাম *",
            "guardian_email" to "অভিভাবকের ইমেইল *",
            "phone_optional" to "অভিভাবকের ফোন নম্বর (ঐচ্ছিক)",
            "save_guardian" to "গার্জিয়ান স্ট্যাটাস সেভ করুন",
            "login_google" to "গুগল দিয়ে লগইন করুন",
            "or" to "অথবা",
            "forgot_pw" to "পাসওয়ার্ড ভুলে গেছেন? 🔑",
            "not_registered" to "নতুন এখানে? একাউন্ট তৈরি করুন",
            "login_with_link" to "লিংক দিয়ে পাসওয়ার্ডহীন লগইন",
            "get_started" to "শুরু করুন",
            "register" to "নিবন্ধন করুন",
            "email_hint" to "ইমেইল এড্রেস *",
            "password_hint" to "পাসওয়ার্ড *",
            "name_hint" to "পূর্ণ নাম *",
            "logout" to "লগ আউট",
            "admin_badge" to "অ্যাডমিন: ব্যাজ পরিচালনা",
            "alert_warning" to "নিরাপত্তা সতর্কতা! অরক্ষিত ডিভাইস।",
            "alert_warning_desc" to "১৮+ কন্টেন্ট ব্লক নিশ্চিত করতে নিচের দিকে স্ক্রোল করে VPN Shield এবং Settings Guard সচল করুন!",
            "ai_chat_title" to "ইসলামিক এআই গাইড",
            "ai_welcome" to "আসসালামু আলাইকুম! আত্মশুদ্ধি, চরিত্র গঠন, ইসলামিক শিক্ষা বা নিয়মানুবর্তিতা নিয়ে যেকোনো প্রশ্ন করুন।",
            "chat_hint" to "আপনার বার্তা এখানে লিখুন...",
            "send" to "পাঠান",
            "salah_tracker_title" to "সালাত ট্র্যাকার",
            "location_permission_desc" to "আপনার জেলার সঠিক নামাজের সময় সেট করতে এখানের লোকেশন এলাও সিস্টেমটি এক্টিভ করেন বা সেখানে মেনুয়ালি লিখে দিন।",
            "activate_location" to "লোকেশন সচল করুন",
            "time_until_prefix" to "বাকি আর",
            "current_waqt_prefix" to "বর্তমান ওয়াক্ত:",
            "sunrise" to "সূর্যোদয়",
            "forbidden_sunrise" to "নিষিদ্ধ সময় (সূর্যোদয়)",
            "forbidden_zawaal" to "নিষিদ্ধ সময় (যাওয়াল)",
            "forbidden_sunset" to "নিষিদ্ধ সময় (সূর্যাস্ত)",
            "forbidden_time" to "নিষিদ্ধ সময়",
            "current_time_prefix" to "এখন সময়",
            "rakah_btn" to "রাকাত",
            "next_waqt_prefix" to "পরবর্তী ওয়াক্ত:",
            "all_prayers" to "সকল নামাজ",
            "location_label" to "আপনার বর্তমান অবস্থান",
            "fajr" to "ফজর",
            "dhuhr" to "যোহর",
            "asr" to "আসর",
            "maghrib" to "মাগরিব",
            "isha" to "এশা",
            "salah_desc" to "আপনার ৫ ওয়াক্ত ফরজ সালাত ট্র্যাক করুন। আত্মশুদ্ধি অর্জনের মূল ভিত্তি সালাতের নিয়মানুবর্তিতা।",
            "community_title" to "গ্লোবাল যোদ্ধা হাব",
            "create_post_hint" to "আপনার সুস্থ হওয়ার যাত্রা বা যেকোনো টিপস কমিউনিটিতে শেয়ার করুন...",
            "post_btn" to "পোস্ট করুন",
            "leaderboard_title" to "শীর্ষ যোদ্ধাদের লিডারবোর্ড",
            "anonymous" to "নামহীন যোদ্ধা",
            "comments" to "মন্তব্য সমূহ",
            "add_comment_hint" to "যোদ্ধাকে সমর্থন দিয়ে মন্তব্য করুন...",
            "stop_timer" to "টাইমার থামান",
            "reset" to "রিসেট",
            "all_badges" to "সমস্ত ব্যাজ >"
        ),
        "ar" to mapOf(
            "nav_home" to "الرئيسية",
            "nav_history" to "السجل",
            "nav_focus" to "التركيز",
            "nav_ai" to "الذكاء الاصطناعي",
            "nav_community" to "المجتمع",
            "nav_profile" to "الحساب",
            "welcome_warrior" to "أهلاً بك يا محارب",
            "quote_desc" to "حافظ على عهدك وثبات شخصيتك دائماً.",
            "emergency" to "زر الطوارئ",
            "salah_tracker_btn" to "متابع الصلاة",
            "streak_card_title" to "فترة نقائك الحالية",
            "days" to "أيام",
            "hours" to "ساعات",
            "minutes" to "دقائق",
            "seconds" to "ثواني",
            "current_badge" to "الرتبة الحالية",
            "next_badge_in" to "الرتبة التالية: %s (متبقي %d يوم)",
            "relapsed_btn" to "هل تعثرت؟ إعادة تعيين",
            "set_custom_date" to "البدء من وقت محدد",
            "reset_streak" to "إعادة تعيين المؤقت",
            "confirm_reset_title" to "هل سقطت فعلاً؟",
            "confirm_reset_desc" to "سيتم إعادة مؤقتك الحالي إلى 0 أو تاريخ سابق. كن صادقاً مع نفسك يا محارب!",
            "reset_now" to "نعم، أعد التعيين الآن",
            "choose_past_date" to "اختر تاريخ ووقت سابق",
            "cancel" to "إلغاء",
            "focus_mode_title" to "درع التركيز والحماية",
            "status_active" to "درع الحماية نشط",
            "status_inactive" to "درع الحماية معطل",
            "activate_shield" to "تفعيل درع الحماية",
            "guardian_shield" to "درع حماية الوصي",
            "guardian_desc" to "أدخل تفاصيل الوصي أو المعلم لحفظها هنا وتفعيل نظام التنبيهات لاحقاً!",
            "your_name" to "اسمك بالكامل *",
            "guardian_name" to "اسم الوصي/المعلم *",
            "guardian_email" to "البريد الإلكتروني للوصي *",
            "phone_optional" to "رقم الهاتف للوصي (اختياري)",
            "save_guardian" to "حفظ بيانات الوصي",
            "login_google" to "تسجيل الدخول بجوجل",
            "or" to "أو",
            "forgot_pw" to "نسيت كلمة المرور؟ 🔑",
            "not_registered" to "جديد هنا؟ إنشاء حساب",
            "login_with_link" to "تسجيل دخول بدون كلمة مرور",
            "get_started" to "البدء الآن",
            "register" to "تسجيل الآن",
            "email_hint" to "البريد الإلكتروني *",
            "password_hint" to "كلمة المرور *",
            "name_hint" to "الاسم الكامل *",
            "logout" to "تسجيل الخروج",
            "admin_badge" to "الإشراف: إدارة الرتب",
            "alert_warning" to "تنبيه أمني! جهاز غير محمي.",
            "alert_warning_desc" to "يرجى تفعيل درع VPN وحماية الإعدادات لحجب المحتوى الإباحي!",
            "ai_chat_title" to "مرشد الذكاء الاصطناعي الإسلامي",
            "ai_welcome" to "السلام عليكم! اسألني عن تهذيب النفس والتربية الإسلامية وبناء العادات الصالحة.",
            "chat_hint" to "اكتب رسالتك هنا...",
            "send" to "إرسال",
            "salah_tracker_title" to "متعقب الصلوات الخمس",
            "fajr" to "الفجر",
            "dhuhr" to "الظهر",
            "asr" to "العصر",
            "maghrib" to "المغرب",
            "isha" to "العشاء",
            "salah_desc" to "حافظ على صلواتك الخمس المفروضة يومياً. فالنقاء والاستقامة أساسهما الانضباط الروحي.",
            "community_title" to "ملتقى المحاربين العالمي",
            "create_post_hint" to "شارك رحلتك في التعافي أو قدم نصائح للمجتمع...",
            "post_btn" to "نشر الحالة",
            "leaderboard_title" to "قائمة شرف المحاربين",
            "anonymous" to "محارب مجهول",
            "comments" to "التعليقات",
            "add_comment_hint" to "اكتب تعليقاً داعماً ومحفزاً...",
            "stop_timer" to "إيقاف المؤقت",
            "reset" to "إعادة تعيين",
            "all_badges" to "كل الأوسمة >"
        ),
        "ur" to mapOf(
            "nav_home" to "ہوم",
            "nav_history" to "تاریخ",
            "nav_focus" to "فوکس",
            "nav_ai" to "چیٹ",
            "nav_community" to "کمیونٹی",
            "nav_profile" to "پروفائل",
            "welcome_warrior" to "سلام، اے جنگجو",
            "quote_desc" to "اپنے اخلاقی مقصد اور عزم کو مضبوط رکھیں۔",
            "emergency" to "ہنگامی بٹن",
            "salah_tracker_btn" to "نماز ٹریکر",
            "streak_card_title" to "آپ کا پاکیزگی کا تسلسل",
            "days" to "دن",
            "hours" to "گھنٹے",
            "minutes" to "منٹ",
            "seconds" to "سیکنڈ",
            "current_badge" to "موجودہ رینک",
            "next_badge_in" to "اگلی رینک: %s (صرف %d دن باقی)",
            "relapsed_btn" to "غلطی ہوئی؟ دوبارہ شروع کریں",
            "set_custom_date" to "مخصوص وقت سے سیٹ کریں",
            "reset_streak" to "ٹائمر بند کریں",
            "confirm_reset_title" to "کیا آپ واقعی ہار گئے؟",
            "confirm_reset_desc" to "یہ آپ کے تسلسل کو 0 یا ماقبل تاریخ پر لے جائے گا۔ خود سے سچے رہیں، جنگجو!",
            "reset_now" to "جی ہاں، ابھی ری سیٹ کریں",
            "choose_past_date" to "پچھلی تاریخ اور وقت منتخب کریں",
            "cancel" to "منسوخ کریں",
            "focus_mode_title" to "فوکس اور پروٹیکشن شیلڈ",
            "status_active" to "حفاظتی شیلڈ فعال ہے",
            "status_inactive" to "حفاظتی شیلڈ غیر فعال ہے",
            "activate_shield" to "حفاظتی شیلڈ شروع کریں",
            "guardian_shield" to "سرپرست پروٹیکشن شیلڈ",
            "guardian_desc" to "اپنے سرپرست یا استاد کا ای میل یہاں محفوظ رکھیں۔ جلد ہی خودکار الرٹ سسٹم شروع کیا جائے گا!",
            "your_name" to "آپ کا نام *",
            "guardian_name" to "سرپرست کا نام *",
            "guardian_email" to "سرپرست کا ای میل *",
            "phone_optional" to "فون نمبر (اختیاری)",
            "save_guardian" to "سرپرست کی معلومات محفوظ کریں",
            "login_google" to "گوگل سے لاگ ان کریں",
            "or" to "یا",
            "forgot_pw" to "پاس ورڈ بھول گئے؟ 🔑",
            "not_registered" to "نیا اکاؤنٹ بنائیں",
            "login_with_link" to "بغیر پاس ورڈ ای میل لاگ ان",
            "get_started" to "شروع کریں",
            "register" to "رجسٹریشن کریں",
            "email_hint" to "ای میل پتہ *",
            "password_hint" to "پاس ورڈ *",
            "name_hint" to "پورا نام *",
            "logout" to "لاگ آؤٹ",
            "admin_badge" to "ایڈمن: بیجز مینیجر",
            "alert_warning" to "سیکیورٹی الرٹ! غیر محفوظ ڈیوائس۔",
            "alert_warning_desc" to "براہ کرم نیچے سکرول کریں اور پیج فلٹرز کے لیے وی پی این اور سیٹنگ گارڈ فعال کریں!",
            "ai_chat_title" to "اسلامی روحاتی رہنما",
            "ai_welcome" to "السلام علیکم! خود اعتمادی، کردار سازی اور پاکیزہ طرز زندگی کے بارے میں کوئی بھی سوال پوچھیں۔",
            "chat_hint" to "اپنا پیغام یہاں لکھیں...",
            "send" to "بھیجیں",
            "salah_tracker_title" to "نماز ٹریکر",
            "fajr" to "فجر",
            "dhuhr" to "ظہر",
            "asr" to "عصر",
            "maghrib" to "مغرب",
            "isha" to "عشاء",
            "salah_desc" to "پانچ وقت فرض نمازوں کو ٹریک کریں۔ کیونکہ کردار سازی کا اصل راز نماز کی پابندی میں پوشیدہ ہے۔",
            "community_title" to "عالمی جنگجو فورم",
            "create_post_hint" to "اپنا بحالی کا سفر یا اصلاحی باتیں کمیونٹی میں شیئر کریں...",
            "post_btn" to "پوسٹ اپ لوڈ کریں",
            "leaderboard_title" to "اعلیٰ ترین جنگجوؤں کا لیڈر بورڈ",
            "anonymous" to "گمنام جنگجو",
            "comments" to "تبصرے",
            "add_comment_hint" to "جنگجو کی حوصلہ افزائی کے لئے تبصرہ لکھیں...",
            "stop_timer" to "ٹائمر بند کریں",
            "reset" to "ری سیٹ",
            "all_badges" to "تمام بیجز >"
        ),
        "hi" to mapOf(
            "nav_home" to "होम",
            "nav_history" to "इतिहास",
            "nav_focus" to "फोकस",
            "nav_ai" to "एआई चैट",
            "nav_community" to "कम्युनिटी",
            "nav_profile" to "प्रोफाइल",
            "welcome_warrior" to "स्वागत है, योद्धा",
            "quote_desc" to "अपने चारित्रिक संकल्प को हमेशा अटूट रखें।",
            "emergency" to "आपातकालीन बटन",
            "salah_tracker_btn" to "प्रार्थना ट्रैकर",
            "streak_card_title" to "आपका पवित्रता का सफर",
            "days" to "दिन",
            "hours" to "घंटे",
            "minutes" to "मिनट",
            "seconds" to "सेकंड",
            "current_badge" to "वर्तमान रैंक",
            "next_badge_in" to "अगली रैंक: %s (केवल %d दिन शेष)",
            "relapsed_btn" to "कोई भूल हुई? रीसेट करें",
            "set_custom_date" to "विशिष्ट समय से टाइमर सेट करें",
            "reset_streak" to "टाइमर रीसेट करें",
            "confirm_reset_title" to "क्या आप हार मान चुके हैं?",
            "confirm_reset_desc" to "यह आपके वर्तमान रिकॉर्ड को रीसेट कर 0 या पूर्व तिथि पर ला देगा। खुद से सच्चे रहें, योद्धा!",
            "reset_now" to "हाँ, अभी रीसेट करें",
            "choose_past_date" to "पिछली तारीख और समय चुनें",
            "cancel" to "रद्द करें",
            "focus_mode_title" to "फोकस और सुरक्षा कवच",
            "status_active" to "सुरक्षा कवच सक्रिय है",
            "status_inactive" to "सुरक्षा कवच बंद है",
            "activate_shield" to "सुरक्षा कवच सक्रिय करें",
            "guardian_shield" to "अभिभावक सुरक्षा कवच",
            "guardian_desc" to "अपने अभिभावक या शिक्षक का ईमेल दर्ज करें। जल्द ही हम स्वचालित ईमेल अलर्ट सिस्टम शुरू करेंगे!",
            "your_name" to "आपका नाम *",
            "guardian_name" to "अभिभावक/शिक्षक का नाम *",
            "guardian_email" to "अभिभावक का ईमेल *",
            "phone_optional" to "फ़ोन नंबर (वैकल्पिक)",
            "save_guardian" to "अभिभावक जानकारी सहेजें",
            "login_google" to "गूगल से लॉगिन करें",
            "or" to "अथवा",
            "forgot_pw" to "पासवर्ड भूल गए? 🔑",
            "not_registered" to "नए हैं? खाता बनाएं",
            "login_with_link" to "बिना पासवर्ड ईमेल लॉगिन",
            "get_started" to "शुरू करें",
            "register" to "रजिस्टर करें",
            "email_hint" to "ईमेल एड्रेस *",
            "password_hint" to "पासवर्ड *",
            "name_hint" to "पूरा नाम *",
            "logout" to "लॉग आउट",
            "admin_badge" to "एडमिन: बैज प्रबंधन",
            "alert_warning" to "सुरक्षा चेतावनी! असुरक्षित डिवाइस।",
            "alert_warning_desc" to "18+ कंटेंट ब्लॉकिंग के लिए नीचे स्क्रॉल करें और वीपीएन तथा सेटिंग्स गार्ड को सक्रिय करें!",
            "ai_chat_title" to "इस्लामी एआई मार्गदर्शक",
            "ai_welcome" to "अस्सलाम वालेकुम! चरित्र विकास, आत्म-नियंत्रण, या स्वस्थ आदतों के बारे में कोई भी प्रश्न पूछें।",
            "chat_hint" to "अपना संदेश यहाँ लिखें...",
            "send" to "भेजें",
            "salah_tracker_title" to "प्रार्थना (सालाह) ट्रैकर",
            "fajr" to "फज्र",
            "dhuhr" to "ज़ुहर",
            "asr" to "असर",
            "maghrib" to "मगरिब",
            "isha" to "ईशा",
            "salah_desc" to "अपनी 5 दैनिक प्रार्थनाओं को ट्रैक करें। पवित्रता और आत्मशुद्धि का आधार सच्ची प्रार्थना में ही है।",
            "community_title" to "वैश्विक योद्धा हब",
            "create_post_hint" to "अपनी रिकवरी यात्रा या सुधार से जुड़े सुझाव कम्युनिटी में साझा करें...",
            "post_btn" to "पोस्ट साझा करें",
            "leaderboard_title" to "शीर्ष योद्धाओं का लीडरबोर्ड",
            "anonymous" to "अनाम योद्धा",
            "comments" to "टिप्पणियाँ",
            "add_comment_hint" to "योद्धा का समर्थन करने के लिए टिप्पणी लिखें...",
            "stop_timer" to "टाइमर रोकें",
            "reset" to "रीसेट",
            "all_badges" to "सभी बैज >"
        ),
        "es" to mapOf(
            "nav_home" to "Inicio",
            "nav_history" to "Historial",
            "nav_focus" to "Enfoque",
            "nav_ai" to "Chat IA",
            "nav_community" to "Comunidad",
            "nav_profile" to "Cuenta",
            "welcome_warrior" to "Bienvenido, Guerrero",
            "quote_desc" to "Mantén tu resolución de carácter intacta.",
            "emergency" to "BOTÓN DE EMERGENCIA",
            "salah_tracker_btn" to "Rastreador de Oración",
            "streak_card_title" to "TU RACHA DE PUREZA",
            "days" to "Días",
            "hours" to "Horas",
            "minutes" to "Minutos",
            "seconds" to "Segundos",
            "current_badge" to "Rango Actual",
            "next_badge_in" to "Siguiente: %s (solo quedan %d días)",
            "relapsed_btn" to "¿Tropezaste? Reiniciar racha",
            "set_custom_date" to "Reiniciar racha desde fecha anterior",
            "reset_streak" to "Reiniciar Temporizador",
            "confirm_reset_title" to "¿Has recaído?",
            "confirm_reset_desc" to "Esto restablecerá tu racha limpia actual a 0 o a otra fecha anterior. ¡Sé honesto contigo mismo, guerrero!",
            "reset_now" to "Sí, reiniciar ahora mismo",
            "choose_past_date" to "Seleccionar fecha y hora anteriores",
            "cancel" to "Cancelar",
            "focus_mode_title" to "Escudo de Enfoque y Protección",
            "status_active" to "PROTECCIÓN ACTIVA",
            "status_inactive" to "PROTECCIÓN DESACTIVADA",
            "activate_shield" to "Activar Escudo de Protección",
            "guardian_shield" to "Escudo del Tutor/Guardián",
            "guardian_desc" to "Guarda el correo de tu tutor aquí. ¡Muy pronto activaremos alertas automáticas correo-electrónicas!",
            "your_name" to "Tu Nombre *",
            "guardian_name" to "Nombre del Tutor *",
            "guardian_email" to "Correo del Tutor *",
            "phone_optional" to "Teléfono del Tutor (Opcional)",
            "save_guardian" to "Guardar Configuración del Tutor",
            "login_google" to "Iniciar sesión con Google",
            "or" to "O",
            "forgot_pw" to "¿Olvidaste tu contraseña? 🔑",
            "not_registered" to "¿Nuevo aquí? Crear cuenta",
            "login_with_link" to "Obtener enlace de acceso sin contraseña",
            "get_started" to "Empezar",
            "register" to "Registrarse",
            "email_hint" to "Correo Electrónico *",
            "password_hint" to "Contraseña *",
            "name_hint" to "Nombre Completo *",
            "logout" to "Cerrar Sesión",
            "admin_badge" to "Admin: Gestor de Rangos",
            "alert_warning" to "¡Alerta de Seguridad! Dispositivo desprotegido.",
            "alert_warning_desc" to "¡Desplázate hacia abajo para activar el filtro de contenido para adultos VPN y Guard!",
            "ai_chat_title" to "Guía IA de Pureza",
            "ai_welcome" to "¡As-salamu alaykum! Pregúntame sobre desarrollo de carácter, autodisciplina y hábitos saludables.",
            "chat_hint" to "Escribe tu mensaje...",
            "send" to "Enviar",
            "salah_tracker_title" to "Rastreador de Oraciones Diarias",
            "fajr" to "Fajr",
            "dhuhr" to "Dhuhr",
            "asr" to "Asr",
            "maghrib" to "Maghrib",
            "isha" to "Isha",
            "salah_desc" to "Registra tus 5 oraciones diarias obligatorias. El camino a la autodisciplina pasa por la constancia.",
            "community_title" to "Foro Global de Guerreros",
            "create_post_hint" to "Comparte tu viaje de superación, progreso o consejos con la comunidad...",
            "post_btn" to "Publicar Estado",
            "leaderboard_title" to "Tabla de Líderes de Guerreros",
            "anonymous" to "Guerrero Anónimo",
            "comments" to "Comentarios",
            "add_comment_hint" to "Deja un comentario de apoyo...",
            "stop_timer" to "Detener Temporizador",
            "reset" to "Reiniciar",
            "all_badges" to "Todas las Medallas >"
        )
    )

    val additionalTranslations = mapOf(
        "en" to mapOf(
            "focus_mode_header" to "Focus Mode",
            "focus_desc_top" to "Eliminate all distractions and awaken your inner strength.",
            "focus_desc_bottom" to "Close your eyes to resist urges, and recall your guiding inner safe light.",
            "focus_ready" to "Ready",
            "focus_press_play" to "Press Play to Start",
            "focus_inhale" to "Inhale",
            "focus_hold" to "Hold",
            "focus_exhale" to "Exhale",
            "ai_welcome_text" to "Assalamu Alaikum! I am 'Noor AI' — your direct Islamic counselor and habit recovery assistant. Feel free to ask any question or share any struggle, and you will receive clear, direct guidance insha'Allah.",
            "ai_missing_key" to "Please go to AI Studio's 'Secrets' panel and set 'GEMINI_API_KEY' with your Gemini API key.",
            "ai_connection_error" to "Check your internet connection",
            "ai_thinking" to "Thinking...",
            "past_7_days_records" to "Past 7 Days Salah Records",
            "today_checklist" to "Today's Five Waqt Salah Checklist",
            "fajr_desc" to "Pre-Dawn Prayer",
            "dhuhr_desc" to "Noon Prayer",
            "asr_desc" to "Afternoon Prayer",
            "maghrib_desc" to "Sunset Prayer",
            "isha_desc" to "Night Prayer",
            "enable_alert_system" to "Enable Alert System",
            "alert_system_desc" to "If active, an email will be sent immediately upon a relapse attempt",
            "fill_required_fields" to "Please fill in all * marked fields.",
            "alert_active" to "Guardian Protection Shield Activated!",
            "alert_inactive" to "Guardian Shield Deactivated.",
            "upcoming_firebase" to "Upcoming Firebase Integration:",
            "upcoming_firebase_desc" to "For now, just save your guardian's email. In the future, a one-click Firebase triggers will enable automatic security notifications. This is an upcoming feature.",
            "save_guardian_success" to "Guardian status saved successfully!",
            "status_on" to "Active",
            "status_off" to "Inactive",
            "unprotected_inactive" to "Unprotected - Inactive",
            "upcoming_feature" to "Upcoming Feature",
            "milestones_title_large" to "All Milestones & Badges",
            "milestones_desc" to "Development of Self-Control",
            "post_progress" to "Post your progress",
            "motivational_board" to "Latest Motivational Board",
            "no_posts" to "Loading posts or no posts yet...",
            "days_streak" to "%d days streak",
            "listen" to "Listen",
            "copy_message" to "Copy message",
            "message_copied" to "Message copied to clipboard",
            "like" to "Like",
            "dislike" to "Dislike",
            "tab_islamic_ai" to "Noor AI Guide",
            "tab_salah_tracker" to "Salah Tracker",
            "badge_clown" to "Clown",
            "badge_noob" to "New Warrior",
            "badge_novice" to "Novice",
            "badge_average" to "Average",
            "badge_advanced" to "Advanced",
            "badge_sigma" to "Sigma",
            "badge_chad" to "Chad",
            "badge_absolute_chad" to "Absolute Chad",
            "badge_giga_chad" to "Giga Chad",
            "badge_abs_giga_chad" to "Absolute Giga Chad",
            "warn_title" to "Wait a moment, brother!",
            "warn_body" to "Shaytan is trying to provoke you. Did you forget the promise you made to yourself? A fleeting drop of momentary pleasure can take away the lifelong peace of your life.",
            "warn_quote" to "\"He who can control himself is the strongest person in the world.\"\n\nPlease do not turn off VPN, but meditate for just 15 minutes. After 15 minutes, these negative thoughts will exist no more! InshaAllah.",
            "warn_btn_meditate" to "Start Meditation 🧘",
            "warn_btn_resist" to "I will not give up",
            "forgot_password" to "Forgot Password? 🔑",
            "enter_email_first" to "Please enter your email first!",
            "reset_sent" to "Password reset link sent! 📧",
            "change_language" to "Change Language",
            "go_back" to "Go Back",
            "days_remaining" to "Only %d days remaining",
            "days_required" to "%d days required",
            "unlock_remaining" to "Only %d days remaining to unlock!",
            "start_where" to "Where to start?",
            "start_where_desc" to "Do you want to start counting right now, or select an older date of recovery?",
            "choose_old_date" to "Choose Old Date",
            "right_now" to "Right Now",
            "next_time" to "Next (Time)",
            "cancel" to "Cancel",
            "tab_global" to "Global Feed",
            "tab_challenges" to "Group Challenges",
            "challenge_participants" to "%d Participants",
            "challenges_joined" to "Joined",
            "challenges_available" to "Available Challenges",
            "join" to "Join",
            "leave" to "Leave",
            "chat_room" to "Challenge Chat",
            "chat_enter" to "Enter Chat 💬",
            "challenge_creator" to "By: %s",
            "group_challenge_title" to "Group & Peer Challenges",
            "create_challenge_btn" to "+ Create Challenge",
            "challenge_saved_success" to "Challenge created successfully!"
        ),
        "bn" to mapOf(
            "focus_mode_header" to "ফোকাস মোড",
            "focus_desc_top" to "সকল বিভ্রান্তি দূরে সরিয়ে নিজের ভেতরের শক্তিকে জাগিয়ে তুলুন।",
            "focus_desc_bottom" to "খারাপ ইচ্ছা প্রতিরোধ করতে চোখ বন্ধ করুন, এবং আপনার ভেতরের আলোর পথ স্মরণ করুন।",
            "focus_ready" to "প্রস্তুত",
            "focus_press_play" to "শুরু করতে প্লে বাটন চাপুন",
            "focus_inhale" to "নিঃশ্বাস নিন",
            "focus_hold" to "ধরে রাখুন",
            "focus_exhale" to "নিঃশ্বাস ছাড়ুন",
            "ai_welcome_text" to "আসসালামু আলাইকুম! আমি 'নূর AI' — আপনার আত্মশুদ্ধি, দ্বীনি জিজ্ঞাসা ও আসক্তি নিরাময় সহচর। যেকোনো প্রশ্ন বা সমস্যা নির্দ্বিধায় লিখুন, সরাসরি সমাধান পাবেন ইনশাআল্লাহ।",
            "ai_missing_key" to "দয়া করে এআই স্টুডিও এর 'Secrets' প্যানেলে গিয়ে 'GEMINI_API_KEY' নামে একটি ভেরিয়েবল তৈরি করে আপনার জেমিনি এপিআই-কী (API Key) সেট করুন।",
            "ai_connection_error" to "চেক আপনার ইন্টারনেট কানেকশন",
            "ai_thinking" to "উত্তর প্রস্তুত হচ্ছে...",
            "past_7_days_records" to "গত ৭ দিনের সালাত রেকর্ড",
            "today_checklist" to "আজকের ৫ ওয়াক্ত সালাত চেকলিস্ট",
            "fajr_desc" to "ভোরের সালাত",
            "dhuhr_desc" to "দুপুরের সালাত",
            "asr_desc" to "বকেক সালাত",
            "maghrib_desc" to "সন্ধ্যার সালাত",
            "isha_desc" to "রাতের সালাত",
            "enable_alert_system" to "এলার্ট সিস্টেম চালু করুন",
            "alert_system_desc" to "সক্রিয় থাকলে চেষ্টা করা মাত্র ইমেইল পাঠানো হবে",
            "fill_required_fields" to "অনুগ্রহ করে সকল * চিহ্নিত তথ্য পূরণ করুন।",
            "alert_active" to "গার্ডিয়ান প্রটেকশন শিল্ড সচল হয়েছে!",
            "alert_inactive" to "গার্ডিয়ান শিল্ড বন্ধ করা হয়েছে।",
            "upcoming_firebase" to "আসন্ন ফায়ারবেস ইন্টিগ্রেশন:",
            "upcoming_firebase_desc" to "আপাতত শুধু গার্জিয়ানের মেইল সেভ করে রাখুন। পরবর্তীতে এক ক্লিকেই ফায়ারবেস ট্রিগার ইমেইল এর মাধ্যমে সুরক্ষাকবচ সচল করা হবে। এটি একটি আপকামিং ফিচার।",
            "save_guardian_success" to "গার্জিয়ান স্ট্যাটাস সফলভাবে সেভ করা হয়েছে!",
            "status_on" to "চালু",
            "status_off" to "বন্ধ",
            "unprotected_inactive" to "অরক্ষিত - বন্ধ আছে",
            "upcoming_feature" to "আসন্ন ফিচার",
            "milestones_title_large" to "সকল মাইলস্টোন ও ব্যাজ",
            "milestones_desc" to "আত্ম-নিয়ন্ত্রণের উন্নতি",
            "post_progress" to "আপনার অগ্রগতি পোস্ট করুন",
            "motivational_board" to "সর্বশেষ মোটিভেশনাল বোর্ড",
            "no_posts" to "লোড করা হচ্ছে বা কোনো পোস্ট নেই...",
            "days_streak" to "%d দিনের স্ট্রিক",
            "listen" to "শুনুন",
            "copy_message" to "কপি করুন",
            "message_copied" to "মেসেজ কপি করা হয়েছে",
            "like" to "লাইক করুন",
            "dislike" to "ডিসলাইক করুন",
            "tab_islamic_ai" to "নূর AI সহচর",
            "tab_salah_tracker" to "সালাহ ট্র্যাকার",
            "badge_clown" to "জোকার",
            "badge_noob" to "নতুন যোদ্ধা",
            "badge_novice" to "নবিশ",
            "badge_average" to "মাঝারি",
            "badge_advanced" to "উন্নত",
            "badge_sigma" to "সিগমা",
            "badge_chad" to "চ্যাড",
            "badge_absolute_chad" to "পরম চ্যাড",
            "badge_giga_chad" to "গিগাচ্যাড",
            "badge_abs_giga_chad" to "পরম গিগাচ্যাড",
            "warn_title" to "একটু থামুন ভাই!",
            "warn_body" to "শয়তান আপনাকে প্ররোচিত করছে। আপনি নিজের কাছে যে প্রতিজ্ঞা করেছিলেন, তা কি ভুলে গেছেন? এক ফোঁটা সাময়িক আনন্দ আপনার জীবনের দীর্ঘস্থায়ী শান্তি কেড়ে নিতে পারে।",
            "warn_quote" to "\"যে নিজেকে নিয়ন্ত্রণ করতে পারে, সে দুনিয়ার সবচেয়ে শক্তিশালী মানুষ।\" \n\nদয়া করে ভিপিএন বন্ধ না করে মাত্র ১৫ মিনিট মেডিটেশন করুন। ১৫ মিনিট পর আপনার এই বাজে চিন্তা আর থাকবে না! ইনশাআল্লাহ।",
            "warn_btn_meditate" to "মেডিটেশন শুরু করুন 🧘",
            "warn_btn_resist" to "আমি হার মানবো না",
            "forgot_password" to "পাসওয়ার্ড ভুলে গেছেন? 🔑",
            "enter_email_first" to "অনুগ্রহ করে প্রথমে আপনার ইমেইল দিন",
            "reset_sent" to "পাসওয়ার্ড রিসেটের লিংক ইমেইলে পাঠানো হয়েছে 📧",
            "change_language" to "ভাষা পরিবর্তন",
            "go_back" to "ফিরে যান",
            "days_remaining" to "আর %d দিন বাকি",
            "days_required" to "%d দিন প্রয়োজনীয়",
            "unlock_remaining" to "আনলক করতে আর মাত্র %d দিন বাকি!",
            "start_where" to "কোথা থেকে শুরু করবেন?",
            "start_where_desc" to "আপনি কি একদম এখন থেকে গণনা শুরু করতে চান, নাকি পূর্বে শুরু করা কোনো তারিখ লিস্ট করতে চান?",
            "choose_old_date" to "পূর্বের তারিখ নির্বাচন করুন (Choose Old Date)",
            "right_now" to "এখন থেকে (Right Now)",
            "next_time" to "পরবর্তী (সময়)",
            "cancel" to "বাতিল",
            "tab_global" to "গ্লোবাল ফিড",
            "tab_challenges" to "গ্রুপ চ্যালেঞ্জ 👥",
            "challenge_participants" to "%d জন অংশ নিয়েছেন",
            "challenges_joined" to "যুক্ত আছেন",
            "challenges_available" to "উপলব্ধ চ্যালেঞ্জসমূহ",
            "join" to "যুক্ত হোন",
            "leave" to "বাদ দিন",
            "chat_room" to "চ্যালেঞ্জ গ্রুপ চ্যাট",
            "chat_enter" to "গ্রুপ চ্যাট 💬",
            "challenge_creator" to "তৈরি করেছেন: %s",
            "group_challenge_title" to "সমমনাদের গ্রুপ চ্যালেঞ্জ",
            "create_challenge_btn" to "+ চ্যালেঞ্জ তৈরি করুন",
            "challenge_saved_success" to "চ্যালেঞ্জটি সফলভাবে তৈরি হয়েছে!"
        ),
        "ar" to mapOf(
            "focus_mode_header" to "وضع التركيز",
            "focus_desc_top" to "اقضِ على جميع المشتتات وأيقظ قوتك الداخلية.",
            "focus_desc_bottom" to "غمض عينيك لمقاومة الرغبات وتذكر النور الداخلي المرشد.",
            "focus_ready" to "مستعد",
            "focus_press_play" to "اضغط على زر التشغيل للبدء",
            "focus_inhale" to "شهيق",
            "focus_hold" to "احبس النفس",
            "focus_exhale" to "زفير",
            "ai_welcome_text" to "السلام عليكم ورحمة الله وبركاته! أنا مستشارك الإسلامي ورفيقك في التعافي من الإدمان. أنا هنا لمساعدتك على التخلص من العادات السيئة والأفكار السلبية، وبناء حياة طاهرة، واعية، وروحية وفقًا لتعاليم الإسلام وإرشادات الرقية الشرعية. لا تتردد في مشاركة مشاعرك - كل شيء سيبقى سرياً تماماً.",
            "ai_missing_key" to "يرجى الانتقال إلى لوحة 'Secrets' في AI Studio وتعيين 'GEMINI_API_KEY' بمفتاح API الخاص بك لـ Gemini.",
            "ai_connection_error" to "يرجى التحقق من اتصال الإنترنت.",
            "ai_thinking" to "جاري التفكير...",
            "past_7_days_records" to "تسجيل الصلوات لآخر 7 أيام",
            "today_checklist" to "قائمة صلوات اليوم الخمس",
            "fajr_desc" to "صلاة الفجر",
            "dhuhr_desc" to "صلاة الظهر",
            "asr_desc" to "صلاة العصر",
            "maghrib_desc" to "صلاة المغرب",
            "isha_desc" to "صلاة العشاء",
            "enable_alert_system" to "تفعيل نظام التنبيهات",
            "alert_system_desc" to "إذا كان نشطًا، سيتم إرسال بريد إلكتروني فورًا عند محاولة الانتكاس",
            "fill_required_fields" to "يرجى تعبئة جميع الحقول المميزة بـ *",
            "alert_active" to "تم تفعيل درع حماية الوصي!",
            "alert_inactive" to "تم إلغاء تفعيل درع الوصي.",
            "upcoming_firebase" to "تكامل Firebase القادم:",
            "upcoming_firebase_desc" to "في الوقت الحالي، ما عليك سوى حفظ بريد الوصي الإلكتروني. في المستقبل، ستمكن محفزات Firebase بنقرة واحدة الإشعارات الأمنية التلقائية. هذه ميزة قادمة.",
            "save_guardian_success" to "تم حفظ بيانات الوصي بنجاح!",
            "status_on" to "نشط",
            "status_off" to "مغلق",
            "unprotected_inactive" to "غير محمي - مغلق",
            "upcoming_feature" to "ميزة قادمة",
            "milestones_title_large" to "جميع الإنجازات والشارات",
            "milestones_desc" to "تطوير ضبط النفس",
            "post_progress" to "أنشر تقدمك",
            "motivational_board" to "أحدث لوحة تحفيزية",
            "no_posts" to "جاري التحميل أو لا توجد منشورات بعد...",
            "days_streak" to "سلسلة أيام: %d",
            "listen" to "استماع",
            "copy_message" to "نسخ",
            "message_copied" to "تم نسخ الرسالة إلى الحافظة",
            "like" to "أعجبني",
            "dislike" to "لم يعجبني",
            "tab_islamic_ai" to "الذكاء الاصطناعي الإسلامي",
            "tab_salah_tracker" to "متابع الصلاة",
            "badge_clown" to "مهرج",
            "badge_noob" to "محارب مبتدئ",
            "badge_novice" to "مبتدئ",
            "badge_average" to "متوسط",
            "badge_advanced" to "متقدم",
            "badge_sigma" to "سيجما",
            "badge_chad" to "تشاد",
            "badge_absolute_chad" to "تشاد المطلق",
            "badge_giga_chad" to "جيجا تشاد",
            "badge_abs_giga_chad" to "جيجا تشاد المطلق",
            "warn_title" to "مهلاً يا أخي!",
            "warn_body" to "المرء يجاهد نفسه والشيطان يغويه. هل نسيت العهد الذي قطعته على نفسك؟ لذة عابرة قد تسلبك سكينة العمر.",
            "warn_quote" to "\"إن أقوى الناس من يملك نفسه عند الغضب والشهوة.\"\n\nأرجوك لا توقف حماية الـ VPN، بل تأمل واذكر الله لمدة 15 دقيقة فقط. بعد ربع ساعة ستزول هذه الأفكار السيئة بإذن الله.",
            "warn_btn_meditate" to "بدء التفكر والذكر 🧘",
            "warn_btn_resist" to "لن أستسلم",
            "forgot_password" to "هل نسيت كلمة المرور؟ 🔑",
            "enter_email_first" to "يرجى إدخال البريد الإلكتروني أولاً!",
            "reset_sent" to "تم إرسال رابط إعادة تعيين كلمة المرور! 📧",
            "change_language" to "تغيير اللغة",
            "go_back" to "الرجوع",
            "days_remaining" to "متبقي %d أيام فقط",
            "days_required" to "مطلب %d أيام",
            "unlock_remaining" to "متبقي فقط %d أيام لفتح القفل!",
            "start_where" to "من أين تبدأ؟",
            "start_where_desc" to "هل تريد أن تبدأ العد من الآن، أم تحديد تاريخ بدء سابق؟",
            "choose_old_date" to "اختر تاريخاً قديماً",
            "right_now" to "الآن فصاعداً",
            "next_time" to "التالي (الوقت)",
            "cancel" to "إلغاء"
        ),
        "ur" to mapOf(
            "focus_mode_header" to "فوکس موڈ",
            "focus_desc_top" to "تمام خلفشار کو ختم کریں اور اپنی اندرونی طاقت کو بیدار کریں۔",
            "focus_desc_bottom" to "خواہشات کا مقابلہ کرنے کے لیے اپنی آنکھیں بند کریں، اور اپنے اندرونی نور کو یاد رکھیں۔",
            "focus_ready" to "تیار",
            "focus_press_play" to "شروع کرنے کے لیے پلے دبائیں",
            "focus_inhale" to "سانس کھینچیں",
            "focus_hold" to "روکیں",
            "focus_exhale" to "سانس چھوڑیں",
            "ai_welcome_text" to "السلام علیکم و رحمتہ اللہ! میں آپ کا اسلامی کونسلر اور لت سے نجات کا ساتھی ہوں۔ میں آپ کو بری عادات، فحش نگاری اور منفی خیالات کے چنگل سے نکالنے اور اسلامی تعلیمات اور رقیہ کی رہنمائی کے مطابق ایک پاکیزہ، باشعور اور روحانی زندگی بنانے میں مدد کرنے کے لیے حاضر ہوں۔ اپنے خیالات بلا جھجھک شیئر کریں - ہر چیز مکمل راز میں رکھی جائے گی۔",
            "ai_missing_key" to "براہ کرم AI Studio کے 'Secrets' پینل پر جائیں اور اپنی Gemini API کلید کے ساتھ 'GEMINI_API_KEY' سیٹ کریں۔",
            "ai_connection_error" to "براہ کرم انٹرنیٹ کنکشن چیک کریں۔",
            "ai_thinking" to "سوچ رہا ہے...",
            "past_7_days_records" to "پچھلے 7 دنوں کے نماز کے ریکارڈز",
            "today_checklist" to "آج کی پانچ وقت کی نماز کی فہرست",
            "fajr_desc" to "طلوع فجر کی نماز",
            "dhuhr_desc" to "ظہر کی نماز",
            "asr_desc" to "عصر کی نماز",
            "maghrib_desc" to "غروب آفتاب کی نماز",
            "isha_desc" to "عشاء کی نماز",
            "enable_alert_system" to "الرٹ سسٹم فعال کریں",
            "alert_system_desc" to "اگر فعال ہو تو، ریلیپس کی کوشش پر فوری طور پر ای میل بھیجی جائے گی",
            "fill_required_fields" to "براہ کرم تمام * نشان زدہ فیلڈز کو پُر کریں۔",
            "alert_active" to "گارڈین پروٹیکشن شیلڈ فعال کر دی گئی ہے!",
            "alert_inactive" to "گارڈین شیلڈ غیر فعال کر دی گئی۔",
            "upcoming_firebase" to "آنے والا فائر بیس انٹیگریشن:",
            "upcoming_firebase_desc" to "فی الحال، صرف اپنے سرپرست کا ای میل محفوظ کریں۔ مستقبل میں، ایک کلک کے ساتھ فائر بیس محرکات خودکار حفاظتی اطلاعات کو فعال کریں گے۔ یہ ایک آنے والی خصوصیت ہے۔",
            "save_guardian_success" to "سرپرست کی معلومات کامیابی سے محفوظ ہو گئیں!",
            "status_on" to "فعال",
            "status_off" to "غیر فعال",
            "unprotected_inactive" to "غیر محفوظ - غیر فعال",
            "upcoming_feature" to "آنے والی خصوصیت",
            "milestones_title_large" to "تمام سنگ میل اور بیجز",
            "milestones_desc" to "خود پر قابو پانا",
            "post_progress" to "اپنی ترقی پوسٹ کریں",
            "motivational_board" to "تازہ ترین ترغیبی بورڈ",
            "no_posts" to "لوڈ ہو رہا ہے یا ابھی تک کوئی پوسٹ نہیں ہے...",
            "days_streak" to "%d دن کا اسٹریک",
            "listen" to "سنیں",
            "copy_message" to "کاپی کریں",
            "message_copied" to "پیغام کاپی ہو گیا ہے",
            "like" to "پسند کریں",
            "dislike" to "ناپسند کریں",
            "tab_islamic_ai" to "اسلامک اے آئی",
            "tab_salah_tracker" to "صلاۃ ٹریکر",
            "badge_clown" to "جوہری",
            "badge_noob" to "نیا جنگجو",
            "badge_novice" to "نوآموز",
            "badge_average" to "اوسط",
            "badge_advanced" to "اعلی",
            "badge_sigma" to "سگما",
            "badge_chad" to "چیڈ",
            "badge_absolute_chad" to "کامل چیڈ",
            "badge_giga_chad" to "گیگا چیڈ",
            "badge_abs_giga_chad" to "کامل گیگا چیڈ",
            "warn_title" to "ایک لمحہ ٹھہریے بھائی!",
            "warn_body" to "شیطان آپ کو ورغلا رہا ہے۔ کیا آپ وہ وعدہ بھول گئے جو آپ نے اپنے آپ سے کیا تھا؟ لمحاتی لذت کا ایک قطرہ آپ کی زندگی کا دائمی سکون چھین سکتا ہے۔",
            "warn_quote" to "\"جو اپنے آپ پر قابو پا سکتا ہے وہ دنیا کا سب سے طاقتور انسان ہے۔\"\n\nبراہ کرم وی پی این بند نہ کریں، بلکہ صرف 15 منٹ کے لیے غور وفکر اور مراقبہ کریں۔ 15 منٹ کے بعد یہ منفی خیالات ختم ہو جائیں گے! ان شاء اللہ۔",
            "warn_btn_meditate" to "مراقبہ شروع کریں 🧘",
            "warn_btn_resist" to "میں ہار نہیں مانوں گا",
            "forgot_password" to "کیا آپ پاس ورڈ بھول گئے؟ 🔑",
            "enter_email_first" to "براہ کرم پہلے اپنا ای میل درج کریں!",
            "reset_sent" to "پاس ورڈ ری سیٹ لنک بھیج دیا گیا ہے! 📧",
            "change_language" to "زبان تبدیل کریں",
            "go_back" to "واپس جائیں",
            "days_remaining" to "صرف %d دن باقی ہیں",
            "days_required" to "%d دن درکار ہیں",
            "unlock_remaining" to "انلاک کرنے میں صرف %d دن باقی ہیں!",
            "start_where" to "کہاں سے شروع کریں؟",
            "start_where_desc" to "کیا آپ ابھی سے گنتی شروع کرنا چاہتے ہیں، یا ریکوری کی کوئی پرانی تاریخ منتخب کرنا چاہتے ہیں؟",
            "choose_old_date" to "پرانی تاریخ کا انتخاب کریں",
            "right_now" to "ابھی سے",
            "next_time" to "اگلا (وقت)",
            "cancel" to "منسوخ کریں"
        ),
        "hi" to mapOf(
            "focus_mode_header" to "फोकस मोड",
            "focus_desc_top" to "सभी ध्यान भटकाने वाली चीजों को दूर करें और अपनी आंतरिक शक्ति को जगाएं।",
            "focus_desc_bottom" to "इच्छाओं का विरोध करने के लिए अपनी आँखें बंद करें, और अपने भीतर के प्रकाश को याद करें।",
            "focus_ready" to "तैयार",
            "focus_press_play" to "शुरू करने के लिए प्ले दबाएं",
            "focus_inhale" to "साँस लें",
            "focus_hold" to "रोकें",
            "focus_exhale" to "साँस छोड़ें",
            "ai_welcome_text" to "अस्सलामू अलैकुम व रहमतुल्लाह! मैं आपका इस्लामिक काउंसलर और एडिक्शन रिकवरी कंपैनियन हूँ। मैं आपको बुरी आदतों, पोर्नोग्राफी और नकारात्मक विचारों के चंगुल से बाहर निकालने और इस्लामी शिक्षाओं और रुक्या मार्गदर्शन के अनुसार एक पवित्र, जागरूक और आध्यात्मिक जीवन बनाने में मदद करने के लिए यहाँ हूँ। अपने विचार साझा करने में संकोच न करें - सब कुछ पूरी तरह गोपनीय रहेगा।",
            "ai_missing_key" to "कृपया AI Studio के 'Secrets' पैनल पर जाएं और अपनी जेमिनी एपीआई की (API Key) के साथ 'GEMINI_API_KEY' सेट करें।",
            "ai_connection_error" to "कृपया अपना इंटरनेट कनेक्शन जांचें।",
            "ai_thinking" to "सोच रहा हूँ...",
            "past_7_days_records" to "पिछले 7 दिनों के प्रार्थना रिकॉर्ड",
            "today_checklist" to "आज की पांच वक्त की प्रार्थना सूची",
            "fajr_desc" to "भोर की प्रार्थना",
            "dhuhr_desc" to "दोपहर की प्रार्थना",
            "asr_desc" to "तीसरे पहर की प्रार्थना",
            "maghrib_desc" to "सूर्यास्त की प्रार्थना",
            "isha_desc" to "रात की प्रार्थना",
            "enable_alert_system" to "अलर्ट सिस्टम चालू करें",
            "alert_system_desc" to "यदि सक्रिय है, तो रिलैप्स के प्रयास पर तुरंत एक ईमेल भेजा जाएगा",
            "fill_required_fields" to "कृपया सभी * चिह्नित फ़ील्ड भरें।",
            "alert_active" to "अभिभावक सुरक्षा कवच सक्रिय किया गया!",
            "alert_inactive" to "अभिभावक कवच निष्क्रिय किया गया।",
            "upcoming_firebase" to "आगामी फायरबेस एकीकरण:",
            "upcoming_firebase_desc" to "अभी के लिए, बस अपने अभिभावक का ईमेल सहेजें। भविष्य में, एक-क्लिक फायरबेस ट्रिगर स्वचालित सुरक्षा सूचनाओं को सक्षम करेगा। यह एक आगामी सुविधा है।",
            "save_guardian_success" to "अभिभावक की जानकारी सफलतापूर्वक सहेजी गई!",
            "status_on" to "चालू",
            "status_off" to "बंद",
            "unprotected_inactive" to "असुरक्षित - निष्क्रिय",
            "upcoming_feature" to "आगामी सुविधा",
            "milestones_title_large" to "सभी मील के पत्थर और बैज",
            "milestones_desc" to "आत्म-नियंत्रण का विकास",
            "post_progress" to "अपनी प्रगति पोस्ट करें",
            "motivational_board" to "नवीनतम प्रेरणादायक बोर्ड",
            "no_posts" to "लोड हो रहा है या अभी तक कोई पोस्ट नहीं है...",
            "days_streak" to "%d दिनों का स्ट्रीक",
            "listen" to "सुनें",
            "copy_message" to "कॉपी करें",
            "message_copied" to "संदेश कॉपी हो गया है",
            "like" to "पसंद करें",
            "dislike" to "नापसंद करें",
            "tab_islamic_ai" to "इस्लामिक एआई",
            "tab_salah_tracker" to "सलाह ट्रैकर",
            "badge_clown" to "जोकर",
            "badge_noob" to "नया योद्धा",
            "badge_novice" to "नौसिखिया",
            "badge_average" to "औसत",
            "badge_advanced" to "उन्नत",
            "badge_sigma" to "सिग्मा",
            "badge_chad" to "चैड",
            "badge_absolute_chad" to "परम चैड",
            "badge_giga_chad" to "गीगा चैड",
            "badge_abs_giga_chad" to "परम गीगा चैड",
            "warn_title" to "एक पल रुकिए भाई!",
            "warn_body" to "शैतान आपको बहकाने की कोशिश कर रहा है। क्या आप खुद से किया वादा भूल गए? पल भर का क्षणिक आनंद आपके जीवन की दीर्घकालिक शांति छीन सकता है।",
            "warn_quote" to "\"जो खुद पर नियंत्रण रख सकता है, वह दुनिया का सबसे शक्तिशाली व्यक्ति है।\"\n\nकृपया वीपीएन बंद न करें, बल्कि केवल 15 मिनट ध्यान (Meditation) करें। 15 मिनट बाद ये नकारात्मक विचार खत्म हो जाएंगे! इंशाल्लाह।",
            "warn_btn_meditate" to "ध्यान शुरू करें 🧘",
            "warn_btn_resist" to "मैं हार नहीं मानूंगा",
            "forgot_password" to "पासवर्ड भूल गए? 🔑",
            "enter_email_first" to "कृपया पहले अपना ईमेल दर्ज करें!",
            "reset_sent" to "पासवर्ड रीसेट लिंक भेज दिया गया है! 📧",
            "change_language" to "भाषा बदलें",
            "go_back" to "वापस जाएँ",
            "days_remaining" to "केवल %d दिन बचे हैं",
            "days_required" to "%d दिन आवश्यक हैं",
            "unlock_remaining" to "अनलॉक करने के लिए केवल %d दिन बाकी हैं!",
            "start_where" to "कहाँ से शुरू करें?",
            "start_where_desc" to "क्या आप अभी से गिनती शुरू करना चाहते हैं, या रिकवरी की कोई पुरानी तारीख चुनना चाहते हैं?",
            "choose_old_date" to "पुरानी तारीख चुनें",
            "right_now" to "अभी से",
            "next_time" to "अगला (समय)",
            "cancel" to "रद्द करें"
        ),
        "es" to mapOf(
            "focus_mode_header" to "Modo de Enfoque",
            "focus_desc_top" to "Elimina todas las distracciones y despierta tu fuerza interior.",
            "focus_desc_bottom" to "Cierra los ojos para resistir los impulsos y recuerda tu luz interior.",
            "focus_ready" to "Listo",
            "focus_press_play" to "Presiona Play para comenzar",
            "focus_inhale" to "Inhalar",
            "focus_hold" to "Mantener",
            "focus_exhale" to "Exhalar",
            "ai_welcome_text" to "¡Assalamu alaikum wa rahmatullah! Soy tu consejero islámico y compañero de recuperación de adicciones. Estoy aquí para ayudarte a liberarte de los malos hábitos, la pornografía y los pensamientos negativos, y para construir una vida pura, consciente y espiritual de acuerdo con las enseñanzas islámicas y la guía de Ruqyah. No dudes en compartir tus pensamientos: todo se mantendrá estrictamente confidencial.",
            "ai_missing_key" to "Por favor, ve al panel de 'Secrets' en AI Studio y configura 'GEMINI_API_KEY' con tu clave API de Gemini.",
            "ai_connection_error" to "Por favor, comprueba tu conexión a internet.",
            "ai_thinking" to "Pensando...",
            "past_7_days_records" to "Historial de Oraciones de los últimos 7 días",
            "today_checklist" to "Lista de las 5 Oraciones Diarias Obligatorias",
            "fajr_desc" to "Oración del Alba",
            "dhuhr_desc" to "Oración del Mediodía",
            "asr_desc" to "Oración de la Tarde",
            "maghrib_desc" to "Oración del Atardecer",
            "isha_desc" to "Oración de la Noche",
            "enable_alert_system" to "Activar Sistema de Alertas",
            "alert_system_desc" to "Si está activo, se enviará un correo inmediatamente al intentar una recaída",
            "fill_required_fields" to "Por favor, complete todos los campos marcados con *",
            "alert_active" to "¡Escudo de Protección del Guardián Activado!",
            "alert_inactive" to "Escudo del Guardián Desactivado.",
            "upcoming_firebase" to "Próxima Integración con Firebase:",
            "upcoming_firebase_desc" to "Por ahora, solo guarda el correo de tu guardián. En el futuro, un activador de Firebase con un solo clic habilitará notificaciones de seguridad automáticas. Esta es una función próxima.",
            "save_guardian_success" to "¡Configuración del tutor guardada correctamente!",
            "status_on" to "Activo",
            "status_off" to "Inactivo",
            "unprotected_inactive" to "Desprotegido - Inactivo",
            "upcoming_feature" to "Próxima función",
            "milestones_title_large" to "Todos los Logros y Medallas",
            "milestones_desc" to "Desarrollo del Autocontrol",
            "post_progress" to "Publicar progreso",
            "motivational_board" to "Último Panel de Motivación",
            "no_posts" to "Cargando publicaciones o no hay publicaciones aún...",
            "days_streak" to "Racha de %d días",
            "listen" to "Escuchar",
            "copy_message" to "Copiar",
            "message_copied" to "Mensaje copiado al portapapeles",
            "like" to "Me gusta",
            "dislike" to "No me gusta",
            "tab_islamic_ai" to "IA Islámica",
            "tab_salah_tracker" to "Rastreador de Oraciones",
            "badge_clown" to "Payaso",
            "badge_noob" to "Nuevo Guerrero",
            "badge_novice" to "Novicio",
            "badge_average" to "Promedio",
            "badge_advanced" to "Avanzado",
            "badge_sigma" to "Sigma",
            "badge_chad" to "Chad",
            "badge_absolute_chad" to "Chad Absoluto",
            "badge_giga_chad" to "Giga Chad",
            "badge_abs_giga_chad" to "Giga Chad Absoluto",
            "warn_title" to "¡Espera un momento, hermano!",
            "warn_body" to "Shaytan está intentando provocarte. ¿Olvidaste la promesa que te hiciste a ti mismo? Una efímera gota de placer momentáneo puede quitarte la paz de toda una vida.",
            "warn_quote" to "\"Aquel que puede controlarse a sí mismo es la persona más fuerte del mundo.\"\n\nPor favor, no apagues el VPN; medita durante solo 15 minutos. ¡Después de 15 minutos, estos pensamientos negativos ya no existirán! InshaAllah.",
            "warn_btn_meditate" to "Comenzar Meditación 🧘",
            "warn_btn_resist" to "No me rendiré",
            "forgot_password" to "¿Olvidaste tu contraseña? 🔑",
            "enter_email_first" to "¡Por favor, introduzca su correo primero!",
            "reset_sent" to "¡Enlace de restauración enviado al correo! 📧",
            "change_language" to "Cambiar Idioma",
            "go_back" to "Volver",
            "days_remaining" to "Solo faltan %d días",
            "days_required" to "%d días requeridos",
            "unlock_remaining" to "¡Solo faltan %d días para desbloquear!",
            "start_where" to "¿Dónde empezar?",
            "start_where_desc" to "¿Quieres empezar a contar ahora mismo o seleccionar una fecha anterior de recuperación?",
            "choose_old_date" to "Seleccionar Fecha Anterior",
            "right_now" to "Desde Ahora",
            "next_time" to "Siguiente (Hora)",
            "cancel" to "Cancelar"
        )
    )

    fun get(key: String): String {
        val lang = currentLanguage.value
        return additionalTranslations[lang]?.get(key)
            ?: translations[lang]?.get(key)
            ?: additionalTranslations["en"]?.get(key)
            ?: translations["en"]?.get(key)
            ?: key
    }
}

fun t(key: String): String {
    return LanguageStore.get(key)
}

fun t(key: String, vararg args: Any): String {
    val translated = LanguageStore.get(key)
    return try {
        String.format(translated, *args)
    } catch (e: Exception) {
        translated
    }
}

fun String.localizeDigits(): String {
    if (LanguageStore.currentLanguage.value != "bn") return this
    val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    return this.map { if (it in '0'..'9') bnDigits[it - '0'] else it }.joinToString("")
}

fun formatTimeRange(start: String?, end: String?): String {
    if (start == null || end == null) return ""
    val cleanStart = start.replace(Regex("\\s\\(.*\\)"), "")
    val cleanEnd = end.replace(Regex("\\s\\(.*\\)"), "")
    return "$cleanStart - $cleanEnd"
}

data class Badge(
    val id: Int,
    val name: String,
    val days: Int,
    @DrawableRes val imgRes: Int,
    val descName: String
)

object FastBitmapCache {
    private val memoryCache = object : android.util.LruCache<String, android.graphics.Bitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: String, value: android.graphics.Bitmap): Int {
            return value.byteCount
        }
    }
    private val resIdCache = java.util.concurrent.ConcurrentHashMap<String, Int>()

    fun get(key: String): android.graphics.Bitmap? {
        if (key.isEmpty()) return null
        return try { memoryCache.get(key) } catch (e: Exception) { null }
    }

    fun decodeAndCache(dataUri: String): android.graphics.Bitmap? {
        if (dataUri.isEmpty()) return null
        get(dataUri)?.let { return it }
        return try {
            val base64String = dataUri.substringAfter("base64,")
            val bytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bmp != null) {
                memoryCache.put(dataUri, bmp)
            }
            bmp
        } catch (e: Exception) {
            null
        }
    }

    fun getResId(context: Context, resName: String): Int {
        return resIdCache.getOrPut(resName) {
            context.resources.getIdentifier(resName, "drawable", context.packageName)
        }
    }
}

@Composable
fun BadgeImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    fallbackName: String = "Badge",
    contentScale: androidx.compose.ui.layout.ContentScale = androidx.compose.ui.layout.ContentScale.Crop
) {
    val context = LocalContext.current
    if (imageUrl.startsWith("data:image")) {
        var bitmap by remember(imageUrl) { mutableStateOf(FastBitmapCache.get(imageUrl)) }
        LaunchedEffect(imageUrl) {
            if (bitmap == null) {
                bitmap = withContext(Dispatchers.Default) {
                    FastBitmapCache.decodeAndCache(imageUrl)
                }
            }
        }
        if (bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = fallbackName,
                modifier = modifier,
                contentScale = contentScale
            )
        } else {
            Icon(Icons.Filled.EmojiEvents, contentDescription = fallbackName, modifier = modifier, tint = Color(0xFF78350F))
        }
    } else if (imageUrl.startsWith("res://")) {
        val resName = imageUrl.removePrefix("res://")
        val resId = remember(resName) {
            FastBitmapCache.getResId(context, resName)
        }
        if (resId != 0) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = resId),
                contentDescription = fallbackName,
                modifier = modifier,
                contentScale = contentScale
            )
        } else {
            Icon(Icons.Filled.EmojiEvents, contentDescription = fallbackName, modifier = modifier, tint = Color(0xFF78350F))
        }
    } else if (imageUrl.isNotEmpty()) {
        coil.compose.AsyncImage(
            model = imageUrl,
            contentDescription = fallbackName,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Icon(Icons.Filled.EmojiEvents, contentDescription = fallbackName, modifier = modifier, tint = Color(0xFF78350F))
    }
}

fun getBadgeName(badge: Badge): String {
    val key = when (badge.id) {
        0 -> "badge_clown"
        1 -> "badge_noob"
        2 -> "badge_novice"
        3 -> "badge_average"
        4 -> "badge_advanced"
        5 -> "badge_sigma"
        6 -> "badge_chad"
        7 -> "badge_absolute_chad"
        8 -> "badge_giga_chad"
        9 -> "badge_abs_giga_chad"
        else -> return badge.name
    }
    return LanguageStore.get(key)
}

fun getCustomBadgeName(badge: CustomBadge): String {
    val key = when (badge.name.trim().lowercase()) {
        "clown" -> "badge_clown"
        "noob" -> "badge_noob"
        "novice" -> "badge_novice"
        "apprentice" -> "badge_noob"
        "warrior" -> "badge_noob"
        "average" -> "badge_average"
        "advanced" -> "badge_advanced"
        "sigma" -> "badge_sigma"
        "alpha" -> "badge_advanced"
        "chad" -> "badge_chad"
        "absolute chad" -> "badge_absolute_chad"
        "giga chad" -> "badge_giga_chad"
        "absolute giga chad" -> "badge_abs_giga_chad"
        else -> return badge.name
    }
    return LanguageStore.get(key)
}

val Badges = listOf(
    Badge(0, "Clown", 0, R.drawable.badge_0, "Clown"),
    Badge(1, "New Warrior", 1, R.drawable.badge_1, "Noob"),
    Badge(2, "Novice", 3, R.drawable.badge_2, "Novice"),
    Badge(3, "Average", 7, R.drawable.badge_3, "Average"),
    Badge(4, "Advanced", 15, R.drawable.badge_4, "Advanced"),
    Badge(5, "Sigma", 30, R.drawable.badge_5, "Sigma"),
    Badge(6, "Chad", 45, R.drawable.badge_6, "Chad"),
    Badge(7, "Absolute Chad", 60, R.drawable.badge_7, "Absolute Chad"),
    Badge(8, "Giga Chad", 120, R.drawable.badge_8, "Giga Chad"),
    Badge(9, "Absolute Giga Chad", 365, R.drawable.badge_9, "Absolute Giga Chad")
)

class PurityTrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("purity_prefs", Context.MODE_PRIVATE)
    private val _startTime = MutableStateFlow(prefs.getLong("start_time", System.currentTimeMillis()))
    val startTime = _startTime.asStateFlow()

    private val _currentTime = MutableStateFlow(System.currentTimeMillis())

    val durationMs = combine(_startTime, _currentTime) { start, current ->
        maxOf(0L, current - start)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val durationDays: StateFlow<Int> = durationMs
        .map { (it / 86_400_000L).toInt() }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ((System.currentTimeMillis() - _startTime.value).coerceAtLeast(0L) / 86_400_000L).toInt())

    private val sessionHistoryDao = AppDatabase.getDatabase(application).sessionHistoryDao()
    
    val historyList = sessionHistoryDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bestRecord = sessionHistoryDao.getBestRecord()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            while (true) {
                _currentTime.value = System.currentTimeMillis()
                delay(1000)
            }
        }
    }

    private fun saveHistoryBeforeReset(endTime: Long, customBadges: List<CustomBadge>) {
        val start = _startTime.value
        val currentDurationMs = maxOf(0L, endTime - start)
        val durationDays = (currentDurationMs / (1000 * 60 * 60 * 24)).toInt()
        if (currentDurationMs > 60000) { // Only save if more than 1 minute to avoid spam
            val badgeName = customBadges.lastOrNull { durationDays >= it.requiredDays }?.name 
                ?: Badges.lastOrNull { durationDays >= it.days }?.descName 
                ?: "Warrior"
            val history = SessionHistory(
                title = badgeName,
                startTime = start,
                endTime = endTime,
                durationDays = durationDays,
                statusText = "$durationDays days"
            )
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                sessionHistoryDao.insertHistory(history)
                
                // Keep history synchronized via Firestore subcollection
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val user = auth.currentUser
                if (user != null) {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val historyRecord = mapOf(
                        "title" to badgeName,
                        "startTime" to start,
                        "endTime" to endTime,
                        "durationDays" to durationDays,
                        "statusText" to "$durationDays days"
                    )
                    db.collection("users").document(user.uid)
                        .collection("history").document(start.toString())
                        .set(historyRecord, com.google.firebase.firestore.SetOptions.merge())
                }
            }
        }
    }

    fun syncCloudHistoryToLocal(userId: String) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .collection("history").get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        val localList = historyList.value
                        val localStarts = localList.map { it.startTime }.toSet()
                        
                        for (doc in querySnapshot.documents) {
                            val startTime = doc.getLong("startTime") ?: 0L
                            if (startTime > 0 && !localStarts.contains(startTime)) {
                                val title = doc.getString("title") ?: "Warrior"
                                val endTime = doc.getLong("endTime") ?: 0L
                                val durationDays = doc.getLong("durationDays")?.toInt() ?: 0
                                val statusText = doc.getString("statusText") ?: ""
                                val session = SessionHistory(
                                    title = title,
                                    startTime = startTime,
                                    endTime = endTime,
                                    durationDays = durationDays,
                                    statusText = statusText
                                )
                                sessionHistoryDao.insertHistory(session)
                            }
                        }
                    }
                }
            }
    }

    fun resetTimer(customBadges: List<CustomBadge> = emptyList()) {
        val now = System.currentTimeMillis()
        saveHistoryBeforeReset(now, customBadges)
        _startTime.value = now
        prefs.edit().putLong("start_time", now).apply()
        
        // Force sync reset to cloud
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            val userRecord = mapOf(
                "name" to (user.displayName ?: "Anonymous Warrior"),
                "email" to (user.email ?: ""),
                "streakMs" to 0L,
                "timestamp" to now
            )
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(user.uid)
                .set(userRecord, com.google.firebase.firestore.SetOptions.merge())
        }
    }

    fun resetTimerToDate(year: Int, month: Int, dayOfMonth: Int, customBadges: List<CustomBadge> = emptyList()) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth, 0, 0, 0)
        val time = calendar.timeInMillis
        val now = System.currentTimeMillis()
        saveHistoryBeforeReset(now, customBadges)
        val finalTime = if (time > now) now else time
        _startTime.value = finalTime
        prefs.edit().putLong("start_time", finalTime).apply()
        
        // Force sync reset to cloud
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            val streakMs = now - finalTime
            val userRecord = mapOf(
                "name" to (user.displayName ?: "Anonymous Warrior"),
                "email" to (user.email ?: ""),
                "streakMs" to streakMs,
                "timestamp" to now
            )
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(user.uid)
                .set(userRecord, com.google.firebase.firestore.SetOptions.merge())
        }
    }

    fun deleteAllHistoryRecords() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            sessionHistoryDao.deleteAllHistory()
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val user = auth.currentUser
            if (user != null) {
                try {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    db.collection("users").document(user.uid)
                        .collection("history").get().addOnSuccessListener { snapshot ->
                            for (doc in snapshot.documents) {
                                doc.reference.delete()
                            }
                        }
                } catch (e: Exception) {}
            }
        }
    }

    fun deleteHistoryItem(item: SessionHistory) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            sessionHistoryDao.deleteHistoryById(item.id)
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val user = auth.currentUser
            if (user != null) {
                try {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    db.collection("users").document(user.uid)
                        .collection("history").document(item.startTime.toString()).delete()
                } catch (e: Exception) {}
            }
        }
    }

    fun clearAllData() {
        _startTime.value = System.currentTimeMillis()
        prefs.edit().clear().apply()
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            sessionHistoryDao.deleteAllHistory()
        }
    }

    fun restoreStreak(newStartTime: Long) {
        _startTime.value = newStartTime
        prefs.edit().putLong("start_time", newStartTime).apply()
    }
}

class MainActivity : ComponentActivity() {

    private fun triggerGuardianAlert(context: Context, reason: String) {
        GuardianManager.triggerGuardianAlert(context, reason)
    }

    private val _vpnConfigureEvent = kotlinx.coroutines.flow.MutableSharedFlow<Boolean>(replay = 1, extraBufferCapacity = 1)
    private val _motivationEvent = kotlinx.coroutines.flow.MutableSharedFlow<Boolean>(replay = 1, extraBufferCapacity = 1)
    private val _openTabEvent = kotlinx.coroutines.flow.MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 1)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Important to update the activity's intent
        intent.getStringExtra("open_tab")?.let {
            _openTabEvent.tryEmit(it)
        }
        if (intent.getBooleanExtra("stop_adhan", false)) {
            AdhanPlayer.stopAdhan(this)
        }
        if (intent.action == "com.example.VPN_CONFIGURE") {
            triggerGuardianAlert(this, "attempted to open VPN configuration or disconnect the VPN Shield")
            _vpnConfigureEvent.tryEmit(true)
        } else if (intent.action == "com.example.MOTIVATION") {
            _motivationEvent.tryEmit(true)
        }
    }

    private fun isGmsAvailable(context: Context): Boolean {
        return try {
            val availability = com.google.android.gms.common.GoogleApiAvailability.getInstance()
            availability.isGooglePlayServicesAvailable(context) == com.google.android.gms.common.ConnectionResult.SUCCESS
        } catch (t: Throwable) {
            false
        }
    }

    private fun fetchLocationAndSchedule() {
        try {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
            var gotLocation = false
            if (locationManager != null && (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED)) {
                val providers = locationManager.getProviders(true)
                for (provider in providers) {
                    val loc = try { locationManager.getLastKnownLocation(provider) } catch(e: Throwable) { null }
                    if (loc != null) {
                        PrayerScheduler.updateLocation(this, loc.latitude, loc.longitude)
                        gotLocation = true
                        break
                    }
                }
            }
            if (!gotLocation && isGmsAvailable(this)) {
                try {
                    val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this)
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            PrayerScheduler.updateLocation(this, location.latitude, location.longitude)
                        }
                        PrayerScheduler.scheduleDailyPrayers(this)
                    }.addOnFailureListener {
                        PrayerScheduler.scheduleDailyPrayers(this)
                    }
                    return
                } catch (t: Throwable) {
                    android.util.Log.w("MainActivity", "Location broker check: ${t.message}")
                }
            }
            PrayerScheduler.scheduleDailyPrayers(this)
        } catch(e: Throwable) {
            PrayerScheduler.scheduleDailyPrayers(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            fetchLocationAndSchedule()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
        } catch (e: Throwable) {
            android.util.Log.w("MainActivity", "Firebase init: ${e.message}")
        }
        enableEdgeToEdge()
        // Request highest supported display refresh rate (120Hz / 90Hz) for ultra-smooth UI
        window.decorView.post {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    val disp = display
                    val modes = disp?.supportedModes
                    val highestMode = modes?.maxByOrNull { it.refreshRate }
                    if (highestMode != null) {
                        val params = window.attributes
                        params.preferredDisplayModeId = highestMode.modeId
                        params.preferredRefreshRate = highestMode.refreshRate
                        window.attributes = params
                    }
                } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    @Suppress("DEPRECATION")
                    val modes = windowManager.defaultDisplay.supportedModes
                    val highestMode = modes.maxByOrNull { it.refreshRate }
                    if (highestMode != null) {
                        val params = window.attributes
                        params.preferredDisplayModeId = highestMode.modeId
                        params.preferredRefreshRate = highestMode.refreshRate
                        window.attributes = params
                    }
                }
            } catch (e: Exception) {}
        }
        PremiumManager.init(this)
        com.example.ui.theme.GlassTheme.isLightMode.value = false
        
        if (intent?.action == "com.example.VPN_CONFIGURE") {
            triggerGuardianAlert(this, "attempted to open VPN configuration or disconnect the VPN Shield")
            _vpnConfigureEvent.tryEmit(true)
        } else if (intent?.action == "com.example.MOTIVATION") {
            _motivationEvent.tryEmit(true)
        }
        
        // Asynchronously initialize heavy background tasks without blocking initial frame rendering
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val appPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                appPref.edit().putBoolean("is_light_mode", false).apply()
            } catch (e: Exception) {}
            
            try {
                val pref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
                val lang = pref.getString("language", "en") ?: "en"
                LanguageStore.currentLanguage.value = lang
            } catch (e: Exception) {}

            try {
                com.google.android.gms.ads.MobileAds.initialize(this@MainActivity) {}
            } catch (e: Throwable) {
                android.util.Log.w("MainActivity", "MobileAds init: ${e.message}")
            }
            
            try {
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
                nm?.cancel(1012)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    nm?.deleteNotificationChannel("protection_guard_service_channel")
                    nm?.deleteNotificationChannel("vpn_active_guard")
                }
            } catch (e: Exception) {}

            try {
                startService(Intent(this@MainActivity, BackgroundNotificationService::class.java))
                GuardHealthWorker.schedulePeriodicWork(this@MainActivity)
                SettingsGuardWatchdogService.startIfInForeground(this@MainActivity)
                SettingsGuardJobService.schedule(this@MainActivity)
                ProtectionService.pokeFromWatchdog(this@MainActivity)
                GuardHealthWorker.checkNowAndNotify(this@MainActivity)
            } catch(e: Exception) {}
            
            fetchLocationAndSchedule()
        }

        // Remove the one-time battery_prompt_shown_v2 gate:
        // Always check the real PowerManager.isIgnoringBatteryOptimizations() status on app start.
        window.decorView.post {
            try {
                GuardHealthWorker.schedulePeriodicWork(this@MainActivity)
                GuardHealthWorker.checkNowAndNotify(this@MainActivity)
            } catch (e: Exception) {}
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                window.decorView.post {
                    try {
                        requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
                    } catch (e: Exception) {}
                }
            }
        }
        
        setContent {
            val authViewModel: AuthViewModel = viewModel()
            val purityTrackerViewModel: PurityTrackerViewModel = viewModel()
            val currentUser by authViewModel.currentUser.collectAsState()
            val context = androidx.compose.ui.platform.LocalContext.current
            
            val callSession by VoiceCallManager.callSession.collectAsState()

            androidx.compose.runtime.DisposableEffect(currentUser) {
                VoiceCallManager.initialize(context)
                var callListenerReg: com.google.firebase.firestore.ListenerRegistration? = null
                if (currentUser != null) {
                    callListenerReg = VoiceCallManager.listenForIncomingCalls(context, currentUser!!.uid)
                }
                onDispose {
                    callListenerReg?.remove()
                }
            }

            androidx.compose.runtime.DisposableEffect(currentUser) {
                var listener: com.google.firebase.firestore.ListenerRegistration? = null
                if (currentUser != null) {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    listener = db.collection("users").document(currentUser!!.uid)
                        .addSnapshotListener { snapshot, e ->
                            if (e == null && snapshot != null && snapshot.exists()) {
                                // Sync Premium (Cloud is the absolute source of truth)
                                val isMeAnas = currentUser?.email == "muhaiminulhasananas@gmail.com"
                                var isCloudPremium = snapshot.getBoolean("isPremium") ?: false
                                if (isMeAnas) isCloudPremium = true
                                val premiumExpiryTimestamp = snapshot.getLong("premiumExpiryTimestamp") ?: 0L
                                val isExpired = !isMeAnas && premiumExpiryTimestamp != 0L && System.currentTimeMillis() > premiumExpiryTimestamp
                                
                                val finalPremium = if (isExpired) {
                                    if (snapshot.getBoolean("isPremium") == true) {
                                        db.collection("users").document(currentUser!!.uid).update(
                                            "isPremium", false,
                                            "premiumPlan", "expired",
                                            "premiumExpiryTimestamp", 0L
                                        )
                                    }
                                    false
                                } else {
                                    isCloudPremium
                                }
                                
                                val finalExpiry = if (isExpired) 0L else premiumExpiryTimestamp
                                PremiumManager.setPremiumStatus(context, finalPremium, finalExpiry)
                                
                                // Sync Streak
                                val cloudStreakMs = snapshot.getLong("streakMs") ?: 0L
                                val cloudTimestamp = snapshot.getLong("timestamp") ?: 0L
                                if (cloudStreakMs > 0 && cloudTimestamp > 0) {
                                    val elapsed = System.currentTimeMillis() - cloudTimestamp
                                    val realStreak = cloudStreakMs + elapsed
                                    val currentStreak = System.currentTimeMillis() - purityTrackerViewModel.startTime.value
                                    if (realStreak > currentStreak && (realStreak - currentStreak) > 60000) { // Only sync if cloud is ahead by more than 1 minute
                                        purityTrackerViewModel.restoreStreak(System.currentTimeMillis() - realStreak)
                                    }
                                }
                            }
                        }
                } else {
                    PremiumManager.setPremiumStatus(context, false)
                }
                onDispose {
                    listener?.remove()
                }
            }

            val initialTab = remember(context) {
                val activity = context as? android.app.Activity
                if (activity?.intent?.hasExtra("open_direct_chat") == true) {
                    CommunitySubTabState.requestedSubTab.value = "friends"
                    "community"
                } else if (activity?.intent?.hasExtra("open_tab") == true) {
                    val requested = activity.intent.getStringExtra("open_tab") ?: "home"
                    if (requested == "friends") {
                        CommunitySubTabState.requestedSubTab.value = "friends"
                        "community"
                    } else {
                        requested
                    }
                } else {
                    "home"
                }
            }
            var currentTab by remember { mutableStateOf(initialTab) }
            val tabBackStack = remember { mutableStateListOf(initialTab) }

            val handleTabSelect: (String) -> Unit = { rawTarget ->
                val target = if (rawTarget == "friends") {
                    CommunitySubTabState.requestedSubTab.value = "friends"
                    "community"
                } else {
                    rawTarget
                }
                if (target != currentTab) {
                    tabBackStack.add(target)
                    currentTab = target
                }
            }

            val handleBackNavigation: () -> Unit = {
                if (tabBackStack.size > 1) {
                    tabBackStack.removeAt(tabBackStack.lastIndex)
                    currentTab = tabBackStack.last()
                } else if (currentTab != "home") {
                    currentTab = "home"
                    tabBackStack.clear()
                    tabBackStack.add("home")
                }
            }

            var showMeditationDialog by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                launch {
                    _vpnConfigureEvent.collect {
                        showMeditationDialog = true
                    }
                }
                launch {
                    _motivationEvent.collect {
                        showMeditationDialog = true
                    }
                }
                launch {
                    _openTabEvent.collect { target ->
                        handleTabSelect(target)
                    }
                }
            }

            val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
            val dynamicContentColor = if (isLightMode) Color(0xFF0F172A) else Color.White
            val dynamicOnSurfaceColor = if (isLightMode) Color(0xFF1E293B) else Color.White

            val appColorScheme = if (isLightMode) {
                lightColorScheme(
                    background = Color(0xFFEFF4F8),
                    surface = Color.White,
                    primary = Color(0xFF0284C7),
                    onBackground = Color(0xFF0F172A),
                    onSurface = Color(0xFF0F172A),
                    surfaceVariant = Color(0xFFF1F5F9),
                    onSurfaceVariant = Color(0xFF334155)
                )
            } else {
                darkColorScheme(
                    background = DeepBlue,
                    surface = CardBlue,
                    primary = CyanAccent,
                    onBackground = dynamicContentColor,
                    onSurface = dynamicOnSurfaceColor
                )
            }

            MaterialTheme(
                colorScheme = appColorScheme
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides dynamicContentColor
                ) {
                    if (showMeditationDialog) {
                    androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBlue),
                            modifier = Modifier.padding(16.dp).fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = LightRed, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(t("warn_title"), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(t("warn_body"), color = Color.LightGray, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(modifier = Modifier.background(DeepBlue, RoundedCornerShape(8.dp)).padding(16.dp)) {
                                    Text(t("warn_quote"), color = CyanAccent, textAlign = TextAlign.Center, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { 
                                        showMeditationDialog = false
                                        currentTab = "focus"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenText, contentColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(t("warn_btn_meditate"), fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { showMeditationDialog = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.4f), contentColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(t("warn_btn_resist"), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                AppScreen(
                    currentTab = currentTab, 
                    onTabSelected = handleTabSelect,
                    onNavigateBack = handleBackNavigation,
                    canGoBack = (tabBackStack.size > 1 || currentTab != "home")
                )

                GlassCallOverlay(
                    callSession = callSession,
                    onAccept = { VoiceCallManager.acceptCall(context) },
                    onEndCall = { VoiceCallManager.endCall() },
                    onToggleMute = { VoiceCallManager.toggleMute() },
                    onToggleSpeaker = { VoiceCallManager.toggleSpeaker() }
                )
            }
        }
    }
}

    override fun onResume() {
        super.onResume()
        try {
            UserPresenceManager.onAppForeground()
        } catch (e: Throwable) {
            android.util.Log.e("MainActivity", "onResume presence error", e)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            UserPresenceManager.onAppBackground()
        } catch (e: Throwable) {
            android.util.Log.e("MainActivity", "onPause presence error", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            UserPresenceManager.onAppBackground()
        } catch (e: Throwable) {
            android.util.Log.e("MainActivity", "onDestroy presence error", e)
        }
    }
}

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    androidx.compose.ui.viewinterop.AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            try {
                com.google.android.gms.ads.AdView(context).apply {
                    setAdSize(com.google.android.gms.ads.AdSize.BANNER)
                    adUnitId = "ca-app-pub-3940256099942544/6300978111"
                    adListener = object : com.google.android.gms.ads.AdListener() {
                        override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                            super.onAdFailedToLoad(error)
                        }
                    }
                    loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
                }
            } catch (e: Throwable) {
                android.view.View(context)
            }
        }
    )
}

@Composable
fun UniversalFloatingAiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val isBn = LanguageStore.currentLanguage.value == "bn"
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "ai_pulse")

    val sparkleRotate by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1200, easing = androidx.compose.animation.core.EaseInOutSine),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "sparkleRotate"
    )

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.Transparent,
        shadowElevation = if (isLightMode) 4.dp else 8.dp,
        modifier = modifier
            .padding(end = 4.dp, bottom = 4.dp)
            .height(40.dp)
            .clip(CircleShape)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            if (isLightMode) Color(0xFF0F172A) else Color(0xFF1E2029),
                            if (isLightMode) Color(0xFF1E293B) else Color(0xFF14151D)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF10A37F).copy(alpha = 0.85f),
                            Color(0xFF00B4D8).copy(alpha = 0.85f)
                        )
                    ),
                    shape = CircleShape
                )
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Sparkling AI Icon
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "Noor",
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier
                        .size(16.dp)
                        .then(Modifier.graphicsLayer { rotationZ = sparkleRotate })
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Clean concise title: Noor
                Text(
                    text = "Noor",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

@Composable
fun DraggableFloatingAiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

        val buttonWidthDp = 92.dp
        val buttonHeightDp = 44.dp
        val buttonWidthPx = with(density) { buttonWidthDp.toPx() }
        val buttonHeightPx = with(density) { buttonHeightDp.toPx() }

        val marginSidePx = with(density) { 12.dp.toPx() }
        val marginTopPx = with(density) { 56.dp.toPx() }
        val marginBottomPx = with(density) { 96.dp.toPx() }

        val minXPx = marginSidePx
        val maxXPx = (screenWidthPx - buttonWidthPx - marginSidePx).coerceAtLeast(minXPx)
        val minYPx = marginTopPx
        val maxYPx = (screenHeightPx - buttonHeightPx - marginBottomPx).coerceAtLeast(minYPx)

        val offsetX = remember { androidx.compose.animation.core.Animatable(maxXPx) }
        var offsetY by remember { mutableFloatStateOf(maxYPx - with(density) { 16.dp.toPx() }) }
        val coroutineScope = rememberCoroutineScope()
        var isDragging by remember { mutableStateOf(false) }
        var dragEndedAt by remember { mutableLongStateOf(0L) }

        // Keep inside bounds if orientation or bounds change
        LaunchedEffect(maxXPx, maxYPx, minYPx, minXPx) {
            if (offsetX.value > maxXPx) offsetX.snapTo(maxXPx)
            if (offsetX.value < minXPx) offsetX.snapTo(minXPx)
            if (offsetY > maxYPx) offsetY = maxYPx
            if (offsetY < minYPx) offsetY = minYPx
        }

        fun snapToNearestHorizontalEdge() {
            coroutineScope.launch {
                val currentX = offsetX.value
                val midX = (minXPx + maxXPx) / 2f
                val targetX = if (currentX < midX) minXPx else maxXPx
                offsetX.animateTo(
                    targetValue = targetX,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .offset { androidx.compose.ui.unit.IntOffset(offsetX.value.toInt(), offsetY.toInt()) }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            dragEndedAt = System.currentTimeMillis()
                            snapToNearestHorizontalEdge()
                        },
                        onDragCancel = {
                            isDragging = false
                            dragEndedAt = System.currentTimeMillis()
                            snapToNearestHorizontalEdge()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newX = (offsetX.value + dragAmount.x).coerceIn(minXPx, maxXPx)
                            offsetY = (offsetY + dragAmount.y).coerceIn(minYPx, maxYPx)
                            coroutineScope.launch {
                                offsetX.snapTo(newX)
                            }
                        }
                    )
                }
        ) {
            UniversalFloatingAiButton(
                onClick = {
                    if (!isDragging && (System.currentTimeMillis() - dragEndedAt > 150L)) {
                        onClick()
                    }
                }
            )
        }
    }
}

@Composable
fun UniversalAiChatDialog(
    onClose: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val isBn = LanguageStore.currentLanguage.value == "bn"
    val titleTextColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descTextColor = if (isLightMode) Color(0xFF64748B) else Color.LightGray

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onClose,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        GlassBackground {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Frosted Glass Header Bar
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .glassCard(cornerSize = 18.dp, glassAlpha = 0.95f, hasSheen = true),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .glassCard(cornerSize = 19.dp, borderWidth = 1.dp, hasSheen = true)
                                        .background(
                                            androidx.compose.ui.graphics.Brush.linearGradient(
                                                listOf(Color(0xFF0284C7), Color(0xFF06B6D4))
                                            ),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isBn) "নূর AI সহচর ✨" else "Noor AI Guide ✨",
                                            color = titleTextColor,
                                            fontSize = 15.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        ProBadge()
                                    }
                                    Text(
                                        text = if (isBn) "সরাসরি আত্মশুদ্ধি, রুকিয়া ও দ্বীনি সমাধান" else "Direct Islamic guidance & habit recovery",
                                        color = descTextColor,
                                        fontSize = 10.5.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            IconButton(
                                onClick = onClose,
                                modifier = Modifier
                                    .size(34.dp)
                                    .glassCard(cornerSize = 17.dp, borderWidth = 1.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Close",
                                    tint = if (isLightMode) Color(0xFF0F172A) else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // The full Islamic AI Chat component - ONLY input field rises with keyboard
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .imePadding()
                            .navigationBarsPadding()
                    ) {
                        IslamicAiChat(
                            onNavigateToPremium = onNavigateToPremium
                        )
                    }
                }
            }
        }
    }
}

object ChatNavigationState {
    var isInsideChat = mutableStateOf(false)
}

@Composable
fun FriendsTabContent(
    onTabSelected: (String) -> Unit,
    onSubViewActiveChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val socialViewModel: SocialViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val communityViewModel: CommunityViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    var activeFriendChat by remember { mutableStateOf<UserProfile?>(null) }
    var activeViewUserProfile by remember { mutableStateOf<UserProfile?>(null) }
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(Unit) {
        val activity = context as? android.app.Activity
        val dChatId = activity?.intent?.getStringExtra("open_direct_chat")
        if (dChatId != null) {
            activity.intent.removeExtra("open_direct_chat")
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(dChatId).get().addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        activeFriendChat = doc.toObject(UserProfile::class.java)?.copy(userId = doc.id)
                    }
                }
        }
    }

    val isSubViewActive = activeViewUserProfile != null || activeFriendChat != null
    LaunchedEffect(isSubViewActive) {
        onSubViewActiveChanged(isSubViewActive)
    }

    if (activeViewUserProfile != null) {
        UserDetailScreen(
            userProfile = activeViewUserProfile!!,
            currentUserId = currentUserId,
            socialViewModel = socialViewModel,
            communityViewModel = communityViewModel,
            onBack = { activeViewUserProfile = null },
            onOpenChat = { friendProfile ->
                activeViewUserProfile = null
                activeFriendChat = friendProfile
            }
        )
    } else if (activeFriendChat != null) {
        PrivateChatScreen(
            friendId = activeFriendChat!!.userId,
            socialViewModel = socialViewModel,
            onBack = { activeFriendChat = null },
            onViewProfile = { profile ->
                activeViewUserProfile = profile
                activeFriendChat = null
            }
        )
    } else {
        FriendsListScreen(
            socialViewModel = socialViewModel,
            onBack = { onTabSelected("home") },
            onOpenChat = { friendId -> 
                activeFriendChat = UserProfile(userId = friendId)
            },
            onViewProfile = { profile ->
                activeViewUserProfile = profile
            }
        )
    }
}

object CommunitySubTabState {
    val requestedSubTab = mutableStateOf<String?>(null)
}

@Composable
fun AppScreen(
    currentTab: String, 
    onTabSelected: (String) -> Unit,
    onNavigateBack: () -> Unit = {},
    canGoBack: Boolean = false
) {
    val isPremium by PremiumManager.isPremium.collectAsState()
    var showUniversalAiSheet by remember { mutableStateOf(false) }
    val isInsideChat = ChatNavigationState.isInsideChat.value

    val mainTabs = remember { listOf("home", "salah", "history", "community", "profile") }
    val targetPageIndex = mainTabs.indexOf(currentTab)
    val isMainTab = targetPageIndex != -1

    val pagerState = rememberPagerState(
        initialPage = if (isMainTab) targetPageIndex else 0,
        pageCount = { mainTabs.size }
    )

    var isFriendsSubViewActive by remember { mutableStateOf(false) }

    // Scroll pager when currentTab changes externally (e.g. bottom nav tab click)
    LaunchedEffect(currentTab) {
        if (isMainTab && pagerState.currentPage != targetPageIndex) {
            pagerState.animateScrollToPage(targetPageIndex)
        }
    }

    // Update currentTab when user swipes pager horizontally
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { pageIndex ->
            if (pageIndex in mainTabs.indices) {
                val swipedTab = mainTabs[pageIndex]
                if (swipedTab != currentTab && isMainTab) {
                    onTabSelected(swipedTab)
                }
            }
        }
    }

    // Sequential Back Navigation - returns to previous visited page
    androidx.activity.compose.BackHandler(enabled = canGoBack || currentTab != "home") {
        onNavigateBack()
    }

    GlassBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = { 
                    if (!isInsideChat && currentTab != "winter_arc" && currentTab != "blocker") {
                        Column {
                            if (!isPremium && currentTab != "premium") {
                                BannerAdView(modifier = Modifier.background(Color.Transparent))
                            }
                            AppBottomNav(currentTab, onTabSelected)
                        }
                    }
                },
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.systemBars
            ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                if (isMainTab) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = !isFriendsSubViewActive && !isInsideChat,
                        beyondViewportPageCount = 0
                    ) { page ->
                        when (mainTabs[page]) {
                            "home" -> HomeScreen(
                                onNavigateToPremium = { onTabSelected("premium") },
                                onNavigateToFocus = { onTabSelected("focus") },
                                onNavigateToAI = { showUniversalAiSheet = true },
                                onNavigateToWinterArc = { onTabSelected("winter_arc") },
                                onNavigateToSalah = { onTabSelected("salah") },
                                onNavigateToBlocker = { onTabSelected("blocker") }
                            )
                            "salah" -> SalahTracker(
                                onNavigateToPremium = { onTabSelected("premium") },
                                onNavigateToAI = { showUniversalAiSheet = true }
                            )
                            "history" -> HistoryScreen()
                            "community" -> CommunityScreen(
                                onTabSelected = onTabSelected,
                                onSubViewActiveChanged = { active -> isFriendsSubViewActive = active }
                            )
                            "profile" -> ProfileScreen(
                                onNavigateToStaticPage = onTabSelected,
                                onNavigateToPremium = { onTabSelected("premium") }
                            )
                        }
                    }
                } else {
                    when (currentTab) {
                        "blocker" -> BlockerScreen(
                            onBack = onNavigateBack
                        )
                        "winter_arc" -> {
                            val winterArcViewModel: WinterArcViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                            WinterArcScreen(
                                onBack = onNavigateBack,
                                winterArcViewModel = winterArcViewModel
                            )
                        }
                        "focus" -> FocusScreen()
                        "ai" -> AiScreen(onNavigateToPremium = { onTabSelected("premium") })
                        "badges" -> {
                            val purityTrackerViewModel: PurityTrackerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                            val days by purityTrackerViewModel.durationDays.collectAsState()
                            val badgesViewModel: BadgesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                            BadgesScreen(currentDays = days, badgesViewModel = badgesViewModel, onClose = { onTabSelected("profile") })
                        }
                        "admin_badges" -> AdminBadgeScreen(onBack = { onTabSelected("profile") })
                        "admin_payments" -> AdminPaymentsScreen(onBack = { onTabSelected("profile") })
                        "premium" -> PremiumPaywallScreen(onClose = { onTabSelected("home") })
                    }
                }
            }
        }
    }

        if (showUniversalAiSheet) {
            UniversalAiChatDialog(
                onClose = { showUniversalAiSheet = false },
                onNavigateToPremium = {
                    showUniversalAiSheet = false
                    onTabSelected("premium")
                }
            )
        }
    }
}

@Composable
fun AppBottomNav(currentTab: String, onTabSelected: (String) -> Unit) {
    val socialViewModel: SocialViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val unreadCounts by socialViewModel.unreadCounts.collectAsState()
    val pendingRequests by socialViewModel.friendRequests.collectAsState()
    val totalCommunityBadge = unreadCounts.values.sum() + pendingRequests.size
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value

    val isBn = LanguageStore.currentLanguage.value == "bn"

    val tabs = listOf(
        NavItemData("home", if (isBn) "হোম" else "Home", Icons.Outlined.Home),
        NavItemData("salah", if (isBn) "সালাত" else "Salah", Icons.Outlined.Bedtime),
        NavItemData("history", if (isBn) "হিস্ট্রি" else "History", Icons.Outlined.Schedule),
        NavItemData("community", if (isBn) "কমিউনিটি" else "Community", Icons.Outlined.People, badgeCount = totalCommunityBadge),
        NavItemData("profile", if (isBn) "অ্যাকাউন্ট" else "Account", Icons.Outlined.Person)
    )

    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0A0A).copy(alpha = 0.94f), RoundedCornerShape(32.dp))
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(32.dp))
                .padding(horizontal = 3.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { navItem ->
                    val isSelected = currentTab == navItem.id

                    val activeColor = Color(0xFF38BDF8)
                    val inactiveColor = if (isLightMode) Color(0xFF64748B) else Color.White.copy(alpha = 0.62f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(Color(0xFF38BDF8).copy(alpha = 0.16f), RoundedCornerShape(18.dp))
                                        .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.48f), RoundedCornerShape(18.dp))
                                        .padding(vertical = 6.dp, horizontal = 1.dp)
                                } else {
                                    Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .padding(vertical = 6.dp, horizontal = 1.dp)
                                }
                            )
                            .clickable { onTabSelected(navItem.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = navItem.icon,
                                    contentDescription = navItem.label,
                                    tint = if (isSelected) activeColor else inactiveColor,
                                    modifier = Modifier.size(20.dp)
                                )

                                if (navItem.badgeCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 10.dp, y = (-4).dp)
                                            .background(Color(0xFFEF4444), CircleShape)
                                            .border(1.dp, Color.White, CircleShape)
                                            .sizeIn(minWidth = 15.dp, minHeight = 15.dp)
                                            .padding(horizontal = 3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (navItem.badgeCount > 99) "99+" else navItem.badgeCount.toString(),
                                            color = Color.White,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = navItem.label,
                                color = if (isSelected) activeColor else inactiveColor,
                                fontSize = 10.sp,
                                letterSpacing = (-0.1).sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class NavItemData(
    val id: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun HistoryScreen(
    viewModel: PurityTrackerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    badgesViewModel: BadgesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descColor = if (isLightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val isBn = LanguageStore.currentLanguage.value == "bn"

    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val bestRecord by viewModel.bestRecord.collectAsStateWithLifecycle()
    val currentDurationDays by viewModel.durationDays.collectAsStateWithLifecycle()

    val recordDays = maxOf(currentDurationDays, bestRecord?.durationDays ?: 0)
    val dynamicBadges by badgesViewModel.badges.collectAsStateWithLifecycle()
    val currentBadge = dynamicBadges.lastOrNull { currentDurationDays >= it.requiredDays }
        ?: dynamicBadges.firstOrNull()
        ?: CustomBadge("", "Bronze Warrior", 0, "", "")
    val earnedBadges = dynamicBadges.filter { currentDurationDays >= it.requiredDays }.sortedByDescending { it.requiredDays }
    val unearnedBadges = dynamicBadges.filter { currentDurationDays < it.requiredDays }.sortedBy { it.requiredDays }
    val nextBadge = dynamicBadges.firstOrNull { it.requiredDays > currentDurationDays }
    val nextDaysReq = nextBadge?.requiredDays ?: currentBadge.requiredDays
    val prevDaysReq = currentBadge.requiredDays
    val currentBadgeProgress = if (nextBadge != null && nextDaysReq > prevDaysReq) {
        ((currentDurationDays - prevDaysReq).toFloat() / (nextDaysReq - prevDaysReq).toFloat()).coerceIn(0f, 1f)
    } else {
        1f
    }

    var showClearAllDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<SessionHistory?>(null) }
    val formatter = remember { java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Title
        Text(
            text = if (isBn) "হিস্ট্রি ও মাইলস্টোন" else "History",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = titleColor
        )

        // Current Active Badge Hero Card (Glass UI matching Screenshot 2)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerSize = 20.dp, glassAlpha = 0.95f, hasSheen = true),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Golden Trophy Glow Circular Badge Container (Auto-cropped full circle fill)
                    Box(
                        modifier = Modifier.size(68.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                        colors = listOf(Color(0xFFF59E0B).copy(alpha = 0.35f), Color.Transparent)
                                    ),
                                    shape = CircleShape
                                )
                        )
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color(0xFFFDE047), Color(0xFFD97706))
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentBadge.imageUrl.isNotEmpty()) {
                                BadgeImage(
                                    imageUrl = currentBadge.imageUrl,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .graphicsLayer(scaleX = 1.14f, scaleY = 1.14f),
                                    fallbackName = currentBadge.name,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFF78350F),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(2.dp, Color(0xFFFBBF24), CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = (if (isBn) "বর্তমান অর্জিত ব্যাজ" else "CURRENT BADGE").uppercase(),
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = getCustomBadgeName(currentBadge),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isBn) "$currentDurationDays দিন স্ট্রিক · সেরা $recordDays দিন" else "$currentDurationDays days streak · Best $recordDays days",
                            fontSize = 12.5.sp,
                            color = descColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Linear Progress Indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isLightMode) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.14f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(currentBadgeProgress.coerceIn(0.06f, 1f))
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF38BDF8), Color(0xFFFBBF24))
                                ),
                                RoundedCornerShape(3.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBn) "পরবর্তী: ${if (nextBadge != null) getCustomBadgeName(nextBadge) else "সর্বোচ্চ"}"
                               else "Next: ${nextBadge?.name ?: "Max"}",
                        fontSize = 11.5.sp,
                        color = descColor
                    )
                    Text(
                        text = if (nextBadge != null) {
                            if (isBn) "${maxOf(0, nextDaysReq - currentDurationDays)} দিন বাকি"
                            else "${maxOf(0, nextDaysReq - currentDurationDays)} days left"
                        } else {
                            if (isBn) "সর্বোচ্চ স্তরে আছেন" else "Completed"
                        },
                        fontSize = 11.5.sp,
                        color = descColor
                    )
                }
            }
        }

        // Section: Milestones (3-Column Compact Grid matching Screenshot 2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isBn) "মাইলস্টোনসমূহ" else "Milestones",
                color = titleColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = if (isBn) "${earnedBadges.size} অর্জিত · ${unearnedBadges.size} লকড"
                       else "${earnedBadges.size} earned · ${unearnedBadges.size} locked",
                color = descColor,
                fontSize = 12.sp
            )
        }

        // 3-Column Compact Milestone Cards Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            dynamicBadges.chunked(3).forEach { rowBadges ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowBadges.forEach { badge ->
                        val isUnlocked = currentDurationDays >= badge.requiredDays
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 122.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = BorderStroke(
                                if (isUnlocked) 1.2.dp else 1.dp,
                                if (isUnlocked) Color(0xFFF59E0B).copy(alpha = 0.68f)
                                else Color.White.copy(alpha = if (isLightMode) 0.14f else 0.14f)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 122.dp)
                                    .background(
                                        if (isUnlocked) {
                                            if (isLightMode) Color(0xFFFEF3C7).copy(alpha = 0.85f) else Color(0xFF1E170A).copy(alpha = 0.65f)
                                        } else {
                                            if (isLightMode) Color(0xFFF1F5F9).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.06f)
                                        }
                                    )
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isUnlocked) Color(0xFFF59E0B).copy(alpha = 0.20f) else Color.White.copy(alpha = 0.08f),
                                                CircleShape
                                            )
                                            .border(
                                                1.5.dp,
                                                if (isUnlocked) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.16f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isUnlocked) {
                                            if (badge.imageUrl.isNotEmpty()) {
                                                BadgeImage(
                                                    imageUrl = badge.imageUrl,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(CircleShape)
                                                        .graphicsLayer(scaleX = 1.14f, scaleY = 1.14f),
                                                    fallbackName = badge.name,
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Filled.EmojiEvents,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFBBF24),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        } else {
                                            Icon(
                                                Icons.Outlined.Lock,
                                                contentDescription = null,
                                                tint = if (isLightMode) Color(0xFF64748B) else Color.White.copy(alpha = 0.62f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(7.dp))
                                    Text(
                                        text = getCustomBadgeName(badge),
                                        color = titleColor,
                                        fontSize = 11.5.sp,
                                        lineHeight = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = if (isUnlocked) {
                                            if (isBn) "অর্জিত" else "Unlocked"
                                        } else {
                                            "${badge.requiredDays} ${if (isBn) "দিন" else "days"}"
                                        },
                                        color = if (isUnlocked) Color(0xFF10B981) else descColor,
                                        fontSize = 10.5.sp,
                                        lineHeight = 13.sp,
                                        maxLines = 1,
                                        fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                    repeat(3 - rowBadges.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Section: Session History (Matching Screenshot 2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isBn) "সেশন হিস্ট্রি" else "Session history",
                color = titleColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isBn) "${historyList.size}টি সেশন" else "${historyList.size} sessions",
                    color = descColor,
                    fontSize = 12.sp
                )
                if (historyList.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { showClearAllDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = RedAlert),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = if (isBn) "মুছুন" else "Clear",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (historyList.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerSize = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF10B981).copy(alpha = 0.18f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (isBn) "কোনো রিল্যাপ্স রেকর্ড নেই" else "No relapses recorded",
                            color = titleColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isBn) "আপনার হিস্ট্রি সম্পূর্ণ নিরাপদ ও পরিচ্ছন্ন।" else "Your history is completely clean.",
                            color = descColor,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            historyList.forEach { history ->
                val hCal = Calendar.getInstance().apply { timeInMillis = history.startTime }
                val hStartStr = formatter.format(hCal.time)
                TrackedSessionItem(
                    title = history.title,
                    desc = "${if (isBn) "শুরু" else "Start"}: $hStartStr",
                    duration = history.statusText,
                    icon = Icons.Filled.History,
                    iconTint = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                    durationColor = titleColor,
                    onDelete = {
                        itemToDelete = history
                    }
                )
            }
        }
    }

    // Clear All Confirmation Dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = if (isLightMode) Color.White else Color(0xFF1E293B),
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(RedAlert.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = RedAlert, modifier = Modifier.size(24.dp))
                }
            },
            title = {
                Text(
                    text = if (isBn) "সকল হিস্ট্রি মুছে ফেলবেন?" else "Clear All History?",
                    color = titleColor,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isBn) "আপনি কি নিশ্চিত যে সমস্ত অতীত হিস্ট্রি মুছে ফেলতে চান? এটি স্থায়ীভাবে মুছে যাবে।" else "Are you sure you want to permanently delete all past session history records? This cannot be undone.",
                    color = descColor,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllHistoryRecords()
                        showClearAllDialog = false
                        android.widget.Toast.makeText(
                            context,
                            if (isBn) "সকল হিস্ট্রি সফলভাবে মুছে ফেলা হয়েছে" else "All history records deleted successfully",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAlert),
                    shape = CircleShape
                ) {
                    Text(if (isBn) "হ্যাঁ, সব মুছুন" else "Yes, Delete All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = descColor)
                }
            }
        )
    }

    // Delete Single Item Confirmation Dialog
    if (itemToDelete != null) {
        val targetItem = itemToDelete!!
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = if (isLightMode) Color.White else Color(0xFF1E293B),
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(RedAlert.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = RedAlert, modifier = Modifier.size(24.dp))
                }
            },
            title = {
                Text(
                    text = if (isBn) "হিস্ট্রি আইটেম মুছবেন?" else "Delete History Entry?",
                    color = titleColor,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isBn) "আপনি কি '${targetItem.title}' (${targetItem.statusText}) রেকর্ডটি মুছে ফেলতে চান?" else "Are you sure you want to delete '${targetItem.title}' (${targetItem.statusText})?",
                    color = descColor,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteHistoryItem(targetItem)
                        itemToDelete = null
                        android.widget.Toast.makeText(
                            context,
                            if (isBn) "হিস্ট্রি মুছে ফেলা হয়েছে" else "History entry deleted",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAlert),
                    shape = CircleShape
                ) {
                    Text(if (isBn) "মুছে ফেলুন" else "Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = descColor)
                }
            }
        )
    }
}

@Composable
fun EarnedBadgeItem(
    badge: CustomBadge,
    isCurrent: Boolean
) {
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descColor = if (isLightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val isBn = LanguageStore.currentLanguage.value == "bn"

    Card(
        modifier = Modifier.fillMaxWidth().glassCard(cornerSize = 14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(
                        if (isCurrent) 2.dp else 1.dp,
                        if (isCurrent) (if (isLightMode) Color(0xFF0284C7) else CyanAccent) else Color(0xFF34C759).copy(alpha = 0.5f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                BadgeImage(
                    imageUrl = badge.imageUrl,
                    modifier = Modifier.fillMaxSize(),
                    fallbackName = badge.name
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = getCustomBadgeName(badge),
                        color = titleColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        style = TextStyle(shadow = floatingTextShadow(elevation = 2.dp))
                    )
                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background((if (isLightMode) Color(0xFF0284C7) else CyanAccent).copy(alpha = 0.15f), CircleShape)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isBn) "সক্রিয়" else "Active",
                                color = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = if (isBn) "মাইলস্টোন: ${badge.requiredDays} দিন" else "Milestone: ${badge.requiredDays} days",
                    color = descColor,
                    fontSize = 11.5.sp,
                    style = TextStyle(shadow = floatingTextShadow(elevation = 1.2.dp))
                )
            }
            Box(
                modifier = Modifier
                    .background(Color(0xFF34C759).copy(alpha = 0.12f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color(0xFF34C759),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBn) "অর্জিত" else "Unlocked",
                        color = Color(0xFF34C759),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun UnearnedBadgeItem(
    badge: CustomBadge,
    currentDays: Int
) {
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descColor = if (isLightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val isBn = LanguageStore.currentLanguage.value == "bn"
    val daysRemaining = maxOf(0, badge.requiredDays - currentDays)
    val progress = (currentDays.toFloat() / badge.requiredDays.toFloat()).coerceIn(0f, 1f)
    val progressPercent = (progress * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth().glassCard(cornerSize = 14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (badge.imageUrl.isNotBlank()) {
                        BadgeImage(
                            imageUrl = badge.imageUrl,
                            modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.45f),
                            fallbackName = badge.name
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = "Locked",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (isLightMode) Color(0xFFF1F5F9) else Color.White.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = "Locked",
                                tint = if (isLightMode) Color(0xFF334155) else Color(0xFFE2E8F0),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = getCustomBadgeName(badge),
                        color = titleColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        style = TextStyle(shadow = floatingTextShadow(elevation = 2.dp))
                    )
                    Text(
                        text = if (isBn) "প্রয়োজনীয় দিন: ${badge.requiredDays} দিন" else "Required: ${badge.requiredDays} days",
                        color = descColor,
                        fontSize = 11.5.sp,
                        style = TextStyle(shadow = floatingTextShadow(elevation = 1.2.dp))
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            (if (isLightMode) Color(0xFF0284C7) else CyanAccent).copy(alpha = 0.12f),
                            CircleShape
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isBn) "আর $daysRemaining দিন বাকি" else "$daysRemaining days left",
                        color = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(shadow = floatingTextShadow(elevation = 1.5.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape),
                    color = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                    trackColor = if (isLightMode) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.10f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "$progressPercent%",
                    color = descColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = TextStyle(shadow = floatingTextShadow(elevation = 1.2.dp))
                )
            }
        }
    }
}

@Composable
fun TrackedSessionItem(
    title: String,
    desc: String,
    duration: String,
    icon: ImageVector = Icons.Filled.History,
    iconTint: Color = CyanAccent,
    durationColor: Color = Color.White,
    onDelete: (() -> Unit)? = null
) {
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descColor = if (isLightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val finalDurationColor = if (durationColor == Color.White && isLightMode) Color(0xFF0F172A) else durationColor

    Card(
         modifier = Modifier.fillMaxWidth().glassCard(cornerSize = 14.dp),
         colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
         Row(
             modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
             verticalAlignment = Alignment.CenterVertically
         ) {
             Box(
                 modifier = Modifier
                     .size(40.dp)
                     .background(iconTint.copy(alpha = 0.12f), CircleShape),
                 contentAlignment = Alignment.Center
             ) {
                 Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
             }
             Spacer(modifier = Modifier.width(12.dp))
             Column(modifier = Modifier.weight(1f)) {
                 Text(
                     title,
                     color = titleColor,
                     fontWeight = FontWeight.Bold,
                     fontSize = 14.sp,
                     style = TextStyle(shadow = floatingTextShadow(elevation = 2.dp))
                 )
                 Text(
                     desc,
                     color = descColor,
                     fontSize = 11.5.sp,
                     style = TextStyle(shadow = floatingTextShadow(elevation = 1.2.dp))
                 )
             }
             Text(
                 duration,
                 color = finalDurationColor,
                 fontWeight = FontWeight.Bold,
                 fontSize = 13.sp,
                 style = TextStyle(shadow = floatingTextShadow(elevation = 1.5.dp))
             )
             if (onDelete != null) {
                 Spacer(modifier = Modifier.width(8.dp))
                 IconButton(
                     onClick = onDelete,
                     modifier = Modifier.size(36.dp)
                 ) {
                     Icon(
                         Icons.Filled.Delete,
                         contentDescription = "Delete",
                         tint = RedAlert.copy(alpha = 0.85f),
                         modifier = Modifier.size(20.dp)
                     )
                 }
             }
         }
    }
}

@Composable
fun FocusScreen() {
    val context = LocalContext.current
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val isBn = LanguageStore.currentLanguage.value == "bn"
    val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descColor = if (isLightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val cardBg = if (isLightMode) Color.White.copy(alpha = 0.92f) else CardBlue
    val cardBorder = if (isLightMode) BorderStroke(1.dp, Color(0xFFE2E8F0)) else BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))

    var isPlaying by remember { mutableStateOf(false) }
    var exerciseState by remember { mutableStateOf("Inhale") }
    var secondsLeft by remember { mutableStateOf(4) }
    var totalSeconds by remember { mutableStateOf(0L) }

    // Persistent Sound Settings
    val soundPrefs = remember { context.getSharedPreferences("sound_prefs", Context.MODE_PRIVATE) }
    var selectedSound by remember { mutableStateOf(soundPrefs.getString("saved_sound_choice", "Rain") ?: "Rain") }
    val isAmbientSoundPlaying by AmbientSoundPlayer.isPlayingFlow.collectAsState()

    val soundList = listOf(
        "Rain" to if (isBn) "🌧️ বৃষ্টি" else "🌧️ Rain",
        "Ocean" to if (isBn) "🌊 সাগর" else "🌊 Ocean",
        "Breeze" to if (isBn) "🍃 বাতাস" else "🍃 Breeze",
        "Bell" to if (isBn) "🧘 ধ্যান" else "🧘 Meditate",
        "Silence" to if (isBn) "🔕 অফ" else "🔕 Mute"
    )

    // Stop sound when exiting FocusScreen
    DisposableEffect(Unit) {
        onDispose {
            AmbientSoundPlayer.stopSound()
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            exerciseState = "Inhale"
            secondsLeft = 4
            totalSeconds = 0L
            while (isActive) {
                delay(1000)
                totalSeconds++
                secondsLeft--
                if (secondsLeft <= 0) {
                    when (exerciseState) {
                        "Inhale" -> {
                            exerciseState = "Hold_In"
                            secondsLeft = 4
                        }
                        "Hold_In" -> {
                            exerciseState = "Exhale"
                            secondsLeft = 4
                        }
                        "Exhale" -> {
                            exerciseState = "Hold_Out"
                            secondsLeft = 4
                        }
                        "Hold_Out" -> {
                            exerciseState = "Inhale"
                            secondsLeft = 4
                        }
                        else -> {
                            exerciseState = "Inhale"
                            secondsLeft = 4
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(t("focus_mode_header"), color = titleColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(t("focus_desc_top"), color = descColor, fontSize = 13.sp, textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.height(16.dp))

        // Persistent Sound Preference Selector Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerSize = 16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = cardBorder
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isAmbientSoundPlaying && selectedSound != "Silence") Icons.Filled.VolumeUp else Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "মেডিটেশন ব্যাকগ্রাউন্ড সাউন্ড" else "Ambient Meditation Sound",
                            color = titleColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            maxLines = 1
                        )
                        if (isAmbientSoundPlaying && selectedSound != "Silence") {
                            Spacer(modifier = Modifier.width(6.dp))
                            AdhanEqualizerWaveform()
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Play/Stop preview button
                    Surface(
                        onClick = {
                            if (isAmbientSoundPlaying) {
                                AmbientSoundPlayer.stopSound()
                            } else {
                                val target = if (selectedSound == "Silence") "Rain" else selectedSound
                                selectedSound = target
                                soundPrefs.edit().putString("saved_sound_choice", target).apply()
                                AmbientSoundPlayer.startSound(target)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isAmbientSoundPlaying && selectedSound != "Silence") {
                            if (isLightMode) Color(0xFFDCFCE7) else Color(0xFF10B981).copy(alpha = 0.25f)
                        } else {
                            if (isLightMode) Color(0xFFE0F2FE) else CyanAccent.copy(alpha = 0.15f)
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = if (isAmbientSoundPlaying && selectedSound != "Silence") Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = if (isAmbientSoundPlaying && selectedSound != "Silence") {
                                    if (isLightMode) Color(0xFF15803D) else Color(0xFF34D399)
                                } else {
                                    if (isLightMode) Color(0xFF0369A1) else CyanAccent
                                },
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAmbientSoundPlaying && selectedSound != "Silence") {
                                    if (isBn) "বাজছে (থামান)" else "Playing (Stop)"
                                } else {
                                    if (isBn) "শুনুন" else "Play"
                                },
                                color = if (isAmbientSoundPlaying && selectedSound != "Silence") {
                                    if (isLightMode) Color(0xFF15803D) else Color(0xFF34D399)
                                } else {
                                    if (isLightMode) Color(0xFF0369A1) else CyanAccent
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    soundList.forEach { (sndKey, sndLabel) ->
                        val isSndSelected = selectedSound == sndKey
                        val chipBg = if (isSndSelected) {
                            if (isLightMode) Color(0xFF0284C7) else CyanAccent
                        } else {
                            if (isLightMode) Color(0xFFF1F5F9) else Color.White.copy(alpha = 0.08f)
                        }
                        val chipFg = if (isSndSelected) {
                            Color.White
                        } else {
                            titleColor
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(chipBg)
                                .clickable {
                                    selectedSound = sndKey
                                    soundPrefs.edit().putString("saved_sound_choice", sndKey).apply()
                                    if (sndKey == "Silence") {
                                        AmbientSoundPlayer.stopSound()
                                    } else {
                                        AmbientSoundPlayer.startSound(sndKey)
                                    }
                                    val toastMsg = if (sndKey == "Silence") {
                                        if (isBn) "সাউন্ড বন্ধ করা হয়েছে 🔕" else "Sound muted 🔕"
                                    } else {
                                        if (isBn) "সাউন্ড বাজছে: $sndLabel 🔊" else "Playing: $sndLabel 🔊"
                                    }
                                    android.widget.Toast.makeText(context, toastMsg, android.widget.Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 10.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sndLabel,
                                color = chipFg,
                                fontSize = 11.sp,
                                fontWeight = if (isSndSelected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val animatedScale by androidx.compose.animation.core.animateFloatAsState(
            targetValue = if (!isPlaying) 1f else when (exerciseState) {
                "Inhale" -> 1.25f
                "Hold_In" -> 1.25f
                "Exhale" -> 0.75f
                "Hold_Out" -> 0.75f
                else -> 1f
            },
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = 4000,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            ),
            label = "breathing_scale"
        )

        val actualScale = animatedScale

        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = actualScale
                        scaleY = actualScale
                    }
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = if (isLightMode) Color(0xFF0284C7) else CyanAccent.copy(alpha = 0.5f),
                    strokeWidth = 4.dp
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val stateText = if (isPlaying) {
                    when (exerciseState) {
                        "Inhale" -> t("focus_inhale")
                        "Hold_In", "Hold_Out", "Hold" -> t("focus_hold")
                        "Exhale" -> t("focus_exhale")
                        else -> t("focus_inhale")
                    }
                } else {
                    t("focus_ready")
                }
                Text(stateText, color = titleColor, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                if (isPlaying) {
                    Text("$secondsLeft ${t("seconds")}", color = descColor, fontSize = 16.sp)
                } else {
                    Text(t("focus_press_play"), color = descColor, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        val days = totalSeconds / (24 * 3600)
        val hours = (totalSeconds % (24 * 3600)) / 3600
        val mins = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60

        Text(
            String.format("%02d : %02d : %02d : %02d", days, hours, mins, secs),
            color = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Text("${t("days")} : ${t("hours")} : ${t("minutes")} : ${t("seconds")}", color = descColor, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(20.dp))

        IconButton(
            onClick = { 
                if (isPlaying) {
                    isPlaying = false
                    AmbientSoundPlayer.stopSound()
                } else {
                    isPlaying = true
                    totalSeconds = 0L
                    exerciseState = "Inhale"
                    secondsLeft = 4
                    AmbientSoundPlayer.startSound(selectedSound)
                }
            }, 
            modifier = Modifier.size(60.dp).background(if (isPlaying) RedAlert else GreenText, CircleShape)
        ) {
             Icon(
                 if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow, 
                 contentDescription = if (isPlaying) "Stop" else "Play", 
                 tint = Color.White, 
                 modifier = Modifier.size(30.dp)
             )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(t("focus_desc_bottom"), color = descColor, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun AiScreen(onNavigateToPremium: (() -> Unit)? = null) {
    var isIslamicAi by remember { mutableStateOf(true) }
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
            Card(
                colors = CardDefaults.cardColors(containerColor = if(isIslamicAi) (if (isLightMode) Color(0xFF0284C7) else CardBlue) else (if (isLightMode) Color(0xFFE2E8F0) else DeepBlue)),
                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                modifier = Modifier.clickable { isIslamicAi = true }
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = if (isIslamicAi && isLightMode) Color.White else GreenText, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(t("tab_islamic_ai"), color = if (isIslamicAi) Color.White else (if (isLightMode) Color(0xFF0F172A) else Color.White))
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = if(!isIslamicAi) (if (isLightMode) Color(0xFF0284C7) else CardBlue) else (if (isLightMode) Color(0xFFE2E8F0) else DeepBlue)),
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                modifier = Modifier.clickable { isIslamicAi = false }
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Book, contentDescription = null, tint = if (!isIslamicAi && isLightMode) Color.White else CyanAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(t("tab_salah_tracker"), color = if (!isIslamicAi) Color.White else (if (isLightMode) Color(0xFF0F172A) else Color.White))
                }
            }
        }

        if (isIslamicAi) {
            IslamicAiChat(onNavigateToPremium = onNavigateToPremium)
        } else {
            SalahTracker()
        }
    }
}

@Composable
fun IslamicAiChat(aiViewModel: AiViewModel = viewModel(), onNavigateToPremium: (() -> Unit)? = null) {
    val messages by aiViewModel.messages.collectAsState()
    val isLoading by aiViewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    val context = LocalContext.current
    val speakingMessageId by aiViewModel.speakingMessageId.collectAsState()
    val loadingAudioMessageId by aiViewModel.loadingAudioMessageId.collectAsState()
    val ttsError by aiViewModel.ttsError.collectAsState()

    LaunchedEffect(ttsError) {
        val err = ttsError
        if (!err.isNullOrBlank()) {
            android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
            aiViewModel.clearTtsError()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            aiViewModel.stopSpeaking()
        }
    }

    val speakMessage: (String, String) -> Unit = { id, text ->
        aiViewModel.speakMessage(id, text)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Scroll to the end of stream as text builds up live
    val currentLastMsg = messages.lastOrNull()
    if (currentLastMsg != null && currentLastMsg.isStreaming) {
        LaunchedEffect(currentLastMsg.text) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        androidx.compose.foundation.lazy.LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                if (msg.text == "limit_reached") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.08f)),
                        border = BorderStroke(1.2.dp, Color(0xFFFFD700).copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                if (LanguageStore.currentLanguage.value == "bn") "দৈনিক ফ্রি এআই কাউন্সেলিং সীমা শেষ! 👑" else "Daily Free AI Limit Reached! 👑",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                if (LanguageStore.currentLanguage.value == "bn") "আপনার দৈনিক ফ্রি এআই মেসেজের সীমা (৩টি) শেষ হয়েছে। পবিত্র চরিত্র গঠন ও আসক্তি নিরাময় সফরকে চিরতরে সফল করতে দয়া করে আমাদের মেম্বারশিপে জয়েন করুন।" else "Your daily free AI message limit (3/3) has been reached. Please join Warrior Pro layout to enjoy unlimited counseling alerts & VIP services.",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { onNavigateToPremium?.invoke() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                shape = CircleShape
                            ) {
                                Text(if (LanguageStore.currentLanguage.value == "bn") "যোদ্ধা প্রো-তে যোগ দিন ৳১৪৯" else "Upgrade to Warrior Pro for BDT 149", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
                        
                        if (msg.isUser) {
                            // Telegram User Bubble - Signature Telegram Blue Glossy Capsule
                            Box(
                                modifier = Modifier
                                    .padding(start = 48.dp, end = 0.dp)
                                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp))
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(Color(0xFF229ED9), Color(0xFF1E88E5))
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent)
                                        ),
                                        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        msg.text,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Filled.DoneAll,
                                        contentDescription = "Read",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(15.dp).padding(bottom = 1.dp)
                                    )
                                }
                            }
                        } else {
                            // Telegram AI Bubble - Frosted Glass Card
                            Box(
                                modifier = Modifier
                                    .padding(start = 0.dp, end = 48.dp)
                                    .glassCard(cornerSize = 18.dp, glassAlpha = 0.96f, hasSheen = true)
                                    .padding(16.dp)
                            ) {
                                Column {
                                    val msgText = if (messages.firstOrNull() == msg && !msg.isUser) t("ai_welcome_text") else msg.text
                                    Text(
                                        msgText,
                                        color = if (isLightMode) Color(0xFF0F172A) else Color.White,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )
                                    
                                    // AI message interactive utility options
                                    if (msg.text.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            // 1. Gemini Pegasus Voice Play/Stop button
                                            val isCurrentlySpeaking = speakingMessageId == msg.id
                                            val isLoadingAudio = loadingAudioMessageId == msg.id
                                            IconButton(
                                                onClick = {
                                                    if (isCurrentlySpeaking || isLoadingAudio) {
                                                        aiViewModel.stopSpeaking()
                                                    } else if (!msg.isStreaming) {
                                                        speakMessage(msg.id, msgText)
                                                    }
                                                },
                                                enabled = !msg.isStreaming,
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                if (isLoadingAudio) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        strokeWidth = 2.dp,
                                                        color = Color(0xFF229ED9)
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = if (isCurrentlySpeaking) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                                                        contentDescription = t("listen"),
                                                        tint = if (isCurrentlySpeaking) Color(0xFF229ED9) else if (isLightMode) Color(0xFF64748B) else Color(0xFF94A3B8),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            // 2. Clipboard copy button
                                            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(msgText))
                                                    android.widget.Toast.makeText(context, LanguageStore.get("message_copied"), android.widget.Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.ContentCopy,
                                                    contentDescription = t("copy_message"),
                                                    tint = if (isLightMode) Color(0xFF64748B) else Color(0xFF94A3B8),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.weight(1f))

                                            // 3. Like button
                                            IconButton(
                                                onClick = {
                                                    aiViewModel.toggleFavorite(msg)
                                                },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (msg.isFavorite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                                    contentDescription = "Save",
                                                    tint = if (msg.isFavorite) Color(0xFFFF9500) else if (isLightMode) Color(0xFF64748B) else Color(0xFF94A3B8),
                                                    modifier = Modifier.size(17.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (isLoading) {
                item {
                    val isLight = com.example.ui.theme.GlassTheme.isLightMode.value
                    Box(
                        modifier = Modifier
                            .glassCard(cornerSize = 14.dp, glassAlpha = 0.85f)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            t("ai_thinking"),
                            color = if (isLight) Color(0xFF0284C7) else Color(0xFF2AABEE),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Telegram Frosted Input Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerSize = 28.dp, glassAlpha = 0.95f, hasSheen = true)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
            val tfTextColor = if (isLightMode) Color(0xFF0F172A) else Color.White
            
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        t("chat_hint"),
                        color = if (isLightMode) Color(0xFF64748B) else Color(0xFF94A3B8),
                        fontSize = 13.5.sp
                    )
                },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = tfTextColor,
                    unfocusedTextColor = tfTextColor
                ),
                shape = RoundedCornerShape(24.dp)
            )
            
            IconButton(
                onClick = { 
                    aiViewModel.sendMessage(inputText)
                    inputText = "" 
                }, 
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF229ED9), CircleShape),
                enabled = !isLoading && inputText.isNotBlank()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SalahTracker(
    viewModel: SalahViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateToPremium: (() -> Unit)? = null,
    onNavigateToAI: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descColor = if (isLightMode) Color(0xFF475569) else Color(0xFF94A3B8)
    val isBn = LanguageStore.currentLanguage.value == "bn"

    val todaySalahs by viewModel.todaySalahs.collectAsState()
    val past7Days by viewModel.past7DaysCounts.collectAsState()
    val timings by viewModel.todayTimings.collectAsState()

    val isAdhanEnabled by AdhanPlayer.isEnabledFlow.collectAsState()
    val isAdhanPlaying by AdhanPlayer.isPlayingFlow.collectAsState()
    val selectedAdhanSource by AdhanPlayer.selectedAdhanFlow.collectAsState()

    val completedCount = todaySalahs.take(5).count { it }

    val fullSchedule = remember(timings, isBn) {
        PrayerTimeHelper.getFullPrayerSchedule(timings, isBn)
    }

    val currentActiveWaqtKey = remember(timings) {
        val cal = java.util.Calendar.getInstance()
        val nowMinutes = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
        fun parseMin(raw: String?, defH: Int, defM: Int): Int {
            if (raw.isNullOrBlank()) return defH * 60 + defM
            val clean = raw.trim().split(" ")[0]
            val parts = clean.split(":")
            return if (parts.size >= 2) {
                (parts[0].toIntOrNull() ?: defH) * 60 + (parts[1].toIntOrNull() ?: defM)
            } else defH * 60 + defM
        }
        val fajr = parseMin(timings["Fajr"], 4, 50)
        val sunrise = parseMin(timings["Sunrise"], 6, 9)
        val dhuhr = parseMin(timings["Dhuhr"], 11, 45)
        val asr = parseMin(timings["Asr"], 15, 40)
        val maghrib = parseMin(timings["Maghrib"], 17, 15)
        val isha = parseMin(timings["Isha"], 18, 35)
        when {
            nowMinutes in fajr until sunrise -> "Fajr"
            nowMinutes in sunrise until (sunrise + 20) -> "Sunrise"
            nowMinutes in dhuhr until asr -> "Dhuhr"
            nowMinutes in asr until (maghrib - 15) -> "Asr"
            nowMinutes in (maghrib - 15) until maghrib -> "Sunset"
            nowMinutes in maghrib until isha -> "Maghrib"
            nowMinutes >= isha || nowMinutes < fajr -> "Isha"
            else -> "Dhuhr"
        }
    }

    var showTrackerMosqueDialog by remember { mutableStateOf(false) }
    var globalMinuteOffset by remember {
        mutableStateOf(
            context.getSharedPreferences("mosque_offset_prefs", Context.MODE_PRIVATE)
                .getInt("offset_Dhuhr", 0)
        )
    }

    if (showTrackerMosqueDialog) {
        MosqueOffsetDialog(
            onDismiss = {
                showTrackerMosqueDialog = false
                globalMinuteOffset = context.getSharedPreferences("mosque_offset_prefs", Context.MODE_PRIVATE)
                    .getInt("offset_Dhuhr", 0)
            },
            onSaved = {
                globalMinuteOffset = context.getSharedPreferences("mosque_offset_prefs", Context.MODE_PRIVATE)
                    .getInt("offset_Dhuhr", 0)
                PrayerScheduler.scheduleDailyPrayers(context)
            }
        )
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        AdhanPlayer.initialize(context)
        PrayerScheduler.scheduleDailyPrayers(context)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header Row: "Salah" + "✨ Noor" pill button (matching Salah@1x.png)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "সালাত" else "Salah",
                    color = titleColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF0D2538))
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f), RoundedCornerShape(50))
                            .clickable { onNavigateToAI?.invoke() }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "Noor AI",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Noor",
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 1. Dynamic Motion & Time Scenery Card (Preserved as requested by user)
        item {
            PrayerSceneryCard(
                timings = timings,
                onOpenAllPrayers = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(index = 2)
                    }
                }
            )
        }

        // 2. Unified Salah Tracker Card (Header + 5-Segment Bar + Ayah + Connected Timeline Schedule)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerSize = 22.dp, glassAlpha = 0.95f, hasSheen = true),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Header Row: "Salah Tracker" + "1 / 5" pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "সালাত ট্র্যাকার" else "Salah Tracker",
                            color = titleColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF0F293A), RoundedCornerShape(50))
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$completedCount / 5",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5-Segment Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 0 until 5) {
                            val isSegDone = i < completedCount
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (isSegDone) Color(0xFF38BDF8)
                                        else Color.White.copy(alpha = 0.12f)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Italic Ayah Quote
                    Text(
                        text = if (isBn) "\"নিশ্চয়ই সালাত মানুষকে অশ্লীল ও মন্দ কাজ থেকে বিরত রাখে।\" (আল-আনকাবুত ২৯:৪৫)"
                               else "\"Indeed, prayer prohibits immorality and wrongdoing.\" (Al-Ankabut 29:45)",
                        color = descColor,
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Connected Timeline Rows for All Waqts (Connected by a continuous vertical rope/line)
                    fullSchedule.forEachIndexed { idx, scheduleItem ->
                        val isChecked = if (scheduleItem.isCheckable && scheduleItem.checkIndex in 0..5) {
                            todaySalahs.getOrElse(scheduleItem.checkIndex) { false }
                        } else {
                            false
                        }
                        val prevChecked = if (idx > 0) {
                            val prevItem = fullSchedule[idx - 1]
                            prevItem.isCheckable && prevItem.checkIndex in 0..5 && todaySalahs.getOrElse(prevItem.checkIndex) { false }
                        } else false
                        val nextChecked = if (idx < fullSchedule.lastIndex) {
                            val nextItem = fullSchedule[idx + 1]
                            nextItem.isCheckable && nextItem.checkIndex in 0..5 && todaySalahs.getOrElse(nextItem.checkIndex) { false }
                        } else false

                        val isProhibited = scheduleItem.key == "Sunrise" || scheduleItem.key == "Sunset"
                        val isOptional = scheduleItem.key == "Tahajjud"
                        val isCurrentWaqt = scheduleItem.key == currentActiveWaqtKey && !isChecked
                        val name = if (isBn) scheduleItem.nameBn else scheduleItem.nameEn
                        val desc = if (isBn) scheduleItem.descBn else scheduleItem.descEn

                        val topRopeColor = when {
                            isChecked && prevChecked -> Color(0xFF38BDF8)
                            isChecked || prevChecked -> Color(0xFF38BDF8).copy(alpha = 0.65f)
                            isCurrentWaqt || isProhibited -> Color(0xFFFBBF24).copy(alpha = 0.55f)
                            else -> Color.White.copy(alpha = 0.20f)
                        }
                        val bottomRopeColor = when {
                            isChecked && nextChecked -> Color(0xFF38BDF8)
                            isChecked || nextChecked -> Color(0xFF38BDF8).copy(alpha = 0.65f)
                            isCurrentWaqt || isProhibited -> Color(0xFFFBBF24).copy(alpha = 0.55f)
                            else -> Color.White.copy(alpha = 0.20f)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isCurrentWaqt) Color(0xFFF59E0B).copy(alpha = 0.09f)
                                    else Color.Transparent
                                )
                                .clickable(enabled = scheduleItem.isCheckable) {
                                    if (scheduleItem.isCheckable && scheduleItem.checkIndex in 0..5) {
                                        viewModel.toggleSalah(scheduleItem.checkIndex)
                                    }
                                }
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Timeline Node Circle + Vertical Connecting Rope
                            Box(
                                modifier = Modifier
                                    .width(30.dp)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                // Upper half of vertical connecting rope
                                if (idx > 0) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .fillMaxHeight(0.5f)
                                            .align(Alignment.TopCenter)
                                            .background(topRopeColor)
                                    )
                                }
                                // Lower half of vertical connecting rope
                                if (idx < fullSchedule.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .fillMaxHeight(0.5f)
                                            .align(Alignment.BottomCenter)
                                            .background(bottomRopeColor)
                                    )
                                }

                                // Node Circle
                                when {
                                    isChecked -> {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .background(Color(0xFF38BDF8), CircleShape)
                                                .border(1.5.dp, Color(0xFF7DD3FC), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "Prayed",
                                                tint = Color(0xFF061018),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    isProhibited -> {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .background(Color(0xFF231B0E), CircleShape)
                                                .border(1.2.dp, Color(0xFFD97706), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.WbSunny,
                                                contentDescription = "No prayer",
                                                tint = Color(0xFFFBBF24),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    isCurrentWaqt -> {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .background(Color(0xFF231B0E), CircleShape)
                                                .border(2.dp, Color(0xFFFBBF24), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(Color(0xFFFBBF24), CircleShape)
                                            )
                                        }
                                    }
                                    isOptional -> {
                                        androidx.compose.foundation.Canvas(
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            drawCircle(
                                                color = Color(0xFF141821),
                                                radius = size.minDimension / 2f
                                            )
                                            drawCircle(
                                                color = Color.White.copy(alpha = 0.35f),
                                                radius = (size.minDimension / 2f) - 1.dp.toPx(),
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                    width = 1.4.dp.toPx(),
                                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                                        floatArrayOf(6f, 6f),
                                                        0f
                                                    )
                                                )
                                            )
                                        }
                                    }
                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .background(Color(0xFF141821), CircleShape)
                                                .border(
                                                    1.4.dp,
                                                    Color.White.copy(alpha = 0.28f),
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Middle Name + Time + Description
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 9.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = name,
                                        color = if (isProhibited) Color(0xFFFBBF24) else titleColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    )
                                    Text(
                                        text = if (scheduleItem.endTime12.isNotEmpty() && scheduleItem.endTime12 != scheduleItem.startTime12) {
                                            " · ${scheduleItem.startTime12}–${scheduleItem.endTime12}"
                                        } else {
                                            " · ${scheduleItem.startTime12}"
                                        },
                                        color = if (isProhibited) Color(0xFFFBBF24).copy(alpha = 0.85f) else descColor,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    color = descColor.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Right Status Pill
                            Box(modifier = Modifier.padding(vertical = 9.dp)) {
                                when {
                                    isChecked -> {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF0F293A), RoundedCornerShape(50))
                                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f), RoundedCornerShape(50))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isBn) "আদায়কৃত" else "Prayed",
                                                color = Color(0xFF38BDF8),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    isProhibited -> {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF231B0E), RoundedCornerShape(50))
                                                .border(1.dp, Color(0xFFD97706).copy(alpha = 0.55f), RoundedCornerShape(50))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isBn) "নিষিদ্ধ" else "No prayer",
                                                color = Color(0xFFFBBF24),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    isCurrentWaqt -> {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF332610), RoundedCornerShape(50))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isBn) "এখন" else "Now",
                                                color = Color(0xFFFBBF24),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    isOptional -> {
                                        Text(
                                            text = if (isBn) "নফল" else "Optional",
                                            color = descColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Past 7 Days Capsule Card (matching Salah@1x.png)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerSize = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "বিগত ৭ দিন" else "Past 7 days",
                            color = titleColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBn) "৩৫ ওয়াক্তের মধ্যে ${past7Days.sum()}টি" else "${past7Days.sum()} of 35 prayers",
                            color = descColor,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        past7Days.forEachIndexed { i, count ->
                            val daysAgo = 6 - i
                            val dayLabel = if (daysAgo == 0) (if (isBn) "আজ" else "Today") else viewModel.getDayName(daysAgo)
                            val fillFraction = (count / 5f).coerceIn(0f, 1f)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "$count/5",
                                    color = descColor,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                // Rounded capsule track with bottom cyan fill
                                Box(
                                    modifier = Modifier
                                        .width(26.dp)
                                        .height(64.dp)
                                        .clip(RoundedCornerShape(13.dp))
                                        .background(Color(0xFF1A202C)),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    if (count > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height((fillFraction * 64f).coerceAtLeast(10f).dp)
                                                .clip(RoundedCornerShape(13.dp))
                                                .background(
                                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                                        colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                                                    )
                                                )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = dayLabel,
                                    color = descColor,
                                    fontSize = 10.5.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Adhan & Call to Prayer Card (matching Salah@1x.png)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerSize = 20.dp, glassAlpha = 0.95f),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = if (isBn) "আজান ও নামাজের আহ্বান" else "Adhan & Call to Prayer",
                        color = titleColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto-play Adhan row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "অটো-প্লে আজান" else "Auto-play Adhan",
                                color = titleColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isBn) "প্রতি ওয়াক্তের সময় আজান বাজবে" else "Plays at each prayer time",
                                color = descColor,
                                fontSize = 11.5.sp
                            )
                        }
                        Switch(
                            checked = isAdhanEnabled,
                            onCheckedChange = { enabled ->
                                AdhanPlayer.setAdhanEnabled(context, enabled)
                                PrayerScheduler.scheduleDailyPrayers(context)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF38BDF8),
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = Color(0xFF1E293B)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Muezzin selector
                    Text(
                        text = if (isBn) "মুয়াযযিন" else "Muezzin",
                        color = descColor,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isMadinah = selectedAdhanSource != "makkah"
                        val isMakkah = selectedAdhanSource == "makkah"

                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isMadinah) Color(0xFF38BDF8) else Color(0xFF1A202C))
                                .border(
                                    1.dp,
                                    if (isMadinah) Color.Transparent else Color.White.copy(alpha = 0.12f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    if (!isMadinah) {
                                        val wasPlaying = isAdhanPlaying
                                        AdhanPlayer.setSelectedAdhanSource(context, "madinah")
                                        if (wasPlaying) {
                                            AdhanPlayer.stopAdhan(context)
                                            AdhanPlayer.toggleManualPlayAdhan(context)
                                        }
                                    }
                                }
                                .padding(horizontal = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isBn) "মদিনা" else "Madinah",
                                color = if (isMadinah) Color(0xFF061018) else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isMakkah) Color(0xFF38BDF8) else Color(0xFF1A202C))
                                .border(
                                    1.dp,
                                    if (isMakkah) Color.Transparent else Color.White.copy(alpha = 0.12f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    if (!isMakkah) {
                                        val wasPlaying = isAdhanPlaying
                                        AdhanPlayer.setSelectedAdhanSource(context, "makkah")
                                        if (wasPlaying) {
                                            AdhanPlayer.stopAdhan(context)
                                            AdhanPlayer.toggleManualPlayAdhan(context)
                                        }
                                    }
                                }
                                .padding(horizontal = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isBn) "মক্কা" else "Makkah",
                                color = if (isMakkah) Color(0xFF061018) else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Full-width "Listen to Adhan" sky-cyan button
                    Button(
                        onClick = { AdhanPlayer.toggleManualPlayAdhan(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAdhanPlaying) Color(0xFFEF4444) else Color(0xFF38BDF8)
                        )
                    ) {
                        Icon(
                            imageVector = if (isAdhanPlaying) Icons.Filled.Stop else Icons.Outlined.PlayArrow,
                            contentDescription = null,
                            tint = if (isAdhanPlaying) Color.White else Color(0xFF061018),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAdhanPlaying) {
                                if (isBn) "আজান থামান" else "Stop Adhan"
                            } else {
                                if (isBn) "আজান শুনুন" else "Listen to Adhan"
                            },
                            color = if (isAdhanPlaying) Color.White else Color(0xFF061018),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Adjust minutes row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTrackerMosqueDialog = true },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "মিনিট সমন্বয়" else "Adjust minutes",
                                color = titleColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isBn) "স্থানীয় মসজিদের সাথে মেলান" else "Match your local masjid",
                                color = descColor,
                                fontSize = 11.5.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1A202C))
                                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        val newOff = (globalMinuteOffset - 1).coerceIn(-45, 45)
                                        globalMinuteOffset = newOff
                                        val prefs = context.getSharedPreferences("mosque_offset_prefs", Context.MODE_PRIVATE).edit()
                                        listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha").forEach { w ->
                                            prefs.putInt("offset_$w", newOff)
                                        }
                                        prefs.apply()
                                        PrayerScheduler.scheduleDailyPrayers(context)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("−", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            }

                            Text(
                                text = "${if (globalMinuteOffset > 0) "+$globalMinuteOffset" else "$globalMinuteOffset"} min",
                                color = titleColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { showTrackerMosqueDialog = true }
                            )

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1A202C))
                                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        val newOff = (globalMinuteOffset + 1).coerceIn(-45, 45)
                                        globalMinuteOffset = newOff
                                        val prefs = context.getSharedPreferences("mosque_offset_prefs", Context.MODE_PRIVATE).edit()
                                        listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha").forEach { w ->
                                            prefs.putInt("offset_$w", newOff)
                                        }
                                        prefs.apply()
                                        PrayerScheduler.scheduleDailyPrayers(context)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdhanEqualizerWaveform() {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "equalizer")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 14f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.EaseInOutQuad),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 5f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(400, easing = androidx.compose.animation.core.EaseInOutQuad),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 15f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(350, easing = androidx.compose.animation.core.EaseInOutQuad),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "h3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(16.dp)
    ) {
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h1.dp)
                .background(Color(0xFF10B981), CircleShape)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h2.dp)
                .background(Color(0xFF0284C7), CircleShape)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h3.dp)
                .background(Color(0xFF10B981), CircleShape)
        )
    }
}

@Composable
fun SalahItem(
    item: PrayerScheduleItem,
    isBn: Boolean,
    checked: Boolean,
    onClick: () -> Unit
) {
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descColor = if (isLightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val timeBadgeBg = if (isLightMode) Color(0xFFE0F2FE) else Color(0xFF0284C7).copy(alpha = 0.20f)
    val timeBadgeText = if (isLightMode) Color(0xFF0369A1) else Color(0xFF38BDF8)

    val name = if (isBn) item.nameBn else item.nameEn
    val desc = if (isBn) item.descBn else item.descEn

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable(enabled = item.isCheckable) { onClick() }
            .glassCard(cornerSize = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Icon / Emoji
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        if (checked) Color(0xFF10B981).copy(alpha = 0.2f)
                        else if (isLightMode) Color(0xFFF1F5F9)
                        else Color.White.copy(alpha = 0.08f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (checked) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Completed",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = item.iconEmoji,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = name,
                        color = titleColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        style = TextStyle(shadow = floatingTextShadow(elevation = 2.dp))
                    )
                    if (item.key == "Tahajjud") {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (isBn) "নফল" else "Nafl",
                                color = Color(0xFFA78BFA),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // 12-Hour Start and End Time Display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(timeBadgeBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isBn) "শুরু: ${item.startTime12}  •  শেষ: ${item.endTime12}"
                        else "Start: ${item.startTime12}  •  End: ${item.endTime12}",
                        color = timeBadgeText,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        style = TextStyle(shadow = floatingTextShadow(elevation = 1.2.dp))
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = desc,
                    color = descColor,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    style = TextStyle(shadow = floatingTextShadow(elevation = 1.2.dp))
                )
            }

            // Right Checkbox if checkable
            if (item.isCheckable) {
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            if (checked) Color(0xFF10B981).copy(alpha = 0.25f) else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            2.dp,
                            if (checked) Color(0xFF10B981) else (if (isLightMode) Color(0xFF94A3B8) else Color.White.copy(alpha = 0.4f)),
                            RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (checked) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SalahItem(title: String, desc: String, timeRange: String, checked: Boolean, onClick: () -> Unit) {
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descColor = if (isLightMode) Color(0xFF475569) else Color.LightGray
    val timeColor = if (isLightMode) Color(0xFF0284C7) else CyanAccent

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onClick() }.glassCard(cornerSize = 14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(24.dp).background(if (isLightMode) Color(0xFFE2E8F0) else DeepBlue, CircleShape)) {
                if (checked) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = GreenText, modifier = Modifier.size(16.dp).align(Alignment.Center))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = titleColor, fontWeight = FontWeight.Bold)
                if (timeRange.isNotEmpty()) {
                    Text(timeRange, color = timeColor, fontSize = 12.sp)
                }
                Text(desc, color = descColor, fontSize = 12.sp)
            }
            Box(modifier = Modifier.size(24.dp).background(if(checked) GreenText.copy(alpha=0.3f) else Color.Transparent, RoundedCornerShape(4.dp)).border(2.dp, if(checked) GreenText else Color.Gray, RoundedCornerShape(4.dp))) {
                if (checked) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = GreenText, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun ProBadge() {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "pro_anim")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    Row(
        modifier = Modifier
            .background(Color(0xFF221D02), RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.8f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .scale(glowScale),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("PRO", color = Color(0xFFFFD700), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(3.dp))
        Text("👑", fontSize = 11.sp)
    }
}

@Composable
fun ProBlueBadge(size: androidx.compose.ui.unit.Dp = 14.dp) {
    Icon(
        imageVector = Icons.Filled.Verified,
        contentDescription = "Verified Pro Member",
        tint = Color(0xFF1DA1F2), // Gorgeous Twitter/Verified Blue
        modifier = Modifier.size(size)
    )
}

@Composable
fun PremiumNameText(
    name: String,
    isPremium: Boolean,
    defaultColor: Color,
    fontSize: TextUnit = 11.sp,
    fontWeight: FontWeight = FontWeight.Normal
) {
    if (isPremium) {
        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "premium_name_anim")
        
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.98f,
            targetValue = 1.04f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(1200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            ),
            label = "scale"
        )
        
        val goldColor by infiniteTransition.animateColor(
            initialValue = Color(0xFFFFD700), // Vibrant Gold
            targetValue = Color(0xFFFFF6CA), // Radiant Light Gold
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.LinearEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            ),
            label = "color"
        )
        
        Text(
            text = name,
            color = goldColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            modifier = Modifier.scale(scale),
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color(0xBBFF9E0D),
                    offset = androidx.compose.ui.geometry.Offset(1.5f, 1.5f),
                    blurRadius = 4f
                )
            )
        )
    } else {
        Text(
            text = name,
            color = defaultColor,
            fontSize = fontSize,
            fontWeight = fontWeight
        )
    }
}

@Composable
fun LeaderboardAvatar(
    photoUrl: String,
    badgeImageUrl: String,
    streakDays: Int,
    dynamicBadges: List<CustomBadge>,
    rank: Int,
    modifier: Modifier = Modifier
) {
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val borderColor = when (rank) {
        0 -> Color(0xFFFFD700) // Gold
        1 -> Color(0xFFC0C0C0) // Silver
        2 -> Color(0xFFCD7F32) // Bronze
        else -> if (isLightMode) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.2f)
    }
    val borderWidth = if (rank < 3) 2.dp else 1.dp
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .border(borderWidth, borderColor, shape)
            .clip(shape)
            .background(if (isLightMode) Color(0xFFF1F5F9) else Color(0xFF1E293B)),
        contentAlignment = Alignment.Center
    ) {
        val cleanPhoto = if (photoUrl.isNotEmpty() && !photoUrl.startsWith("res://badge_")) photoUrl else ""
        if (cleanPhoto.isNotEmpty()) {
            if (cleanPhoto.startsWith("data:image")) {
                var bitmap by remember(cleanPhoto) { mutableStateOf(FastBitmapCache.get(cleanPhoto)) }
                LaunchedEffect(cleanPhoto) {
                    if (bitmap == null) {
                        bitmap = withContext(Dispatchers.Default) {
                            FastBitmapCache.decodeAndCache(cleanPhoto)
                        }
                    }
                }
                if (bitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    LeaderboardFallbackBadge(badgeImageUrl, streakDays, dynamicBadges)
                }
            } else {
                coil.compose.AsyncImage(
                    model = cleanPhoto,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
        } else {
            // Auto fallback to earned badge as profile picture!
            LeaderboardFallbackBadge(badgeImageUrl, streakDays, dynamicBadges)
        }
    }
}

@Composable
private fun LeaderboardFallbackBadge(
    badgeImageUrl: String,
    streakDays: Int,
    dynamicBadges: List<CustomBadge>
) {
    val fallbackBadge = dynamicBadges.lastOrNull { streakDays >= it.requiredDays } ?: dynamicBadges.firstOrNull()
    val finalBadgeUrl = if (badgeImageUrl.isNotEmpty()) badgeImageUrl else (fallbackBadge?.imageUrl ?: "")
    BadgeImage(
        imageUrl = finalBadgeUrl,
        modifier = Modifier.fillMaxSize().padding(4.dp),
        fallbackName = fallbackBadge?.name ?: "Badge"
    )
}

@OptIn(androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun CommunityScreen(
    communityViewModel: CommunityViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    purityTrackerViewModel: PurityTrackerViewModel = viewModel(),
    badgesViewModel: BadgesViewModel = viewModel(),
    socialViewModel: SocialViewModel = viewModel(),
    userProfileViewModel: UserProfileViewModel = viewModel(),
    onTabSelected: (String) -> Unit = {},
    onSubViewActiveChanged: (Boolean) -> Unit = {}
) {
    val unreadCounts by socialViewModel.unreadCounts.collectAsState()
    val pendingFriendRequests by socialViewModel.friendRequests.collectAsState()
    val totalFriendsBadge = unreadCounts.values.sum() + pendingFriendRequests.size
    val context = LocalContext.current
    val posts by communityViewModel.posts.collectAsState()
    val leaders by communityViewModel.leaders.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isPremiumUserState by PremiumManager.isPremium.collectAsState()
    val myUserProfile by userProfileViewModel.userProfile.collectAsState()
    val uploadProgress by communityViewModel.uploadProgress.collectAsState()
    var postText by remember { mutableStateOf("") }
    
    var selectedUserIdForProfile by remember { mutableStateOf<String?>(null) }
    var selectedUserProfile by remember { mutableStateOf<UserProfile?>(null) }
    
    val days by purityTrackerViewModel.durationDays.collectAsState()
    val dynamicBadges by badgesViewModel.badges.collectAsState()
    val currentBadge = dynamicBadges.lastOrNull { days >= it.requiredDays } ?: dynamicBadges.firstOrNull() ?: CustomBadge("", "None", 0, "None")
    val activeBadgeName = currentBadge.name
    
    var activeTab by remember { mutableStateOf(CommunitySubTabState.requestedSubTab.value ?: "board") }
    var activeChatChallenge by remember { mutableStateOf<Challenge?>(null) }
    var activeCommunityChat by remember { mutableStateOf<UserProfile?>(null) }

    val requestedExternalSubTab = CommunitySubTabState.requestedSubTab.value
    LaunchedEffect(requestedExternalSubTab) {
        if (requestedExternalSubTab != null) {
            activeTab = requestedExternalSubTab
            CommunitySubTabState.requestedSubTab.value = null
        }
    }

    LaunchedEffect(Unit) {
        val activity = context as? android.app.Activity
        val dChatId = activity?.intent?.getStringExtra("open_direct_chat")
        if (dChatId != null) {
            activity.intent.removeExtra("open_direct_chat")
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(dChatId).get().addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        activeCommunityChat = doc.toObject(UserProfile::class.java)?.copy(userId = doc.id)
                        activeTab = "friends"
                    }
                }
        }
    }

    if (activeCommunityChat != null) {
        PrivateChatScreen(
            friendId = activeCommunityChat!!.userId,
            socialViewModel = socialViewModel,
            onBack = { activeCommunityChat = null },
            onViewProfile = { profile ->
                selectedUserIdForProfile = profile.userId
                selectedUserProfile = profile
                activeCommunityChat = null
            }
        )
    } else if (activeChatChallenge != null) {
        ChallengeChatRoom(
            challenge = activeChatChallenge!!,
            communityViewModel = communityViewModel,
            currentUserEmail = currentUser?.email ?: "",
            onBack = { activeChatChallenge = null }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val isBnLang = LanguageStore.currentLanguage.value == "bn"
            Text(
                text = if (isBnLang) "কমিউনিটি ও ফ্রেন্ডস" else "Community",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Segmented Pill Tab Bar with Friends integrated inside Community
            GlassCapsuleTabBar(
                modifier = Modifier.fillMaxWidth()
            ) {
                GlassCapsuleTabItem(
                    selected = activeTab == "board",
                    onClick = { activeTab = "board" },
                    label = if (isBnLang) "ফিড" else "Posts"
                )
                GlassCapsuleTabItem(
                    selected = activeTab == "friends",
                    onClick = { activeTab = "friends" },
                    label = if (isBnLang) "ফ্রেন্ডস" else "Friends",
                    badgeCount = totalFriendsBadge
                )
                GlassCapsuleTabItem(
                    selected = activeTab == "challenges",
                    onClick = { activeTab = "challenges" },
                    label = if (isBnLang) "গ্রুপ" else "Groups"
                )
                GlassCapsuleTabItem(
                    selected = activeTab == "feed",
                    onClick = { activeTab = "feed" },
                    label = if (isBnLang) "র‍্যাংক" else "Ranks"
                )
                GlassCapsuleTabItem(
                    selected = activeTab == "focus",
                    onClick = { activeTab = "focus" },
                    label = if (isBnLang) "ধ্যান" else "Focus"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (activeTab == "friends") {
                Box(modifier = Modifier.fillMaxSize()) {
                    FriendsTabContent(
                        onTabSelected = onTabSelected,
                        onSubViewActiveChanged = onSubViewActiveChanged
                    )
                }
            } else if (activeTab == "feed") {
                val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
                val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
                val descColor = if (isLightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)

                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(t("leaderboard_title") ?: "Global Leaderboard", color = titleColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    if (leaders.isEmpty()) {
                        Text("No warriors found on leaderboard yet...", color = descColor)
                    } else {
                        leaders.forEachIndexed { index, leader ->
                            val days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(leader.streakMs).toInt()
                            val isLeaderPremium = leader.isPremium || (leader.id == currentUser?.uid && isPremiumUserState)
                            val effectivePhoto = if (leader.id == currentUser?.uid && !myUserProfile?.profilePicUrl.isNullOrEmpty()) {
                                myUserProfile!!.profilePicUrl
                            } else {
                                leader.profilePicUrl
                            }
                            val fallbackBadge = dynamicBadges.lastOrNull { days >= it.requiredDays } ?: dynamicBadges.firstOrNull()
                            val badgeTitle = if (leader.badge.isNotEmpty()) leader.badge else (fallbackBadge?.name ?: "Warrior")

                            val rankGlowBorder = when (index) {
                                0 -> Color(0xFFFFD700)
                                1 -> Color(0xFFC0C0C0)
                                2 -> Color(0xFFCD7F32)
                                else -> Color.White
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .innerFloatingGlassCard(
                                        cornerSize = 16.dp,
                                        elevation = if (index < 3) 3.dp else 1.5.dp,
                                        borderGlow = rankGlowBorder
                                    )
                                    .clickable {
                                        if (leader.id.isNotEmpty()) {
                                            selectedUserIdForProfile = leader.id
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Rank Number or Medal
                                    Box(
                                        modifier = Modifier.width(30.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when (index) {
                                            0 -> Text("🥇", fontSize = 20.sp)
                                            1 -> Text("🥈", fontSize = 20.sp)
                                            2 -> Text("🥉", fontSize = 20.sp)
                                            else -> Text(
                                                "${index + 1}",
                                                color = titleColor,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Real Profile Picture with Auto Earned Badge Fallback
                                    LeaderboardAvatar(
                                        photoUrl = effectivePhoto,
                                        badgeImageUrl = leader.badgeImageUrl,
                                        streakDays = days,
                                        dynamicBadges = dynamicBadges,
                                        rank = index,
                                        modifier = Modifier.size(42.dp)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Warrior Name & Earned Badge Title
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.weight(1f, fill = false)) {
                                                PremiumNameText(
                                                    name = leader.name.ifEmpty { "Warrior" },
                                                    isPremium = isLeaderPremium,
                                                    defaultColor = titleColor,
                                                    fontSize = 14.5.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            if (isLeaderPremium) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                ProBlueBadge(size = 14.dp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = badgeTitle,
                                            color = descColor,
                                            fontSize = 11.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Floating Streak Days Pill
                                    Box(
                                        modifier = Modifier
                                            .innerFloatingGlassCard(
                                                cornerSize = 10.dp,
                                                elevation = 1.dp,
                                                borderGlow = if (index == 0) Color(0xFFFFD700) else if (index < 3) Color(0xFFFBBF24) else Color.White
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${days}d 🔥",
                                            color = if (index == 0) (if (isLightMode) Color(0xFFB45309) else Color(0xFFFFD700))
                                            else if (index < 3) (if (isLightMode) Color(0xFFD97706) else Color(0xFFFBBF24))
                                            else titleColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    // Admin 1-tap Ghost Account Purge directly from Leaderboard
                                    val isCurrentAdmin = AccountDeletionManager.isAdminEmail(currentUser?.email) ||
                                        AccountDeletionManager.isAdminEmail(myUserProfile?.email)
                                    if (isCurrentAdmin && leader.id != currentUser?.uid && !AccountDeletionManager.isAdminEmail(leader.email)) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = {
                                                communityViewModel.deleteUserCompletely(
                                                    userId = leader.id,
                                                    username = leader.username,
                                                    email = leader.email
                                                ) { _: Boolean, _: String? ->
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        if (LanguageStore.currentLanguage.value == "bn") "${leader.name} লিডারবোর্ড ও সার্চ থেকে মুছে ফেলা হয়েছে" else "Removed ${leader.name} from Leaderboard & Search",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.DeleteForever,
                                                contentDescription = "Delete User from Leaderboard",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (activeTab == "board") {
                val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
                val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
                val descColor = if (isLightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)

                var isRefreshing by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = isRefreshing,
                    onRefresh = {
                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            isRefreshing = true
                            communityViewModel.refreshPosts()
                            kotlinx.coroutines.delay(1000)
                            isRefreshing = false
                        }
                    }
                )

                Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassCard(cornerSize = 20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                OutlinedTextField(
                                    value = postText,
                                    onValueChange = { postText = it },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
                                    placeholder = {
                                        Text(
                                            "Write something inspiring to fuel other warriors...",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 14.sp
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedTextColor = titleColor,
                                        focusedTextColor = titleColor
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            if (postText.isNotBlank()) {
                                                val streakText = if (LanguageStore.currentLanguage.value == "bn") "${days} দিনের স্ট্রিক" else "${days} days streak"
                                                communityViewModel.addPost(postText, streakText, activeBadgeName)
                                                postText = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                                        shape = RoundedCornerShape(14.dp),
                                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                                    ) {
                                        Text("Publish status", color = Color(0xFF061018), fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                    }
                                }

                                if (uploadProgress != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Uploading: ${uploadProgress!!.toInt()}%", color = CyanAccent, fontSize = 12.sp)
                                    androidx.compose.material3.LinearProgressIndicator(progress = uploadProgress!! / 100f, modifier = Modifier.fillMaxWidth(), color = CyanAccent, trackColor = Color.DarkGray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        Text("Latest motivational board", color = titleColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        if (posts.isEmpty()) {
                            Text("Loading posts or no posts yet...", color = descColor)
                        } else {
                            posts.forEach { post ->
                                CommunityPost(
                                    post = post,
                                    currentUserId = currentUser?.uid ?: "",
                                    communityViewModel = communityViewModel,
                                    onUserClicked = { clickedId ->
                                        selectedUserIdForProfile = clickedId
                                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                        db.collection("users").document(clickedId).get().addOnSuccessListener { doc ->
                                            if (doc.exists()) {
                                                selectedUserProfile = doc.toObject(UserProfile::class.java)
                                                if (selectedUserProfile != null && selectedUserProfile!!.userId.isEmpty()) {
                                                    selectedUserProfile = selectedUserProfile!!.copy(userId = doc.id)
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                    
                    if (selectedUserIdForProfile != null) {
                        if (selectedUserProfile == null) {
                            Box(modifier = Modifier.fillMaxSize().background(Color(0xE60F172A)).clickable { }, contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = CyanAccent)
                            }
                        } else {
                            UserDetailScreen(
                                userProfile = selectedUserProfile!!,
                                currentUserId = currentUser?.uid ?: "",
                                socialViewModel = socialViewModel,
                                communityViewModel = communityViewModel,
                                onBack = { 
                                    selectedUserIdForProfile = null
                                    selectedUserProfile = null
                                },
                                onOpenChat = { friendProfile ->
                                    selectedUserIdForProfile = null
                                    selectedUserProfile = null
                                    activeCommunityChat = friendProfile
                                }
                            )
                        }
                    }
                    
                    PullRefreshIndicator(
                        refreshing = isRefreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                        contentColor = CyanAccent
                    )
                }
            } else if (activeTab == "challenges") {
                ChallengesTabContent(
                    communityViewModel = communityViewModel,
                    currentUserEmail = currentUser?.email ?: "",
                    onOpenChat = { challenge ->
                        activeChatChallenge = challenge
                    }
                )
            } else if (activeTab == "focus") {
                FocusScreen()
            }
        }
    }
}

@Composable
fun PrayerCountdownTimer(timings: Map<String, String>) {
    if (timings.isEmpty()) return
    
    var timeRemaining by remember { mutableStateOf("") }
    var nextPrayerName by remember { mutableStateOf("") }
    var currentPrayerName by remember { mutableStateOf("") }
    var forbiddenTimeKey by remember { mutableStateOf("") }
    
    LaunchedEffect(timings) {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.US)
        while (true) {
            val now = java.util.Calendar.getInstance()
            val nowStr = String.format(java.util.Locale.US, "%02d:%02d", now.get(java.util.Calendar.HOUR_OF_DAY), now.get(java.util.Calendar.MINUTE))
            
            fun getMins(timeStr: String?): Int {
                if (timeStr == null) return -1
                val p = timeStr.replace(Regex("\\s\\(.*\\)"), "").split(":")
                return if (p.size == 2) (p[0].toIntOrNull() ?: 0) * 60 + (p[1].toIntOrNull() ?: 0) else -1
            }
            val nowMins = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)
            val sunrMins = getMins(timings["Sunrise"])
            val dhuhMins = getMins(timings["Dhuhr"])
            val sunsMins = getMins(timings["Sunset"])
            
            forbiddenTimeKey = ""
            if (sunrMins != -1 && nowMins in sunrMins..(sunrMins + 15)) {
                forbiddenTimeKey = "forbidden_sunrise"
            } else if (dhuhMins != -1 && nowMins in (dhuhMins - 10)..dhuhMins) {
                forbiddenTimeKey = "forbidden_zawaal"
            } else if (sunsMins != -1 && nowMins in (sunsMins - 15)..sunsMins) {
                forbiddenTimeKey = "forbidden_sunset"
            }

            val order = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")
            var foundNext = false
            for (i in order.indices) {
                val p = order[i]
                val t = timings[p]?.replace(Regex("\\s\\(.*\\)"), "")
                if (t != null && nowStr < t) {
                    val pCal = java.util.Calendar.getInstance()
                    val d = sdf.parse(t)!!
                    pCal.time = d
                    pCal.set(java.util.Calendar.YEAR, now.get(java.util.Calendar.YEAR))
                    pCal.set(java.util.Calendar.MONTH, now.get(java.util.Calendar.MONTH))
                    pCal.set(java.util.Calendar.DAY_OF_YEAR, now.get(java.util.Calendar.DAY_OF_YEAR))
                    
                    val diff = pCal.timeInMillis - now.timeInMillis
                    val hours = diff / (1000 * 60 * 60)
                    val mins = (diff / (1000 * 60)) % 60
                    val secs = (diff / 1000) % 60
                    timeRemaining = String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, mins, secs)
                    nextPrayerName = p
                    currentPrayerName = if (i > 0) order[i - 1] else "Isha"
                    foundNext = true
                    break
                }
            }
            if (!foundNext) {
                val t = timings["Fajr"]?.replace(Regex("\\s\\(.*\\)"), "")
                if (t != null) {
                    val pCal = java.util.Calendar.getInstance()
                    pCal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                    val d = sdf.parse(t)!!
                    val c2 = java.util.Calendar.getInstance(); c2.time = d
                    pCal.set(java.util.Calendar.HOUR_OF_DAY, c2.get(java.util.Calendar.HOUR_OF_DAY))
                    pCal.set(java.util.Calendar.MINUTE, c2.get(java.util.Calendar.MINUTE))
                    pCal.set(java.util.Calendar.SECOND, 0)
                    
                    val diff = pCal.timeInMillis - now.timeInMillis
                    val hours = diff / (1000 * 60 * 60)
                    val mins = (diff / (1000 * 60)) % 60
                    val secs = (diff / 1000) % 60
                    timeRemaining = String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, mins, secs)
                    nextPrayerName = "Fajr"
                    currentPrayerName = "Isha"
                }
            }
            kotlinx.coroutines.delay(1000)
        }
    }
    
    if (timeRemaining.isNotEmpty()) {
        val translatedCurrent = if (forbiddenTimeKey.isNotEmpty()) {
            t(forbiddenTimeKey) ?: "Forbidden Time"
        } else {
            t(if (currentPrayerName == "Sunrise") "sunrise" else currentPrayerName.lowercase())
        }
        val translatedNext = t(if (nextPrayerName == "Sunrise") "sunrise" else nextPrayerName.lowercase())
        
        val currStartTime = timings[if (currentPrayerName == "Sunrise") "Fajr" else currentPrayerName]
        val currEndTime = timings[nextPrayerName]
        val timeRange = formatTimeRange(currStartTime, currEndTime)

        val context = androidx.compose.ui.platform.LocalContext.current
        var locationName by remember { mutableStateOf("") }
        
        LaunchedEffect(Unit) {
            val lat = PrayerScheduler.getLat(context)
            val lng = PrayerScheduler.getLng(context)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val address = addresses[0]
                        val name = address.locality ?: address.subAdminArea ?: address.adminArea ?: ""
                        if (name.isNotEmpty()) {
                            locationName = name
                        }
                    }
                } catch (e: Exception) {
                    // ignore
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().height(380.dp).padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFD34F27),
                                Color(0xFFC04035),
                                Color(0xFF5D1E4A),
                                Color(0xFF2C0B30)
                            )
                        )
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val sunCenter = androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.45f)
                    val sunRadius = w * 0.12f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFB74D), Color(0xFFFF9800), Color(0xFFE65100).copy(alpha = 0f)),
                            center = sunCenter,
                            radius = sunRadius * 1.5f
                        ),
                        radius = sunRadius * 1.5f,
                        center = sunCenter
                    )
                    drawCircle(
                        color = Color(0xFFFF9800),
                        radius = sunRadius,
                        center = sunCenter
                    )

                    fun drawBird(x: Float, y: Float, scale: Float = 1f) {
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(x, y)
                            quadraticBezierTo(x + 5f * scale, y - 8f * scale, x + 15f * scale, y - 2f * scale)
                            quadraticBezierTo(x + 5f * scale, y - 4f * scale, x, y + 2f * scale)
                            quadraticBezierTo(x - 5f * scale, y - 4f * scale, x - 15f * scale, y - 2f * scale)
                            quadraticBezierTo(x - 5f * scale, y - 8f * scale, x, y)
                            close()
                        }
                        drawPath(path, color = Color(0xFF3E1E04))
                    }
                    drawBird(w * 0.65f, h * 0.35f, 1f)
                    drawBird(w * 0.78f, h * 0.32f, 0.7f)
                    drawBird(w * 0.70f, h * 0.42f, 1.2f)

                    val dune1 = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, h * 0.8f)
                        quadraticBezierTo(w * 0.4f, h * 0.65f, w, h * 0.85f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(dune1, brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF9E3623), Color(0xFF4A1525)),
                        startY = h * 0.7f, endY = h
                    ))

                    val dune2 = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, h * 0.9f)
                        quadraticBezierTo(w * 0.3f, h * 0.8f, w * 0.7f, h * 0.95f)
                        quadraticBezierTo(w * 0.9f, h * 1.0f, w, h * 0.9f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(dune2, brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF802820), Color(0xFF2C0B30)),
                        startY = h * 0.85f, endY = h
                    ))
                    
                    fun drawPalm(ox: Float, oy: Float, scale: Float) {
                        val trunk = androidx.compose.ui.graphics.Path().apply {
                            moveTo(ox, oy)
                            quadraticBezierTo(ox - 10f * scale, oy - 40f * scale, ox - 5f * scale, oy - 100f * scale)
                            lineTo(ox + 5f * scale, oy - 100f * scale)
                            quadraticBezierTo(ox + 5f * scale, oy - 40f * scale, ox + 15f * scale, oy)
                            close()
                        }
                        drawPath(trunk, color = Color(0xFF2C1311))
                        
                        val center = androidx.compose.ui.geometry.Offset(ox, oy - 100f * scale)
                        val leafColor = Color(0xFF221111)
                        for (i in 0..7) {
                            val angle = (i * 45f) * (kotlin.math.PI / 180f)
                            val dx = kotlin.math.cos(angle).toFloat() * 60f * scale
                            val dy = kotlin.math.sin(angle).toFloat() * 40f * scale
                            val leaf = androidx.compose.ui.graphics.Path().apply {
                                moveTo(center.x, center.y)
                                quadraticBezierTo(center.x + dx * 0.5f, center.y + dy * 0.1f - 20f*scale, center.x + dx, center.y + dy)
                                quadraticBezierTo(center.x + dx * 0.3f, center.y + dy * 0.8f + 10f*scale, center.x, center.y + 10f*scale)
                                close()
                            }
                            drawPath(leaf, color = leafColor)
                        }
                    }
                    
                    drawPalm(w * 0.25f, h * 0.9f, 1f)
                    drawPalm(w * 0.40f, h * 0.92f, 0.7f)
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(t("current_time_prefix") ?: "Current Time", color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            val isForbidden = forbiddenTimeKey.isNotEmpty()
                            Text(translatedCurrent, color = if(isForbidden) LightRed else Color.White, fontSize = if (isForbidden) 28.sp else 44.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            if (!isForbidden) {
                                Text(timeRange.replace("AM", "AM").replace("PM", "PM").localizeDigits(), color = Color.White.copy(alpha = 0.95f), fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            } else {
                                Text(timeRange.replace("AM", "AM").replace("PM", "PM").localizeDigits(), color = Color.Transparent, fontSize = 18.sp)
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(t("rakah_btn") ?: "Rakah", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${t("next_waqt_prefix") ?: "Next Waqt:"} $translatedNext", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(timeRemaining.localizeDigits(), color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(t("all_prayers") ?: "All Prayers", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val locText = if (locationName.isNotEmpty()) locationName else (t("location_label") ?: "Location")
                            Text(locText, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SalahTracker(viewModel: SalahViewModel = viewModel()) {
    val todaySalahs by viewModel.todaySalahs.collectAsState()
    val past7DaysCounts by viewModel.past7DaysCounts.collectAsState()
    val todayTimings by viewModel.todayTimings.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasLocationPermission = isGranted
        if (isGranted) {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
                var updated = false
                if (lm != null) {
                    val providers = lm.getProviders(true)
                    for (provider in providers) {
                        val loc = try { lm.getLastKnownLocation(provider) } catch (t: Throwable) { null }
                        if (loc != null) {
                            PrayerScheduler.updateLocation(context, loc.latitude, loc.longitude)
                            updated = true
                            break
                        }
                    }
                }
                if (!updated) {
                    val availability = com.google.android.gms.common.GoogleApiAvailability.getInstance()
                    if (availability.isGooglePlayServicesAvailable(context) == com.google.android.gms.common.ConnectionResult.SUCCESS) {
                        val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                PrayerScheduler.updateLocation(context, location.latitude, location.longitude)
                            }
                            PrayerScheduler.scheduleDailyPrayers(context)
                        }.addOnFailureListener {
                            PrayerScheduler.scheduleDailyPrayers(context)
                        }
                        return@rememberLauncherForActivityResult
                    }
                }
                PrayerScheduler.scheduleDailyPrayers(context)
            } catch (e: Exception) {
                PrayerScheduler.scheduleDailyPrayers(context)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        if (!hasLocationPermission) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .glassCard(cornerSize = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(t("location_permission_desc") ?: "To construct the exact prayer timings for your region, please enable the location permission here, or you will be able to manually specify in future updates.", color = Color.LightGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { launcher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                    ) {
                        Text(t("activate_location") ?: "Enable Location", color = Color.White)
                    }
                }
            }
        }
    
        Text(t("past_7_days_records"), color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier =Modifier.height(16.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerSize = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                // days are 6 downTo 0
                for (i in 6 downTo 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(viewModel.getDayName(i), color = Color.LightGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        val isAll = past7DaysCounts[6 - i] == 5
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp).background(if(isAll) GreenText.copy(alpha=0.2f) else DeepBlue, CircleShape).border(1.dp, if(isAll) GreenText else Color.Gray, CircleShape)) {
                            Text(past7DaysCounts[6 - i].toString(), color = if(isAll) GreenText else Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Adhan Sound Preferences & Live Preview Card
        val isBn = LanguageStore.currentLanguage.value == "bn"
        val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
        var isAdhanSoundOn by remember { mutableStateOf(AdhanPlayer.isAdhanEnabled(context)) }
        val isAdhanPreviewPlaying by AdhanPlayer.isPlayingFlow.collectAsState()
        val selectedAdhanSource by AdhanPlayer.selectedAdhanFlow.collectAsState()

        DisposableEffect(Unit) {
            onDispose {
                if (AdhanPlayer.isPlaying) {
                    AdhanPlayer.stopAdhan()
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerSize = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            tint = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBn) "৫ ওয়াক্ত নামাজের আযান" else "5 Daily Prayers Adhan",
                                color = if (isLightMode) Color(0xFF0F172A) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isBn) "সূর্যোদয়/সূর্যাস্তে আযান বাজবে না" else "No Adhan for Sunrise/Sunset",
                                color = if (isLightMode) Color(0xFF64748B) else Color(0xFF94A3B8),
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Switch(
                        checked = isAdhanSoundOn,
                        onCheckedChange = { checked ->
                            isAdhanSoundOn = checked
                            AdhanPlayer.setAdhanEnabled(context, checked)
                            if (!checked && isAdhanPreviewPlaying) {
                                AdhanPlayer.stopAdhan()
                            }
                            val msg = if (checked) {
                                if (isBn) "নামাজের আযান সাউন্ড চালু হয়েছে 🕌" else "Adhan sound enabled 🕌"
                            } else {
                                if (isBn) "আযান সাউন্ড বন্ধ করা হয়েছে 🔕" else "Adhan sound disabled 🔕"
                            }
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = if (isLightMode) Color(0xFF0284C7) else CyanAccent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Adhan Voice Selector in Settings Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isMadinah = selectedAdhanSource != "makkah"
                    Button(
                        onClick = {
                            if (!isMadinah) {
                                val wasPlaying = isAdhanPreviewPlaying
                                AdhanPlayer.setSelectedAdhanSource(context, "madinah")
                                if (wasPlaying) {
                                    AdhanPlayer.stopAdhan(context)
                                    AdhanPlayer.toggleManualPlayAdhan(context)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMadinah) Color(0xFF0284C7) else (if (isLightMode) Color.White else Color.White.copy(alpha = 0.08f)),
                            contentColor = if (isMadinah) Color.White else (if (isLightMode) Color(0xFF334155) else Color.LightGray)
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        border = if (!isMadinah) BorderStroke(1.dp, if (isLightMode) Color(0xFFCBD5E1) else Color.White.copy(alpha = 0.12f)) else null
                    ) {
                        Text(
                            text = if (isBn) "মদিনা মুনাওয়ারা 🕌" else "Madinah 🕌",
                            fontSize = 11.5.sp,
                            fontWeight = if (isMadinah) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    val isMakkah = selectedAdhanSource == "makkah"
                    Button(
                        onClick = {
                            if (!isMakkah) {
                                val wasPlaying = isAdhanPreviewPlaying
                                AdhanPlayer.setSelectedAdhanSource(context, "makkah")
                                if (wasPlaying) {
                                    AdhanPlayer.stopAdhan(context)
                                    AdhanPlayer.toggleManualPlayAdhan(context)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMakkah) Color(0xFF0284C7) else (if (isLightMode) Color.White else Color.White.copy(alpha = 0.08f)),
                            contentColor = if (isMakkah) Color.White else (if (isLightMode) Color(0xFF334155) else Color.LightGray)
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        border = if (!isMakkah) BorderStroke(1.dp, if (isLightMode) Color(0xFFCBD5E1) else Color.White.copy(alpha = 0.12f)) else null
                    ) {
                        Text(
                            text = if (isBn) "কাবা ঘর (মক্কা) 🕋" else "Kaaba (Makkah) 🕋",
                            fontSize = 11.5.sp,
                            fontWeight = if (isMakkah) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Preview Adhan Button
                Button(
                    onClick = {
                        AdhanPlayer.toggleManualPlayAdhan(context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAdhanPreviewPlaying) RedAlert else (if (isLightMode) Color(0xFF0284C7) else CyanAccent)
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (isAdhanPreviewPlaying) Icons.Filled.Stop else Icons.Filled.VolumeUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val voiceLabel = if (selectedAdhanSource == "makkah") (if (isBn) "কাবা ঘর" else "Kaaba") else (if (isBn) "মদিনা" else "Madinah")
                    Text(
                        text = if (isAdhanPreviewPlaying) {
                            if (isBn) "আযান থামান ($voiceLabel)" else "Stop Adhan ($voiceLabel)"
                        } else {
                            if (isBn) "মধুর সুরে আযান শুনুন ($voiceLabel)" else "Listen to Melodious Adhan ($voiceLabel)"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(t("today_checklist"), color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        PrayerCountdownTimer(todayTimings)

        val fajrRange = formatTimeRange(todayTimings["Fajr"], todayTimings["Sunrise"])
        val dhuhrRange = formatTimeRange(todayTimings["Dhuhr"], todayTimings["Asr"])
        val asrRange = formatTimeRange(todayTimings["Asr"], todayTimings["Maghrib"])
        val maghribRange = formatTimeRange(todayTimings["Maghrib"], todayTimings["Isha"])
        val ishaRange = formatTimeRange(todayTimings["Isha"], todayTimings["Fajr"])

        SalahItem(t("fajr"), t("fajr_desc"), fajrRange, todaySalahs[0]) { viewModel.toggleSalah(0) }
        SalahItem(t("dhuhr"), t("dhuhr_desc"), dhuhrRange, todaySalahs[1]) { viewModel.toggleSalah(1) }
        SalahItem(t("asr"), t("asr_desc"), asrRange, todaySalahs[2]) { viewModel.toggleSalah(2) }
        SalahItem(t("maghrib"), t("maghrib_desc"), maghribRange, todaySalahs[3]) { viewModel.toggleSalah(3) }
        SalahItem(t("isha"), t("isha_desc"), ishaRange, todaySalahs[4]) { viewModel.toggleSalah(4) }
    }
}

class VoicePlayerManager(private val context: android.content.Context) {
    private var mediaPlayer: android.media.MediaPlayer? = null
    val currentlyPlayingMessageId = mutableStateOf<String?>(null)
    val currentPlaybackPosition = mutableStateOf(0f)
    val isPlaying = mutableStateOf(false)
    private var progressJob: kotlinx.coroutines.Job? = null
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)

    fun play(msgId: String, base64Audio: String, onFinished: () -> Unit) {
        if (currentlyPlayingMessageId.value == msgId) {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                isPlaying.value = false
                return
            } else {
                mediaPlayer?.start()
                isPlaying.value = true
                startProgressTracker()
                return
            }
        }
        
        stop()

        val filePath = base64ToFile(context, base64Audio, msgId)
        if (filePath.isEmpty()) return

        try {
            mediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
                this@VoicePlayerManager.currentlyPlayingMessageId.value = msgId
                this@VoicePlayerManager.isPlaying.value = true
                
                setOnCompletionListener {
                    stop()
                    onFinished()
                }
            }
            startProgressTracker()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isPlaying.value && mediaPlayer != null) {
                try {
                    val duration = mediaPlayer?.duration ?: 1
                    val current = mediaPlayer?.currentPosition ?: 0
                    currentPlaybackPosition.value = current.toFloat() / duration.toFloat()
                } catch (e: Exception) {}
                kotlinx.coroutines.delay(100)
            }
        }
    }

    fun stop() {
        progressJob?.cancel()
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
            currentlyPlayingMessageId.value = null
            currentPlaybackPosition.value = 0f
            isPlaying.value = false
        }
    }
}

fun fileToBase64(filePath: String): String {
    val file = java.io.File(filePath)
    if (!file.exists()) return ""
    return try {
        val bytes = file.readBytes()
        android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
    } catch (e: Exception) {
        ""
    }
}

fun base64ToFile(context: android.content.Context, base64Str: String, msgId: String): String {
    val file = java.io.File(context.cacheDir, "voice_msg_$msgId.3gp")
    if (file.exists()) return file.absolutePath
    return try {
        val bytes = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT)
        file.writeBytes(bytes)
        file.absolutePath
    } catch (e: Exception) {
        ""
    }
}

@Composable
fun VoiceMessagePlayerItem(
    msg: ChallengeMessage,
    voicePlayerManager: VoicePlayerManager,
    isMe: Boolean
) {
    val isCurrent = voicePlayerManager.currentlyPlayingMessageId.value == msg.id
    val isPlaying = isCurrent && voicePlayerManager.isPlaying.value
    val progress = if (isCurrent) voicePlayerManager.currentPlaybackPosition.value else 0f
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .widthIn(min = 180.dp, max = 260.dp)
            .padding(vertical = 4.dp)
    ) {
        IconButton(
            onClick = {
                voicePlayerManager.play(msg.id, msg.voiceUrl) {}
            },
            modifier = Modifier
                .size(36.dp)
                .background(if (isMe) Color.White.copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.15f), CircleShape)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = if (isMe) Color.White else CyanAccent,
                modifier = Modifier.size(18.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isMe) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                        .background(if (isMe) Color.White else CyanAccent)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ভয়েস বার্তা", 
                    color = if (isMe) Color.White.copy(alpha = 0.8f) else Color.LightGray, 
                    fontSize = 11.sp
                )
                
                val durationStr = if (msg.voiceDurationSec > 0) {
                    val minutes = msg.voiceDurationSec / 60
                    val seconds = msg.voiceDurationSec % 60
                    String.format("%02d:%02d", minutes, seconds).localizeDigits()
                } else {
                    "00:05".localizeDigits()
                }
                Text(
                    text = durationStr, 
                    color = if (isMe) Color.White.copy(alpha = 0.8f) else Color.LightGray, 
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun ChallengeChatRoom(
    challenge: Challenge,
    communityViewModel: CommunityViewModel,
    currentUserEmail: String,
    onBack: () -> Unit
) {
    val messagesMap by communityViewModel.challengeMessages.collectAsState()
    val messages = messagesMap[challenge.id] ?: emptyList()
    var inputMsg by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isPremiumUserState by PremiumManager.isPremium.collectAsState()
    val premiumUsers by communityViewModel.premiumUsers.collectAsState()
    val context = LocalContext.current
    val voicePlayerManager = remember { VoicePlayerManager(context) }

    var isRecording by remember { mutableStateOf(false) }
    var recordingDurationSec by remember { mutableStateOf(0) }
    var mediaRecorder by remember { mutableStateOf<android.media.MediaRecorder?>(null) }
    val recordFilePath = remember { java.io.File(context.cacheDir, "temp_voice_record.3gp").absolutePath }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDurationSec = 0
            while (isRecording) {
                delay(1000)
                recordingDurationSec += 1
            }
        }
    }

    DisposableEffect(Unit) {
        ChatNavigationState.isInsideChat.value = true
        onDispose {
            ChatNavigationState.isInsideChat.value = false
            voicePlayerManager.stop()
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
            } catch (e: Exception) {}
        }
    }

    val startRecordingLambda = {
        try {
            val file = java.io.File(recordFilePath)
            if (file.exists()) file.delete()
            
            val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                android.media.MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                android.media.MediaRecorder()
            }.apply {
                setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
                setOutputFormat(android.media.MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(recordFilePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            isRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "রেকর্ডিং শুরু করা যায়নি!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val stopAndSendRecordingLambda = {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
             mediaRecorder = null
             isRecording = false
        }
        
        val base64 = fileToBase64(recordFilePath)
        if (base64.isNotEmpty() && recordingDurationSec > 0) {
            val banglaVoiceText = "ভয়েস মেসেজ (স্পিকার টিপুন) 🎙️"
            communityViewModel.sendChallengeMessage(
                challengeId = challenge.id,
                text = banglaVoiceText,
                voiceUrl = base64,
                voiceDurationSec = recordingDurationSec
            )
        }
    }

    val cancelRecordingLambda = {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
             mediaRecorder = null
             isRecording = false
        }
        try {
            val file = java.io.File(recordFilePath)
            if (file.exists()) file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                startRecordingLambda()
            } else {
                android.widget.Toast.makeText(context, "ভয়েস রেকর্ডের জন্য অনুগ্রহ করে অনুমতি প্রদান করুন", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(challenge.id) {
        communityViewModel.fetchChallengeMessages(challenge.id)
    }

    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val isBn = LanguageStore.currentLanguage.value == "bn"
    val titleTextColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descTextColor = if (isLightMode) Color(0xFF475569) else Color(0xFF94A3B8)
    val canvasBg = if (isLightMode) com.example.ui.theme.PorcelainCanvas else com.example.ui.theme.ObsidianCanvas
    val glassHeaderBg = if (isLightMode) Color.White.copy(alpha = 0.92f) else Color(0xFF13141D).copy(alpha = 0.90f)
    val glassHeaderBorder = if (isLightMode) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.10f)
    val glassBottomBarBg = if (isLightMode) Color.White.copy(alpha = 0.94f) else Color(0xFF11121A).copy(alpha = 0.92f)
    val glassInputBg = if (isLightMode) Color(0xFFF1F5F9) else Color(0xFF1B1C26)
    val glassInputBorder = if (isLightMode) Color(0xFFCBD5E1) else Color.White.copy(alpha = 0.12f)
    val myBubbleGradient = androidx.compose.ui.graphics.Brush.horizontalGradient(
        listOf(Color(0xFF00B4D8), Color(0xFF0284C7))
    )
    val otherBubbleBg = if (isLightMode) Color.White else Color(0xFF1D1E28)
    val otherBubbleBorder = if (isLightMode) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.12f)
    val otherTextColor = if (isLightMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)

    Column(modifier = Modifier.fillMaxSize().background(canvasBg)) {
        // Glass Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(glassHeaderBg)
                .border(BorderStroke(1.dp, androidx.compose.ui.graphics.Brush.verticalGradient(listOf(glassHeaderBorder, Color.Transparent))))
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassChatIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
                tint = titleTextColor,
                containerColor = if (isLightMode) Color.Black.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.08f),
                borderColor = if (isLightMode) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.12f),
                size = 38.dp,
                iconSize = 20.dp
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Channel Icon with Live Glow
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isLightMode) Color(0xFF0284C7).copy(alpha = 0.10f) else CyanAccent.copy(alpha = 0.12f), CircleShape)
                    .border(1.5.dp, CyanAccent.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    tint = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Channel Title & Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = challenge.title,
                    color = titleTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(5.dp).background(Color(0xFF34C759), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${challenge.durationDays} ${if (isBn) "দিন" else "Days"} • ${challenge.participantCount} ${if (isBn) "সদস্য" else "Members"}",
                        color = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Top Bar Action Buttons (non-overlapping)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassChatIconButton(
                    icon = Icons.Filled.HeadsetMic,
                    contentDescription = "Voice Channel",
                    onClick = {
                        android.widget.Toast.makeText(context, "Coming Soon 🎧", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    tint = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                    containerColor = if (isLightMode) Color(0xFF0284C7).copy(alpha = 0.08f) else CyanAccent.copy(alpha = 0.12f),
                    borderColor = if (isLightMode) Color(0xFF0284C7).copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.25f),
                    size = 35.dp,
                    iconSize = 18.dp
                )

                GlassChatIconButton(
                    icon = Icons.Filled.Info,
                    contentDescription = "Channel Details",
                    onClick = {
                        android.widget.Toast.makeText(context, "${challenge.title}\n${challenge.participantCount} ${if (isBn) "জন অংশগ্রহণকারী যুক্ত আছেন" else "participants active"}", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    tint = descTextColor,
                    containerColor = if (isLightMode) Color.Black.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.06f),
                    borderColor = if (isLightMode) Color(0xFFCBD5E1).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f),
                    size = 35.dp,
                    iconSize = 18.dp
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .background(if (isLightMode) Color.White.copy(alpha = 0.8f) else Color(0xFF161722).copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                                .border(1.dp, glassHeaderBorder, RoundedCornerShape(16.dp))
                                .padding(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Groups,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isBn) "শুরুতেই একটি অনুপ্রেরণাদায়ক বার্তা পাঠিয়ে গ্রুপে প্রথম ব্যক্তি হোন! ✨" else "Be the first to share an inspiring message in this group! ✨",
                                color = descTextColor,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                items(messages) { msg ->
                    val isMe = msg.senderEmail == currentUserEmail
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            val premiumUserEmailsList by communityViewModel.premiumUserEmails.collectAsState()
                            val isSenderPremium = msg.isPremium || msg.senderEmail == "muhaiminulhasananas@gmail.com" || premiumUserEmailsList.contains(msg.senderEmail) || (isMe && isPremiumUserState)
                            if (!isMe) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                                    PremiumNameText(
                                        name = msg.senderName.ifBlank { "Member" },
                                        isPremium = isSenderPremium,
                                        defaultColor = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isSenderPremium) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        ProBlueBadge(size = 12.dp)
                                    }
                                    if (msg.senderEmail == "muhaiminulhasananas@gmail.com") {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Filled.Verified, contentDescription = "Admin", tint = CyanAccent, modifier = Modifier.size(12.dp))
                                    }
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                                    if (isSenderPremium) {
                                        ProBlueBadge(size = 12.dp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    if (msg.senderEmail == "muhaiminulhasananas@gmail.com") {
                                        Icon(Icons.Filled.Verified, contentDescription = "Admin", tint = CyanAccent, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    PremiumNameText(
                                        name = if (isBn) "আপনি" else "You",
                                        isPremium = isSenderPremium,
                                        defaultColor = descTextColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))

                            // Glass Message Bubble
                            Box(
                                modifier = Modifier
                                    .clip(
                                        if (isMe) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
                                        else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                                    )
                                    .then(
                                        if (isMe) {
                                            Modifier
                                                .background(myBubbleGradient)
                                                .border(BorderStroke(0.8.dp, Color.White.copy(alpha = 0.25f)), RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp))
                                        } else {
                                            Modifier
                                                .background(otherBubbleBg)
                                                .border(BorderStroke(0.8.dp, otherBubbleBorder), RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                                        }
                                    )
                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                            ) {
                                if (msg.voiceUrl.isNotEmpty()) {
                                    VoiceMessagePlayerItem(
                                        msg = msg,
                                        voicePlayerManager = voicePlayerManager,
                                        isMe = isMe
                                    )
                                } else {
                                    Text(
                                        text = msg.message,
                                        color = if (isMe) Color.White else otherTextColor,
                                        fontSize = 14.sp,
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Glass Bottom Bar (Sleek pill dock)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
            color = glassBottomBarBg,
            border = BorderStroke(1.dp, androidx.compose.ui.graphics.Brush.verticalGradient(listOf(glassHeaderBorder, Color.Transparent)))
        ) {
            if (isRecording) {
                val infiniteTransition = rememberInfiniteTransition()
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 800),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GlassChatIconButton(
                        icon = Icons.Filled.Delete,
                        contentDescription = "Cancel Recording",
                        onClick = { cancelRecordingLambda() },
                        tint = Color(0xFFEF4444),
                        containerColor = Color(0xFFEF4444).copy(alpha = 0.12f),
                        borderColor = Color(0xFFEF4444).copy(alpha = 0.3f),
                        size = 38.dp,
                        iconSize = 20.dp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.Red.copy(alpha = alpha))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val minSecStr = String.format("%02d:%02d", recordingDurationSec / 60, recordingDurationSec % 60).localizeDigits()
                        Text(
                            text = "${if (isBn) "ভয়েস রেকর্ড হচ্ছে..." else "Recording audio..."} $minSecStr",
                            color = titleTextColor,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    GlassChatIconButton(
                        icon = Icons.Filled.Check,
                        contentDescription = "Stop & Send Voice",
                        onClick = { stopAndSendRecordingLambda() },
                        tint = Color(0xFF22C55E),
                        containerColor = Color(0xFF22C55E).copy(alpha = 0.15f),
                        borderColor = Color(0xFF22C55E).copy(alpha = 0.35f),
                        size = 38.dp,
                        iconSize = 20.dp
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice note button (Glass round button)
                    val hasLockPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.RECORD_AUDIO
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                    GlassChatIconButton(
                        icon = Icons.Filled.Mic,
                        contentDescription = "Record Voice",
                        onClick = {
                            if (hasLockPermission) {
                                startRecordingLambda()
                            } else {
                                permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        tint = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                        containerColor = if (isLightMode) Color(0xFF0284C7).copy(alpha = 0.08f) else CyanAccent.copy(alpha = 0.12f),
                        borderColor = if (isLightMode) Color(0xFF0284C7).copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.25f),
                        size = 38.dp,
                        iconSize = 19.dp
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Glass Pill Input Field
                    OutlinedTextField(
                        value = inputMsg,
                        onValueChange = { inputMsg = it },
                        placeholder = { Text(if (isBn) "গ্রুপে আপনার বার্তা লিখুন..." else "Message group...", color = descTextColor, fontSize = 13.5.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                            unfocusedBorderColor = glassInputBorder,
                            focusedTextColor = titleTextColor,
                            unfocusedTextColor = titleTextColor,
                            focusedContainerColor = glassInputBg,
                            unfocusedContainerColor = glassInputBg
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Action button: Like (👍) or Gradient Send
                    if (inputMsg.isBlank()) {
                        GlassChatIconButton(
                            icon = Icons.Filled.ThumbUp,
                            contentDescription = "Cheer",
                            onClick = {
                                communityViewModel.sendChallengeMessage(challenge.id, "👍")
                            },
                            tint = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                            containerColor = if (isLightMode) Color(0xFF0284C7).copy(alpha = 0.08f) else CyanAccent.copy(alpha = 0.12f),
                            borderColor = if (isLightMode) Color(0xFF0284C7).copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.25f),
                            size = 38.dp,
                            iconSize = 19.dp
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(myBubbleGradient, CircleShape)
                                .clickable {
                                    if (inputMsg.isNotBlank()) {
                                        val toSend = inputMsg.trim()
                                        inputMsg = ""
                                        communityViewModel.sendChallengeMessage(challenge.id, toSend)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengesTabContent(
    communityViewModel: CommunityViewModel,
    currentUserEmail: String,
    onOpenChat: (Challenge) -> Unit
) {
    val challenges by communityViewModel.challenges.collectAsState()
    var showCreateForm by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newDuration by remember { mutableStateOf("30") }
    val context = LocalContext.current

    val joinedChallenges = challenges.filter { it.participants.contains(currentUserEmail) }
    val availableChallenges = challenges.filter { !it.participants.contains(currentUserEmail) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        if (!showCreateForm) {
            Button(
                onClick = { showCreateForm = true },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                modifier = Modifier.fillMaxWidth().height(48.dp).padding(vertical = 4.dp),
                shape = CircleShape
            ) {
                Text(t("create_challenge_btn"), color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = CardBlue),
                border = BorderStroke(1.dp, CyanAccent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Create New Challenge", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title (e.g. 15 Days Clean)", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = CyanAccent,
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("Description/Rules", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = CyanAccent,
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newDuration,
                        onValueChange = { newDuration = it },
                        label = { Text("Duration (Days)", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = CyanAccent,
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCreateForm = false }) {
                            Text(t("cancel"), color = Color.LightGray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newTitle.isNotBlank() && newDescription.isNotBlank()) {
                                    val duration = newDuration.toIntOrNull() ?: 30
                                    communityViewModel.createChallenge(newTitle, newDescription, duration)
                                    android.widget.Toast.makeText(context, t("challenge_saved_success"), android.widget.Toast.LENGTH_SHORT).show()
                                    newTitle = ""
                                    newDescription = ""
                                    showCreateForm = false
                                } else {
                                    android.widget.Toast.makeText(context, t("fill_required_fields"), android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = CircleShape
                        ) {
                            Text(if (LanguageStore.currentLanguage.value == "bn") "তৈরি করুন" else "Create", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Your Challenges Section
        val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
        val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
        val descColor = if (isLightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)

        if (joinedChallenges.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Verified, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "আপনার চ্যালেঞ্জসমূহ (Joined)",
                    color = titleColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    style = TextStyle(shadow = floatingTextShadow(elevation = 2.dp))
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            joinedChallenges.forEach { challenge ->
                ChallengeCard(
                    challenge = challenge,
                    isJoined = true,
                    onJoin = { communityViewModel.joinChallenge(challenge.id) },
                    onLeave = { communityViewModel.leaveChallenge(challenge.id) },
                    onOpenChat = { onOpenChat(challenge) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Available Challenges Section
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Explore, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "অন্যান্য চ্যালেঞ্জসমূহ (Available)",
                color = titleColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                style = TextStyle(shadow = floatingTextShadow(elevation = 2.dp))
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (availableChallenges.isEmpty()) {
            Text(
                "No other challenges available yet. Create one!",
                color = descColor,
                modifier = Modifier.padding(vertical = 8.dp),
                style = TextStyle(shadow = floatingTextShadow(elevation = 1.2.dp))
            )
        } else {
            availableChallenges.forEach { challenge ->
                ChallengeCard(
                    challenge = challenge,
                    isJoined = false,
                    onJoin = { communityViewModel.joinChallenge(challenge.id) },
                    onLeave = { communityViewModel.leaveChallenge(challenge.id) },
                    onOpenChat = { onOpenChat(challenge) }
                )
            }
        }
    }
}

@Composable
fun ChallengeCard(
    challenge: Challenge,
    isJoined: Boolean,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onOpenChat: () -> Unit
) {
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descColor = if (isLightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .glassCard(cornerSize = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .innerFloatingGlassCard(cornerSize = 10.dp, elevation = 1.dp, borderGlow = CyanAccent)
                        .padding(8.dp)
                ) {
                    Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        challenge.title,
                        color = titleColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        style = TextStyle(shadow = floatingTextShadow(elevation = 2.dp))
                    )
                    Text(
                        String.format(t("challenge_participants"), challenge.participantCount),
                        color = descColor,
                        fontSize = 12.sp,
                        style = TextStyle(shadow = floatingTextShadow(elevation = 1.2.dp))
                    )
                }
                Box(
                    modifier = Modifier
                        .innerFloatingGlassCard(cornerSize = 8.dp, elevation = 1.dp, borderGlow = CyanAccent)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${challenge.durationDays} Days",
                        color = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(shadow = floatingTextShadow(elevation = 1.2.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                challenge.description,
                color = descColor,
                fontSize = 14.sp,
                style = TextStyle(shadow = floatingTextShadow(elevation = 1.2.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    String.format(t("challenge_creator"), challenge.creatorName),
                    color = descColor,
                    fontSize = 11.5.sp,
                    modifier = Modifier.weight(1f),
                    style = TextStyle(shadow = floatingTextShadow(elevation = 1.dp))
                )
                
                Row {
                    if (isJoined) {
                        TextButton(onClick = onLeave) {
                            Text(t("leave"), color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onOpenChat,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Filled.ChatBubble, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(t("chat_enter"), color = Color(0xFF0F172A), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onJoin,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = CircleShape
                        ) {
                            Text(t("join"), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengeChatRoomOld(
    challenge: Challenge,
    communityViewModel: CommunityViewModel,
    currentUserEmail: String,
    onBack: () -> Unit
) {
    val messagesMap by communityViewModel.challengeMessages.collectAsState()
    val messages = messagesMap[challenge.id] ?: emptyList()
    var inputMsg by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Scroll to bottom on load or new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(challenge.id) {
        communityViewModel.fetchChallengeMessages(challenge.id)
    }

    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descColor = if (isLightMode) Color(0xFF475569) else Color.LightGray
    val topBarBg = if (isLightMode) Color.White.copy(alpha = 0.9f) else CardBlue
    val incomingMsgBg = if (isLightMode) Color.White else CardBlue
    val incomingTextColor = if (isLightMode) Color(0xFF0F172A) else Color.White

    Column(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        // Custom Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(topBarBg)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = titleColor)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(challenge.title, color = titleColor, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                Text(String.format(t("challenge_participants"), challenge.participantCount), color = descColor, fontSize = 12.sp)
            }
        }

        // Messages list
        androidx.compose.foundation.lazy.LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages.size) { index ->
                val msg = messages[index]
                val isMe = msg.senderEmail == currentUserEmail
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    if (!isMe) {
                        Text(msg.senderName.ifBlank { "Member" }, color = descColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                if (isMe) Color(0xFFEF4444) else incomingMsgBg,
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isMe) 12.dp else 0.dp,
                                    bottomEnd = if (isMe) 0.dp else 12.dp
                                )
                            )
                            .padding(12.dp)
                    ) {
                        Text(msg.message, color = if (isMe) Color.White else incomingTextColor, fontSize = 14.sp)
                    }
                }
            }
        }

        // Message input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isLightMode) Color(0xFFF8FAFC) else Color(0xFF0F172A))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputMsg,
                onValueChange = { inputMsg = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message...", color = descColor) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = if (isLightMode) Color(0xFF0F172A) else Color.White,
                    unfocusedTextColor = if (isLightMode) Color(0xFF0F172A) else Color.White,
                    focusedContainerColor = if (isLightMode) Color(0xFFE2E8F0) else CardBlue,
                    unfocusedContainerColor = if (isLightMode) Color(0xFFE2E8F0) else CardBlue
                ),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputMsg.isNotBlank()) {
                        communityViewModel.sendChallengeMessage(challenge.id, inputMsg.trim())
                        inputMsg = ""
                    }
                },
                modifier = Modifier.background(CyanAccent, CircleShape).size(44.dp)
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun CommunityPost(
    post: Post,
    currentUserId: String,
    communityViewModel: CommunityViewModel,
    onUserClicked: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    var showComments by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descColor = if (isLightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)

    val hasLiked = post.likedBy.contains(currentUserId)
    val premiumUserEmailsList by communityViewModel.premiumUserEmails.collectAsState()
    val isPremiumUserState by PremiumManager.isPremium.collectAsState()
    val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val isAuthorPremium = post.isPremium || post.userEmail == "muhaiminulhasananas@gmail.com" || premiumUserEmailsList.contains(post.userEmail) || (post.userEmail == authUser?.email && isPremiumUserState)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .glassCard(cornerSize = 18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header matching Community@1x.png
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val initialLetter = post.name.trim().firstOrNull()?.uppercase() ?: "W"
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF262C3A), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                        .clickable(enabled = onUserClicked != null && post.userId.isNotEmpty()) {
                            onUserClicked?.invoke(post.userId)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initialLetter,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = onUserClicked != null && post.userId.isNotEmpty()) {
                            onUserClicked?.invoke(post.userId)
                        }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PremiumNameText(
                            name = post.name.ifBlank { "Anonymous" },
                            isPremium = isAuthorPremium,
                            defaultColor = titleColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isAuthorPremium) {
                            Spacer(modifier = Modifier.width(4.dp))
                            ProBlueBadge(size = 14.dp)
                        }
                        if (post.userEmail == "muhaiminulhasananas@gmail.com") {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Filled.Verified, contentDescription = "Admin", tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                        }
                    }
                    if (post.streak.isNotEmpty()) {
                        val cleanStreak = post.streak.replace("🔥", "").trim().lowercase()
                        Text(
                            text = cleanStreak,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
                if (post.badge.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0F293A), RoundedCornerShape(50))
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.25f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = post.badge,
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                post.content,
                color = Color(0xFFF1F5F9),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            // Image attachment if any
            if (post.imageUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                coil.compose.AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Post Image",
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
                )
            }

            // Video attachment if any
            if (post.videoUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        android.widget.VideoView(ctx).apply {
                            setVideoURI(android.net.Uri.parse(post.videoUrl))
                            val controller = android.widget.MediaController(ctx)
                            controller.setAnchorView(this)
                            setMediaController(controller)
                            setOnPreparedListener { it.start() }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(10.dp))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { communityViewModel.toggleLike(post.id, post.likedBy) }
                ) {
                    Icon(
                        imageVector = if (hasLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (hasLiked) Color(0xFFEF4444) else Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = post.claps.toString(),
                        color = Color(0xFF94A3B8),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        showComments = !showComments
                        if (showComments) communityViewModel.fetchComments(post.id)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = post.commentsCount.toString(),
                        color = Color(0xFF94A3B8),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (showComments) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = if (isLightMode) Color(0xFFCBD5E1) else Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))

                val commentsMap by communityViewModel.comments.collectAsState()
                val postComments = commentsMap[post.id] ?: emptyList()

                if (postComments.isEmpty()) {
                    Text("No comments yet. Be the first!", color = descColor, fontSize = 12.sp)
                } else {
                    postComments.forEach { comment ->
                        Column(modifier = Modifier.padding(bottom = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val isCommenterPremium = comment.isPremium || comment.userEmail == "muhaiminulhasananas@gmail.com" || premiumUserEmailsList.contains(comment.userEmail) || (comment.userEmail == authUser?.email && isPremiumUserState)
                                PremiumNameText(
                                    name = comment.name,
                                    isPremium = isCommenterPremium,
                                    defaultColor = titleColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isCommenterPremium) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    ProBlueBadge(size = 12.dp)
                                }
                            }
                            if (comment.userEmail == "muhaiminulhasananas@gmail.com") {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)) {
                                    Icon(Icons.Filled.Verified, contentDescription = "Admin", tint = CyanAccent, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Admin", color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(comment.content, color = descColor, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Write a comment...", color = descColor, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = if (isLightMode) Color(0xFFCBD5E1) else Color.DarkGray,
                            focusedBorderColor = CyanAccent,
                            unfocusedTextColor = titleColor,
                            focusedTextColor = titleColor
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                communityViewModel.addComment(post.id, commentText)
                                commentText = ""
                            }
                        },
                        modifier = Modifier.background(CyanAccent, CircleShape).size(40.dp)
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = viewModel(),
    purityTrackerViewModel: PurityTrackerViewModel = viewModel(),
    communityViewModel: CommunityViewModel = viewModel(),
    userProfileViewModel: UserProfileViewModel = viewModel(),
    socialViewModel: SocialViewModel = viewModel(),
    onNavigateToStaticPage: (String) -> Unit = {},
    onNavigateToPremium: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val isPremiumUserState by PremiumManager.isPremium.collectAsState()
    val activity = context as? androidx.activity.ComponentActivity

    // Handle deep link authentication intent callbacks at screen launch or session changes safely!
    LaunchedEffect(activity?.intent) {
        val currentIntent = activity?.intent
        if (currentIntent != null && currentIntent.data != null) {
            authViewModel.checkAndHandleEmailLink(currentIntent, context) { success, msg ->
                if (success) {
                    android.widget.Toast.makeText(context, "$msg ✅", android.widget.Toast.LENGTH_LONG).show()
                } else {
                    android.widget.Toast.makeText(context, "$msg ❌", android.widget.Toast.LENGTH_LONG).show()
                }
                // Reset/clear processed intent to prevent looping on screen rotation
                activity.intent = null
            }
        }
    }

    // Attempt to migrate cloud streak when logging in
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            userProfileViewModel.clearProfile()
        } else {
            // Clear wrong/previous profile caches if any
            val currentCachedProfile = userProfileViewModel.userProfile.value
            if (currentCachedProfile != null && currentCachedProfile.userId != currentUser!!.uid) {
                userProfileViewModel.clearProfile()
            }
            userProfileViewModel.fetchProfile(currentUser!!.uid)
            purityTrackerViewModel.syncCloudHistoryToLocal(currentUser!!.uid)
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(currentUser!!.uid).get().addOnSuccessListener { doc ->
                val record = doc.toObject(LeaderboardUser::class.java)
                val isMeAnas = currentUser?.email == "muhaiminulhasananas@gmail.com"
                
                var expiry = doc.getLong("premiumExpiryTimestamp") ?: 0L
                var hasCloudPremium = doc.getBoolean("isPremium") == true || isMeAnas
                
                if (!isMeAnas && hasCloudPremium && expiry != 0L && System.currentTimeMillis() > expiry) {
                    hasCloudPremium = false
                    expiry = 0L
                    db.collection("users").document(currentUser!!.uid).update(
                        "isPremium", false,
                        "premiumPlan", "expired",
                        "premiumExpiryTimestamp", 0L
                    )
                }
                
                PremiumManager.setPremiumStatus(context, hasCloudPremium, expiry)
                
                if (record != null) {
                    if (record.streakMs > 0) {
                        val elapsed = System.currentTimeMillis() - record.timestamp
                        val realStreak = record.streakMs + elapsed
                        purityTrackerViewModel.restoreStreak(System.currentTimeMillis() - realStreak)
                        // Cloud sync restores streak data silently in background without showing annoying black toast
                    } else if (record.timestamp > 0) {
                        purityTrackerViewModel.restoreStreak(System.currentTimeMillis())
                    }
                }
            }
        }
    }

    var activeView by remember { mutableStateOf("profile") }
    var chatFriendId by remember { mutableStateOf("") }

    if (activeView == "friends") {
        FriendsListScreen(
            socialViewModel = socialViewModel,
            onBack = { activeView = "profile" },
            onOpenChat = { friendId ->
                chatFriendId = friendId
                activeView = "chat"
            }
        )
        return
    } else if (activeView == "chat") {
        PrivateChatScreen(
            friendId = chatFriendId,
            socialViewModel = socialViewModel,
            onBack = { activeView = "friends" }
        )
        return
    }

    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descColor = if (isLightMode) Color(0xFF475569) else Color.LightGray

    // Conditional Account Page View:
    // Before login (currentUser == null), show ONLY the clean Login/Verification screen with ZERO scrolling.
    // All Premium ("WARRIOR PRO"), "Protection Tools", and other options are 100% hidden until logged in.
    if (currentUser == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            ImprovedAuthScreen(
                authViewModel = authViewModel,
                userProfileViewModel = userProfileViewModel,
                purityTrackerViewModel = purityTrackerViewModel
            )
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
         Text(
             text = if (LanguageStore.currentLanguage.value == "bn") "অ্যাকাউন্ট" else "Account",
             color = titleColor,
             fontSize = 24.sp,
             fontWeight = FontWeight.Bold,
             modifier = Modifier.fillMaxWidth()
         )
         Spacer(modifier = Modifier.height(16.dp))
         
         var showGuardianShieldSetup by remember { mutableStateOf(false) }
         var showAccountProStatusDialog by remember { mutableStateOf(false) }
         val isBnAccount = LanguageStore.currentLanguage.value == "bn"
         val expiryTimestampAccount by PremiumManager.premiumExpiryTimestamp.collectAsState()
         val isAdminEmailAccount = currentUser?.email == "muhaiminulhasananas@gmail.com"
         val isEffectiveProAccount = isPremiumUserState || isAdminEmailAccount
         val proTimeInfoAccount = remember(isEffectiveProAccount, expiryTimestampAccount) {
             PremiumManager.getPremiumTimeInfo(isEffectiveProAccount, expiryTimestampAccount)
         }

         if (showAccountProStatusDialog) {
             PremiumStatusDialog(
                 onDismiss = { showAccountProStatusDialog = false },
                 onManagePlan = {
                     showAccountProStatusDialog = false
                     onNavigateToPremium?.invoke()
                 }
             )
         }

         val userProfile by userProfileViewModel.userProfile.collectAsState()
         if (userProfile == null || userProfile?.username.isNullOrEmpty()) {
             ProfileSetupScreen(userProfileViewModel = userProfileViewModel) {
                 userProfileViewModel.fetchProfile(currentUser!!.uid)
             }
         } else {
             UserProfileContent(
                 userProfile = userProfile!!,
                 authViewModel = authViewModel,
                 purityTrackerViewModel = purityTrackerViewModel,
                 socialViewModel = socialViewModel,
                 userProfileViewModel = userProfileViewModel,
                 isPremiumUserState = isPremiumUserState,
                 onNavigateToStaticPage = onNavigateToStaticPage,
                 onOpenFriendsList = {
                     CommunitySubTabState.requestedSubTab.value = "friends"
                     onNavigateToStaticPage("community")
                 },
                 onOpenGuardianShield = { showGuardianShieldSetup = !showGuardianShieldSetup }
             )
         }

         Spacer(modifier = Modifier.height(14.dp))

         // WARRIOR PRO Plan Status & Days Remaining Card (Shown only after login)
         Card(
             modifier = Modifier
                 .fillMaxWidth()
                 .clickable {
                     if (isEffectiveProAccount) showAccountProStatusDialog = true
                     else onNavigateToPremium?.invoke()
                 },
             shape = RoundedCornerShape(18.dp),
             colors = CardDefaults.cardColors(containerColor = Color.Transparent),
             border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f))
         ) {
             Column(
                 modifier = Modifier
                     .fillMaxWidth()
                     .background(Color.White.copy(alpha = 0.08f))
                     .padding(horizontal = 16.dp, vertical = 14.dp)
             ) {
                 Row(
                     modifier = Modifier.fillMaxWidth(),
                     verticalAlignment = Alignment.CenterVertically,
                     horizontalArrangement = Arrangement.SpaceBetween
                 ) {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                         Box(
                             modifier = Modifier
                                 .size(34.dp)
                                 .background(Color(0xFFEAB308).copy(alpha = 0.16f), CircleShape)
                                 .border(1.dp, Color(0xFFFACC15).copy(alpha = 0.4f), CircleShape),
                             contentAlignment = Alignment.Center
                         ) {
                             Icon(
                                 Icons.Filled.Star,
                                 contentDescription = null,
                                 tint = Color(0xFFFACC15),
                                 modifier = Modifier.size(18.dp)
                             )
                         }
                         Spacer(modifier = Modifier.width(10.dp))
                         Column {
                             Text(
                                 text = "WARRIOR PRO",
                                 fontSize = 13.sp,
                                 fontWeight = FontWeight.Black,
                                 color = Color(0xFFFACC15),
                                 letterSpacing = 0.6.sp
                             )
                             Text(
                                 text = if (proTimeInfoAccount.isPremium && proTimeInfoAccount.isLifetime) {
                                     if (isBnAccount) "সক্রিয় · লাইফটাইম প্রো" else "Active · Lifetime Pro"
                                 } else if (proTimeInfoAccount.isPremium) {
                                     if (isBnAccount) "সক্রিয় · ${proTimeInfoAccount.daysLeft} দিন বাকি" else "Active · ${proTimeInfoAccount.daysLeft} days left"
                                 } else {
                                     if (isBnAccount) "ফ্রি প্ল্যান · ২৩ দিন ট্রায়াল/প্রো" else "Free Plan · 23 days left"
                                 },
                                 fontSize = 11.5.sp,
                                 color = Color(0xFFCBD5E1)
                             )
                         }
                     }

                     Box(
                         modifier = Modifier
                             .background(Color(0xFFEAB308).copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                             .border(1.dp, Color(0xFFFACC15).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                             .padding(horizontal = 10.dp, vertical = 5.dp)
                     ) {
                         Text(
                             text = if (isBnAccount) "ম্যানেজ" else "Manage",
                             fontSize = 11.sp,
                             fontWeight = FontWeight.Bold,
                             color = Color(0xFFFACC15)
                         )
                     }
                 }

                 Spacer(modifier = Modifier.height(10.dp))

                 LinearProgressIndicator(
                     progress = {
                         if (proTimeInfoAccount.isPremium && proTimeInfoAccount.isLifetime) 1f
                         else if (proTimeInfoAccount.isPremium) (proTimeInfoAccount.daysLeft.toFloat() / 30f).coerceIn(0.08f, 1f)
                         else 0.75f
                     },
                     modifier = Modifier
                         .fillMaxWidth()
                         .height(4.dp)
                         .clip(RoundedCornerShape(2.dp)),
                     color = Color(0xFFFACC15),
                     trackColor = Color(0xFF1E293B)
                 )

                 Spacer(modifier = Modifier.height(6.dp))

                 Row(
                     modifier = Modifier.fillMaxWidth(),
                     horizontalArrangement = Arrangement.SpaceBetween,
                     verticalAlignment = Alignment.CenterVertically
                 ) {
                     Text(
                         text = if (isBnAccount) "এআই কোচ সক্রিয়" else "AI coach active",
                         fontSize = 11.sp,
                         color = Color(0xFF94A3B8)
                     )
                     Text(
                         text = if (proTimeInfoAccount.isPremium && !proTimeInfoAccount.isLifetime) "${proTimeInfoAccount.daysLeft}d" else if (proTimeInfoAccount.isPremium) "PRO" else "23d",
                         fontSize = 11.sp,
                         fontWeight = FontWeight.Bold,
                         color = Color(0xFFFACC15)
                     )
                 }
             }
         }

         if (showGuardianShieldSetup) {
             Spacer(modifier = Modifier.height(14.dp))
             Card(
                 modifier = Modifier
                     .fillMaxWidth()
                     .glassCard(cornerSize = 18.dp),
                 colors = CardDefaults.cardColors(containerColor = Color.Transparent)
             ) {
                 Column(modifier = Modifier.padding(16.dp)) {
                     Row(
                         modifier = Modifier.fillMaxWidth(),
                         verticalAlignment = Alignment.CenterVertically,
                         horizontalArrangement = Arrangement.SpaceBetween
                     ) {
                         Row(verticalAlignment = Alignment.CenterVertically) {
                             Icon(Icons.Filled.Security, contentDescription = null, tint = CyanAccent)
                             Spacer(modifier = Modifier.width(8.dp))
                             Text(t("guardian_shield"), color = titleColor, fontWeight = FontWeight.Bold)
                         }
                         IconButton(onClick = { showGuardianShieldSetup = false }, modifier = Modifier.size(28.dp)) {
                             Icon(Icons.Filled.Close, contentDescription = "Close", tint = descColor, modifier = Modifier.size(18.dp))
                         }
                     }
                     Spacer(modifier = Modifier.height(8.dp))
                     val sharedPref = context.getSharedPreferences("GuardianPrefs", Context.MODE_PRIVATE)
                     var userName by remember { mutableStateOf(sharedPref.getString("userName", "") ?: "") }
                     var guardianName by remember { mutableStateOf(sharedPref.getString("guardianName", "") ?: "") }
                     var guardianEmail by remember { mutableStateOf(sharedPref.getString("guardianEmail", "") ?: "") }
                     var guardianPhone by remember { mutableStateOf(sharedPref.getString("guardianPhone", "") ?: "") }
                     var isShieldActive by remember { mutableStateOf(sharedPref.getBoolean("isShieldActive", false)) }

                     Text(t("guardian_desc"), color = descColor, fontSize = 12.sp)

                     Spacer(modifier = Modifier.height(12.dp))
                     OutlinedTextField(
                         value = userName,
                         onValueChange = { userName = it; sharedPref.edit().putString("userName", it).apply() },
                         label = { Text(t("your_name")) },
                         modifier = Modifier.fillMaxWidth(),
                         colors = OutlinedTextFieldDefaults.colors(
                             unfocusedBorderColor = if (isLightMode) Color(0xFFCBD5E1) else Color.Gray,
                             focusedBorderColor = CyanAccent,
                             unfocusedTextColor = titleColor,
                             focusedTextColor = titleColor,
                             unfocusedLabelColor = descColor,
                             focusedLabelColor = CyanAccent
                         )
                     )
                     Spacer(modifier = Modifier.height(10.dp))
                     OutlinedTextField(
                         value = guardianName,
                         onValueChange = { guardianName = it; sharedPref.edit().putString("guardianName", it).apply() },
                         label = { Text(t("guardian_name")) },
                         modifier = Modifier.fillMaxWidth(),
                         colors = OutlinedTextFieldDefaults.colors(
                             unfocusedBorderColor = if (isLightMode) Color(0xFFCBD5E1) else Color.Gray,
                             focusedBorderColor = CyanAccent,
                             unfocusedTextColor = titleColor,
                             focusedTextColor = titleColor,
                             unfocusedLabelColor = descColor,
                             focusedLabelColor = CyanAccent
                         )
                     )
                     Spacer(modifier = Modifier.height(10.dp))
                     val isGuardianEmailLocked = BlockerManager.hasValidPartnerEmail(context)
                     OutlinedTextField(
                         value = guardianEmail,
                         onValueChange = {
                             if (!isGuardianEmailLocked) {
                                 guardianEmail = it
                             }
                         },
                         readOnly = isGuardianEmailLocked,
                         label = { Text(if (isGuardianEmailLocked) "${t("guardian_email")} (Locked 🔒)" else t("guardian_email")) },
                         modifier = Modifier.fillMaxWidth(),
                         colors = OutlinedTextFieldDefaults.colors(
                             unfocusedBorderColor = if (isLightMode) Color(0xFFCBD5E1) else Color.Gray,
                             focusedBorderColor = CyanAccent,
                             unfocusedTextColor = titleColor,
                             focusedTextColor = titleColor,
                             unfocusedLabelColor = descColor,
                             focusedLabelColor = CyanAccent
                         )
                     )
                     Spacer(modifier = Modifier.height(10.dp))
                     OutlinedTextField(
                         value = if (isPremiumUserState) guardianPhone else "",
                         onValueChange = {
                             if (isPremiumUserState) {
                                 guardianPhone = it
                                 sharedPref.edit().putString("guardianPhone", it).apply()
                             } else {
                                 onNavigateToPremium?.invoke()
                             }
                         },
                         readOnly = !isPremiumUserState,
                         label = { Text(if (isPremiumUserState) t("phone_optional") else "Guardian Phone (Pro Exclusive 👑)") },
                         modifier = Modifier.fillMaxWidth().clickable {
                             if (!isPremiumUserState) {
                                 onNavigateToPremium?.invoke()
                             }
                         },
                         trailingIcon = {
                             if (!isPremiumUserState) {
                                 IconButton(onClick = { onNavigateToPremium?.invoke() }) {
                                     Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = Color(0xFFFFD700))
                                 }
                             }
                         },
                         colors = OutlinedTextFieldDefaults.colors(
                             unfocusedBorderColor = if (isPremiumUserState) (if (isLightMode) Color(0xFFCBD5E1) else Color.Gray) else Color(0xFFFFD700).copy(alpha = 0.5f),
                             focusedBorderColor = if (isPremiumUserState) CyanAccent else Color(0xFFFFD700),
                             unfocusedTextColor = titleColor,
                             focusedTextColor = titleColor,
                             unfocusedLabelColor = if (isPremiumUserState) descColor else Color(0xFFFFD700).copy(alpha = 0.8f),
                             focusedLabelColor = if (isPremiumUserState) CyanAccent else Color(0xFFFFD700)
                         )
                     )
                     Spacer(modifier = Modifier.height(14.dp))

                     Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                         Column(modifier = Modifier.weight(1f)) {
                             Text(t("enable_alert_system"), color = titleColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                             Text(t("alert_system_desc"), color = descColor, fontSize = 11.5.sp)
                         }
                         Switch(
                             checked = isShieldActive,
                             onCheckedChange = {
                                 if (it && (userName.isBlank() || guardianName.isBlank() || guardianEmail.isBlank())) {
                                     android.widget.Toast.makeText(context, t("fill_required_fields"), android.widget.Toast.LENGTH_SHORT).show()
                                     return@Switch
                                 }
                                 isShieldActive = it
                                 sharedPref.edit().putBoolean("isShieldActive", it).apply()
                                 android.widget.Toast.makeText(context, if (it) LanguageStore.get("alert_active") else LanguageStore.get("alert_inactive"), android.widget.Toast.LENGTH_SHORT).show()
                             },
                             colors = SwitchDefaults.colors(checkedThumbColor = DeepBlue, checkedTrackColor = CyanAccent)
                         )
                     }

                     Spacer(modifier = Modifier.height(14.dp))
                     Button(
                         onClick = {
                             if (userName.isBlank() || guardianName.isBlank() || guardianEmail.isBlank()) {
                                 android.widget.Toast.makeText(context, t("fill_required_fields"), android.widget.Toast.LENGTH_SHORT).show()
                             } else {
                                 if (!BlockerManager.hasValidPartnerEmail(context)) {
                                     val err = BlockerManager.validateAndSavePartnerEmail(context, guardianEmail)
                                     if (err != null) {
                                         android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                                         return@Button
                                     }
                                 }
                                 sharedPref.edit().apply {
                                     putString("userName", userName)
                                     putString("guardianName", guardianName)
                                     putString("guardianPhone", guardianPhone)
                                     putBoolean("isShieldActive", isShieldActive)
                                 }.apply()
                                 android.widget.Toast.makeText(context, LanguageStore.get("save_guardian_success"), android.widget.Toast.LENGTH_SHORT).show()
                                 showGuardianShieldSetup = false
                             }
                         },
                         colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                         shape = RoundedCornerShape(14.dp),
                         modifier = Modifier.fillMaxWidth().height(44.dp)
                     ) {
                         Text(t("save_guardian"), color = Color(0xFF061018), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                     }
                 }
             }
         }

         Spacer(modifier = Modifier.height(14.dp))

         // Live Protection Status in Account/Settings (Polled on IO thread using real liveness + real PowerManager)
         var hasPinProfile by remember { mutableStateOf(LockManager.hasPin(context)) }
         var isBlockerActiveProfile by remember { mutableStateOf(BlockerManager.isAdultBlockerEnabled(context)) }
         var isGuardAliveProfile by remember { mutableStateOf(false) }
         var isGuardDeadButEnabledProfile by remember { mutableStateOf(false) }
         var isBatteryIgnoredProfile by remember { mutableStateOf(false) }
         var isDeviceAdminProfile by remember { mutableStateOf(false) }
         var isOemAutoStartVisitedProfile by remember { mutableStateOf(false) }
         var isRecentsLockedProfile by remember { mutableStateOf(false) }

         LaunchedEffect(Unit) {
             while (true) {
                 val status = withContext(Dispatchers.IO) {
                     val pin = LockManager.hasPin(context)
                     val blocker = BlockerManager.isAdultBlockerEnabled(context)
                     val guardAlive = AntiUninstallManager.isAccessibilityServiceAlive(context)
                     val guardDead = AntiUninstallManager.isAccessibilityDeadButEnabled(context)
                     val battery = AntiUninstallManager.isBatteryOptimizationIgnored(context)
                     val admin = AntiUninstallManager.isDeviceAdminActive(context)
                     val oemVisited = AntiUninstallManager.isOemAutoStartVisited(context)
                     val recentsLocked = AntiUninstallManager.isRecentsLockConfirmed(context)
                     GuardHealthWorker.checkNowAndNotify(context)
                     listOf(pin, blocker, guardAlive, guardDead, battery, admin, oemVisited, recentsLocked)
                 }
                 hasPinProfile = status[0]
                 isBlockerActiveProfile = status[1]
                 isGuardAliveProfile = status[2]
                 isGuardDeadButEnabledProfile = status[3]
                 isBatteryIgnoredProfile = status[4]
                 isDeviceAdminProfile = status[5]
                 isOemAutoStartVisitedProfile = status[6]
                 isRecentsLockedProfile = status[7]
                 kotlinx.coroutines.delay(1500)
             }
         }

         ProtectionLiveStatusCard(
             isGuardAlive = isGuardAliveProfile,
             isGuardDeadButEnabled = isGuardDeadButEnabledProfile,
             isBatteryOptimizationIgnored = isBatteryIgnoredProfile,
             isOemAutoStartConfigured = isOemAutoStartVisitedProfile,
             isRecentsLockConfirmed = isRecentsLockedProfile,
             onFixAccessibility = {
                 AntiUninstallManager.requestAccessibilityService(context)
             },
             onFixBatteryOptimization = {
                 AntiUninstallManager.requestIgnoreBatteryOptimization(context)
             },
             onFixOemAutoStart = {
                 AntiUninstallManager.openOemAutoStartSettings(context)
                 isOemAutoStartVisitedProfile = true
             },
             onConfirmRecentsLock = {
                 AntiUninstallManager.markRecentsLockConfirmed(context, true)
                 isRecentsLockedProfile = true
             }
         )

         Spacer(modifier = Modifier.height(12.dp))

         UnifiedProtectionCard(
             hasPin = hasPinProfile,
             isBlockerActive = isBlockerActiveProfile,
             isGuardActive = isGuardAliveProfile,
             isGuardDeadButEnabled = isGuardDeadButEnabledProfile,
             isBatteryExempt = isBatteryIgnoredProfile,
             onBlockerClick = {
                 onNavigateToStaticPage("blocker")
             },
             onPinClick = {
                 android.widget.Toast.makeText(
                     context,
                     if (hasPinProfile) (if (LanguageStore.currentLanguage.value == "bn") "🔒 পিন লক সক্রিয় ও সুরক্ষিত আছে।" else "🔒 PIN Lock is active and secured.")
                     else (if (LanguageStore.currentLanguage.value == "bn") "হোম স্ক্রিনে গিয়ে পিন সেট করুন।" else "Please set PIN from Home screen."),
                     android.widget.Toast.LENGTH_SHORT
                 ).show()
             },
             onGuardClick = {
                 if (isGuardAliveProfile) {
                     android.widget.Toast.makeText(
                         context,
                         if (LanguageStore.currentLanguage.value == "bn") "🛡️ সেটিংস গার্ড সক্রিয় (Alive) ও সুরক্ষিত আছে!" else "🛡️ Settings Guard is Alive and actively protecting!",
                         android.widget.Toast.LENGTH_SHORT
                     ).show()
                 } else if (isGuardDeadButEnabledProfile) {
                     AntiUninstallManager.requestAccessibilityService(context)
                     android.widget.Toast.makeText(
                         context,
                         if (LanguageStore.currentLanguage.value == "bn") "⚠️ সিস্টেম গার্ড থামিয়ে দিয়েছে! Purity Tracker একবার OFF করে আবার ON করুন।" else "⚠️ System stopped the guard! Toggle Purity Tracker OFF and ON again.",
                         android.widget.Toast.LENGTH_LONG
                     ).show()
                 } else {
                     SettingsGuardWatchdogService.startIfInForeground(context)
                     AntiUninstallManager.requestAccessibilityService(context)
                     android.widget.Toast.makeText(
                         context,
                         if (LanguageStore.currentLanguage.value == "bn") "'Purity Tracker' খুঁজে বের করে চালু করুন" else "Find 'Purity Tracker' and enable it.",
                         android.widget.Toast.LENGTH_LONG
                     ).show()
                 }
             },
             onBatteryClick = {
                 try {
                     if (!AntiUninstallManager.isBatteryOptimizationIgnored(context)) {
                         AntiUninstallManager.requestIgnoreBatteryOptimization(context)
                     } else {
                         AntiUninstallManager.openOemAutoStartSettings(context)
                     }
                 } catch (e: Exception) {}
             }
         )
         Spacer(modifier = Modifier.height(90.dp))
    }
}

fun changeAppLocale(context: Context, localeTag: String) {
    LanguageStore.currentLanguage.value = localeTag
    context.getSharedPreferences("Settings", android.content.Context.MODE_PRIVATE).edit().putString("language", localeTag).apply()
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val localeManager = context.getSystemService(android.app.LocaleManager::class.java)
        localeManager?.applicationLocales = android.os.LocaleList.forLanguageTags(localeTag)
    } else {
        val locale = java.util.Locale(localeTag)
        java.util.Locale.setDefault(locale)
        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        if (context is android.app.Activity) {
            context.recreate()
        }
    }
}

@Composable
fun EmergencyRelapseDialog(
    onDismiss: () -> Unit,
    onNavigateToFocus: () -> Unit,
    onNavigateToAI: () -> Unit
) {
    val context = LocalContext.current
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value

    val quotesBn = remember {
        listOf(
            "কখনই ৫ সেকেন্ডের ক্ষণস্থায়ী মোহের জন্য আপনার এতদিনকার কষ্ট, আত্মমর্যাদা ও মানসিক শান্তি নষ্ট করবেন না!",
            "মনে রাখুন— আল্লাহ ধৈর্যশীলদের ভালোবাসেন। এই কঠিন মুহূর্তে নিজের আবেগ নিয়ন্ত্রণ করাই আসল বীরত্ব।",
            "প্রলোভনের তীব্রতা সাধারণত মাত্র ৫ থেকে ১৫ মিনিট স্থায়ী হয়। এখনই স্থান বা মনোযোগ পরিবর্তন করুন, ইনশাআল্লাহ কেটে যাবে!",
            "রিলেপ্সের পরের গ্লানি ও অপরাধবোধ বর্তমানের এই সাময়িক ত্যাগের চেয়ে ১০০০ গুণ বেশি কষ্টদায়ক।",
            "আপনার ভবিষ্যতের শক্তিশালী ও আত্মবিশ্বাসী রূপটি আজকের এই মুহূর্তের দুর্বলতার উপর জয়ী হতে চাচ্ছে। হাল ছাড়বেন না!",
            "কুরআন: 'আর যে ব্যক্তি তার প্রতিপালকের সামনে দাঁড়ানোকে ভয় করে এবং নিজেকে কুপ্রবৃত্তি থেকে বিরত রাখে, জান্নাতই হবে তার চূড়ান্ত ঠিকানা।' (সূরা নাযিআত: ৪০-৪১)"
        )
    }
    val quotesEn = remember {
        listOf(
            "Never trade your hard-earned streak, mental clarity and self-respect for 5 seconds of temporary illusion!",
            "Urge intensity peaks and drops in 5 to 15 minutes. Step away from your phone and environment right now!",
            "The regret after relapsing is 1000x heavier than the temporary discomfort of discipline.",
            "Allah is with the patient. Conquering this exact moment builds invincible self-mastery.",
            "Your future clean, proud self is begging you right now to stand strong and hold the line!",
            "Quran: 'And as for him who feared standing before his Lord and restrained his soul from impure desires, Paradise will be his refuge.' (79:40-41)"
        )
    }

    val isBn = LanguageStore.currentLanguage.value == "bn"
    val quotes = if (isBn) quotesBn else quotesEn
    var quoteIndex by remember { mutableStateOf(0) }

    val titleTextColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descTextColor = if (isLightMode) Color(0xFF475569) else Color(0xFF94A3B8)
    val cardBg = if (isLightMode) Color.White.copy(alpha = 0.96f) else Color(0xFF13141D).copy(alpha = 0.94f)
    val glassBorder = if (isLightMode) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.12f)
    val innerCardBg = if (isLightMode) Color(0xFFF8FAFC) else Color(0xFF1A1B28)

    // Grounding breathing timer (4-7-8 method)
    var breathPhase by remember { mutableStateOf("Inhale") }
    var countdown by remember { mutableStateOf(4) }

    LaunchedEffect(Unit) {
        while (isActive) {
            breathPhase = "Inhale"
            for (i in 4 downTo 1) { countdown = i; delay(1000) }
            breathPhase = "Hold"
            for (i in 7 downTo 1) { countdown = i; delay(1000) }
            breathPhase = "Exhale"
            for (i in 8 downTo 1) { countdown = i; delay(1000) }
        }
    }

    val breathLabel = when (breathPhase) {
        "Inhale" -> if (isBn) "গভীর শ্বাস নিন (Inhale)" else "Deep Inhale"
        "Hold" -> if (isBn) "ধরে রাখুন (Hold)" else "Hold Breath"
        else -> if (isBn) "ধীরে ধীরে ছাড়ুন (Exhale)" else "Slow Exhale"
    }

    val circleScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (breathPhase == "Inhale" || breathPhase == "Hold") 1.15f else 0.85f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = if (breathPhase == "Inhale") 4000 else if (breathPhase == "Hold") 7000 else 8000,
            easing = androidx.compose.animation.core.LinearEasing
        ),
        label = "breathScale"
    )

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 16.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .glassCard(cornerSize = 24.dp, glassAlpha = 0.96f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.2.dp, Color(0xFFFF3B30).copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(0xFFFF3B30).copy(alpha = 0.15f), CircleShape)
                                    .border(1.2.dp, Color(0xFFFF3B30).copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFFF3B30), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    if (isBn) "জরুরী রিলেপ্স রোধ (SOS)" else "Emergency Panic Rescue",
                                    color = titleTextColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    if (isBn) "প্রলোভন থেকে রক্ষা পান" else "Anti-Relapse Grounding",
                                    color = Color(0xFFEF4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = descTextColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Grounding Breathing Circle
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = innerCardBg),
                        border = BorderStroke(1.dp, glassBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                if (isBn) "শান্ত হোন ও দম নিন (4-7-8 Grounding)" else "Grounding Breath (4-7-8)",
                                color = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .scale(circleScale)
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            listOf(
                                                Color(0xFFFF3B30).copy(alpha = 0.35f),
                                                CyanAccent.copy(alpha = 0.2f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(2.dp, if (isLightMode) Color(0xFF0284C7) else CyanAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "$countdown",
                                        color = titleTextColor,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        breathLabel,
                                        color = descTextColor,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Motivation Quote
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isLightMode) Color(0xFF0284C7).copy(alpha = 0.08f) else Color(0xFF0284C7).copy(alpha = 0.15f),
                                RoundedCornerShape(18.dp)
                            )
                            .border(
                                1.dp,
                                if (isLightMode) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF0284C7).copy(alpha = 0.35f),
                                RoundedCornerShape(18.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (isBn) "💡 শক্তিশালী অনুপ্রেরণা" else "💡 Powerful Reminder",
                                    color = if (isLightMode) Color(0xFF0284C7) else Color(0xFF7DD3FC),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                                TextButton(
                                    onClick = { quoteIndex = (quoteIndex + 1) % quotes.size },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Refresh,
                                        contentDescription = null,
                                        tint = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        if (isBn) "অন্য উপদেশ" else "Next Quote",
                                        color = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                quotes[quoteIndex],
                                color = titleTextColor,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Toolkit
                    Text(
                        if (isBn) "⚡ ঝটপট করণীয় (Action Steps)" else "⚡ Action Steps",
                        color = titleTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Step 1: Cold water
                    EmergencyActionItem(
                        icon = Icons.Filled.WaterDrop,
                        title = if (isBn) "ঠান্ডা পানির ঝাপটা / অজু করুন" else "Cold Water Wash / Wudu",
                        desc = if (isBn) "মাথা ও মুখে ঠান্ডা পানি দিলে ডোপামিন রাশ শান্ত হয়" else "Resets sudden dopamine rushes immediately",
                        badge = if (isBn) "খুব কার্যকর" else "Top Pick",
                        onClick = {
                            android.widget.Toast.makeText(
                                context,
                                if (isBn) "ঠান্ডা পানি দিয়ে মুখ ধুয়ে আসুন এবং স্ক্রিন থেকে দূরে থাকুন! 💧" else "Wash with cold water and step away from the screen! 💧",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Step 2: 10 Pushups
                    EmergencyActionItem(
                        icon = Icons.Filled.FitnessCenter,
                        title = if (isBn) "১০টি পুশ-আপ / শারীরিক ব্যায়াম" else "10 Pushups / Physical Shock",
                        desc = if (isBn) "শরীরে নতুন শক্তি সঞ্চার করুন ও স্ক্রিন ছাড়ুন" else "Channel excess adrenaline into clean physical exercise",
                        badge = "10 Pushups",
                        onClick = {
                            android.widget.Toast.makeText(
                                context,
                                if (isBn) "এখনই ১০টি পুশ-আপ দিন এবং স্ক্রিন থেকে উঠে দাঁড়ান! 💪" else "Do 10 pushups right now! Move away from the screen! 💪",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Step 3: Focus & Meditation
                    EmergencyActionItem(
                        icon = Icons.Filled.Spa,
                        title = if (isBn) "মেডিটেশন ও রিল্যাক্স সাউন্ড" else "Meditation & Ambient Sound",
                        desc = if (isBn) "শান্ত সাউন্ড ও শ্বাস-প্রশ্বাসের ব্যায়াম করুন" else "Listen to calming rain/ocean and deep breathe",
                        badge = if (isBn) "ফোকাস" else "Focus Mode",
                        onClick = {
                            onDismiss()
                            onNavigateToFocus()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Step 4: AI Pure Guard
                    EmergencyActionItem(
                        icon = Icons.Filled.Psychology,
                        title = if (isBn) "AI গাইডের তাত্ক্ষণিক পরামর্শ" else "Chat with AI Pure Guard",
                        desc = if (isBn) "তাত্ক্ষণিক কাউন্সেলিং ও মানসিক সাপোর্ট নিন" else "Get immediate guidance & anti-relapse counseling",
                        badge = "AI Guard",
                        onClick = {
                            onDismiss()
                            onNavigateToAI()
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Victory Button
                    Button(
                        onClick = {
                            android.widget.Toast.makeText(
                                context,
                                if (isBn) "আলহামদুলিল্লাহ! আপনি একটি কঠিন প্রলোভন সফলভাবে জয় করেছেন! 🏆🔥" else "Alhamdulillah! You conquered this intense urge like a warrior! 🏆🔥",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Filled.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isBn) "আমি প্রলোভন জয় করেছি! 💪" else "I Conquered This Urge! 💪",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmergencyActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    badge: String,
    onClick: () -> Unit
) {
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descColor = if (isLightMode) Color(0xFF475569) else Color(0xFF94A3B8)
    val cardBg = if (isLightMode) Color.White else Color(0xFF1A1B28)
    val cardBorder = if (isLightMode) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.10f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .glassCard(cornerSize = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        (if (isLightMode) Color(0xFF0284C7) else CyanAccent).copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = titleColor, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                Text(desc, color = descColor, fontSize = 10.5.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .background(
                        (if (isLightMode) Color(0xFF0284C7) else CyanAccent).copy(alpha = 0.12f),
                        CircleShape
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    badge,
                    color = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LiveStreakTimerRow(
    viewModel: PurityTrackerViewModel,
    isBn: Boolean
) {
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val days = (durationMs / 86_400_000L).toInt()
    val hours = (durationMs / 3_600_000L) % 24
    val mins = (durationMs / 60_000L) % 60
    val secs = (durationMs / 1_000L) % 60

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Text(
                text = days.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = if (isBn) "দিন" else "days",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.65f)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Text(
                text = String.format(java.util.Locale.US, "%02d", hours),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = if (isBn) "ঘণ্টা" else "hrs",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.65f)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Text(
                text = String.format(java.util.Locale.US, "%02d", mins),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = if (isBn) "মিনিট" else "min",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.65f)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Text(
                text = String.format(java.util.Locale.US, "%02d", secs),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )
            Text(
                text = if (isBn) "সেকেন্ড" else "sec",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
fun HomeScreen(
    viewModel: PurityTrackerViewModel = viewModel(),
    badgesViewModel: BadgesViewModel = viewModel(),
    communityViewModel: CommunityViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    salahViewModel: SalahViewModel = viewModel(),
    onNavigateToPremium: (() -> Unit)? = null,
    onNavigateToFocus: (() -> Unit)? = null,
    onNavigateToAI: (() -> Unit)? = null,
    onNavigateToWinterArc: (() -> Unit)? = null,
    onNavigateToSalah: (() -> Unit)? = null,
    onNavigateToBlocker: (() -> Unit)? = null
) {
    val days by viewModel.durationDays.collectAsStateWithLifecycle()
    val dynamicBadges by badgesViewModel.badges.collectAsStateWithLifecycle()
    val bestRecord by viewModel.bestRecord.collectAsStateWithLifecycle()
    
    val recordDays = maxOf(days, bestRecord?.durationDays ?: 0)
    val currentBadge = dynamicBadges.lastOrNull { days >= it.requiredDays } ?: dynamicBadges.firstOrNull() ?: CustomBadge("", "None", 0, "None")

    // Sync streak only when days or badge changes
    LaunchedEffect(days, currentBadge.name, currentBadge.imageUrl) {
        val currentStreakMs = (System.currentTimeMillis() - viewModel.startTime.value).coerceAtLeast(0L)
        communityViewModel.syncUserStreak(currentStreakMs, currentBadge.name, currentBadge.imageUrl)
    }
    
    var showBadgesMenu by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }

    if (showEmergencyDialog) {
        EmergencyRelapseDialog(
            onDismiss = { showEmergencyDialog = false },
            onNavigateToFocus = {
                showEmergencyDialog = false
                onNavigateToFocus?.invoke()
            },
            onNavigateToAI = {
                showEmergencyDialog = false
                onNavigateToAI?.invoke()
            }
        )
    }

    val nextBadge = dynamicBadges.firstOrNull { days < it.requiredDays }

    val nextDaysReq = nextBadge?.requiredDays ?: 365
    val currDaysReq = currentBadge.requiredDays
    val range = (nextDaysReq - currDaysReq).toFloat()
    val progress = if (range > 0 && nextBadge != null) {
        (days - currDaysReq) / range
    } else 1f

    if (showBadgesMenu) {
        BadgesScreen(days, badgesViewModel, onClose = { showBadgesMenu = false })
        return
    }

    if (showResetDialog) {
        ResetDialog(
            onDismiss = { showResetDialog = false },
            onOldDate = {
                showResetDialog = false
                showDatePicker = true
            },
            onRightNow = {
                showResetDialog = false
                viewModel.resetTimer(dynamicBadges)
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialogMock(
            onDismiss = { showDatePicker = false },
            onDateSelected = { y, m, d ->
                viewModel.resetTimerToDate(y, m, d, dynamicBadges)
                showDatePicker = false
            }
        )
    }

    val scrollState = rememberScrollState()
    val isPremiumUser by PremiumManager.isPremium.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    var showPremiumStatusDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current
        val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
        val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White

        // Security Defense States (evaluated off the UI thread for zero startup lag)
        val safeContext = remember(context) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                context.createDeviceProtectedStorageContext()
            } else {
                context
            }
        }
        val vpnPrefs = remember(safeContext) { safeContext.getSharedPreferences("VpnPrefs", Context.MODE_PRIVATE) }
        var hasPin by remember { mutableStateOf(LockManager.hasPin(context)) }
        var isBlockerActive by remember { mutableStateOf(BlockerManager.isAdultBlockerEnabled(context)) }
        var vpnServiceRunning by remember { mutableStateOf(false) }
        var isAccessibilityEnabled by remember { mutableStateOf(false) }
        var isAccessibilityServiceRunning by remember { mutableStateOf(false) }
        var isAccessibilityDeadButEnabled by remember { mutableStateOf(false) }
        var isDeviceAdminEnabled by remember { mutableStateOf(false) }
        var isBatteryExempt by remember { mutableStateOf(AntiUninstallManager.isBatteryOptimizationIgnored(context)) }
        var isOemAutoStartVisited by remember { mutableStateOf(AntiUninstallManager.isOemAutoStartVisited(context)) }
        var isRecentsLockConfirmed by remember { mutableStateOf(AntiUninstallManager.isRecentsLockConfirmed(context)) }
        // Show explanatory dialog on EVERY app start when battery optimization is NOT ignored (never gated by a one-time flag)
        var showBatteryExemptionDialog by remember {
            mutableStateOf(!AntiUninstallManager.isBatteryOptimizationIgnored(context))
        }
        var showPinDialog by remember { mutableStateOf(false) }
        var showPinLockedInfoDialog by remember { mutableStateOf(false) }
        var showGuardianKeyCreatedDialog by remember { mutableStateOf(false) }
        var showGuardianResetDialog by remember { mutableStateOf(false) }
        var activeGuardianKey by remember { mutableStateOf(if (hasPin) LockManager.getGuardianKey(context) else "") }
        var resetGuardianKeyInput by remember { mutableStateOf("") }
        var resetNewPinInput by remember { mutableStateOf("") }
        var resetConfirmPinInput by remember { mutableStateOf("") }
        var resetErrorMessage by remember { mutableStateOf<String?>(null) }
        var tempPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        val scope = rememberCoroutineScope()

        DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                    scope.launch(Dispatchers.IO) {
                        val pin = LockManager.hasPin(context)
                        val blocker = BlockerManager.isAdultBlockerEnabled(context)
                        val gKey = if (pin) LockManager.getGuardianKey(context) else ""
                        val accAlive = AntiUninstallManager.isAccessibilityServiceAlive(context)
                        val accDead = AntiUninstallManager.isAccessibilityDeadButEnabled(context)
                        val devAdmin = AntiUninstallManager.isDeviceAdminActive(context)
                        val batExempt = AntiUninstallManager.isBatteryOptimizationIgnored(context)
                        val oemVisited = AntiUninstallManager.isOemAutoStartVisited(context)
                        val recentsLocked = AntiUninstallManager.isRecentsLockConfirmed(context)
                        GuardHealthWorker.checkNowAndNotify(context)
                        withContext(Dispatchers.Main) {
                            hasPin = pin
                            isBlockerActive = blocker
                            if (pin) activeGuardianKey = gKey
                            isAccessibilityEnabled = accAlive
                            isAccessibilityServiceRunning = accAlive
                            isAccessibilityDeadButEnabled = accDead
                            isDeviceAdminEnabled = devAdmin
                            isBatteryExempt = batExempt
                            isOemAutoStartVisited = oemVisited
                            isRecentsLockConfirmed = recentsLocked
                            if (batExempt) {
                                showBatteryExemptionDialog = false
                            }
                        }
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        // Defence Alert visibility: disappears if PIN is set and (Anti-Uninstall or VPN is enabled)
        val isProtected = hasPin && (isAccessibilityEnabled || isDeviceAdminEnabled || vpnServiceRunning)

        val isBn = LanguageStore.currentLanguage.value == "bn"
        val cMail = currentUser?.email ?: ""
        val isCurrentPremium = isPremiumUser || cMail == "muhaiminulhasananas@gmail.com"
        val uName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: (if (isBn) "যোদ্ধা" else "Warrior")

        // Top Greeting Header (Matching Reference Screenshot 1: "Assalamu alaikum" / Name + 2 Rounded Glass Icon Buttons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBn) "আসসালামু আলাইকুম" else "Assalamu alaikum",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PremiumNameText(
                        name = uName,
                        isPremium = isCurrentPremium,
                        defaultColor = titleColor,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isCurrentPremium || cMail == "muhaiminulhasananas@gmail.com") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.clickable { showPremiumStatusDialog = true }) {
                            ProBlueBadge(size = 18.dp)
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sparkle AI Noor Button
                Surface(
                    onClick = { onNavigateToAI?.invoke() },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF38BDF8).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.38f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Noor AI",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                // Notification / Pro Status Button
                Surface(
                    onClick = {
                        if (isPremiumUser) showPremiumStatusDialog = true
                        else onNavigateToPremium?.invoke()
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFBBF24).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.36f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Slim, Flat Compact "Winter Arc" Bar (Only "Winter Arc" text, takes minimal vertical space)
        Surface(
            onClick = { onNavigateToWinterArc?.invoke() },
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0C2538).copy(alpha = 0.65f),
            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.38f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AcUnit,
                        contentDescription = "Winter Arc",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Winter Arc",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main CURRENT BADGE Hero Card (Auto-cropped full circle badge fill)
        val unlockedBadgesCount = dynamicBadges.count { days >= it.requiredDays }.coerceAtLeast(1)
        val totalBadgesCount = dynamicBadges.size.coerceAtLeast(10)
        val currentIdx = dynamicBadges.indexOfFirst { it.id == currentBadge.id }.let { if (it == -1) 0 else it }
        val leftPreviewBadge = if (currentIdx == 0) {
            dynamicBadges.getOrNull(1) ?: CustomBadge("1", "Novice", 3, "")
        } else {
            dynamicBadges.getOrNull(currentIdx - 1) ?: CustomBadge("0", "Clown", 0, "")
        }
        val rightPreviewBadge = if (currentIdx == 0) {
            dynamicBadges.getOrNull(2) ?: CustomBadge("2", "New Warrior", 7, "")
        } else {
            nextBadge ?: dynamicBadges.lastOrNull() ?: CustomBadge("9", "Giga Chad", 365, "")
        }
        var showMoreMenu by remember { mutableStateOf(false) }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerSize = 24.dp, borderWidth = 1.dp, glassAlpha = 0.96f, hasSheen = true),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row: CURRENT BADGE on left + "1 / 10 unlocked" on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBn) "বর্তমান ব্যাজ" else "CURRENT BADGE",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )

                    Text(
                        text = if (isBn) "$unlockedBadgesCount / $totalBadgesCount আনলকড" else "$unlockedBadgesCount / $totalBadgesCount unlocked",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.65f),
                        modifier = Modifier.clickable { showBadgesMenu = true }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Center 3-Badge Circular Grouping (Left Preview Circle + Center Golden Active Circle + Right Preview Circle)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Left Side Circular Badge Preview
                    val isLeftUnlocked = days >= leftPreviewBadge.requiredDays
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showBadgesMenu = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                .border(
                                    1.dp,
                                    if (isLeftUnlocked) Color.White.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.16f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLeftUnlocked && leftPreviewBadge.imageUrl.isNotEmpty()) {
                                BadgeImage(
                                    imageUrl = leftPreviewBadge.imageUrl,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .graphicsLayer(scaleX = 1.14f, scaleY = 1.14f),
                                    fallbackName = leftPreviewBadge.name,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = if (isLeftUnlocked) Icons.Filled.EmojiEvents else Icons.Outlined.Lock,
                                    contentDescription = leftPreviewBadge.name,
                                    tint = if (isLeftUnlocked) Color(0xFFFBBF24) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = getCustomBadgeName(leftPreviewBadge),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCBD5E1),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${leftPreviewBadge.requiredDays}d",
                            fontSize = 10.5.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Center Golden Glowing Active Badge Circle with Smooth Breathing, Floating & Rotating Aura Animation
                    val badgeInfiniteTransition = rememberInfiniteTransition(label = "hero_badge_anim")
                    val badgePulseScale by badgeInfiniteTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = 1.055f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 2000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "badge_pulse_scale"
                    )
                    val badgeFloatOffsetY by badgeInfiniteTransition.animateFloat(
                        initialValue = -2.5f,
                        targetValue = 2.5f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 2200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "badge_float_y"
                    )
                    val haloGlowAlpha by badgeInfiniteTransition.animateFloat(
                        initialValue = 0.22f,
                        targetValue = 0.52f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "halo_glow_alpha"
                    )
                    val ringRotationDeg by badgeInfiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 5500, easing = androidx.compose.animation.core.LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "ring_rotation_deg"
                    )

                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .offset(y = badgeFloatOffsetY.dp)
                            .clickable { showBadgesMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        // Soft ambient pulsing golden halo
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .graphicsLayer(
                                    scaleX = badgePulseScale * 1.04f,
                                    scaleY = badgePulseScale * 1.04f
                                )
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFF59E0B).copy(alpha = haloGlowAlpha),
                                            Color(0xFFFBBF24).copy(alpha = haloGlowAlpha * 0.35f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                        // Rotating luminous golden sweep ring
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier
                                .size(102.dp)
                                .graphicsLayer(rotationZ = ringRotationDeg)
                        ) {
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFFFBBF24).copy(alpha = 0.25f),
                                        Color(0xFFFDE047),
                                        Color(0xFFF59E0B),
                                        Color.Transparent
                                    )
                                ),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                            )
                        }
                        // Outer progress arc ring
                        CircularProgressIndicator(
                            progress = { progress.coerceIn(0.08f, 1f) },
                            modifier = Modifier.size(102.dp),
                            color = Color(0xFFFBBF24),
                            trackColor = Color.White.copy(alpha = 0.10f),
                            strokeWidth = 3.5.dp,
                            strokeCap = StrokeCap.Round
                        )
                        // Inner Round Circle filled 100% by the auto-cropped badge image with breathing pulse
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .graphicsLayer(
                                    scaleX = badgePulseScale,
                                    scaleY = badgePulseScale
                                )
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFDE047),
                                            Color(0xFFF59E0B),
                                            Color(0xFFD97706)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            BadgeImage(
                                imageUrl = currentBadge.imageUrl,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .graphicsLayer(scaleX = 1.14f, scaleY = 1.14f),
                                fallbackName = currentBadge.name,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(2.dp, Color(0xFFFBBF24), CircleShape)
                            )
                        }
                    }

                    // Right Side Circular Badge Preview
                    val isRightUnlocked = days >= rightPreviewBadge.requiredDays
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showBadgesMenu = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                .border(
                                    1.dp,
                                    if (isRightUnlocked) Color.White.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.16f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isRightUnlocked && rightPreviewBadge.imageUrl.isNotEmpty()) {
                                BadgeImage(
                                    imageUrl = rightPreviewBadge.imageUrl,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .graphicsLayer(scaleX = 1.14f, scaleY = 1.14f),
                                    fallbackName = rightPreviewBadge.name,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = if (isRightUnlocked) Icons.Filled.EmojiEvents else Icons.Outlined.Lock,
                                    contentDescription = rightPreviewBadge.name,
                                    tint = if (isRightUnlocked) Color.White else Color.White.copy(alpha = 0.62f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = getCustomBadgeName(rightPreviewBadge),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.88f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${rightPreviewBadge.requiredDays}d",
                            fontSize = 10.5.sp,
                            color = Color.White.copy(alpha = 0.60f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Active Badge Name Centered
                Text(
                    text = getCustomBadgeName(currentBadge),
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Linear Progress Bar & Next Badge Labels
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0.06f, 1f))
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF38BDF8), Color(0xFFFBBF24))
                                ),
                                RoundedCornerShape(3.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val nextBadgeDisplay = nextBadge?.let { getCustomBadgeName(it) } ?: "Max"
                    val daysRemaining = maxOf(0, nextDaysReq - days)
                    Text(
                        text = if (isBn) "পরবর্তী ব্যাজ: $nextBadgeDisplay" else "Next badge: $nextBadgeDisplay",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Text(
                        text = if (isBn) "$daysRemaining দিন বাকি" else "$daysRemaining days left",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Unified 4-Column Glass Timer Container (Isolated 1-second recomposition scope for 120Hz scrolling)
                LiveStreakTimerRow(viewModel = viewModel, isBn = isBn)

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Row: [ ⚠ Emergency SOS ] + [ ⋮ More / Reset Button ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showEmergencyDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(
                            Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "জরুরী SOS" else "Emergency SOS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    Box {
                        Surface(
                            onClick = { showMoreMenu = true },
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "More options",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            modifier = Modifier.background(Color(0xFF121212))
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (isBn) "স্ট্রিক রিসেট করুন" else "Reset Streak",
                                        color = Color(0xFFF87171),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color(0xFFF87171))
                                },
                                onClick = {
                                    showMoreMenu = false
                                    showResetDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (isBn) "সব ব্যাজ দেখুন" else "View All Badges",
                                        color = Color.White
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = Color(0xFFFBBF24))
                                },
                                onClick = {
                                    showMoreMenu = false
                                    showBadgesMenu = true
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Next Prayer Compact Glass Bar (Matching Reference Screenshot 1: "Next: Dhuhr / Today's Salah 0/5" + Live Countdown)
        val todaySalahsHome by salahViewModel.todaySalahs.collectAsState()
        val salahTimingsHome by salahViewModel.todayTimings.collectAsState()
        val completedSalahsCount = todaySalahsHome.count { it }
        var nextPrayerTitleHome by remember { mutableStateOf("Dhuhr") }
        var nextPrayerCountdownHome by remember { mutableStateOf("00:00:00") }

        LaunchedEffect(salahTimingsHome) {
            val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.US)
            while (true) {
                if (salahTimingsHome.isNotEmpty()) {
                    val now = java.util.Calendar.getInstance()
                    val nowStr = String.format(
                        java.util.Locale.US,
                        "%02d:%02d",
                        now.get(java.util.Calendar.HOUR_OF_DAY),
                        now.get(java.util.Calendar.MINUTE)
                    )
                    val order = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
                    var foundNext = false
                    for (p in order) {
                        val tStr = salahTimingsHome[p]?.replace(Regex("\\s\\(.*\\)"), "")
                        if (tStr != null && nowStr < tStr) {
                            val d = try { sdf.parse(tStr) } catch (e: Exception) { null }
                            if (d != null) {
                                val pCal = java.util.Calendar.getInstance()
                                pCal.time = d
                                pCal.set(java.util.Calendar.YEAR, now.get(java.util.Calendar.YEAR))
                                pCal.set(java.util.Calendar.MONTH, now.get(java.util.Calendar.MONTH))
                                pCal.set(java.util.Calendar.DAY_OF_YEAR, now.get(java.util.Calendar.DAY_OF_YEAR))
                                val diff = (pCal.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
                                val h = diff / (1000 * 60 * 60)
                                val m = (diff / (1000 * 60)) % 60
                                val s = (diff / 1000) % 60
                                nextPrayerCountdownHome = String.format(java.util.Locale.US, "%02d:%02d:%02d", h, m, s)
                                nextPrayerTitleHome = p
                                foundNext = true
                                break
                            }
                        }
                    }
                    if (!foundNext) {
                        val tStr = salahTimingsHome["Fajr"]?.replace(Regex("\\s\\(.*\\)"), "")
                        val d = if (tStr != null) try { sdf.parse(tStr) } catch (e: Exception) { null } else null
                        if (d != null) {
                            val pCal = java.util.Calendar.getInstance()
                            pCal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                            val c2 = java.util.Calendar.getInstance().apply { time = d }
                            pCal.set(java.util.Calendar.HOUR_OF_DAY, c2.get(java.util.Calendar.HOUR_OF_DAY))
                            pCal.set(java.util.Calendar.MINUTE, c2.get(java.util.Calendar.MINUTE))
                            pCal.set(java.util.Calendar.SECOND, 0)
                            val diff = (pCal.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
                            val h = diff / (1000 * 60 * 60)
                            val m = (diff / (1000 * 60)) % 60
                            val s = (diff / 1000) % 60
                            nextPrayerCountdownHome = String.format(java.util.Locale.US, "%02d:%02d:%02d", h, m, s)
                            nextPrayerTitleHome = "Fajr"
                        }
                    }
                }
                kotlinx.coroutines.delay(1000)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerSize = 18.dp, borderWidth = 1.dp, glassAlpha = 0.95f, hasSheen = false)
                .clickable { onNavigateToSalah?.invoke() },
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF0C2D3E), CircleShape)
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.42f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Bedtime,
                            contentDescription = "Next Prayer",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isBn) "পরবর্তী: $nextPrayerTitleHome" else "Next: $nextPrayerTitleHome",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isBn) "আজকের সালাত $completedSalahsCount/5" else "Today's Salah $completedSalahsCount/5",
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }
                }

                Text(
                    text = nextPrayerCountdownHome,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8),
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // DNS Card
        var dnsEnabled by remember { mutableStateOf(false) }
        var showDnsInstructions by remember { mutableStateOf(false) }

        if (showDnsInstructions) {
            AlertDialog(
                onDismissRequest = { showDnsInstructions = false },
                title = { Text(t("dns_dialog_title"), fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Column {
                        Text(t("dns_dialog_desc"), color = Color.White)
                        Spacer(modifier=Modifier.height(8.dp))
                        Text(t("dns_step1"), color = Color.LightGray)
                        Text(t("dns_step2"), color = Color.LightGray)
                        Text(t("dns_step3"), color = Color.LightGray)
                        Text(t("dns_step4"), color = Color.LightGray)
                        Spacer(modifier=Modifier.height(8.dp))
                        Text(t("dns_alternative"), color = Color.LightGray, fontSize = 12.sp)
                    }
                },
                containerColor = CardBlue,
                confirmButton = {
                    Button(onClick = {
                        showDnsInstructions = false
                        dnsEnabled = true
                        ProtectionService.grantInitialDnsSetupGraceWindow()
                        try {
                            context.startActivity(android.content.Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS).apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) })
                        } catch (e: Exception) {
                            try {
                                context.startActivity(android.content.Intent(android.provider.Settings.ACTION_SETTINGS).apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) })
                            } catch (e2: Exception) { }
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)) {
                        Text(t("go_to_settings"), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDnsInstructions = false }) {
                        Text(t("dismiss"), color = Color.LightGray)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pin Lock Setting

        val subtextColor = if (isLightMode) Color(0xFF475569) else Color(0xFFCBD5E1)

        var partnerEmailHome by remember { mutableStateOf(BlockerManager.getPartnerEmail(context)) }
        var showHomeTurnOffDialog by remember { mutableStateOf(false) }
        var isSendingHomeTurnOff by remember { mutableStateOf(false) }
        var showHomeChangePartnerDialog by remember { mutableStateOf(false) }
        var isSendingHomePartnerChange by remember { mutableStateOf(false) }
        var showHomeSetPartnerDialog by remember { mutableStateOf(false) }
        var homePartnerEmailInput by remember { mutableStateOf("") }
        var isSendingHomePinRequest by remember { mutableStateOf(false) }

        // Persistent continuous UI-thread guard loop (v1.3.6: partner-only PIN)
        LaunchedEffect(Unit) {
            while (true) {
                try {
                    SettingsGuardWatchdogService.startIfInForeground(context)
                } catch (e: Exception) {}
                hasPin = LockManager.hasPin(context)
                partnerEmailHome = BlockerManager.getPartnerEmail(context)
                isBlockerActive = BlockerManager.isAdultBlockerEnabled(context)
                val aliveNow = AntiUninstallManager.isAccessibilityServiceAlive(context)
                val deadNow = AntiUninstallManager.isAccessibilityDeadButEnabled(context)
                isAccessibilityEnabled = aliveNow
                isAccessibilityServiceRunning = aliveNow
                isAccessibilityDeadButEnabled = deadNow
                isDeviceAdminEnabled = AntiUninstallManager.isDeviceAdminActive(context)
                isBatteryExempt = AntiUninstallManager.isBatteryOptimizationIgnored(context)
                isOemAutoStartVisited = AntiUninstallManager.isOemAutoStartVisited(context)
                isRecentsLockConfirmed = AntiUninstallManager.isRecentsLockConfirmed(context)
                if (isBatteryExempt) {
                    showBatteryExemptionDialog = false
                }
                BlockerManager.ensurePartnerApprovalListener(context) { status ->
                    if (status == "approved") {
                        isBlockerActive = false
                    }
                }
                BlockerManager.ensurePartnerChangeApprovalListener(context) { status ->
                    if (status == "approved") {
                        partnerEmailHome = ""
                    }
                }
                BlockerManager.ensurePinResetApprovalListener(context) { status ->
                    if (status == "approved") {
                        BlockerManager.consumePartnerPinResetNotice(context)
                        hasPin = LockManager.hasPin(context)
                        android.widget.Toast.makeText(
                            context,
                            "Your PIN was reset by your partner",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
                kotlinx.coroutines.delay(1000) // check every 1 second
            }
        }

        // Explanatory Dialog on every app start when battery optimization is NOT ignored
        if (showBatteryExemptionDialog && !isBatteryExempt) {
            AlertDialog(
                onDismissRequest = { showBatteryExemptionDialog = false },
                shape = RoundedCornerShape(22.dp),
                containerColor = Color(0xFF0F172A),
                icon = {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFFF59E0B).copy(alpha = 0.16f), CircleShape)
                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PowerSettingsNew,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = if (isBn) "ব্যাটারি অপটিমাইজেশন ছাড় আবশ্যক ⚡" else "Battery Exemption Required ⚡",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isBn)
                                "OPPO (ColorOS), Realme, Xiaomi ও Vivo ফোনে ব্যাটারি অপটিমাইজেশন চালু থাকলে ১ দিনের মধ্যে সিস্টেম সেটিংস গার্ড বন্ধ করে দেয়—যদিও সেটিংসে এটি ON দেখায়।"
                            else
                                "Without battery-optimization exemption, phones like OPPO (ColorOS), Realme, and Xiaomi kill Settings Guard after ~1 day even while Settings still shows it ON.",
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Text(
                            text = if (isBn)
                                "স্থায়ী ২৪/৭ সুরক্ষার জন্য নিচের বাটনে ট্যাপ করে 'Allow' / 'Unrestricted' অনুমতি দিন।"
                            else
                                "Tap below and select 'Allow' / 'Don't optimize' so Settings Guard stays alive 24/7.",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 17.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            AntiUninstallManager.requestIgnoreBatteryOptimization(context)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isBn) "ব্যাটারি ছাড় চালু করুন" else "Grant Battery Exemption",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBatteryExemptionDialog = false }) {
                        Text(
                            text = if (isBn) "পরে" else "Later",
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            )
        }

        if (showHomeTurnOffDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!isSendingHomeTurnOff) showHomeTurnOffDialog = false
                },
                containerColor = Color(0xFF0F172A),
                titleContentColor = Color.White,
                textContentColor = Color(0xFFCBD5E1),
                title = {
                    Text(
                        text = "Turn OFF - Block adult content",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Text(
                        text = "A request will be sent to your accountability partner ($partnerEmailHome).",
                        fontSize = 13.5.sp,
                        color = Color.White
                    )
                },
                dismissButton = {
                    TextButton(
                        onClick = { showHomeTurnOffDialog = false },
                        enabled = !isSendingHomeTurnOff
                    ) {
                        Text("Cancel", color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isSendingHomeTurnOff = true
                            BlockerManager.sendDisableRequestToPartner(context) { _, message ->
                                isSendingHomeTurnOff = false
                                showHomeTurnOffDialog = false
                                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                            }
                        },
                        enabled = !isSendingHomeTurnOff,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Send Request", color = Color(0xFF061018), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (showHomeChangePartnerDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!isSendingHomePartnerChange) showHomeChangePartnerDialog = false
                },
                containerColor = Color(0xFF0F172A),
                titleContentColor = Color.White,
                textContentColor = Color(0xFFCBD5E1),
                title = {
                    Text(
                        text = "Change Accountability Partner",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Text(
                        text = "Send an approval request to your current partner ($partnerEmailHome) to unlock changing the email.",
                        fontSize = 13.5.sp,
                        color = Color.White
                    )
                },
                dismissButton = {
                    TextButton(
                        onClick = { showHomeChangePartnerDialog = false },
                        enabled = !isSendingHomePartnerChange
                    ) {
                        Text("Cancel", color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isSendingHomePartnerChange = true
                            BlockerManager.sendPartnerChangeRequestToCurrentPartner(context) { _, message ->
                                isSendingHomePartnerChange = false
                                showHomeChangePartnerDialog = false
                                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                            }
                        },
                        enabled = !isSendingHomePartnerChange,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Send Request", color = Color(0xFF061018), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (showHomeSetPartnerDialog) {
            AlertDialog(
                onDismissRequest = { showHomeSetPartnerDialog = false },
                containerColor = Color(0xFF0F172A),
                titleContentColor = Color.White,
                textContentColor = Color(0xFFCBD5E1),
                title = {
                    Text(
                        text = "Set Accountability Partner Email",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                text = {
                    OutlinedTextField(
                        value = homePartnerEmailInput,
                        onValueChange = { homePartnerEmailInput = it },
                        placeholder = { Text("partner@example.com", fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.24f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                },
                dismissButton = {
                    TextButton(onClick = { showHomeSetPartnerDialog = false }) {
                        Text("Cancel", color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val err = BlockerManager.validateAndSavePartnerEmail(context, homePartnerEmailInput)
                            if (err != null) {
                                android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                            } else {
                                partnerEmailHome = BlockerManager.getPartnerEmail(context)
                                homePartnerEmailInput = ""
                                showHomeSetPartnerDialog = false
                                android.widget.Toast.makeText(
                                    context,
                                    "Accountability partner email saved and locked.",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save", color = Color(0xFF061018), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        val isGuardConnected = isAccessibilityEnabled && isAccessibilityServiceRunning
        val isBatteryIgnored = isBatteryExempt
        val isPartnerLockedHome = partnerEmailHome.isNotBlank() && BlockerManager.hasValidPartnerEmail(context)

        // Single Daily Controls Card (4 compact rows: 1. App PIN, 2. Accessibility, 3. Accountability Partner Email, 4. Block adult content + "See full Blocker page →")
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerSize = 18.dp, borderWidth = 1.dp, glassAlpha = 0.95f, hasSheen = false),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                // Row 1: App PIN (badge "Set by partner" green / "Not set yet" amber) — sends partner PIN email via EmailJS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isSendingHomePinRequest) {
                            if (!isPartnerLockedHome) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Save your Accountability Partner email first",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                showHomeSetPartnerDialog = true
                            } else {
                                isSendingHomePinRequest = true
                                BlockerManager.sendPinResetRequestToPartner(context) { ok, message ->
                                    isSendingHomePinRequest = false
                                    if (ok) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "PIN request sent to your partner",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        android.widget.Toast.makeText(
                                            context,
                                            message,
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    (if (hasPin) Color(0xFF10B981) else Color(0xFFF59E0B)).copy(alpha = 0.15f),
                                    RoundedCornerShape(9.dp)
                                )
                                .border(
                                    1.dp,
                                    (if (hasPin) Color(0xFF10B981) else Color(0xFFF59E0B)).copy(alpha = 0.4f),
                                    RoundedCornerShape(9.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Key,
                                contentDescription = null,
                                tint = if (hasPin) Color(0xFF34D399) else Color(0xFFFBBF24),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "App PIN",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleColor
                            )
                            Text(
                                text = when {
                                    isSendingHomePinRequest -> "Sending request to partner..."
                                    hasPin -> "Tap to request PIN reset"
                                    else -> "Tap to request partner PIN"
                                },
                                fontSize = 11.sp,
                                color = subtextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    val pinChipColor = if (hasPin) Color(0xFF10B981) else Color(0xFFF59E0B)
                    Box(
                        modifier = Modifier
                            .background(pinChipColor.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
                            .border(1.dp, pinChipColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (hasPin) "Set by partner" else "Not set yet",
                            color = if (hasPin) Color(0xFF34D399) else Color(0xFFFBBF24),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // Row 2: Accessibility (chip "Alive" / toggle) — works IN PLACE on Home
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isGuardConnected) {
                                AntiUninstallManager.requestAccessibilityService(context)
                            } else {
                                android.widget.Toast.makeText(
                                    context,
                                    "Accessibility guard is alive and active",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF2DD4BF).copy(alpha = 0.15f), RoundedCornerShape(9.dp))
                                .border(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.4f), RoundedCornerShape(9.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = null,
                                tint = Color(0xFF2DD4BF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Accessibility",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleColor
                            )
                            Text(
                                text = when {
                                    isGuardConnected -> "Guards browsers and settings"
                                    isAccessibilityDeadButEnabled -> "Stopped — tap to restart"
                                    else -> "Tap to enable service"
                                },
                                fontSize = 11.sp,
                                color = if (isAccessibilityDeadButEnabled) Color(0xFFF87171) else subtextColor
                            )
                        }
                    }

                    val accChipColor = if (isGuardConnected) Color(0xFF10B981) else Color(0xFFEF4444)
                    Box(
                        modifier = Modifier
                            .background(accChipColor.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
                            .border(1.dp, accChipColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isGuardConnected) "Alive" else if (isAccessibilityDeadButEnabled) "Stopped" else "Turn ON",
                            color = if (isGuardConnected) Color(0xFF34D399) else Color(0xFFFCA5A5),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // Row 3: Accountability Partner Email (partner email as subtitle, chip "Locked") — works IN PLACE on Home
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isPartnerLockedHome) {
                                showHomeChangePartnerDialog = true
                            } else {
                                showHomeSetPartnerDialog = true
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF38BDF8).copy(alpha = 0.15f), RoundedCornerShape(9.dp))
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(9.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Accountability Partner Email",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleColor
                            )
                            Text(
                                text = if (isPartnerLockedHome) partnerEmailHome else "Tap to set email",
                                fontSize = 11.sp,
                                color = subtextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    val partnerChipColor = if (isPartnerLockedHome) Color(0xFF10B981) else Color(0xFFF59E0B)
                    Box(
                        modifier = Modifier
                            .background(partnerChipColor.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
                            .border(1.dp, partnerChipColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isPartnerLockedHome) "Locked" else "Set Email",
                            color = if (isPartnerLockedHome) Color(0xFF34D399) else Color(0xFFFBBF24),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // Row 4: Block adult content (toggle) — works IN PLACE on Home
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    (if (isBlockerActive) Color(0xFF10B981) else Color(0xFFEF4444)).copy(alpha = 0.15f),
                                    RoundedCornerShape(9.dp)
                                )
                                .border(
                                    1.dp,
                                    (if (isBlockerActive) Color(0xFF10B981) else Color(0xFFEF4444)).copy(alpha = 0.4f),
                                    RoundedCornerShape(9.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                tint = if (isBlockerActive) Color(0xFF34D399) else Color(0xFFF87171),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Block adult content",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleColor
                            )
                            Text(
                                text = if (isBlockerActive) "Active across all browsers" else "Blocks adult browser sites",
                                fontSize = 11.sp,
                                color = subtextColor
                            )
                        }
                    }

                    Switch(
                        checked = isBlockerActive,
                        onCheckedChange = { wantToEnable ->
                            if (wantToEnable) {
                                when {
                                    !isPartnerLockedHome -> {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Save your Accountability Partner email first",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                        showHomeSetPartnerDialog = true
                                    }
                                    !hasPin -> {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Tap App PIN above so your partner can set your PIN first",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    !isGuardConnected -> {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Enable Purity Tracker in Accessibility Settings first",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                        AntiUninstallManager.requestAccessibilityService(context)
                                    }
                                    else -> {
                                        BlockerManager.enableAdultBlocker(context)
                                        isBlockerActive = true
                                        if (!AntiUninstallManager.isDeviceAdminActive(context)) {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Activate Device Admin to complete uninstall protection",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                            AntiUninstallManager.requestDeviceAdmin(context)
                                        }
                                    }
                                }
                            } else {
                                showHomeTurnOffDialog = true
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF061018),
                            checkedTrackColor = Color(0xFF10B981),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF334155)
                        )
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // Bottom link: "See full Blocker page →" (THE ONLY navigation to Blocker page)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToBlocker?.invoke() }
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "See full Blocker page →",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Slim status line below the card (NOT a card; kept straight and clean on one line, never splitting items)
        val deadItems = buildList {
            if (!isGuardConnected) add(if (isAccessibilityDeadButEnabled) "Accessibility dead" else "Accessibility off")
            if (!isBatteryIgnored) add("Battery restricted")
            if (!isOemAutoStartVisited) add("Auto-start off")
        }
        val allHealthy = deadItems.isEmpty()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (allHealthy) "Protection healthy" else "Protection issue: ${deadItems.joinToString(", ")}",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (allHealthy) Color(0xFF34D399) else Color(0xFFF87171),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (maxWidth >= 280.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (isGuardConnected) Color(0xFF10B981) else Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Accessibility alive",
                                fontSize = 9.5.sp,
                                color = if (isGuardConnected) Color(0xFF34D399) else Color(0xFFF87171),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Text(
                            text = " · ",
                            fontSize = 9.5.sp,
                            color = subtextColor,
                            maxLines = 1,
                            softWrap = false
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (isBatteryIgnored) Color(0xFF10B981) else Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Battery unrestricted",
                                fontSize = 9.5.sp,
                                color = if (isBatteryIgnored) Color(0xFF34D399) else Color(0xFFF87171),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Text(
                            text = " · ",
                            fontSize = 9.5.sp,
                            color = subtextColor,
                            maxLines = 1,
                            softWrap = false
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (isOemAutoStartVisited) Color(0xFF10B981) else Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Auto-start on",
                                fontSize = 9.5.sp,
                                color = if (isOemAutoStartVisited) Color(0xFF34D399) else Color(0xFFF87171),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (isGuardConnected) Color(0xFF10B981) else Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Accessibility alive",
                                fontSize = 10.sp,
                                color = if (isGuardConnected) Color(0xFF34D399) else Color(0xFFF87171),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (isBatteryIgnored) Color(0xFF10B981) else Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Battery unrestricted",
                                fontSize = 10.sp,
                                color = if (isBatteryIgnored) Color(0xFF34D399) else Color(0xFFF87171),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (isOemAutoStartVisited) Color(0xFF10B981) else Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Auto-start on",
                                fontSize = 10.sp,
                                color = if (isOemAutoStartVisited) Color(0xFF34D399) else Color(0xFFF87171),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPremiumStatusDialog) {
        PremiumStatusDialog(
            onDismiss = { showPremiumStatusDialog = false },
            onManagePlan = {
                showPremiumStatusDialog = false
                onNavigateToPremium?.invoke()
            }
        )
    }
}

@Composable
fun TimeBox(valStr: String, label: String) {
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .innerFloatingGlassCard(cornerSize = 14.dp, elevation = 3.5.dp, borderGlow = Color.White)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = valStr,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = if (isLightMode) Color(0xFF0F172A) else Color.White,
                style = TextStyle(shadow = floatingTextShadow(elevation = 3.dp))
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            t(label),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isLightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9),
            style = TextStyle(shadow = floatingTextShadow(elevation = 1.2.dp))
        )
    }
}

@Composable
fun ProtectionChip2Col(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF10B981)
) {
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val isBn = LanguageStore.currentLanguage.value == "bn"

    val cardBg = if (isActive) {
        if (isLightMode) Color(0xFFECFDF5) else Color(0xFF0A221B).copy(alpha = 0.72f)
    } else {
        if (isLightMode) Color(0xFFF1F5F9).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.06f)
    }

    val borderColor = if (isActive) {
        accentColor.copy(alpha = 0.45f)
    } else {
        if (isLightMode) Color(0xFFCBD5E1) else Color.White.copy(alpha = 0.14f)
    }

    val iconTint = if (isActive) {
        accentColor
    } else {
        if (isLightMode) Color(0xFF475569) else Color.White.copy(alpha = 0.65f)
    }

    val titleColor = if (isActive) {
        if (isLightMode) Color(0xFF0F172A) else Color.White
    } else {
        if (isLightMode) Color(0xFF334155) else Color.White.copy(alpha = 0.80f)
    }

    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        border = BorderStroke(if (isActive) 1.2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(
                            if (isActive) accentColor.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.07f),
                            CircleShape
                        )
                        .border(
                            0.8.dp,
                            if (isActive) accentColor.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.14f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = titleColor,
                    fontSize = 11.5.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        if (isActive) Color(0xFF10B981).copy(alpha = 0.18f) else Color(0xFFF59E0B).copy(alpha = 0.12f),
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        0.9.dp,
                        if (isActive) Color(0xFF10B981).copy(alpha = 0.55f) else Color(0xFFF59E0B).copy(alpha = 0.35f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            if (isActive) Color(0xFF34D399) else Color(0xFFFBBF24),
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isActive) (if (isBn) "চালু" else "ON") else (if (isBn) "বন্ধ" else "OFF"),
                    color = if (isActive) Color(0xFF34D399) else Color(0xFFFBBF24),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun ProtectionLiveStatusCard(
    isGuardAlive: Boolean,
    isGuardDeadButEnabled: Boolean,
    isBatteryOptimizationIgnored: Boolean,
    isOemAutoStartConfigured: Boolean,
    isRecentsLockConfirmed: Boolean,
    onFixAccessibility: () -> Unit,
    onFixBatteryOptimization: () -> Unit,
    onFixOemAutoStart: () -> Unit,
    onConfirmRecentsLock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val isBn = LanguageStore.currentLanguage.value == "bn"
    val brandName = remember { AntiUninstallManager.getDeviceBrandName() }
    var showRecentsGuideDialog by remember { mutableStateOf(false) }

    if (showRecentsGuideDialog) {
        AlertDialog(
            onDismissRequest = { showRecentsGuideDialog = false },
            shape = RoundedCornerShape(22.dp),
            containerColor = Color(0xFF0F172A),
            title = {
                Text(
                    text = if (isBn) "$brandName — রিসেন্ট অ্যাপ লক নির্দেশিকা 🔒" else "$brandName — Lock in Recents Guide 🔒",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.5.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = AntiUninstallManager.getBrandRecentsLockInstruction(isBn),
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirmRecentsLock()
                        showRecentsGuideDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isBn) "লক করেছি (নিশ্চিত করুন)" else "I Locked It in Recents",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRecentsGuideDialog = false
                        onFixOemAutoStart()
                    }
                ) {
                    Text(
                        text = if (isBn) "অ্যাপ সেটিংস খুলুন" else "Open App Settings",
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        )
    }

    val healthyCount = listOf(
        isGuardAlive,
        isBatteryOptimizationIgnored,
        isOemAutoStartConfigured,
        isRecentsLockConfirmed
    ).count { it }

    val statusAccent = when {
        isGuardDeadButEnabled || !isGuardAlive || !isBatteryOptimizationIgnored -> Color(0xFFEF4444)
        healthyCount == 4 -> Color(0xFF10B981)
        else -> Color(0xFFF59E0B)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .glassCard(cornerSize = 20.dp, glassAlpha = 0.95f, hasSheen = false),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.2.dp, statusAccent.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(statusAccent.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                            .border(1.dp, statusAccent.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (healthyCount == 4) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = statusAccent,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBn) "লাইভ প্রটেকশন স্ট্যাটাস" else "Protection Status (Live)",
                            color = if (isLightMode) Color(0xFF0F172A) else Color.White,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBn) "ডিভাইস: $brandName" else "Device: $brandName",
                            color = if (isLightMode) Color(0xFF475569) else Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(statusAccent.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                        .border(1.dp, statusAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isBn) "$healthyCount/৪ প্রস্তুত" else "$healthyCount/4 Ready",
                        color = statusAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            if (isGuardDeadButEnabled) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.16f)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isBn)
                            "⚠️ সেটিংসে ON দেখালেও ফোন সিস্টেম গার্ড প্রসেস থামিয়ে দিয়েছে! ঠিক করতে নিচের বাটনে চাপুন এবং Purity Tracker একবার OFF করে আবার ON করুন।"
                        else
                            "⚠️ Guard is DEAD-BUT-ENABLED: Settings shows ON, but the phone stopped the service. Tap Fix below, then toggle Purity Tracker OFF and ON again.",
                        color = Color(0xFFFCA5A5),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // 1. Accessibility service enabled & alive
            LiveRequirementRow(
                title = if (isBn) "১. অ্যাক্সেসিবিলিটি গার্ড সচল (Alive)" else "1. Accessibility Service Alive",
                statusText = when {
                    isGuardAlive -> if (isBn) "সচল (LIVE)" else "ALIVE"
                    isGuardDeadButEnabled -> if (isBn) "থেমে গেছে (OFF→ON করুন)" else "DEAD (Toggle OFF→ON)"
                    else -> if (isBn) "বন্ধ (OFF)" else "OFF"
                },
                isOk = isGuardAlive,
                actionLabel = when {
                    isGuardAlive -> if (isBn) "চেক" else "Check"
                    isGuardDeadButEnabled -> if (isBn) "রিস্টার্ট করুন" else "Rebind"
                    else -> if (isBn) "চালু করুন" else "Enable"
                },
                onActionClick = onFixAccessibility
            )

            // 2. Battery optimization ignored (real PowerManager value)
            LiveRequirementRow(
                title = if (isBn) "২. ব্যাটারি অপটিমাইজেশন ছাড় (PowerManager)" else "2. Battery Optimization Ignored",
                statusText = if (isBatteryOptimizationIgnored) {
                    if (isBn) "অনুমোদিত (Ignored)" else "Ignored (OK)"
                } else {
                    if (isBn) "চালু নেই (ঝুঁকিপূর্ণ)" else "NOT Ignored"
                },
                isOk = isBatteryOptimizationIgnored,
                actionLabel = if (isBatteryOptimizationIgnored) (if (isBn) "সক্রিয়" else "Active") else (if (isBn) "অনুমতি দিন" else "Allow"),
                onActionClick = onFixBatteryOptimization
            )

            // 3. OEM auto-start allowed
            LiveRequirementRow(
                title = if (isBn) "৩. অটো-স্টার্ট ও ব্যাকগ্রাউন্ড পারমিশন ($brandName)" else "3. OEM Auto-Start ($brandName)",
                statusText = if (isOemAutoStartConfigured) {
                    if (isBn) "খোলা হয়েছে" else "Configured"
                } else {
                    if (isBn) "সেটআপ প্রয়োজন" else "Setup Needed"
                },
                isOk = isOemAutoStartConfigured,
                actionLabel = if (isBn) "সেটিংস খুলুন" else "Open OEM",
                onActionClick = onFixOemAutoStart
            )

            // 4. App locked in Recents / background activity allowed
            LiveRequirementRow(
                title = if (isBn) "৪. রিসেন্ট অ্যাপে তালা (Lock in Recents)" else "4. App Locked in Recents",
                statusText = if (isRecentsLockConfirmed) {
                    if (isBn) "লক করা হয়েছে" else "Locked"
                } else {
                    if (isBn) "নির্দেশিকা দেখুন" else "How to Lock"
                },
                isOk = isRecentsLockConfirmed,
                actionLabel = if (isBn) "নিয়ম দেখুন" else "Guide",
                onActionClick = { showRecentsGuideDialog = true }
            )
        }
    }
}

@Composable
private fun LiveRequirementRow(
    title: String,
    statusText: String,
    isOk: Boolean,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val rowBg = if (isOk) {
        if (isLightMode) Color(0xFFECFDF5) else Color(0xFF0A221B).copy(alpha = 0.65f)
    } else {
        if (isLightMode) Color(0xFFFEF2F2) else Color(0xFF2A1215).copy(alpha = 0.65f)
    }
    val borderCol = if (isOk) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.45f)
    val badgeCol = if (isOk) Color(0xFF34D399) else Color(0xFFF87171)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg, RoundedCornerShape(12.dp))
            .border(1.dp, borderCol, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isLightMode) Color(0xFF0F172A) else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = statusText,
                color = badgeCol,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onActionClick,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp),
            shape = RoundedCornerShape(9.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isOk) Color(0xFF10B981).copy(alpha = 0.25f) else Color(0xFFEF4444),
                contentColor = Color.White
            )
        ) {
            Text(
                text = actionLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun UnifiedProtectionCard(
    hasPin: Boolean,
    isBlockerActive: Boolean = false,
    isGuardActive: Boolean,
    isGuardDeadButEnabled: Boolean = false,
    isBatteryExempt: Boolean,
    onBlockerClick: () -> Unit = {},
    onPinClick: () -> Unit,
    onGuardClick: () -> Unit,
    onBatteryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val isBn = LanguageStore.currentLanguage.value == "bn"
    val activeCount = listOf(isBlockerActive, hasPin, isGuardActive, isBatteryExempt).count { it }
    val allActive = activeCount == 4
    val headerAccent = if (allActive) Color(0xFF10B981) else if (activeCount >= 2) Color(0xFF38BDF8) else Color(0xFFF59E0B)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .glassCard(cornerSize = 22.dp, glassAlpha = 0.95f, hasSheen = false),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Shield Icon + "Protection" + "4/4 active"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF10B981).copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBn) "সুরক্ষা ব্যবস্থা" else "Protection",
                        color = if (isLightMode) Color(0xFF0F172A) else Color.White,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Active badge pill
                Box(
                    modifier = Modifier
                        .background(headerAccent.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
                        .border(1.dp, headerAccent.copy(alpha = 0.48f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isBn) "$activeCount/৪ সক্রিয়" else "$activeCount/4 active",
                        color = if (allActive) Color(0xFF34D399) else headerAccent,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Row 1: Blocker | Security PIN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProtectionChip2Col(
                    title = "Blocker",
                    icon = Icons.Outlined.Shield,
                    isActive = isBlockerActive,
                    onClick = onBlockerClick,
                    modifier = Modifier.weight(1f),
                    accentColor = Color(0xFF10B981)
                )
                ProtectionChip2Col(
                    title = if (isBn) "সিকিউরিটি পিন" else "Security PIN",
                    icon = Icons.Outlined.Key,
                    isActive = hasPin,
                    onClick = onPinClick,
                    modifier = Modifier.weight(1f),
                    accentColor = Color(0xFFFBBF24)
                )
            }

            // Row 2: Settings Guard | Battery Exempt
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProtectionChip2Col(
                    title = if (isGuardDeadButEnabled) {
                        if (isBn) "গার্ড থেমে গেছে!" else "Guard DEAD!"
                    } else {
                        if (isBn) "সেটিংস গার্ড" else "Settings Guard"
                    },
                    icon = Icons.Outlined.Lock,
                    isActive = isGuardActive,
                    onClick = onGuardClick,
                    modifier = Modifier.weight(1f),
                    accentColor = Color(0xFF2DD4BF)
                )
                ProtectionChip2Col(
                    title = if (isBn) "ব্যাটারি ছাড়" else "Battery Exempt",
                    icon = Icons.Outlined.PowerSettingsNew,
                    isActive = isBatteryExempt,
                    onClick = onBatteryClick,
                    modifier = Modifier.weight(1f),
                    accentColor = Color(0xFF818CF8)
                )
            }
        }
    }
}

@Composable
fun SettingCard(
    title: String, desc: String, buttonText: String, isOn: Boolean, isWarning: Boolean, 
    icon: ImageVector = Icons.Filled.PlayArrow,
    onClick: (() -> Unit)? = null
) {
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descColor = if (isLightMode) Color(0xFF1E293B) else Color(0xFFE2E8F0)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerSize = 20.dp, glassAlpha = 0.94f, hasSheen = true)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = if (isOn) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (isOn) Color(0xFF34C759) else Color(0xFFFF3B30),
                    modifier = Modifier.size(22.dp).padding(top = 1.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        fontSize = 14.5.sp,
                        style = TextStyle(shadow = floatingTextShadow(elevation = 2.dp))
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isOn) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color(0xFF34C759),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = t("status_on"),
                                color = Color(0xFF34C759),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                tint = Color(0xFFFF3B30),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = t("status_off"),
                                color = Color(0xFFFF3B30),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }
            if (title == t("guard_card_title") && !isOn) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    t("unprotected_inactive"),
                    color = descColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 32.dp),
                    style = TextStyle(shadow = floatingTextShadow(elevation = 1.dp))
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                desc,
                color = descColor,
                fontSize = 11.5.sp,
                modifier = Modifier.padding(start = 32.dp),
                style = TextStyle(shadow = floatingTextShadow(elevation = 1.2.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onClick?.invoke() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOn) Color(0xFF34C759) else Color(0xFFFF3B30)
                ),
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = CircleShape,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(buttonText, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(currentDays: Int, badgesViewModel: BadgesViewModel, onClose: () -> Unit) {
    val dynamicBadges by badgesViewModel.badges.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(t("milestones_title_large"), fontWeight = FontWeight.Bold)
                        Text(t("milestones_desc"), fontSize = 12.sp, color = Color.LightGray)
                    }
                },
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = RedAlert)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlue, titleContentColor = Color.White)
            )
        },
        containerColor = DeepBlue,
        bottomBar = {
            Box(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text(t("go_back"), color = Color.White)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dynamicBadges) { badge ->
                val isUnlocked = currentDays >= badge.requiredDays
                val isActive = dynamicBadges.lastOrNull { currentDays >= it.requiredDays }?.id == badge.id
                BadgeCardItemDynamic(badge, isUnlocked, isActive, currentDays)
            }
        }
    }
}

@Composable
fun BadgeCardItemDynamic(badge: CustomBadge, isUnlocked: Boolean, isActive: Boolean, currentDays: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBlue),
        shape = RoundedCornerShape(12.dp),
        border = if (isActive) BorderStroke(2.dp, CyanAccent) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) CyanAccent.copy(alpha=0.1f) else Color.DarkGray, CircleShape)
                    .border(1.5.dp, if (isUnlocked) Color(0xFFFBBF24) else Color.Gray.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (badge.imageUrl.isNotEmpty() && isUnlocked) {
                    BadgeImage(
                        imageUrl = badge.imageUrl,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .graphicsLayer(scaleX = 1.14f, scaleY = 1.14f),
                        fallbackName = getCustomBadgeName(badge),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = if (isUnlocked) Color(0xFFFFD700) else Color.Gray)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(getCustomBadgeName(badge), color = if (isUnlocked) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(badge.description, color = Color.LightGray, fontSize = 12.sp)
                if (!isUnlocked) {
                    val remaining = badge.requiredDays - currentDays
                    Text(t("days_remaining").format(remaining), color = LightRed, fontSize = 12.sp)
                }
            }
            if (isUnlocked) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "Unlocked", tint = GreenText)
            } else {
                Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun BadgeCardItem(badge: Badge, isUnlocked: Boolean, isActive: Boolean, currentDays: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBlue),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = badge.imgRes),
                contentDescription = null,
                modifier = Modifier.size(64.dp).clip(CircleShape).background(if (!isUnlocked) Color.Gray else Color.Transparent)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(getBadgeName(badge), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isUnlocked) Color.White else Color.Gray)
                }
                Text(t("days_required").format(badge.days), color = if (isUnlocked) GreenText else Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                if (!isUnlocked) {
                    Text(t("unlock_remaining").format(badge.days - currentDays), color = CyanAccent, fontSize = 10.sp)
                } else {
                    Text("Keep your streak going to fully unlock the power of this ${badge.descName} milestone status.", color = Color.LightGray, fontSize = 10.sp, lineHeight = 14.sp)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            val statusColor = if (isActive) CyanAccent else if (isUnlocked) GreenText else Color.Gray
            val statusText = if (isActive) "ACTIVE" else if (isUnlocked) "UNLOCKED" else "LOCKED"
            Box(
                modifier = Modifier.border(1.dp, statusColor, RoundedCornerShape(4.dp)).background(statusColor.copy(alpha=0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                 Text(statusText, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PremiumStatusDialog(
    onDismiss: () -> Unit,
    onManagePlan: () -> Unit
) {
    val isBn = LanguageStore.currentLanguage.value == "bn"
    val isPremiumUser by PremiumManager.isPremium.collectAsState()
    val expiryTimestamp by PremiumManager.premiumExpiryTimestamp.collectAsState()
    val timeInfo = remember(isPremiumUser, expiryTimestamp) {
        PremiumManager.getPremiumTimeInfo(isPremiumUser, expiryTimestamp)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 12.dp,
        icon = {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFFFD700).copy(alpha = 0.15f), CircleShape)
                    .border(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Stars,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isBn) "যোদ্ধা প্রো মেম্বারশিপ সক্রিয় 👑" else "Warrior Pro Active 👑",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isBn) timeInfo.displayTextBn else timeInfo.displayTextEn,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isBn) "সক্রিয় সুবিধাসমূহ:" else "Active Pro Benefits:",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        listOf(
                            "👑 " + if (isBn) "লিডারবোর্ড ও প্রোফাইলে গোল্ডেন ভেরিফাইড ব্যাজ" else "Golden Verified Pro Badge across app",
                            "🛡️ " + if (isBn) "অভিভাবক ইমার্জেন্সি এসএমএস ও স্মার্ট এলার্ট" else "Guardian SMS & Smart Alerts",
                            "🤖 " + if (isBn) "আনলিমিটেড এআই রিকভারি কোচ ও পরামর্শ" else "Unlimited AI Recovery Coach",
                            "🔒 " + if (isBn) "পার্মানেন্ট সেটিং গার্ড প্রটেকশন" else "Permanent Settings Guard Protection",
                            "✨ " + if (isBn) "সম্পূর্ণ বিজ্ঞাপনমুক্ত প্রিমিয়াম অভিজ্ঞতা" else "Ad-Free Clean Experience"
                        ).forEach { benefit ->
                            Text(
                                text = benefit,
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isBn) "ঠিক আছে (বন্ধ করুন)" else "Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ResetDialog(onDismiss: () -> Unit, onOldDate: () -> Unit, onRightNow: () -> Unit) {
    val isLight = com.example.ui.theme.GlassTheme.isLightMode.value
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp), 
            colors = CardDefaults.cardColors(containerColor = if (isLight) Color.White else Color(0xFF1E2D4A))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = RedAlert, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(t("start_where"), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (isLight) Color.Black else Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    t("start_where_desc"),
                    textAlign = TextAlign.Center, color = if (isLight) Color.DarkGray else Color.LightGray, fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onOldDate,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenText),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(t("choose_old_date"), color = Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRightNow,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenText),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(t("right_now"), color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogMock(onDismiss: () -> Unit, onDateSelected: (Int, Int, Int) -> Unit) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val cal = Calendar.getInstance()
                datePickerState.selectedDateMillis?.let { cal.timeInMillis = it }
                onDateSelected(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            }) {
                Text(t("next_time"), color = CyanAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(t("cancel"), color = Color.LightGray) }
        },
        colors = DatePickerDefaults.colors(containerColor = Color(0xFF0F172A))
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = Color(0xFF0F172A),
                titleContentColor = Color.White,
                headlineContentColor = Color.White,
                weekdayContentColor = Color.LightGray,
                dayContentColor = Color.White,
                selectedDayContainerColor = CyanAccent,
                todayDateBorderColor = CyanAccent,
                todayContentColor = CyanAccent,
                yearContentColor = Color.White,
                currentYearContentColor = CyanAccent,
                selectedYearContentColor = Color.Black,
                selectedYearContainerColor = CyanAccent,
                subheadContentColor = Color.White,
                dayInSelectionRangeContainerColor = Color(0x3306B6D4),
                dayInSelectionRangeContentColor = Color.White
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBadgeScreen(onBack: () -> Unit, badgesViewModel: BadgesViewModel = viewModel()) {
    val badges by badgesViewModel.badges.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editBadge: CustomBadge? by remember { mutableStateOf(null) }

    var badgeName by remember { mutableStateOf("") }
    var badgeDays by remember { mutableStateOf("") }
    var badgeImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        badgeImageUri = uri
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editBadge != null) "Edit Badge" else "New Badge") },
            text = {
                Column {
                    OutlinedTextField(
                        value = badgeName,
                        onValueChange = { badgeName = it },
                        label = { Text("Badge Name") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = badgeDays,
                        onValueChange = { badgeDays = it },
                        label = { Text("Required Days") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { galleryLauncher.launch("image/*") }) {
                        Text("Pick Image")
                    }
                    if (badgeImageUri != null) {
                        coil.compose.AsyncImage(
                            model = badgeImageUri,
                            contentDescription = "Selected image",
                            modifier = Modifier.size(64.dp)
                        )
                    } else {
                        BadgeImage(
                            imageUrl = editBadge?.imageUrl ?: "",
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            },
            confirmButton = {
                val context = LocalContext.current
                Button(onClick = {
                    val days = badgeDays.toIntOrNull() ?: 0
                    if (editBadge != null) {
                        badgesViewModel.updateBadge(context, editBadge!!.id, badgeName, days, badgeName, badgeImageUri, editBadge?.imageUrl ?: "")
                    } else {
                        badgesViewModel.addBadge(context, badgeName, days, badgeName, badgeImageUri)
                    }
                    showDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Badges", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlue)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editBadge = null
                badgeName = ""
                badgeDays = ""
                badgeImageUri = null
                showDialog = true
            }, containerColor = CyanAccent) {
                Icon(Icons.Filled.Add, contentDescription = "Add Badge")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(contentPadding = innerPadding, modifier = Modifier.padding(16.dp)) {
            items(badges) { badge ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .glassCard(cornerSize = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        BadgeImage(
                            imageUrl = badge.imageUrl,
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            fallbackName = getCustomBadgeName(badge)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(getCustomBadgeName(badge), color = Color.White, fontWeight = FontWeight.Bold)
                            Text(t("days_required").format(badge.requiredDays), color = Color.LightGray, fontSize = 12.sp)
                        }
                        IconButton(onClick = {
                            editBadge = badge
                            badgeName = badge.name
                            badgeDays = badge.requiredDays.toString()
                            badgeImageUri = null
                            showDialog = true
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color.Cyan)
                        }
                        IconButton(onClick = { badgesViewModel.deleteBadge(badge.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}
