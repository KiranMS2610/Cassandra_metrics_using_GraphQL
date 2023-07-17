package com.solarwinds.monitor.database.connector;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MSSqlConnector {

    private String host;
    private int port;

    private String userName;
    private String password;
    private String databaseName;

    public MSSqlConnector(String host,int port,String databasename,String username,String password){
        this.host = host;
        this.port = port;
        this.databaseName = databasename;
        this.userName = username;
        this.password = password;
    }
    Connection databaseConnection = null;
    public void connect() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("JDBC driver loaded");
            String url = "jdbc:sqlserver://"+host+":"+port+";databaseName="+databaseName+";user="+userName+";password="+password+";encrypt=true;trustServerCertificate=true";
            databaseConnection = DriverManager.getConnection(url);

        } catch (Exception err) {
            System.err.println("Error loading JDBC driver");
            err.printStackTrace(System.err);
            System.exit(0);
        }
    }
    public Connection getConnection()
    {
        return this.databaseConnection;
    }

    public void close() throws SQLException {
        databaseConnection.close();
    }

    /*public static void main(String[] args) {
        MSSqlConnector c = new MSSqlConnector("10.199.8.109",1433,"master","sa","Confio123");
        c.connect();

    }*/

    public static void main(String[] args) {
        // Create a session with your Cassandra cluster
        //CqlSession session = CqlSession.builder().addContactPoint(new InetSocketAddress("10.199.8.109",9042)).build();
        Session session = Cluster.builder().addContactPoint("10.199.8.109").withPort(9042).build().connect();
        // Simulate read requests
        for (int i = 0; i < 1000; i++) {
            ResultSet resultSet = session.execute("SELECT * FROM mydb.books WHERE id = " + i);
            Row row = resultSet.one();

            if (row != null) {
                // Process the retrieved data if necessary
                System.out.println("Read data: " + row.toString());
            }else{
                System.out.println("Read data: null ");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // Close the session when you're done
        session.close();
    }
}
