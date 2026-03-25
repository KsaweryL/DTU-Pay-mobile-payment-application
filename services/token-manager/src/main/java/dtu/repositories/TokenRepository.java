package dtu.repositories;

import dtu.aggregate.Token;
import dtu.aggregate.TokenId;
import messaging.MessageQueue;
import org.jmolecules.ddd.annotation.Repository;

@Repository
public class TokenRepository {

    private EventStore eventStore;

	// @author Tobias
    public TokenRepository(MessageQueue bus) {
        eventStore = new EventStore(bus);
    }

	// @author Christoffer
    public Token getById(TokenId tokenId) {
        return Token.createFromEvents(eventStore.getEventsFor(tokenId));
    }

	// @author Frederik
    public void save(Token token) {
        eventStore.addEvents(token.getTokenId(), token.getAppliedEvents());
        token.clearAppliedEvents();
    }
}
