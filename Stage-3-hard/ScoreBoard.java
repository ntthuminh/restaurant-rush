public class ScoreBoard {
    private int revenueCents;
    private int tipsCents;
    private int servedCount;
    private int unhappyDepartures;
    private int paidCount;
    private int paidSatisfactionTotal;

    void recordServe() {
        servedCount++;
    }

    void recordDeparture() {
        unhappyDepartures++;
    }

    void recordPayment(int amountCents, int billCents, int satisfaction) {
        revenueCents += amountCents;
        tipsCents += amountCents - billCents;
        paidCount++;
        paidSatisfactionTotal += satisfaction;
    }

    public int getRevenueCents() {
        return revenueCents;
    }

    public int getTipsCents() {
        return tipsCents;
    }

    public int getServedCount() {
        return servedCount;
    }

    public int getUnhappyDepartures() {
        return unhappyDepartures;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public double getAverageSatisfaction() {
        return paidCount == 0 ? 0.0 : (double) paidSatisfactionTotal / paidCount;
    }

    public boolean meetsTargets(GameMode mode) {
        return paidCount >= mode.getTargetPaidCustomers()
                && revenueCents >= mode.getTargetRevenueCents()
                && paidSatisfactionTotal >= mode.getTargetAverageSatisfaction() * paidCount;
    }
}