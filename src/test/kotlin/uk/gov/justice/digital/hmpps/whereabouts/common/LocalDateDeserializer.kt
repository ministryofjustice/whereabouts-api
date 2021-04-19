package uk.gov.justice.digital.hmpps.whereabouts.common

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private class LocalDateDeserializer : JsonDeserializer<LocalDate> {
  override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext?): LocalDate {
    return LocalDate.parse(json.getAsJsonPrimitive().getAsString())
  }
}

private class LocalDateTimeDeserializer : JsonSerializer<LocalDateTime?>, JsonDeserializer<LocalDateTime> {
  override fun serialize(
    localDateTime: LocalDateTime?,
    srcType: Type,
    context: JsonSerializationContext
  ): JsonElement {
    return JsonPrimitive(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime))
  }

  override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LocalDateTime {
    return LocalDateTime.parse(
      json.asString,
      DateTimeFormatter.ISO_LOCAL_DATE_TIME.withLocale(Locale.ENGLISH)
    )
  }
}

fun getGson(): Gson {
  return GsonBuilder()
    .registerTypeAdapter(LocalDate::class.java, LocalDateDeserializer())
    .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
    .create()
}
