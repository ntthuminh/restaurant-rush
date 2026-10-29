import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

/** Coordinates turns and keeps the restaurant's customers, tables and staff. */
public class Restaurant {
    public static final int MAX_TURNS = 18;
    public static final int WAITER_ID = 1;
    public static final int CHEF_ID = 2;
    private static final int CASHIER_ID = 3;

    private final List<Customer> customers = new ArrayList<>();
    private final Queue<Customer> waitingQueue = new ArrayDeque<>();
    private final List<Order> orders = new ArrayList<>();
    private final List<Table> tables = new ArrayList<>();
    private final List<Staff> staff = new ArrayList<>();
    private int turn;
    private int revenueCents;
    private int servedCount;
    private int unhappyDepartures;
    private int paidCount;
    private int paidSatisfactionTotal;
    private Phase phase = Phase.BETWEEN_TURNS;

    private enum Phase { BETWEEN_TURNS, WAITER, CHEF, RESOLUTION }

    public Restaurant() {
        tables.add(new Table(1));
        tables.add(new Table(2));
        staff.add(new Staff.Waiter());
        staff.add(new Staff.Chef());
        staff.add(new Staff.Cashier());
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

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    public void startTurn() {
        require(phase == Phase.BETWEEN_TURNS, "Finish the current turn first.");
        require(turn < MAX_TURNS, "The game has finished.");
        turn++;
        phase = Phase.WAITER;
        // Arrivals are scheduled for turns 1, 4, 7, 10, 13 and 16.
        if ((turn - 1) % 3 == 0) {
            int id = customers.size() + 1;
            Customer customer;

            if (id == 2) {
                customer = new Customer.VIPCustomer(id);
            } else if (id == 3) {
                customer = new Customer.CriticCustomer(id);
            } else {
                customer = new Customer(id);
            }

            customers.add(customer);
            waitingQueue.add(customer);
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

    public void assignTask(int staffId, Staff.Task task) {
        require(task != null, "Choose a task.");
        require(staffId == WAITER_ID || staffId == CHEF_ID,
                "Only the Waiter and Chef take manager assignments.");
        boolean waiterTurn = staffId == WAITER_ID && phase == Phase.WAITER;
        boolean chefTurn = staffId == CHEF_ID && phase == Phase.CHEF;
        require(waiterTurn || chefTurn, "Staff must act in order: Waiter, then Chef.");
        if (task.getCustomer() != null) {
            require(customers.contains(task.getCustomer()), "That customer is not in this game.");
        }
        if (task.getAction() == Staff.Action.SEAT && task.getTable() != null) {
            require(tables.contains(task.getTable()), "That table is not in this restaurant.");
        }

        Staff assignedStaff = staff.get(staffId - 1);
        assignedStaff.perform(task, turn);
        if (task.getAction() == Staff.Action.SEAT) {
            waitingQueue.remove(task.getCustomer());
        }
        if (task.getAction() == Staff.Action.TAKE_ORDER) {
            orders.add(task.getCustomer().getOrder());
        }
        if (task.getAction() == Staff.Action.SERVE) {
            servedCount++;
        }
        phase = staffId == WAITER_ID ? Phase.CHEF : Phase.RESOLUTION;
    }

    public void finishTurn() {
        require(phase == Phase.RESOLUTION, "Both staff choices must be completed.");

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

        Staff cashier = staff.get(CASHIER_ID - 1);
        cashier.perform(task, turn);

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
                && paidCount >= 4 && revenueCents >= 4500
                && paidSatisfactionTotal >= 60 * paidCount;
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
        for (Order order : orders) {
            if (order.getStatus() == Order.Status.READY) {
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
            // Becoming ready and paying must happen on different turns.
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

    static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
