public class VIPCustomer extends Customer {
    public VIPCustomer(int id, Restaurant.HappyHour happyHour) {
        super(id, happyHour);
    }

    @Override
    public String getType() {
        return "VIP";
    }

    @Override
    protected int getWaitingLoss() {
        return 5;
    }

    @Override
    protected int calculateBill(int priceCents) {
        return (int) ((priceCents * 90L + 50) / 100);
    }
}