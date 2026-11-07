import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // Create a list of jobs to schedule
        
        List<job> jobs = Arrays.asList(
            new job("A", 150, 3, 0, 0),
            new job("B", 80, 1, 0, 0),
            new job("C", 90, 1, 1, 1),
            new job("D", 250, 3, 0, 0),
            new job("E", 60, 1, 2, 2)
        );

        // GreedyScheduler.Result result = GreedyScheduler.schedule(jobs);
        
        // System.out.println("\n\nAvaialable jobs:\n");
        // for (job job : jobs) {
        //     System.out.println(job);
        // }
        

        // System.out.println("\n\nRESOURCE ALLOCATION SCHEDULER");
        // System.out.println("=============================");

        // System.out.println("Accepted jobs:");

        // for (GreedyScheduler.Scheduledjob s : result.scheduledjobs) {

        //     System.out.println(
        //             "job " + s.job.getId()
        //                     + " | Start: " + s.start
        //                     + " | Finish: " + s.finish
        //                     + " | Profit: $" + s.job.getProfit());
        // }

        // System.out.println("-----------------------------");
        // System.out.println(
        //         "Total Profit: $" + result.totalProfit);



    // Brute-force scheduling******************************************
    // Run greedy algorithm
        GreedyScheduler.Result greedy =
            GreedyScheduler.schedule(jobs);

        // Run brute-force algorithm
        GreedyScheduler.Result brute =
            BruteForceScheduler.schedule(jobs);

        System.out.println(
            "RESOURCE ALLOCATION SCHEDULER"
        );
        System.out.println("=============================");

        System.out.println("\nGREEDY RESULTS:");
        printResult(greedy);

        System.out.println("\nBRUTE-FORCE RESULTS:");
        printResult(brute);

        System.out.println("\nCOMPARISON:");
        System.out.println(
            "Greedy Profit: $" + greedy.totalProfit
        );
        System.out.println(
            "Optimal Profit: $" + brute.totalProfit
        );
    }

    public static void printResult(
            GreedyScheduler.Result result) {

        for (GreedyScheduler.Scheduledjob s :
                result.scheduledjobs) {

            System.out.println(
                "job " + s.job.getId()
                + " | Start: " + s.start
                + " | Finish: " + s.finish
                + " | Profit: $" + s.job.getProfit()
            );
        }

        System.out.println(
            "Total Profit: $" + result.totalProfit
        );
     


    
    }





    
}
