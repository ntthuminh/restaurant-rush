**You said:** 

**SC2002_Restaur…** Document 

give me a list of things to add or change when developing stage 2 on top of an existing stage 1 

#### **ChatGPT said:** 

Based on your **SC2002 Restaurant Rush assignment brief** , Stage 2 should extend your existing Stage 1 restaurant game by introducing **VIP and Critic customers** , while keeping the original gameplay and rules intact. 

I would approach Stage 2 in three areas: 

**1.** New features and mechanics. 

**2.** Changes to existing classes and their responsibilities. 

**3.** Code structure improvements to support extensibility and polymorphism. 

Here is a practical checklist of what to add or change. 

# **1. New features to implement in Stage 2** 

The assignment specifically requires two new customer types. 

### **VIP Customer** 



Required 

Satisfaction decreases by 5 per waiting turn instead of 8. Receives a 10% discount on the bill. Otherwise follows the regular customer flow. 

### **Critic Customer** 

Required Satisfaction decreases by 12 per waiting turn instead of 8. Pays full price. <mark>ES:</mark> 

Must have a distinct order preference or reaction to service, defined and documented by your group. 

## **Suggested Critic behaviour** 

The assignment gives you freedom to define the Critic's special behaviour. Here are some possible implementations: 

|**Option**|**Behaviour**|**Complexity**|
|---|---|---|
|A|Critic prefers Pasta and gains satisfaction when served Pasta.|Low|
|B|Critic loses additional satisfaction if they wait too long after being seated.|Medium|
|C|Critic gains satisfaction when served quickly after their order becomes READY.|Medium|
|D|Critic has higher expectations and receives a satisfaction bonus for a particular service<br>condition.|Medium|



### **My recommendation: Option A or C.** 

Option A is particularly straightforward to implement and test. Option C provides a more interesting servicemanagement mechanic. 

Make sure the behaviour is meaningful, deterministic and documented. 

**2. Existing classes to modify** 

These are the main Stage 1 classes I would inspect and potentially update. 

|**Existing class**|**Suggested changes**|
|---|---|
|`Customer`|Make it an abstract superclass or introduce overridable behaviour methods.|
|`RegularCustomer`|Extend<br>`Customer`and preserve existing Stage 1 behaviour.|
|`Waiter`|Check that customer-specific behaviour is handled through customer objects, not customer-|
||type checks.|
|`Cashier`|Apply the appropriate customer discount when processing payment.|
|`Order`|Ensure the final payable amount can reflect the customer's discount.|
|`GameController`|Update customer creation and registration to support VIP and Critic arrivals.|
|`Game`/<br>`Restaurant`|Update customer generation and game setup while preserving the original turn sequence.|
|`CLI`|Display customer types and relevant information to the player.|



Not every class necessarily needs modification. Only change classes where the new requirements actually affect their responsibilities. 

# **3. New classes to consider adding** 

I recommend the following class structure if your Stage 1 design does not already support it. 



<!-- Start of picture text -->
Suggested inheritance structure<br>Customer<br>Common attributes and<br>behaviour<br>RegularCustomer VIPCustomer CriticCustomer<br>Loss: 8 Loss: 5 Loss: 12<br>10% off Special behaviour<br><!-- End of picture text -->

Illustrative inheritance structure; adapt it to your existing Stage 1 design. 

Potentially useful new classes: 



<!-- Start of picture text -->
Class Responsibility Priority<br>VIPCustomer Implements VIP-specific satisfaction loss and discount. Required<br>CriticCustomer Implements Critic-specific satisfaction loss and special behaviour. Required<br>CustomerFactory Creates different customer types according to the arrival schedule. Optional<br>CustomerType Enum to identify customer types for display or configuration. Optional<br><!-- End of picture text -->

**Important:** You do not need a `CustomerFactory` or `CustomerType` just to satisfy the assignment. Avoid unnecessary abstractions. 

# **4. Important OOP improvements** 

This is probably the most important part of Stage 2 from an SC2002 perspective. 

The assignment explicitly requires different customer behaviour to be dispatched through customer objects rather than chains of type checks in the turn controller. 

Here are the improvements I recommend. 

|**OOP concept**|**What to implement**|
|---|---|
|Inheritance|VIP and Critic inherit common attributes and methods from Customer.|
|Polymorphism|Override methods such as<br>`getSatisfactionLoss()`or<br>`calculateBill()`.|
|Encapsulation|Keep satisfaction, discounts and customer-specific rules inside the relevant classes.|
|Composition|Customer continues to have an associated Order, while Restaurant manages its customers<br>and tables.|



