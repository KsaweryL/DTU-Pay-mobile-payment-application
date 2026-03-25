package dtu.aggregate;

import java.io.Serializable;
import org.jmolecules.ddd.annotation.ValueObject;

import lombok.Value;

// @author Ksawery
@ValueObject
@Value
public class MetaData implements Serializable {
	private static final long serialVersionUID = 1L;

	private RequestedAt requestedAt;
	private CreatedAt createdAt;
	private UserType userType;
}
