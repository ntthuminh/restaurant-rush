public class Order {
    public enum MenuItem {
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

    public enum Status { PREPARING, READY, SERVED, READY_TO_PAY, PAID, CANCELLED }

    private final MenuItem item;
    // Keep the price fixed from the moment the order is placed.
    private final int billCents;
    private int completedUnits;
    private int servedTurn = -1;
    private int readyToPayTurn = -1;
    private Status status = Status.PREPARING;

    public Order(MenuItem item) {
        Restaurant.require(item != null, "Choose a menu item.");
        this.item = item;
        this.billCents = item.getPriceCents();
    }

    public MenuItem getItem() {
        return item;
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

    public void prepare() {
        Restaurant.require(status == Status.PREPARING,
                "That order does not need preparation.");
        completedUnits++;
        if (completedUnits == item.getPreparationUnits()) {
            status = Status.READY;
        }
    }

    public void serve(int turn) {
        Restaurant.require(status == Status.READY, "That order is not READY.");
        status = Status.SERVED;
        servedTurn = turn;
    }

    public void finishEating(int turn) {
        Restaurant.require(status == Status.SERVED && servedTurn < turn,
                "The customer must finish one full turn of eating.");
        status = Status.READY_TO_PAY;
        readyToPayTurn = turn;
    }

    public void markPaid() {
        Restaurant.require(status == Status.READY_TO_PAY,
                "That order is not ready for payment.");
        status = Status.PAID;
    }

    public void cancel() {
        Restaurant.require(status == Status.PREPARING || status == Status.READY,
                "That order cannot be cancelled.");
        status = Status.CANCELLED;
    }

    public String getDescription() {
        if (status == Status.PREPARING) {
            return item.getName() + " PREPARING " + completedUnits + "/"
                    + item.getPreparationUnits();
        }
        return item.getName() + " " + status;
    }
}
