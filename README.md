# Restaurant Rush

Restaurant Rush is a Java command-line game developed for SC2002 Object-Oriented Design and Programming.

You play as a restaurant manager with 18 turns to meet customer, revenue and satisfaction targets. Each staff member has limited actions, so you need to decide who to seat, which orders to prepare and when to serve customers.

## Project structure

```text
restaurant-rush/
├── README.md
├── Stage-1/
│   ├── Codes/
│   └── testrun_stage1.xlsx
├── Stage-2/
│   ├── Codes/
│   └── testrun_stage2.xlsx
├── Stage-3/
│   ├── Codes/
│   └── testrun_stage3.xlsx
├── Stage-3-hard/
└── Stage-3-replay/
```

| Folder | Contents |
|---|---|
| `Stage-1/Codes` | Basic game with Regular customers |
| `Stage-2/Codes` | Customer behaviour extension with VIP and Critic customers |
| `Stage-3` | Host, ComboMeal and Happy Hour |
| `Stage-3-hard` | Normal and Hard modes, with recording and replay |
| `Stage-3-replay` | Input helper classes; this folder is not independently runnable |

The versions contain classes with the same names. Compile them separately to avoid conflicts.

## Build and run

Run the following commands from the repository root.

### Stage 1

```bash
mkdir -p build/stage-1
javac -d build/stage-1 Stage-1/Codes/*.java
java -cp build/stage-1 RestaurantRushApp
```

### Stage 2

```bash
mkdir -p build/stage-2
javac -d build/stage-2 Stage-2/Codes/*.java
java -cp build/stage-2 RestaurantRushApp
```

### Stage 3

```bash
mkdir -p build/stage-3
javac -d build/stage-3 Stage-3/*.java
java -cp build/stage-3 RestaurantRushApp
```

### Extended Stage 3

This version includes difficulty selection, recording and replay.

```bash
mkdir -p build/stage-3-hard
javac -d build/stage-3-hard Stage-3-hard/*.java
```

Run Normal mode:

```bash
java -cp build/stage-3-hard RestaurantRushApp
```

Run Hard mode:

```bash
java -cp build/stage-3-hard RestaurantRushApp --hard
```

## Game objectives

To win in Normal mode, you must meet all three targets at the end of turn 18:

| Condition | Target |
|---|---:|
| Paid customers | At least 4 |
| Revenue | At least $45.00 |
| Average satisfaction of paid customers | At least 60 |

Missing any target results in Defeat. The game continues for all 18 turns, even if you reach the targets earlier.

Customers count towards the paid customer total and revenue only after paying. Average satisfaction uses their satisfaction at payment and displays `N/A` until the first customer pays.

## Base game settings

The restaurant has two tables, one Waiter, one Chef and one Cashier. Stage 3 adds a Host.

Customers arrive on turns 1, 4, 7, 10, 13 and 16, each starting with 100 satisfaction. They choose their own meals according to fixed rules, so the same decisions produce repeatable runs.

| Menu item | Price | Preparation units |
|---|---:|---:|
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |

## Controls

Enter one number at each prompt.

| Staff or option | Input |
|---|---|
| Happy Hour | `1` to activate, `0` to decline |
| Host | Customer number to seat, or `0` to wait |
| Waiter | `1` seat, `2` take order, `3` serve, `0` wait |
| Chef | Customer number whose order to prepare, or `0` to wait |
| Cashier | Automatic |

After choosing a Waiter action other than Wait, enter the customer number.

If a choice is invalid, the game explains why and lets you try again without using up the staff member’s action.

## Turn sequence

1. Scheduled customers arrive.
2. In Stage 3, the Host acts.
3. The Waiter performs one task or waits.
4. The Chef advances one order by one preparation unit or waits.
5. Customers served on an earlier turn finish eating.
6. The Cashier collects at most one eligible payment, oldest first.
7. Waiting and seated customers who have not been served lose satisfaction. Customers reaching zero leave.
8. The game displays the end-of-turn summary.

Food finished by the Chef can be served on the next turn at the earliest. Customers then need a later turn to finish eating and another turn before they can pay.

A table stays occupied until its customer pays or leaves. If a customer abandons the restaurant, their order is cancelled and earns no revenue.

## Development stages

### Stage 1: Basic operation

Stage 1 contains the basic restaurant game: Regular customers, tables, orders, a waiting queue and the original three staff roles.

Regular customers lose eight satisfaction points each turn while waiting for a table or food. Their meal choices repeat by customer ID: Salad, Burger, Pasta.

### Stage 2: Customer behaviour

Stage 2 adds VIP and Critic customers.

| Customer | Waiting loss per turn | Behaviour |
|---|---:|---|
| Regular | 8 | Full menu price |
| VIP | 5 | 10% discount |
| Critic | 12 | Always orders Pasta |

C2 is a VIP, C3 is a Critic and the other arrivals are Regular customers.

