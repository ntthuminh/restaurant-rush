import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Restaurant {
    public static final int MAX_TURNS = 18;
    public static final int TABLE_COUNT = 2;

    public enum Role {
        HOST("Host"), WAITER("Waiter"), CHEF("Chef"), BARTENDER("Bartender"), CASHIER("Cashier");

        private final String label;

        Role(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private static final int BETWEEN_TURNS = -1;

    private final GameMode mode;
    private final List<Role> turnOrder;
    private final List<Customer> customers = new ArrayList<>();
    private final Queue<Customer> waitingQueue = new ArrayDeque<>();
    private final List<Table> tables = new ArrayList<>();
    private final Map<Role, Staff> staff = new EnumMap<>(Role.class);
    private final HappyHour happyHour = new HappyHour();
    private final ScoreBoard score = new ScoreBoard();
    private Customer lastPaid;
    private int turn;
    private int nextRole = BETWEEN_TURNS;

    public Restaurant(GameMode mode) {
        Preconditions.check(mode != null, "Choose a game mode.");
        this.mode = mode;
        this.turnOrder = new ArrayList<>(mode.getManagerRoles());
        for (int id = 1; id <= TABLE_COUNT; id++) {
            tables.add(new Table(id));
        }
        for (Role role : turnOrder) {
            staff.put(role, createStaff(role));
        }
        staff.put(Role.CASHIER, createStaff(Role.CASHIER));
    }

    private static Staff createStaff(Role role) {
        switch (role) {
            case HOST:
                return new Staff.Host();
            case WAITER:
                return new Staff.Waiter();
            case CHEF:
                return new Staff.Chef();
            case BARTENDER:
                return new Staff.Bartender();
            default:
                return new Staff.Cashier();
        }
    }

    public GameMode getMode() {
        return mode;
    }

    public ScoreBoard getScore() {
        return score;
    }

    public int getTurn() {
        return turn;
    }

    public List<Role> getTurnOrder() {
        return Collections.unmodifiableList(turnOrder);
    }

    public boolean hasRole(Role role) {
        return staff.containsKey(role);
    }

    public Customer getLastPaid() {
        return lastPaid;
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
        return nextRole == BETWEEN_TURNS && turn < MAX_TURNS && !happyHour.isUsed();
    }

    public int getHappyHourTurnsRemaining() {
        return happyHour.getTurnsRemaining();
    }

    public void startTurn(boolean activateHappyHour) {
        Preconditions.check(nextRole == BETWEEN_TURNS, "Finish the current turn first.");
        Preconditions.check(turn < MAX_TURNS, "The game has finished.");

        happyHour.startTurn(activateHappyHour);
        turn++;
        nextRole = 0;

        if ((turn - 1) % mode.getArrivalInterval() == 0) {
            Customer customer = mode.createCustomer(customers.size() + 1, happyHour);
            customers.add(customer);
            waitingQueue.add(customer);
        }

        if (happyHour.isActive()) {
            for (Customer customer : customers) {
                customer.recoverSatisfaction(HappyHour.SATISFACTION_RECOVERY);
            }
        }
    }

    public Staff.Task createTask(Staff.Action action, int customerId) {
        Preconditions.check(action != null, "Choose a task.");
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
        Preconditions.check(task != null, "Choose a task.");
        Preconditions.check(nextRole >= 0 && nextRole < turnOrder.size()
                && turnOrder.get(nextRole) == role, "Staff must act in the turn order.");
        if (task.getCustomer() != null) {
            Preconditions.check(customers.contains(task.getCustomer()), "That customer is not in this game.");
        }
        if (task.getAction() == Staff.Action.SEAT && task.getTable() != null) {
            Preconditions.check(tables.contains(task.getTable()), "That table is not in this restaurant.");
        }

        staff.get(role).perform(task, turn);
        if (task.getAction() == Staff.Action.SEAT) {
            waitingQueue.remove(task.getCustomer());
        }
        if (task.getAction() == Staff.Action.SERVE) {
            score.recordServe();
        }
        nextRole++;
    }

    public void finishTurn() {
        Preconditions.check(nextRole == turnOrder.size(), "All staff choices must be completed.");

        for (Customer customer : customers) {
            customer.finishEating(turn);
        }
        collectPayment();
        updateWaitingCustomers();
        nextRole = BETWEEN_TURNS;
    }

    private void collectPayment() {
        Customer customer = findOldestReadyToPay();
        lastPaid = customer;
        Staff.Task task = customer == null
                ? Staff.Task.waiting()
                : Staff.Task.forCustomer(Staff.Action.PAY, customer);

        staff.get(Role.CASHIER).perform(task, turn);

        if (customer != null) {
            score.recordPayment(mode.amountPaid(customer), customer.getOrder().getBillCents(),
                    customer.getSatisfaction());
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
                    score.recordDeparture();
                }
            }
        }
    }

    public boolean isGameOver() {
        return turn == MAX_TURNS && nextRole == BETWEEN_TURNS;
    }

    public boolean isVictory() {
        return isGameOver() && score.meetsTargets(mode);
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
        throw new InvalidActionException("That customer does not exist.");
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
}