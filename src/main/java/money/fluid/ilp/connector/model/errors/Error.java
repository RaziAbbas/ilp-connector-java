package money.fluid.ilp.connector.model.errors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import money.fluid.ilp.connector.model.constants.ErrorConstants;

/**
 * An Exact-count Counter in instacount.
 */
@Getter
@ToString
@EqualsAndHashCode
public class Error {
    // A custom instacount error code. Not currently used.
    // @JsonProperty("instacount_code")
    // private final String instacountCode;

    // A user-friendly message that can be displayed to the user.
    @JsonProperty("message")
    private final String message;

    // A developer-friendly message with more details about what went wrong.
    @JsonProperty("developerMessage")
    private final String developerMessage;

    // A String with more information, possibly a link.
    @JsonProperty("moreInfo")
    private final String moreInfo;

    /**
     * Required-args Constructor.
     *
     * @param message
     * @param developerMessage
     * @param moreInfo
     */
    public Error(
            @JsonProperty("message") final String message,
            @JsonProperty("developerMessage") final String developerMessage,
            @JsonProperty("moreInfo") final String moreInfo
    ) {
        this.message = Preconditions.checkNotNull(message);
        this.developerMessage = Preconditions.checkNotNull(developerMessage);
        this.moreInfo = Preconditions.checkNotNull(moreInfo);
    }

    /**
     * Constructor for building from an instance of {@link Builder}.
     *
     * @param builder
     */
    private Error(final Builder builder) {
        Preconditions.checkNotNull(builder);

        this.message = builder.message;
        this.developerMessage = builder.developerMessage;
        this.moreInfo = builder.moreInfo;
    }

    /**
     * Construct a default instance of {@link Error}.
     *
     * @return
     */
    public static Error defaultError() {
        return new Error(ErrorConstants.DEFAULT_ERROR_MESSAGE, ErrorConstants.DEFAULT_DEVELOPER_ERROR_MESSAGE,
                         ErrorConstants.DEFAULT_MORE_INFO
        );
    }

    /**
     * A builder for constructing instances of {@link Error}.
     */
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Builder {
        private String message;
        private String developerMessage;
        private String moreInfo;

        // NOTE: No no-args constructor.  Start with the default if you need to tweak things.

        /**
         * Required-args Constructor. If you're not sure what to start with, start with {@link Error#defaultError()}.
         *
         * @param error
         */
        public Builder(final Error error) {
            Preconditions.checkNotNull(error);

            this.message = error.message;
            this.developerMessage = error.developerMessage;
            this.moreInfo = error.moreInfo;
        }

        /**
         * Builder method.
         *
         * @return
         */
        public Error build() {
            return new Error(this);
        }

        /**
         * @param message
         * @return
         */
        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * @param developerMessage
         * @return
         */
        public Builder withDeveloperMessage(String developerMessage) {
            this.developerMessage = developerMessage;
            return this;
        }

        /**
         * @param moreInfo
         * @return
         */
        public Builder withMoreInfo(String moreInfo) {
            this.moreInfo = moreInfo;
            return this;
        }
    }
}
