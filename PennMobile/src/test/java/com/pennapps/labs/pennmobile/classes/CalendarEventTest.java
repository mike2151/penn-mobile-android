package com.pennapps.labs.pennmobile.classes;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Jason on 12/1/2016.
 */

public class CalendarEventTest {

    @Test
    public void test() throws Exception {
        CalendarEvent e1 = new CalendarEvent("0", "100", "e1", "loc");
        CalendarEvent e2 = new CalendarEvent("50", "200", "e2", "loc");
        CalendarEvent e3 = new CalendarEvent("100", "200", "e3", "loc");
        CalendarEvent[] es = {e1, e2, e3};
        CalendarEvent[][] grid = CalendarEvent.fillCalendarGrid(es);
        assertNotNull(grid);
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] != null) {
                    System.out.print(grid[i][j].title + "-" + grid[i][j].color + " ");
                } else {
                    System.out.print(" blank ");
                }
            }
            System.out.println();
        }
    }

    @Test
    public void test2() throws Exception {
        CalendarEvent e1 = new CalendarEvent("0", "100", "e1", "loc");
        CalendarEvent e3 = new CalendarEvent("100", "200", "e3", "loc");
        CalendarEvent[] es = {e1, e3};
        CalendarEvent[][] grid = CalendarEvent.fillCalendarGrid(es);
        assertNotNull(grid);
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] != null) {
                    System.out.print(grid[i][j].title + "-" + grid[i][j].color + " ");
                } else {
                    System.out.print(" blank ");
                }
            }
            System.out.println();
        }
    }

    @Test
    public void test3() throws Exception {
        CalendarEvent e1 = new CalendarEvent("0", "100", "e1", "loc");
        CalendarEvent e3 = new CalendarEvent("110", "200", "e3", "loc");
        CalendarEvent[] es = {e1, e3};
        CalendarEvent[][] grid = CalendarEvent.fillCalendarGrid(es);
        assertNotNull(grid);
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] != null) {
                    System.out.print(grid[i][j].title + "-" + grid[i][j].color + " ");
                } else {
                    System.out.print(" blank ");
                }
            }
            System.out.println();
        }
    }
}
