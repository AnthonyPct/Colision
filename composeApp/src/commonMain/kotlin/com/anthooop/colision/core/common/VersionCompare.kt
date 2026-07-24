package com.anthooop.colision.core.common

/**
 * Minimal dotted-numeric version comparison ("1.2.10" > "1.2.9"). Non-numeric
 * suffixes (e.g. "-dev") are ignored per segment, and missing segments count
 * as 0 ("1.2" == "1.2.0"). Pure + testable, no SemVer pre-release handling
 * (not needed — store versions are plain x.y.z).
 */
object VersionCompare {

    fun parse(version: String): List<Int> =
        version.trim().split('.').map { segment ->
            segment.takeWhile { it.isDigit() }.toIntOrNull() ?: 0
        }

    fun compare(a: String, b: String): Int {
        val pa = parse(a)
        val pb = parse(b)
        val count = maxOf(pa.size, pb.size)
        for (i in 0 until count) {
            val x = pa.getOrElse(i) { 0 }
            val y = pb.getOrElse(i) { 0 }
            if (x != y) return x.compareTo(y)
        }
        return 0
    }

    /** True when [installed] is strictly older than [target]. */
    fun isOutdated(installed: String, target: String): Boolean =
        compare(installed, target) < 0
}
