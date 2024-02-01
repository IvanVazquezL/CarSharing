package Final;

import Final.controller.CarController;
import Final.controller.CompanyController;
import Final.controller.CustomerController;
import Final.repo.DBClient;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        String JDBC_DRIVER = "org.h2.Driver";
        String DB_URL = "jdbc:h2:./src/carsharing/db/carsharing";

        DBClient dbClient = new DBClient(JDBC_DRIVER, DB_URL);
        CarSharingManager csm = new CarSharingManager(dbClient);
        csm.displayMenu();
    }
}

class CarSharingManager {
    private final static Scanner scanner = new Scanner(System.in);
    private final DBClient dbClient;
    private final String mainMenu = """
                1. Log in as a manager
                2. Log in as a customer
                3. Create a customer
                0. Exit
                """;
    private final String companyMenu = """
                \n1. Company list
                2. Create a company
                0. Back
                """;
    private final String carMenu = """
                1. Car list
                2. Create a car
                0. Back
               """;
    private final String customerMenu = """
            1. Rent a car
            2. Return a rented car
            3. My rented car
            0. Back
            """;
    private final CompanyController companyController;
    private final CarController carController;
    private final CustomerController customerController;

    CarSharingManager(DBClient dbClient) {
        this.dbClient = dbClient;
        initializeDatabaseSchema();
        this.companyController = new CompanyController(this.dbClient);
        this.carController = new CarController(this.dbClient);
        this.customerController = new CustomerController(this.dbClient);
    }

    private void initializeDatabaseSchema() {
        dbClient.createTable("COMPANY",
                """
                            (ID INTEGER PRIMARY KEY AUTO_INCREMENT,
                            NAME VARCHAR(255) UNIQUE NOT NULL)
                            """);
        dbClient.createTable("CAR", """
                    (ID INTEGER PRIMARY KEY AUTO_INCREMENT,
                    NAME VARCHAR(255) UNIQUE NOT NULL,
                    COMPANY_ID INTEGER NOT NULL,
                    FOREIGN KEY (COMPANY_ID) REFERENCES COMPANY(ID))
                    """);
        dbClient.createTable("CUSTOMER", """
                    (ID INTEGER PRIMARY KEY AUTO_INCREMENT,
                    NAME VARCHAR(255) UNIQUE NOT NULL,
                    RENTED_CAR_ID INTEGER,
                    FOREIGN KEY (RENTED_CAR_ID) REFERENCES CAR(ID))
                """);
    }

    public void displayMenu() {
        int option;

        do {
            System.out.print(mainMenu);
            option = scanner.nextInt();

            switch(option) {
                case 1:
                    displayCompanyMenu();
                    break;
                case 2:
                    displayCustomerMenu();
                    break;
                case 3:
                    createCustomer();
                    break;
                default:
                    break;
            }
        } while(option != 0);
    }

    private void displayCompanyMenu() {
        int option;

        do {
            System.out.print(companyMenu);
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
    }

    private void listCompanies() {
        try {
            ResultSet allCompanies = companyController.getAllRecords();

            if (!allCompanies.next()) {
                System.out.println("The company list is empty!");
                return;
            }

            int counter = 1;
            Map<String, Integer> companies = new HashMap<>();
            Map<Integer, String> options = new HashMap<>();

            System.out.println("Choose the company:");
            do {
                int id = allCompanies.getInt("ID");
                String name = allCompanies.getString("NAME");

                companies.put(name, id);
                options.put(counter, name);

                System.out.printf("%d. %s\n", counter, name);
                counter++;
            } while (allCompanies.next());

            System.out.println("0. Back");

            int option = scanner.nextInt();

            if (option == 0) {
                return;
            }
            String selectedCompany = options.get(option);
            displayCarMenu(selectedCompany, companies.get(selectedCompany));

        } catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
    }

    private void createCompany() {
        // Consume the newline character
        scanner.nextLine();

        System.out.println("Enter the company name:");
        String name = scanner.nextLine();

        companyController.createRecord(name);
        System.out.println("The company was created!");
    }

    private void displayCarMenu(String company, int id) {
        int option;

        do {
            System.out.printf("'%s' company\n", company);
            System.out.println(carMenu);
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
            String conditions = String.format("WHERE COMPANY_ID = %d", id);
            ResultSet rs = carController.getAllRecords(conditions);

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
        // Consume the newline character
        scanner.nextLine();

        System.out.println("Enter the car name:");
        String name = scanner.nextLine();

        String values = String.format("'%s', %d", name, id);
        carController.createRecord(values);

        System.out.println("The car was added!");
    }

    private void displayCustomerMenu() {
        try {
            ResultSet allCustomers = customerController.getAllRecords();

            if (!allCustomers.next()) {
                System.out.println("The customer list is empty!");
                return;
            }

            System.out.println("Customer list:");
            int counter = 1;

            do {
                String customerName = allCustomers.getString("NAME");
                System.out.printf("%d. %s\n", counter, customerName);
                counter++;
            } while(allCustomers.next());

            System.out.println("0. Back");

            int option = scanner.nextInt();

            if (option == 0) {
                return;
            }

        } catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
    }

    private void displayRentedCarsMenu() {
        System.out.println(customerMenu);
        int option;

        do {
            option = scanner.nextInt();

            switch(option) {
                case 1:
                    rentACar();
                    break;
                case 2:
                    //createCar(id);
                    break;
                case 0:
                default:
                    break;
            }
        } while(option != 0);
    }

    private void rentACar() {
        try {
            ResultSet allCompanies = companyController.getAllRecords();

            if (!allCompanies.next()) {
                System.out.println("The company list is empty!");
                return;
            }

            System.out.println("Choose a company:");
            Map<String, Integer> companies = new HashMap<>();
            Map<Integer, String> options = new HashMap<>();
            int counter = 1;

            do {
                int id = allCompanies.getInt("ID");
                String companyName = allCompanies.getString("NAME");

                companies.put(companyName, id);
                options.put(counter, companyName);

                System.out.printf("%d. %s\n", counter, companyName);
                counter++;
            } while(allCompanies.next());

            System.out.println("0. Back");

            int option = scanner.nextInt();

            if (option == 0) {
                return;
            }

            String selectedCompany = options.get(option);
            selectACar(companies.get(selectedCompany));
        } catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
    }

    private void selectACar(int id) {
        try {
            String conditions = String.format("WHERE COMPANY_ID = %d", id);
            ResultSet rs = carController.getAllRecords(conditions);

            if (!rs.next()) {
                System.out.println("The car list is empty!");
                return;
            }

            System.out.println("Choose a car:");
            int counter = 1;

            Map<String, Integer> cars = new HashMap<>();
            Map<Integer, String> options = new HashMap<>();

            do {
                int carId = rs.getInt("ID");
                String carName = rs.getString("NAME");

                cars.put(carName, id);
                options.put(counter, carName);

                System.out.printf("%d. %s\n", counter, carName);
                counter++;
            }  while (rs.next());

            int option = scanner.nextInt();

            if (option == 0) {
                return;
            }

            String selectedCar = options.get(option);
            int selectedCarId = cars.get(selectedCar);

        } catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
    }

    private void createCustomer() {
        System.out.println("Enter the customer name:");

        String customerName = scanner.nextLine();
        String values = String.format("'%s', null", customerName);
        customerController.createRecord(values);

        System.out.println("The customer was added!");
    }
}
