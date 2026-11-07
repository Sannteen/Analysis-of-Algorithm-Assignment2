import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class PerformanceTest {
    private static final int[] SIZES = {5, 10, 50, 100, 1000};
    private static final int WARMUP = 5, RUNS = 20;

    public static void main(String[] args) throws IOException {
        Path results = Paths.get("Results");
        Files.createDirectories(results);
        double[] times = new double[SIZES.length];
        double[] profits = new double[SIZES.length];
        double[] optimal = new double[SIZES.length];
        Arrays.fill(optimal, Double.NaN);

        try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(results.resolve("test_results.csv")))) {
            csv.println("jobs,GreedyProfit,GreedyTimeMs,OptimalProfit,BruteTimeMs");
            System.out.printf("%-7s %-15s %-16s %-15s %-16s%n", "jobs", "Greedy Profit", "Greedy Time(ms)", "Optimal Profit", "Brute Time(ms)");
            for (int i = 0; i < SIZES.length; i++) {
                int n = SIZES[i];
                List<job> jobs = generatejobs(n);
                for (int w = 0; w < WARMUP; w++) GreedyScheduler.schedule(jobs);
                long elapsed = 0;
                GreedyScheduler.Result greedy = null;
                for (int r = 0; r < RUNS; r++) {
                    long start = System.nanoTime();
                    greedy = GreedyScheduler.schedule(jobs);
                    elapsed += System.nanoTime() - start;
                }
                times[i] = elapsed / (RUNS * 1_000_000.0);
                profits[i] = greedy.totalProfit;
                String op = "N/A", bt = "N/A";
                if (n <= 10) {
                    long start = System.nanoTime();
                    GreedyScheduler.Result brute = BruteForceScheduler.schedule(jobs);
                    double ms = (System.nanoTime() - start) / 1_000_000.0;
                    optimal[i] = brute.totalProfit;
                    op = Long.toString(brute.totalProfit);
                    bt = String.format(Locale.US, "%.4f", ms);
                }
                String gt = String.format(Locale.US, "%.4f", times[i]);
                System.out.printf("%-7d %-15d %-16s %-15s %-16s%n", n, greedy.totalProfit, gt, op, bt);
                csv.printf(Locale.US, "%d,%d,%s,%s,%s%n", n, greedy.totalProfit, gt, op, bt);
            }
        }
        lineChart(results.resolve("runtime_graph.png"), "Input Size vs. Greedy Running Time", "Average time (ms)", times);
        lineChart(results.resolve("profit_graph.png"), "Input Size vs. Greedy Total Profit", "Total profit ($)", profits);
        comparisonChart(results.resolve("comparison_graph.png"), profits, optimal);
        System.out.println("\nSaved CSV and three PNG graphs in: " + results.toAbsolutePath());
    }

    public static List<job> generatejobs(int n) {
        Random random = new Random(42 + n);
        List<job> jobs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int profit = 50 + random.nextInt(451);
            int duration = 1 + random.nextInt(5);
            int earliest = random.nextInt(100);
            int latest = earliest + random.nextInt(6);
            jobs.add(new job("J" + (i + 1), profit, duration, earliest, latest));
        }
        return jobs;
    }

    private static Graphics2D canvas(BufferedImage image, String title, String ylabel) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setColor(new Color(30, 41, 59));
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString(title, 85, 45);
        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g.drawString("Number of jobs", 380, 565);
        g.rotate(-Math.PI / 2);
        g.drawString(ylabel, -365, 25);
        g.rotate(Math.PI / 2);
        g.setColor(Color.DARK_GRAY);
        g.drawLine(90, 85, 90, 510);
        g.drawLine(90, 510, 860, 510);
        return g;
    }

    private static void lineChart(Path file, String title, String ylabel, double[] values) throws IOException {
        BufferedImage img = new BufferedImage(960, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(img, title, ylabel);
        double max = Arrays.stream(values).max().orElse(1.0) * 1.12;
        if (max == 0) max = 1;
        int[] xs = new int[values.length];
        int[] ys = new int[values.length];
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        for (int tick = 0; tick <= 5; tick++) {
            int y = 510 - tick * 80;
            g.setColor(new Color(220, 225, 230));
            g.drawLine(90, y, 860, y);
            g.setColor(Color.DARK_GRAY);
            g.drawString(String.format(Locale.US, "%.2f", max * tick / 5), 35, y + 5);
        }
        for (int i = 0; i < values.length; i++) {
            xs[i] = 110 + i * 180;
            ys[i] = 510 - (int)Math.round(400 * values[i] / max);
            g.setColor(Color.DARK_GRAY);
            g.drawString(Integer.toString(SIZES[i]), xs[i] - 12, 533);
        }
        g.setColor(new Color(37, 99, 235));
        g.setStroke(new BasicStroke(3));
        for (int i = 1; i < xs.length; i++) g.drawLine(xs[i-1], ys[i-1], xs[i], ys[i]);
        for (int i = 0; i < xs.length; i++) {
            g.fillOval(xs[i]-5, ys[i]-5, 10, 10);
            g.setColor(Color.DARK_GRAY);
            g.drawString(String.format(Locale.US, "%.4f", values[i]), xs[i]-22, Math.max(75, ys[i]-13));
            g.setColor(new Color(37, 99, 235));
        }
        g.dispose();
        ImageIO.write(img, "png", file.toFile());
    }

    private static void comparisonChart(Path file, double[] greedy, double[] optimal) throws IOException {
        BufferedImage img = new BufferedImage(960, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(img, "Greedy vs. Optimal Profit (Small Inputs)", "Total profit ($)");
        double max = Math.max(Math.max(greedy[0], greedy[1]), Math.max(optimal[0], optimal[1])) * 1.2;
        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        for (int t = 0; t <= 5; t++) {
            int y = 510 - t * 80;
            g.setColor(new Color(220,225,230));
            g.drawLine(90, y, 860, y);
            g.setColor(Color.DARK_GRAY);
            g.drawString(String.format(Locale.US, "%.0f", max*t/5), 35, y+5);
        }
        for (int i = 0; i < 2; i++) {
            int center = 300 + i * 350;
            int gh = (int)(400 * greedy[i] / max);
            int oh = (int)(400 * optimal[i] / max);
            g.setColor(new Color(37,99,235));
            g.fillRect(center-75, 510-gh, 70, gh);
            g.setColor(new Color(13,148,136));
            g.fillRect(center+5, 510-oh, 70, oh);
            g.setColor(Color.DARK_GRAY);
            g.drawString(SIZES[i] + " jobs", center-25, 535);
            g.drawString(String.format(Locale.US, "%.0f", greedy[i]), center-75, 495-gh);
            g.drawString(String.format(Locale.US, "%.0f", optimal[i]), center+5, 495-oh);
        }
        g.setColor(new Color(37,99,235)); g.fillRect(340, 65, 18, 18);
        g.setColor(Color.DARK_GRAY); g.drawString("Greedy", 365, 80);
        g.setColor(new Color(13,148,136)); g.fillRect(470, 65, 18, 18);
        g.setColor(Color.DARK_GRAY); g.drawString("Optimal", 495, 80);
        g.dispose();
        ImageIO.write(img, "png", file.toFile());
    }
}
