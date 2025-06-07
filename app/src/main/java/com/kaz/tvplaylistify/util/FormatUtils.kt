package com.kaz.tvplaylistify.util

object FormatUtils {
    fun parseDurationToMillis(iso: String?): Long {
        if (iso.isNullOrBlank()) return 0L
        val regex = Regex("""PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?""")
        val match = regex.find(iso) ?: return 0L
        val h = match.groupValues[1].toIntOrNull() ?: 0
        val m = match.groupValues[2].toIntOrNull() ?: 0
        val s = match.groupValues[3].toIntOrNull() ?: 0
        return ((h * 3600 + m * 60 + s) * 1000).toLong()
    }

    fun formatDurationToIso(ms: Long): String {
        val totalSeconds = ms / 1000
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return buildString {
            append("PT")
            if (h > 0) append("${h}H")
            if (m > 0) append("${m}M")
            if (s > 0 || (h == 0L && m == 0L)) append("${s}S")
        }
    }
}