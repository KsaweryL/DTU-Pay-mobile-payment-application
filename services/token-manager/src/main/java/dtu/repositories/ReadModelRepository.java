package dtu.repositories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import dtu.aggregate.AccountId;
import dtu.aggregate.TokenId;
import dtu.aggregate.TokenStatus;
import dtu.events.TokenConsumed;
import dtu.events.TokenInvalidated;
import dtu.events.TokenIssued;
import dtu.events.TokenReleased;
import dtu.events.TokenReserved;
import messaging.MessageQueue;
import org.jmolecules.ddd.annotation.Repository;

@Repository
public class ReadModelRepository {

    private Map<AccountId, List<TokenId>> activeTokens = new HashMap<>();
    private Map<AccountId, List<TokenId>> reservedTokens = new HashMap<>();
    private Map<TokenId, AccountId> tokenOwners = new HashMap<>();
    private Map<TokenId, TokenStatus> tokenStatuses = new HashMap<>();
    private Map<TokenId, Integer> reservedPositions = new HashMap<>();

    public ReadModelRepository(MessageQueue eventQueue) {
        eventQueue.addHandler("TokenIssued", this::commandApplyTokenIssued, TokenIssued.class);
        eventQueue.addHandler("TokenReserved", this::commandApplyTokenReserved, TokenReserved.class);
        eventQueue.addHandler("TokenReleased", this::commandApplyTokenReleased, TokenReleased.class);
        eventQueue.addHandler("TokenConsumed", this::commandApplyTokenConsumed, TokenConsumed.class);
        eventQueue.addHandler("TokenInvalidated", this::commandApplyTokenInvalidated, TokenInvalidated.class);
    }

    // @author Frederik
    public List<TokenId> getTokens(AccountId accountId) {
        List<TokenId> tokens = activeTokens.get(accountId);
        if (tokens == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(tokens);
    }

    // @author Frederik
    public List<TokenId> getReservedTokens(AccountId accountId) {
        List<TokenId> tokens = reservedTokens.get(accountId);
        if (tokens == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(tokens);
    }

    // @author Nikolaj
    public boolean hasActiveTokens(AccountId accountId) {
        List<TokenId> active = activeTokens.get(accountId);
        if (active != null && !active.isEmpty()) {
            return true;
        }
        List<TokenId> reserved = reservedTokens.get(accountId);
        return reserved != null && !reserved.isEmpty();
    }

    // @author Nikolaj
    public boolean isInvalidated(TokenId tokenId) {
        TokenStatus status = tokenStatuses.get(tokenId);
        return status != null && status.getState() == TokenStatus.State.INVALIDATED;
    }

    // @author Nikolaj
    public Optional<AccountId> findOwner(TokenId tokenId) {
        return Optional.ofNullable(tokenOwners.get(tokenId));
    }

    // @author Peter
    public TokenStatus getStatus(TokenId tokenId) {
        return tokenStatuses.get(tokenId);
    }

    // @author Frederik
    public void commandApplyTokenIssued(TokenIssued event) {
        AccountId accountId = event.getAccountId();
        TokenId tokenId = event.getTokenId();
        List<TokenId> active = activeTokens.computeIfAbsent(accountId, key -> new ArrayList<>());
        active.add(tokenId);
        tokenOwners.put(tokenId, accountId);
        tokenStatuses.put(tokenId, TokenStatus.active());
    }

    // @author Frederik
    public void commandApplyTokenReserved(TokenReserved event) {
        AccountId accountId = event.getAccountId();
        TokenId tokenId = event.getTokenId();
        List<TokenId> active = activeTokens.get(accountId);
        if (active != null) {
            int index = active.indexOf(tokenId);
            if (index >= 0) {
                active.remove(index);
                reservedPositions.put(tokenId, index);
            }
        }
        List<TokenId> reserved = reservedTokens.computeIfAbsent(accountId, key -> new ArrayList<>());
        if (!reserved.contains(tokenId)) {
            reserved.add(tokenId);
        }
        tokenStatuses.put(tokenId, TokenStatus.reserved());
    }

    // @author Peter
    public void commandApplyTokenReleased(TokenReleased event) {
        AccountId accountId = event.getAccountId();
        TokenId tokenId = event.getTokenId();
        List<TokenId> reserved = reservedTokens.get(accountId);
        if (reserved != null) {
            reserved.remove(tokenId);
        }
        List<TokenId> active = activeTokens.computeIfAbsent(accountId, key -> new ArrayList<>());
        if (!active.contains(tokenId)) {
            Integer index = reservedPositions.remove(tokenId);
            if (index == null) {
                active.add(tokenId);
            } else {
                int safeIndex = Math.min(Math.max(index, 0), active.size());
                active.add(safeIndex, tokenId);
            }
        }
        tokenStatuses.put(tokenId, TokenStatus.active());
    }

    // @author peter
    public void commandApplyTokenConsumed(TokenConsumed event) {
        AccountId accountId = event.getAccountId();
        TokenId tokenId = event.getTokenId();
        List<TokenId> reserved = reservedTokens.get(accountId);
        if (reserved != null) {
            reserved.remove(tokenId);
        }
        reservedPositions.remove(tokenId);
        tokenStatuses.put(tokenId, TokenStatus.consumed());
    }

    // @author peter
    public void commandApplyTokenInvalidated(TokenInvalidated event) {
        AccountId accountId = event.getAccountId();
        TokenId tokenId = event.getTokenId();
        List<TokenId> active = activeTokens.get(accountId);
        if (active != null) {
            active.remove(tokenId);
        }
        List<TokenId> reserved = reservedTokens.get(accountId);
        if (reserved != null) {
            reserved.remove(tokenId);
        }
        reservedPositions.remove(tokenId);
        tokenStatuses.put(tokenId, TokenStatus.invalidated());
    }
}
