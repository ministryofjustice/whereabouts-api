package uk.gov.justice.digital.hmpps.whereabouts.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private Integer status;
    private Integer errorCode;
    private String userMessage;
    private String developerMessage;
    private String moreInfo;

    public ErrorResponse(Integer status, Integer errorCode, String userMessage, String developerMessage, String moreInfo) {
        this.status = status;
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.developerMessage = developerMessage;
        this.moreInfo = moreInfo;
    }

    public ErrorResponse() {
    }

    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    public Integer getStatus() {
        return this.status;
    }

    public Integer getErrorCode() {
        return this.errorCode;
    }

    public String getUserMessage() {
        return this.userMessage;
    }

    public String getDeveloperMessage() {
        return this.developerMessage;
    }

    public String getMoreInfo() {
        return this.moreInfo;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public void setDeveloperMessage(String developerMessage) {
        this.developerMessage = developerMessage;
    }

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ErrorResponse)) return false;
        final ErrorResponse other = (ErrorResponse) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
        final Object this$errorCode = this.getErrorCode();
        final Object other$errorCode = other.getErrorCode();
        if (this$errorCode == null ? other$errorCode != null : !this$errorCode.equals(other$errorCode)) return false;
        final Object this$userMessage = this.getUserMessage();
        final Object other$userMessage = other.getUserMessage();
        if (this$userMessage == null ? other$userMessage != null : !this$userMessage.equals(other$userMessage))
            return false;
        final Object this$developerMessage = this.getDeveloperMessage();
        final Object other$developerMessage = other.getDeveloperMessage();
        if (this$developerMessage == null ? other$developerMessage != null : !this$developerMessage.equals(other$developerMessage))
            return false;
        final Object this$moreInfo = this.getMoreInfo();
        final Object other$moreInfo = other.getMoreInfo();
        if (this$moreInfo == null ? other$moreInfo != null : !this$moreInfo.equals(other$moreInfo)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ErrorResponse;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        final Object $errorCode = this.getErrorCode();
        result = result * PRIME + ($errorCode == null ? 43 : $errorCode.hashCode());
        final Object $userMessage = this.getUserMessage();
        result = result * PRIME + ($userMessage == null ? 43 : $userMessage.hashCode());
        final Object $developerMessage = this.getDeveloperMessage();
        result = result * PRIME + ($developerMessage == null ? 43 : $developerMessage.hashCode());
        final Object $moreInfo = this.getMoreInfo();
        result = result * PRIME + ($moreInfo == null ? 43 : $moreInfo.hashCode());
        return result;
    }

    public String toString() {
        return "ErrorResponse(status=" + this.getStatus() + ", errorCode=" + this.getErrorCode() + ", userMessage=" + this.getUserMessage() + ", developerMessage=" + this.getDeveloperMessage() + ", moreInfo=" + this.getMoreInfo() + ")";
    }

    public static class ErrorResponseBuilder {
        private Integer status;
        private Integer errorCode;
        private String userMessage;
        private String developerMessage;
        private String moreInfo;

        ErrorResponseBuilder() {
        }

        public ErrorResponse.ErrorResponseBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public ErrorResponse.ErrorResponseBuilder errorCode(Integer errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ErrorResponse.ErrorResponseBuilder userMessage(String userMessage) {
            this.userMessage = userMessage;
            return this;
        }

        public ErrorResponse.ErrorResponseBuilder developerMessage(String developerMessage) {
            this.developerMessage = developerMessage;
            return this;
        }

        public ErrorResponse.ErrorResponseBuilder moreInfo(String moreInfo) {
            this.moreInfo = moreInfo;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(status, errorCode, userMessage, developerMessage, moreInfo);
        }

        public String toString() {
            return "ErrorResponse.ErrorResponseBuilder(status=" + this.status + ", errorCode=" + this.errorCode + ", userMessage=" + this.userMessage + ", developerMessage=" + this.developerMessage + ", moreInfo=" + this.moreInfo + ")";
        }
    }
}
