
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 
 * BruteForceScheduler: this fins the maximum possible profit by examining every valid combination of jobs and their possible start times
 * 
 * Unlike the greed scheduler, this algorithm explores all the possible scheduling decision to guarantee an optimal result
 * 
 * so this is intending to mainly for small dataset because it execution time increases exponentially as the number of jobs grows
 */

public class BruteForceScheduler {

    // Stores the best schedule discovered during the recursive search
    // keeping the information in a separate object avoids static variables that could retain results between seperate executions

    private static class SearchState {
        private long bestProfit = 0;
        private List<GreedyScheduler.ScheduledJob> bestSchedule = 
                new ArrayList<>();   
    }

    

    public static GreedyScheduler.Result schedule(List<Job> jobs) {

        if (jobs == null) {
            throw new IllegalArgumentException(
                "Job list cannot be null."
            );
        }

        // Create a seperate list so the original input is not modified.
        List<Job> jobList = new ArrayList<>(jobs);

        // validate that the list does not contain null entries

            for (Job job : jobList) {
                if(job == null) {
                    throw new IllegalArgumentException("Job list cannot contain null entries.");
                }
            }
        
        // Stores the best solution found during the search
        SearchState state = new SearchState();

        // Explore every possible valid schedule
        search(jobList, 0, new ArrayList<>(), 0, state);


        // Sort the final schedule by start time for consistent output
        state.bestSchedule.sort(
            Comparator
                .comparingInt(
                    (GreedyScheduler.ScheduledJob scheduledJob)
                        -> scheduledJob.start
                )
                .thenComparing(scheduledJob -> scheduledJob.job.getId())
        );

        return new GreedyScheduler.Result(
            state.bestSchedule,
            state.bestProfit
        );
    }



    private static void search(
            List<Job> jobs,
            int index,
            List<GreedyScheduler.ScheduledJob> currentSchedule,
            long currentProfit,
            SearchState state
        ) {

            // Base case : when every job has been considered, compare the current schedule profit with the best profit found so far

        // All jobs have been considered
        if (index == jobs.size()) {

            if (currentProfit > state.bestProfit) {
                state.bestProfit = currentProfit;
                // save a copy so later backtracking doesn't alter it
                state.bestSchedule = new ArrayList<>(currentSchedule);
            }

            return;
        }

        Job currentJob = jobs.get(index);

        // Option 1: Skip this job
        search(jobs, index + 1, currentSchedule, currentProfit, state);

        // Option 2: Try every permitted start time
        for (
                long candidateStart = currentJob.getEarliestStart();
                candidateStart <= currentJob.getLatestStart();
                candidateStart++
        ) {
            int start = (int) candidateStart;

            // calculate the finishing time of the curent job
            long finish = candidateStart + currentJob.getDuration();

            // check whether the job conflicts with the current schedule
            boolean overlaps = hasOverlap(
                currentSchedule,
                start,
                finish
            );

            // only non-overlapptin jobs can be added to the schedule.
            if (!overlaps) {
                currentSchedule.add(
                    new GreedyScheduler.ScheduledJob(
                        currentJob, start
                    )
                );

                // Continue searching with the next job
                search(jobs, index + 1, currentSchedule, currentProfit + currentJob.getProfit(), state);


                // backtracking: remove the job after exploring this possibilty which allow the alogrithm to examine other possible start times and scheduling combinations

                currentSchedule.remove(
                    currentSchedule.size() - 1
                );
               
            
        }
    }
}


    private static boolean hasOverlap(
                List<GreedyScheduler.ScheduledJob> schedule,
                int start,
                long finish
    ){
        for (GreedyScheduler.ScheduledJob existingJob : schedule) {

            if(
                start < existingJob.finish
                && finish > existingJob.start
            ) {
                return true;
            }
    }

    return false;
    }
}
