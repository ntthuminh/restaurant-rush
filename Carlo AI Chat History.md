**Prompt:** Generate ONLY key ideas required to build a complex sequence diagram for a use case of VIP customer ordering during happy hour.

**Output:**

# Key ideas: VIP orders during Happy Hour (Stage 3)

## Scenario setup

- Starting conditions: Happy Hour is off, it hasn't been used yet this game, a table is free, and a VIP is in the queue.
- The manager choice that changes the outcome is turning on Happy Hour before the order is taken. That choice locks in the lower price.
- The run goes from arrival to the end-of-turn result check, which covers everything the brief requires.

## Lifelines

- CLI (boundary), game/turn controller (control)
- Host, Waiter, Chef, Cashier (staff)
- VIPCustomer, Table, Order, MenuItem, waiting queue
- Happy Hour pricing policy (an object of its own, not a flag inside the controller)

## Message flow

1. Turn start: the CLI asks the manager whether to turn on Happy Hour. Use an alt guarded by [not used yet]. Then show the remaining duration (2 turns).
2. Recovery loop: for each customer still waiting for a table or food, add 15 satisfaction, capped at 100. Guard it with [happyHourActive].
3. Arrival: the VIP joins the queue, and the Host or the Waiter seats them. Use an alt for which one does it, guarded by [table free].
4. Order: the Waiter takes the order and creates an Order linked to a MenuItem.
5. Pricing (the core of the diagram):
    - Order → VIPCustomer: applyDiscount(price) gives ×0.9. This is a polymorphic call with no type check in the controller.
    - Order → HappyHourPolicy: apply(price) gives ×0.8, only when [active].
    - Order: lockPrice(). The worked example is $15 × 0.9 × 0.8 = $10.80.
6. Kitchen: a loop of prepareUnit() that runs until the order reaches READY.
7. Serving: the Waiter serves on a later turn and the order becomes SERVED. Add a note that an order can't be served on the turn it becomes READY.
8. Eating: the customer finishes and becomes READY_TO_PAY.
9. Payment: the Cashier charges the locked price, guarded by [READY_TO_PAY]. The order becomes PAID, the table is freed, and the customer's satisfaction is recorded.
10. Result check: the controller updates revenue and the average, then an alt checks victory, defeat or continue.

## Control notation to include

- loop over the turns, the waiting customers and the preparation units.
- alt for the Happy Hour choice, invalid choices (re-prompt without using up the staff task), and the end-of-game check.
- Guards: [remainingTurns > 0], [satisfaction > 0], [status == READY].
- An opt where Happy Hour expires and its duration is decremented.

## Points worth calling out in notes

- The price locks when the order is taken, so orders taken before Happy Hour started don't change.
- The VIP discount is applied before Happy Hour, and you should show that order explicitly.
- VIP satisfaction drops 5 per waiting turn, compared with 8 for a regular customer. Show it inside the satisfaction loop.

## A tradeoff to decide on

Showing the whole flow in one turn-by-turn diagram makes it very large. You can either fold the repeated turns into a single loop frame, which is cleaner, or draw the turns out one by one, which makes the satisfaction changes easier to follow. Graders look at clarity, so I'd fold the turns into a loop and use notes to mark which turn each step happens in. Do
