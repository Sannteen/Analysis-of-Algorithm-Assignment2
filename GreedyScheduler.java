
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * GreedyScheduler.java
 *  Implements a greedy approach to the Resouce allocation scheduler.
 * 
 * The algorithm would prioritizes jobs according to their profit density
 * 
 * each job is considered in priority order and scheduled at the earlist available starting time within its permitted time window
 * 
 * NOTE: this greedy approch doesn't guarantee a globally optimal schedule. 
 * 
 * its performance is evlauated against the brute-force algorithm
 * 
*/

public class GreedyScheduler {

    // Represents a job that has been scheduled
    public static class ScheduledJob {
        Job job;
        int start;
        long finish;

        public ScheduledJob(Job job, int start) {
            this.job = job;
            this.start = start;
            this.finish = (long) start + job.getDuration();
        }
    }

    // Stores the results of scheduling
    public static class Result {
        List<ScheduledJob> scheduledJobs;
        long totalProfit;

        public Result(List<ScheduledJob> scheduledJobs,
                long totalProfit) {
            this.scheduledJobs = scheduledJobs;
            this.totalProfit = totalProfit;
        }
    }

    // Main greedy scheduling algorithm
    public static Result schedule(List<Job> jobs) {

        if (jobs == null) {
            throw new IllegalArgumentException("Job list cannot be null.");
        }

        // // Sort by highest profit density first
        // sortedJobs.sort(
        //         Comparator.comparingDouble(
        //                 job::getProfitDensity).reversed()
        //                 .thenComparing(
        //                         Comparator.comparingInt(
        //                                 job::getProfit).reversed())
        //                 .thenComparing(job::getId));

        // Copy jobs to avoid changing the original list
        List<Job> sortedJobs = new ArrayList<>(jobs);

        if (sortedJobs.contains(null)) {
            throw new IllegalArgumentException(
                "Job list cannot contain null entries."
            );
        }

        // sort the jobs according to the greedy priority rule
        // so profit density is compared using cross multiplication instead of floating poin division which avoid precision error in case when comparing two jobs

        sortedJobs.sort((job1, job2) -> {
            long density1 = 
            (long) job1.getProfit() * job2.getDuration();

            long density2 = 
                (long) job2.getProfit() * job1.getDuration();

            // higher profit density gets higher priority
            int comparison = Long.compare(density2, density1);

            if (comparison !=0 ) {
                return comparison;
            }

            // if densities are equal then prioritize higher profit.
            comparison = Integer.compare(job2.getProfit(), job1.getProfit());

            if (comparison != 0) {
                return comparison;
            }

            // if both values are equal use the job id for consistency
            return job1.getId().compareTo(job2.getId());
        });


        //  initialize the schedule and total profit 
        List<ScheduledJob> scheduled = new ArrayList<>();
        long totalProfit = 0;



        // Examine jobs in greedy priority order
        for (Job job : sortedJobs) {

            // Search for the earliest feasible starting time.
            //  a long counter prevents integer overflow if the latest permitted start time is integer.MAX_VALUE
            for (long candidateStart = job.getEarliestStart();
                 candidateStart <= job.getLatestStart();
                 candidateStart++) {

                    int start = (int) candidateStart;

                    long finish = start + (long) job.getDuration();

                    // check whether the proposed time interval overlaps any previously scheduled job
                    boolean overlaps = false;

                    for (ScheduledJob existing : scheduled) {
                        if (start < existing.finish
                             && finish > existing.start) {
                                overlaps = true;
                                break;
                             }
                    }
                 

                //  accept the job if a valid time is found.
                if (!overlaps) {
                    scheduled.add(new ScheduledJob(job, start));
                    totalProfit += job.getProfit();
                    // Only the earliest feasible starting time is used
                    break;
                }
            }
        }


        // Arrange accepted job chronologically
        scheduled.sort(
            Comparator.comparingInt((ScheduledJob s) -> s.start)
                    .thenComparing(s -> s.job.getId())
        );

        // return the completed scheduling result
        return new Result(scheduled, totalProfit);
    }
}
