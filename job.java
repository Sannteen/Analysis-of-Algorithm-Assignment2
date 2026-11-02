
public class job {

    // Properties of a computational job
    private String id;
    private int profit;
    private int duration;
    private int earliestStart;
    private int latestStart;

    // Constructor
    public job(String id, int profit, int duration,
            int earliestStart, int latestStart) {

        if (id == null || id.isBlank()
                || profit < 0 || duration <= 0
                || earliestStart < 0
                || latestStart < earliestStart) {
            throw new IllegalArgumentException(
                    "Invalid job properties");
        }

        this.id = id;
        this.profit = profit;
        this.duration = duration;
        this.earliestStart = earliestStart;
        this.latestStart = latestStart;
    }

    // Getter methods
    public String getId() {
        return id;
    }

    public int getProfit() {
        return profit;
    }

    public int getDuration() {
        return duration;
    }

    public int getEarliestStart() {
        return earliestStart;
    }

    public int getLatestStart() {
        return latestStart;
    }

    // Calculate profit earned per hour
    public double getProfitDensity() {
        return (double) profit / duration;
    }

    @Override
    public String toString() {
        return "job " + id
                + " | Profit: $" + profit
                + " | Duration: " + duration + " hours"
                + " | Start Window: "
                + earliestStart + "-" + latestStart
                + " | Density: "
                + String.format("%.2f", getProfitDensity());
    }
}
