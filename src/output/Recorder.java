package output;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;

import org.apache.log4j.Logger;

import sim.Sim;
import discreteEvent.Event;

/**
 * A thread-safe class for writing metrics to files.
 * 
 * @author ftubilla
 *
 */
public class Recorder {

    public static final int DEFAULT_DIGITS = 6;
    private Logger          logger         = Logger.getLogger(Recorder.class);

    private BufferedWriter writer;
    private String         filename;
    private NumberFormat   decimalFormatter;

    public Recorder(String filename) {
        this(filename, DEFAULT_DIGITS);
    }

    public Recorder(String filename, int digits) {
        try {
            logger.info("Creating " + this.getClass().getSimpleName());
            writer = new BufferedWriter(new FileWriter(filename));
            this.filename = filename;
        } catch (IOException e) {
            System.out.println("Problems creating " + filename);
            e.printStackTrace();
            System.exit(-1);
        }
        decimalFormatter = NumberFormat.getNumberInstance();
        decimalFormatter.setMaximumFractionDigits(digits);
        decimalFormatter.setGroupingUsed(false);
    }

    public void record(String line) {
        writeToFile(line);
    }

    public void record(Object[] row) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < row.length; i++) {
            if (row[i] instanceof Double) {
                Double number = (Double) row[i];
                if ( number.isInfinite() || number.isNaN() ) {
                    line.append(String.format("%s", number));
                } else {
                    line.append(decimalFormatter.format(number));
                }
            } else {
                line.append(row[i]);
            }
            line.append(" ");
        }
        record(line.toString());
    }

    public void recordBeforeEvent(Sim sim, Event event) {
        // Override
    }

    public void recordAfterEvent(Sim sim, Event event) {
        // Override
    }

    public void recordEndOfSim(Sim sim) {
        // Override
    }

    public void updateBeforeEvent(Sim sim, Event event) {

    }

    public void updateAfterEvent(Sim sim, Event event) {

    }

    public void close() {
        try {
            logger.info("Closing " + this.getClass().getSimpleName());
            writer.close();
        } catch (IOException e) {
            System.out.println("Problems closing metric at " + filename);
            System.exit(-1);
        }
    }

    public void writeHeader(Class<?> columns) {
        String header = "";
        for (Object col : columns.getEnumConstants()) {
            header += col.toString() + " ";
        }
        writeToFile(header);
    }

    private void writeToFile(String line) {
        try {
            synchronized (writer) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Problems writing line in " + filename);
            System.exit(-1);
        }
    }

}
