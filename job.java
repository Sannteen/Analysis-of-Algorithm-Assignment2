/**
 * Job.java
 * 
 * This represents a computational job which would be submitterd to the resource allocation scheduler
 * 
 *  Each job would contain an identifier, profit, execution duration, and a permitted starting-time window.
 * 
 * the class also provides a method which calculate profit density, which is used by the greedy scheduling algorithm
 * 
 */


public class Job {

    // Properties of a computational job
    private String id;
    private int profit;
    private int duration;
    private int earliestStart;
    private int latestStart;

    // Constructor
    public Job(String id, int profit, int duration,
            int earliestStart, int latestStart) {
                
                // Validate the job properties before creating the object
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
    // Profit density is used as the priority metric in the greedy scheduling algorithm. Formula is Profit Density = Profit / Duration
    public double getProfitDensity() {
        return (double) profit / duration;
    }

    @Override
    public String toString() {
        return "Job " + id
                + " | Profit: $" + profit
                + " | Duration: " + duration + " hours"
                + " | Start Window: "
                + earliestStart + "-" + latestStart
                + " | Profit Density: "
                + String.format("%.2f", getProfitDensity());
    }
}
