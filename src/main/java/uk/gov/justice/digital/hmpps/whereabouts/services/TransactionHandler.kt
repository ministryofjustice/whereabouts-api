package uk.gov.justice.digital.hmpps.whereabouts.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Supplier

/**
 * A utility service which provides a simple way of defining a transaction boundary for service methods
 * that require the boundary to be smaller than the method itself.  See CourtService#createVideoLinkBooking for
 * an example.
 */
@Service
class TransactionHandler {
  @Transactional
  fun <T> runInTransaction(supplier: Supplier<T>): T {
    return supplier.get()
  }
}
