public class Customer {
    public enum Status { WAITING, SEATED, SERVED, READY_TO_PAY, PAID, LEFT }

    private final int id;
    private final Restaurant.HappyHour happyHour;
    private int satisfaction = 100;
    private Status status = Status.WAITING;
    private Order order;

    public Customer(int id, Restaurant.HappyHour happyHour) {
        Restaurant.require(happyHour != null, "A pricing policy is required.");
        this.id = id;
        this.happyHour = happyHour;
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
        return 8;
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
        if (id % 4 == 0) {
            return new Order.ComboMeal(Order.MenuItem.SALAD, Order.MenuItem.PASTA);
        }

        Order.MenuItem[] items = Order.MenuItem.values();
        int menuIndex = (id - 1) % items.length;
        return items[menuIndex];
    }

    public void seat() {
        Restaurant.require(status == Status.WAITING,
                "That customer is not in the waiting queue.");
        status = Status.SEATED;
    }

    public Order placeOrder() {
        Restaurant.require(status == Status.SEATED, "That customer is not seated.");
        Restaurant.require(order == null, "That customer has already placed an order.");
        Order.Meal item = chooseFood();
        int billCents = calculateBill(item.getPriceCents());
        billCents = happyHour.applyToBill(billCents);
        order = new Order(item, billCents);
        return order;
    }

    public void receiveFood(int turn) {
        Restaurant.require(status == Status.SEATED, "That customer is not waiting for food.");
        Restaurant.require(order != null, "That customer has not placed an order.");
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
        Restaurant.require(status == Status.READY_TO_PAY, "That customer is not ready to pay.");
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
            satisfaction = Math.min(100, satisfaction + points);
        }
    }

    public void leave() {
        Restaurant.require(isWaitingForService(),
                "Only a waiting customer can abandon the restaurant.");
        if (order != null) {
            order.cancel();
        }
        status = Status.LEFT;
    }
}