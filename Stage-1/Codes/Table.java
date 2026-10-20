public class Table {
    private final int id;
    private Customer customer;

    public Table(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public boolean isFree() {
        return customer == null;
    }

    public void seat(Customer customer) {
        Restaurant.require(isFree(), "That table is occupied.");
        customer.seat();
        this.customer = customer;
    }

    public void release() {
        if (customer == null) {
            return;
        }
        Customer.Status status = customer.getStatus();
        boolean canLeave = status == Customer.Status.PAID || status == Customer.Status.LEFT;
        Restaurant.require(canLeave,
                "A table remains occupied until payment or abandonment.");
        customer = null;
    }
}
