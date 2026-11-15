# Restaurant Rush

Restaurant Rush is our SC2002 Java command-line restaurant management game.
Repository: [ntthuminh/restaurant-rush](https://github.com/ntthuminh/restaurant-rush)

## Versions

| Source folder | Version |
|---|---|
| `Stage-1/Codes` | Basic restaurant operation with Regular customers |
| `Stage-2/Codes` | VIP and Critic customer behaviour |
| `Stage-3/Codes` | Host, ComboMeal and Happy Hour |
| `Stage-3-hard/Codes` | Extended Stage 3 with Normal/Hard selection, drinks, tips, recording and replay |

Compile each version separately because they contain classes with the same names. `Stage-3-replay` contains input helpers; run the integrated recording/replay feature through `Stage-3-hard/Codes`.

## Build and run

Use a Java Development Kit (JDK) 17 or later, with `java` and `javac` available in your terminal. Run the following commands from the repository root. Build from the `.java` sources into the separate output folders below.

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
javac -d build/stage-3 Stage-3/Codes/*.java
java -cp build/stage-3 RestaurantRushApp
```

### Extended Stage 3

This version includes an opening mode-selection menu, drinks and a Bartender in Hard mode, and recording/replay.

```bash
mkdir -p build/stage-3-hard
javac -d build/stage-3-hard Stage-3-hard/Codes/*.java
java -cp build/stage-3-hard RestaurantRushApp
```

At the opening prompt, enter `1` for Normal mode or `2` for Hard mode. Normal mode uses the Stage 3 rules. Hard mode is an optional extension with drinks, a Bartender and tips.

## Controls

Enter one number at each prompt.

| Staff or option | Input |
|---|---|
| Mode selection (extended application only) | `1` Normal, `2` Hard |
| Happy Hour (Stage 3 and extended application) | `1` to activate, `0` to decline; the prompt stops after activation |
| Host (Stage 3 and extended application) | Customer number to seat, or `0` to wait |
| Waiter | `1` seat, `2` take order, `3` serve, `0` wait |
| Chef | Customer number whose order to prepare, or `0` to wait |
| Bartender (Hard mode only) | Customer number whose drink to advance by one preparation unit, or `0` to wait |
| Cashier | Automatic |

After choosing a Waiter action other than Wait, enter the customer number.

If a choice is invalid, the game explains why and lets you try again without using up the staff member’s action.

## Recording and replay

Use the application in `Stage-3-hard` to record and replay sessions.

Record a session:

```bash
java -cp build/stage-3-hard RestaurantRushApp --record game-run.txt
```

At the opening menu, enter `1` to record Normal mode or `2` to record Hard mode, then play as usual.

Replay that recording:

```bash
java -cp build/stage-3-hard RestaurantRushApp --replay game-run.txt
```

The file stores entered choices one per line, including the opening mode selection. Replay supplies those choices to the same prompts and displays the saved inputs. Use recordings made with this version; older recordings that use a `#mode` header or omit the opening selection are incompatible.

Supported arguments are `--record FILE` or `--replay FILE`, used separately. Difficulty is selected through the menu.

Standard `Stage-3` does not handle recording or replay arguments. The separate `Stage-3-replay` folder contains input helper classes only; use `Stage-3-hard/Codes` to run the integrated application.

## Stage tags and comparisons

Release and comparison links:

| Evidence | Link |
|---|---|
| Stage 1 release | [stage-1](https://github.com/ntthuminh/restaurant-rush/releases/tag/stage-1) |
| Stage 2 release | [stage-2](https://github.com/ntthuminh/restaurant-rush/releases/tag/stage-2) |
| Stage 3 release | [stage-3](https://github.com/ntthuminh/restaurant-rush/releases/tag/stage-3) |
| Stage 3 Hard release | [stage-3-hard](https://github.com/ntthuminh/restaurant-rush/releases/tag/stage-3-hard) |
| Stage 1 → Stage 2 comparison | [View changes](https://github.com/ntthuminh/restaurant-rush/compare/stage-1...stage-2) |
| Stage 2 → Stage 3 comparison | [View changes](https://github.com/ntthuminh/restaurant-rush/compare/stage-2...stage-3) |
| Stage 3 → Stage 3 Hard comparison | [View changes](https://github.com/ntthuminh/restaurant-rush/compare/stage-3...stage-3-hard) |

## Changes between stages

The file summaries below describe the submitted source folders. The comparison links above show the corresponding GitHub changes. Paths in each table are relative to that stage's `Codes` folder.

### Stage 1 to Stage 2

No Java files were added or deleted. VIPCustomer and CriticCustomer were introduced as nested classes in `Customer.java`.

| Changed file | Change and reason |
|---|---|
| `Customer.java` | Adds the VIP and Critic subclasses and overridable behaviour for waiting loss, pricing and food choice. This keeps customer differences in the customer objects. |
| `Order.java` | Accepts the calculated bill in its constructor so it can store the VIP price when the order is taken. |
| `Restaurant.java` | Creates C2 as a VIP and C3 as a Critic so the base run includes both customer types. |
| `RestaurantRushApp.java` | Displays customer types and the new rules so the player can see their effects. |

The existing staff and table code is retained. The report explains the design changes and the test results.

### Stage 2 to Stage 3

| File status | Files | Change and reason |
|---|---|---|
| Added | `VIPCustomer.java`, `CriticCustomer.java` | Moves the existing nested subclasses into separate files and passes them the shared Happy Hour policy. |
| Modified | `Customer.java` | Uses `Order.Meal`, selects eligible combos, applies Happy Hour after customer pricing and restores satisfaction only while waiting for service. |
| Modified | `Order.java` | Adds the nested `Meal` interface and `ComboMeal` class so individual items and combos share price and preparation operations. |
| Modified | `Staff.java` | Adds the nested Host class and shared seating validation so the Host and Waiter use the same seating checks. |
| Modified | `Restaurant.java` | Adds the nested HappyHour policy and Host phase. Uses roles to identify staff and reads orders through customers instead of keeping a separate order list. These changes coordinate promotion timing and the extra staff role. |
| Modified | `RestaurantRushApp.java` | Adds Host and Happy Hour prompts and updates the displayed rules and status information. |
| Formatting only | `Table.java` | Removes the final newline; table behaviour is unchanged. |

No Java files were deleted. Host, Meal, ComboMeal and HappyHour are new types inside existing files, so they do not appear as new files in the comparison. The report covers their responsibilities, design effects and checks of earlier behaviour.

## Report and presentation

| Submission item                                                                             | Location                                         |
| ------------------------------------------------------------------------------------------- | ------------------------------------------------ |
| PDF report, including diagrams, design discussion, test evidence and individual reflections | Refer to report in submission zip                |
| Recorded presentation                                                                       | Refer to video in submission zip                 |
| Signed Declaration of Original Work                                                         | Refer to appendix under report in submission zip |
| Each member's Declaration on Use of GAI Assistance                                          | Refer to AI declaration in submission zip        |

The five narration segments are listed in order of speaker changes. Timestamps are rounded to the nearest second.

| Narration segment | Presentation content                                                                                                  | Timestamp   |
| ----------------- | --------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1                 | Introduction, game overview and the three development stages (Tiffany)                                                | 00:00–01:43 |
| 2                 | Stage 3 gameplay demonstration, Normal-mode victory, and the Hard-mode victory / Normal-mode defeat comparison (Ziyi) | 01:43–05:39 |
| 3                 | Class diagrams for Stages 1, 2 and 3, with changes between stages (Kaiser)                                            | 05:39–09:18 |
| 4                 | Selected use case and sequence diagram: VIP customer ordering during Happy Hour (Carlo)                               | 09:18–13:06 |
| 5                 | Individual contributions, team learning and closing (Nguyen)                                                          | 13:06–14:54 |

### Gameplay evidence in the video

The gameplay demonstration runs from 01:43 to 05:41. It includes Normal-mode victory at 04:58–05:10 and the Hard-mode victory / Normal-mode defeat results at 05:39. The sequence-diagram explanation at 09:18–13:06 covers a VIP order during Happy Hour through serving and payment.

Before submission, check that the gameplay segment visibly includes launch from the submitted project, a constrained staff decision, READY → SERVED → payment, VIP or Critic behaviour and a Stage 3 feature. The stage comparison and diagram segment must show the tag comparisons as well as the diagrams.

## Individual AI histories

Each member must upload their full project-related prompts, AI replies and follow-up exchanges in order to their own branch in the group repository. A summary alone does not meet the requirement. Link to the history folder or file on each member's branch below.

If unaccessible, please refer to the AI declaration submission zip.

| Member                   | AI history branch or folder                                 |
| ------------------------ | ----------------------------------------------------------- |
| Low ZiYi | [Full AI conversation history](https://github.com/ntthuminh/restaurant-rush/blob/ziyi-ai-history/ziyi-history.md) |
| Nguyen Thi Thu Minh | [Full AI conversation history](https://github.com/ntthuminh/restaurant-rush/blob/nguyen-ai-history/Minh%20AI%20Chat%20History.md) |
| Toh Kai Zhe, Kaiser | [Full AI conversation history](https://github.com/ntthuminh/restaurant-rush/blob/kaiser-ai-history/Kaiser%20AI%20Chat%20History.md) |
| Carlo Ray Balcos De Luna | [Full AI conversation history](https://github.com/ntthuminh/restaurant-rush/blob/carlo-ai-history/Carlo%20AI%20Chat%20History.md) |
| Tiffany Ngo Cai Wen | [Full AI conversation history](https://github.com/ntthuminh/restaurant-rush/blob/tiffany-ai-history/Tiffany%20AI%20Chat%20History.md) |

