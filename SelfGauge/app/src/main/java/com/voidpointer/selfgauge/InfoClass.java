package com.voidpointer.selfgauge;

import java.util.Calendar;

/**
 * Created by SHIN on 2016-08-16.
 */
public class InfoClass {
    public int _id;
    public long datetime;
    public String type;             // "power" , "gas"
    public int usage;
    public String deleted;
    public boolean selected;

    public InfoClass(){}

    public InfoClass(int _id, long datetime, String type, int usage, String deleted ){
        this._id = _id;
        this.datetime = datetime;
        this.type = type;
        this.usage = usage;
        this.deleted = deleted;
        this.selected = false;
    }
}