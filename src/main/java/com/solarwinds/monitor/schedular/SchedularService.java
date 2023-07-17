package com.solarwinds.monitor.schedular;

import com.solarwinds.monitor.service.MonitorService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedularService {

    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    public void scheduleJobs(){
        scheduler.scheduleAtFixedRate(new MonitorService(), 0, 1, TimeUnit.SECONDS);

        // Sleep for 10 seconds to allow the scheduler to run
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Shut down the scheduler
        scheduler.shutdown();
    }
}