The customer subclasses override methods for their specific behaviour. They still follow the same ordering, serving and payment process.

### Stage 3: Host, ComboMeal and Happy Hour

The Host can seat one customer per turn before the Waiter acts. The Waiter can still seat customers, but the Host allows the Waiter to spend that turn taking an order or serving someone instead.

A ComboMeal contains two existing menu items. It takes the combined preparation units and costs 90% of their combined price, rounded to the nearest cent.

The game uses a Salad + Pasta combo:

```text
Preparation: 1 + 2 = 3 units
Price: ($9.00 + $18.00) × 90% = $24.30
```

Regular and VIP customers whose IDs are divisible by four choose this combo. Critics continue to choose Pasta.

Happy Hour can be activated once, at the start of a turn. It lasts for that turn and the next. While active:

- New orders receive a 20% discount.
- Customers waiting for a table or food recover 15 satisfaction at turn start, up to a maximum of 100.
- VIP discounts apply before the Happy Hour discount.

Each bill is fixed when the order is placed. Starting or ending Happy Hour does not change an existing bill.

## Design changes between stages

### Stage 1 to Stage 2

- `Customer.java`: Added VIP and Critic subclasses and methods that subclasses can override.
- `Order.java`: Allowed the bill to reflect customer discounts.
- `Restaurant.java`: Added the fixed VIP and Critic arrival rules.
- `RestaurantRushApp.java`: Updated the instructions and displayed customer types.

The seating, preparation, serving and payment sequence stayed the same.

### Stage 2 to Stage 3

- `Staff.java`: Added the Host and shared seating checks.
- `Order.java`: Added the `Meal` interface and `ComboMeal`.
- `Customer.java`: Added combo selection and Happy Hour pricing.
- `VIPCustomer.java` and `CriticCustomer.java`: Moved the customer subclasses into their own files.
- `Restaurant.java`: Added the Host phase, Happy Hour management and staff lookup by role.
- `RestaurantRushApp.java`: Added Host choices, Happy Hour activation and updated action displays.

Both individual dishes and combos implement `Meal`, so orders can prepare them through the same interface.

## Hard mode

Hard mode is an optional extension in `Stage-3-hard`.

| Setting | Normal | Hard |
|---|---:|---:|
| Turns | 18 | 18 |
| Arrival interval | Every 3 turns | Every 2 turns |
| Paid customer target | 4 | 5 |
| Revenue target | $45.00 | $110.00 |
| Average satisfaction target | 60 | 75 |
| Tips | None | $0.05 per satisfaction point at payment |

Hard mode has its own fixed meal sequence. Tips count towards revenue and are also shown separately in the final result.

## Recording and replay

Use the application in `Stage-3-hard` to record and replay sessions.

Record a Normal-mode session:

```bash
java -cp build/stage-3-hard RestaurantRushApp --record normal-run.txt
```

Record a Hard-mode session:

```bash
java -cp build/stage-3-hard RestaurantRushApp --hard --record hard-run.txt
```

Replay a recording:

```bash
java -cp build/stage-3-hard RestaurantRushApp --replay normal-run.txt
```

The recording saves the game mode and entered choices. Replay uses the saved mode automatically.

Standard `Stage-3` does not handle recording or replay arguments. The separate `Stage-3-replay` folder contains input helper classes only.

## Verification

These winning results were reproduced during local checks with Java 17:

| Version | Paid customers | Revenue | Average satisfaction |
|---|---:|---:|---:|
| Stage 1 | 5 | $66.00 | 79.2 |
| Stage 2 | 5 | $64.50 | 78.0 |
| Stage 3 | 5 | $74.94 | 86.2 |
| Hard mode | 6 | $111.30 | 75.0 |

Losing runs were also checked. Choosing Wait for every staff member throughout Normal mode results in zero paid customers, zero revenue and Defeat.

Other checks covered invalid-input retries, staff action order, payment delays, abandonment, discounts, Happy Hour duration, fixed bills and consistent recording/replay results.

The Stage 1, Stage 2 and Stage 3 folders include their test workbooks.

## Stage tags and comparisons

The tagged releases contain the saved versions of each stage. The comparison pages show the code changes between stages.

| Evidence | Link |
|---|---|
| Stage 1 release | [stage-1](https://github.com/ntthuminh/restaurant-rush/releases/tag/stage-1) |
| Stage 2 release | [stage-2](https://github.com/ntthuminh/restaurant-rush/releases/tag/stage-2) |
| Stage 3 release | [stage-3](https://github.com/ntthuminh/restaurant-rush/releases/tag/stage-3) |
| Stage 1 → Stage 2 comparison | [View changes](https://github.com/ntthuminh/restaurant-rush/compare/stage-1...stage-2) |
| Stage 2 → Stage 3 comparison | [View changes](https://github.com/ntthuminh/restaurant-rush/compare/stage-2...stage-3) |

