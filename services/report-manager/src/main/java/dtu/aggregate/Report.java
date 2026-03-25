package dtu.aggregate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Entity;

import dtu.events.ReportEntryCreated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import messaging.Event;
import com.google.gson.Gson;

// @author Nikolaj
@AggregateRoot
@Entity
@Getter
public class Report {
	private AccountId accountId;
	private List<MetaData> metaDatas;

	@Setter(AccessLevel.NONE)
	private List<Event> appliedEvents = new ArrayList<Event>();
	private Map<Class<? extends Event>, Consumer<Event>> handlers = new HashMap<>();

	public static Report create(String correlationId, AccountId accountId, CreatedAt createdAt, UserType userType) {
		RequestedAt requestedAt = new RequestedAt(Instant.now());
		MetaData metaData = new MetaData(requestedAt, createdAt, userType);
		ReportEntryCreated event = new ReportEntryCreated(correlationId, accountId, metaData);
		Report report = new Report();
		report.apply(event);
		report.appliedEvents.add(event);
		return report;
	}

	public static Report create(String correlationId, AccountId accountId, UserType userType) {
		CreatedAt createdAt = null;
		return create(correlationId, accountId, createdAt, userType);
	}

	public static Report createFromEvents(Stream<Event> events) {
		Report report = new Report();
		report.applyEvents(events);
		return report;
	}

	public Report() {
		registerEventHandlers();
	}

	private void registerEventHandlers() {
		handlers.put(ReportEntryCreated.class, e -> apply((ReportEntryCreated) e));
	}

	private void applyEvents(Stream<Event> events) throws Error {
		events.forEachOrdered(e -> {
			this.applyEvent(e);
		});
		if (this.getAccountId() == null) {
			throw new Error("user does not exist");
		}
	}

	private void applyEvent(Event e) {
		handlers.getOrDefault(e.getClass(), this::missingHandler).accept(e);
	}

	private void missingHandler(Event e) {
		throw new Error("handler for event " + e + " missing");
	}

	private void apply(ReportEntryCreated event) {
		metaDatas = List.of(event.getMetaData());
		accountId = event.getAccountId();

	}

	public void clearAppliedEvents() {
		appliedEvents.clear();
	}


	public static Report createFromMetaData(AccountId accountId2, List<MetaData> data) {
		Report report = new Report();
		report.accountId = accountId2;
		report.metaDatas = data == null ? new ArrayList<>() : new ArrayList<>(data);
		return report;
	}

	public String toJson() {
		Map<String, Object> payload = new HashMap<>();
		payload.put("history", metaDatas == null ? List.of() : metaDatas);
		return new Gson().toJson(payload);
	}

}
