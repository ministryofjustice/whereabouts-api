package uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi;

import java.time.LocalDateTime;
import java.util.List;

public class CaseNoteDto {
    private long caseNoteId;
    private long bookingId;
    private long staffId;
    private String type;
    private String typeDescription;
    private String subType;
    private String subTypeDescription;
    private String source;
    private LocalDateTime creationDateTime;
    private LocalDateTime occurrenceDateTime;
    private String authorName;
    private String text;
    private String originalNoteText;
    private String agencyId;
    private List<AmendmentDto> amendments;

    public CaseNoteDto(long caseNoteId, long bookingId, long staffId, String type, String typeDescription, String subType, String subTypeDescription, String source, LocalDateTime creationDateTime, LocalDateTime occurrenceDateTime, String authorName, String text, String originalNoteText, String agencyId, List<AmendmentDto> amendments) {
        this.caseNoteId = caseNoteId;
        this.bookingId = bookingId;
        this.staffId = staffId;
        this.type = type;
        this.typeDescription = typeDescription;
        this.subType = subType;
        this.subTypeDescription = subTypeDescription;
        this.source = source;
        this.creationDateTime = creationDateTime;
        this.occurrenceDateTime = occurrenceDateTime;
        this.authorName = authorName;
        this.text = text;
        this.originalNoteText = originalNoteText;
        this.agencyId = agencyId;
        this.amendments = amendments;
    }

    public CaseNoteDto() {
    }

    public static CaseNoteDtoBuilder builder() {
        return new CaseNoteDtoBuilder();
    }

    public long getCaseNoteId() {
        return this.caseNoteId;
    }

    public long getBookingId() {
        return this.bookingId;
    }

    public long getStaffId() {
        return this.staffId;
    }

    public String getType() {
        return this.type;
    }

    public String getTypeDescription() {
        return this.typeDescription;
    }

    public String getSubType() {
        return this.subType;
    }

    public String getSubTypeDescription() {
        return this.subTypeDescription;
    }

    public String getSource() {
        return this.source;
    }

    public LocalDateTime getCreationDateTime() {
        return this.creationDateTime;
    }

    public LocalDateTime getOccurrenceDateTime() {
        return this.occurrenceDateTime;
    }

    public String getAuthorName() {
        return this.authorName;
    }

    public String getText() {
        return this.text;
    }

    public String getOriginalNoteText() {
        return this.originalNoteText;
    }

    public String getAgencyId() {
        return this.agencyId;
    }

    public List<AmendmentDto> getAmendments() {
        return this.amendments;
    }

    public void setCaseNoteId(long caseNoteId) {
        this.caseNoteId = caseNoteId;
    }

    public void setBookingId(long bookingId) {
        this.bookingId = bookingId;
    }

