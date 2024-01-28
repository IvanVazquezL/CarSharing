package ForeignKey;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        CarSharingManager csm = new CarSharingManager();
        csm.Menu();
    }
}

class CarSharingManager {
    private final static Scanner scanner = new Scanner(System.in);
    private final String JDBC_DRIVER = "org.h2.Driver";
    private final String DB_URL = "jdbc:h2:./src/carsharing/db/carsharing";
    private Connection conn = null;
    private Statement stmt = null;
    CarSharingManager() {
        try {
            // STEP 1: Register JDBC driver
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();

            String createCompany = String.format("""
                    CREATE TABLE COMPANY
                    (ID INTEGER PRIMARY KEY AUTO_INCREMENT,
                    NAME VARCHAR(255) UNIQUE NOT NULL)  
                    """);
            stmt.executeUpdate(createCompany);

            String createCar = String.format("""
                    CREATE TABLE CAR
                    (ID INTEGER PRIMARY KEY AUTO_INCREMENT,
                    NAME VARCHAR(255) UNIQUE NOT NULL,
                    COMPANY_ID INTEGER NOT NULL,
                    FOREIGN KEY (COMPANY_ID) REFERENCES COMPANY(ID))
                    """);
            stmt.executeUpdate(createCar);

        } catch(Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }

    public void Menu() {
        int option;

        do {
            System.out.print("""
                1. Log in as a manager
                0. Exit
                """);
            option = scanner.nextInt();

            if (option == 1) {
                innerMenu();
            }
        } while(option != 0);
    }

    private void innerMenu() {
        int option;

        do {
            System.out.print("""
            \n1. Company list
            2. Create a company
            0. Back
                    """);
            option = scanner.nextInt();

            switch(option) {
                case 1:
                    listCompanies();
                    break;
                case 2:
                    createCompany();
                    break;
                default:
                    break;
            }
        } while(option != 0);

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

    private void listCompanies() {
        try {
            String sql = "SELECT * FROM COMPANY";
            ResultSet rs = stmt.executeQuery(sql);

            if (!rs.next()) {
                System.out.println("The company list is empty!");
                return;
            }

            int counter = 1;
            Map<String, Integer> companies = new HashMap<>();
            Map<Integer, String> options = new HashMap<>();

            System.out.println("Choose the company:");
            do {
                int id = rs.getInt("ID");
                String name = rs.getString("NAME");
                companies.put(name, id);
                options.put(counter, name);
                System.out.printf("%d. %s\n", counter, name);
                counter++;
            } while (rs.next());

            System.out.println("0. Back");

            int option = scanner.nextInt();

            if (option == 0) {
                return;
            }
            String selectedCompany = options.get(option);
            companyMenu(selectedCompany, companies.get(selectedCompany));

        } catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
    }

    private void createCompany() {
        try {
            // Consume the newline character
            scanner.nextLine();

            System.out.println("Enter the company name:");
            String name = scanner.nextLine();

            String sql = String.format("INSERT INTO COMPANY (NAME) VALUES('%s')", name);
            stmt.executeUpdate(sql);
            System.out.println("The company was created!");
        } catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
    }

    private void companyMenu(String company, int id) {
        int option;

        do {
            System.out.printf("'%s' company\n", company);
            System.out.println("""
            1. Car list
            2. Create a car
            0. Back
                    """);
            option = scanner.nextInt();

            switch(option) {
                case 1:
                    listCars(id);
                    break;
                case 2:
                    createCar(id);
                    break;
                case 0:
                default:
                    break;
            }
        } while(option != 0);
    }

    private void listCars(int id) {
        try {
            String sql = String.format("""
                    SELECT * FROM CAR
                    WHERE COMPANY_ID = %d 
                    """, id);
            ResultSet rs = stmt.executeQuery(sql);

            if (!rs.next()) {
                System.out.println("The car list is empty!");
                return;
            }

            System.out.println("Car list:");
            int counter = 1;

            do {
                String name = rs.getString("NAME");
                System.out.printf("%d. %s\n", counter, name);
                counter++;
            }  while (rs.next());
        } catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
    }

    private void createCar(int id) {
        try {
            // Consume the newline character
            scanner.nextLine();

            System.out.println("Enter the car name:");
            String name = scanner.nextLine();

            String sql = String.format("""
            INSERT INTO CAR (NAME, COMPANY_ID) VALUES('%s', %d)
            """, name, id);
            stmt.executeUpdate(sql);
            System.out.println("The car was added!");
        } catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
    }
}
