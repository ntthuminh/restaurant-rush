public class CriticCustomer extends Customer {
    public static final int WAITING_LOSS = 12;

    public CriticCustomer(int id, PricingPolicy pricing) {
        super(id, pricing);
    }

    public CriticCustomer(int id, PricingPolicy pricing, Order.Drink plannedDrink) {
        super(id, pricing, null, plannedDrink);
    }

    @Override
    public String getType() {
        return "Critic";
    }

    @Override
    protected int getWaitingLoss() {
        return WAITING_LOSS;
    }

    @Override
    public Order.MenuItem chooseFood() {
        return Order.MenuItem.PASTA;
    }
}
