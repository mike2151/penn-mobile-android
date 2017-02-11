package com.pennapps.labs.pennmobile;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pennapps.labs.pennmobile.adapters.OnStartDragListener;
import com.pennapps.labs.pennmobile.adapters.RecyclerListAdapter;
import com.pennapps.labs.pennmobile.adapters.SimpleItemTouchHelperCallback;
import com.pennapps.labs.pennmobile.classes.CalendarEvent;
import com.pennapps.labs.pennmobile.classes.CustomViewHolder;


import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Set;


public class MainFragment extends Fragment implements OnStartDragListener{

    private static final int TIMEWIDTH = 200;
    private static final int EVENTHEIGHT = 180;

    private ItemTouchHelper mItemTouchHelper;
    private List<RecyclerView.ViewHolder> viewHolderList;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ((MainActivity) getActivity()).closeKeyboard();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewHolderList = new ArrayList<>();
//        createWeatherView(inflater, container);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR) ==
                PackageManager.PERMISSION_GRANTED) {
            createCalendarView(inflater, container);
        }
        return new RecyclerView(container.getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        RecyclerListAdapter adapter = new RecyclerListAdapter(viewHolderList, this);

        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.main_title);
        ((MainActivity) getActivity()).setNav(R.id.nav_home);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
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
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.h01d, options);
        iv.setImageBitmap(bitmap);
        params2.addRule(RelativeLayout.BELOW, tv.getId());
        params2.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        params2.topMargin = - size.y / 25;
        iv.setId(SearchFavoriteTab.generateViewId());
        layout.addView(iv);

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


        viewHolderList.add(new CustomViewHolder.WeatherViewHolder(layout));
    }

    @SuppressWarnings("ResourceType")
    public void createCalendarView(LayoutInflater inflater, ViewGroup container) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat
                    .requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, MainActivity.CODE_MAIN_CAL);
        }
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        RelativeLayout layout = (RelativeLayout) inflater
                .inflate(R.layout.main_custom, container, false);
        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI
                .buildUpon();
        DateTime today = new DateTime().withTimeAtStartOfDay();
        ContentUris.appendId(eventsUriBuilder, today.toDate().getTime());
        DateTime tmr = today.plusDays(1).withTimeAtStartOfDay();
        ContentUris.appendId(eventsUriBuilder, tmr.toDate().getTime());
        Uri eventsUri = eventsUriBuilder.build();
        Cursor cursor = null;
        cursor = getContext().getContentResolver().query(eventsUri, new String[] {
                CalendarContract.Events.CALENDAR_ID, CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND,
                CalendarContract.Events.EVENT_LOCATION, CalendarContract.Events.DURATION},
                null, null, CalendarContract.Instances.DTSTART + " ASC");
        //build Uri here with the fix time
//               Uri.Builder builder = CalendarContract.Events.CONTENT_URI.buildUpon();

//        ContentResolver resolver = getContext().getContentResolver();
//        Cursor cursor = resolver.query(CalendarContract.Events.CONTENT_URI, , null, null, null);
        if (cursor == null || cursor.getCount() == 0) {
            layout.setBackgroundColor(getResources().getColor(R.color.graywhite));
            ImageView iv = new ImageView(getContext());
            RelativeLayout.LayoutParams param = new RelativeLayout
                    .LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            iv.setLayoutParams(param);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 10;
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.beardedman, options);
            iv.setImageBitmap(bitmap);
            param.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            layout.addView(iv);
            return;
        }
        ArrayList<CalendarEvent> eventList = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            if (cursor.getString(2) != null && !cursor.getString(2).equals("")) { //if start time is not null{
                if (cursor.getString(3) != null && !cursor.getString(3).equals("")) {//end time not null
                    eventList.add(new CalendarEvent(cursor.getString(2), cursor.getString(3),
                            cursor.getString(1), cursor.getString(4)));
                } else if (cursor.getString(5) != null) {
                    eventList.add(new CalendarEvent(cursor.getString(2),
                            CalendarEvent.RFC2445ToMilliseconds(cursor.getString(5)),
                                    cursor.getString(1), cursor.getString(4)));
                }
            }
            Log.d("TAG", "event:" + cursor.getString(2));
        }
        cursor.close();

        CalendarEvent[] events = new CalendarEvent[eventList.size()];
        eventList.toArray(events);
        Long lastEndTime = today.getMillis();
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
        viewHolderList.add(new CustomViewHolder.CalendarHomeViewHolder(layout));
    }

}
