package com.example.domain.prayer

import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

data class PrayerTimesResult(
    val fajrMinutes: Int,
    val sunriseMinutes: Int,
    val dhuhrMinutes: Int,
    val asrMinutes: Int,
    val maghribMinutes: Int,
    val ishaMinutes: Int,
    val qiblaDegrees: Double
) {
    fun formatTime(totalMinutes: Int): String {
        val norm = ((totalMinutes % 1440) + 1440) % 1440
        val h = norm / 60
        val m = norm % 60
        return String.format("%02d:%02d", h, m)
    }

    fun nextPrayer(nowMinutes: Int): Pair<String, Int> {
        val list = listOf(
            "FAJR" to fajrMinutes,
            "SUNRISE" to sunriseMinutes,
            "DHUHR" to dhuhrMinutes,
            "ASR" to asrMinutes,
            "MAGHRIB" to maghribMinutes,
            "ISHA" to ishaMinutes
        )
        return list.firstOrNull { it.second > nowMinutes } ?: ("FAJR" to (fajrMinutes + 1440))
    }
}

data class HijriDateInfo(
    val day: Int,
    val month: Int,
    val year: Int,
    val monthNameAr: String,
    val monthNameEn: String,
    val monthNameFr: String
)

data class PresetCity(
    val nameAr: String,
    val nameEn: String,
    val nameFr: String,
    val lat: Double,
    val lon: Double
)

object PrayerTimesCalculator {
    val presetCities = listOf(
        PresetCity("الرياض", "Riyadh", "Riyad", 24.7136, 46.6753),
        PresetCity("مكة المكرمة", "Makkah", "La Mecque", 21.3891, 39.8579),
        PresetCity("المدينة المنورة", "Madinah", "Médine", 24.5247, 39.5692),
        PresetCity("القاهرة", "Cairo", "Le Caire", 30.0444, 31.2357),
        PresetCity("دبي", "Dubai", "Dubaï", 25.2048, 55.2708),
        PresetCity("عمّان", "Amman", "Amman", 31.9454, 35.9284),
        PresetCity("الدار البيضاء", "Casablanca", "Casablanca", 33.5731, -7.5898),
        PresetCity("الرباط", "Rabat", "Rabat", 34.0209, -6.8416),
        PresetCity("فاس", "Fes", "Fès", 34.0331, -5.0003),
        PresetCity("مكناس", "Meknes", "Meknès", 33.8935, -5.5473),
        PresetCity("مراكش", "Marrakesh", "Marrakech", 31.6295, -7.9811),
        PresetCity("طنجة", "Tangier", "Tanger", 35.7595, -5.8340),
        PresetCity("تطوان", "Tetouan", "Tétouan", 35.5889, -5.3626),
        PresetCity("أكادير", "Agadir", "Agadir", 30.4278, -9.5981),
        PresetCity("وجدة", "Oujda", "Oujda", 34.6814, -1.9086),
        PresetCity("القنيطرة", "Kenitra", "Kénitra", 34.2610, -6.5802),
        PresetCity("آسفي", "Safi", "Safi", 32.2994, -9.2372),
        PresetCity("الجزائر", "Algiers", "Alger", 36.7538, 3.0588),
        PresetCity("تونس", "Tunis", "Tunis", 36.8065, 10.1815),
        PresetCity("باريس", "Paris", "Paris", 48.8566, 2.3522),
        PresetCity("لندن", "London", "Londres", 51.5072, -0.1276),
        PresetCity("كوالالمبور", "Kuala Lumpur", "Kuala Lumpur", 3.1390, 101.6869)
    )

    fun calculate(
        lat: Double,
        lon: Double,
        method: String,
        calendar: Calendar = Calendar.getInstance()
    ): PrayerTimesResult {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val tzHours = calendar.timeZone.getOffset(calendar.timeInMillis) / 3600000.0

        val jd = julianDate(year, month, day) - lon / (15.0 * 24.0)
        val (fajrAngle, ishaAngle, ishaFixedMinutes) = when (method) {
            "UMM_AL_QURA" -> Triple(18.5, 0.0, 90)
            "MWL" -> Triple(18.0, 17.0, 0)
            "EGYPTIAN" -> Triple(19.5, 17.5, 0)
            "ISNA" -> Triple(15.0, 15.0, 0)
            "KARACHI" -> Triple(18.0, 18.0, 0)
            else -> Triple(18.5, 0.0, 90)
        }

        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sinDeg(g) + 0.020 * sinDeg(2 * g))
        val e = 23.439 - 0.00000036 * d
        val ra = atan2Deg(cosDeg(e) * sinDeg(l), cosDeg(l)) / 15.0
        val eqTime = q / 15.0 - fixHour(ra)
        val decl = asinDeg(sinDeg(e) * sinDeg(l))

