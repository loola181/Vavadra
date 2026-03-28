package com.vavarda.clicker

private const val PROFILE_NAME_MAX_LENGTH = 24
private val profileNameWhitespace = "\\s+".toRegex()

internal fun normalizeProfileName(raw: String): String {
    return raw
        .replace(profileNameWhitespace, " ")
        .trim()
        .take(PROFILE_NAME_MAX_LENGTH)
}

internal fun resolvedProfileName(raw: String, fallback: String): String {
    val normalized = normalizeProfileName(raw)
    return if (normalized.isBlank()) fallback else normalized
}
