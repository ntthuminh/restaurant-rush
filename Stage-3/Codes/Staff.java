public abstract class Staff {
    public enum Action { WAIT, SEAT, TAKE_ORDER, SERVE, PREPARE, PAY }

    public static final class Task {
        private final Action action;
        private final Customer customer;
        private final Table table;

        private Task(Action action, Customer customer, Table table) {
            Restaurant.require(action != null, "Choose a task.");
            this.action = action;
            this.customer = customer;
            this.table = table;
        }

        public static Task waiting() {
            return new Task(Action.WAIT, null, null);
        }

        public static Task seating(Customer customer, Table table) {
            return new Task(Action.SEAT, customer, table);
        }

        public static Task forCustomer(Action action, Customer customer) {
            Restaurant.require(action != Action.WAIT && action != Action.SEAT,
                    "Use the waiting or seating task factory for that action.");
            return new Task(action, customer, null);
        }

        public Action getAction() {
            return action;
        }

        public Customer getCustomer() {
            return customer;
        }

        public Table getTable() {
            return table;
        }
    }

    private int lastActionTurn = -1;

    public final void perform(Task task, int turn) {
        Restaurant.require(task != null, "Choose a task.");
        Restaurant.require(turn > 0, "The turn must be positive.");
        Restaurant.require(turn > lastActionTurn, "This staff member has already acted this turn.");
        if (task.getAction() != Action.WAIT) {
            Restaurant.require(task.getCustomer() != null, "Choose a customer.");
            validateTask(task);
            executeTask(task, turn);
        }
        lastActionTurn = turn;
    }

    protected abstract void validateTask(Task task);
    protected abstract void executeTask(Task task, int turn);

    protected final void validateSeating(Task task) {
        Restaurant.require(task.getCustomer().getStatus() == Customer.Status.WAITING,
                "That customer is not in the waiting queue.");
        Restaurant.require(task.getTable() != null, "Both tables are occupied.");
        Restaurant.require(task.getTable().isFree(), "That table is occupied.");
    }

    public static final class Host extends Staff {
        @Override
        protected void validateTask(Task task) {
            Restaurant.require(task.getAction() == Action.SEAT, "The Host can only seat customers.");
            validateSeating(task);
        }

        @Override
        protected void executeTask(Task task, int turn) {
            task.getTable().seat(task.getCustomer());
        }
    }

    public static final class Waiter extends Staff {
        @Override
        protected void validateTask(Task task) {
            Customer customer = task.getCustomer();
            switch (task.getAction()) {
                case SEAT:
                    validateSeating(task);
                    break;
                case TAKE_ORDER:
                    Restaurant.require(customer.getStatus() == Customer.Status.SEATED,
                            "That customer is not seated.");
                    Restaurant.require(customer.getOrder() == null,
                            "That customer has already placed an order.");
                    break;
                case SERVE:
                    Restaurant.require(customer.getStatus() == Customer.Status.SEATED,
                            "That customer is not waiting for food.");
                    Restaurant.require(customer.getOrder() != null,
                            "That customer has not placed an order.");
                    Restaurant.require(customer.getOrder().getStatus() == Order.Status.READY,
                            "That order is not READY.");
                    break;
                default:
                    throw new IllegalArgumentException("The Waiter can seat, take orders or serve.");
            }
        }

        @Override
        protected void executeTask(Task task, int turn) {
            switch (task.getAction()) {
                case SEAT:
                    task.getTable().seat(task.getCustomer());
                    break;
                case TAKE_ORDER:
                    task.getCustomer().placeOrder();
                    break;
                case SERVE:
                    task.getCustomer().receiveFood(turn);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported Waiter task.");
            }
        }
    }

    public static final class Chef extends Staff {
        @Override
        protected void validateTask(Task task) {
            Restaurant.require(task.getAction() == Action.PREPARE, "The Chef can only prepare food.");
            Restaurant.require(task.getCustomer().getOrder() != null,
                    "That customer has not placed an order.");
            Restaurant.require(task.getCustomer().getOrder().getStatus() == Order.Status.PREPARING,
                    "That order does not need preparation.");
        }

        @Override
        protected void executeTask(Task task, int turn) {
            task.getCustomer().getOrder().prepare();
        }
    }

    public static final class Cashier extends Staff {
        @Override
        protected void validateTask(Task task) {
            Restaurant.require(task.getAction() == Action.PAY, "The Cashier can only collect payment.");
            Restaurant.require(task.getCustomer().getStatus() == Customer.Status.READY_TO_PAY,
                    "That customer is not ready to pay.");
        }

        @Override
        protected void executeTask(Task task, int turn) {
            Restaurant.require(task.getCustomer().getOrder().getReadyToPayTurn() < turn,
                    "A newly ready customer cannot pay until the next turn.");
            task.getCustomer().pay();
        }
    }
}