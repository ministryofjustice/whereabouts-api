package uk.gov.justice.digital.hmpps.whereabouts.services.court

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventWithRoomNames
import java.io.StringWriter

@Component
class EventToCsvConverter {
  fun toCsv(events: List<VideoLinkBookingEventWithRoomNames>): String {
    val stringWriter = StringWriter()
    val sequenceWriter = csvWriter.writeValues(stringWriter)
    events.forEach { e ->
      sequenceWriter.write(e)
    }
    sequenceWriter.close()
    return stringWriter.toString()
  }

  companion object {

    private val csvWriter: ObjectWriter = CsvMapper()
      .findAndRegisterModules()
      .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
      .configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
      .writer(
        CsvSchema.builder()
          .setUseHeader(true)
          .addColumn("eventId", CsvSchema.ColumnType.NUMBER)
          .addColumn("timestamp")
          .addColumn("videoLinkBookingId", CsvSchema.ColumnType.NUMBER)
          .addColumn("eventType")
          .addColumn("agencyId")
          .addColumn("court")
          .addColumn("courtId")
          .addColumn("madeByTheCourt", CsvSchema.ColumnType.BOOLEAN)
          .addColumn("mainStartTime")
          .addColumn("mainEndTime")
          .addColumn("preStartTime")
          .addColumn("preEndTime")
          .addColumn("postStartTime")
          .addColumn("postEndTime")
          .addColumn("mainLocationName")
          .addColumn("preLocationName")
          .addColumn("postLocationName")
          .build(),
      ).with(JsonGenerator.Feature.IGNORE_UNKNOWN)
  }
}
