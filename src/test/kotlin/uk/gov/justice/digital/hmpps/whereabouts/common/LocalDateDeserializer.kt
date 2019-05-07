package uk.gov.justice.digital.hmpps.whereabouts.common

import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDate

private class LocalDateDeserializer : JsonDeserializer<LocalDate> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext?): LocalDate {
        return LocalDate.parse(json.getAsJsonPrimitive().getAsString())
    }
}


fun getGson() : Gson {
    return GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateDeserializer()).create()
}
