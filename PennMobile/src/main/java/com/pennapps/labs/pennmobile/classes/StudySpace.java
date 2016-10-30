package com.pennapps.labs.pennmobile.classes;

import java.util.List;

/**
 * Created by Jason on 10/29/2016.
 */

public class StudySpace {
    public int id;
    public String name;
    public String url;
    public List<TimeSlot> slots;

    public class TimeSlot {
        public String room_name;
        public String start_time;
        public String end_time;

    }
}
