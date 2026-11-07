Resource Allocation Scheduler

Analysis of Algorithms – Assignment #2

A Java-based Resource Allocation Scheduler that implements and compares
two scheduling algorithms:

-   Greedy Scheduling Algorithm
-   Brute Force Scheduling Algorithm

The project demonstrates how different algorithmic approaches can be
used to allocate computational jobs while maximizing total profit and
preventing scheduling conflicts.

------------------------------------------------------------------------

1. Project Overview

The Resource Allocation Scheduler accepts a collection of computational
jobs. Each job has a unique identifier, profit, execution duration, and
permitted starting-time window.

The scheduler attempts to select and schedule jobs to maximize the total
profit while ensuring that no two scheduled jobs overlap.

Two algorithms are implemented:

1.  Greedy Algorithm: Prioritizes jobs using profit density (profit
    divided by duration) and schedules each accepted job at the earliest
    available permitted starting time.

2.  Brute Force Algorithm: Explores possible combinations of jobs and
    their valid starting times to determine the maximum achievable
    profit.

The project also includes a performance-testing component that evaluates
the algorithms using different input sizes and generates an Excel
workbook containing the results, datasets, analysis, and charts.

2. Project Objectives

The main objectives are to:

-   Implement a greedy scheduling algorithm.
-   Implement a brute-force scheduling algorithm for comparison.
-   Demonstrate the greedy choice property and optimal substructure.
-   Evaluate whether the greedy algorithm always produces an optimal
    solution.
-   Analyze the time and space complexity of both algorithms.
-   Measure algorithm execution times using different input sizes.
-   Compare the total profit produced by each algorithm.
-   Present performance results using an Excel workbook with charts.

3. Technologies Used

Technology: Java 17+ Purpose: Main programming language

Technology: Apache Maven Purpose: Dependency management and project
compilation

Technology: Apache POI 6.0.0 Purpose: Excel workbook and chart
generation

Technology: Java Collections Framework Purpose: Managing jobs and
schedules

Technology: Java Random Purpose: Generating reproducible test datasets

Technology: Excel (.xlsx) Purpose: Storing performance results and
visualizations

4. Project Structure

Analysis-of-Algorithm-Assignment2/
Job.java
GreedyScheduler.java
BruteForceScheduler.java
Main.java
PerformanceTest.java
pom.xml
README.txt
Results/  Scheduler_Performance_Results.xlsx

File Descriptions

Job.java Defines the computational job and its properties.

GreedyScheduler.java Implements the greedy scheduling algorithm.

BruteForceScheduler.java Implements the brute-force scheduling
algorithm.

Main.java Demonstrates the algorithms using sample jobs.

PerformanceTest.java Generates datasets, measures execution times, and
creates the Excel workbook.

pom.xml Defines project configuration and Maven dependencies.

README.txt Provides project documentation and execution instructions.

5. Job Representation

Each computational job contains the following attributes:

Attribute: ID Description: Unique identifier for the job.

Attribute: Profit Description: Financial value earned when the job is
completed.

Attribute: Duration Description: Amount of time required to execute the
job.

Attribute: Earliest Start Description: Earliest permitted starting time.

Attribute: Latest Start Description: Latest permitted starting time.

The Job.java class also provides a profit-density calculation.

Profit Density Formula:

Profit Density = Profit / Duration

This value is used by the greedy algorithm to establish job priority.

6. Scheduling Algorithms

6.1 Greedy Scheduling Algorithm

The greedy scheduler prioritizes jobs according to their profit density.

The algorithm:

1.  Receives a list of available jobs.
2.  Sorts jobs by descending profit density.
3.  Uses higher profit as a tie-breaker.
4.  Uses job ID as a final tie-breaker.
5.  Attempts to schedule each job at the earliest available starting
    time within its permitted window.
6.  Rejects jobs that overlap with previously scheduled jobs.
7.  Calculates the total profit of the accepted jobs.

Time Complexity:

O(n log n + n^2 W)

Where: - n represents the number of jobs. - W represents the maximum
number of permitted starting positions per job.

Auxiliary Space Complexity:

O(n)

6.2 Brute Force Scheduling Algorithm

The brute-force scheduler examines possible job selections and valid
starting times.

For each job, the algorithm considers:

-   Skipping the job.
-   Including the job at each valid non-overlapping starting time.

It explores the possible combinations recursively and retains the
schedule with the highest total profit.

Worst-Case Time Complexity:

O(n(W + 1)^n)

Auxiliary Space Complexity:

O(n)

The brute-force algorithm is computationally expensive as the number of
jobs increases. Therefore, it is restricted to smaller datasets during
performance testing.

7. Demonstration

The Main.java file provides a sample demonstration using five
computational jobs.

Job A: Profit 150; Duration 3; Earliest Start 0; Latest Start 0. Job B:
Profit 80; Duration 1; Earliest Start 0; Latest Start 0. Job C: Profit
90; Duration 1; Earliest Start 1; Latest Start 1. Job D: Profit 250;
Duration 3; Earliest Start 0; Latest Start 0. Job E: Profit 60; Duration
1; Earliest Start 2; Latest Start 2.

