package dtu.aggregate;

import java.io.Serializable;
import java.util.UUID;

import org.jmolecules.ddd.annotation.ValueObject;

import lombok.Value;

// @author Christoffer
@ValueObject
@Value
public class AccountId implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID uuid;
}
