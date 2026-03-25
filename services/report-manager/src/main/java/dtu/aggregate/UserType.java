package dtu.aggregate;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public enum UserType {
    CUSTOMER,
    MERCHANT,
    MANAGER,
    UNKNOWN
}
