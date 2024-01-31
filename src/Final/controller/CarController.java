package Final.controller;

import Final.repo.DBClient;
import Final.service.impl.ICRUD;

import java.sql.ResultSet;

public class CarController implements ICRUD {
    private final DBClient dbClient;
    private final String tableName = "CAR";
    private final String tableColumns = "NAME, COMPANY_ID";

    public CarController(DBClient dbClient) {
        this.dbClient = dbClient;
    }
    @Override
    public ResultSet getAllRecords(String conditions) {
        return dbClient.getAllRecords(tableName, conditions);
    }

    @Override
    public void createRecord(String values) {
        dbClient.createRecord(tableName, tableColumns, values);
    }
}
