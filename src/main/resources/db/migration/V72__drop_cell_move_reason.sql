/*
 * Drop CELL_MOVE_REASON (MAPA-282).
 *
 * This is the last of whereabouts-api's cell move functionality. The endpoints went first:
 * POST /cell/make-cell-move and GET /cell/cell-move-reasons in #1027, and the read endpoint
 * GET /cell/cell-move-reason/booking/{bookingId}/bed-assignment-sequence/{bedAssignmentId} in
 * #1029, which also removed the entity and repository. Nothing has referenced this table in code
 * since #1029 reached production on 2026-08-27.
 *
 * The rows are not being deleted so much as un-duplicated. Every one was copied into
 * hmpps-change-someones-cell-api's cell_movement_nomis table and reconciled against this table's
 * own count(*) - dev 1,460, preprod 3,481,920, prod 3,516,520 - with each source table re-counted
 * afterwards and found unchanged, which is what proves nothing was written below the sweep cursor.
 * Both consumers, hmpps-prisoner-profile and hmpps-change-someones-cell-api, have served this data
 * from there in production since 2026-08-26.
 *
 * Deliberately a separate release from #1029 rather than folded into it: while the table was still
 * here, a rollback of that code had something to roll back to. Once this runs, that option is gone
 * and hmpps-change-someones-cell-api is the sole holder of the data - which is the intended end
 * state, but is worth doing knowingly rather than as a side effect of a code change.
 *
 * IF EXISTS matches V6__drop_unused_offender_event_table.sql, the repo's other drop.
 */
DROP TABLE IF EXISTS CELL_MOVE_REASON;