Expected results:

Greedy: Selected jobs: B, C, E Total profit: 230

Brute Force: Selected jobs: D Total profit: 250

This example demonstrates that the greedy algorithm does not always
produce an optimal solution.

Although the greedy approach uses profit density to prioritize jobs, its
local decisions can prevent it from selecting a combination that
produces a higher overall profit.

8. Performance Testing

The PerformanceTest.java file evaluates the performance of both
scheduling algorithms using generated datasets.

8.1 Input Sizes

The following input sizes are tested:

Test 1: 5 jobs Test 2: 10 jobs Test 3: 50 jobs Test 4: 100 jobs Test 5:
1,000 jobs

8.2 Benchmark Configuration

Greedy: - Warm-up executions: 5 - Measured executions: 20 - Maximum
input size: 1,000

Brute Force: - Warm-up executions: 1 - Measured executions: 5 - Maximum
input size: 10

Brute-force testing is restricted to input sizes of 5 and 10 because of
its exponential worst-case complexity.

Execution times are measured using Java’s System.nanoTime() method and
converted into milliseconds.

Average execution time is calculated as:

Average Time = Total Execution Time / Number of Executions

8.3 Dataset Generation

Test datasets are generated programmatically using Java’s Random class.

Each generated job receives:

-   A unique job identifier.
-   A randomly generated profit between 50 and 500.
-   A duration between 1 and 5.
-   An earliest starting time between 0 and 99.
-   A latest starting time equal to the earliest starting time or one
    unit later.

A deterministic random seed is used for each input size to make the
generated datasets reproducible.

9. Excel Performance Report

After running PerformanceTest.java, the application generates a single
Excel workbook:

Results/Scheduler_Performance_Results.xlsx

The workbook contains five worksheets.

9.1 Summary Dashboard

Provides an overview of the performance tests, benchmark configuration,
key findings, and observations.

9.2 Test Results

Contains the aggregated performance results, including:

-   Input size.
-   Greedy average execution time.
-   Greedy total profit.
-   Brute-force average execution time.
-   Brute-force total profit.
-   Profit difference.
-   Whether greedy matched brute force.
-   Whether brute force was tested.

9.3 Runtime Analysis

Contains charts comparing the execution times of the scheduling
algorithms.

The charts include:

-   Greedy execution time across all input sizes.
-   Greedy versus brute-force execution time for smaller datasets.

9.4 Profit Analysis

Contains charts comparing the total profits produced by the scheduling
algorithms.

The charts include:

-   Greedy total profit across all input sizes.
-   Greedy versus brute-force total profit for smaller datasets.

9.5 Generated Datasets

Contains the generated jobs used during testing, including their
identifiers, profits, durations, and permitted starting-time windows.

The workbook uses native Excel charts, allowing the charts and
underlying data to be edited directly in Microsoft Excel.

10. Interpreting the Results

The performance report is intended to demonstrate the relationship
between input size, execution time, and total profit.

The following considerations should be made when interpreting the
results:

-   The greedy algorithm is expected to scale more efficiently than
    brute force.
-   Brute-force execution time increases rapidly as the number of jobs
    grows.
-   Similar profits between the two algorithms on particular datasets do
    not establish that the greedy algorithm is always optimal.
-   The sample demonstration provides a counterexample in which brute
    force produces a higher total profit.
-   Execution times may vary between computers and repeated benchmark
    runs.

11. Requirements

Before running the project, ensure the following are installed:

1.  Java Development Kit (JDK) 17 or later.
2.  Apache Maven.
3.  Microsoft Excel or another application capable of opening .xlsx
    files.

Apache POI is managed automatically through the Maven configuration in
pom.xml.

12. Compilation and Execution

Open a terminal in the project root directory.

Step 1: Verify Java and Maven

java -version mvn -version

Step 2: Compile the project

mvn clean compile

Maven will download the required dependencies and compile the Java
source files.

Step 3: Run the scheduling demonstration

mvn exec:java -Dexec.mainClass=Main

This executes the sample demonstration in Main.java.

Step 4: Run performance testing

mvn exec:java -Dexec.mainClass=PerformanceTest

This runs the performance tests and generates the Excel workbook.

Step 5: Access the results

Results/Scheduler_Performance_Results.xlsx

Open the workbook in Microsoft Excel to examine the summary, performance
results, charts, and generated datasets.

13. Important Notes

-   The greedy and brute-force algorithms use non-preemptive scheduling.
-   Scheduled jobs cannot overlap.
-   Jobs are scheduled only within their permitted starting-time
    windows.
-   A job may start at the exact time another job finishes.
-   Brute-force testing is limited to smaller input sizes.
-   Performance results are dependent on the computer and runtime
    environment.
-   The Excel workbook is regenerated when the performance-testing
    program is executed again.
-   Close the Excel workbook before regenerating it, because Microsoft
    Excel may lock the file while it is open.

------------------------------------------------------------------------

Project: Resource Allocation Scheduler Course: Analysis of Algorithms
Assignment: Assignment #2 Programming Language: Java Performance Output:
Excel Performance Workbook (.xlsx)
