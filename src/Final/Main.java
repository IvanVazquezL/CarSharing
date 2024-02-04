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

        /*
        ResultSet rs = dbClient.test();
        try {
            rs.next();
            System.out.println(rs.getString("NAME"));
            System.out.println(rs.getInt("ID"));
            System.out.println(rs.getInt("RENTED_CAR_ID"));

        }  catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
        */



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

        String nameFormat =  String.format("'%s'", name);
        companyController.createRecord(nameFormat);
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

            System.out.println("The customer list:");
            int counter = 1;

            HashMap<Integer, Integer> customersId = new HashMap<Integer, Integer>();
            do {
                int customerId = allCustomers.getInt("ID");
                String customerName = allCustomers.getString("NAME");
                System.out.printf("%d. %s\n", counter, customerName);
                customersId.put(counter, customerId);
                counter++;
            } while(allCustomers.next());

            System.out.println("0. Back");

            int option = scanner.nextInt();

            if (option == 0) {
                return;
            }

            displayRentedCarsMenu(customersId.get(option));
        } catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
    }

    private void displayRentedCarsMenu(int customerId) {
        int option;

        do {
            System.out.println(customerMenu);
            option = scanner.nextInt();

            switch(option) {
                case 1:
                    rentACar(customerId);
                    break;
                case 2:
                    returnACar(customerId);
                    break;
                case 3:
                    checkRentedCar(customerId);
                case 0:
                default:
                    break;
            }
        } while(option != 0);
    }

    private void rentACar(int customerId) {
        try {
            ResultSet currentCustomer = customerController.getRecordById(customerId);
            currentCustomer.next();

            if (HasARentedCar(currentCustomer)) {
                System.out.println("You've already rented a car!");
                return;
            }

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
            selectACar(companies.get(selectedCompany), customerId);
        } catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
    }

    private Boolean HasARentedCar(ResultSet customer) {
        try {
            Integer rentedCarId = customer.getInt("RENTED_CAR_ID");
            return !customer.wasNull();
        } catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            return false;
        }
    }

    private void selectACar(int companyId, int customerId) {
        try {
            String carConditions = String.format("c WHERE c.COMPANY_ID = %d AND NOT EXISTS (SELECT 1 FROM CUSTOMER cust WHERE cust.RENTED_CAR_ID = c.ID)", companyId);
            ResultSet allCarsFromCompany = carController.getAllRecords(carConditions);

            if (!allCarsFromCompany.next()) {
                System.out.println("The car list is empty!");
                return;
            }

            System.out.println("Choose a car:");
            int counter = 1;

            Map<String, Integer> cars = new HashMap<>();
            Map<Integer, String> options = new HashMap<>();

            do {
                int carId = allCarsFromCompany.getInt("ID");
                String carName = allCarsFromCompany.getString("NAME");

                cars.put(carName, carId);
                options.put(counter, carName);

                System.out.printf("%d. %s\n", counter, carName);
                counter++;
            }  while (allCarsFromCompany.next());

            int option = scanner.nextInt();

            if (option == 0) {
                return;
            }

            String selectedCar = options.get(option);
            int selectedCarId = cars.get(selectedCar);

            String updateClause = String.format("RENTED_CAR_ID = %d", selectedCarId);
            customerController.updateRecord(updateClause, customerId);
            System.out.printf("You rented '%s'\n", selectedCar);
        } catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
    }

    private void returnACar(int customerId) {
        try {
            ResultSet currentCustomer = customerController.getRecordById(customerId);
            currentCustomer.next();

            if (!HasARentedCar(currentCustomer)) {
                System.out.println("You didn't rent a car!");
                return;
            }

            String updateClause = "RENTED_CAR_ID = null";
            customerController.updateRecord(updateClause, customerId);
            System.out.println("You've returned a rented car!");
        }  catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
    }

    private void checkRentedCar(int customerId) {
        try {
            ResultSet currentCustomer = customerController.getRecordById(customerId);
            currentCustomer.next();

            if (!HasARentedCar(currentCustomer)) {
                System.out.println("You didn't rent a car!");
                return;
            }

            int rentedCarId = currentCustomer.getInt("RENTED_CAR_ID");
            ResultSet car = carController.getRecordById(rentedCarId);
            car.next();
            String carName = car.getString("NAME");

            ResultSet company = companyController.getRecordById(car.getInt("COMPANY_ID"));
            company.next();
            String companyName = company.getString("NAME");

            System.out.printf("""
                    Your rented car:
                    %s
                    Company:
                    %s
                    """, carName, companyName);
        } catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }


    }

    private void createCustomer() {
        // Consume the newline character
        scanner.nextLine();

        System.out.println("Enter the customer name:");

        String customerName = scanner.nextLine();
        String values = String.format("'%s', null", customerName);
        customerController.createRecord(values);

        System.out.println("The customer was added!");
    }
}

