package Final.controller;


import Final.repo.DBClient;
import Final.service.impl.ICRUD;

import java.sql.ResultSet;
public class CustomerController implements ICRUD {
    private final DBClient dbClient;
    private final String tableName = "CUSTOMER";
    private final String tableColumns = "NAME, RENTED_CAR_ID";

    public CustomerController(DBClient dbClient) {
        this.dbClient = dbClient;
    }

    public ResultSet getAllRecords() {
        return getAllRecords("");
    }
    @Override
    public ResultSet getAllRecords(String conditions) {
        return dbClient.getAllRecords(tableName, conditions);
    }

    @Override
    public void createRecord(String values) {
        dbClient.createRecord(tableName, tableColumns, values);
    }

    @Override
    public ResultSet getRecordById(int id) {
        String conditions = String.format("WHERE ID = %d", id);
        return this.getAllRecords(conditions);
    }

    @Override
    public void updateRecord(String updateClause, int id) {
        dbClient.updateRecord(tableName, updateClause, id);
    }
}