        val dhuhrHour = fixHour(12.0 + tzHours - lon / 15.0 - eqTime)
        val sunriseDiff = sunAngleTime(0.833, lat, decl)
        val fajrDiff = sunAngleTime(fajrAngle, lat, decl)
        val asrDiff = asrTime(1.0, lat, decl)

        val sunriseHour = dhuhrHour - sunriseDiff
        val maghribHour = dhuhrHour + sunriseDiff
        val fajrHour = dhuhrHour - fajrDiff
        val asrHour = dhuhrHour + asrDiff
        val ishaHour = if (ishaFixedMinutes > 0) {
            maghribHour + ishaFixedMinutes / 60.0
        } else {
            dhuhrHour + sunAngleTime(ishaAngle, lat, decl)
        }

        return PrayerTimesResult(
            fajrMinutes = (fajrHour * 60).toInt(),
            sunriseMinutes = (sunriseHour * 60).toInt(),
            dhuhrMinutes = (dhuhrHour * 60).toInt(),
            asrMinutes = (asrHour * 60).toInt(),
            maghribMinutes = (maghribHour * 60).toInt(),
            ishaMinutes = (ishaHour * 60).toInt(),
            qiblaDegrees = calculateQibla(lat, lon)
        )
    }

    fun getHijriDate(calendar: Calendar = Calendar.getInstance()): HijriDateInfo {
        val hijri = IslamicCalendar(ULocale("@calendar=islamic-umalqura"))
        hijri.timeInMillis = calendar.timeInMillis
        val day = hijri.get(IslamicCalendar.DAY_OF_MONTH)
        val month = hijri.get(IslamicCalendar.MONTH) + 1
        val year = hijri.get(IslamicCalendar.YEAR)

        val monthsAr = listOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
            "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
            "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
        )
        val monthsEn = listOf(
            "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
            "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
            "Ramadan", "Shawwal", "Dhul-Qi'dah", "Dhul-Hijjah"
        )
        val monthsFr = listOf(
            "Mouharram", "Safar", "Rabi' al-Awwal", "Rabi' ath-Thani",
            "Joumada al-Oula", "Joumada ath-Thaniya", "Rajab", "Cha'ban",
            "Ramadan", "Chawwal", "Dhou al-Qi'da", "Dhou al-Hijja"
        )
        val idx = (month - 1).coerceIn(0, 11)
        return HijriDateInfo(day, month, year, monthsAr[idx], monthsEn[idx], monthsFr[idx])
    }

    private fun calculateQibla(lat: Double, lon: Double): Double {
        val makkahLat = Math.toRadians(21.4225)
        val makkahLon = Math.toRadians(39.8262)
        val phi = Math.toRadians(lat)
        val deltaLon = makkahLon - Math.toRadians(lon)
        val y = sin(deltaLon)
        val x = cos(phi) * tan(makkahLat) - sin(phi) * cos(deltaLon)
        val deg = Math.toDegrees(atan2(y, x))
        return (deg + 360.0) % 360.0
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunAngleTime(angle: Double, lat: Double, decl: Double): Double {
        val num = -sinDeg(angle) - sinDeg(lat) * sinDeg(decl)
        val den = cosDeg(lat) * cosDeg(decl)
        val ratio = (num / den).coerceIn(-1.0, 1.0)
        return acosDeg(ratio) / 15.0
    }

    private fun asrTime(factor: Double, lat: Double, decl: Double): Double {
        val angle = -Math.toDegrees(atan2(1.0, factor + tan(Math.toRadians(kotlin.math.abs(lat - decl)))))
        return sunAngleTime(angle, lat, decl)
    }

    private fun sinDeg(d: Double) = sin(d * PI / 180.0)
    private fun cosDeg(d: Double) = cos(d * PI / 180.0)
    private fun asinDeg(x: Double) = Math.toDegrees(asin(x.coerceIn(-1.0, 1.0)))
    private fun acosDeg(x: Double) = Math.toDegrees(acos(x.coerceIn(-1.0, 1.0)))
    private fun atan2Deg(y: Double, x: Double) = Math.toDegrees(atan2(y, x))
    private fun fixAngle(a: Double): Double {
        var res = a % 360.0
        if (res < 0) res += 360.0
        return res
    }
    private fun fixHour(h: Double): Double {
        var res = h % 24.0
        if (res < 0) res += 24.0
        return res
    }
}
