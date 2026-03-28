package com.vavarda.clicker

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

private const val PROFILE_PHOTO_DIR = "profile"
private const val PROFILE_PHOTO_FILE = "profile_avatar.jpg"

internal data class PendingProfileCameraCapture(
    val tempFilePath: String,
    val outputUri: Uri
)

internal fun prepareProfileCameraCapture(context: Context): PendingProfileCameraCapture? {
    return runCatching {
        val tempDir = File(context.cacheDir, PROFILE_PHOTO_DIR).apply { mkdirs() }
        val tempFile = File.createTempFile("profile_capture_", ".jpg", tempDir)
        PendingProfileCameraCapture(
            tempFilePath = tempFile.absolutePath,
            outputUri = appFileProviderUri(context, tempFile)
        )
    }.getOrNull()
}

internal fun finalizeProfileCameraCapture(
    context: Context,
    tempFilePath: String
): String? {
    if (tempFilePath.isBlank()) return null
    return runCatching {
        val tempFile = File(tempFilePath)
        if (!tempFile.exists()) return null

        val targetDir = File(context.filesDir, PROFILE_PHOTO_DIR).apply { mkdirs() }
        val targetFile = File(targetDir, PROFILE_PHOTO_FILE)
        tempFile.copyTo(targetFile, overwrite = true)
        tempFile.delete()
        appFileProviderUri(context, targetFile).toString()
    }.getOrNull()
}

internal fun discardProfileCameraCapture(tempFilePath: String) {
    if (tempFilePath.isBlank()) return
    runCatching { File(tempFilePath).delete() }
}

internal fun clearStoredProfilePhoto(
    context: Context,
    uriString: String
) {
    if (uriString.isBlank()) return

    runCatching {
        context.contentResolver.releasePersistableUriPermission(
            Uri.parse(uriString),
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }

    if (isOwnedProfilePhoto(context, uriString)) {
        runCatching { File(context.filesDir, "$PROFILE_PHOTO_DIR/$PROFILE_PHOTO_FILE").delete() }
    }

    clearProfileImageCache(uriString)
}

private fun isOwnedProfilePhoto(context: Context, uriString: String): Boolean {
    return runCatching {
        Uri.parse(uriString).authority == "${context.packageName}.fileprovider"
    }.getOrDefault(false)
}

private fun appFileProviderUri(context: Context, file: File): Uri {
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}
