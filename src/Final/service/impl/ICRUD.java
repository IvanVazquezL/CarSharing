package Final.service.impl;

import java.sql.ResultSet;

public interface ICRUD {
    ResultSet getAllRecords(String conditions);
    void createRecord(String values);

}
