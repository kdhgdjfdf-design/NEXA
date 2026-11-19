package com.example.data.local

import android.content.Context
import android.net.Uri
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object SectionImageCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun decode(sectionImagesJson: String?): Map<String, String> {
        if (sectionImagesJson.isNullOrBlank()) return emptyMap()
        return runCatching {
            json.decodeFromString<Map<String, String>>(sectionImagesJson)
        }.getOrDefault(emptyMap())
    }

    fun put(sectionImagesJson: String?, sectionKey: String, uri: String): String {
        val mutable = decode(sectionImagesJson).toMutableMap()
        if (uri.isBlank()) {
            mutable.remove(sectionKey)
        } else {
            mutable[sectionKey] = uri
        }
        return json.encodeToString(mutable)
    }

    fun remove(sectionImagesJson: String?, sectionKey: String): String {
        val mutable = decode(sectionImagesJson).toMutableMap()
        mutable.remove(sectionKey)
        return json.encodeToString(mutable)
    }

    fun resolveExistingLocalFile(uriString: String?): File? {
        if (uriString.isNullOrBlank()) return null
        val path = runCatching {
            val parsed = Uri.parse(uriString)
            if (parsed.scheme == "file") parsed.path else uriString
        }.getOrNull() ?: return null
        val file = File(path)
        return if (file.exists() && file.length() > 0L) file else null
    }

    fun deleteManagedFileIfOwned(context: Context, uriString: String?) {
        val file = resolveExistingLocalFile(uriString) ?: return
        val managedDir = File(context.filesDir, "custom_images")
        if (file.parentFile?.absolutePath == managedDir.absolutePath) {
            runCatching { file.delete() }
        }
    }
}
