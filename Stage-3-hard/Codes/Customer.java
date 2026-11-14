public class Customer {
    public static final int WAITING_LOSS = 8;
    public static final int MAX_SATISFACTION = 100;

    public enum Status { WAITING, SEATED, SERVED, READY_TO_PAY, PAID, LEFT }

    private final int id;
    private final PricingPolicy pricing;
    private final Order.Meal plannedMeal;
    private final Order.Drink plannedDrink;
    private int satisfaction = MAX_SATISFACTION;
    private Status status = Status.WAITING;
    private Order order;

    public Customer(int id, PricingPolicy pricing) {
        this(id, pricing, null, null);
    }

    public Customer(int id, PricingPolicy pricing, Order.Meal plannedMeal, Order.Drink plannedDrink) {
        Preconditions.check(pricing != null, "A pricing policy is required.");
        this.id = id;
        this.pricing = pricing;
        this.plannedMeal = plannedMeal;
        this.plannedDrink = plannedDrink;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return "C" + id + " (" + getType() + ")";
    }

    public String getType() {
        return "Regular";
    }

    protected int getWaitingLoss() {
        return WAITING_LOSS;
    }

    protected int calculateBill(int priceCents) {
        return priceCents;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public Status getStatus() {
        return status;
    }

    public Order getOrder() {
        return order;
    }

    public Order.Meal chooseFood() {
        if (plannedMeal != null) {
            return plannedMeal;
        }
        if (id % 4 == 0) {
            return new Order.ComboMeal(Order.MenuItem.SALAD, Order.MenuItem.PASTA);
        }
        Order.MenuItem[] items = Order.MenuItem.values();
        return items[(id - 1) % items.length];
    }

    public Order.Drink chooseDrink() {
        return plannedDrink;
    }

    public void seat() {
        Preconditions.check(status == Status.WAITING, "That customer is not in the waiting queue.");
        status = Status.SEATED;
    }

    public Order placeOrder() {
        Preconditions.check(status == Status.SEATED, "That customer is not seated.");
        Preconditions.check(order == null, "That customer has already placed an order.");
        Order.Meal item = chooseFood();
        Order.Drink drink = chooseDrink();
        int priceCents = item.getPriceCents() + (drink == null ? 0 : drink.getPriceCents());
        int billCents = pricing.applyToBill(calculateBill(priceCents));
        order = new Order(item, drink, billCents);
        return order;
    }

    public void receiveFood(int turn) {
        Preconditions.check(status == Status.SEATED, "That customer is not waiting for food.");
        Preconditions.check(order != null, "That customer has not placed an order.");
        order.serve(turn);
        status = Status.SERVED;
    }

    public void finishEating(int turn) {
        if (status == Status.SERVED && order.getServedTurn() < turn) {
            order.finishEating(turn);
            status = Status.READY_TO_PAY;
        }
    }

    public void pay() {
        Preconditions.check(status == Status.READY_TO_PAY, "That customer is not ready to pay.");
        order.markPaid();
        status = Status.PAID;
    }

    public boolean isWaitingForService() {
        return status == Status.WAITING || status == Status.SEATED;
    }

    public void loseSatisfaction() {
        if (isWaitingForService()) {
            satisfaction = Math.max(0, satisfaction - getWaitingLoss());
        }
    }

    public void recoverSatisfaction(int points) {
        if (isWaitingForService()) {
            satisfaction = Math.min(MAX_SATISFACTION, satisfaction + points);
        }
    }

    public void leave() {
        Preconditions.check(isWaitingForService(), "Only a waiting customer can abandon the restaurant.");
        if (order != null) {
            order.cancel();
        }
        status = Status.LEFT;
    }
}