import java.util.Arrays;
import java.util.List;

public class HardMode extends GameMode {
    public static final int TIP_CENTS_PER_POINT = 5;

    private static final Order.Meal COMBO =
            new Order.ComboMeal(Order.MenuItem.SALAD, Order.MenuItem.PASTA);

    private static final Order.Meal[] MEAL_SEQUENCE = {
            Order.MenuItem.PASTA,
            Order.MenuItem.SALAD,
            Order.MenuItem.PASTA,
            Order.MenuItem.SALAD,
            COMBO,
            Order.MenuItem.BURGER,
            Order.MenuItem.PASTA,
            Order.MenuItem.PASTA,
            COMBO
    };

    private static final Order.Drink[] DRINK_SEQUENCE = {
            Order.Drink.LEMONADE,
            Order.Drink.MOCKTAIL,
            null,
            Order.Drink.LEMONADE,
            null,
            Order.Drink.MOCKTAIL,
            Order.Drink.LEMONADE,
            null,
            Order.Drink.MOCKTAIL
    };

    @Override
    public String getName() {
        return "Hard";
    }

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
        return 12500;
    }

    @Override
    public int getTargetAverageSatisfaction() {
        return 75;
    }

    @Override
    public List<Restaurant.Role> getManagerRoles() {
        return Arrays.asList(Restaurant.Role.HOST, Restaurant.Role.WAITER,
                Restaurant.Role.CHEF, Restaurant.Role.BARTENDER);
    }

    @Override
    public List<Order.Drink> getDrinks() {
        return Arrays.asList(Order.Drink.values());
    }

    @Override
    public List<String> getRuleLines() {
        return Arrays.asList(
                "C2 is a VIP. C3 is a Critic. The other arrivals are Regular.",
                "A new customer arrives every 2 turns.",
                "Meals: C1 Pasta, C2 Salad, C4 Salad, C5 Combo, C6 Burger, C7 Pasta, C8 Pasta, C9 Combo.",
                "Drinks: C1 Lemonade, C2 Mocktail, C4 Lemonade, C6 Mocktail, C7 Lemonade, C9 Mocktail.",
                "Salad + Pasta combo: $24.30, 3 preparation units.",
                "Critics always choose Pasta.",
                "The Bartender makes drinks after the Chef cooks. An order with a drink is READY"
                        + " only when both are done.",
                "Payment = bill + tip of 5 cents per satisfaction point (up to $5.00).");
    }

    @Override
    protected Order.Meal planMeal(int customerId) {
        return MEAL_SEQUENCE[(customerId - 1) % MEAL_SEQUENCE.length];
    }

    @Override
    protected Order.Drink planDrink(int customerId) {
        return DRINK_SEQUENCE[(customerId - 1) % DRINK_SEQUENCE.length];
    }

    @Override
    public int getTipCentsPerPoint() {
        return TIP_CENTS_PER_POINT;
    }
}