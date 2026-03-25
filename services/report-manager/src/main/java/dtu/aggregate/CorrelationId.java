package dtu.aggregate;

import java.io.Serializable;

import org.jmolecules.ddd.annotation.ValueObject;

import lombok.Value;

@ValueObject
@Value
public class CorrelationId implements Serializable {
    private static final long serialVersionUID = 1L;

    String value;

    public static CorrelationId from(String value) {
        return value == null ? null : new CorrelationId(value);
    }
}