Open-Closed Principle 

Allow new customer types to be introduced without rewriting the entire turn controller. 

For example: 

### **Instead of this approach:** 



<!-- Start of picture text -->
Java<br>if (customer instanceof VIPCustomer) {<br>satisfaction -= 5;<br>} else if (customer instanceof CriticCustomer) {<br>satisfaction -= 12;<br>} else {<br>satisfaction -= 8;<br>}<br><!-- End of picture text -->

### **Use polymorphism instead:** 



<!-- Start of picture text -->
Java<br>// Inside Customer<br>public abstract int getSatisfactionLoss();<br>Java<br>// Inside RegularCustomer<br>@Override<br>public int getSatisfactionLoss() {<br>return 8;<br>}<br>Java<br>// Inside VIPCustomer<br>@Override<br>public int getSatisfactionLoss() {<br>return 5;<br>}<br>Java<br>// Inside CriticCustomer<br>@Override<br>public int getSatisfactionLoss() {<br>return 12;<br>}<br>Your existing turn controller can then simply call:<br>Java<br>customer.reduceSatisfaction(<br>customer.getSatisfactionLoss()<br>);<br><!-- End of picture text -->

This means the controller does not need to know what type of customer it is handling. 

# **5. Changes to the game mechanics** 

Make sure you preserve all existing Stage 1 mechanics. 

|**Mechanic**|**Stage 2 changes**|
|---|---|
|Customer arrivals|Still six arrivals, with at least one VIP and one Critic in the reproducible base run.|
|Waiting queue|Supports all three customer types.|
|Seating|No changes to the original rules.|
|Ordering|All customer types follow the same order flow.|
|Food preparation|No changes to Chef mechanics.|
|Serving|No changes to Waiter mechanics.|
|Satisfaction|Different decreases depending on customer type.|
|Payment|VIP receives 10% off; Regular and Critic pay full price.|
|Revenue|Must reflect actual amounts paid.|
|Victory/Defeat|Same original criteria.|



One important detail: make sure the VIP discount is applied only once. Avoid accidentally discounting the bill both when the order is created and when payment is processed. 

# **6. Testing requirements** 

I would add the following test scenarios before considering Stage 2 complete. 

|**Test case**|**Expected result**|
|---|---|
|Regular customer waits one turn|Satisfaction decreases by 8.|
|VIP waits one turn|Satisfaction decreases by 5.|
|Critic waits one turn|Satisfaction decreases by 12.|
|VIP orders a $15 Burger|Pays $13.50.|
|Regular orders a $15 Burger|Pays $15.00.|
|Critic orders a $15 Burger|Pays $15.00.|
|Critic exhibits special behaviour|Behaviour triggers under the documented condition.|
|Customer reaches 0 satisfaction|Leaves without paying.|
|Mixed customer queue|All customer types can be seated, ordered and served.|
|Game reaches turn 18|Correct victory/defeat evaluation.|



Also verify that: 

Invalid manager choices do not consume staff actions. 

VIP discounts do not incorrectly affect revenue calculations. 

Customer satisfaction averages use the satisfaction recorded at payment. 

All customer types follow the same ordering, serving and payment sequence. 

# **7. GitHub and documentation changes** 

The assignment requires three independently runnable versions, not simply one final version with all features enabled. 

For Stage 2, make sure you: 

- Preserve the original `stage-1` Git tag. Create a new runnable Stage 2 version. Tag the corresponding commit as `stage-2` . Document all newly created classes. Document all existing classes modified or deleted. Explain why each modification was necessary. 

- Include a reproducible run containing VIP and Critic customers. Update your UML class diagram to show the new inheritance relationships. 

Ensure Stage 1 remains independently runnable. 

For your README, consider a change summary like this: 

|**File**|**Change**|**Reason**|
|---|---|---|
|`Customer.java`|Modified|Introduce extensible customer-specific behaviour.|
|`VIPCustomer.java`|Added|Implement VIP discount and satisfaction rules.|
|`CriticCustomer.java`|Added|Implement Critic-specific behaviour.|
|`GameController.java`|Modified|Support the new customer types.|
|`Cashier.java`|Modified|Account for VIP discounts during payment.|



