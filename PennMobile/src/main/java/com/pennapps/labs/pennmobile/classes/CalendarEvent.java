package com.pennapps.labs.pennmobile.classes;

import android.support.annotation.NonNull;
import android.util.SparseIntArray;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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

    private static void swap(CalendarEvent[][] grid, int x, int x2) {
        for (int i = 0; i < grid.length; i++) {
            CalendarEvent temp = grid[i][x];
            grid[i][x] = grid[i][x2];
            grid[i][x] = temp;
        }
    }

    @NonNull
    public static CalendarEvent[][] fillCalendarGrid(CalendarEvent[] copy) {
        CalendarEvent[] events = new CalendarEvent[copy.length];
        HashMap<CalendarEvent, CalendarEvent> eventMap = new HashMap<>();
        for (int i = 0; i < events.length; i++) {
            events[i] = new CalendarEvent(copy[i].startDate + "", copy[i].endDate + "", copy[i].title, copy[i].location);
            eventMap.put(events[i], copy[i]);
        }
        if (events.length == 0) {
            return null;
        }
        int[] overlap = new int[events.length];
        TreeMap<Long, CalendarEvent> specialTime = new TreeMap<>();
        HashMap<CalendarEvent, Integer> eventToIndex = new HashMap<>();
        HashMap<CalendarEvent, Integer> seenMap = new HashMap<>();

        colorAndUniquafy(events, specialTime, eventToIndex);

        calculateOverlap(overlap, specialTime, eventToIndex, seenMap);

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
                boolean in = tryFit(overlap[eventToIndex.get(event)], grid, grid[i], event);
                //if it doesn't go in
                if (!in) {
                    if (moveBlanks(overlap, eventToIndex, grid, i, event)) {
                        continue; //can't put it in gg
                    }
                    tryFit(overlap[eventToIndex.get(event)], grid, grid[i], event);
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
        doubleCheckColor(grid);

        //delete blank rows and wrong rows
        CalendarEvent[][] answer = deleteBlankRows(eventMap, specialTime, grid);
        return answer;
    }

    private static void colorAndUniquafy(CalendarEvent[] events, TreeMap<Long, CalendarEvent> specialTime, HashMap<CalendarEvent, Integer> eventToIndex) {
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
    }

    private static void calculateOverlap(int[] overlap, TreeMap<Long, CalendarEvent> specialTime, HashMap<CalendarEvent, Integer> eventToIndex, HashMap<CalendarEvent, Integer> seenMap) {
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
    }

    private static boolean tryFit(int i, CalendarEvent[][] grid, CalendarEvent[] calendarEvents, CalendarEvent event) {
        boolean in = false;
        for (int k = 0; k < grid[0].length && !in; k++) {
            boolean fit = true;
            //check if it fits
            for (int l = 0; l < grid[0].length / i && fit; l++) {
                if (k + l >= grid[0].length || calendarEvents[k + l] != null) {
                    fit = false;
                    k += l;
                }
            }
            //if it fits
            if (fit) {
                for (int l = 0; l < grid[0].length / i; l++) {
                    calendarEvents[k + l] = event;
                }
                in = true;
            }
        }
        return in;
    }

    private static boolean moveBlanks(int[] overlap, HashMap<CalendarEvent, Integer> eventToIndex, CalendarEvent[][] grid, int i, CalendarEvent event) {
        //find the breaks
        SparseIntArray blanks = new SparseIntArray();
        for (int k = 0; k < grid[0].length; k++) {
            if (grid[i][k] == null) {
                boolean foundBlanks = false;
                for (int l = 0; l <= grid[0].length - k && !foundBlanks; l++) {
                    if (k + l >= grid[0].length || grid[i][k + l] != null) {
                        blanks.append(k, l);
                        k += l;
                        foundBlanks = true;
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
                    if (grid[j][blankColumn] != null && grid[j][blankColumn].equals(grid[j][blankColumn - 1])) {
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
                    if (grid[j][blankColumn] != null && grid[j][blankColumn].equals(grid[j][blankColumn + 1])) {
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
                            (grid[j][blankColumn] != null && !grid[j][blankColumn].equals(grid[j][blankColumn + l]))) {
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
                return true;
            }
            //we can move them all to the unmovable blank
            for (int k = 0; k < selfContained.size(); k++) {
                if (selfContained.keyAt(k) < unmovableBlanks.keyAt(moveindex)) {
                    int moves = unmovableBlanks.keyAt(moveindex) - selfContained.keyAt(k) - selfContained.valueAt(k);
                    for (int l = 0; l < selfContained.valueAt(k); k++) {
                        for (int m = 0; m < moves; m++) {
                            int start = selfContained.keyAt(k) + selfContained.valueAt(k) - l;
                            swap(grid, start + m - 1, start + m);
                        }
                    }
                } else {
                    int moves = selfContained.keyAt(k) - unmovableBlanks.keyAt(moveindex) - unmovableBlanks.valueAt(moveindex);
                    for (int l = 0; l < selfContained.valueAt(k); k++) {
                        for (int m = 0; m < moves; m++) {
                            int start = selfContained.keyAt(k) + l - m;
                            swap(grid, start, start - 1);
                        }
                    }
                }
            }
        }
        return false;
    }

    private static void doubleCheckColor(CalendarEvent[][] grid) {
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
    }

    @NonNull
    private static CalendarEvent[][] deleteBlankRows(HashMap<CalendarEvent, CalendarEvent> eventMap, TreeMap<Long, CalendarEvent> specialTime, CalendarEvent[][] grid) {
        LinkedList<CalendarEvent[]> gridList = new LinkedList<>();
        CalendarEvent[] specialArray = new CalendarEvent[specialTime.size()];
        int count = 0;
        for (Map.Entry<Long, CalendarEvent> entry : specialTime.entrySet()) {
            specialArray[count] = entry.getValue();
            count++;
        }
        for (int i = 0; i < grid.length; i++) {
            CalendarEvent[] array = grid[i];
            int save = i;
            int cover = 1;
            int maxEvent = eventCount(array);
            boolean stop = false;
            for (int j = 1; j < grid.length - i && !stop; j++) {
                stop = true;
                if (!specialArray[i+j].equals(eventMap.get(specialArray[i+j]))) {
                    count = eventCount(grid[i+j]);
                    cover++;
                    if (count > maxEvent) {
                        maxEvent = count;
                        save = i+j;
                    }
                    stop = false;
                }
            }
            if (cover == 1) {
                boolean notBlank = false;
                for (int l = 0; l < array.length && !notBlank; l++) {
                    if (array[l] != null) {
                        notBlank = true;
                    }
                }
                if (notBlank) {
                    gridList.add(array);
                }
            } else {
                gridList.add(grid[save]);
                i += cover - 1;
            }
        }
        CalendarEvent[][] answer = new CalendarEvent[gridList.size()][grid[0].length];
        int i = 0;
        for (CalendarEvent[] array : gridList) {
            answer[i] = array;
            i++;
        }
        return answer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CalendarEvent that = (CalendarEvent) o;

        if (startDate != null ? !startDate.equals(that.startDate) : that.startDate != null)
            return false;
        if (endDate != null ? !endDate.equals(that.endDate) : that.endDate != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        return location != null ? location.equals(that.location) : that.location == null;

    }

    @Override
    public int hashCode() {
        int result = startDate != null ? startDate.hashCode() : 0;
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        return result;
    }

    private static int eventCount(CalendarEvent[] array) {
        int count = 0;

        for (CalendarEvent e : array) {
            if (e != null) {
                count++;
            }
        }
        return count;
    }
}
