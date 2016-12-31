package com.voidpointer.selfgauge;

import android.provider.BaseColumns;

/**
 * Created by SHIN on 2016-08-16.
 */
public class Tables {
    public static final class Usage implements BaseColumns {
        public static final String DATE = "date";
        public static final String TYPE = "type";
        public static final String USAGE = "usage";
        public static final String DELETED = "deleted";
        public static final String _TABLENAME = "_usage_powergas";
        public static final String _CREATE_STRING =
                "create table "+_TABLENAME+"("
                        +_ID+" integer primary key autoincrement, "
                        +DATE+" long not null , "
                        +TYPE+" text not null , "
                        +USAGE+" integer not null , "
                        +DELETED+" text not null );";
    }
}