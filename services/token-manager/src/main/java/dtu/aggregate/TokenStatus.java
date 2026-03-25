package dtu.aggregate;

import java.io.Serializable;

import org.jmolecules.ddd.annotation.ValueObject;

import lombok.Value;

// @author Fabian
@ValueObject
@Value
public class TokenStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum State {
        ACTIVE,
        RESERVED,
        CONSUMED,
        INVALIDATED
    }

    private static final TokenStatus ACTIVE = new TokenStatus(State.ACTIVE);
    private static final TokenStatus RESERVED = new TokenStatus(State.RESERVED);
    private static final TokenStatus CONSUMED = new TokenStatus(State.CONSUMED);
    private static final TokenStatus INVALIDATED = new TokenStatus(State.INVALIDATED);

    private State state;

    public static TokenStatus active() {
        return ACTIVE;
    }

    public static TokenStatus reserved() {
        return RESERVED;
    }

    public static TokenStatus consumed() {
        return CONSUMED;
    }

    public static TokenStatus invalidated() {
        return INVALIDATED;
    }
}
