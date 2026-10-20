import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/** Reads manager choices and displays the game; Restaurant owns the rules. */
public class RestaurantRushApp {
    private final Restaurant restaurant = new Restaurant();
    private final Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        RestaurantRushApp app = new RestaurantRushApp();
        app.run();
    }

    public void run() {
        displayWelcome();
        while (restaurant.getTurn() < Restaurant.MAX_TURNS) {
            restaurant.startTurn();
            displayState();
            if (!readManagerChoices()) {
                System.out.println("\nInput closed. Game stopped before completion.");
                return;
            }
            restaurant.finishTurn();
            displayTurnSummary();
        }
        displayResult();
    }

    private void displayWelcome() {
        System.out.println("RESTAURANT RUSH - STAGE 1");
        System.out.println("Goal: 4 paid customers, $45.00 revenue, 60 average satisfaction.");
        System.out.println("18 turns | 2 tables | 1 Waiter | 1 Chef | 1 Cashier");
        for (Order.MenuItem item : Order.MenuItem.values()) {
            System.out.printf("%s: %s (%d preparation unit(s))%n",
                    item.getName(), money(item.getPriceCents()), item.getPreparationUnits());
        }
        System.out.println("Customer choices repeat: Salad, Burger, Pasta.");
        System.out.println("Enter customer numbers: for C1, enter 1.");
    }

    private void displayState() {
        System.out.println();
        System.out.printf("TURN %d/%d | Revenue %s | Paid avg %s%n",
                restaurant.getTurn(), Restaurant.MAX_TURNS,
                money(restaurant.getRevenueCents()), average());
        List<String> queue = new ArrayList<>();
        for (Customer customer : restaurant.getWaitingQueue()) {
            queue.add(customer.getLabel() + " satisfaction="
                    + customer.getSatisfaction() + " order=NONE");
        }
        System.out.println("Queue: " + (queue.isEmpty() ? "-" : String.join(" | ", queue)));
        for (Table table : restaurant.getTables()) {
            Customer customer = table.getCustomer();
            if (table.isFree()) {
                System.out.println("Table " + table.getId() + ": FREE");
            } else {
                String order = customer.getOrder() == null
                        ? "NONE" : customer.getOrder().getDescription();
                System.out.println("Table " + table.getId() + ": " + customer.getLabel()
                        + " satisfaction=" + customer.getSatisfaction() + " order=" + order);
            }
        }
        List<String> actions = new ArrayList<>();
        if (restaurant.canSeat()) {
            actions.add("seat");
        }
        if (restaurant.canTakeOrder()) {
            actions.add("take order");
        }
        if (restaurant.canServe()) {
            actions.add("serve");
        }
        actions.add("wait");
        System.out.println("Waiter available: " + String.join(" / ", actions));
        System.out.println("Chef available: " + chefOptions());
        System.out.println("Cashier: automatic, at most one eligible payment");
    }

    private boolean readManagerChoices() {
        if (!chooseWaiterTask()) {
            return false;
        }
        System.out.println("Chef available: " + chefOptions());
        return chooseChefTask();
    }

    private boolean chooseWaiterTask() {
        while (true) {
            Integer choice = readInt("Waiter [1 seat, 2 take order, 3 serve, 0 wait]: ");
            if (choice == null) {
                return false;
            }
            if (choice < 0 || choice > 3) {
                System.out.println("Invalid: choose 0, 1, 2 or 3.");
                continue;
            }
            Integer id = 0;
            if (choice != 0) {
                id = readInt("Customer number: ");
                if (id == null) {
                    return false;
                }
            }

            Staff.Action action;
            switch (choice) {
                case 1:
                    action = Staff.Action.SEAT;
                    break;
                case 2:
                    action = Staff.Action.TAKE_ORDER;
                    break;
                case 3:
                    action = Staff.Action.SERVE;
                    break;
                default:
                    action = Staff.Action.WAIT;
            }
            try {
                Staff.Task task = restaurant.createTask(action, id);
                restaurant.assignTask(Restaurant.WAITER_ID, task);
                if (action == Staff.Action.SEAT) {
                    System.out.println("Seated C" + id + ".");
                } else if (action == Staff.Action.TAKE_ORDER) {
                    Order order = task.getCustomer().getOrder();
                    System.out.println("C" + id + " ordered " + order.getItem().getName()
                            + " (" + money(order.getBillCents()) + ", "
                            + order.getItem().getPreparationUnits() + " unit(s)).");
                } else if (action == Staff.Action.SERVE) {
                    System.out.println("Served C" + id + ".");
                }
                return true;
            } catch (IllegalArgumentException exception) {
                System.out.println("Invalid: " + exception.getMessage());
            }
        }
    }

    private boolean chooseChefTask() {
        while (true) {
            Integer id = readInt("Chef [customer number to prepare, 0 wait]: ");
            if (id == null) {
                return false;
            }
            try {
                Staff.Action action;
                if (id == 0) {
                    action = Staff.Action.WAIT;
                } else {
                    action = Staff.Action.PREPARE;
                }

                Staff.Task task = restaurant.createTask(action, id);
                restaurant.assignTask(Restaurant.CHEF_ID, task);
                if (id != 0) {
                    System.out.println("C" + id + ": "
                            + task.getCustomer().getOrder().getDescription());
                }
                return true;
            } catch (IllegalArgumentException exception) {
                System.out.println("Invalid: " + exception.getMessage());
            }
        }
    }

    private String chefOptions() {
        List<String> labels = new ArrayList<>();
        for (Customer customer : restaurant.getCustomers()) {
            if (customer.getOrder() != null
                    && customer.getOrder().getStatus() == Order.Status.PREPARING) {
                labels.add(customer.getLabel());
            }
        }
        return labels.isEmpty() ? "wait" : "prepare " + String.join(", ", labels) + " / wait";
    }

    private void displayTurnSummary() {
        System.out.printf("End %d | Served %d | Unhappy departures %d"
                        + " | Revenue %s | Paid avg %s | Turns used %d%n",
                restaurant.getTurn(), restaurant.getServedCount(),
                restaurant.getUnhappyDepartures(), money(restaurant.getRevenueCents()),
                average(), restaurant.getTurn());
    }

    private void displayResult() {
        if (restaurant.isVictory()) {
            System.out.println("\nVICTORY");
        } else {
            System.out.println("\nDEFEAT");
        }
        System.out.printf("Paid customers: %d / target 4%n", restaurant.getPaidCount());
        System.out.printf("Revenue: %s / target $45.00%n", money(restaurant.getRevenueCents()));
        System.out.printf("Paid average satisfaction: %s / target 60%n", average());
    }

    // null means the input stream was closed, rather than an invalid choice.
    private Integer readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!input.hasNextLine()) {
                return null;
            }
            try {
                return Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException exception) {
                System.out.println("Invalid: enter a whole number.");
            }
        }
    }

    private String average() {
        if (restaurant.getPaidCount() == 0) {
            return "N/A";
        }
        return String.format(Locale.ROOT, "%.1f", restaurant.getAverageSatisfaction());
    }

    private String money(int cents) {
        return String.format(Locale.ROOT, "$%d.%02d", cents / 100, cents % 100);
    }
}
