package com.pennapps.labs.pennmobile.classes;

import android.graphics.Color;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Jason on 11/10/2016.
 */

public class CalendarEvent {
    public Long startDate;
    public Long endDate;
    public String title;
    public String location;
    public Color color;

    public CalendarEvent(String startDate, String endDate, String title, String location) {
        this.startDate = Long.parseLong(startDate);
        this.endDate = Long.parseLong(endDate);
        this.title = title;
        this.location = location;
    }

    @NonNull
    public static int[] countOverlap(CalendarEvent[] events, Color[] colors) {
        int[] answer = new int[events.length];
        TreeMap<Long, CalendarEvent> specialTime = new TreeMap<>();
        HashMap<CalendarEvent, Integer> eventToIndex = new HashMap<>();
        HashMap<CalendarEvent, Integer> seenMap = new HashMap<>();
        for (int i = 0; i < events.length; i++) {
            CalendarEvent event = events[i];
            specialTime.put(event.startDate, event);
            specialTime.put(event.endDate, event);
            eventToIndex.put(event, i);
        }
        for (Map.Entry<Long, CalendarEvent> entry : specialTime.entrySet()) {
            CalendarEvent event = entry.getValue();
            if (entry.getKey().equals(event.startDate)) {
                seenMap.put(event, eventToIndex.get(event));

                for (Map.Entry<CalendarEvent, Integer> ent : seenMap.entrySet()) {
                    if (answer[ent.getValue()] < seenMap.size()) {
                        answer[ent.getValue()] = seenMap.size();
                    }
                }
            } else {
                seenMap.remove(event);
            }
        }
        return answer;
    }
}
