public class HappyHour implements PricingPolicy {
    public static final int DURATION_TURNS = 2;
    public static final int DISCOUNT_PERCENT = 20;
    public static final int SATISFACTION_RECOVERY = 15;

    private boolean used;
    private int turnsRemaining;

    void startTurn(boolean activate) {
        Preconditions.check(!activate || !used, "Happy Hour can only be activated once.");
        if (turnsRemaining > 0) {
            turnsRemaining--;
        }
        if (activate) {
            used = true;
            turnsRemaining = DURATION_TURNS;
        }
    }

    public boolean isUsed() {
        return used;
    }

    public boolean isActive() {
        return turnsRemaining > 0;
    }

    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    @Override
    public int applyToBill(int billCents) {
        if (isActive()) {
            return (int) ((billCents * (100L - DISCOUNT_PERCENT) + 50) / 100);
        }
        return billCents;
    }
}