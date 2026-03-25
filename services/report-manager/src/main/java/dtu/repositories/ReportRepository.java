package dtu.repositories;

import org.jmolecules.ddd.annotation.Repository;

import dtu.aggregate.AccountId;
import dtu.aggregate.Report;
import messaging.MessageQueue;

// @author Ksawery
@Repository
public class ReportRepository {
	
	private EventStore eventStore;

	public ReportRepository(MessageQueue bus) {
		eventStore = new EventStore(bus);
	}

	public Report getById(AccountId accountId) {
		return Report.createFromEvents(eventStore.getEventsFor(accountId));
	}
	
	public void save(Report report) {
		eventStore.addEvents(report.getAccountId(), report.getAppliedEvents());
		report.clearAppliedEvents();
	}
}
