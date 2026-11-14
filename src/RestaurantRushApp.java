import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;

public class RestaurantRushApp {
    private static final Staff.Action[] WAITER_ACTIONS = {
            Staff.Action.WAIT, Staff.Action.SEAT, Staff.Action.TAKE_ORDER, Staff.Action.SERVE
    };

    private final InputSource input;
    private Restaurant restaurant;

    public RestaurantRushApp(InputSource input) {
        this.input = input;
    }

    public static void main(String[] args) {
        if (args.length != 0 && !(args.length == 2
                && (args[0].equals("--record") || args[0].equals("--replay")))) {
            System.out.println("Usage: java RestaurantRushApp [--record FILE | --replay FILE]");
            return;
        }
        try (InputSource input = openInput(args)) {
            new RestaurantRushApp(input).run();
        } catch (IOException exception) {
            System.out.println("Cannot use that file: " + exception.getMessage());
        }
    }

    private static InputSource openInput(String[] args) throws IOException {
        if (args.length == 0) {
            return new ConsoleInput();
        }
        if (args[0].equals("--record")) {
            System.out.println("Recording choices to " + args[1]);
            return new RecordingInput(new ConsoleInput(), Paths.get(args[1]));
        }
        InputSource replay = new ReplayInput(Paths.get(args[1]));
        System.out.println("REPLAY of " + args[1]);
        return replay;
    }

