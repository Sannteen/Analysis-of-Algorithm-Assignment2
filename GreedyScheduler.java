
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GreedyScheduler {

    // Represents a job that has been scheduled
    public static class Scheduledjob {
        job job;
        int start;
        int finish;

        public Scheduledjob(job job, int start) {
            this.job = job;
            this.start = start;
            this.finish = start + job.getDuration();
        }
    }

    // Stores the results of scheduling
    public static class Result {
        List<Scheduledjob> scheduledjobs;
        long totalProfit;

        public Result(List<Scheduledjob> scheduledjobs,
                long totalProfit) {
            this.scheduledjobs = scheduledjobs;
            this.totalProfit = totalProfit;
        }
    }

    // Main greedy scheduling algorithm
    public static Result schedule(List<job> jobs) {

        // Copy jobs to avoid changing the original list
        List<job> sortedjobs = new ArrayList<>(jobs);

        // Sort by highest profit density first
        sortedjobs.sort(
                Comparator.comparingDouble(
                        job::getProfitDensity).reversed()
                        .thenComparing(
                                Comparator.comparingInt(
                                        job::getProfit).reversed())
                        .thenComparing(job::getId));

        List<Scheduledjob> scheduled = new ArrayList<>();
        long totalProfit = 0;

        // Consider jobs in greedy priority order
        for (job job : sortedjobs) {

            // Search for the earliest feasible start
            for (int start = job.getEarliestStart(); start <= job.getLatestStart(); start++) {

                long finish = (long) start + job.getDuration();

                boolean overlaps = false;

                // Check against previously scheduled jobs
                for (Scheduledjob existing : scheduled) {

                    if (start < existing.finish
                            && finish > existing.start) {
                        overlaps = true;
                        break;
                    }
                }

                // Accept the job if there is no overlap
                if (!overlaps) {
                    scheduled.add(new Scheduledjob(job, start));
                    totalProfit += job.getProfit();
                    break;
                }
            }
        }

        // Arrange accepted jobs by execution time
        scheduled.sort(
                Comparator.comparingInt(s -> s.start));

        return new Result(scheduled, totalProfit);
    }
}
