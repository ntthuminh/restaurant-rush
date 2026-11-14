public class VIPCustomer extends Customer {
    public static final int WAITING_LOSS = 5;
    public static final int DISCOUNT_PERCENT = 10;

    public VIPCustomer(int id, PricingPolicy pricing) {
        super(id, pricing);
    }

    public VIPCustomer(int id, PricingPolicy pricing, Order.Meal plannedMeal, Order.Drink plannedDrink) {
        super(id, pricing, plannedMeal, plannedDrink);
    }

    @Override
    public String getType() {
        return "VIP";
    }

    @Override
    protected int getWaitingLoss() {
        return WAITING_LOSS;
    }

    @Override
    protected int calculateBill(int priceCents) {
        return (int) ((priceCents * (100L - DISCOUNT_PERCENT) + 50) / 100);
    }
}