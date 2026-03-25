package dtu.businesslogic;

import java.util.Map;
import java.util.UUID;

import dtu.aggregate.AccountId;
import dtu.aggregate.Report;
import dtu.aggregate.UserType;
import dtu.repositories.ReadModelRepository;
import dtu.repositories.ReportRepository;
import messaging.Event;
import messaging.MessageQueue;

public class ReportManagerService {

    private final MessageQueue queue;
    private final ReportRepository reportRepository;
    private final ReadModelRepository readModelRepository;
    
    private final String CUSTOMER_REPORT_HISTORY_REQUESTED = "GetCustomerReportHistoryRequested";
    private final String MERCHANT_REPORT_HISTORY_REQUESTED = "GetMerchantReportHistoryRequested";
    private final String MANAGER_REPORT_HISTORY_REQUESTED = "GetManagerReportHistoryRequested";
    private final String CUSTOMER_REPORT_HISTORY_RECEIVED = "CustomerReportHistoryReceived";
    private final String MERCHANT_REPORT_HISTORY_RECEIVED = "MerchantReportHistoryReceived";
    private final String MANAGER_REPORT_HISTORY_RECEIVED = "ManagerReportHistoryReceived";

    public ReportManagerService(MessageQueue q) {
        this.queue = q;
        this.reportRepository = new ReportRepository(queue);
        this.readModelRepository = new ReadModelRepository(queue);

        // These events triggers saving of metadata
        q.addHandler("GetCustomerReportRequested", this::commandReportCreated);
        q.addHandler("GetMerchantReportRequested", this::commandReportCreated);
        q.addHandler("GetAllReportRequested", this::commandReportCreated);

        // These events triggers retrieval of report history
        q.addHandler(CUSTOMER_REPORT_HISTORY_REQUESTED, this::commandGetReportHistory);
        q.addHandler(MERCHANT_REPORT_HISTORY_REQUESTED, this::commandGetReportHistory);
        q.addHandler(MANAGER_REPORT_HISTORY_REQUESTED, this::commandGetReportHistory);
    }

    // @author Tobias
    private void commandReportCreated(Event event) {
        String correlationId = event.getArgument(0, String.class);
        AccountId accountId = parseAccountId(event);
        UserType userType;
        String topic = event.getTopic();
        if ("GetCustomerReportRequested".equals(topic)) {
            userType = UserType.CUSTOMER;
        } else if ("GetMerchantReportRequested".equals(topic)) {
            userType = UserType.MERCHANT;
        } else {
            userType = UserType.MANAGER;
        }
        Report record = Report.create(correlationId, accountId, userType);
        reportRepository.save(record);
    }

    // @author Nikolaj
    private void commandGetReportHistory(Event event) {
        String topic = event.getTopic();
        String correlationId = event.getArgument(0, String.class);
        String newTopic;
        UserType userType;
        if (CUSTOMER_REPORT_HISTORY_REQUESTED.equals(topic)) {
            newTopic = CUSTOMER_REPORT_HISTORY_RECEIVED;
            userType = UserType.CUSTOMER;
        } else if (MERCHANT_REPORT_HISTORY_REQUESTED.equals(topic)) {
            newTopic = MERCHANT_REPORT_HISTORY_RECEIVED;
            userType = UserType.MERCHANT;
        } else if (MANAGER_REPORT_HISTORY_REQUESTED.equals(topic)) {
            newTopic = MANAGER_REPORT_HISTORY_RECEIVED;
            userType = UserType.MANAGER;
        } else {
            return;
        }

        String reportJson;
        try {
            AccountId accountId = parseAccountId(event);
        Report report = readModelRepository.fromAccountId(accountId);
            if (report == null || report.getMetaDatas() == null || report.getMetaDatas().isEmpty()) {
                Report record = Report.create(correlationId, accountId, userType);
                reportRepository.save(record);
                report = record;
            }
            reportJson = report.toJson();
        } catch (RuntimeException ex) {
            reportJson = "{\"history\":[{}]}";
        }

        Event newEvent = new Event(newTopic, correlationId, reportJson);
        queue.publish(newEvent);
    }

    // @author Fabian
    private AccountId parseAccountId(Event event) {
        Object raw;
        try {
            raw = event.getArgument(1, Object.class);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Missing account id argument", ex);
        }
        if (raw == null) {
            throw new IllegalArgumentException("Account id argument is null");
        }
        if (raw instanceof String rawId) {
            return new AccountId(UUID.fromString(rawId));
        }
        if (raw instanceof UUID uuid) {
            return new AccountId(uuid);
        }
        if (raw instanceof Map<?, ?> map) {
            Object uuidValue = map.get("uuid");
            if (uuidValue != null) {
                return new AccountId(UUID.fromString(uuidValue.toString()));
            }
            Object msb = map.get("mostSigBits");
            Object lsb = map.get("leastSigBits");
            if (msb instanceof Number && lsb instanceof Number) {
                return new AccountId(new UUID(((Number) msb).longValue(), ((Number) lsb).longValue()));
            }
        }
        throw new IllegalArgumentException("Unsupported account id argument: " + raw);
    }
}
