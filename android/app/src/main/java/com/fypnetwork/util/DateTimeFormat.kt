package com.fypnetwork.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/**
 * The backend sends timestamps as ISO-8601 strings (e.g. "2026-08-01T14:32:00.000Z").
 * These helpers turn that into something a person would actually want to read,
 * instead of showing the raw ISO string in the UI.
 */
object DateTimeFormat {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy 'at' h:mm a")
    private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

    /** "3m ago", "5h ago", "2d ago", or a plain date once it's more than a week old. */
    fun relative(isoString: String): String {
        val instant = parse(isoString) ?: return isoString
        val now = Instant.now()
        val seconds = ChronoUnit.SECONDS.between(instant, now)

        return when {
            seconds < 0 -> "just now" // clock skew guard
            seconds < 60 -> "just now"
            seconds < 3600 -> "${seconds / 60}m ago"
            seconds < 86400 -> "${seconds / 3600}h ago"
            seconds < 7 * 86400 -> "${seconds / 86400}d ago"
            else -> dateFormatter.format(instant.atZone(ZoneId.systemDefault()))
        }
    }

    /** "1 Aug 2026 at 2:32 PM" - used where a precise timestamp matters more than relative time. */
    fun full(isoString: String): String {
        val instant = parse(isoString) ?: return isoString
        return dateTimeFormatter.format(instant.atZone(ZoneId.systemDefault()))
    }

    /** "1 Aug 2026" - used for due dates, where time-of-day isn't relevant. */
    fun dateOnly(isoString: String): String {
        val instant = parse(isoString) ?: return isoString
        return dateFormatter.format(instant.atZone(ZoneId.systemDefault()))
    }

    /** "Due in 3 days", "Due today", "Overdue by 2 days" - for task due dates. */
    fun countdown(isoString: String): String {
        val instant = parse(isoString) ?: return ""
        val now = Instant.now()
        val days = ChronoUnit.DAYS.between(now.atZone(ZoneId.systemDefault()).toLocalDate(), instant.atZone(ZoneId.systemDefault()).toLocalDate())

        return when {
            days < 0 -> "Overdue by ${-days} day${if (-days == 1L) "" else "s"}"
            days == 0L -> "Due today"
            days == 1L -> "Due tomorrow"
            else -> "Due in $days days"
        }
    }

    fun isOverdue(isoString: String): Boolean {
        val instant = parse(isoString) ?: return false
        return instant.isBefore(Instant.now())
    }

    private fun parse(isoString: String): Instant? = try {
        Instant.parse(isoString)
    } catch (e: DateTimeParseException) {
        null
    }
}
