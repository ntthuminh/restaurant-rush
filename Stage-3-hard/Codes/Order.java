public class Order {
    public interface Meal {
        String getName();
        int getPriceCents();
        int getPreparationUnits();
    }

    public enum MenuItem implements Meal {
        SALAD("Salad", 900, 1),
        BURGER("Burger", 1500, 1),
        PASTA("Pasta", 1800, 2);

        private final String name;
        private final int priceCents;
        private final int preparationUnits;

        MenuItem(String name, int priceCents, int preparationUnits) {
            this.name = name;
            this.priceCents = priceCents;
            this.preparationUnits = preparationUnits;
        }

        public String getName() {
            return name;
        }

        public int getPriceCents() {
            return priceCents;
        }

        public int getPreparationUnits() {
            return preparationUnits;
        }
    }

    public static final class ComboMeal implements Meal {
        public static final int DISCOUNT_PERCENT = 10;

        private final MenuItem first;
        private final MenuItem second;

        public ComboMeal(MenuItem first, MenuItem second) {
            Preconditions.check(first != null && second != null, "Choose two menu items.");
            this.first = first;
            this.second = second;
        }

        @Override
        public String getName() {
            return "Combo (" + first.getName() + " + " + second.getName() + ")";
        }

        @Override
        public int getPriceCents() {
            int combinedPrice = first.getPriceCents() + second.getPriceCents();
            return (int) ((combinedPrice * (100L - DISCOUNT_PERCENT) + 50) / 100);
        }

        @Override
        public int getPreparationUnits() {
            return first.getPreparationUnits() + second.getPreparationUnits();
        }
    }

    public enum Drink {
        LEMONADE("Lemonade", 400, 1),
        MOCKTAIL("Mocktail", 700, 2);

        private final String name;
        private final int priceCents;
        private final int preparationUnits;

        Drink(String name, int priceCents, int preparationUnits) {
            this.name = name;
            this.priceCents = priceCents;
            this.preparationUnits = preparationUnits;
        }

        public String getName() {
            return name;
        }

        public int getPriceCents() {
            return priceCents;
        }

        public int getPreparationUnits() {
            return preparationUnits;
        }
    }

    public enum Status { PREPARING, READY, SERVED, READY_TO_PAY, PAID, CANCELLED }

    private final Meal item;
    private final Drink drink;
    private final int billCents;
    private int completedUnits;
    private int completedDrinkUnits;
    private int servedTurn = -1;
    private int readyToPayTurn = -1;
    private Status status = Status.PREPARING;

    public Order(Meal item, int billCents) {
        this(item, null, billCents);
    }

    public Order(Meal item, Drink drink, int billCents) {
        Preconditions.check(item != null, "Choose a menu item.");
        Preconditions.check(billCents >= 0, "The bill cannot be negative.");
        this.item = item;
        this.drink = drink;
        this.billCents = billCents;
    }

    public Meal getItem() {
        return item;
    }

    public Drink getDrink() {
        return drink;
    }

    public String getName() {
        return drink == null ? item.getName() : item.getName() + " + " + drink.getName();
    }

    public int getBillCents() {
        return billCents;
    }

    public int getServedTurn() {
        return servedTurn;
    }

    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    public Status getStatus() {
        return status;
    }

    public boolean needsCooking() {
        return status == Status.PREPARING && completedUnits < item.getPreparationUnits();
    }

    public boolean needsMixing() {
        return status == Status.PREPARING && drink != null
                && completedDrinkUnits < drink.getPreparationUnits();
    }

    public void prepare() {
        Preconditions.check(needsCooking(), "That order does not need preparation.");
        completedUnits++;
        updateReady();
    }

    public void mixDrink() {
        Preconditions.check(needsMixing(), "That order has no drink to make.");
        completedDrinkUnits++;
        updateReady();
    }

    private void updateReady() {
        if (!needsCooking() && !needsMixing()) {
            status = Status.READY;
        }
    }

    public void serve(int turn) {
        Preconditions.check(status == Status.READY, "That order is not READY.");
        status = Status.SERVED;
        servedTurn = turn;
    }

    public void finishEating(int turn) {
        Preconditions.check(status == Status.SERVED && servedTurn < turn,
                "The customer must finish one full turn of eating.");
        status = Status.READY_TO_PAY;
        readyToPayTurn = turn;
    }

    public void markPaid() {
        Preconditions.check(status == Status.READY_TO_PAY,
                "That order is not ready for payment.");
        status = Status.PAID;
    }

    public void cancel() {
        Preconditions.check(status == Status.PREPARING || status == Status.READY,
                "That order cannot be cancelled.");
        status = Status.CANCELLED;
    }

    public String getDescription() {
        if (status != Status.PREPARING) {
            return getName() + " " + status;
        }
        String description = item.getName() + " PREPARING " + completedUnits + "/"
                + item.getPreparationUnits();
        if (drink != null) {
            description += " + " + drink.getName() + " " + completedDrinkUnits + "/"
                    + drink.getPreparationUnits();
        }
        return description;
    }
}