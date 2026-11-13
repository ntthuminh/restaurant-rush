public class CriticCustomer extends Customer {
    public CriticCustomer(int id, Restaurant.HappyHour happyHour) {
        super(id, happyHour);
    }

    @Override
    public String getType() {
        return "Critic";
    }

    @Override
    protected int getWaitingLoss() {
        return 12;
    }

    @Override
    public Order.MenuItem chooseFood() {
        return Order.MenuItem.PASTA;
    }
}