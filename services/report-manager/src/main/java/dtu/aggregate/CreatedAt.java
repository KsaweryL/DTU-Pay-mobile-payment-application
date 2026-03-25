package dtu.aggregate;

import java.io.Serializable;
import java.time.Instant;

import org.jmolecules.ddd.annotation.ValueObject;

import lombok.Value;

@ValueObject
@Value
public class CreatedAt implements Serializable {
    private static final long serialVersionUID = 1L;

    Instant value;

    public static CreatedAt now() {
        return new CreatedAt(Instant.now());
    }

    public static CreatedAt from(Instant value) {
        return value == null ? null : new CreatedAt(value);
    }
}
