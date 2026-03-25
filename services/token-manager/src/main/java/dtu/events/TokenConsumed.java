package dtu.events;

import dtu.aggregate.AccountId;
import dtu.aggregate.TokenId;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import messaging.Event;

// @author Fabian
@Value
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class TokenConsumed extends Event {
    private static final long serialVersionUID = 1L;

    private final AccountId accountId;
    private final TokenId tokenId;

    public TokenConsumed(AccountId accountId, TokenId tokenId) {
        super("TokenConsumed", accountId, tokenId);
        this.accountId = accountId;
        this.tokenId = tokenId;
    }

    public AccountId getAccountId() {
        return accountId;
    }

    public TokenId getTokenId() {
        return tokenId;
    }
}
