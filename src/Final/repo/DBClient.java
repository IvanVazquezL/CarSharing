package Final.repo;

import java.sql.*;

public class DBClient {
    private final String JDBC_DRIVER;
    private final String DB_URL;
    private Connection conn = null;
    private Statement stmt = null;
    public DBClient(String JDBC_DRIVER, String DB_URL) {
        this.JDBC_DRIVER = JDBC_DRIVER;
        this.DB_URL = DB_URL;
        run();
    }

    public void run() {
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();
        }  catch(Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }

    public ResultSet test() {
        try {
            String getAllQuery = "SELECT * FROM CUSTOMER";
            ResultSet rs = stmt.executeQuery(getAllQuery);

            return rs;
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            return null;
        }
    }

    public void createTable(String tableName, String tableColumns) {
        try {
            String createQuery = String.format("CREATE TABLE %s %s", tableName, tableColumns);
            stmt.executeUpdate(createQuery);
        } catch(Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }

    public ResultSet getAllRecords(String tableName, String conditions) {
        try {
            System.out.println(String.format("SELECT * FROM %s %s", tableName, conditions));
            String getAllQuery = String.format("SELECT * FROM %s %s", tableName, conditions);
            ResultSet rs = stmt.executeQuery(getAllQuery);

            return rs;
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            return null;
        }
    }

    public void createRecord(String tableName, String columns, String values) {
        try {
            String createRecordQuery = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, values);
            stmt.executeUpdate(createRecordQuery);
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
    }

    public void updateRecord(String tableName, String updateClause, int id) {
        try {
            String updateRecordQuery = String.format("UPDATE %s SET %s WHERE ID = %d", tableName, updateClause, id);
            stmt.executeUpdate(updateRecordQuery);
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
    }

    public void closeJDBCResources() {
        try {
            // Clean-up environment
            stmt.close();
            conn.close();
        } catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } finally {
            //finally block used to close resources
            try{
                if(stmt!=null) stmt.close();
            } catch(SQLException se2) {
            } // nothing we can do
            try {
                if(conn!=null) conn.close();
            } catch(SQLException se){
                se.printStackTrace();
            } //end finally try
        }
    }
}


