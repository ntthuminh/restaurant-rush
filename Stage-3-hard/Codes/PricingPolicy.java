public interface PricingPolicy {
    int applyToBill(int billCents);
}