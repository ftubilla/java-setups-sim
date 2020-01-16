package sim;

public class ProgressBar {

    private int size;
    private double taskLength;
    private char covered = '+';
    private int currentBarProgress = 0;
    private double taskProgress = 0.0;
    private boolean first = true;
    private Long initialTime;

    public ProgressBar(int size, double taskLength) {
        this.size = size;
        this.taskLength = taskLength;
    }

    public void init() {
        this.initialTime = System.currentTimeMillis();
    }

    synchronized public void setProgress(double progress) {
        taskProgress += taskProgress + progress;
    }

    synchronized public void addOneUnitOfProgress() {
        taskProgress++;
    }

    public void display() {
        int barProgress = (int) Math.floor(size * taskProgress / (1.0 * taskLength));
        if (barProgress > currentBarProgress || first) {
            StringBuilder bar = new StringBuilder(size + 2);
            bar.append('[');
            for (int i = 0; i <= size; i++) {
                if (i <= barProgress) {
                    bar.append(covered);
                } else {
                    bar.append(' ');
                }
            }
            bar.append(']');
            double elapsedTimeMin = (System.currentTimeMillis() - this.initialTime) / (double) 1_000 / (double) 60;
            bar.append(String.format(" %.0f/%.0f | %.2f%% | %.2f min per task | %.2f ETA min",
                    taskProgress, taskLength, taskProgress / taskLength * 100,
                    elapsedTimeMin / taskProgress,
                    elapsedTimeMin / taskProgress * ( taskLength - taskProgress ) ));
            System.out.println(bar);
            currentBarProgress = barProgress;
            first = false;
        }
    }
}
