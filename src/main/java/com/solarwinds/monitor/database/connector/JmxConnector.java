package com.solarwinds.monitor.database.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JmxConnector {
    private static final long INSERT_INTERVAL = 2000; // 2 seconds

    public static void main(String[] args) {
        // JMX connection parameters
        String jmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:7199/jmxrmi";
        String dbUrl = "jdbc:mysql://localhost:3306/cas_metrics";
        String dbUsername = "root";
        String dbPassword = "msk@123";

        try {
            // Connect to JMX
            JMXServiceURL jmxServiceUrl = new JMXServiceURL(jmxUrl);
            Map<String, Object> env = new HashMap<>();
            JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxServiceUrl, env);
            MBeanServerConnection mbeanConnection = jmxConnector.getMBeanServerConnection();

            // Connect to the MySQL database
            Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

            // Prepare the SQL INSERT statement
            String insertQuery = "INSERT INTO latency_stats (read_latency_count, read_latency_one_minute_rate, read_latency_five_minute_rate, read_latency_fifteen_minute_rate, read_total_latency, write_latency_count, write_latency_one_minute_rate, write_latency_five_minute_rate, write_latency_fifteen_minute_rate, write_total_latency, total_disk_space_used) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

            // Create and start the thread
            Thread insertThread = new Thread(() -> {
                try {
                    while (true) {
                        String objectNameStr = "org.apache.cassandra.metrics:type=Table,keyspace=test,scope=employee_by_id,name=ReadLatency";
                        ObjectName objectName = new ObjectName(objectNameStr);
                        double readLatencyCount = ((Number) mbeanConnection.getAttribute(objectName, "Count")).doubleValue();
                        double readLatencyOneMinuteRate = ((Number) mbeanConnection.getAttribute(objectName, "OneMinuteRate")).doubleValue();
                        double readLatencyFiveMinuteRate = ((Number) mbeanConnection.getAttribute(objectName, "FiveMinuteRate")).doubleValue();
                        double readLatencyFifteenMinuteRate = ((Number) mbeanConnection.getAttribute(objectName, "FifteenMinuteRate")).doubleValue();
                        long readTotalLatency = (long) mbeanConnection.getAttribute(new ObjectName("org.apache.cassandra.metrics:type=Keyspace,keyspace=test,name=ReadTotalLatency"), "Count");
                        double writeLatencyCount = ((Number) mbeanConnection.getAttribute(new ObjectName("org.apache.cassandra.metrics:type=Keyspace,keyspace=test,name=WriteLatency"), "Count")).doubleValue();
                        double writeLatencyOneMinuteRate = ((Number) mbeanConnection.getAttribute(new ObjectName("org.apache.cassandra.metrics:type=Keyspace,keyspace=test,name=WriteLatency"), "OneMinuteRate")).doubleValue();
                        double writeLatencyFiveMinuteRate = ((Number) mbeanConnection.getAttribute(new ObjectName("org.apache.cassandra.metrics:type=Keyspace,keyspace=test,name=WriteLatency"), "FiveMinuteRate")).doubleValue();
                        double writeLatencyFifteenMinuteRate = ((Number) mbeanConnection.getAttribute(new ObjectName("org.apache.cassandra.metrics:type=Keyspace,keyspace=test,name=WriteLatency"), "FifteenMinuteRate")).doubleValue();
                        long writeTotalLatency = (long) mbeanConnection.getAttribute(new ObjectName("org.apache.cassandra.metrics:type=Keyspace,keyspace=test,name=WriteTotalLatency"), "Count");
                        double totalDiskSpaceUsed = ((Number) mbeanConnection.getAttribute(new ObjectName("org.apache.cassandra.metrics:type=Keyspace,keyspace=test,name=TotalDiskSpaceUsed"), "Value")).doubleValue();

                        // Set the values for the prepared statement
                        preparedStatement.setDouble(1, readLatencyCount);
                        preparedStatement.setDouble(2, readLatencyOneMinuteRate);
                        preparedStatement.setDouble(3, readLatencyFiveMinuteRate);
                        preparedStatement.setDouble(4, readLatencyFifteenMinuteRate);
                        preparedStatement.setLong(5, readTotalLatency);
                        preparedStatement.setDouble(6, writeLatencyCount);
                        preparedStatement.setDouble(7, writeLatencyOneMinuteRate);
                        preparedStatement.setDouble(8, writeLatencyFiveMinuteRate);
                        preparedStatement.setDouble(9, writeLatencyFifteenMinuteRate);
                        preparedStatement.setLong(10, writeTotalLatency);
                        preparedStatement.setDouble(11, totalDiskSpaceUsed);

                        // Execute the INSERT query
                        preparedStatement.executeUpdate();
                        System.out.println("Data inserted successfully!");

                        Thread.sleep(INSERT_INTERVAL); // Wait for the specified interval
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // Clean up resources
                    try {
                        preparedStatement.close();
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });

            insertThread.start(); // Start the thread

            // Wait for the thread to finish (optional)
            insertThread.join();

            // Close the JMX connection
            jmxConnector.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
