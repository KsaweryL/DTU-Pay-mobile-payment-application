package dtu.merchant_communication;

// @author Christoffer
public record PaymentRequest(int amount, String merchantID, String token) {
}
