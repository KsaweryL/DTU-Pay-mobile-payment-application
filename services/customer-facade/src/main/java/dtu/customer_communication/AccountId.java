package dtu.customer_communication;

import java.io.Serializable;
import java.util.UUID;

import org.jmolecules.ddd.annotation.ValueObject;

import lombok.Value;
import messaging.Event;

// @author Frederik
@ValueObject
@Value
public class AccountId implements Serializable{
	private static final long serialVersionUID = -1455308747700082116L;
	private UUID uuid;


	public static UUID fromEvent(Event event) {
		AccountId accountId = new AccountId(fromEventArg(event, 1));
		return accountId.getUuid();
	}
	public static UUID fromEventArg(Event event, int index) {
		return event.getArgument(index, AccountId.class).getUuid();
	}

}