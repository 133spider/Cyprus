package net.incus.cyprus;

import java.util.HashMap;

/**
 * Created by spider on 1/2/2017.
 */

public class ScheduleObject {

    String name;
    HashMap<String, String> schedule;
    String total;

    public ScheduleObject(String name, HashMap<String, String> schedule, String total){
        this.name = name;
        this.schedule = schedule;
        this.total = total;
    }

    public String getName(){
        return this.name;
    }

    public String getSchedule(String key){
        return this.schedule.get(key);
    }

    public String getTotal(){
        return this.total;
    }
}
