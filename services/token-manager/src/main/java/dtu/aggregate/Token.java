package dtu.aggregate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import dtu.events.TokenConsumed;
import dtu.events.TokenInvalidated;
import dtu.events.TokenIssued;
import dtu.events.TokenReleased;
import dtu.events.TokenReserved;
import messaging.Event;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Entity;
import lombok.Getter;

@AggregateRoot
@Entity
@Getter
public class Token {
    private TokenId tokenId;
    private AccountId accountId;
    private TokenStatus status;

    private transient final List<Event> appliedEvents = new ArrayList<>();
    private transient final Map<Class<? extends Event>, Consumer<Event>> handlers = new HashMap<>();

	// @author Ksawery 
    public static Token issue(AccountId accountId, TokenId tokenId) {
        Token token = new Token();
        TokenIssued event = new TokenIssued(accountId, tokenId);
        token.apply(event);
        token.appliedEvents.add(event);
        return token;
    }

	// @author Fabian
    public static Token createFromEvents(Stream<Event> events) {
        Token token = new Token();
        token.applyEvents(events);
        return token;
    }
    
    public Token() {
        registerEventHandlers();
    }

	// @author Tobias
    public void reserve() {
        requireStatus(TokenStatus.State.ACTIVE);
        TokenReserved event = new TokenReserved(requireAccountId(), requireTokenId());
        apply(event);
        appliedEvents.add(event);
    }

    // @author Tobias 
    public void release() {
        requireStatus(TokenStatus.State.RESERVED);
        TokenReleased event = new TokenReleased(requireAccountId(), requireTokenId());
        apply(event);
        appliedEvents.add(event);
    }

    // @author Christoffer
    public void consume() {
        requireStatus(TokenStatus.State.RESERVED);
        TokenConsumed event = new TokenConsumed(requireAccountId(), requireTokenId());
        apply(event);
        appliedEvents.add(event);
    }

    // @author Frederik
    public void invalidate() {
        if (status != null && status.getState() == TokenStatus.State.CONSUMED) {
            throw new IllegalStateException("Token is already consumed");
        }
        TokenInvalidated event = new TokenInvalidated(requireAccountId(), requireTokenId());
        apply(event);
        appliedEvents.add(event);
    }

    // @author Ksawery
    public List<Event> getAppliedEvents() {
        return appliedEvents;
    }

    // @author Ksawery
    public void clearAppliedEvents() {
        appliedEvents.clear();
    }

    // @author Ksawery
    private void registerEventHandlers() {
        handlers.put(TokenIssued.class, e -> apply((TokenIssued) e));
        handlers.put(TokenReserved.class, e -> apply((TokenReserved) e));
        handlers.put(TokenReleased.class, e -> apply((TokenReleased) e));
        handlers.put(TokenConsumed.class, e -> apply((TokenConsumed) e));
        handlers.put(TokenInvalidated.class, e -> apply((TokenInvalidated) e));
    }

    // @author Ksawery
    private void applyEvents(Stream<Event> events) {
        events.forEachOrdered(this::applyEvent);
        if (tokenId == null) {
            throw new Error("token does not exist");
        }
    }

    // @author Peter
    private void applyEvent(Event e) {
        handlers.getOrDefault(e.getClass(), this::missingHandler).accept(e);
    }

    // @author Peter
    private void missingHandler(Event e) {
        throw new Error("handler for event " + e + " missing");
    }

    // @author Fabian
    private void apply(TokenIssued event) {
        accountId = ensureAccountId(accountId, event.getAccountId());
        tokenId = ensureTokenId(tokenId, event.getTokenId());
        status = TokenStatus.active();
    }

    // @author Fabian
    private void apply(TokenReserved event) {
        accountId = ensureAccountId(accountId, event.getAccountId());
        tokenId = ensureTokenId(tokenId, event.getTokenId());
        status = TokenStatus.reserved();
    }

    // @author Fabian
    private void apply(TokenReleased event) {
        accountId = ensureAccountId(accountId, event.getAccountId());
        tokenId = ensureTokenId(tokenId, event.getTokenId());
        status = TokenStatus.active();
    }

    // @author Fabian
    private void apply(TokenConsumed event) {
        accountId = ensureAccountId(accountId, event.getAccountId());
        tokenId = ensureTokenId(tokenId, event.getTokenId());
        status = TokenStatus.consumed();
    }

    // @author Fabian   
    private void apply(TokenInvalidated event) {
        accountId = ensureAccountId(accountId, event.getAccountId());
        tokenId = ensureTokenId(tokenId, event.getTokenId());
        status = TokenStatus.invalidated();
    }

    // @author Nikolaj
    private void requireStatus(TokenStatus.State expected) {
        if (status == null || status.getState() != expected) {
            throw new IllegalStateException("Token is not " + expected.name().toLowerCase());
        }
    }

    // @author Nikolaj
    private AccountId requireAccountId() {
        if (accountId == null) {
            throw new IllegalStateException("Token owner is not set");
        }
        return accountId;
    }

    // @author Nikolaj
    private TokenId requireTokenId() {
        if (tokenId == null) {
            throw new IllegalStateException("Token id is not set");
        }
        return tokenId;
    }

    // @author Nikolaj
    private AccountId ensureAccountId(AccountId current, AccountId next) {
        if (current == null) {
            return next;
        }
        if (!current.equals(next)) {
            throw new IllegalStateException("Token owner mismatch");
        }
        return current;
    }

    // @author Nikolaj
    private TokenId ensureTokenId(TokenId current, TokenId next) {
        if (current == null) {
            return next;
        }
        if (!current.equals(next)) {
            throw new IllegalStateException("Token id mismatch");
        }
        return current;
    }
}
