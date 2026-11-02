
import java.util.ArrayList;
import java.util.List;

public class BruteForceScheduler {

    // Best solution found so far
    private static long bestProfit;
    private static List<GreedyScheduler.Scheduledjob> bestSchedule;

    public static GreedyScheduler.Result schedule(List<job> jobs) {

        bestProfit = 0;
        bestSchedule = new ArrayList<>();

        // Explore every possible valid schedule
        search(jobs, 0, new ArrayList<>(), 0);

        bestSchedule.sort(
                (a, b) -> Integer.compare(a.start, b.start));

        return new GreedyScheduler.Result(
                bestSchedule, bestProfit);
    }

    private static void search(
            List<job> jobs,
            int index,
            List<GreedyScheduler.Scheduledjob> current,
            long currentProfit) {

        // All jobs have been considered
        if (index == jobs.size()) {

            if (currentProfit > bestProfit) {
                bestProfit = currentProfit;
                bestSchedule = new ArrayList<>(current);
            }

            return;
        }

        job job = jobs.get(index);

        // Option 1: Skip this job
        search(jobs, index + 1, current, currentProfit);

        // Option 2: Try every permitted start time
        for (int start = job.getEarliestStart(); start <= job.getLatestStart(); start++) {

            long finish = (long) start + job.getDuration();

            boolean overlaps = false;

            for (GreedyScheduler.Scheduledjob s : current) {

                if (start < s.finish && finish > s.start) {
                    overlaps = true;
                    break;
                }
            }

            if (!overlaps) {

                // Add this job to the current schedule
                current.add(
                        new GreedyScheduler.Scheduledjob(job, start));

                search(
                        jobs,
                        index + 1,
                        current,
                        currentProfit + job.getProfit());

                // Backtrack: remove the job and try others
                current.remove(current.size() - 1);
            }
        }
    }
}
