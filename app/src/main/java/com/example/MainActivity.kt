package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.isActive
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

val DeepBlue = Color(0xFF131D2D)
val CardBlue = Color(0xFF1E2A40)
val RedAlert = Color(0xFFE53E3E)
val LightRed = Color(0xFFFF8A8A)
val GreenText = Color(0xFF10B981)
val CyanAccent = Color(0xFF06B6D4)

object LanguageStore {
    var currentLanguage = mutableStateOf("en")
    
    val translations = mapOf(
        "en" to mapOf(
            "nav_home" to "Home",
            "nav_history" to "History",
            "nav_focus" to "Focus",
            "nav_ai" to "AI Chat",
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
            "pin_dialog_title_change" to "Change PIN",
            "pin_dialog_title_set" to "Set Security PIN",
            "pin_dialog_advice" to "Advice: Ask a trusted friend/family member to set this PIN so you don't know it yourself.",
            "pin_dialog_enter" to "Enter 4-digit PIN",
            "pin_save_success" to "PIN Saved successfully",
            "pin_too_short" to "PIN too short",
            "pin_card_title" to "Security PIN",
            "pin_card_desc_set" to "PIN is set. You will need it to disable VPN or change settings.",
            "pin_card_desc_not_set" to "No PIN set. Anyone can turn off protection.",
            "pin_card_btn_set" to "PIN Set (Locked)",
            "pin_card_btn_not_set" to "Set PIN",
            "vpn_card_title" to "Built-in VPN Shield (Block 18+)",
            "vpn_card_desc" to "Routes traffic locally to filter adult content with AdGuard DNS. Does not leak your data.",
            "vpn_card_btn_connected" to "VPN Shield Connected",
            "vpn_card_btn_activate" to "Activate VPN Shield",
            "guard_card_title" to "Settings Guard (Anti-Uninstall)",
            "guard_card_desc_active" to "Guard is active. Settings and uninstallation are fully protected.",
            "guard_card_desc_inactive" to "Your hard-earned clean streak can be deleted or uninstalled anytime! Enable settings guard now.",
            "guard_card_btn_active" to "Guard Activated",
            "guard_card_btn_inactive" to "Enable Settings Guard",
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
            "pin_dialog_title_change" to "পিন পরিবর্তন করুন",
            "pin_dialog_title_set" to "নিরাপত্তা পিন সেট করুন",
            "pin_dialog_advice" to "উপদেশ: আপনার কোনো বিশ্বস্ত বন্ধু বা পরিবারের কাউকে দিয়ে এই পিনটি সেট করান, যাতে আপনার নিজের পিনটি অজানা থাকে।",
            "pin_dialog_enter" to "৪ ডিজিটের পিন লিখুন",
            "pin_save_success" to "পিন সফলভাবে সংরক্ষণ করা হয়েছে",
            "pin_too_short" to "পিন অত্যন্ত ছোট",
            "pin_card_title" to "নিরাপত্তা পিন",
            "pin_card_desc_set" to "পিন সেট করা আছে। ভিপিএন বা সেটিংস বদলাতে এটি প্রয়োজন হবে।",
            "pin_card_desc_not_set" to "কোনো পিন সেট করা নেই। যেকোনো কেউ প্রটেকশন বন্ধ করতে পারে।",
            "pin_card_btn_set" to "পিন লকড",
            "pin_card_btn_not_set" to "পিন সেট করুন",
            "vpn_card_title" to "বিল্ট-ইন VPN শিল্ড (১৮+ ব্লক)",
            "vpn_card_desc" to "অ্যাডাল্ট কন্টেন্ট ফিল্টার করতে AdGuard DNS ব্যবহার করে ট্রাফিক রিডাইরেক্ট করে। কোনো বাহ্যিক অ্যাপ লাগবে না।",
            "vpn_card_btn_connected" to "VPN কানেক্টেড",
            "vpn_card_btn_activate" to "VPN শিল্ড চালু করুন",
            "guard_card_title" to "সেটিংস গার্ড (অ্যান্টি-আনইনস্টল)",
            "guard_card_desc_active" to "গার্ড সচল আছে। আপনার সেটিংস এবং আনইনস্টল সুরক্ষিত।",
            "guard_card_desc_inactive" to "আপনার কঠোর পরিশ্রমের স্ট্রিকটি যে কোনো সময় ডিলিট বা আনইনস্টল হয়ে যেতে পারে! এখনই সেটিংস গার্ড সচল করুন।",
            "guard_card_btn_active" to "সচল রয়েছে",
            "guard_card_btn_inactive" to "সেটিংস গার্ড চালু করুন",
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
            "ai_welcome_text" to "Assalamu alaikum wa rahmatullah! I am your Islamic counselor and addiction recovery companion. I am here to help you break free from bad habits, pornography and negative thoughts, and to build a pure, conscious, and spiritual life in accordance with Islamic teachings and Ruqyah guidance. Feel free to share your thoughts - everything will be kept strictly confidential.",
            "ai_missing_key" to "Please go to AI Studio's 'Secrets' panel and set 'GEMINI_API_KEY' with your Gemini API key.",
            "ai_connection_error" to "We are sorry, Gemini API connection failed. Please check your internet connection and try again.",
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
            "tab_islamic_ai" to "Islamic AI",
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
            "ai_welcome_text" to "আসসালামু আলাইকুম ওয়া রাহমাতুল্লাহ! আমি আপনার ইসলামিক কাউনসিলর এবং আসক্তি নিরাময় সঙ্গী। আমি আপনাকে খারাপ অভ্যাস, পর্নোগ্রাফি এবং নেতিবাচক চিন্তার ফাঁদ থেকে মুক্ত হয়ে একটি পবিত্র, সচেতন এবং আধ্যাত্মিক জীবন গড়ে তুলতে সাহায্য করার জন্য ও আপনার রুকইয়াহ্ হিসেবে নির্দেশনা দিতে নিয়োজিত রয়েছি ইনশাআল্লাহ। আপনার যেকোনো মানসিক বা আত্মিক অনুভূতি কোনো দ্বিধা ছাড়াই সরাসরি লিখে শেয়ার করতে পারেন, যা সম্পূর্ণভাবে গোপন থাকবে।",
            "ai_missing_key" to "দয়া করে এআই স্টুডিও এর 'Secrets' প্যানেলে গিয়ে 'GEMINI_API_KEY' নামে একটি ভেরিয়েবল তৈরি করে আপনার জেমিনি এপিআই-কী (API Key) সেট করুন।",
            "ai_connection_error" to "আমরা দুঃখিত, জেমিনি এপিআই সংযোগ বিচ্ছিন্ন হয়েছে। অনুগ্রহ করে ইন্টারনেট কানেকশন চেক করে আবার চেষ্টা করুন।",
            "ai_thinking" to "ভাবছি...",
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
            "tab_islamic_ai" to "ইসলামিক এআই",
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
            "ai_connection_error" to "نحن آسفون، فشل اتصال Gemini API. يرجى التحقق من اتصال الإنترنت والمحاولة مرة أخرى.",
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
            "ai_connection_error" to "ہم معذرت خواہ ہیں، جیمنی API کنکشن ناکام ہوگیا۔ براہ کرم اپنا انٹرنیٹ کنکشن چیک کریں اور دوبارہ کوشش کریں۔",
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
            "ai_connection_error" to "हमें खेद है, जेमिनी एपीआई कनेक्शन विफल हो गया। कृपया आपका इंटरनेट कनेक्शन जांचें और पुनः प्रयास करें।",
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
            "ai_connection_error" to "Lo sentimos, falló la conexión con la API de Gemini. Por favor, comprueba tu conexión a internet e inténtalo de nuevo.",
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

data class Badge(
    val id: Int,
    val name: String,
    val days: Int,
    @DrawableRes val imgRes: Int,
    val descName: String
)

@Composable
fun BadgeImage(imageUrl: String, modifier: Modifier = Modifier, fallbackName: String = "Badge") {
    if (imageUrl.startsWith("data:image")) {
        val base64String = imageUrl.substringAfter("base64,")
        val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        if (bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = fallbackName,
                modifier = modifier,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            Icon(Icons.Filled.EmojiEvents, contentDescription = fallbackName, modifier = modifier, tint = Color(0xFFFFD700))
        }
    } else if (imageUrl.isNotEmpty()) {
        coil.compose.AsyncImage(
            model = imageUrl,
            contentDescription = fallbackName,
            modifier = modifier,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    } else {
        Icon(Icons.Filled.EmojiEvents, contentDescription = fallbackName, modifier = modifier, tint = Color(0xFFFFD700))
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0L)

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
            }
        }
    }

    fun resetTimer(customBadges: List<CustomBadge> = emptyList()) {
        val now = System.currentTimeMillis()
        saveHistoryBeforeReset(now, customBadges)
        _startTime.value = now
        prefs.edit().putLong("start_time", now).apply()
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

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Important to update the activity's intent
        if (intent.action == "com.example.VPN_CONFIGURE") {
            triggerGuardianAlert(this, "attempted to open VPN configuration or disconnect the VPN Shield")
            _vpnConfigureEvent.tryEmit(true)
        } else if (intent.action == "com.example.MOTIVATION") {
            _motivationEvent.tryEmit(true)
        }
    }

    private fun fetchLocationAndSchedule() {
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
        } catch(e: SecurityException) {
            PrayerScheduler.scheduleDailyPrayers(this)
        } catch(e: Exception) {
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
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        
        try {
            startService(Intent(this, BackgroundNotificationService::class.java))
        } catch(e: Exception) {}
        
        if (intent?.action == "com.example.VPN_CONFIGURE") {
            triggerGuardianAlert(this, "attempted to open VPN configuration or disconnect the VPN Shield")
            _vpnConfigureEvent.tryEmit(true)
        } else if (intent?.action == "com.example.MOTIVATION") {
            _motivationEvent.tryEmit(true)
        }
        try {
            val pref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
            val lang = pref.getString("language", "en") ?: "en"
            LanguageStore.currentLanguage.value = lang
            if (lang.isNotBlank() && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
                val locale = java.util.Locale(lang)
                java.util.Locale.setDefault(locale)
                val config = android.content.res.Configuration(resources.configuration)
                config.setLocale(locale)
                resources.updateConfiguration(config, resources.displayMetrics)
            }
        } catch (e: Exception) {}
        
        enableEdgeToEdge()
        setContent {
            var currentTab by remember { mutableStateOf("home") }
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
            }

            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = DeepBlue,
                    surface = CardBlue,
                    primary = CyanAccent,
                    onBackground = Color.White,
                    onSurface = Color.White
                )
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

                AppScreen(currentTab = currentTab, onTabSelected = { currentTab = it })
            }
        }
    }
}

