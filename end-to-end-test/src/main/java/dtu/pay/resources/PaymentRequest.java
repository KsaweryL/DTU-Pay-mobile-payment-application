package dtu.pay.resources;

public record PaymentRequest(int amount, String merchantID, String token) {
}
