import java.util.List;

public abstract class GameMode {
    public abstract String getTitle();
    public abstract int getArrivalInterval();
    public abstract int getTargetPaidCustomers();
    public abstract int getTargetRevenueCents();
    public abstract int getTargetAverageSatisfaction();

    public abstract List<String> getRuleLines();

    public abstract String getSettingsLine();

    public Order.Meal planMeal(int customerId) {
        return null;
    }

    public int getTipCentsPerPoint() {
        return 0;
    }

    public final int amountPaid(Customer customer) {
        return customer.getOrder().getBillCents()
                + getTipCentsPerPoint() * customer.getSatisfaction();
    }

    public static GameMode fromSettingsLine(String line) {
        String text = line.trim();
        if (text.equals("#mode hard")) {
            return new HardMode();
        }
        Restaurant.require(text.equals("#mode normal"), "Unknown mode line: " + line);
        return new NormalMode();
    }
}
