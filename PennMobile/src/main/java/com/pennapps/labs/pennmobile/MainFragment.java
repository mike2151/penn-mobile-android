package com.pennapps.labs.pennmobile;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.pennapps.labs.pennmobile.adapters.OnStartDragListener;
import com.pennapps.labs.pennmobile.adapters.RecyclerListAdapter;
import com.pennapps.labs.pennmobile.adapters.SimpleItemTouchHelperCallback;
import com.pennapps.labs.pennmobile.api.Labs;
import com.pennapps.labs.pennmobile.classes.CalendarEvent;
import com.pennapps.labs.pennmobile.classes.Course;
import com.pennapps.labs.pennmobile.classes.PennCalEvent;
import com.pennapps.labs.pennmobile.classes.StudySpace;
import com.pennapps.labs.pennmobile.classes.Weather;


import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;


public class MainFragment extends Fragment {


    private static final int TIMEWIDTH = 200;
    private static final int EVENTHEIGHT = 180;

    private Labs mLabs;
    private HashMap<String, Bitmap> bitmapMap;

    private HashMap<String, View> viewMap;
    // map keys
    private static final String WEATHER_IMAGE_KEY = "weather image";
    private static final String TEMPERATURE_KEY = "temperature";
    private static final String PENN_CAL_EVENT_KEY = "penn_cal_key";

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ((MainActivity) getActivity()).closeKeyboard();
        super.onCreate(savedInstanceState);
        viewMap = new HashMap<>();
        bitmapMap = new HashMap<>();
        mLabs = MainActivity.getLabsInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        LinearLayout ll = (LinearLayout) v.findViewById(R.id.main_ll);
        createWeatherView(inflater, ll);
        createCalendarView(inflater, ll);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.main_title);
        ((MainActivity) getActivity()).setNav(R.id.nav_home);
    }


    @SuppressWarnings("ResourceType")
    public void createWeatherView(LayoutInflater inflater, ViewGroup container) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        RelativeLayout layout = (RelativeLayout) inflater
                .inflate(R.layout.main_custom, container, false);

        TextView tv = new TextView(getContext());
        tv.setId(SearchFavoriteTab.generateViewId());
        tv.setText("You should bring an umbrella today");
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        RelativeLayout.LayoutParams params = new RelativeLayout
                .LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        tv.setGravity(Gravity.BOTTOM);
        params.height = size.y / 10;
        layout.addView(tv);
        tv.setLayoutParams(params);

        ImageView iv = new ImageView(getContext());
        RelativeLayout.LayoutParams params2 = new RelativeLayout
                .LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        iv.setLayoutParams(params2);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 10;
        options.inJustDecodeBounds = false;
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.h01d, options);
        iv.setImageBitmap(bitmap);
        params2.addRule(RelativeLayout.BELOW, tv.getId());
        params2.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        params2.topMargin = - size.y / 25;
        iv.setId(SearchFavoriteTab.generateViewId());
        layout.addView(iv);
        viewMap.put(WEATHER_IMAGE_KEY, iv);

        RelativeLayout rl = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams params3 = new RelativeLayout
                .LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params3.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        params3.addRule(RelativeLayout.BELOW, iv.getId());
        rl.setLayoutParams(params3);
        rl.setId(SearchFavoriteTab.generateViewId());
        params3.height = size.y/6;
        params3.width = size.y/2;
        params3.topMargin = - size.y / 17;
        layout.addView(rl);

        View v = new View(getContext());
        RelativeLayout.LayoutParams params4 = new RelativeLayout.LayoutParams(15, 0);
        params4.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        v.setLayoutParams(params4);
        v.setId(SearchFavoriteTab.generateViewId());
        rl.addView(v);

        TextView tv2 = new TextView(getContext());
        RelativeLayout.LayoutParams params5 = new RelativeLayout
                .LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params5.addRule(RelativeLayout.ABOVE, v.getId());
        params5.addRule(RelativeLayout.LEFT_OF, v.getId());
        tv2.setLayoutParams(params5);
        tv2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        tv2.setText("Today is");
        rl.addView(tv2);

        TextView tv3 = new TextView(getContext());
        RelativeLayout.LayoutParams params6 = new RelativeLayout
                .LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params6.addRule(RelativeLayout.BELOW, v.getId());
        params6.addRule(RelativeLayout.LEFT_OF, v.getId());
        tv3.setLayoutParams(params6);
        tv3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 26);
        tv3.setText("Rainy");
        rl.addView(tv3);

        TextView tv4 = new TextView(getContext());
        RelativeLayout.LayoutParams params7 = new RelativeLayout
                .LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params7.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        params7.addRule(RelativeLayout.RIGHT_OF, v.getId());
        tv4.setLayoutParams(params7);
        tv4.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 60);
        tv4.setText("50\u00b0");
        rl.addView(tv4);

        viewMap.put(TEMPERATURE_KEY, tv4);

        mLabs.weather().subscribe(new Action1<Weather>() {
                                      @Override
                                      public void call(final Weather weather) {
                                          TextView tv = (TextView) viewMap.get(TEMPERATURE_KEY);
                                          int round = (int) weather.main.temp;
                                          if (weather.main.temp - 0.5 >= round) {
                                              round++;
                                          }
                                          tv.setText(round + "\u00b0");
                                          String mapId = weather.weather.get(0).icon;
                                          ImageView iv = (ImageView) viewMap.get(WEATHER_IMAGE_KEY);
                                          if (bitmapMap.containsKey(weather.weather.get(0).icon)) {
                                              iv.setImageBitmap(bitmapMap.get(mapId));
                                          } else {
                                              Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                                                      Weather.getIconId(mapId));
                                              bitmapMap.put(mapId, bitmap);
                                          }
                                      }
                                  }, new Action1<Throwable>() {
                                      @Override
                                      public void call(Throwable throwable) {
                                          Toast.makeText(getContext(), "Can't get weather", Toast.LENGTH_LONG)
                                                  .show();
                                      }
                                  });
        container.addView(layout);
    }

    @SuppressWarnings("ResourceType")
    public void createCalendarView(LayoutInflater inflater, ViewGroup container) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        RelativeLayout layout = (RelativeLayout) inflater
                .inflate(R.layout.main_custom, container, false);

        TextView pennCalView = new TextView(getContext());
        int penncalid = SearchFavoriteTab.generateViewId();
        pennCalView.setId(penncalid);
        //TODO: ask about colors and margins
        pennCalView.setTextColor(Color.WHITE);
        pennCalView.setBackgroundColor(Color.BLUE);
        pennCalView.setVisibility(View.INVISIBLE);
        pennCalView.setGravity(Gravity.CENTER);
        viewMap.put(PENN_CAL_EVENT_KEY, pennCalView);
        mLabs.pennCalEvents().subscribe(new Action1<List<PennCalEvent>>() {

            @Override
            public void call(List<PennCalEvent> events) {
                //Todo: ask tiff what to do about more than 1 at the same time
                final TextView tv = (TextView) viewMap.get(PENN_CAL_EVENT_KEY);
                String string = "";
                for (PennCalEvent event : events) {
                    if (!string.isEmpty()) {
                        string += "\n";
                    }
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar theDay = Calendar.getInstance();
                    theDay.set(Calendar.HOUR_OF_DAY, 0);
                    theDay.set(Calendar.MINUTE, 0);
                    theDay.set(Calendar.SECOND, 0);
                    try {
                        theDay.setTime(format.parse(event.start));
                        if (theDay.before(c)) {
                            theDay.setTime(format.parse(event.end));
                            theDay.add(Calendar.DATE, -1);
                            if (theDay.before(c)) { //that means it's today
                                string += event.name + " ends today";
                            } else {
                                long diff = theDay.getTimeInMillis() - c.getTimeInMillis();
                                long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1;
                                string += event.name + " ends in " + days
                                         + (days > 1 ? " days" : " day");
                            }
                        } else {
                            long diff = theDay.getTimeInMillis() - c.getTimeInMillis();
                            long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1;
                            string += event.name + " begins in " + days
                                     + (days > 1 ? " days" : " day");
                        }
                    } catch (ParseException e) {
                        Log.d("Main Fragment", "error parsing", e);
                    }
                }
                final String finalString = string;
                if (!string.isEmpty()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv.setText(finalString);
                            tv.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.d("Main Fragment", "failed to get the penn calendar", throwable);
            }
        });

        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        pennCalView.setLayoutParams(param);
        layout.addView(pennCalView);

        // TODO: change the thing we will display to events
        ArrayList<Course> calendarStuff = new ArrayList<>();
        if (calendarStuff.isEmpty()) {
            layout.setBackgroundColor(getResources().getColor(R.color.graywhite));
            ImageView iv = new ImageView(getContext());
            param = new RelativeLayout
                    .LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            iv.setLayoutParams(param);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 10;
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.beardedman, options);
            iv.setImageBitmap(bitmap);
            param.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            param.addRule(RelativeLayout.BELOW, penncalid);
            layout.addView(iv);
            container.addView(layout);
            return;
        }

        // Todo: add the row for "schedule looks like" (ask tiff about when should we place this)



        ArrayList<CalendarEvent> eventList = new ArrayList<>(calendarStuff.size());
        for (Course course : calendarStuff) {
            eventList.add(new CalendarEvent(course.getMeetingStartTimeInMilli(), course.getMeetingEndTimeInMilli(),
                    course.course_title, course.getMeetingLocation()));
        }

        CalendarEvent[] events = new CalendarEvent[eventList.size()];
        eventList.toArray(events);
        Long lastEndTime = (new DateTime()).withTimeAtStartOfDay().getMillis();
        for (CalendarEvent event : events) {
            if (event.endDate > lastEndTime) {
                lastEndTime = event.endDate;
            }
        }
        GridLayout gridLayout = new GridLayout(getContext());
        CalendarEvent[][] grid = CalendarEvent.fillCalendarGrid(events);
        // need to collapse and add rows...
        PriorityQueue<Long> specialTime = new PriorityQueue<>();
        for (CalendarEvent event : events) {
            specialTime.add(event.startDate);
            specialTime.add(event.endDate);
        }
        gridLayout.setColumnCount(grid[0].length + 1); //need one more column for the time...
        gridLayout.setRowCount(specialTime.size());
        Set<CalendarEvent> eventsToPut = new HashSet<>();
        int offset = 0; //the number of times we have an extra row
        int rowCount = specialTime.size(); //copy it because now specialTime will be changed inside the loop
        for (int i = 0; i < rowCount; i++) {
            long currentTime = specialTime.poll();
            TextView tv = new TextView(getContext());
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(currentTime);
            StringBuilder builder = new StringBuilder();
            builder.append(c.get(Calendar.HOUR) == 0 ? 12 : c.get(Calendar.HOUR));
            builder.append(":").append(String.format(Locale.getDefault(),
                    "%02d", c.get(Calendar.MINUTE)));
            builder.append(c.get(Calendar.AM_PM) == 1 ? "PM" : "AM");
            tv.setText(builder.toString());

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = TIMEWIDTH;
            tv.setLayoutParams(params);
            gridLayout.addView(tv);

            //want to check if this row is extra
            boolean hasStuff = false;

            for (int j = 0; j < grid[0].length; j++) {
                //insert if haven't seen before
                if (i-offset < grid.length && grid[i-offset][j] != null &&
                        !eventsToPut.contains(grid[i-offset][j])
                        && grid[i-offset][j].startDate == currentTime) {
                    eventsToPut.add(grid[i-offset][j]);
                    tv = new TextView(getContext());
                    CalendarEvent current = grid[i-offset][j];
                    builder = new StringBuilder();
                    builder.append(current.title).append("\n");
                    builder.append(current.location);
                    tv.setText(builder.toString());
                    tv.setBackgroundColor(current.color);
                    params = new GridLayout.LayoutParams();

                    params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, current.colSpan);
                    params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, current.totalGrids / current.colSpan);
                    params.width = (int) (size.x * 0.9) / (current.colSpan)- TIMEWIDTH;
                    params.height = EVENTHEIGHT;
                    tv.setPadding(50, 10, 10, 10);
                    tv.setMaxLines(2);
                    tv.setLayoutParams(params);
                    gridLayout.addView(tv);
                    hasStuff = true;
                } else if (!(i-offset < grid.length && eventsToPut.contains(grid[i-offset][j]))) {
                    //put an empty view unless this tile should've been something else that has been
                    //put in already
                    //insert empty
                    View v = new View(getContext());
                    params = new GridLayout.LayoutParams();
                    params.height = 0;
                    params.width = 0;
                    v.setLayoutParams(params);
                    gridLayout.addView(v);
                }
            }
            if (!hasStuff) {
                offset++;
            }
        }
        RelativeLayout.LayoutParams gridParam = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        gridParam.leftMargin = size.x / 20;
        gridParam.rightMargin = size.x / 20;
        gridLayout.setLayoutParams(gridParam);
        layout.addView(gridLayout);
        layout.setPadding(0,16,16,0);

        ImageButton imageButton = new ImageButton(getContext());
        Bitmap arrow = BitmapFactory.decodeResource(getResources(), R.drawable.ic_chevron_right_black_36dp);
        Bitmap buffer = arrow;
        arrow = Bitmap.createScaledBitmap(buffer, (int) (buffer.getWidth()/1.8), (int) (buffer.getHeight()/1.8), false);
        buffer.recycle();
        imageButton.setImageBitmap(arrow);
        imageButton.setBackground(null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT
                );
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.BELOW, penncalid);
        imageButton.setLayoutParams(params);
        layout.addView(imageButton);
        container.addView(layout);
    }

    @SuppressWarnings("ResourceType")
    public void createStudySpacesView(LayoutInflater inflater, ViewGroup container) {
        final RelativeLayout layout = (RelativeLayout) inflater
                .inflate(R.layout.main_custom, container, false);

        mLabs.studySpaceId().subscribe(new Action1<List<StudySpace>>() {
            @Override
            public void call(final List<StudySpace> studySpaces) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                Set<String> savedSpaces = sp.getStringSet(getString(R.string.saved_study_spaces_key), null);
                if (savedSpaces == null) { //first time launching this
                    //saving the default two locations in
                    SharedPreferences.Editor ed = sp.edit();
                    savedSpaces = new HashSet<>();
                    savedSpaces.add(getString(R.string.vanpelt_default));
                    savedSpaces.add(getString(R.string.education_commons_default));
                    ed.putStringSet(getString(R.string.saved_study_spaces_key), savedSpaces);
                    ed.apply();
                }
                Calendar c = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                for (final StudySpace ss : studySpaces) {
                    if (savedSpaces.contains(ss.name)) {
                        mLabs.studySpaceTimeSlot(format.format(c.getTime()), ss.id)
                                .subscribe(new Action1<List<StudySpace.TimeSlot>>() {
                                    @Override
                                    public void call(List<StudySpace.TimeSlot> timeSlots) {
                                        populateTimeSlots(ss, timeSlots, layout);
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        Log.d("Main fragment", "study spaces time slot call failed", throwable);
                                    }
                                });
                    }
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.d("Main fragment", "study spaces id call failed", throwable);
            }
        });
        container.addView(layout);
    }

    /**
     * Called by the retrofit after getting the time slots, to be filtered and populate
     * @param studySpace study spaces
     * @param timeSlots the timeslots used to populate, unfiltered
     * @param layout the layout to populate
     */
    private void populateTimeSlots(final StudySpace studySpace, List<StudySpace.TimeSlot> timeSlots, RelativeLayout layout) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        //TODO: finish up textview and the button
        //add in the textview for the building
        TextView buildingTitle = new TextView(getContext());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(size.x/18, size.y/25, 0, 0);
        buildingTitle.setLayoutParams(params);

        List<StudySpace.TimeSlot> filteredSlots = filteredSlots(timeSlots);
        for (StudySpace.TimeSlot timeslot : filteredSlots) {
            Button reserve = new Button(getContext());
            reserve.setBackgroundColor(getResources().getColor(R.color.home_pink));
            reserve.setText(R.string.reserve);
            reserve.setGravity(Gravity.CENTER);
            reserve.setWidth(size.x/3);
            reserve.setHeight(size.y/10);
            int reserveId = SearchFavoriteTab.generateViewId();
            reserve.setId(reserveId);
            reserve.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: link it to somewhere
                }
            });
            layout.addView(reserve);
        }

    }

    /**
     * Called by populateTimeSlots to filter the time slots
     * @param timeSlots original timeslots
     * @return timeslots used to populate the layout
     */
    private List<StudySpace.TimeSlot> filteredSlots(List<StudySpace.TimeSlot> timeSlots) {
        //TODO: figure out which timeslots to show
        return timeSlots.subList(0,3); //at the moment hard code first 3
    }

}
