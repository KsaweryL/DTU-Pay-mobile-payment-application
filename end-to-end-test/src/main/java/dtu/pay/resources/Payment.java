package dtu.pay.resources;

public record Payment(String merchantId, int amount, String token) {
    
}
