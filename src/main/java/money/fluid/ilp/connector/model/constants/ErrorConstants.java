package money.fluid.ilp.connector.model.constants;


import money.fluid.ilp.connector.model.constants.HeaderConstants.ApiVersions;

/**
 * Constants for storing error messages.
 */
public class ErrorConstants {
    public static final int DEFAULT_STATUS = 500;
    public static final String DEFAULT_ERROR_MESSAGE = "Oops, Instacount had a problem!  Please try your request again.";
    public static final String DEFAULT_DEVELOPER_ERROR_MESSAGE = DEFAULT_ERROR_MESSAGE;
    public static final String DEFAULT_MORE_INFO = "https://instacount.readme.io";

    // 400
    public static final String BAD_REQUEST__MESSAGE = "Your request was not valid.";
    // No Developer Message here. See IllegalArgumentExceptionMapper for more details.
    public static final String BAD_REQUEST__MORE_INFO = DEFAULT_MORE_INFO;

    // 401
    public static final String NOT_AUTHENTICATED__MESSAGE = "You must be signed-in to access the information you requested.";
    public static final String NOT_AUTHENTICATED__DEVELOPER_MESSAGE = "The request must be made with proper Instacount Authentication credentials!";
    public static final String NOT_AUTHENTICATED__MORE_INFO = DEFAULT_MORE_INFO;

    // 403
    public static final String UNAUTHORIZED__MESSAGE = "Uh-oh, it seems you don't have permission to do that.";
    public static final String UNAUTHORIZED__DEVELOPER_MESSAGE = "The request was not authorized to access this resource.";
    public static final String UNAUTHORIZED__MORE_INFO = DEFAULT_MORE_INFO;

    // 403
    public static final String OVER_QUOTA__MESSAGE = "Uh-oh, you're out of quota.";
    public static final String OVER_QUOTA__DEVELOPER_MESSAGE = "The request was not allowed because your account does not have sufficent quota.  Please upgrade your account at https://www.instacount.io";
    public static final String OVER_QUOTA__MORE_INFO = DEFAULT_MORE_INFO;

    // 404
    public static final String NOT_FOUND__MESSAGE = "What you requested does not exist.";
    public static final String NOT_FOUND__DEVELOPER_MESSAGE = "The requested resource was not found!";
    public static final String NOT_FOUND__MORE_INFO = DEFAULT_MORE_INFO;

    // 405
    public static final String INVALID_HTTP_METHOD__MESSAGE = NOT_FOUND__MESSAGE;
    public static final String INVALID_HTTP_METHOD__DEVELOPER_MESSAGE = "The specified HTTP method is not allowed for the resource identified by the Request-URI.";
    public static final String INVALID_HTTP_METHOD__MORE_INFO = DEFAULT_MORE_INFO;

    // 406
    public static final String INVALID_HTTP_NOT_ACCEPTABLE__MESSAGE = "Your client is requesting a representation of this resource that the API does not provide.";
    public static final String INVALID_HTTP_NOT_ACCEPTABLE__DEVELOPER_MESSAGE = String.format(
            "The requested resource is only capable of generating content not acceptable according to the Accept headers sent in the request.  Consider setting the Accept header of your response to '%s'",
            ApiVersions.API_VERSION_1
    );
    public static final String INVALID_HTTP_NOT_ACCEPTABLE__MORE_INFO = DEFAULT_MORE_INFO;

    // 409
    public static final String CONFLICT__MESSAGE = "This counter already exists!";
    public static final String CONFLICT__DEVELOPER_MESSAGE = "A sharded counter with the specified name already exists!";
    public static final String CONFLICT__MORE_INFO = DEFAULT_MORE_INFO;

    // 415
    public static final String INVALID_MEDIA_TYPE__MESSAGE = NOT_FOUND__MESSAGE;
    public static final String INVALID_MEDIA_TYPE__DEVELOPER_MESSAGE = "The requested entity has a media type which the resource does not support";
    public static final String INVALID_MEDIA_TYPE__MORE_INFO = DEFAULT_MORE_INFO;
}