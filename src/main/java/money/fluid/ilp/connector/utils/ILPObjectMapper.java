package money.fluid.ilp.connector.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.sappenin.utils.json.jackson.mappers.modules.HttpUrlModule;

/**
 * An extension of {@link ObjectMapper}.
 */
public class ILPObjectMapper extends ObjectMapper {
    private static final long serialVersionUID = 1L;

    public ILPObjectMapper() {
        // Enables Joda Searialization/Deserialization
        // See https://github.com/FasterXML/jackson-datatype-joda
        registerModule(new HttpUrlModule());
        registerModule(new JodaModule());
        registerModule(new GuavaModule());

        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        setDateFormat(new ISO8601DateFormat());

        setVisibility(
                getSerializationConfig().getDefaultVisibilityChecker()
                        .withFieldVisibility(Visibility.ANY)
                        .withGetterVisibility(Visibility.ANY)
                        .withSetterVisibility(Visibility.NONE)
                        .withCreatorVisibility(Visibility.PUBLIC_ONLY)
                        .withIsGetterVisibility(Visibility.NONE)
        );
    }
}
