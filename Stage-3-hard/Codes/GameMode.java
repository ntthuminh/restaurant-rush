import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class GameMode {
    public static final int VIP_ID = 2;
    public static final int CRITIC_ID = 3;

    public abstract String getName();

    public abstract String getTitle();

    public abstract int getArrivalInterval();

    public abstract int getTargetPaidCustomers();

    public abstract int getTargetRevenueCents();

    public abstract int getTargetAverageSatisfaction();

    public abstract List<String> getRuleLines();

    public List<Restaurant.Role> getManagerRoles() {
        return Arrays.asList(Restaurant.Role.HOST, Restaurant.Role.WAITER, Restaurant.Role.CHEF);
    }

    public List<Order.Drink> getDrinks() {
        return Collections.emptyList();
    }

    public Customer createCustomer(int id, PricingPolicy pricing) {
        Order.Drink drink = planDrink(id);
        if (id == VIP_ID) {
            return new VIPCustomer(id, pricing, planMeal(id), drink);
        }
        if (id == CRITIC_ID) {
            return new CriticCustomer(id, pricing, drink);
        }
        return new Customer(id, pricing, planMeal(id), drink);
    }

    protected Order.Meal planMeal(int customerId) {
        return null;
    }

    protected Order.Drink planDrink(int customerId) {
        return null;
    }

    public int getTipCentsPerPoint() {
        return 0;
    }

    public final int amountPaid(Customer customer) {
        return customer.getOrder().getBillCents()
                + getTipCentsPerPoint() * customer.getSatisfaction();
    }
}