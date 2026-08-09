package jforgame.data.common;

import jforgame.data.annotation.DataTable;
import jforgame.data.annotation.Id;
import jforgame.data.annotation.Index;


/**
 * Common constant table
 * If the configuration table name is not "common", modify the jforgame.data.commonTableName property in application.yml
 */
@DataTable(name = "common")
public class CommonData {

    @Id
    private int id;

    @Index(unique = true)
    private String key;

    private String value;

    private String desc;

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}
