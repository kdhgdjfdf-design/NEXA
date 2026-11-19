package com.example

import com.example.data.local.FocusSessionEntity
import com.example.data.local.StudyFlashcardEntity
import com.example.data.local.TaskEntity
import com.example.domain.prayer.PrayerTimesCalculator
import com.example.domain.stats.XpAndStatsEngine
import com.example.domain.study.SpacedRepetitionEngine
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class NexaDomainUnitTest {

    @Test
    fun spacedRepetition_sm2IntervalsAndReset() {
        val base = StudyFlashcardEntity(id = 1L, subjectId = 1L, front = "F", back = "B")
        val r1 = SpacedRepetitionEngine.scheduleNext(base, recallQuality = 3, nowMs = 1_000_000L)
        assertEquals(1, r1.repetitions)
        assertEquals(1, r1.intervalDays)

        val r2 = SpacedRepetitionEngine.scheduleNext(r1, recallQuality = 3, nowMs = 2_000_000L)
        assertEquals(2, r2.repetitions)
        assertEquals(6, r2.intervalDays)

        val reset = SpacedRepetitionEngine.scheduleNext(r2, recallQuality = 1, nowMs = 3_000_000L)
        assertEquals(0, reset.repetitions)
        assertEquals(1, reset.intervalDays)
        assertEquals(3_000_000L + 10 * 60_000L, reset.nextReviewAt)
    }

    @Test
    fun spacedRepetition_noteSuggestionsAndApproximateRecall() {
        val notes = """
            التسارع: معدل تغير السرعة بالنسبة للزمن
            Force - Mass multiplied by acceleration
        """.trimIndent()
        val pairs = SpacedRepetitionEngine.suggestFlashcardsFromNotes(notes)
        assertEquals(2, pairs.size)
        assertEquals("التسارع", pairs[0].first)
        assertEquals("Force", pairs[1].first)

        val exactArabic = SpacedRepetitionEngine.evaluateRecallLocally(
            "مُعَدَّل تَغَيُّر السُّرْعَة",
            "معدل تغير السرعة"
        )
        assertTrue("Expected high normalized score, got $exactArabic", exactArabic >= 85)

        val empty = SpacedRepetitionEngine.evaluateRecallLocally("الخلية", "")
        assertEquals(0, empty)
    }

    @Test
    fun prayerTimes_orderAndQiblaCheck() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Riyadh")).apply {
            set(2025, Calendar.MARCH, 15, 12, 0, 0)
        }
        val riyadh = PrayerTimesCalculator.calculate(24.7136, 46.6753, "UMM_AL_QURA", cal)
        assertTrue(riyadh.fajrMinutes < riyadh.sunriseMinutes)
        assertTrue(riyadh.sunriseMinutes < riyadh.dhuhrMinutes)
        assertTrue(riyadh.dhuhrMinutes < riyadh.asrMinutes)
        assertTrue(riyadh.asrMinutes < riyadh.maghribMinutes)
        assertTrue(riyadh.maghribMinutes < riyadh.ishaMinutes)
        // Umm al-Qura Isha is +90 minutes after Maghrib
        assertEquals(riyadh.maghribMinutes + 90, riyadh.ishaMinutes)
        // Riyadh Qibla bearing is roughly West-Southwest (~240..255 deg)
        assertTrue("Riyadh qibla was ${riyadh.qiblaDegrees}", riyadh.qiblaDegrees in 230.0..265.0)
    }

    @Test
    fun xpAndStats_levelAndSelfComparison() {
        val lvl1 = XpAndStatsEngine.computeLevel(0)
        assertEquals(1, lvl1.level)
        val lvl3 = XpAndStatsEngine.computeLevel(100)
        assertEquals(3, lvl3.level)

        val now = 100L * 86_400_000L
        val tasks = listOf(
            TaskEntity(title = "T1", isCompleted = true, completedAt = now - 2 * 86_400_000L),
            TaskEntity(title = "T2", isCompleted = true, completedAt = now - 10 * 86_400_000L)
        )
        val sessions = listOf(
            FocusSessionEntity(elapsedFocusSeconds = 1800, startedAt = now - 86_400_000L),
            FocusSessionEntity(elapsedFocusSeconds = 600, startedAt = now - 9 * 86_400_000L)
        )
        val stats = XpAndStatsEngine.computeSelfComparison(tasks, sessions, emptyList(), now)
        assertEquals(1, stats.thisWeekTasksDone)
        assertEquals(1, stats.lastWeekTasksDone)
        assertEquals(30, stats.thisWeekFocusMinutes)
        assertEquals(10, stats.lastWeekFocusMinutes)
    }

    @Test
    fun sectionImageCodec_independentPersistenceAndFallback() {
        val initial = "{}"
        val withTasks = com.example.data.local.SectionImageCodec.put(initial, "tasks", "file:///tmp/tasks.jpg")
        val withStudy = com.example.data.local.SectionImageCodec.put(withTasks, "study", "file:///tmp/study.jpg")

        val decoded = com.example.data.local.SectionImageCodec.decode(withStudy)
        assertEquals(2, decoded.size)
        assertEquals("file:///tmp/tasks.jpg", decoded["tasks"])
        assertEquals("file:///tmp/study.jpg", decoded["study"])
        assertNull(decoded["habits"])

        // Restore tasks without disturbing study
        val afterRestore = com.example.data.local.SectionImageCodec.remove(withStudy, "tasks")
        val decodedAfter = com.example.data.local.SectionImageCodec.decode(afterRestore)
        assertNull(decodedAfter["tasks"])
        assertEquals("file:///tmp/study.jpg", decodedAfter["study"])

        // Malformed JSON safely yields empty map
        assertTrue(com.example.data.local.SectionImageCodec.decode("{invalid").isEmpty())

        // Missing file safely returns null so UI falls back to bundled drawable
        assertNull(com.example.data.local.SectionImageCodec.resolveExistingLocalFile("/nonexistent/missing_12345.jpg"))
    }

    @Test
    fun nexastrings_localizesSerenityPhrasesAchievementsAndRoutines() {
        val ar = com.example.domain.i18n.NexaStrings.forLang(com.example.domain.i18n.NexaLang.AR)
        val (tag, phrase) = ar.serenityPhraseFor(setOf("Tired"))
        assertEquals("متعب", tag)
        assertTrue(phrase.contains("التعب رسالة لطيفة"))

        assertEquals("أول خطوة", ar.achievementTitle("first_task"))
        assertEquals("قلب ذاكر", ar.achievementTitle("life_adhkar_7"))
        assertEquals("الصباح", ar.habitRoutineDisplay("Morning"))
        assertEquals("الدراسة", ar.habitRoutineDisplay("Study"))
        assertEquals("المساء", ar.habitRoutineDisplay("Evening"))

        val en = com.example.domain.i18n.NexaStrings.forLang(com.example.domain.i18n.NexaLang.EN)
        val fr = com.example.domain.i18n.NexaStrings.forLang(com.example.domain.i18n.NexaLang.FR)
        listOf(ar, en, fr).forEach { s ->
            assertEquals("NEXA", s.appName)
            assertTrue(s.offlineFirstNotice.contains("NEXA"))
            assertFalse(s.offlineFirstNotice.contains("NOX STUDY"))
            assertTrue(s.accessibilityRequiredHint.contains("NEXA"))
        }
        assertEquals(en.flashcards, en.reviewModeDisplay("FLASHCARD"))
        assertEquals(fr.testYourMemory, fr.reviewModeDisplay("TEST_MEMORY"))
    }

    @Test
    fun noxClassicPalette_maintainsWhiteBlackCoffeeLightAndDarkTokens() {
        val pal = com.example.ui.theme.NexaPalettes.noxEspresso
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFFFFFF), pal.light.surface)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFEFE6DC), pal.light.background)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF171310), pal.light.primary)

        assertEquals(androidx.compose.ui.graphics.Color(0xFF120E0C), pal.dark.background)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF1E1815), pal.dark.surface)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFAF6F0), pal.dark.primary)
    }
}