    public void run() {
        GameMode mode = chooseMode();
        if (mode == null) {
            System.out.println("\nInput closed. Game stopped before completion.");
            return;
        }
        restaurant = new Restaurant(mode);
        displayWelcome();
        while (restaurant.getTurn() < Restaurant.MAX_TURNS) {
            OptionalInt happyHourChoice = chooseHappyHour();
            if (!happyHourChoice.isPresent()) {
                System.out.println("\nInput closed. Game stopped before completion.");
                return;
            }
            restaurant.startTurn(happyHourChoice.getAsInt() == 1);
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

    private GameMode chooseMode() {
        printHeading("RESTAURANT RUSH");
        System.out.println("1. Normal - the Stage 3 game");
        System.out.println("2. Hard - a customer every 2 turns, drinks made by a Bartender, and tips");
        while (true) {
            OptionalInt choice = readInt("Choose a mode [1 normal, 2 hard]: ");
            if (!choice.isPresent()) {
                return null;
            }
            if (choice.getAsInt() == 1) {
                System.out.println();
                return new NormalMode();
            }
            if (choice.getAsInt() == 2) {
                System.out.println();
                return new HardMode();
            }
            System.out.println("Invalid: choose 1 or 2.");
        }
    }

    private void displayWelcome() {
        GameMode mode = restaurant.getMode();
        printHeading(mode.getTitle());
        System.out.printf("Goal: %d paid customers, %s revenue, %d average satisfaction.%n",
                mode.getTargetPaidCustomers(), money(mode.getTargetRevenueCents()),
                mode.getTargetAverageSatisfaction());
        StringBuilder setup = new StringBuilder();
        setup.append(Restaurant.MAX_TURNS).append(" turns | ")
                .append(Restaurant.TABLE_COUNT).append(" tables");
        for (Restaurant.Role role : Restaurant.Role.values()) {
            if (restaurant.hasRole(role)) {
                setup.append(" | 1 ").append(role.getLabel());
            }
        }
        System.out.println(setup);
        System.out.println("\nMENU");
        for (Order.MenuItem item : Order.MenuItem.values()) {
            System.out.printf("  %-10s %s (%d preparation unit(s))%n",
                    item.getName(), money(item.getPriceCents()), item.getPreparationUnits());
        }
        for (Order.Drink drink : mode.getDrinks()) {
            System.out.printf("  %-10s %s (%d preparation unit(s))%n",
                    drink.getName(), money(drink.getPriceCents()), drink.getPreparationUnits());
        }
        System.out.println("\nCUSTOMERS AND RULES");
        for (String line : mode.getRuleLines()) {
            System.out.println(line);
        }
        System.out.printf("Waiting loss: Regular %d, VIP %d, Critic %d. VIP bills get %d%% off.%n",
                Customer.WAITING_LOSS, VIPCustomer.WAITING_LOSS, CriticCustomer.WAITING_LOSS,
                VIPCustomer.DISCOUNT_PERCENT);
        System.out.printf("Happy Hour: activate once, lasting %s.%n",
                HappyHour.DURATION_TURNS == 2 ? "this turn and the next"
                        : HappyHour.DURATION_TURNS + " turns");
        System.out.printf("Active turns: %d%% off new orders, +%d waiting satisfaction (maximum %d).%n",
                HappyHour.DISCOUNT_PERCENT, HappyHour.SATISFACTION_RECOVERY, Customer.MAX_SATISFACTION);
        System.out.println("\nCONTROLS");
        System.out.println("The Host acts before the Waiter. Both can seat customers.");
        System.out.println("Enter customer numbers: for C1, enter 1.");
    }

    private void displayState() {
        ScoreBoard score = restaurant.getScore();
        printHeading("TURN " + restaurant.getTurn() + " / " + Restaurant.MAX_TURNS);
        System.out.printf("Revenue: %s    Paid average satisfaction: %s%n",
                money(score.getRevenueCents()), average());
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
        for (Restaurant.Role role : restaurant.getTurnOrder()) {
            System.out.printf("  %-10s %s%n", role.getLabel() + ":", optionsFor(role));
        }
        System.out.printf("  %-10s %s%n", "Cashier:", "automatic, at most one eligible payment");
    }

    private OptionalInt chooseHappyHour() {
        if (!restaurant.canActivateHappyHour()) {
            return OptionalInt.of(0);
        }
        while (true) {
            OptionalInt choice = readInt("\nStart turn " + (restaurant.getTurn() + 1)
                    + " - activate Happy Hour? [1 yes, 0 no]: ");
            if (!choice.isPresent() || choice.getAsInt() == 0 || choice.getAsInt() == 1) {
                return choice;
            }
            System.out.println("Invalid: choose 0 or 1.");
        }
    }

    private boolean readManagerChoices() {
        List<Restaurant.Role> order = restaurant.getTurnOrder();
        for (int i = 0; i < order.size(); i++) {
            Restaurant.Role role = order.get(i);
            System.out.println("\n--- " + role.getLabel() + " ---");
            if (i > 0) {
                System.out.println("Available: " + optionsFor(role));
            }
            if (!chooseTask(role)) {
                return false;
            }
        }
        return true;
    }

    private boolean chooseTask(Restaurant.Role role) {
        while (true) {
            OptionalInt first = readInt(promptFor(role));
            if (!first.isPresent()) {
                return false;
            }
            Staff.Action action;
            int customerId;
            if (role == Restaurant.Role.WAITER) {
                int choice = first.getAsInt();
                if (choice < 0 || choice >= WAITER_ACTIONS.length) {
                    System.out.println("Invalid: choose 0, 1, 2 or 3.");
                    continue;
                }
                action = WAITER_ACTIONS[choice];
                customerId = 0;
                if (action != Staff.Action.WAIT) {
                    OptionalInt id = readInt("Customer number: ");
                    if (!id.isPresent()) {
                        return false;
                    }
                    customerId = id.getAsInt();
                }
            } else {
                customerId = first.getAsInt();
                action = customerId == 0 ? Staff.Action.WAIT : actionFor(role);
            }

            try {
                Staff.Task task = restaurant.createTask(action, customerId);
                restaurant.assignTask(role, task);
                printResult(role, task);
                return true;
            } catch (InvalidActionException exception) {
                System.out.println("Invalid: " + exception.getMessage());
            }
        }
    }

    private String promptFor(Restaurant.Role role) {
        switch (role) {
            case HOST:
                return "Host [customer number to seat, 0 wait]: ";
            case WAITER:
                return "Waiter [1 seat, 2 take order, 3 serve, 0 wait]: ";
            case CHEF:
                return "Chef [customer number to prepare, 0 wait]: ";
            default:
                return "Bartender [customer number to make a drink, 0 wait]: ";
        }
    }

    private Staff.Action actionFor(Restaurant.Role role) {
        switch (role) {
            case HOST:
                return Staff.Action.SEAT;
            case CHEF:
                return Staff.Action.PREPARE;
            default:
                return Staff.Action.MIX_DRINK;
        }
    }

    private void printResult(Restaurant.Role role, Staff.Task task) {
        Customer customer = task.getCustomer();
        switch (task.getAction()) {
            case SEAT:
                System.out.println((role == Restaurant.Role.HOST ? "Host seated C" : "Seated C")
                        + customer.getId() + ".");
                break;
            case TAKE_ORDER:
                Order order = customer.getOrder();
                String units = String.valueOf(order.getItem().getPreparationUnits());
                if (order.getDrink() != null) {
                    units += " + " + order.getDrink().getPreparationUnits();
                }
                System.out.println("C" + customer.getId() + " ordered " + order.getName()
                        + " (" + money(order.getBillCents()) + ", " + units + " unit(s)).");
                break;
            case SERVE:
                System.out.println("Served C" + customer.getId() + ".");
                break;
            case PREPARE:
            case MIX_DRINK:
                System.out.println("C" + customer.getId() + ": " + customer.getOrder().getDescription());
                break;
            default:
                break;
        }
    }

    private String optionsFor(Restaurant.Role role) {
        switch (role) {
            case HOST:
                return restaurant.canSeat() ? "seat / wait" : "wait";
            case WAITER:
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
            default:
                List<String> labels = new ArrayList<>();
                for (Customer customer : restaurant.getCustomers()) {
                    Order order = customer.getOrder();
                    boolean needsWork = order != null && (role == Restaurant.Role.CHEF
                            ? order.needsCooking() : order.needsMixing());
                    if (needsWork) {
                        labels.add(customer.getLabel());
                    }
                }
                String verb = role == Restaurant.Role.CHEF ? "prepare " : "make drink ";
                return labels.isEmpty() ? "wait" : verb + String.join(", ", labels) + " / wait";
        }
    }

    private void displayTurnSummary() {
        System.out.println("\n------------------------------------------------------------");
        ScoreBoard score = restaurant.getScore();
        Customer paid = restaurant.getLastPaid();
        if (restaurant.getMode().getTipCentsPerPoint() > 0 && paid != null) {
            int bill = paid.getOrder().getBillCents();
            int tip = restaurant.getMode().amountPaid(paid) - bill;
            System.out.println(paid.getLabel() + " paid " + money(bill) + " + tip " + money(tip)
                    + " (satisfaction " + paid.getSatisfaction() + ")");
        }
        System.out.printf("END OF TURN %d - TOTALS%nServed: %d    Unhappy departures: %d"
                        + "%nRevenue: %s    Paid average satisfaction: %s%nTurns used: %d%n",
                restaurant.getTurn(), score.getServedCount(), score.getUnhappyDepartures(),
                money(score.getRevenueCents()), average(), restaurant.getTurn());
        System.out.println("------------------------------------------------------------");
    }

    private void displayResult() {
        ScoreBoard score = restaurant.getScore();
        GameMode mode = restaurant.getMode();
        printHeading(restaurant.isVictory() ? "VICTORY" : "DEFEAT");
        System.out.printf("Paid customers: %d / target %d%n",
                score.getPaidCount(), mode.getTargetPaidCustomers());
        System.out.printf("Revenue: %s / target %s%n",
                money(score.getRevenueCents()), money(mode.getTargetRevenueCents()));
        if (mode.getTipCentsPerPoint() > 0) {
            System.out.println("  of which tips: " + money(score.getTipsCents()));
        }
        System.out.printf("Paid average satisfaction: %s / target %d%n",
                average(), mode.getTargetAverageSatisfaction());
    }

    private OptionalInt readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = input.readLine();
            if (line == null) {
                return OptionalInt.empty();
            }
            if (input.echoesInput()) {
                System.out.println(line);
            }
            try {
                return OptionalInt.of(Integer.parseInt(line.trim()));
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
        if (restaurant.getScore().getPaidCount() == 0) {
            return "N/A";
        }
        return String.format(Locale.ROOT, "%.1f", restaurant.getScore().getAverageSatisfaction());
    }

    private String money(int cents) {
        return String.format(Locale.ROOT, "$%d.%02d", cents / 100, cents % 100);
    }
}