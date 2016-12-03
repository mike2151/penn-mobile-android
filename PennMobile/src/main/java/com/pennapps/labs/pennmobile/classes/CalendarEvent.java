package com.pennapps.labs.pennmobile.classes;

import android.support.annotation.NonNull;
import android.util.SparseIntArray;

import java.util.HashMap;
import java.util.Iterator;
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
    public int color;
    public final static int[] colors = {0xFF95CFAF, 0xFFF26E67, 0xFFFFC107, 0x4990E2, 0xFFAC92EC};

    public CalendarEvent(String startDate, String endDate, String title, String location) {
        this.startDate = Long.parseLong(startDate);
        this.endDate = Long.parseLong(endDate);
        this.title = title;
        this.location = location;
    }

    @NonNull
    public static CalendarEvent[][] fillCalendarGrid(CalendarEvent[] copy) {
        CalendarEvent[] events = new CalendarEvent[copy.length];
        System.arraycopy(copy, 0, events, 0, copy.length);
        if (events.length == 0) {
            return null;
        }
        int[] overlap = new int[events.length];
        TreeMap<Long, CalendarEvent> specialTime = new TreeMap<>();
        HashMap<CalendarEvent, Integer> eventToIndex = new HashMap<>();
        HashMap<CalendarEvent, Integer> seenMap = new HashMap<>();
        int index = 0;
        for (int i = 0; i < events.length; i++) {
            CalendarEvent event = events[i];
            //avoid overlapping key, startDate still < endDate
            while (specialTime.containsKey(event.startDate)) {
                event.startDate++;
            }
            specialTime.put(event.startDate, event);
            while (specialTime.containsKey(event.endDate)) {
                event.endDate++;
            }
            specialTime.put(event.endDate, event);
            eventToIndex.put(event, i);
            event.color = colors[index];
            index = (index + 1) % 4;
        }
        for (Map.Entry<Long, CalendarEvent> entry : specialTime.entrySet()) {
            CalendarEvent event = entry.getValue();
            if (entry.getKey().equals(event.startDate)) {
                seenMap.put(event, eventToIndex.get(event));

                for (Map.Entry<CalendarEvent, Integer> ent : seenMap.entrySet()) {
                    if (overlap[ent.getValue()] < seenMap.size()) {
                        overlap[ent.getValue()] = seenMap.size();
                    }
                }
            } else {
                seenMap.remove(event);
            }
        }
        //find length of the grid
        int cm = 1;
        for (int i : overlap) {
            if (cm % i != 0) {
                cm *= i;
            }
        }
        //fill grid
        Iterator<Map.Entry<Long, CalendarEvent>> iter = specialTime.entrySet().iterator();
        iter.next();
        CalendarEvent[][] grid = new CalendarEvent[specialTime.size()][cm];
        for (int i = 0; i < cm / overlap[0]; i++) {
            grid[0][i] = events[0];
        }
        for (int i = 1; i < specialTime.size(); i++) {
            System.arraycopy(grid[i - 1], 0, grid[i], 0, cm);
            Map.Entry<Long, CalendarEvent> entry = iter.next();
            CalendarEvent event = entry.getValue();
            if (entry.getKey().equals(event.startDate)) {
                //fit it into the grid
                boolean in = false;
                for (int k = 0; k < grid[0].length; k++) {
                    boolean fit = true;
                    for (int l = 0; l < grid[0].length / overlap[eventToIndex.get(event)]; l++) {
                        if (k + l >= grid[0].length || grid[i][k + l] != null) {
                            fit = false;
                            k += l;
                            break;
                        }
                    }
                    if (fit) {
                        for (int l = 0; l < grid[0].length / overlap[eventToIndex.get(event)]; l++) {
                            grid[i][k + l] = event;
                        }
                        in = true;
                        break;
                    }
                }
                if (!in) {
                    //find the breaks
                    SparseIntArray blanks = new SparseIntArray();
                    for (int k = 0; k < grid[0].length; k++) {
                        if (grid[i][k] == null) {
                            for (int l = 0; l <= grid[0].length - k; l++) {
                                if (k+l >= grid[0].length || grid[i][k+l] != null) {
                                    blanks.append(k, l);
                                    k += l;
                                    break;
                                }
                            }
                        }
                    }
                    SparseIntArray unmovableBlanks = new SparseIntArray();
                    SparseIntArray selfContained = new SparseIntArray();
                    while (blanks.size() > 0) {
                        int blankColumn = blanks.keyAt(0);
                        int blankSize = blanks.valueAt(0);
                        blanks.removeAt(0);
                        boolean interrupt = false;

                        for (int j = 0; j < i && !interrupt; j++) {
                            //first check if the left side is fine
                            if (blankColumn > 0) {
                                if (grid[j][blankColumn] != null && grid[j][blankColumn].equals(grid[j][blankColumn-1])) {
                                    unmovableBlanks.put(blankColumn, blankSize);
                                    blankSize--;
                                    blankColumn++;
                                    if (blankSize > 0) {
                                        blanks.put(blankColumn, blankSize);
                                    }
                                    interrupt = true;
                                    break;
                                }
                            }
                            //then check if the second side is fine
                            if (blankColumn + blankSize < grid[0].length - 1) {
                                if (grid[j][blankColumn] != null && grid[j][blankColumn].equals(grid[j][blankColumn+1])) {
                                    unmovableBlanks.put(blankColumn, blankSize);
                                    blankSize--;
                                    blankColumn--;
                                    if (blankSize > 0) {
                                        blanks.put(blankColumn, blankSize);
                                    }
                                    interrupt = true;
                                    break;
                                }
                            }
                            //then check the middle to see if we need to break it into anything
                            for (int l = 1; l < blankSize; l++) {
                                if ((grid[j][blankColumn] == null && grid[j][blankColumn + 1] != null) ||
                                        (grid[j][blankColumn] != null && !grid[j][blankColumn].equals(grid[j][blankColumn+l]))) {
                                    blanks.put(blankColumn, l);
                                    blanks.put(blankColumn + l, blankSize - l);
                                    interrupt = true;
                                    break;
                                }
                            }
                        }
                        if (!interrupt) {
                            selfContained.put(blankColumn, blankSize);
                        }
                    }

                    //now that we know where the unmovable blanks are, and the selfcontained where we can move them
                    //we want to group the selfcontained together
                    if (unmovableBlanks.size() > 1) {
                        //check if can't group the blanks together
                        int max = 0;
                        int moveindex = 0;
                        for (int k = 0; k < unmovableBlanks.size(); k++) {
                            if (unmovableBlanks.valueAt(k) > max) {
                                max = unmovableBlanks.valueAt(k);
                                moveindex = k;
                            }
                        }
                        for (int k = 0; k < selfContained.size(); k++) {
                            max += selfContained.valueAt(k);
                        }
                        if (max < grid[0].length / overlap[eventToIndex.get(event)]) {
                            continue; //can't put it in gg
                        }
                        //we can move them all to the unmovable blank
                        for (int k =  0; k < selfContained.size(); k++) {
                            if (selfContained.keyAt(k) < unmovableBlanks.keyAt(moveindex)) {
                                int moves = unmovableBlanks.keyAt(moveindex) - selfContained.keyAt(k) - selfContained.valueAt(k);
                                for (int l = 0; l < selfContained.keyAt(k); k++) {
                                    for (int m = 0; m < moves; m++) {
                                        //to be continued.
                                    }
                                }

                            } else {
                                int moves = selfContained.keyAt(k) - unmovableBlanks.keyAt(moveindex) - unmovableBlanks.valueAt(moveindex);
                                //not completed.
                            }
                        }
                    }

                }
            } else {
                for (int k = 0; k < cm; k++) {
                    if (grid[i][k] != null && grid[i][k].equals(event)) {
                        grid[i][k] = null;
                    }
                }
            }
        }

        //check grid for color:
        for (int i = 1; i < grid.length; i++) {
            for (int j = 1; j < grid[0].length; j++) {
                if (grid[i][j-1] != null && grid[i][j] != null && (!grid[i][j-1].equals(grid[i][j])
                        && grid[i][j-1].color == grid[i][j].color)) {
                    grid[i][j].color = colors[4];
                }
                if (grid[i-1][j] != null && grid[i][j] != null && (!grid[i-1][j].equals(grid[i][j])
                        && grid[i-1][j].color == grid[i][j].color)) {
                    grid[i][j].color = colors[4];
                }
            }
        }
        return grid;
    }
}
