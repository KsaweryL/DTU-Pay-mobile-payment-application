package dtu.businesslogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import dtu.aggregate.AccountId;
import dtu.aggregate.Token;
import dtu.aggregate.TokenId;
import dtu.aggregate.TokenStatus;
import dtu.repositories.ReadModelRepository;
import dtu.repositories.TokenRepository;
import messaging.MessageQueue;
import messaging.implementations.MessageQueueSync;

public class TokenManagerService {

    private final TokenRepository tokenRepository;
    private final ReadModelRepository readModel;

    public TokenManagerService(MessageQueue queue) {
        this.tokenRepository = new TokenRepository(queue);
        this.readModel = new ReadModelRepository(queue);
    }

    public TokenManagerService() {
        this(new MessageQueueSync());
    }

    // @author Peter 
    public List<String> createTokens(UUID customerId, int requested) {
        if (requested <= 0) {
            throw new IllegalArgumentException("Number of tokens must be positive");
        }

        AccountId accountId = new AccountId(customerId);
        List<String> createdTokens = new ArrayList<>(requested);

        for (int i = 0; i < requested; i++) {
            TokenId tokenId = new TokenId(generateUniqueToken());
            Token token = Token.issue(accountId, tokenId);
            tokenRepository.save(token);
            createdTokens.add(tokenId.getValue());
        }

        return createdTokens;
    }

    // @author Peter
    public boolean hasActiveTokens(UUID customerId) {
        return readModel.hasActiveTokens(new AccountId(customerId));
    }

    // @author Peter
    public List<String> getTokens(UUID customerId) {
        return readModel.getTokens(new AccountId(customerId))
                .stream()
                .map(TokenId::getValue)
                .collect(Collectors.toList());
    }

    // @author Fabian
    public Optional<UUID> findOwner(String token) {
        return readModel.findOwner(new TokenId(token))
                .map(AccountId::getUuid);
    }
    
    // @author Ksawery
    public Optional<UUID> reserveToken(String token) {
        TokenId tokenId = new TokenId(token);
        if (readModel.isInvalidated(tokenId)) {
            return Optional.empty();
        }
        TokenStatus status = readModel.getStatus(tokenId);
        if (status == null || status.getState() != TokenStatus.State.ACTIVE) {
            return Optional.empty();
        }

        Optional<AccountId> owner = readModel.findOwner(tokenId);
        if (owner.isEmpty()) {
            return Optional.empty();
        }

        try {
            Token aggregate = tokenRepository.getById(tokenId);
            aggregate.reserve();
            tokenRepository.save(aggregate);
            return Optional.of(owner.get().getUuid());
        } catch (RuntimeException | Error ex) {
            return Optional.empty();
        }
    }

    // @author Fabian
    public boolean releaseToken(String token) {
        TokenId tokenId = new TokenId(token);
        TokenStatus status = readModel.getStatus(tokenId);
        if (status == null || status.getState() != TokenStatus.State.RESERVED) {
            return false;
        }

        try {
            Token aggregate = tokenRepository.getById(tokenId);
            aggregate.release();
            tokenRepository.save(aggregate);
            return true;
        } catch (RuntimeException | Error ex) {
            return false;
        }
    }

    // @author Christoffer
    public boolean consumeToken(String token) {
        TokenId tokenId = new TokenId(token);
        TokenStatus status = readModel.getStatus(tokenId);
        if (status == null) {
            return false;
        }

        try {
            Token aggregate = tokenRepository.getById(tokenId);
            if (status.getState() == TokenStatus.State.ACTIVE) {
                aggregate.reserve();
            } else if (status.getState() != TokenStatus.State.RESERVED) {
                return false;
            }
            aggregate.consume();
            tokenRepository.save(aggregate);
            return true;
        } catch (RuntimeException | Error ex) {
            return false;
        }
    }

    // @author Christoffer
    public boolean isInvalidated(String token) {
        return readModel.isInvalidated(new TokenId(token));
    }

    // @author Frederik
    public void invalidateTokensForCustomer(UUID customerId) {
        AccountId accountId = new AccountId(customerId);
        List<TokenId> tokens = new ArrayList<>();
        tokens.addAll(readModel.getTokens(accountId));
        tokens.addAll(readModel.getReservedTokens(accountId));

        for (TokenId tokenId : tokens) {
            invalidateToken(tokenId);
        }
    }

    // @author Frederik
    private void invalidateToken(TokenId tokenId) {
        TokenStatus status = readModel.getStatus(tokenId);
        if (status != null && status.getState() == TokenStatus.State.INVALIDATED) {
            return;
        }
        try {
            Token aggregate = tokenRepository.getById(tokenId);
            aggregate.invalidate();
            tokenRepository.save(aggregate);
        } catch (RuntimeException | Error ex) {
            // Ignore missing tokens for now.
        }
    }

    // @author Peter
    private String generateUniqueToken() {
        String token = UUID.randomUUID().toString();
        while (readModel.findOwner(new TokenId(token)).isPresent()) {
            token = UUID.randomUUID().toString();
        }
        return token;
    }
}
