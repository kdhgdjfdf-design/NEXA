package com.example.domain.study

import com.example.data.local.StudyFlashcardEntity
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 100% Local, Non-AI Study Engine:
 * - SM-2 Spaced Repetition scheduling
 * - Rule-based local flashcard extraction from student notes
 * - Approximate string-similarity memory quiz evaluation (explicitly non-AI)
 */
object SpacedRepetitionEngine {

    /**
     * recallQuality: 1 = Again, 2 = Hard, 3 = Good, 4 = Easy
     */
    fun scheduleNext(card: StudyFlashcardEntity, recallQuality: Int, nowMs: Long = System.currentTimeMillis()): StudyFlashcardEntity {
        val q = when (recallQuality) {
            1 -> 1
            2 -> 3
            3 -> 4
            4 -> 5
            else -> 4
        }

        val newReps: Int
        val newInterval: Int
        if (q < 3) {
            newReps = 0
            newInterval = 1
        } else {
            newReps = card.repetitions + 1
            newInterval = when (newReps) {
                1 -> 1
                2 -> 6
                else -> (card.intervalDays * card.easeFactor).roundToInt().coerceAtLeast(1)
            }
        }

        val efDelta = 0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)
        val newEf = (card.easeFactor + efDelta).coerceAtLeast(1.3)
        val dayMs = 86_400_000L
        val nextReview = if (recallQuality == 1) {
            nowMs + 10 * 60_000L // 10 minutes retry
        } else {
            nowMs + newInterval * dayMs
        }

        return card.copy(
            easeFactor = newEf,
            intervalDays = newInterval,
            repetitions = newReps,
            nextReviewAt = nextReview,
            lastRecallLevel = recallQuality
        )
    }

    /**
     * Local rule-based flashcard suggestion generator (zero AI).
     * Parses lines formatted like "Term: Definition", "Term - Definition", or "Q? A".
     */
    fun suggestFlashcardsFromNotes(notes: String): List<Pair<String, String>> {
        if (notes.isBlank()) return emptyList()
        val suggestions = mutableListOf<Pair<String, String>>()
        val lines = notes.lines().map { it.trim() }.filter { it.length >= 5 }
        for (line in lines) {
            val colonIdx = line.indexOf(':')
            val dashIdx = line.indexOf(" - ")
            val arCommaIdx = line.indexOf("؛")
            when {
                colonIdx in 2 until line.length - 2 -> {
                    val front = line.substring(0, colonIdx).trim()
                    val back = line.substring(colonIdx + 1).trim()
                    if (front.isNotBlank() && back.isNotBlank()) suggestions += front to back
                }
                dashIdx in 2 until line.length - 4 -> {
                    val front = line.substring(0, dashIdx).trim()
                    val back = line.substring(dashIdx + 3).trim()
                    if (front.isNotBlank() && back.isNotBlank()) suggestions += front to back
                }
                arCommaIdx in 2 until line.length - 2 -> {
                    val front = line.substring(0, arCommaIdx).trim()
                    val back = line.substring(arCommaIdx + 1).trim()
                    if (front.isNotBlank() && back.isNotBlank()) suggestions += front to back
                }
            }
        }
        return suggestions.distinctBy { it.first }
    }

    /**
     * Local non-AI "Test Your Memory" evaluator.
     * Computes normalized keyword overlap + Levenshtein similarity.
     * Results are strictly approximate (تقييم تقريبي محلي).
     */
    fun evaluateRecallLocally(expected: String, userAnswer: String): Int {
        val normExpected = normalizeText(expected)
        val normUser = normalizeText(userAnswer)
        if (normUser.isBlank() || normExpected.isBlank()) return 0
        if (normExpected == normUser) return 100

        val expectedTokens = normExpected.split(" ").filter { it.length > 1 }.toSet()
        val userTokens = normUser.split(" ").filter { it.length > 1 }.toSet()
        val tokenScore = if (expectedTokens.isEmpty()) 0.0 else {
            val matches = expectedTokens.count { exp ->
                userTokens.any { usr -> usr == exp || levenshteinSimilarity(exp, usr) >= 0.78 }
            }
            matches.toDouble() / expectedTokens.size.toDouble()
        }
        val charScore = levenshteinSimilarity(normExpected, normUser)
        val combined = (tokenScore * 0.65 + charScore * 0.35) * 100.0
        return combined.roundToInt().coerceIn(0, 100)
    }

    private fun normalizeText(input: String): String {
        // Strip Arabic diacritics and normalize alef/teh marbuta + punctuation
        val noDiacritics = input.replace(Regex("[\u064B-\u065F\u0670]"), "")
        return noDiacritics
            .lowercase()
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ة', 'ه')
            .replace('ى', 'ي')
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun levenshteinSimilarity(a: String, b: String): Double {
        if (a.isEmpty() && b.isEmpty()) return 1.0
        val maxLen = max(a.length, b.length)
        if (maxLen == 0) return 1.0
        val dp = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..b.length) {
                val temp = dp[j]
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[j] = min(min(dp[j] + 1, dp[j - 1] + 1), prev + cost)
                prev = temp
            }
        }
        return 1.0 - (dp[b.length].toDouble() / maxLen.toDouble())
    }
}
