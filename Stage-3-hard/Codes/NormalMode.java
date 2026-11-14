import java.util.Arrays;
import java.util.List;

public class NormalMode extends GameMode {
    @Override
    public String getTitle() {
        return "RESTAURANT RUSH - STAGE 3";
    }

    @Override
    public int getArrivalInterval() {
        return 3;
    }

    @Override
    public int getTargetPaidCustomers() {
        return 4;
    }

    @Override
    public int getTargetRevenueCents() {
        return 4500;
    }

    @Override
    public int getTargetAverageSatisfaction() {
        return 60;
    }

    @Override
    public List<String> getRuleLines() {
        return Arrays.asList(
                "C2 is a VIP. C3 is a Critic. The other arrivals are Regular.",
                "Regular/VIP IDs divisible by 4 choose a Salad + Pasta combo.",
                "Other Regular/VIP choices repeat by ID: Salad, Burger, Pasta.",
                "Salad + Pasta combo: $24.30, 3 preparation units.",
                "Critics always choose Pasta.");
    }

    @Override
    public String getName() {
        return "Normal";
    }
}
