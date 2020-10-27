package uk.gov.justice.digital.hmpps.whereabouts.common

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.time.LocalDate

private class LocalDateDeserializer : JsonDeserializer<LocalDate> {
  override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext?): LocalDate {
    return LocalDate.parse(json.getAsJsonPrimitive().getAsString())
  }
}

fun getGson(): Gson {
  return GsonBuilder()
    .registerTypeAdapter(LocalDate::class.java, LocalDateDeserializer())
    .create()
}
