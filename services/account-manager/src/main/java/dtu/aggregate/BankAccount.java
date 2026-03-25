package dtu.aggregate;

import java.io.Serializable;

import org.jmolecules.ddd.annotation.ValueObject;

import lombok.Value;

// @author Fabian
@ValueObject
@Value
public class BankAccount implements Serializable{
	private static final long serialVersionUID = -1455308747700082116L;
	private String id;
}
