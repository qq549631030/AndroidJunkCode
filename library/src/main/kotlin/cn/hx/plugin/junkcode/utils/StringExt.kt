package cn.hx.plugin.junkcode.utils

import java.util.Locale

fun String.capitalizeCompat(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}