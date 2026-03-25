package dtu.aggregate;

import java.io.Serializable;
import java.time.Instant;

import org.jmolecules.ddd.annotation.ValueObject;

import lombok.Value;

@ValueObject
@Value
public class RequestedAt implements Serializable {
    private static final long serialVersionUID = 1L;

    Instant value;

    public static RequestedAt now() {
        return new RequestedAt(Instant.now());
    }

    public static RequestedAt from(Instant value) {
        return value == null ? null : new RequestedAt(value);
    }
}