@Composable
fun AppScreen(currentTab: String, onTabSelected: (String) -> Unit) {
    Scaffold(
        bottomBar = { AppBottomNav(currentTab, onTabSelected) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                "home" -> HomeScreen()
                "history" -> HistoryScreen()
                "focus" -> FocusScreen()
                "ai" -> AiScreen()
                "community" -> CommunityScreen()
                "profile" -> ProfileScreen(onNavigateToStaticPage = onTabSelected)
                "admin_badges" -> AdminBadgeScreen(onBack = { onTabSelected("profile") })
            }
        }
    }
}

@Composable
fun AppBottomNav(currentTab: String, onTabSelected: (String) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFF151F30),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text(t("nav_home"), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
            selected = currentTab == "home",
            onClick = { onTabSelected("home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GreenText,
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.History, contentDescription = "History") },
            label = { Text(t("nav_history"), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
            selected = currentTab == "history",
            onClick = { onTabSelected("history") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GreenText,
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.GpsFixed, contentDescription = "Focus") },
            label = { Text(t("nav_focus"), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
            selected = currentTab == "focus",
            onClick = { onTabSelected("focus") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GreenText,
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Spa, contentDescription = "AI") },
            label = { Text(t("nav_ai"), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
            selected = currentTab == "ai",
            onClick = { onTabSelected("ai") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GreenText,
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Group, contentDescription = "Community") },
            label = { Text(t("nav_community"), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
            selected = currentTab == "community",
            onClick = { onTabSelected("community") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GreenText,
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text(t("nav_profile"), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
            selected = currentTab == "profile",
            onClick = { onTabSelected("profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GreenText,
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color.Gray
            )
        )
    }
}

@Composable
fun HistoryScreen(viewModel: PurityTrackerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val bestRecord by viewModel.bestRecord.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val currentDurationDays = (durationMs / (1000 * 60 * 60 * 24)).toInt()

    val recordDays = maxOf(currentDurationDays, bestRecord?.durationDays ?: 0)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CardBlue)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFD700))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Best Record", color = Color.LightGray, fontSize = 12.sp)
                    Text("$recordDays days", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CardBlue)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GreenText)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Current Streak", color = Color.LightGray, fontSize = 12.sp)
                    Text("$currentDurationDays days", color = GreenText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Text("Tracked Purity Sessions (${historyList.size + 1})", color = Color.White, fontWeight = FontWeight.Bold)

        val startTime by viewModel.startTime.collectAsStateWithLifecycle()
        val calendar = Calendar.getInstance().apply { timeInMillis = startTime }
        val formatter = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
        val startStr = formatter.format(calendar.time)

        TrackedSessionItem("Current (Active)", "Start Time: $startStr", "$currentDurationDays days", Icons.Filled.Star, CyanAccent, CyanAccent)

        historyList.forEach { history ->
            val hCal = Calendar.getInstance().apply { timeInMillis = history.startTime }
            val hStartStr = formatter.format(hCal.time)
            TrackedSessionItem(history.title, "Start Time: $hStartStr", history.statusText, Icons.Filled.Star, Color.Gray, Color.White)
        }

        if (historyList.isEmpty()) {
            Text("Failure History (0)", color = Color.White, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBlue)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = GreenText, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No Relapses Recorded!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Your preservation history is perfectly clean.", color = Color.LightGray, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun TrackedSessionItem(title: String, desc: String, duration: String, icon: ImageVector, iconTint: Color, durationColor: Color) {
    Card(
         modifier = Modifier.fillMaxWidth(),
         colors = CardDefaults.cardColors(containerColor = CardBlue)
    ) {
         Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
             Icon(icon, contentDescription = null, tint = iconTint)
             Spacer(modifier = Modifier.width(16.dp))
             Column(modifier = Modifier.weight(1f)) {
                 Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                 Text(desc, color = Color.LightGray, fontSize = 12.sp)
             }
             Text(duration, color = durationColor, fontWeight = FontWeight.Bold)
         }
    }
}

@Composable
fun FocusScreen() {
    var isPlaying by remember { mutableStateOf(false) }
    var exerciseState by remember { mutableStateOf("Inhale") }
    var secondsLeft by remember { mutableStateOf(4) }
    var totalSeconds by remember { mutableStateOf(0L) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive) {
                delay(1000)
                totalSeconds++
                secondsLeft--
                if (secondsLeft <= 0) {
                    when (exerciseState) {
                        "Inhale" -> {
                            exerciseState = "Hold"
                            secondsLeft = 4
                        }
                        "Hold" -> {
                            if (totalSeconds % 12L < 8L) { 
                                exerciseState = "Exhale" 
                                secondsLeft = 4 
                            } else { 
                                exerciseState = "Inhale" 
                                secondsLeft = 4 
                            }
                        }
                        "Exhale" -> {
                            exerciseState = "Hold "
                            secondsLeft = 4
                        }
                        "Hold " -> {
                            exerciseState = "Inhale"
                            secondsLeft = 4
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(t("focus_mode_header"), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(t("focus_desc_top"), color = Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.weight(1f))

        val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "breathing")
        val circleScale by transition.animateFloat(
            initialValue = if (exerciseState == "Inhale") 0.8f else 1.2f,
            targetValue = if (exerciseState == "Inhale") 1.2f else if (exerciseState == "Exhale") 0.8f else (if (exerciseState == "Hold") 1.2f else 0.8f),
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(4000, easing = androidx.compose.animation.core.LinearEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Restart
            ),
            label = "scale"
        )
        
        val actualScale = if (isPlaying) circleScale else 1f

        Box(
            modifier = Modifier.size(280.dp),
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
                    color = CyanAccent.copy(alpha = 0.5f),
                    strokeWidth = 4.dp
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val stateText = if (isPlaying) {
                    when (exerciseState.trim()) {
                        "Inhale" -> t("focus_inhale")
                        "Hold" -> t("focus_hold")
                        "Exhale" -> t("focus_exhale")
                        else -> exerciseState
                    }
                } else {
                    t("focus_ready")
                }
                Text(stateText, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                if (isPlaying) {
                    Text("$secondsLeft ${t("seconds")}", color = Color.LightGray, fontSize = 18.sp)
                } else {
                    Text(t("focus_press_play"), color = Color.LightGray, fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val days = totalSeconds / (24 * 3600)
        val hours = (totalSeconds % (24 * 3600)) / 3600
        val mins = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60

        Text(String.format("%02d : %02d : %02d : %02d", days, hours, mins, secs), color = CyanAccent, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Text("${t("days")} : ${t("hours")} : ${t("minutes")} : ${t("seconds")}", color = Color.LightGray, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(32.dp))

        IconButton(
            onClick = { 
                if (isPlaying) {
                    isPlaying = false
                } else {
                    isPlaying = true
                    totalSeconds = 0L
                    exerciseState = "Inhale"
                    secondsLeft = 4
                }
            }, 
            modifier = Modifier.size(64.dp).background(if (isPlaying) RedAlert else GreenText, CircleShape)
        ) {
             Icon(
                 if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow, 
                 contentDescription = if (isPlaying) "Stop" else "Play", 
                 tint = DeepBlue, 
                 modifier = Modifier.size(32.dp)
             )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(t("focus_desc_bottom"), color = Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun AiScreen() {
    var isIslamicAi by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
            Card(
                colors = CardDefaults.cardColors(containerColor = if(isIslamicAi) CardBlue else DeepBlue),
                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                modifier = Modifier.clickable { isIslamicAi = true }
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = GreenText, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(t("tab_islamic_ai"), color = Color.White)
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = if(!isIslamicAi) CardBlue else DeepBlue),
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                modifier = Modifier.clickable { isIslamicAi = false }
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Book, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(t("tab_salah_tracker"), color = Color.White)
                }
            }
        }

        if (isIslamicAi) {
            IslamicAiChat()
        } else {
            SalahTracker()
        }
    }
}

@Composable
fun IslamicAiChat(aiViewModel: AiViewModel = viewModel()) {
    val messages by aiViewModel.messages.collectAsState()
    val isLoading by aiViewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    val context = LocalContext.current
    var tts by remember { mutableStateOf<android.speech.tts.TextToSpeech?>(null) }
    var speakingMessageId by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val ttsInstance = android.speech.tts.TextToSpeech(context) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                // Set language to Bengali BD, if missing/not supported fall back to default locale
                val result = tts?.setLanguage(java.util.Locale("bn", "BD"))
                if (result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED || result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA) {
                    tts?.setLanguage(java.util.Locale.getDefault())
                }
            }
        }
        
        ttsInstance.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                if (speakingMessageId == utteranceId) {
                    speakingMessageId = null
                }
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                if (speakingMessageId == utteranceId) {
                    speakingMessageId = null
                }
            }
        })

        tts = ttsInstance

        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    val speakMessage: (String, String) -> Unit = { id, text ->
        tts?.let {
            it.stop()
            speakingMessageId = id
            val params = Bundle().apply {
                putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id)
            }
            it.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, id)
        }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (msg.isUser) CyanAccent.copy(alpha=0.2f) else CardBlue),
                        shape = if (msg.isUser) RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp) else RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp),
                        modifier = Modifier.padding(
                            start = if (msg.isUser) 32.dp else 0.dp,
                            end = if (msg.isUser) 0.dp else 32.dp
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val msgText = if (messages.firstOrNull() == msg && !msg.isUser) t("ai_welcome_text") else msg.text
                            Text(msgText, color = Color.White, fontSize = 14.sp)
                            
                            // AI message interactive utility options
                            if (!msg.isUser && msg.text.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // 1. Text to Speech Play/Stop button
                                    val isCurrentlySpeaking = speakingMessageId == msg.id
                                    IconButton(
                                        onClick = {
                                            if (isCurrentlySpeaking) {
                                                tts?.stop()
                                                speakingMessageId = null
                                            } else {
                                                speakMessage(msg.id, msgText)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isCurrentlySpeaking) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                                            contentDescription = t("listen"),
                                            tint = if (isCurrentlySpeaking) CyanAccent else Color.LightGray.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // 2. Clipboard copy button
                                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(msgText))
                                            android.widget.Toast.makeText(context, LanguageStore.get("message_copied"), android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ContentCopy,
                                            contentDescription = t("copy_message"),
                                            tint = Color.LightGray.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    // 3. Like button
                                    IconButton(
                                        onClick = { aiViewModel.toggleLike(msg.id, true) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (msg.isLiked == true) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                            contentDescription = t("like"),
                                            tint = if (msg.isLiked == true) CyanAccent else Color.LightGray.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // 4. Dislike button
                                    IconButton(
                                        onClick = { aiViewModel.toggleLike(msg.id, false) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (msg.isLiked == false) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                                            contentDescription = t("dislike"),
                                            tint = if (msg.isLiked == false) Color.Red.copy(alpha = 0.8f) else Color.LightGray.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (isLoading) {
                item {
                    Text(t("ai_thinking"), color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text(t("chat_hint")) },
                modifier = Modifier.weight(1f).background(CardBlue, RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardBlue,
                    unfocusedContainerColor = CardBlue,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { 
                    aiViewModel.sendMessage(inputText)
                    inputText = "" 
                }, 
                modifier = Modifier.background(CyanAccent, CircleShape),
                enabled = !isLoading && inputText.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = DeepBlue)
            }
        }
    }
}

fun formatTime(time24: String?): String {
    if (time24 == null) return ""
    try {
        val sdf24 = java.text.SimpleDateFormat("HH:mm", java.util.Locale.US)
        val sdf12 = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        return sdf12.format(sdf24.parse(time24.replace(Regex("\\s\\(.*\\)"), ""))!!)
    } catch(e: Exception) { return time24 }
}

fun formatTimeRange(start: String?, end: String?): String {
    if (start == null || end == null) return ""
    return "${formatTime(start)} - ${formatTime(end)}"
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
            
            // Check forbidden times
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

            // simple ordered list
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
                // Next is tomorrow's Fajr
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
                // Background Illustration
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
                    // Top area
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
                    
                    // Bottom area
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${t("next_waqt_prefix") ?: "Next Waqt:"} $translatedNext", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(timeRemaining.localizeDigits(), color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Bottom links
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
                val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        PrayerScheduler.updateLocation(context, location.latitude, location.longitude)
                    }
                    PrayerScheduler.scheduleDailyPrayers(context)
                }.addOnFailureListener {
                    PrayerScheduler.scheduleDailyPrayers(context)
                }
            } catch(e: SecurityException) {
                PrayerScheduler.scheduleDailyPrayers(context)
            } catch(e: Exception) {
                PrayerScheduler.scheduleDailyPrayers(context)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        if (!hasLocationPermission) {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = CardBlue)) {
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
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBlue)) {
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
        Text(t("today_checklist"), color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        PrayerCountdownTimer(todayTimings)

        val fajrRange = formatTimeRange(todayTimings["Fajr"], todayTimings["Sunrise"])
        val dhuhrRange = formatTimeRange(todayTimings["Dhuhr"], todayTimings["Asr"])
        val asrRange = formatTimeRange(todayTimings["Asr"], todayTimings["Maghrib"])
        val maghribRange = formatTimeRange(todayTimings["Maghrib"], todayTimings["Isha"])
        val ishaRange = formatTimeRange(todayTimings["Isha"], todayTimings["Fajr"]) // technically next day's mid/fajr.

        SalahItem(t("fajr"), t("fajr_desc"), fajrRange, todaySalahs[0]) { viewModel.toggleSalah(0) }
        SalahItem(t("dhuhr"), t("dhuhr_desc"), dhuhrRange, todaySalahs[1]) { viewModel.toggleSalah(1) }
        SalahItem(t("asr"), t("asr_desc"), asrRange, todaySalahs[2]) { viewModel.toggleSalah(2) }
        SalahItem(t("maghrib"), t("maghrib_desc"), maghribRange, todaySalahs[3]) { viewModel.toggleSalah(3) }
        SalahItem(t("isha"), t("isha_desc"), ishaRange, todaySalahs[4]) { viewModel.toggleSalah(4) }
    }
}

@Composable
fun SalahItem(title: String, desc: String, timeRange: String, checked: Boolean, onClick: () -> Unit) {
     Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = CardBlue)) {
          Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
               Box(modifier = Modifier.size(24.dp).background(DeepBlue, CircleShape)) {
                   if (checked) {
                       Icon(Icons.Filled.Check, contentDescription = null, tint = GreenText, modifier = Modifier.size(16.dp).align(Alignment.Center))
                   }
               }
               Spacer(modifier = Modifier.width(16.dp))
               Column(modifier = Modifier.weight(1f)) {
                   Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                   if (timeRange.isNotEmpty()) {
                       Text(timeRange, color = CyanAccent, fontSize = 12.sp)
                   }
                   Text(desc, color = Color.LightGray, fontSize = 12.sp)
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
fun CommunityScreen(communityViewModel: CommunityViewModel = viewModel(), authViewModel: AuthViewModel = viewModel()) {
    val posts by communityViewModel.posts.collectAsState()
    val leaders by communityViewModel.leaders.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    var postText by remember { mutableStateOf("") }
    
    var activeTab by remember { mutableStateOf("feed") } // "feed" or "challenges"
    var activeChatChallenge by remember { mutableStateOf<Challenge?>(null) }

    if (activeChatChallenge != null) {
        ChallengeChatRoom(
            challenge = activeChatChallenge!!,
            communityViewModel = communityViewModel,
            currentUserEmail = currentUser?.email ?: "",
            onBack = { activeChatChallenge = null }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Tab switcher Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBlue)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeTab == "feed") CyanAccent else Color.Transparent)
                        .clickable { activeTab = "feed" }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Public,
                            contentDescription = null,
                            tint = if (activeTab == "feed") DeepBlue else Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = t("tab_global"),
                            color = if (activeTab == "feed") DeepBlue else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeTab == "challenges") CyanAccent else Color.Transparent)
                        .clickable { activeTab = "challenges" }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.People,
                            contentDescription = null,
                            tint = if (activeTab == "challenges") DeepBlue else Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = t("tab_challenges"),
                            color = if (activeTab == "challenges") DeepBlue else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeTab == "feed") {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Global Leaderboard (Top Days)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (leaders.isEmpty()) {
                        Text("No warriors found on leaderboard yet...", color = Color.LightGray)
                    } else {
                        leaders.forEachIndexed { index, leader ->
                            val days = TimeUnit.MILLISECONDS.toDays(leader.streakMs).toInt()
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("${index + 1}.", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
                                Text(leader.name, color = Color.White, modifier = Modifier.weight(1f))
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${days} days streak", color = Color.White, fontSize = 12.sp, modifier = Modifier.width(100.dp), textAlign = TextAlign.End)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Post your progress", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBlue)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = postText,
                                onValueChange = { postText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Write something inspiring to fuel other warriors...", color = Color.LightGray, fontSize = 14.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Gray,
                                    focusedBorderColor = CyanAccent,
                                    unfocusedTextColor = Color.White,
                                    focusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { 
                                    if (postText.isNotBlank()) {
                                        communityViewModel.addPost(postText, "Active Streak: Unknown", "Novice")
                                        postText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                  Text("Publish Status", color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Latest Motivational Board", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (posts.isEmpty()) {
                        Text("Loading posts or no posts yet...", color = Color.LightGray)
                    } else {
                        posts.forEach { post ->
                            CommunityPost(
                                post = post,
                                currentUserId = currentUser?.uid ?: "",
                                communityViewModel = communityViewModel
                            )
                        }
                    }
                }
            } else {
                ChallengesTabContent(
                    communityViewModel = communityViewModel,
                    currentUserEmail = currentUser?.email ?: "",
                    onOpenChat = { challenge ->
                        activeChatChallenge = challenge
                    }
                )
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
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(t("create_challenge_btn"), color = DeepBlue, fontWeight = FontWeight.Bold)
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
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                        ) {
                            Text("Create", color = DeepBlue)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Your Challenges Section
        if (joinedChallenges.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Verified, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("আপনার চ্যালেঞ্জসমূহ (Joined)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            Text("অন্যান্য চ্যালেঞ্জসমূহ (Available)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (availableChallenges.isEmpty()) {
            Text("No other challenges available yet. Create one!", color = Color.LightGray, modifier = Modifier.padding(vertical = 8.dp))
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBlue)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.background(CyanAccent.copy(alpha=0.1f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                    Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(challenge.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(String.format(t("challenge_participants"), challenge.participantCount), color = Color.LightGray, fontSize = 12.sp)
                }
                Box(modifier = Modifier.background(DeepBlue, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("${challenge.durationDays} Days", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(challenge.description, color = Color.LightGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(String.format(t("challenge_creator"), challenge.creatorName), color = Color.Gray, fontSize = 11.sp, modifier = Modifier.weight(1f))
                
                Row {
                    if (isJoined) {
                        TextButton(onClick = onLeave) {
                            Text(t("leave"), color = Color(0xFFEF4444), fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onOpenChat,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.ChatBubble, contentDescription = null, tint = DeepBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(t("chat_enter"), color = DeepBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onJoin,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp)
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

    // Scroll to bottom on load or new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(challenge.id) {
        communityViewModel.fetchChallengeMessages(challenge.id)
    }

    Column(modifier = Modifier.fillMaxSize().background(DeepBlue)) {
        // Custom Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBlue)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(challenge.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${challenge.durationDays} Days • ${challenge.participantCount} Participants", color = CyanAccent, fontSize = 12.sp)
            }
        }

        // Message Scroll Area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "শুরুতেই একটি অনুপ্রেরণাদায়ক বার্তা পাঠিয়ে গ্রুপে প্রথম ব্যক্তি হোন! ✨",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
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
                            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                        ) {
                            if (!isMe) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(msg.senderName, color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    if (msg.senderEmail == "muhaiminulhasananas@gmail.com") {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Filled.Verified, contentDescription = "Admin", tint = CyanAccent, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isMe) 12.dp else 0.dp,
                                            bottomEnd = if (isMe) 0.dp else 12.dp
                                        )
                                    )
                                    .background(if (isMe) Color(0xFFEF4444) else CardBlue)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(msg.message, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        // Chat input strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBlue)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputMsg,
                onValueChange = { inputMsg = it },
                placeholder = { Text("গ্রুপে আপনার অনুপ্রেরণাদায়ক বার্তা লিখুন...", color = Color.Gray, fontSize = 13.sp) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.DarkGray,
                    focusedBorderColor = CyanAccent,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputMsg.isNotBlank()) {
                         communityViewModel.sendChallengeMessage(challenge.id, inputMsg)
                         inputMsg = ""
                    }
                },
                modifier = Modifier
                    .background(CyanAccent, CircleShape)
                    .size(44.dp)
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send", tint = DeepBlue, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun CommunityPost(post: Post, currentUserId: String, communityViewModel: CommunityViewModel) {
    val context = LocalContext.current
    val hasLiked = post.likedBy.contains(currentUserId)
    var showComments by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = CardBlue)) {
        Column(modifier = Modifier.padding(16.dp)) {
             Row(verticalAlignment = Alignment.CenterVertically) {
                 Box(modifier = Modifier.size(40.dp).background(CyanAccent, CircleShape))
                 Spacer(modifier = Modifier.width(12.dp))
                 Column(modifier = Modifier.weight(1f)) {
                      Text(post.name, color = Color.White, fontWeight = FontWeight.Bold)
                      if (post.userEmail == "muhaiminulhasananas@gmail.com") {
                          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)) {
                              Icon(Icons.Filled.Verified, contentDescription = "Admin", tint = CyanAccent, modifier = Modifier.size(18.dp))
                              Spacer(modifier = Modifier.width(4.dp))
                              Text("Admin & Developer", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                          }
                      }
                      Text(post.streak, color = Color.LightGray, fontSize = 12.sp)
                 }
                 Box(modifier = Modifier.background(DeepBlue, RoundedCornerShape(4.dp)).border(1.dp, CyanAccent, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                      Text(post.badge, color = CyanAccent, fontSize = 10.sp)
                 }
             }
             Spacer(modifier = Modifier.height(12.dp))
             Text(post.content, color = Color.White, fontSize = 14.sp)
             Spacer(modifier = Modifier.height(12.dp))
             Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier=Modifier.fillMaxWidth()) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     Box(modifier = Modifier.background(if(hasLiked) Color(0xFFEF4444).copy(alpha=0.2f) else Color(0xFF4B5563), RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 6.dp).clickable { communityViewModel.toggleLike(post.id, post.likedBy) }) {
                         Row(verticalAlignment = Alignment.CenterVertically) {
                             Icon(Icons.Filled.Favorite, contentDescription = null, tint = if(hasLiked) Color(0xFFEF4444) else LightRed, modifier = Modifier.size(16.dp))
                             Spacer(modifier = Modifier.width(4.dp))
                             Text(post.claps.toString(), color = Color.White, fontSize = 12.sp)
                         }
                     }
                     Spacer(modifier = Modifier.width(12.dp))
                     Box(modifier = Modifier.background(Color(0xFF4B5563), RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 6.dp).clickable {
                         showComments = !showComments
                         if (showComments) communityViewModel.fetchComments(post.id)
                     }) {
                         Row(verticalAlignment = Alignment.CenterVertically) {
                             Icon(Icons.Filled.ChatBubble, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                             Spacer(modifier = Modifier.width(4.dp))
                             Text(post.commentsCount.toString(), color = Color.White, fontSize = 12.sp)
                         }
                     }
                 }
                 Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color.LightGray, modifier = Modifier.clickable {
                     val sendIntent: android.content.Intent = android.content.Intent().apply {
                         action = android.content.Intent.ACTION_SEND
                         putExtra(android.content.Intent.EXTRA_TEXT, "${post.name} shared on Purity Tracker:\n\n${post.content}")
                         type = "text/plain"
                     }
                     val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                     context.startActivity(shareIntent)
                 })
             }
             
             if (showComments) {
                 Spacer(modifier = Modifier.height(16.dp))
                 Divider(color = Color.DarkGray)
                 Spacer(modifier = Modifier.height(8.dp))
                 
                 val commentsMap by communityViewModel.comments.collectAsState()
                 val postComments = commentsMap[post.id] ?: emptyList()
                 
                 if (postComments.isEmpty()) {
                     Text("No comments yet. Be the first!", color = Color.LightGray, fontSize = 12.sp)
                 } else {
                     postComments.forEach { comment ->
                         Column(modifier = Modifier.padding(bottom = 8.dp)) {
                             Text(comment.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                             if (comment.userEmail == "muhaiminulhasananas@gmail.com") {
                                 Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)) {
                                     Icon(Icons.Filled.Verified, contentDescription = "Admin", tint = CyanAccent, modifier = Modifier.size(14.dp))
                                     Spacer(modifier = Modifier.width(4.dp))
                                     Text("Admin", color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                 }
                             }
                             Spacer(modifier = Modifier.height(2.dp))
                             Text(comment.content, color = Color.LightGray, fontSize = 12.sp)
                         }
                     }
                 }
                 
                 Spacer(modifier = Modifier.height(8.dp))
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     OutlinedTextField(
                         value = commentText,
                         onValueChange = { commentText = it },
                         placeholder = { Text("Write a comment...", color = Color.Gray, fontSize = 12.sp) },
                         modifier = Modifier.weight(1f).height(50.dp),
                         colors = OutlinedTextFieldDefaults.colors(
                             unfocusedBorderColor = Color.DarkGray,
                             focusedBorderColor = CyanAccent,
                             unfocusedTextColor = Color.White,
                             focusedTextColor = Color.White
                         ),
                         singleLine = true
                     )
                     Spacer(modifier = Modifier.width(8.dp))
                     IconButton(onClick = {
                         if (commentText.isNotBlank()) {
                             communityViewModel.addComment(post.id, commentText)
                             commentText = ""
                         }
                     }, modifier = Modifier.background(CyanAccent, CircleShape).size(40.dp)) {
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
    onNavigateToStaticPage: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
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
        if (currentUser != null) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(currentUser!!.uid).get().addOnSuccessListener { doc ->
                val record = doc.toObject(LeaderboardUser::class.java)
                if (record != null && record.streakMs > 0) {
                    val elapsed = System.currentTimeMillis() - record.timestamp
                    val realStreak = record.streakMs + elapsed
                    val currentStreak = System.currentTimeMillis() - purityTrackerViewModel.startTime.value
                    if (realStreak > currentStreak) {
                        purityTrackerViewModel.restoreStreak(System.currentTimeMillis() - realStreak)
                        android.widget.Toast.makeText(context, "Cloud sync: Restored your previous streak!", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
         Text(androidx.compose.ui.res.stringResource(id = R.string.nav_profile), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
         Spacer(modifier = Modifier.height(24.dp))
         
         if (currentUser == null) {
             var isPasswordlessMode by remember { mutableStateOf(false) }
             var isSignUpMode by remember { mutableStateOf(true) }       // Default to sign up mode
             var name by remember { mutableStateOf("") }
             var email by remember { mutableStateOf("") }
             var password by remember { mutableStateOf("") }
             var isPasswordVisible by remember { mutableStateOf(false) }
             var isLoading by remember { mutableStateOf(false) }

             Box(
                 modifier = Modifier
                     .fillMaxWidth()
                     .background(
                         brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                             colors = listOf(
                                 CardBlue,
                                 DeepBlue
                             )
                         ),
                         shape = RoundedCornerShape(20.dp)
                     )
                     .border(1.2.dp, CyanAccent.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                     .padding(18.dp),
                  contentAlignment = Alignment.Center
              ) {
                  Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      modifier = Modifier.fillMaxWidth()
                  ) {
                      Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.Center,
                          modifier = Modifier.fillMaxWidth()
                      ) {
                          Box(
                              modifier = Modifier
                                  .size(36.dp)
                                  .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                                  .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                              contentAlignment = Alignment.Center
                          ) {
                              Icon(
                                  imageVector = if (isSignUpMode) Icons.Filled.Person else Icons.Filled.Lock,
                                  contentDescription = null,
                                  modifier = Modifier.size(16.dp),
                                  tint = CyanAccent
                              )
                          }
                          
                          Spacer(modifier = Modifier.width(10.dp))
                          
                          Text(
                              text = if (isSignUpMode) "Create Account" else "Sign In",
                              color = Color.White,
                              fontSize = 18.sp,
                              fontWeight = FontWeight.Bold
                          )
                      }
                      
                      Spacer(modifier = Modifier.height(4.dp))
                      
                      Text(
                          text = "Sync your progress, stats, and badges safely.",
                          color = Color.LightGray.copy(alpha = 0.7f),
                          fontSize = 11.sp,
                          textAlign = TextAlign.Center
                      )
                      
                      Spacer(modifier = Modifier.height(14.dp))

                      Row(
                          modifier = Modifier
                              .fillMaxWidth()
                              .height(38.dp)
                              .background(Color(0xFF0F172A), RoundedCornerShape(24.dp))
                              .padding(2.dp)
                      ) {
                          Button(
                              onClick = { isSignUpMode = true },
                              colors = ButtonDefaults.buttonColors(
                                  containerColor = if (isSignUpMode) CyanAccent else Color.Transparent,
                                  contentColor = if (isSignUpMode) Color.Black else Color.LightGray
                              ),
                              shape = RoundedCornerShape(24.dp),
                              elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                              modifier = Modifier.weight(1f).fillMaxHeight(),
                              contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                          ) {
                              Text(text = "Register 📝", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                          }
                          
                          Button(
                              onClick = { isSignUpMode = false },
                              colors = ButtonDefaults.buttonColors(
                                  containerColor = if (!isSignUpMode) CyanAccent else Color.Transparent,
                                  contentColor = if (!isSignUpMode) Color.Black else Color.LightGray
                              ),
                              shape = RoundedCornerShape(24.dp),
                              elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                              modifier = Modifier.weight(1f).fillMaxHeight(),
                              contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                          ) {
                              Text(text = "Sign In 🔑", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                          }
                      }

                      /*
                 contentAlignment = Alignment.Center
             ) {
                  Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      modifier = Modifier.fillMaxWidth()
                 ) {
                     Box(
                         modifier = Modifier
                             .size(60.dp)
                             .background(Color(0xFF131D2D), RoundedCornerShape(16.dp))
                             .border(1.2.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                         contentAlignment = Alignment.Center
                     ) {
                         Icon(
                             imageVector = if (isSignUpMode) Icons.Filled.Person else Icons.Filled.Lock,
                             contentDescription = null,
                             modifier = Modifier.size(28.dp),
                             tint = CyanAccent
                         )
                     }
                     
                     Spacer(modifier = Modifier.height(16.dp))
                     
                     Text(
                         text = if (isSignUpMode) "Create Account" else "Sign In",
                         color = Color.White,
                         fontSize = 22.sp,
                         fontWeight = FontWeight.ExtraBold
                     )
                     
                     Spacer(modifier = Modifier.height(6.dp))
                     
                     Text(
                         text = "Purity Tracker: Sign in to sync your progress, stats, and badges safely.",
                         color = Color.LightGray.copy(alpha = 0.8f),
                         fontSize = 12.sp,
                         textAlign = TextAlign.Center,
                         modifier = Modifier.padding(horizontal = 12.dp)
                     )
                     
                     Spacer(modifier = Modifier.height(18.dp))

                     Row(
                         modifier = Modifier
                             .fillMaxWidth(0.95f)
                             .background(Color(0xFF131D2D), RoundedCornerShape(50.dp))
                             .padding(4.dp)
                     ) {
                         Button(
                             onClick = { isSignUpMode = true },
                             colors = ButtonDefaults.buttonColors(
                                 containerColor = if (isSignUpMode) CyanAccent else Color.Transparent,
                                 contentColor = if (isSignUpMode) Color.Black else Color.LightGray
                             ),
                             shape = RoundedCornerShape(50.dp),
                             elevation = if (isSignUpMode) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                             modifier = Modifier.weight(1f),
                             contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                         ) {
                             Text(text = "Register 📝", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                         }
                         
                         Button(
                             onClick = { isSignUpMode = false },
                             colors = ButtonDefaults.buttonColors(
                                 containerColor = if (!isSignUpMode) CyanAccent else Color.Transparent,
                                 contentColor = if (!isSignUpMode) Color.Black else Color.LightGray
                             ),
                             shape = RoundedCornerShape(50.dp),
                             elevation = if (!isSignUpMode) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                             modifier = Modifier.weight(1f),
                             contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                         ) {
                             Text(text = "Sign In 🔑", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                         }
                     }

                     Spacer(modifier = Modifier.height(20.dp))

                     */
                     Spacer(modifier = Modifier.height(14.dp))
                     Column(modifier = Modifier.fillMaxWidth()) {
                         if (isSignUpMode) {
                             androidx.compose.material3.OutlinedTextField(
                                 value = name,
                                 onValueChange = { name = it },
                                 placeholder = { Text("Full Name", color = Color.LightGray.copy(alpha = 0.6f)) },
                                 leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = CyanAccent) },
                                 colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                     focusedTextColor = Color.White,
                                     unfocusedTextColor = Color.White,
                                     focusedPlaceholderColor = Color.LightGray,
                                     unfocusedPlaceholderColor = Color.LightGray,
                                     focusedBorderColor = CyanAccent,
                                     unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                     focusedContainerColor = Color(0xFF131D2D).copy(alpha = 0.8f),
                                     unfocusedContainerColor = Color(0xFF131D2D).copy(alpha = 0.5f),
                                     errorContainerColor = Color.Transparent
                                 ),
                                 shape = RoundedCornerShape(16.dp),
                                 modifier = Modifier.fillMaxWidth(),
                                 singleLine = true
                             )
                             Spacer(modifier = Modifier.height(10.dp))
                         }

                         androidx.compose.material3.OutlinedTextField(
                             value = email,
                             onValueChange = { email = it },
                             placeholder = { Text("Email Address", color = Color.LightGray.copy(alpha = 0.6f)) },
                             leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = CyanAccent) },
                             colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                 focusedTextColor = Color.White,
                                 unfocusedTextColor = Color.White,
                                 focusedPlaceholderColor = Color.LightGray,
                                 unfocusedPlaceholderColor = Color.LightGray,
                                 focusedBorderColor = CyanAccent,
                                 unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                 focusedContainerColor = Color(0xFF131D2D).copy(alpha = 0.8f),
                                 unfocusedContainerColor = Color(0xFF131D2D).copy(alpha = 0.5f),
                                 errorContainerColor = Color.Transparent
                             ),
                             shape = RoundedCornerShape(16.dp),
                             modifier = Modifier.fillMaxWidth(),
                             singleLine = true
                         )

                         if (!isPasswordlessMode) {
                             Spacer(modifier = Modifier.height(10.dp))
                             androidx.compose.material3.OutlinedTextField(
                                 value = password,
                                 onValueChange = { password = it },
                                 placeholder = { Text("Password", color = Color.LightGray.copy(alpha = 0.6f)) },
                                 leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = CyanAccent) },
                                 trailingIcon = {
                                     val icon = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                     IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                         Icon(icon, contentDescription = null, tint = Color.LightGray)
                                     }
                                 },
                                 visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                 colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                     focusedTextColor = Color.White,
                                     unfocusedTextColor = Color.White,
                                     focusedPlaceholderColor = Color.LightGray,
                                     unfocusedPlaceholderColor = Color.LightGray,
                                     focusedBorderColor = CyanAccent,
                                     unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                     focusedContainerColor = Color(0xFF131D2D).copy(alpha = 0.8f),
                                     unfocusedContainerColor = Color(0xFF131D2D).copy(alpha = 0.5f),
                                     errorContainerColor = Color.Transparent
                                 ),
                                 shape = RoundedCornerShape(16.dp),
                                 modifier = Modifier.fillMaxWidth(),
                                 singleLine = true
                             )

                             if (!isSignUpMode) {
                                 Box(
                                     modifier = Modifier.fillMaxWidth(),
                                     contentAlignment = Alignment.CenterEnd
                                 ) {
                                     TextButton(
                                         onClick = {
                                             if (email.isBlank()) {
                                                 android.widget.Toast.makeText(context, LanguageStore.get("enter_email_first"), android.widget.Toast.LENGTH_LONG).show()
                                                 return@TextButton
                                             }
                                             isLoading = true
                                             authViewModel.sendPasswordReset(email) { success, msg ->
                                                 isLoading = false
                                                 if (success) {
                                                     android.widget.Toast.makeText(context, LanguageStore.get("reset_sent"), android.widget.Toast.LENGTH_LONG).show()
                                                 } else {
                                                     android.widget.Toast.makeText(context, msg ?: "Failed to send reset link", android.widget.Toast.LENGTH_LONG).show()
                                                 }
                                             }
                                         },
                                         contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                     ) {
                                         Text(
                                             text = t("forgot_password"),
                                             color = CyanAccent,
                                             fontSize = 11.sp,
                                             fontWeight = FontWeight.SemiBold
                                         )
                                     }
                                 }
                             }
                         }
                     }

                     Spacer(modifier = Modifier.height(20.dp))

                     if (isLoading) {
                         CircularProgressIndicator(color = CyanAccent)
                     } else {
                         Button(
                             onClick = {
                                 if (email.isBlank()) {
                                     android.widget.Toast.makeText(context, "Please enter your email", android.widget.Toast.LENGTH_SHORT).show()
                                     return@Button
                                 }
                                 if (isPasswordlessMode) {
                                     if (name.isBlank()) {
                                         android.widget.Toast.makeText(context, "Please enter your name", android.widget.Toast.LENGTH_SHORT).show()
                                         return@Button
                                     }
                                     isLoading = true
                                     authViewModel.sendPasswordlessSignInLink(email, name) { success, msg ->
                                         isLoading = false
                                         if (success) {
                                             android.widget.Toast.makeText(context, "Magic login link sent to your email! 📧 Please check your inbox.", android.widget.Toast.LENGTH_LONG).show()
                                         } else {
                                             android.widget.Toast.makeText(context, msg ?: "Failed to send link", android.widget.Toast.LENGTH_LONG).show()
                                         }
                                     }
                                 } else {
                                     if (password.length < 6) {
                                         android.widget.Toast.makeText(context, "Password must be at least 6 characters", android.widget.Toast.LENGTH_SHORT).show()
                                         return@Button
                                     }
                                     isLoading = true
                                     if (isSignUpMode) {
                                         if (name.isBlank()) {
                                             android.widget.Toast.makeText(context, "Please enter your name", android.widget.Toast.LENGTH_SHORT).show()
                                             isLoading = false
                                             return@Button
                                         }
                                         authViewModel.signUpWithEmail(email, name, password, context) { success, msg ->
                                             isLoading = false
                                             if (!success) {
                                                 android.widget.Toast.makeText(context, msg ?: "Signup failed", android.widget.Toast.LENGTH_SHORT).show()
                                             }
                                         }
                                     } else {
                                         authViewModel.signInWithEmail(email, password, context) { success, msg ->
                                             isLoading = false
                                             if (!success) {
                                                 android.widget.Toast.makeText(context, msg ?: "Login failed", android.widget.Toast.LENGTH_SHORT).show()
                                             }
                                         }
                                     }
                                 }
                             },
                             colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                             shape = RoundedCornerShape(16.dp),
                             modifier = Modifier.fillMaxWidth(),
                             contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp)
                         ) {
                             Text(
                                 text = if (isPasswordlessMode) "Get Login Link" else if (isSignUpMode) "Register" else "Get Started",
                                 color = Color.Black,
                                 fontSize = 14.sp,
                                 fontWeight = FontWeight.Bold
                             )
                         }
                     }

                     if (!isPasswordlessMode) {
                         Spacer(modifier = Modifier.height(8.dp))
                         TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
                             Text(
                                 text = if (isSignUpMode) "Already have an account? Sign In" else "New here? Create Account",
                                 color = CyanAccent,
                                 fontSize = 12.sp,
                                 fontWeight = FontWeight.SemiBold
                             )
                         }
                     }

                     Spacer(modifier = Modifier.height(18.dp))

                     Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(0.9f)) {
                         Box(modifier = Modifier.height(1.dp).weight(1f).background(Color.White.copy(alpha = 0.15f)))
                         Text(" OR ", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                         Box(modifier = Modifier.height(1.dp).weight(1f).background(Color.White.copy(alpha = 0.15f)))
                     }

                     Spacer(modifier = Modifier.height(14.dp))

                     Button(
                         onClick = { authViewModel.signInWithGoogle(context) },
                         colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                         shape = RoundedCornerShape(16.dp),
                         elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                         modifier = Modifier.fillMaxWidth(),
                         contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp)
                     ) {
                         Icon(
                             painter = painterResource(id = R.drawable.ic_google),
                             contentDescription = null,
                             modifier = Modifier.size(20.dp),
                             tint = Color.Unspecified
                         )
                         Spacer(modifier = Modifier.width(10.dp))
                         Text("Log In with Google", color = Color(0xFF1E293B), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                     }

                     Spacer(modifier = Modifier.height(16.dp))
                     Text("Developed by Anas", color = Color.LightGray.copy(alpha = 0.5f), fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                 }
             }
         } else {
             Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBlue)) {
                  Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                       Icon(Icons.Filled.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                       Spacer(modifier = Modifier.height(16.dp))
                       Text("Welcome, ${currentUser?.displayName ?: "Warrior"}!", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                       Spacer(modifier = Modifier.height(8.dp))
                       Text("Email: ${currentUser?.email}", color = Color.LightGray, fontSize = 14.sp)
                       Spacer(modifier = Modifier.height(24.dp))
                       Button(onClick = { authViewModel.signOut() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)), modifier = Modifier.fillMaxWidth()) {
                           Text("Log Out")
                       }
                       
                       // Check if user is admin based on email
                       if (currentUser?.email == "muhaiminulhasananas@gmail.com") {
                           Spacer(modifier = Modifier.height(12.dp))
                           Button(onClick = { onNavigateToStaticPage("admin_badges") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), modifier = Modifier.fillMaxWidth()) {
                               Icon(Icons.Filled.AdminPanelSettings, contentDescription = null)
                               Spacer(modifier = Modifier.width(8.dp))
                               Text("Admin: Manage Badges")
                           }
                       }
                  }
             }
         }

         Spacer(modifier = Modifier.height(24.dp))
         Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBlue)) {
             Column(modifier = Modifier.padding(16.dp)) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     Icon(Icons.Filled.Security, contentDescription = null, tint = CyanAccent)
                     Spacer(modifier = Modifier.width(8.dp))
                     Text(t("guardian_shield"), color = Color.White, fontWeight = FontWeight.Bold)
                 }
                 Spacer(modifier = Modifier.height(12.dp))
                 val sharedPref = context.getSharedPreferences("GuardianPrefs", Context.MODE_PRIVATE)
                 var userName by remember { mutableStateOf(sharedPref.getString("userName", "") ?: "") }
                 var guardianName by remember { mutableStateOf(sharedPref.getString("guardianName", "") ?: "") }
                 var guardianEmail by remember { mutableStateOf(sharedPref.getString("guardianEmail", "") ?: "") }
                 var guardianPhone by remember { mutableStateOf(sharedPref.getString("guardianPhone", "") ?: "") }
                 var isShieldActive by remember { mutableStateOf(sharedPref.getBoolean("isShieldActive", false)) }

                 Row(verticalAlignment = Alignment.CenterVertically) {
                     androidx.compose.foundation.layout.Box(
                         modifier = Modifier
                             .background(Color.Red.copy(alpha = 0.2f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                             .padding(horizontal = 6.dp, vertical = 2.dp)
                     ) {
                         Text(t("upcoming_feature"), color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                     }
                 }
                 Spacer(modifier = Modifier.height(4.dp))
                 Text(t("guardian_desc"), color = Color.LightGray, fontSize = 12.sp)

                 Spacer(modifier = Modifier.height(16.dp))
                 OutlinedTextField(value = userName, onValueChange = { userName = it; sharedPref.edit().putString("userName", it).apply() }, label = { Text(t("your_name")) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Gray, focusedBorderColor = CyanAccent, unfocusedTextColor = Color.White, focusedTextColor = Color.White, unfocusedLabelColor = Color.Gray, focusedLabelColor = CyanAccent))
                 Spacer(modifier = Modifier.height(12.dp))
                 OutlinedTextField(value = guardianName, onValueChange = { guardianName = it; sharedPref.edit().putString("guardianName", it).apply() }, label = { Text(t("guardian_name")) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Gray, focusedBorderColor = CyanAccent, unfocusedTextColor = Color.White, focusedTextColor = Color.White, unfocusedLabelColor = Color.Gray, focusedLabelColor = CyanAccent))
                 Spacer(modifier = Modifier.height(12.dp))
                 OutlinedTextField(value = guardianEmail, onValueChange = { guardianEmail = it; sharedPref.edit().putString("guardianEmail", it).apply() }, label = { Text(t("guardian_email")) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Gray, focusedBorderColor = CyanAccent, unfocusedTextColor = Color.White, focusedTextColor = Color.White, unfocusedLabelColor = Color.Gray, focusedLabelColor = CyanAccent))
                 Spacer(modifier = Modifier.height(12.dp))
                 OutlinedTextField(value = guardianPhone, onValueChange = { guardianPhone = it; sharedPref.edit().putString("guardianPhone", it).apply() }, label = { Text(t("phone_optional")) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Gray, focusedBorderColor = CyanAccent, unfocusedTextColor = Color.White, focusedTextColor = Color.White, unfocusedLabelColor = Color.Gray, focusedLabelColor = CyanAccent))
                 Spacer(modifier = Modifier.height(16.dp))

                 Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                     Column {
                         Text(t("enable_alert_system"), color = Color.White, fontWeight = FontWeight.Bold)
                         Text(t("alert_system_desc"), color = Color.LightGray, fontSize = 12.sp)
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

                 Spacer(modifier = Modifier.height(16.dp))
                 Card(colors = CardDefaults.cardColors(containerColor = GreenText.copy(alpha = 0.1f)), border = BorderStroke(1.dp, GreenText)) {
                     Column(modifier = Modifier.padding(12.dp)) {
                         Row(verticalAlignment = Alignment.CenterVertically) {
                             Icon(Icons.Filled.Info, contentDescription = null, tint = GreenText, modifier = Modifier.size(16.dp))
                             Spacer(modifier = Modifier.width(4.dp))
                             Text(t("upcoming_firebase"), color = GreenText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                         }
                         Spacer(modifier = Modifier.height(4.dp))
                         Text(t("upcoming_firebase_desc"), color = GreenText, fontSize = 12.sp)
                     }
                 }
                 Spacer(modifier = Modifier.height(16.dp))
                 Button(onClick = {
                      if (userName.isBlank() || guardianName.isBlank() || guardianEmail.isBlank()) {
                          android.widget.Toast.makeText(context, t("fill_required_fields"), android.widget.Toast.LENGTH_SHORT).show()
                      } else {
                          sharedPref.edit().apply {
                              putString("userName", userName)
                              putString("guardianName", guardianName)
                              putString("guardianEmail", guardianEmail)
                              putString("guardianPhone", guardianPhone)
                              putBoolean("isShieldActive", isShieldActive)
                          }.apply()
                          android.widget.Toast.makeText(context, LanguageStore.get("save_guardian_success"), android.widget.Toast.LENGTH_SHORT).show()
                      }
                  }, colors = ButtonDefaults.buttonColors(containerColor = CyanAccent), modifier = Modifier.fillMaxWidth()) {
                     Text(t("save_guardian"), color = DeepBlue)
                  }
             }
         }

         Spacer(modifier = Modifier.height(24.dp))
         var expanded by remember { mutableStateOf(false) }
         Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().clickable { expanded = true }.padding(vertical = 8.dp)) {
             Row(verticalAlignment = Alignment.CenterVertically) {
                 Icon(Icons.Filled.Language, contentDescription = null, tint = Color.LightGray)
                 Spacer(modifier = Modifier.width(8.dp))
                 Text(t("change_language"), color = Color.White)
             }
             Box {
                 Text(t("select_arrow"), color = CyanAccent)
                 DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                     val languages = listOf("en" to "English", "bn" to "বাংলা", "hi" to "हिंदी", "es" to "Español", "ar" to "العربية", "ur" to "اردو")
                     languages.forEach { (tag, name) ->
                         DropdownMenuItem(text = { Text(name) }, onClick = {
                             changeAppLocale(context, tag)
                             expanded = false
                         })
                     }
                 }
             }
         }
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
fun HomeScreen(
    viewModel: PurityTrackerViewModel = viewModel(),
    badgesViewModel: BadgesViewModel = viewModel(),
    communityViewModel: CommunityViewModel = viewModel()
) {
    val durationMs by viewModel.durationMs.collectAsState()
    val dynamicBadges by badgesViewModel.badges.collectAsState()
    
    // Sync streak when it changes periodically (every time recomposed with new days is too frequent, so use a simple LaunchedEffect on Days change)
    val days = TimeUnit.MILLISECONDS.toDays(durationMs).toInt()
    LaunchedEffect(days) {
        communityViewModel.syncUserStreak(durationMs)
    }
    
    var showBadgesMenu by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val hours = TimeUnit.MILLISECONDS.toHours(durationMs) % 24
    val mins = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
    val secs = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60

    val currentBadge = dynamicBadges.lastOrNull { days >= it.requiredDays } ?: dynamicBadges.firstOrNull() ?: CustomBadge("", "None", 0, "None")
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text(t("welcome_warrior"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text(t("quote_desc"), fontSize = 14.sp, color = Color.LightGray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Warning Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = RedAlert.copy(alpha = 0.2f)),
            border = BorderStroke(1.dp, RedAlert),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = LightRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(t("alert_warning"), fontWeight = FontWeight.Bold, color = LightRed)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    t("alert_warning_desc"),
                    fontSize = 12.sp, color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Timer Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBlue),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(t("current_badge"), color = CyanAccent, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.05f,
                        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                            animation = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                        )
                    )
                    
                    val glowAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.2f,
                        targetValue = 0.8f,
                        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                            animation = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                        )
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(scale)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(CyanAccent.copy(alpha = glowAlpha), Color.Transparent)
                                ),
                                shape = CircleShape
                            )
                    )
                    
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize().scale(scale),
                        color = CyanAccent,
                        trackColor = Color(0xFF374151),
                        strokeWidth = 8.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(144.dp)
                            .scale(scale)
                            .background(CyanAccent.copy(alpha=0.1f), CircleShape)
                            .border(4.dp, Color(0xFF2D3748), CircleShape)
                            .clip(CircleShape)
                    ) {
                        BadgeImage(
                            imageUrl = currentBadge.imageUrl,
                            modifier = Modifier.fillMaxSize(),
                            fallbackName = currentBadge.name
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(currentBadge.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(100.dp),
                        color = Color.White,
                        trackColor = Color(0xFF374151),
                        strokeWidth = 4.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(String.format("%02d", days), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(t("days"), fontSize = 10.sp, color = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TimeBox(String.format("%02d", hours), "hours")
                    TimeBox(String.format("%02d", mins), "minutes")
                    TimeBox(String.format("%02d", secs), "seconds")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = RedAlert),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Menu, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(t("stop_timer"))
                    }
                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B5563)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(t("reset"))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GreenText, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(t("next_badge_in", nextBadge?.name ?: "Max", nextDaysReq - days), color = Color.LightGray, fontSize = 12.sp)
                    }
                    TextButton(onClick = { showBadgesMenu = true }, contentPadding = PaddingValues(0.dp)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(t("all_badges"), color = CyanAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DNS Card
        var dnsEnabled by remember { mutableStateOf(false) }
        var showDnsInstructions by remember { mutableStateOf(false) }
        val context = LocalContext.current

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
        var hasPin by remember { mutableStateOf(LockManager.hasPin(context)) }
        var showPinDialog by remember { mutableStateOf(false) }
        var tempPin by remember { mutableStateOf("") }

        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = { showPinDialog = false },
                title = { Text(if(hasPin) t("pin_dialog_title_change") else t("pin_dialog_title_set"), color = Color.White) },
                text = {
                    Column {
                        if (!hasPin) {
                            Text(t("pin_dialog_advice"), color = LightRed, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        OutlinedTextField(
                            value = tempPin,
                            onValueChange = { tempPin = it },
                        label = { Text(t("pin_dialog_enter")) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor=Color.White, unfocusedTextColor = Color.White)
                    )
                    }
                },
                containerColor = CardBlue,
                confirmButton = {
                    Button(onClick = {
                        if (tempPin.length >= 4) {
                            LockManager.setPin(context, tempPin)
                            hasPin = true
                            showPinDialog = false
                            android.widget.Toast.makeText(context, LanguageStore.get("pin_save_success"), android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(context, LanguageStore.get("pin_too_short"), android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)) {
                        Text(t("reset"))
                    }
                },
                   dismissButton = {
                    TextButton(onClick = { showPinDialog = false }) { Text(t("cancel"), color = Color.Gray) }
                }
            )
        }

        SettingCard(
            title = t("pin_card_title"),
            desc = if (hasPin) t("pin_card_desc_set") else t("pin_card_desc_not_set"),
            buttonText = if (hasPin) t("pin_card_btn_set") else t("pin_card_btn_not_set"),
            isOn = hasPin,
            isWarning = !hasPin,
            icon = Icons.Filled.Lock,
            onClick = {
                if (!hasPin) {
                    tempPin = ""
                    showPinDialog = true
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Check if VPN is running/started natively.
        val safeContext = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            context.createDeviceProtectedStorageContext()
        } else {
            context
        }
        val vpnPrefs = safeContext.getSharedPreferences("VpnPrefs", Context.MODE_PRIVATE)
        var vpnServiceRunning by remember { mutableStateOf(LocalDnsVpnService.isVpnInterfaceUp()) }
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        
        DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                    val preferRunning = vpnPrefs.getBoolean("vpn_is_running", false)
                    if (preferRunning) {
                        if (android.net.VpnService.prepare(context) == null) {
                            // We have permission. If not running, start it.
                            if (!LocalDnsVpnService.isVpnInterfaceUp()) {
                                try {
                                    val startIntent = Intent(context, LocalDnsVpnService::class.java).apply { action = LocalDnsVpnService.ACTION_START }
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        context.startForegroundService(startIntent)
                                    } else {
                                        context.startService(startIntent)
                                    }
                                } catch(e:Exception){}
                            }
                        }
                    }
                    // small delay to let service update its static var
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        vpnServiceRunning = LocalDnsVpnService.isVpnInterfaceUp()
                    }, 200)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        var vpnLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                try {
                    val startIntent = Intent(context, LocalDnsVpnService::class.java).apply { action = LocalDnsVpnService.ACTION_START }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(startIntent)
                    } else {
                        context.startService(startIntent)
                    }
                } catch(e:Exception){}
                vpnPrefs.edit().putBoolean("vpn_is_running", true).apply()
                vpnServiceRunning = true
                android.widget.Toast.makeText(context, "VPN Shield Activated", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        // Persistent continuous UI-thread guard loop to keep VPN alive when app is open or active
        LaunchedEffect(Unit) {
            while (true) {
                val preferRunning = vpnPrefs.getBoolean("vpn_is_running", false)
                if (preferRunning) {
                    val isUp = LocalDnsVpnService.isVpnInterfaceUp()
                    if (!isUp) {
                        val prepareIntent = android.net.VpnService.prepare(context)
                        if (prepareIntent == null) {
                            try {
                                val startIntent = Intent(context, LocalDnsVpnService::class.java).apply { action = LocalDnsVpnService.ACTION_START }
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    context.startForegroundService(startIntent)
                                } else {
                                    context.startService(startIntent)
                                }
                                LocalDnsVpnService.isRunning = true
                            } catch (e: Exception) {}
                        } else {
                            // If permission is missing but preferred, auto-launch prompt to ensure protection
                            try {
                                vpnLauncher.launch(prepareIntent)
                            } catch (e: Exception) {}
                        }
                    }
                }
                vpnServiceRunning = LocalDnsVpnService.isVpnInterfaceUp()
                kotlinx.coroutines.delay(1000) // check every 1 second
            }
        }
        
        SettingCard(
            title = t("vpn_card_title"),
            desc = t("vpn_card_desc"),
            buttonText = if (vpnServiceRunning) t("vpn_card_btn_connected") else t("vpn_card_btn_activate"),
            isOn = vpnServiceRunning,
            isWarning = !vpnServiceRunning,
            onClick = {
                if (!hasPin) {
                    android.widget.Toast.makeText(context, LanguageStore.get("error_set_pin"), android.widget.Toast.LENGTH_SHORT).show()
                    return@SettingCard
                }
                if (!vpnServiceRunning) {
                    val intent = android.net.VpnService.prepare(context)
                    if (intent != null) {
                        try {
                            vpnLauncher.launch(intent)
                        } catch(e: Exception){}
                    } else {
                        try {
                            val startIntent = Intent(context, LocalDnsVpnService::class.java).apply { action = LocalDnsVpnService.ACTION_START }
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                context.startForegroundService(startIntent)
                            } else {
                                context.startService(startIntent)
                            }
                        } catch(e:Exception){}
                        vpnPrefs.edit().putBoolean("vpn_is_running", true).apply()
                        vpnServiceRunning = true
                        android.widget.Toast.makeText(context, "VPN Shield Activated", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Settings Guard Card
        var isAccessibilityEnabled by remember { mutableStateOf(false) }

        DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
                    isAccessibilityEnabled = am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC).any {
                        it.resolveInfo.serviceInfo.packageName == context.packageName && it.resolveInfo.serviceInfo.name == "com.example.ProtectionService"
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
        
        SettingCard(
            title = t("guard_card_title"),
            desc = if (isAccessibilityEnabled) t("guard_card_desc_active") else t("guard_card_desc_inactive"),
            buttonText = if (isAccessibilityEnabled) t("guard_card_btn_active") else t("guard_card_btn_inactive"),
            isOn = isAccessibilityEnabled,
            isWarning = !isAccessibilityEnabled,
            icon = Icons.Filled.Lock,
            onClick = {
                if (!vpnServiceRunning) {
                    android.widget.Toast.makeText(context, LanguageStore.get("error_enable_vpn"), android.widget.Toast.LENGTH_SHORT).show()
                    return@SettingCard
                }
                if (!isAccessibilityEnabled) {
                    try {
                        val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                        android.widget.Toast.makeText(context, "Find 'Purity Tracker' and enable it.", android.widget.Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
                        context.startActivity(intent)
                    }
                }
            }
        )
    }
}

@Composable
fun TimeBox(valStr: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = valStr,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(t(label), fontSize = 10.sp, color = Color.LightGray)
    }
}

@Composable
fun SettingCard(
    title: String, desc: String, buttonText: String, isOn: Boolean, isWarning: Boolean, 
    icon: ImageVector = Icons.Filled.PlayArrow,
    onClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBlue),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = if (isOn) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (isOn) GreenText else RedAlert,
                    modifier = Modifier.size(24.dp).padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isOn) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = GreenText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = t("status_on"),
                                color = GreenText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                tint = RedAlert,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = t("status_off"),
                                color = RedAlert,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            if (title == t("guard_card_title") && !isOn) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(t("unprotected_inactive"), color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(start = 36.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(desc, color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(start = 36.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onClick?.invoke() },
                colors = ButtonDefaults.buttonColors(containerColor = if (isOn) GreenText else RedAlert),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonText)
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
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
                    .background(if (isUnlocked) CyanAccent.copy(alpha=0.1f) else Color.DarkGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (badge.imageUrl.isNotEmpty() && isUnlocked) {
                    BadgeImage(badge.imageUrl, modifier = Modifier.size(40.dp).clip(CircleShape), fallbackName = getCustomBadgeName(badge))
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
fun ResetDialog(onDismiss: () -> Unit, onOldDate: () -> Unit, onRightNow: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardBlue)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = RedAlert, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(t("start_where"), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    t("start_where_desc"),
                    textAlign = TextAlign.Center, color = Color.LightGray, fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onOldDate,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenText),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(t("choose_old_date"))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRightNow,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenText),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(t("right_now"))
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
        colors = DatePickerDefaults.colors(containerColor = CardBlue)
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = CardBlue,
                titleContentColor = Color.White,
                headlineContentColor = Color.White,
                weekdayContentColor = Color.LightGray,
                dayContentColor = Color.White,
                selectedDayContainerColor = CyanAccent,
                todayDateBorderColor = CyanAccent,
                todayContentColor = CyanAccent
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
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = CardBlue)) {
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
