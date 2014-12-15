package com.pennapps.labs.pennmobile;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pennapps.labs.pennmobile.api.Labs;
import com.pennapps.labs.pennmobile.api.RegistrarAPI;
import com.pennapps.labs.pennmobile.classes.Course;
import com.pennapps.labs.pennmobile.pcr.RegCourse;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class RegistrarFragment extends Fragment {

    private RegistrarAPI mAPI;
    private Labs mLabs;
    private GoogleMap map;
    private SupportMapFragment mapFragment;

    @InjectView(R.id.course_code) TextView courseCodeTextView;
    @InjectView(R.id.course_title) TextView courseTitleTextView;
    @InjectView(R.id.instructor) TextView instructorTextView;
    @InjectView(R.id.course_desc_title) TextView descriptionTitle;
    @InjectView(R.id.course_desc) TextView descriptionTextView;
    @InjectView(R.id.registrar_map_frame) View mapFrame;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAPI = new RegistrarAPI();
        mLabs = ((MainActivity) getActivity()).getLabsInstance();
        new GetRequestTask(getArguments().getString(RegistrarSearchFragment.COURSE_ID_EXTRA)).execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_registrar, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().add(R.id.registrar_map_container, mapFragment).commit();
            fm.executePendingTransactions();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map == null) {
            map = mapFragment.getMap();
            if (map != null) {
                map.addMarker(new MarkerOptions().position(new LatLng(39.95198, -75.19368)));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(39.95198, -75.19368), 17));
                map.getUiSettings().setZoomControlsEnabled(false);
            }
        }
    }
    public LatLng getBuildingLatLng(RegCourse course) {
        Geocoder geocoder = new Geocoder(getActivity().getApplicationContext());
        try {
            List<Address> locationList = geocoder.getFromLocationName(course.getBuildingName(), 1);
            try {
                return new LatLng(locationList.get(0).getLatitude(), locationList.get(0).getLongitude());
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class GetRequestTask extends AsyncTask<Void, Void, Boolean> {
        private String input;
        private List<Course> courses;

        GetRequestTask(String s) {
            input = s;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                courses = mLabs.courses(input);
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean valid) {
            if (!valid) {
                courseCodeTextView.setText(input);
                courseTitleTextView.setText(input + " is not currently offered.");
                return;
            }
            try {
                Course course = courses.get(0);

//                LatLng courseLatLng = getBuildingLatLng(course);

                String courseCodeText = course.course_department + " " + course.course_number;
                courseCodeTextView.setText(courseCodeText);

//                String locationText;
//                if (course.getBuildingName().equals("")) {
//                    locationText = courseCodeText;
//                } else {
//                    locationText = courseCodeText + " - " + course.getBuildingCode() + " " + course.getRoomNumber();
//                }

//                if (map != null && courseLatLng != null) {
//                    mapFrame.setVisibility(View.VISIBLE);
//                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(courseLatLng, 17));
//                    map.addMarker(new MarkerOptions()
//                        .position(courseLatLng)
//                        .title(locationText));
//                }

                String courseTitleText = course.course_title;
                courseTitleTextView.setText(courseTitleText);

                String instructorsText = course.instructors.get(0).name;
                instructorTextView.setText(instructorsText);

                String courseDescription = course.course_description;
                if (courseDescription.equals("")) {
                    descriptionTitle.setVisibility(View.GONE);
                    descriptionTextView.setVisibility(View.GONE);
                } else {
                    descriptionTitle.setVisibility(View.VISIBLE);
                    descriptionTextView.setVisibility(View.VISIBLE);
                    descriptionTextView.setText(courseDescription);
                }
            } catch (NullPointerException ignored) {

            }
        }
    }

}
