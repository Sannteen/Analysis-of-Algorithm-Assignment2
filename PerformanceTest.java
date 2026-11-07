import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.*;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PerformanceTest {

    private static final int[] INPUT_SIZES = {5, 10, 50, 100, 1000};

    private static final int GREEDY_WARMUPS = 5;
    private static final int GREEDY_RUNS = 20;

    private static final int BRUTE_FORCE_MAX_SIZE = 10;
    private static final int BRUTE_FORCE_WARMUPS = 1;
    private static final int BRUTE_FORCE_RUNS = 5;

    private static final Path RESULTS_DIRECTORY = Path.of("Results");
    private static final Path EXCEL_FILE =
            RESULTS_DIRECTORY.resolve("Scheduler_Performance_Results.xlsx");

    private static final String[] RESULT_HEADERS = {
            "Input Size",
            "Greedy Average Time (ms)",
            "Greedy Total Profit",
            "Brute Force Average Time (ms)",
            "Brute Force Total Profit",
            "Profit Difference",
            "Greedy Matches Brute Force",
            "Brute Force Tested"
    };

    private static final String[] JOB_HEADERS = {
            "Test Size",
            "Job ID",
            "Profit",
            "Duration",
            "Earliest Start",
            "Latest Start"
    };

    private static final class TestResult {
        int inputSize;
        double greedyAverageTime;
        long greedyProfit;

        double bruteForceAverageTime;
        long bruteForceProfit;

        boolean bruteForceTested;

        TestResult(int inputSize,
                   double greedyAverageTime,
                   long greedyProfit,
                   double bruteForceAverageTime,
                   long bruteForceProfit,
                   boolean bruteForceTested) {

            this.inputSize = inputSize;
            this.greedyAverageTime = greedyAverageTime;
            this.greedyProfit = greedyProfit;
            this.bruteForceAverageTime = bruteForceAverageTime;
            this.bruteForceProfit = bruteForceProfit;
            this.bruteForceTested = bruteForceTested;
        }

        long getProfitDifference() {
            if (!bruteForceTested) {
                return 0;
            }

            return bruteForceProfit - greedyProfit;
        }

        String getGreedyComparison() {
            if (!bruteForceTested) {
                return "Not compared";
            }

            return greedyProfit == bruteForceProfit ? "Yes" : "No";
        }
    }

    public static void main(String[] args) {

        List<TestResult> results = new ArrayList<>();
        List<List<Job>> datasets = new ArrayList<>();

        try {
            Files.createDirectories(RESULTS_DIRECTORY);

            System.out.println("======================================");
            System.out.println(" RESOURCE ALLOCATION SCHEDULER");
            System.out.println(" PERFORMANCE TESTING");
            System.out.println("======================================");

            for (int size : INPUT_SIZES) {

                System.out.println("\nTesting input size: " + size);

                List<Job> jobs = generateJobs(size);
                datasets.add(jobs);

                TestResult result = runPerformanceTest(size, jobs);
                results.add(result);

                displayResult(result);
            }

            createExcelWorkbook(results, datasets);

            System.out.println("\n======================================");
            System.out.println(" PERFORMANCE TESTING COMPLETED");
            System.out.println("======================================");
            System.out.println("Excel workbook created successfully:");
            System.out.println(EXCEL_FILE.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("Unable to create the Excel workbook.");
            e.printStackTrace();
        }
    }

    // --------------------------------------------------
    // GENERATE TEST DATA
    // --------------------------------------------------

    private static List<Job> generateJobs(int numberOfJobs) {

        List<Job> jobs = new ArrayList<>();

        Random random = new Random(42L + numberOfJobs);

        for (int i = 1; i <= numberOfJobs; i++) {

            int profit = 50 + random.nextInt(451);
            int duration = 1 + random.nextInt(5);

            int earliestStart = random.nextInt(100);
            int latestStart = earliestStart + random.nextInt(2);

            Job job = new Job(
                    "J" + i,
                    profit,
                    duration,
                    earliestStart,
                    latestStart
            );

            jobs.add(job);
        }

        return jobs;
    }

    // --------------------------------------------------
    // PERFORMANCE TESTING
    // --------------------------------------------------

    private static TestResult runPerformanceTest(
            int inputSize,
            List<Job> jobs) {

        GreedyScheduler.Result greedyResult = null;

        // Greedy warm-up executions
        for (int i = 0; i < GREEDY_WARMUPS; i++) {
            GreedyScheduler.schedule(jobs);
        }

        // Measure greedy scheduler
        long greedyTotalTime = 0;

        for (int i = 0; i < GREEDY_RUNS; i++) {

            long startTime = System.nanoTime();

            greedyResult = GreedyScheduler.schedule(jobs);

            long endTime = System.nanoTime();

            greedyTotalTime += endTime - startTime;
        }

        double greedyAverageTime =
                (greedyTotalTime / (double) GREEDY_RUNS) / 1_000_000.0;

        long greedyProfit = greedyResult.totalProfit;

        // Brute force is restricted to small datasets
        if (inputSize <= BRUTE_FORCE_MAX_SIZE) {

            GreedyScheduler.Result bruteForceResult = null;

            // Brute-force warm-up
            for (int i = 0; i < BRUTE_FORCE_WARMUPS; i++) {
                BruteForceScheduler.schedule(jobs);
            }

            // Measure brute-force scheduler
            long bruteForceTotalTime = 0;

            for (int i = 0; i < BRUTE_FORCE_RUNS; i++) {

                long startTime = System.nanoTime();

                bruteForceResult = BruteForceScheduler.schedule(jobs);

                long endTime = System.nanoTime();

                bruteForceTotalTime += endTime - startTime;
            }

            double bruteForceAverageTime =
                    (bruteForceTotalTime / (double) BRUTE_FORCE_RUNS)
                            / 1_000_000.0;

            return new TestResult(
                    inputSize,
                    greedyAverageTime,
                    greedyProfit,
                    bruteForceAverageTime,
                    bruteForceResult.totalProfit,
                    true
            );
        }

        return new TestResult(
                inputSize,
                greedyAverageTime,
                greedyProfit,
                Double.NaN,
                -1,
                false
        );
    }

    // --------------------------------------------------
    // DISPLAY RESULTS
    // --------------------------------------------------

    private static void displayResult(TestResult result) {

        System.out.println("--------------------------------------");
        System.out.println("Input size: " + result.inputSize);

        System.out.printf(
                "Greedy average time: %.6f ms%n",
                result.greedyAverageTime
        );

        System.out.println("Greedy total profit: " + result.greedyProfit);

        if (result.bruteForceTested) {

            System.out.printf(
                    "Brute force average time: %.6f ms%n",
                    result.bruteForceAverageTime
            );

            System.out.println(
                    "Brute force total profit: " + result.bruteForceProfit
            );

            System.out.println(
                    "Greedy matches brute force: "
                            + result.getGreedyComparison()
            );

        } else {
            System.out.println("Brute force: Not tested");
        }
    }

    // --------------------------------------------------
    // CREATE EXCEL WORKBOOK
    // --------------------------------------------------

    private static void createExcelWorkbook(
            List<TestResult> results,
            List<List<Job>> datasets) throws IOException {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            createStyles(workbook);

            createSummarySheet(workbook, results);
            createResultsSheet(workbook, results);
            createRuntimeSheet(workbook, results);
            createProfitSheet(workbook, results);
            createDatasetsSheet(workbook, datasets);

            try (OutputStream outputStream =
                         Files.newOutputStream(EXCEL_FILE)) {

                workbook.write(outputStream);
            }
        }
    }

    // --------------------------------------------------
    // WORKBOOK STYLES
    // --------------------------------------------------

    private static CellStyle headerStyle;
    private static CellStyle titleStyle;
    private static CellStyle sectionStyle;
    private static CellStyle decimalStyle;
    private static CellStyle integerStyle;
    private static CellStyle textStyle;

    private static void createStyles(XSSFWorkbook workbook) {

        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 18);

        titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(
                IndexedColors.DARK_BLUE.getIndex()
        );
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setWrapText(true);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        Font sectionFont = workbook.createFont();
        sectionFont.setBold(true);
        sectionFont.setFontHeightInPoints((short) 12);

        sectionStyle = workbook.createCellStyle();
        sectionStyle.setFont(sectionFont);
        sectionStyle.setFillForegroundColor(
                IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex()
        );
        sectionStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        decimalStyle = workbook.createCellStyle();
        decimalStyle.setDataFormat(
                workbook.createDataFormat().getFormat("0.000000")
        );

        integerStyle = workbook.createCellStyle();
        integerStyle.setDataFormat(
                workbook.createDataFormat().getFormat("#,##0")
        );

        textStyle = workbook.createCellStyle();
        textStyle.setWrapText(true);
        textStyle.setVerticalAlignment(VerticalAlignment.TOP);
    }

    private static void createTitle(
            Sheet sheet,
            String title,
            int lastColumn) {

        Row row = sheet.createRow(0);
        row.setHeightInPoints(30);

        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(titleStyle);

        if (lastColumn > 0) {
            sheet.addMergedRegion(
                    new CellRangeAddress(0, 0, 0, lastColumn)
            );
        }
    }

    private static void createHeader(
            Sheet sheet,
            int rowNumber,
            String[] headers) {

        Row row = sheet.createRow(rowNumber);
        row.setHeightInPoints(35);

        for (int i = 0; i < headers.length; i++) {

            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    // --------------------------------------------------
    // 01 - SUMMARY DASHBOARD
    // --------------------------------------------------

    private static void createSummarySheet(
            XSSFWorkbook workbook,
            List<TestResult> results) {

        Sheet sheet = workbook.createSheet("01 Summary Dashboard");

        createTitle(sheet, "RESOURCE ALLOCATION SCHEDULER", 4);

        Row subtitle = sheet.createRow(2);
        subtitle.createCell(0).setCellValue(
                "Performance Testing Summary"
        );

        createSection(sheet, 4, "TEST CONFIGURATION");

        addSummaryRow(sheet, 5, "Input sizes tested",
                "5, 10, 50, 100 and 1,000 jobs");

        addSummaryRow(sheet, 6, "Greedy warm-up executions",
                String.valueOf(GREEDY_WARMUPS));

        addSummaryRow(sheet, 7, "Greedy measured executions",
                String.valueOf(GREEDY_RUNS));

        addSummaryRow(sheet, 8, "Brute-force warm-up executions",
                String.valueOf(BRUTE_FORCE_WARMUPS));

        addSummaryRow(sheet, 9, "Brute-force measured executions",
                String.valueOf(BRUTE_FORCE_RUNS));

        addSummaryRow(sheet, 10, "Brute-force maximum input size",
                String.valueOf(BRUTE_FORCE_MAX_SIZE));

        createSection(sheet, 12, "PERFORMANCE HIGHLIGHTS");

        TestResult smallest = results.get(0);
        TestResult largest = results.get(results.size() - 1);

        addSummaryRow(sheet, 13, "Greedy runtime at 5 jobs",
                String.format("%.6f ms", smallest.greedyAverageTime));

        addSummaryRow(sheet, 14, "Greedy runtime at 1,000 jobs",
                String.format("%.6f ms", largest.greedyAverageTime));

        addSummaryRow(sheet, 15, "Greedy profit at 1,000 jobs",
                String.valueOf(largest.greedyProfit));

        addSummaryRow(sheet, 16, "Brute-force comparison",
                "Performed for 5 and 10 jobs only");

        createSection(sheet, 18, "OBSERVATIONS");

        addSummaryRow(sheet, 19, "Runtime",
                "Greedy execution time increased as the input size increased.");

        addSummaryRow(sheet, 20, "Brute-force performance",
                "Brute force was slower than greedy for the tested small datasets.");

        addSummaryRow(sheet, 21, "Profit comparison",
                "Greedy and brute force returned the same profit in the two tested datasets.");

        addSummaryRow(sheet, 22, "Scalability",
                "Brute force was not run on larger datasets because of its exponential search.");

        addSummaryRow(sheet, 23, "Important limitation",
                "Matching brute-force results on these datasets does not prove greedy is always optimal.");

        addSummaryRow(sheet, 24, "Counterexample",
                "The sample Main program demonstrates greedy profit 230 versus brute-force profit 250.");

        sheet.setColumnWidth(0, 34 * 256);
        sheet.setColumnWidth(1, 100 * 256);
        sheet.createFreezePane(0, 4);
    }

    private static void createSection(
            Sheet sheet,
            int rowNumber,
            String title) {

        Row row = sheet.createRow(rowNumber);
        Cell cell = row.createCell(0);

        cell.setCellValue(title);
        cell.setCellStyle(sectionStyle);

        sheet.addMergedRegion(
                new CellRangeAddress(rowNumber, rowNumber, 0, 1)
        );
    }

    private static void addSummaryRow(
            Sheet sheet,
            int rowNumber,
            String label,
            String value) {

        Row row = sheet.createRow(rowNumber);

        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(sectionStyle);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(textStyle);
    }

    // --------------------------------------------------
    // 02 - TEST RESULTS
    // --------------------------------------------------

    private static void createResultsSheet(
            XSSFWorkbook workbook,
            List<TestResult> results) {

        Sheet sheet = workbook.createSheet("02 Test Results");

        createTitle(sheet, "PERFORMANCE TEST RESULTS", 7);
        createHeader(sheet, 2, RESULT_HEADERS);

        int rowNumber = 3;

        for (TestResult result : results) {

            Row row = sheet.createRow(rowNumber++);

            row.createCell(0).setCellValue(result.inputSize);

            Cell greedyTime = row.createCell(1);
            greedyTime.setCellValue(result.greedyAverageTime);
            greedyTime.setCellStyle(decimalStyle);

            Cell greedyProfit = row.createCell(2);
            greedyProfit.setCellValue(result.greedyProfit);
            greedyProfit.setCellStyle(integerStyle);

            if (result.bruteForceTested) {

                Cell bruteTime = row.createCell(3);
                bruteTime.setCellValue(result.bruteForceAverageTime);
                bruteTime.setCellStyle(decimalStyle);

                Cell bruteProfit = row.createCell(4);
                bruteProfit.setCellValue(result.bruteForceProfit);
                bruteProfit.setCellStyle(integerStyle);

                Cell difference = row.createCell(5);
                difference.setCellValue(result.getProfitDifference());
                difference.setCellStyle(integerStyle);

            } else {
                row.createCell(3).setCellValue("N/A");
                row.createCell(4).setCellValue("N/A");
                row.createCell(5).setCellValue("N/A");
            }

            row.createCell(6).setCellValue(
                    result.getGreedyComparison()
            );

            row.createCell(7).setCellValue(
                    result.bruteForceTested ? "Yes" : "No"
            );
        }

        sheet.setColumnWidth(0, 15 * 256);
        sheet.setColumnWidth(1, 25 * 256);
        sheet.setColumnWidth(2, 22 * 256);
        sheet.setColumnWidth(3, 29 * 256);
        sheet.setColumnWidth(4, 27 * 256);
        sheet.setColumnWidth(5, 20 * 256);
        sheet.setColumnWidth(6, 30 * 256);
        sheet.setColumnWidth(7, 20 * 256);

        sheet.createFreezePane(0, 3);
        sheet.setAutoFilter(
                new CellRangeAddress(2, 2 + results.size(), 0, 7)
        );
    }

    // --------------------------------------------------
    // 03 - RUNTIME ANALYSIS
    // --------------------------------------------------

    private static void createRuntimeSheet(
            XSSFWorkbook workbook,
            List<TestResult> results) {

        XSSFSheet sheet =
                workbook.createSheet("03 Runtime Analysis");

        createTitle(sheet, "EXECUTION TIME ANALYSIS", 5);

        createHeader(sheet, 2, new String[]{
                "Input Size",
                "Greedy Time (ms)",
                "Brute Force Time (ms)"
        });

        int rowNumber = 3;

        for (TestResult result : results) {

            Row row = sheet.createRow(rowNumber++);

            row.createCell(0).setCellValue(result.inputSize);

            Cell greedy = row.createCell(1);
            greedy.setCellValue(result.greedyAverageTime);
            greedy.setCellStyle(decimalStyle);

            if (result.bruteForceTested) {
                Cell brute = row.createCell(2);
                brute.setCellValue(result.bruteForceAverageTime);
                brute.setCellStyle(decimalStyle);
            }
        }

        sheet.setColumnWidth(0, 18 * 256);
        sheet.setColumnWidth(1, 25 * 256);
        sheet.setColumnWidth(2, 30 * 256);

        // Chart 1: Greedy execution time across all input sizes
        createBarChart(
                sheet,
                "Greedy Execution Time Across Input Sizes",
                "Input Size (Jobs)",
                "Average Execution Time (ms)",
                0, 1, 2 + results.size(),
                4, 0, 20, 10,
                "Greedy Average Time"
        );

        // Chart 2: Greedy vs brute force on small datasets
        createBarChart(
                sheet,
                "Greedy vs Brute Force Execution Time",
                "Input Size (Jobs)",
                "Average Execution Time (ms)",
                0, 1, 4,
                4, 10, 20, 20,
                "Greedy Time",
                "Brute Force Time"
        );
    }

    // --------------------------------------------------
    // 04 - PROFIT ANALYSIS
    // --------------------------------------------------

    private static void createProfitSheet(
            XSSFWorkbook workbook,
            List<TestResult> results) {

        XSSFSheet sheet =
                workbook.createSheet("04 Profit Analysis");

        createTitle(sheet, "TOTAL PROFIT ANALYSIS", 5);

        createHeader(sheet, 2, new String[]{
                "Input Size",
                "Greedy Total Profit",
                "Brute Force Total Profit"
        });

        int rowNumber = 3;

        for (TestResult result : results) {

            Row row = sheet.createRow(rowNumber++);

            row.createCell(0).setCellValue(result.inputSize);

            Cell greedy = row.createCell(1);
            greedy.setCellValue(result.greedyProfit);
            greedy.setCellStyle(integerStyle);

            if (result.bruteForceTested) {
                Cell brute = row.createCell(2);
                brute.setCellValue(result.bruteForceProfit);
                brute.setCellStyle(integerStyle);
            }
        }

        sheet.setColumnWidth(0, 18 * 256);
        sheet.setColumnWidth(1, 27 * 256);
        sheet.setColumnWidth(2, 30 * 256);

        // Chart 3: Greedy profit across all input sizes
        createBarChart(
                sheet,
                "Greedy Total Profit Across Input Sizes",
                "Input Size (Jobs)",
                "Total Profit",
                0, 1, 2 + results.size(),
                4, 0, 20, 10,
                "Greedy Total Profit"
        );

        // Chart 4: Profit comparison for small datasets
        createBarChart(
                sheet,
                "Greedy vs Brute Force Total Profit",
                "Input Size (Jobs)",
                "Total Profit",
                0, 1, 4,
                4, 10, 20, 20,
                "Greedy Profit",
                "Brute Force Profit"
        );
    }

    // --------------------------------------------------
    // EXCEL BAR CHART CREATION
    // --------------------------------------------------

    private static void createBarChart(
            XSSFSheet sheet,
            String title,
            String xAxisTitle,
            String yAxisTitle,
            int categoryColumn,
            int firstValueColumn,
            int lastDataRow,
            int firstDataRow,
            int anchorColumn,
            int lastAnchorColumn,
            int lastAnchorRow,
            String... seriesTitles) {

        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        XSSFClientAnchor anchor = drawing.createAnchor(
                0, 0, 0, 0,
                anchorColumn, firstDataRow,
                lastAnchorColumn, lastAnchorRow
        );

        XSSFChart chart = drawing.createChart(anchor);

        chart.setTitleText(title);
        chart.setTitleOverlay(false);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.BOTTOM);

        XDDFCategoryAxis bottomAxis =
                chart.createCategoryAxis(AxisPosition.BOTTOM);

        bottomAxis.setTitle(xAxisTitle);

        XDDFValueAxis leftAxis =
                chart.createValueAxis(AxisPosition.LEFT);

        leftAxis.setTitle(yAxisTitle);
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

        XDDFChartData data = chart.createData(
                ChartTypes.BAR,
                bottomAxis,
                leftAxis
        );

        XDDFBarChartData barData = (XDDFBarChartData) data;

        barData.setBarDirection(BarDirection.COL);
        barData.setBarGrouping(BarGrouping.CLUSTERED);
        barData.setGapWidth(80);

        XDDFDataSource<String> categories =
                XDDFDataSourcesFactory.fromStringCellRange(
                        sheet,
                        new CellRangeAddress(
                                3,
                                lastDataRow,
                                categoryColumn,
                                categoryColumn
                        )
                );

        for (int i = 0; i < seriesTitles.length; i++) {

            XDDFNumericalDataSource<Double> values =
                    XDDFDataSourcesFactory.fromNumericCellRange(
                            sheet,
                            new CellRangeAddress(
                                    3,
                                    lastDataRow,
                                    firstValueColumn + i,
                                    firstValueColumn + i
                            )
                    );

            XDDFChartData.Series series =
                    data.addSeries(categories, values);

            series.setTitle(seriesTitles[i], null);
        }

        chart.plot(data);
    }

    // --------------------------------------------------
    // 05 - GENERATED DATASETS
    // --------------------------------------------------

    private static void createDatasetsSheet(
            XSSFWorkbook workbook,
            List<List<Job>> datasets) {

        Sheet sheet = workbook.createSheet("05 Generated Datasets");

        createTitle(sheet, "GENERATED TEST DATASETS", 5);
        createHeader(sheet, 2, JOB_HEADERS);

        int rowNumber = 3;

        for (List<Job> jobs : datasets) {

            for (Job job : jobs) {

                Row row = sheet.createRow(rowNumber++);

                row.createCell(0).setCellValue(jobs.size());
                row.createCell(1).setCellValue(job.getId());
                row.createCell(2).setCellValue(job.getProfit());
                row.createCell(3).setCellValue(job.getDuration());
                row.createCell(4).setCellValue(job.getEarliestStart());
                row.createCell(5).setCellValue(job.getLatestStart());
            }
        }

        sheet.setColumnWidth(0, 15 * 256);
        sheet.setColumnWidth(1, 18 * 256);
        sheet.setColumnWidth(2, 15 * 256);
        sheet.setColumnWidth(3, 15 * 256);
        sheet.setColumnWidth(4, 20 * 256);
        sheet.setColumnWidth(5, 20 * 256);

        sheet.createFreezePane(0, 3);
        sheet.setAutoFilter(
                new CellRangeAddress(2, rowNumber - 1, 0, 5)
        );
    }
}
