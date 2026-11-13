import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Restaurant {
    public static final int MAX_TURNS = 18;
    public static final int TARGET_PAID_CUSTOMERS = 4;
    public static final int TARGET_REVENUE_CENTS = 4500;
    public static final int TARGET_AVERAGE_SATISFACTION = 60;

    public enum Role { HOST, WAITER, CHEF, CASHIER }

    private final List<Customer> customers = new ArrayList<>();
    private final Queue<Customer> waitingQueue = new ArrayDeque<>();
    private final List<Table> tables = new ArrayList<>();
    private final Map<Role, Staff> staff = new EnumMap<>(Role.class);
    private final HappyHour happyHour = new HappyHour();
    private int turn;
    private int revenueCents;
    private int servedCount;
    private int unhappyDepartures;
    private int paidCount;
    private int paidSatisfactionTotal;
    private Phase phase = Phase.BETWEEN_TURNS;

    private enum Phase { BETWEEN_TURNS, HOST, WAITER, CHEF, RESOLUTION }

    public Restaurant() {
        tables.add(new Table(1));
        tables.add(new Table(2));
        staff.put(Role.WAITER, new Staff.Waiter());
        staff.put(Role.CHEF, new Staff.Chef());
        staff.put(Role.CASHIER, new Staff.Cashier());
        staff.put(Role.HOST, new Staff.Host());
    }

    public int getTurn() {
        return turn;
    }

    public int getRevenueCents() {
        return revenueCents;
    }

    public int getServedCount() {
        return servedCount;
    }

    public int getUnhappyDepartures() {
        return unhappyDepartures;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public double getAverageSatisfaction() {
        return paidCount == 0 ? 0.0 : (double) paidSatisfactionTotal / paidCount;
    }

    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(customers);
    }

    public List<Customer> getWaitingQueue() {
        return Collections.unmodifiableList(new ArrayList<>(waitingQueue));
    }

    public List<Table> getTables() {
        return Collections.unmodifiableList(tables);
    }

    public boolean canActivateHappyHour() {
        return phase == Phase.BETWEEN_TURNS && turn < MAX_TURNS && !happyHour.isUsed();
    }

    public int getHappyHourTurnsRemaining() {
        return happyHour.getTurnsRemaining();
    }

    public void startTurn() {
        startTurn(false);
    }

    public void startTurn(boolean activateHappyHour) {
        require(phase == Phase.BETWEEN_TURNS, "Finish the current turn first.");
        require(turn < MAX_TURNS, "The game has finished.");

        happyHour.startTurn(activateHappyHour);
        turn++;
        phase = Phase.HOST;

        if ((turn - 1) % 3 == 0) {
            int id = customers.size() + 1;
            Customer customer;

            if (id == 2) {
                customer = new VIPCustomer(id, happyHour);
            } else if (id == 3) {
                customer = new CriticCustomer(id, happyHour);
            } else {
                customer = new Customer(id, happyHour);
            }

            customers.add(customer);
            waitingQueue.add(customer);
        }

        if (happyHour.isActive()) {
            for (Customer customer : customers) {
                customer.recoverSatisfaction(15);
            }
        }
    }

    public Staff.Task createTask(Staff.Action action, int customerId) {
        require(action != null, "Choose a task.");
        if (action == Staff.Action.WAIT) {
            return Staff.Task.waiting();
        }
        Customer customer = findCustomer(customerId);
        if (action == Staff.Action.SEAT) {
            return Staff.Task.seating(customer, findFreeTable());
        }
        return Staff.Task.forCustomer(action, customer);
    }

    public void assignTask(Role role, Staff.Task task) {
        require(task != null, "Choose a task.");
        require(role == Role.HOST || role == Role.WAITER || role == Role.CHEF,
                "Only the Host, Waiter and Chef take manager assignments.");
        boolean hostTurn = role == Role.HOST && phase == Phase.HOST;
        boolean waiterTurn = role == Role.WAITER && phase == Phase.WAITER;
        boolean chefTurn = role == Role.CHEF && phase == Phase.CHEF;
        require(hostTurn || waiterTurn || chefTurn,
                "Staff must act in order: Host, Waiter, then Chef.");
        if (task.getCustomer() != null) {
            require(customers.contains(task.getCustomer()), "That customer is not in this game.");
        }
        if (task.getAction() == Staff.Action.SEAT && task.getTable() != null) {
            require(tables.contains(task.getTable()), "That table is not in this restaurant.");
        }

        staff.get(role).perform(task, turn);
        if (task.getAction() == Staff.Action.SEAT) {
            waitingQueue.remove(task.getCustomer());
        }
        if (task.getAction() == Staff.Action.SERVE) {
            servedCount++;
        }
        if (role == Role.HOST) {
            phase = Phase.WAITER;
        } else if (role == Role.WAITER) {
            phase = Phase.CHEF;
        } else {
            phase = Phase.RESOLUTION;
        }
    }

    public void finishTurn() {
        require(phase == Phase.RESOLUTION, "All staff choices must be completed.");

        for (Customer customer : customers) {
            customer.finishEating(turn);
        }
        collectPayment();
        updateWaitingCustomers();
        phase = Phase.BETWEEN_TURNS;
    }

    private void collectPayment() {
        Customer customer = findOldestReadyToPay();
        Staff.Task task;
        if (customer == null) {
            task = Staff.Task.waiting();
        } else {
            task = Staff.Task.forCustomer(Staff.Action.PAY, customer);
        }

        staff.get(Role.CASHIER).perform(task, turn);

        if (customer != null) {
            revenueCents += customer.getOrder().getBillCents();
            paidCount++;
            paidSatisfactionTotal += customer.getSatisfaction();
            releaseTable(customer);
        }
    }

    private void updateWaitingCustomers() {
        for (Customer customer : customers) {
            if (customer.isWaitingForService()) {
                customer.loseSatisfaction();
                if (customer.getSatisfaction() == 0) {
                    customer.leave();
                    waitingQueue.remove(customer);
                    releaseTable(customer);
                    unhappyDepartures++;
                }
            }
        }
    }

    public boolean isVictory() {
        return turn == MAX_TURNS && phase == Phase.BETWEEN_TURNS
                && paidCount >= TARGET_PAID_CUSTOMERS
                && revenueCents >= TARGET_REVENUE_CENTS
                && paidSatisfactionTotal >= TARGET_AVERAGE_SATISFACTION * paidCount;
    }

    public boolean canSeat() {
        return !waitingQueue.isEmpty() && findFreeTable() != null;
    }

    public boolean canTakeOrder() {
        for (Customer customer : customers) {
            if (customer.getStatus() == Customer.Status.SEATED && customer.getOrder() == null) {
                return true;
            }
        }
        return false;
    }

    public boolean canServe() {
        for (Customer customer : customers) {
            Order order = customer.getOrder();
            if (order != null && order.getStatus() == Order.Status.READY) {
                return true;
            }
        }
        return false;
    }

    private Customer findCustomer(int id) {
        for (Customer customer : customers) {
            if (customer.getId() == id) {
                return customer;
            }
        }
        throw new IllegalArgumentException("That customer does not exist.");
    }

    private Table findFreeTable() {
        for (Table table : tables) {
            if (table.isFree()) {
                return table;
            }
        }
        return null;
    }

    private Customer findOldestReadyToPay() {
        Customer oldest = null;
        for (Customer customer : customers) {
            if (customer.getStatus() != Customer.Status.READY_TO_PAY) {
                continue;
            }

            int readyTurn = customer.getOrder().getReadyToPayTurn();
            if (readyTurn >= turn) {
                continue;
            }

            if (oldest == null) {
                oldest = customer;
            } else {
                int oldestReadyTurn = oldest.getOrder().getReadyToPayTurn();
                boolean readyEarlier = readyTurn < oldestReadyTurn;
                boolean sameTurnEarlierArrival =
                        readyTurn == oldestReadyTurn && customer.getId() < oldest.getId();
                if (readyEarlier || sameTurnEarlierArrival) {
                    oldest = customer;
                }
            }
        }
        return oldest;
    }

    private void releaseTable(Customer customer) {
        for (Table table : tables) {
            if (table.getCustomer() == customer) {
                table.release();
                return;
            }
        }
    }

    public static final class HappyHour {
        private boolean used;
        private int turnsRemaining;

        private void startTurn(boolean activate) {
            require(!activate || !used, "Happy Hour can only be activated once.");

            if (turnsRemaining > 0) {
                turnsRemaining--;
            }
            if (activate) {
                used = true;
                turnsRemaining = 2;
            }
        }

        public boolean isUsed() {
            return used;
        }

        public boolean isActive() {
            return turnsRemaining > 0;
        }

        public int getTurnsRemaining() {
            return turnsRemaining;
        }

        public int applyToBill(int billCents) {
            if (isActive()) {
                return (int) ((billCents * 80L + 50) / 100);
            }
            return billCents;
        }
    }

    static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}