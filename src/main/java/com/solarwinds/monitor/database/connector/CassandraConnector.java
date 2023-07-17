package com.solarwinds.monitor.database.connector;


import com.codahale.metrics.*;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Metrics;
import com.datastax.driver.core.Session;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class CassandraConnector
{
    private Cluster cluster;

    private Session session;

    public void connect(final String node, final int port)
    {
        this.cluster = Cluster.builder().addContactPoint(node).withPort(port).build();
        /*Metadata metadata = cluster.getMetadata();
        System.out.println("Connected to cluster: "+ metadata.getClusterName());*/
        session = cluster.connect("test");
        //session.execute("USE system" );
        /*CqlSession session = CqlSession.builder().addContactPoint(new InetSocketAddress("127.0.0.1",port)).build();
        Metadata metadata = session.getMetadata();
        Metrics metrics = session.getMetrics().
        MetricValue readRequestsMetric = metrics.getCounter("Read-Requests");
        long readRequestsCount = readRequestsMetric.getValue().asLong();
        System.out.println("Read Requests Count: " + readRequestsCount);*/
    }

    public Session getSession()
    {
        return this.session;
    }

    public void close()
    {
        cluster.close();
    }

    public static void main(String[] args) {
        CassandraConnector c = new CassandraConnector();
        c.connect("127.0.0.1",9042);

        Metrics metrics = c.cluster.getMetrics();
        metrics.getRegistry().counter("Read-Requests");
        //System.out.println("Active connection "+metrics.getOpenConnections());
        System.out.println("Read Requests Count: " + metrics.getRegistry().counter("Read-Requests").getCount());
        SortedMap<String, Counter> map = metrics.getRegistry().getCounters();
        for (String key : map.keySet()) {
            //System.out.println( key + " Count: " + map.get(key).getCount());
        }
        MetricRegistry metricRegistry = metrics.getRegistry();
        String nodeName = MetricRegistry.name("org.apache.cassandra.metrics", "CQL", "Requests", "all");

        Gauge<Integer> preparedStatementsCount = (Gauge<Integer>) metricRegistry.getMetrics().get(nodeName);
        for (String key : metricRegistry.getMetrics().keySet()) {
            if(map.get(key)!=null)
                System.out.println( key + " Count: " + map.get(key).getCount());
            else
                System.out.println( key + " Count: " + map.get(key));
        }
        System.out.println("Prepared Statements Count: " + preparedStatementsCount);


        CsvReporter reporter = CsvReporter.forRegistry(metricRegistry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build(new File("/users/kiran.kumarms/downloads/metrics/"));
        reporter.start(20, TimeUnit.SECONDS );


    }
    public static void main1(String[] args) {
        String keyspace = "system";

        // Connect to Cassandra JMX
        try {
            JMXConnector jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL("service:jmx:rmi:///jndi/rmi://10.199.8.109:7199/jmxrmi"));
            MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();

            // Retrieve read latency metrics for the keyspace
            ObjectName latencyObjectName = new ObjectName("org.apache.cassandra.metrics:type=Keyspace,keyspace=" + keyspace);
            AttributeList attributeList = mbeanServerConnection.getAttributes(latencyObjectName, new String[]{"ReadLatency"});
            Attribute attribute = (Attribute) attributeList.get(0);
            CompositeData compositeData = (CompositeData) attribute.getValue();
            Double readLatency = (Double) compositeData.get("Value");

            // Print the read latency value
            System.out.println("Read Latency for keyspace '" + keyspace + "': " + readLatency + " ms");

            jmxConnector.close();
        } catch (IOException | MalformedObjectNameException  | ReflectionException e) {
            e.printStackTrace();
        }  catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }



}
