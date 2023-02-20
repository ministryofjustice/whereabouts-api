package uk.gov.justice.digital.hmpps.whereabouts.model

enum class CourtHearingType(val description: String) {
  APPEAL("Appeal"),
  APPLICATION("Application"),
  BACKER_TRIAL("Backer Trial"),
  BAIL("Bail"),
  CIVIL("Civil"),
  COMMITTAL_FOR_SENTENCE("Committal for Sentence (CSE)"),
  CUSTODY_TIME_LIMIT_APPLICATIONS("Custody Time Limit Applications (CTA)"),
  IMMIGRATION_DEPORTATION("Immigration/Deportation"),
  FAMILY("Family"),
  TRIAL("Trial"),
  FURTHER_CASE_MANAGEMENT("Further case Management Hearings (FCMH)"),
  FUTURE_TRIAL_REVIEW("Future Trial Review hearing (FTR)"),
  GROUND_RULES("Ground Rules Hearing (GRH)"),
  MENTION_DEFENDANT_MUST_ATTEND("Mention hearing Defendant must attend (MDA)"),
  MENTION_TO_FIX("Mention to Fix (MEF)"),
  NEWTON("Newton Hearing"),
  PLEA("Plea Hearing (PLE)"),
  PLEA_TRIAL_PREPARATION("Plea Trial and Preparation Hearing (PTPH)"),
  PRE_TRIAL_REVIEW("Pre-Trial Review (PTR)"),
  PROCEEDS_OF_CRIME_APPLICATIONS("Proceeds of Crime Applications (POCA)"),
  REMAND("Remand Hearing"),
  SECTION_28("Section 28"),
  SENTENCE("Sentence (SEN)"),
  TRIBUNAL("Tribunal"),
  OTHER("Other")
}
