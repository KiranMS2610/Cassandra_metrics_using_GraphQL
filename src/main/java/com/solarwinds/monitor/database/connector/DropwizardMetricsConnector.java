package com.solarwinds.monitor.database.connector;

import com.codahale.metrics.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DropwizardMetricsConnector {
	private static final MetricRegistry metricRegistry = new MetricRegistry();

    public static void main(String[] args) throws IOException {

        // Create a JmxReporter
        JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).build();
        jmxReporter.start();

        // Create a Timer
        Meter readLatencyTimer = metricRegistry.meter("read_latency");
        Meter writeLatencyTimer = metricRegistry.meter("write_latency");

        // Start a read latency measurement
        long start = System.currentTimeMillis();
        int readLatency = 1000;
        long end = System.currentTimeMillis();
        readLatencyTimer.mark(end - start);

        // Start a write latency measurement
        start = System.currentTimeMillis();
        int writeLatency = 2000;
        end = System.currentTimeMillis();
   
        writeLatencyTimer.mark(end-start);

        // Sleep for a second
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Print the latencies
        System.out.println("read_latency: " + readLatencyTimer.getMeanRate());
        System.out.println("write_latency: " + writeLatencyTimer.getMeanRate());
    }
}