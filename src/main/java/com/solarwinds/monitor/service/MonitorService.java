package com.solarwinds.monitor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class MonitorService implements Runnable {


    @Autowired
    private Environment env;

    @Override
    public void run() {

    }
}
