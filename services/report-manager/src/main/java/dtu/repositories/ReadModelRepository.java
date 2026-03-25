package dtu.repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmolecules.ddd.annotation.Repository;

import dtu.aggregate.AccountId;
import dtu.aggregate.MetaData;
import dtu.aggregate.Report;
import dtu.events.ReportEntryCreated;
import messaging.MessageQueue;

// @author Frederik
@Repository
public class ReadModelRepository {

	private Map<AccountId, List<MetaData>> metadata = new HashMap<>();

	public ReadModelRepository(MessageQueue eventQueue) {
		eventQueue.addHandler("ReportEntryCreated", this::apply, ReportEntryCreated.class);
	}

	public Report fromAccountId(AccountId accountId) {
		List<MetaData> data = metadata.get(accountId);
		if (data == null) {
			return null;
		}
		return Report.createFromMetaData(accountId, data);
	}

	public void apply(ReportEntryCreated event) {
		AccountId accountId = event.getAccountId();
		List<MetaData> metaDatas = List.of(event.getMetaData());
		metadata.computeIfAbsent(accountId, k -> new java.util.ArrayList<>()).addAll(metaDatas);
	}
}
