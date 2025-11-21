package com.blog.myandroidblog.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

object BooleanIntSerializer : KSerializer<Boolean?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BooleanIntSerializer", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): Boolean? {
        val jsonDecoder = decoder as? JsonDecoder ?: return null
        val element = jsonDecoder.decodeJsonElement()
        return try {
            val s = element.jsonPrimitive.content.lowercase()
            when (s) {
                "true", "1", "yes" -> true
                "false", "0", "no" -> false
                else -> null
            }
        } catch (_: Exception) { null }
    }
    override fun serialize(encoder: Encoder, value: Boolean?) {
        encoder.encodeBoolean(value ?: false)
    }
}

@Serializable
data class VersionInfo(
    val id: Int? = null,
    val version_code: Int,
    val version_name: String,
    val apk_url: String,
    val release_notes: String? = null,
    val min_sdk_version: Int? = null,
    val target_sdk_version: Int? = null,
    val file_size: Long? = null,
    @Serializable(with = BooleanIntSerializer::class)
    val is_force_update: Boolean? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class VersionLatestResponse(
    val success: Boolean,
    val data: VersionInfo?
)

@Serializable
data class VersionCheckResponse(
    val success: Boolean,
    val data: VersionCheckData?
)

@Serializable
data class VersionCheckData(
    val hasUpdate: Boolean,
    val latestVersion: VersionInfo? = null,
    val message: String? = null
)
