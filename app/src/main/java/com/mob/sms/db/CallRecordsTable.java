package com.mob.sms.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "call_records")
public class CallRecordsTable {
    @DatabaseField(generatedId = true)
    public long table_id;
    @DatabaseField
    public String name;
    @DatabaseField
    public String mobile;
    @DatabaseField
    public long time;

    public CallRecordsTable() {

    }

    public CallRecordsTable(String name, String mobile, long time) {
        this.name = name;
        this.mobile = mobile;
        this.time = time;
    }

}
