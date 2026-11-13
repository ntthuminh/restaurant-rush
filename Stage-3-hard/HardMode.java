import java.util.Arrays;
import java.util.List;

public class HardMode extends GameMode {
    public static final int TIP_CENTS_PER_POINT = 5;

    private static final Order.Meal[] ORDER_SEQUENCE = {
            Order.MenuItem.PASTA,
            Order.MenuItem.SALAD,
            Order.MenuItem.PASTA,
            Order.MenuItem.SALAD,
            new Order.ComboMeal(Order.MenuItem.SALAD, Order.MenuItem.PASTA),
            Order.MenuItem.BURGER,
            Order.MenuItem.PASTA,
            Order.MenuItem.PASTA,
            new Order.ComboMeal(Order.MenuItem.SALAD, Order.MenuItem.PASTA)
    };

    @Override
    public String getTitle() {
        return "RESTAURANT RUSH - HARD MODE";
    }

    @Override
    public int getArrivalInterval() {
        return 2;
    }

    @Override
    public int getTargetPaidCustomers() {
        return 5;
    }

    @Override
    public int getTargetRevenueCents() {
        return 11000;
    }

    @Override
    public int getTargetAverageSatisfaction() {
        return 75;
    }

    @Override
    public List<String> getRuleLines() {
        return Arrays.asList(
                "C2 is a VIP. C3 is a Critic. The other arrivals are Regular.",
                "A new customer arrives every 2 turns.",
                "Orders: C1 Pasta, C2 Salad, C4 Salad, C5 Combo, C6 Burger,"
                        + " C7 Pasta, C8 Pasta, C9 Combo.",
                "Salad + Pasta combo: $24.30, 3 preparation units.",
                "Critics always choose Pasta.",
                "Payment = bill + tip of 5 cents per satisfaction point (up to $5.00).");
    }

    @Override
    public String getSettingsLine() {
        return "#mode hard";
    }

    @Override
    public Order.Meal planMeal(int customerId) {
        return ORDER_SEQUENCE[(customerId - 1) % ORDER_SEQUENCE.length];
    }

    @Override
    public int getTipCentsPerPoint() {
        return TIP_CENTS_PER_POINT;
    }
}
