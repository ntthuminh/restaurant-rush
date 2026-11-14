import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

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
            Integer happyHourChoice = chooseHappyHour();
            if (happyHourChoice == null) {
                System.out.println("\nInput closed. Game stopped before completion.");
                return;
            }
            restaurant.startTurn(happyHourChoice == 1);
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
        printHeading("RESTAURANT RUSH - STAGE 3");
        System.out.printf("Goal: %d paid customers, %s revenue, %d average satisfaction.%n",
                Restaurant.TARGET_PAID_CUSTOMERS, money(Restaurant.TARGET_REVENUE_CENTS),
                Restaurant.TARGET_AVERAGE_SATISFACTION);
        System.out.println("18 turns | 2 tables | 1 Host | 1 Waiter | 1 Chef | 1 Cashier");
        System.out.println("\nMENU");
        for (Order.MenuItem item : Order.MenuItem.values()) {
            System.out.printf("  %-10s %s (%d preparation unit(s))%n",
                    item.getName(), money(item.getPriceCents()), item.getPreparationUnits());
        }
        System.out.println("\nCUSTOMERS AND RULES");
        System.out.println("C2 is a VIP. C3 is a Critic. The other arrivals are Regular.");
        System.out.println("Regular/VIP IDs divisible by 4 choose a Salad + Pasta combo.");
        System.out.println("Other Regular/VIP choices repeat by ID: Salad, Burger, Pasta.");
        System.out.println("Salad + Pasta combo: $24.30, 3 preparation units.");
        System.out.println("Critics always choose Pasta.");
        System.out.println("Waiting loss: Regular 8, VIP 5, Critic 12. VIP bills get 10% off.");
        System.out.println("Happy Hour: activate once, lasting this turn and the next.");
        System.out.println("Active turns: 20% off new orders, +15 waiting satisfaction (maximum 100).");
        System.out.println("\nCONTROLS");
        System.out.println("The Host acts before the Waiter. Both can seat customers.");
        System.out.println("Enter customer numbers: for C1, enter 1.");
    }

    private void displayState() {
        printHeading("TURN " + restaurant.getTurn() + " / " + Restaurant.MAX_TURNS);
        System.out.printf("Revenue: %s    Paid average satisfaction: %s%n",
                money(restaurant.getRevenueCents()), average());
        System.out.println("Happy Hour: " + restaurant.getHappyHourTurnsRemaining()
                + " turn(s) remaining");

        System.out.println("\nQUEUE");
        if (restaurant.getWaitingQueue().isEmpty()) {
            System.out.println("  Empty");
        }
        for (Customer customer : restaurant.getWaitingQueue()) {
            System.out.println("  " + customer.getLabel() + " | Satisfaction: "
                    + customer.getSatisfaction() + " | Order: NONE");
        }

        System.out.println("\nTABLES");
        for (Table table : restaurant.getTables()) {
            Customer customer = table.getCustomer();
            if (table.isFree()) {
                System.out.println("  Table " + table.getId() + ": FREE");
            } else {
                String order = customer.getOrder() == null
                        ? "NONE" : customer.getOrder().getDescription();
                System.out.println("  Table " + table.getId() + ": " + customer.getLabel()
                        + " | Satisfaction: " + customer.getSatisfaction());
                System.out.println("    Order: " + order);
            }
        }

        System.out.println("\nSTAFF AVAILABILITY");
        System.out.println("  Host:    " + (restaurant.canSeat() ? "seat / wait" : "wait"));
        System.out.println("  Waiter:  " + waiterOptions());
        System.out.println("  Chef:    " + chefOptions());
        System.out.println("  Cashier: automatic, at most one eligible payment");
    }

    private Integer chooseHappyHour() {
        if (!restaurant.canActivateHappyHour()) {
            return 0;
        }

        while (true) {
            Integer choice = readInt("\nStart turn " + (restaurant.getTurn() + 1)
                    + " - activate Happy Hour? [1 yes, 0 no]: ");
            if (choice == null || choice == 0 || choice == 1) {
                return choice;
            }
            System.out.println("Invalid: choose 0 or 1.");
        }
    }

    private boolean chooseHostTask() {
        while (true) {
            Integer id = readInt("Host [customer number to seat, 0 wait]: ");
            if (id == null) {
                return false;
            }

            Staff.Action action = Staff.Action.SEAT;
            if (id == 0) {
                action = Staff.Action.WAIT;
            }

            try {
                Staff.Task task = restaurant.createTask(action, id);
                restaurant.assignTask(Restaurant.Role.HOST, task);
                if (id != 0) {
                    System.out.println("Host seated C" + id + ".");
                }
                return true;
            } catch (IllegalArgumentException exception) {
                System.out.println("Invalid: " + exception.getMessage());
            }
        }
    }

    private boolean readManagerChoices() {
        System.out.println("\n--- Host ---");
        if (!chooseHostTask()) {
            return false;
        }
        System.out.println("\n--- Waiter ---");
        System.out.println("Available: " + waiterOptions());
        if (!chooseWaiterTask()) {
            return false;
        }
        System.out.println("\n--- Chef ---");
        System.out.println("Available: " + chefOptions());
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
                restaurant.assignTask(Restaurant.Role.WAITER, task);
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
                restaurant.assignTask(Restaurant.Role.CHEF, task);
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

    private String waiterOptions() {
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
        return String.join(" / ", actions);
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
        System.out.println("\n------------------------------------------------------------");
        System.out.printf("END OF TURN %d - TOTALS%nServed: %d    Unhappy departures: %d"
                        + "%nRevenue: %s    Paid average satisfaction: %s%nTurns used: %d%n",
                restaurant.getTurn(), restaurant.getServedCount(),
                restaurant.getUnhappyDepartures(), money(restaurant.getRevenueCents()),
                average(), restaurant.getTurn());
        System.out.println("------------------------------------------------------------");
    }

    private void displayResult() {
        printHeading(restaurant.isVictory() ? "VICTORY" : "DEFEAT");
        System.out.printf("Paid customers: %d / target %d%n",
                restaurant.getPaidCount(), Restaurant.TARGET_PAID_CUSTOMERS);
        System.out.printf("Revenue: %s / target %s%n",
                money(restaurant.getRevenueCents()), money(Restaurant.TARGET_REVENUE_CENTS));
        System.out.printf("Paid average satisfaction: %s / target %d%n",
                average(), Restaurant.TARGET_AVERAGE_SATISFACTION);
    }

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

    private void printHeading(String title) {
        System.out.println("\n============================================================");
        System.out.println(title);
        System.out.println("============================================================");
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