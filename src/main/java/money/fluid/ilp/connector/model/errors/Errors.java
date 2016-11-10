package money.fluid.ilp.connector.model.errors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * A representation of of the V1 resource.
 */
@Getter
@ToString
@EqualsAndHashCode
public class Errors {
    private final List<java.lang.Error> errors;

    /**
     * Required-args Constructor.
     *
     * @param errors
     */
    public Errors(@JsonProperty("errors") final List<java.lang.Error> errors) {
        this.errors = Preconditions.checkNotNull(errors);
    }

    /**
     * Helper method to construct an instance of {@link Errors} from a {@link List} of type {@link java.lang.Error}.
     *
     * @param errors
     * @return
     */
    public static Errors of(final java.lang.Error... errors) {
        Preconditions.checkNotNull(errors);
        return new Errors(ImmutableList.copyOf(errors));
    }

}
