package dtu.merchant_communication;

import java.io.Serializable;
import java.util.UUID;

import messaging.Event;

// @author Christoffer
public class AccountId implements Serializable {
	private static final long serialVersionUID = -1455308747700082116L;

	private UUID uuid;

	public AccountId() {
	}

	public UUID getUuid() {
		return uuid;
	}

	public static UUID fromEvent(Event event) {
		return fromEventArg(event, 1);
	}

	public static UUID fromEventArg(Event event, int index) {
		AccountId accountId = event.getArgument(index, AccountId.class);
		return accountId.getUuid();
	}
}
