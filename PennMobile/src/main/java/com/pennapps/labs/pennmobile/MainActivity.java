package com.pennapps.labs.pennmobile;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pennapps.labs.pennmobile.api.Labs;
import com.pennapps.labs.pennmobile.api.Serializer;
import com.pennapps.labs.pennmobile.classes.Building;
import com.pennapps.labs.pennmobile.classes.BusRoute;
import com.pennapps.labs.pennmobile.classes.BusStop;
import com.pennapps.labs.pennmobile.classes.Course;
import com.pennapps.labs.pennmobile.classes.DiningHall;
import com.pennapps.labs.pennmobile.classes.LaundryRoom;
import com.pennapps.labs.pennmobile.classes.LaundryMachine;
import com.pennapps.labs.pennmobile.classes.PennCalEvent;
import com.pennapps.labs.pennmobile.classes.Person;
import com.pennapps.labs.pennmobile.classes.Venue;
import com.pennapps.labs.pennmobile.classes.Weather;

import java.util.List;

import io.fabric.sdk.android.Fabric;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private NavigationView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private static Labs mLabs;
    private boolean from_alarm;
    public static final int CODE_MAP = 1, CODE_TRANSIT = 2, CODE_MAIN_CAL = 3;
    private boolean tab_showed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        from_alarm = getIntent().getBooleanExtra(getString(R.string.laundry_notification_alarm_intent), false);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerList = (NavigationView) findViewById(R.id.navigation);
        mDrawerList.setNavigationItemSelectedListener(new DrawerItemClickListener());
        mDrawerList.getMenu().findItem(R.id.nav_home).setChecked(true);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Set default fragment to MainFragment
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.content_frame, new MainFragment());
        tx.commit();
    }

    @Override
    public void onBackPressed() {
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.syncState();
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
            return;
        }

        try {
            WebView webView = NewsTab.currentWebView;
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                super.onBackPressed();
            }
        } catch (NullPointerException ignored) {
            // No webview exists currently
            super.onBackPressed();
            if (CourseFragment.containsNum(getTitle())) {
                mDrawerToggle.setDrawerIndicatorEnabled(false);
                mDrawerToggle.syncState();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.main_title);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();

        if (from_alarm) {
            navigateLayout(R.id.nav_laundry);
        }
    }

    public void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            closeKeyboard();
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            removeTabs();
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            mDrawerToggle.syncState();
            int id = item.getItemId();
            item.setChecked(true);
            navigateLayout(id);
            return false;
        }
    }

    private void navigateLayout(@AnyRes int id) {
        Fragment fragment = null;
        switch (id) {
            case R.id.nav_home:
                fragment = new MainFragment();
                break;
            case R.id.nav_registrar:
                fragment = new RegistrarFragment();
                break;
            case R.id.nav_directory:
                fragment = new DirectoryFragment();
                break;
            case R.id.nav_dining:
                fragment = new DiningFragment();
                break;
            case R.id.nav_transit:
                getLocPermission(CODE_TRANSIT);
                return;
            case R.id.nav_news:
                fragment = new NewsFragment();
                break;
            case R.id.nav_map:
                getLocPermission(CODE_MAP);
                return;
            case R.id.nav_laundry:
                fragment = new LaundryFragment();
                if (from_alarm) {
                    from_alarm = false;
                    Bundle arg = new Bundle();
                    arg.putInt(getString(R.string.laundry_hall_no), getIntent().getIntExtra(getString(R.string.laundry_hall_no), -1));
                    fragment.setArguments(arg);
                }
                break;
            case R.id.nav_nso:
                fragment = new NsoFragment();
                break;
            case R.id.nav_support:
                fragment = new SupportFragment();
                break;
            case R.id.nav_about:
                fragment = new AboutFragment();
                break;
            case R.id.nav_pref:
                fragment = new PreferenceFragment();
                break;
        }

        fragmentTransact(fragment);
    }

    public void onHomeButtonClick(View v) {
        navigateLayout(v.getId());
    }

    public static Labs getLabsInstance() {
        if (mLabs == null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(new TypeToken<List<Course>>(){}.getType(), new Serializer.CourseSerializer());
            gsonBuilder.registerTypeAdapter(new TypeToken<List<Building>>(){}.getType(), new Serializer.BuildingSerializer());
            gsonBuilder.registerTypeAdapter(new TypeToken<List<Person>>(){}.getType(), new Serializer.DataSerializer());
            gsonBuilder.registerTypeAdapter(new TypeToken<List<Venue>>(){}.getType(), new Serializer.VenueSerializer());
            gsonBuilder.registerTypeAdapter(new TypeToken<List<BusStop>>(){}.getType(), new Serializer.BusStopSerializer());
            gsonBuilder.registerTypeAdapter(DiningHall.class, new Serializer.MenuSerializer());
            gsonBuilder.registerTypeAdapter(BusRoute.class, new Serializer.BusRouteSerializer());
            gsonBuilder.registerTypeAdapter(new TypeToken<List<BusRoute>>(){}.getType(), new Serializer.BusRouteListSerializer());
            gsonBuilder.registerTypeAdapter(new TypeToken<List<LaundryRoom>>(){}.getType(), new Serializer.LaundryListSerializer());
            gsonBuilder.registerTypeAdapter(new TypeToken<List<LaundryMachine>>(){}.getType(), new Serializer.LaundryMachineSerializer());
            gsonBuilder.registerTypeAdapter(new TypeToken<Weather>(){}.getType(), new Serializer.WeatherSerializer());
            gsonBuilder.registerTypeAdapter(new TypeToken<List<PennCalEvent>>(){}.getType(), new Serializer.PennCalEventSerializer());
            Gson gson = gsonBuilder.create();
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setConverter(new GsonConverter(gson))
                    .setEndpoint("https://api.pennlabs.org")
                    .build();
            mLabs = restAdapter.create(Labs.class);
        }
        return mLabs;
    }

    public void showErrorToast(final int errorMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public ActionBarDrawerToggle getActionBarToggle() {
        return mDrawerToggle;
    }

    public void setNav(int id){
        final Menu menu = mDrawerList.getMenu();
        menu.findItem(id).setChecked(true);
    }

    public void addTabs(FragmentStatePagerAdapter pageAdapter, final ViewPager pager, boolean scrollable) {
        if (tab_showed) {
            return;
        }
        final AppBarLayout appBar = (AppBarLayout) findViewById(R.id.appbar);

        final TabLayout tabLayout = (TabLayout) getLayoutInflater().inflate(R.layout.tab_layout, null);
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(pager);
            }
        });
        if (!scrollable) {
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
        }
        appBar.addView(tabLayout);

        pager.setAdapter(pageAdapter);
        tab_showed = true;
    }



    public void removeTabs() {
        tab_showed = false;
        final AppBarLayout appBar = (AppBarLayout) findViewById(R.id.appbar);
        if (appBar != null && appBar.getChildCount() >= 2) {
            appBar.removeViewAt(1);
        }
    }

    private void getLocPermission(int code) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            ActivityCompat
                    .requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, code);
        } else {
            Fragment fragment = null;
            switch(code) {
                case CODE_MAP:
                    fragment = new MapFragment();
                    break;
                case CODE_TRANSIT:
                    fragment = new TransitFragment();
                    break;
            }
            fragmentTransact(fragment);
        }
    }

    private void fragmentTransact(Fragment fragment) {
        if (fragment != null) {
            final Fragment frag = fragment;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.content_frame, frag)
                                .addToBackStack(null)
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .commit();
                    } catch (IllegalStateException e) {
                        //ignore because the onSaveInstanceState etc states are called when activity is going to background etc
                    }
                }
            });
        }
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == CODE_MAP || requestCode == CODE_TRANSIT) {
            if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Access of your location is required to use this feature.", Toast.LENGTH_LONG).show();
                return;
            }
            final int code = requestCode;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Fragment fragment = null;
                    switch (code) {
                        case CODE_MAP:
                            fragment = new MapFragment();
                            break;
                        case CODE_TRANSIT:
                            fragment = new TransitFragment();
                            break;
                    }
                    if (fragment != null) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.content_frame, fragment)
                                .addToBackStack(null)
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .commitAllowingStateLoss();
                    }
                    mDrawerLayout.closeDrawer(mDrawerList);
                }
            });
        }
    }
}
