package org.pa.dbutil;

import java.sql.Connection;
import java.sql.DriverManager;

import org.h2.*;

public class DbConnection {

    public final static int DEV = 1;
    public final static int TEST = 2;
    
    // Note ~// designates the current user's home directory. TODO. Externalize the directory and a db user name and password.
  
    private final static String DB_DEV_URL = "jdbc:h2:~/development/workspace/java-projects/book-site-jpa/db/dev.db;AUTO_SERVER=TRUE";
    private final static String DB_TEST_URL = "jdbc:h2:~/development/workspace/java-projects/book-site-jpa/db/test.db;AUTO_SERVER=TRUE";
    
  
    private final static String DRIVER = "org.h2.Driver";
    private final static String DB_USER = "";
    private final static String DB_PASS = "";
  

    private int instace;

    public DbConnection(int instance) {
        this.instace = instance;
    }

    public Connection getConnection() throws Exception {

        try {
            Class.forName(DRIVER);
            Connection conn;
            switch (instace) {
                case DEV:
                    conn = DriverManager.getConnection(DB_DEV_URL, DB_USER, DB_PASS);
                    break;
                case TEST:
                    conn = DriverManager.getConnection(DB_TEST_URL, DB_USER, DB_PASS);
                    break;
                default:
                    conn = DriverManager.getConnection(DB_TEST_URL, DB_USER, DB_PASS);
                    break;
            }

            return conn;
        } catch (Exception e) {
            System.err.println("Exception createing Database Exception: " + e.getMessage());
            throw e;
        }

    }

    public void closeConnection(Connection conn) {
        try {
            conn.close();
        } catch (Exception e) {
            System.err.println("Exception Closing Database Connection Exception: " + e.getMessage());
        } finally {
            conn = null;
        }
    }

}
