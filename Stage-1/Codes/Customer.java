public class Customer {
    public enum Status { WAITING, SEATED, SERVED, READY_TO_PAY, PAID, LEFT }

    private final int id;
    private int satisfaction = 100;
    private Status status = Status.WAITING;
    private Order order;

    public Customer(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return "C" + id;
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

    public Order.MenuItem chooseFood() {
        Order.MenuItem[] items = Order.MenuItem.values();
        // Keep food choices reproducible: Salad, Burger, Pasta, then repeat.
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
        order = new Order(chooseFood());
        return order;
    }

    public void receiveFood(int turn) {
        Restaurant.require(status == Status.SEATED, "That customer is not waiting for food.");
        Restaurant.require(order != null, "That customer has not placed an order.");
        order.serve(turn);
        status = Status.SERVED;
    }

    // A customer served this turn must wait until the following turn to finish.
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
            satisfaction = Math.max(0, satisfaction - 8);
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