    public void setStaffId(long staffId) {
        this.staffId = staffId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTypeDescription(String typeDescription) {
        this.typeDescription = typeDescription;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public void setSubTypeDescription(String subTypeDescription) {
        this.subTypeDescription = subTypeDescription;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setCreationDateTime(LocalDateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public void setOccurrenceDateTime(LocalDateTime occurrenceDateTime) {
        this.occurrenceDateTime = occurrenceDateTime;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setOriginalNoteText(String originalNoteText) {
        this.originalNoteText = originalNoteText;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public void setAmendments(List<AmendmentDto> amendments) {
        this.amendments = amendments;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof CaseNoteDto)) return false;
        final CaseNoteDto other = (CaseNoteDto) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getCaseNoteId() != other.getCaseNoteId()) return false;
        if (this.getBookingId() != other.getBookingId()) return false;
        if (this.getStaffId() != other.getStaffId()) return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        final Object this$typeDescription = this.getTypeDescription();
        final Object other$typeDescription = other.getTypeDescription();
        if (this$typeDescription == null ? other$typeDescription != null : !this$typeDescription.equals(other$typeDescription))
            return false;
        final Object this$subType = this.getSubType();
        final Object other$subType = other.getSubType();
        if (this$subType == null ? other$subType != null : !this$subType.equals(other$subType)) return false;
        final Object this$subTypeDescription = this.getSubTypeDescription();
        final Object other$subTypeDescription = other.getSubTypeDescription();
        if (this$subTypeDescription == null ? other$subTypeDescription != null : !this$subTypeDescription.equals(other$subTypeDescription))
            return false;
        final Object this$source = this.getSource();
        final Object other$source = other.getSource();
        if (this$source == null ? other$source != null : !this$source.equals(other$source)) return false;
        final Object this$creationDateTime = this.getCreationDateTime();
        final Object other$creationDateTime = other.getCreationDateTime();
        if (this$creationDateTime == null ? other$creationDateTime != null : !this$creationDateTime.equals(other$creationDateTime))
            return false;
        final Object this$occurrenceDateTime = this.getOccurrenceDateTime();
        final Object other$occurrenceDateTime = other.getOccurrenceDateTime();
        if (this$occurrenceDateTime == null ? other$occurrenceDateTime != null : !this$occurrenceDateTime.equals(other$occurrenceDateTime))
            return false;
        final Object this$authorName = this.getAuthorName();
        final Object other$authorName = other.getAuthorName();
        if (this$authorName == null ? other$authorName != null : !this$authorName.equals(other$authorName))
            return false;
        final Object this$text = this.getText();
        final Object other$text = other.getText();
        if (this$text == null ? other$text != null : !this$text.equals(other$text)) return false;
        final Object this$originalNoteText = this.getOriginalNoteText();
        final Object other$originalNoteText = other.getOriginalNoteText();
        if (this$originalNoteText == null ? other$originalNoteText != null : !this$originalNoteText.equals(other$originalNoteText))
            return false;
        final Object this$agencyId = this.getAgencyId();
        final Object other$agencyId = other.getAgencyId();
        if (this$agencyId == null ? other$agencyId != null : !this$agencyId.equals(other$agencyId)) return false;
        final Object this$amendments = this.getAmendments();
        final Object other$amendments = other.getAmendments();
        if (this$amendments == null ? other$amendments != null : !this$amendments.equals(other$amendments))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof CaseNoteDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $caseNoteId = this.getCaseNoteId();
        result = result * PRIME + (int) ($caseNoteId >>> 32 ^ $caseNoteId);
        final long $bookingId = this.getBookingId();
        result = result * PRIME + (int) ($bookingId >>> 32 ^ $bookingId);
        final long $staffId = this.getStaffId();
        result = result * PRIME + (int) ($staffId >>> 32 ^ $staffId);
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        final Object $typeDescription = this.getTypeDescription();
        result = result * PRIME + ($typeDescription == null ? 43 : $typeDescription.hashCode());
        final Object $subType = this.getSubType();
        result = result * PRIME + ($subType == null ? 43 : $subType.hashCode());
        final Object $subTypeDescription = this.getSubTypeDescription();
        result = result * PRIME + ($subTypeDescription == null ? 43 : $subTypeDescription.hashCode());
        final Object $source = this.getSource();
        result = result * PRIME + ($source == null ? 43 : $source.hashCode());
        final Object $creationDateTime = this.getCreationDateTime();
        result = result * PRIME + ($creationDateTime == null ? 43 : $creationDateTime.hashCode());
        final Object $occurrenceDateTime = this.getOccurrenceDateTime();
        result = result * PRIME + ($occurrenceDateTime == null ? 43 : $occurrenceDateTime.hashCode());
        final Object $authorName = this.getAuthorName();
        result = result * PRIME + ($authorName == null ? 43 : $authorName.hashCode());
        final Object $text = this.getText();
        result = result * PRIME + ($text == null ? 43 : $text.hashCode());
        final Object $originalNoteText = this.getOriginalNoteText();
        result = result * PRIME + ($originalNoteText == null ? 43 : $originalNoteText.hashCode());
        final Object $agencyId = this.getAgencyId();
        result = result * PRIME + ($agencyId == null ? 43 : $agencyId.hashCode());
        final Object $amendments = this.getAmendments();
        result = result * PRIME + ($amendments == null ? 43 : $amendments.hashCode());
        return result;
    }

    public String toString() {
        return "CaseNoteDto(caseNoteId=" + this.getCaseNoteId() + ", bookingId=" + this.getBookingId() + ", staffId=" + this.getStaffId() + ", type=" + this.getType() + ", typeDescription=" + this.getTypeDescription() + ", subType=" + this.getSubType() + ", subTypeDescription=" + this.getSubTypeDescription() + ", source=" + this.getSource() + ", creationDateTime=" + this.getCreationDateTime() + ", occurrenceDateTime=" + this.getOccurrenceDateTime() + ", authorName=" + this.getAuthorName() + ", text=" + this.getText() + ", originalNoteText=" + this.getOriginalNoteText() + ", agencyId=" + this.getAgencyId() + ", amendments=" + this.getAmendments() + ")";
    }

    public CaseNoteDtoBuilder toBuilder() {
        return new CaseNoteDtoBuilder().caseNoteId(this.caseNoteId).bookingId(this.bookingId).staffId(this.staffId).type(this.type).typeDescription(this.typeDescription).subType(this.subType).subTypeDescription(this.subTypeDescription).source(this.source).creationDateTime(this.creationDateTime).occurrenceDateTime(this.occurrenceDateTime).authorName(this.authorName).text(this.text).originalNoteText(this.originalNoteText).agencyId(this.agencyId).amendments(this.amendments);
    }

    public static class CaseNoteDtoBuilder {
        private long caseNoteId;
        private long bookingId;
        private long staffId;
        private String type;
        private String typeDescription;
        private String subType;
        private String subTypeDescription;
        private String source;
        private LocalDateTime creationDateTime;
        private LocalDateTime occurrenceDateTime;
        private String authorName;
        private String text;
        private String originalNoteText;
        private String agencyId;
        private List<AmendmentDto> amendments;

        CaseNoteDtoBuilder() {
        }

        public CaseNoteDto.CaseNoteDtoBuilder caseNoteId(long caseNoteId) {
            this.caseNoteId = caseNoteId;
            return this;
        }

        public CaseNoteDto.CaseNoteDtoBuilder bookingId(long bookingId) {
            this.bookingId = bookingId;
            return this;
        }

        public CaseNoteDto.CaseNoteDtoBuilder staffId(long staffId) {
            this.staffId = staffId;
            return this;
        }

        public CaseNoteDto.CaseNoteDtoBuilder type(String type) {
            this.type = type;
            return this;
        }

        public CaseNoteDto.CaseNoteDtoBuilder typeDescription(String typeDescription) {
            this.typeDescription = typeDescription;
            return this;
        }

        public CaseNoteDto.CaseNoteDtoBuilder subType(String subType) {
            this.subType = subType;
            return this;
        }

        public CaseNoteDto.CaseNoteDtoBuilder subTypeDescription(String subTypeDescription) {
            this.subTypeDescription = subTypeDescription;
            return this;
        }

        public CaseNoteDto.CaseNoteDtoBuilder source(String source) {
            this.source = source;
            return this;
        }

        public CaseNoteDto.CaseNoteDtoBuilder creationDateTime(LocalDateTime creationDateTime) {
            this.creationDateTime = creationDateTime;
            return this;
        }

        public CaseNoteDto.CaseNoteDtoBuilder occurrenceDateTime(LocalDateTime occurrenceDateTime) {
            this.occurrenceDateTime = occurrenceDateTime;
            return this;
        }

        public CaseNoteDto.CaseNoteDtoBuilder authorName(String authorName) {
            this.authorName = authorName;
            return this;
        }

        public CaseNoteDto.CaseNoteDtoBuilder text(String text) {
            this.text = text;
            return this;
        }

        public CaseNoteDto.CaseNoteDtoBuilder originalNoteText(String originalNoteText) {
            this.originalNoteText = originalNoteText;
            return this;
        }

        public CaseNoteDto.CaseNoteDtoBuilder agencyId(String agencyId) {
            this.agencyId = agencyId;
            return this;
        }

        public CaseNoteDto.CaseNoteDtoBuilder amendments(List<AmendmentDto> amendments) {
            this.amendments = amendments;
            return this;
        }

        public CaseNoteDto build() {
            return new CaseNoteDto(caseNoteId, bookingId, staffId, type, typeDescription, subType, subTypeDescription, source, creationDateTime, occurrenceDateTime, authorName, text, originalNoteText, agencyId, amendments);
        }

        public String toString() {
            return "CaseNoteDto.CaseNoteDtoBuilder(caseNoteId=" + this.caseNoteId + ", bookingId=" + this.bookingId + ", staffId=" + this.staffId + ", type=" + this.type + ", typeDescription=" + this.typeDescription + ", subType=" + this.subType + ", subTypeDescription=" + this.subTypeDescription + ", source=" + this.source + ", creationDateTime=" + this.creationDateTime + ", occurrenceDateTime=" + this.occurrenceDateTime + ", authorName=" + this.authorName + ", text=" + this.text + ", originalNoteText=" + this.originalNoteText + ", agencyId=" + this.agencyId + ", amendments=" + this.amendments + ")";
        }
    }
}