These are illustrative changes; your actual README should reflect your code. 

# **Recommended implementation order** 

If I were developing Stage 2 on top of your existing Stage 1, I would follow this sequence: 

- 1 **Step 1 – Review existing Customer class** 

Identify which attributes and methods should be shared between customer types. 

- 2 **Step 2 – Refactor Customer for polymorphism** 

Introduce overridable or abstract methods for satisfaction loss and customer-specific behaviour. 

- 3 **Step 3 – Implement VIPCustomer** 

   - Add the 5-point satisfaction loss and 10% discount. 

- 4 **Step 4 – Implement CriticCustomer** 

   - Add the 12-point satisfaction loss and your chosen special behaviour. 

- 5 **Step 5 – Modify customer generation** 

Introduce VIP and Critic arrivals while retaining six total arrivals. 

- 6 **Step 6 – Integrate discounts and behaviour** 

   - Ensure the Cashier and other relevant components work with the new customer types. 

- 7 **Step 7 – Test and document** 

   - Test mixed customer runs, check revenue and satisfaction, and update UML and README. 

## **My overall recommendation** 

Prioritise these five things: 

**1. Refactor** `Customer` **properly** so that inheritance and polymorphism are demonstrated meaningfully. 

**2. Implement** `VIPCustomer` with its satisfaction and discount mechanics. 

**3. Implement** `CriticCustomer` with a simple but meaningful special behaviour. 

**4. Minimise changes to Stage 1 classes** wherever possible, especially the core turn sequence. 

**5. Keep Stage 1 and Stage 2 independently runnable** with their own Git tags. 

The biggest mistake to avoid is simply adding `if-else` statements throughout your existing code to accommodate VIPs and Critics. Stage 2 is an opportunity to demonstrate that your Stage 1 architecture can accommodate new requirements without extensive rewriting. 

If you share your existing Stage 1 Java source code, I can map out the exact files to add, modify and leave untouched, with a suggested implementation for each. 

- 💻<sup>Review Stage 1 code for Stage 2 changes</sup> Tap to set up 

**You said:** 

compile this into a table that is brief 

**ChatGPT said:** 

## **Stage 2 Development Checklist (Building on Stage 1)** 

|**Category**|**Add / Change**|**Key Requirements**|
|---|---|---|
|Customer classes|Add<br>`VIPCustomer`|5 satisfaction loss per waiting turn; 10% discount.|
||Add<br>`CriticCustomer`|12 satisfaction loss per waiting turn; unique order preference or<br>service reaction.|
|Customer class|Modify<br>`Customer`|Support inheritance and polymorphism through overridable<br>methods.|
|Regular customer|Preserve<br>`RegularCustomer`|Retain existing behaviour and 8 satisfaction loss.|
|Customer<br>generation|Modify controller / game<br>setup|Support VIP and Critic arrivals; still 6 customers total.|
|Payment|Modify<br>`Cashier`/<br>`Order`|Apply VIP discount correctly; other customers pay full price.|
|Game controller|Modify if necessary|Dispatch customer-specific behaviour through objects, not type-<br>checking chains.|
|CLI|Update display|Show customer types and relevant information.|
|Game mechanics|Preserve existing systems|Maintain seating, ordering, serving, payment and turn sequence.|
|Victory / Defeat|Preserve existing criteria|Same win conditions as Stage 1.|
|Testing|Add test scenarios|Test satisfaction, discounts, Critic behaviour and mixed customer<br>runs.|
|UML|Update class diagram|Show inheritance, polymorphism and relevant relationships.|
|GitHub|Create<br>`stage-2`tag|Keep Stage 1 independently runnable and document changes.|
|README|Document modifications|List new files, modified/deleted files and reasons.|



### **Recommended implementation order** 

|**Priority**|**Task**|
|---|---|
|1|Review and refactor<br>`Customer`class.|
|2|Implement<br>`VIPCustomer`and its discount.|
|3|Implement<br>`CriticCustomer`and its special behaviour.|
|4|Update customer generation and payment logic.|
|5|Integrate changes with existing game controller and CLI.|
|6|Test all customer types and preserve Stage 1 mechanics.|
|7|Update UML, README and GitHub Stage 2 tag.|



**Key consideration:** Minimise changes to stable Stage 1 code. Use inheritance and polymorphism to introduce new customer behaviours without rewriting the existing game logic. 

