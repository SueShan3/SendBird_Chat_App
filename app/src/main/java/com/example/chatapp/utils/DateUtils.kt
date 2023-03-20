package com.example.chatapp.utils

import java.text.SimpleDateFormat
import java.util.*

class DateUtils {
    companion object {
        /**
         * Gets timestamp in millis and converts it to HH:mm (e.g. 16:44).
         */
        fun formatTime(timeInMillis: Long): String? {
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            return dateFormat.format(timeInMillis)
        }

        /**
         * Formats timestamp to 'date month' format (e.g. 'February 3').
         */
        fun formatDate(timeInMillis: Long): String? {
            val dateFormat = SimpleDateFormat("MMMM dd", Locale.getDefault())
            return dateFormat.format(timeInMillis)
        }

        /**
         * Checks if two dates are of the same day.
         * @param millisFirst   The time in milliseconds of the first date.
         * @param millisSecond  The time in milliseconds of the second date.
         * @return  Whether {@param millisFirst} and {@param millisSecond} are off the same day.
         */
        fun hasSameDate(millisFirst: Long, millisSecond: Long): Boolean {
            val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            return dateFormat.format(millisFirst) == dateFormat.format(millisSecond)
        }
    }
}