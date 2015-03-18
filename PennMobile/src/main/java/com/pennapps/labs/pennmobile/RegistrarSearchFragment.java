package com.pennapps.labs.pennmobile;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.pennapps.labs.pennmobile.adapters.RegistrarAdapter;
import com.pennapps.labs.pennmobile.api.Labs;
import com.pennapps.labs.pennmobile.classes.Course;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class RegistrarSearchFragment extends Fragment {

    public static final String COURSE_ID_EXTRA = "COURSE_ID";
    private Labs mLabs;
    public static Fragment mFragment;
    private Activity mActivity;
    private boolean hideKeyboard;
    private RegistrarAdapter mAdapter;
    private SearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        hideKeyboard = false;
        mLabs = ((MainActivity) getActivity()).getLabsInstance();
        mFragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_registrar_search, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.registrar_search:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem searchMenuItem = menu.findItem(R.id.registrar_search);
        searchView = (SearchView) menu.findItem(R.id.registrar_search).getActionView();
        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);
        searchMenuItem.expandActionView();
        if (hideKeyboard) {
            searchView.clearFocus();
        } else {
            searchView.setQuery("", false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.registrar, menu);
        getActivity().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        MenuItem searchMenuItem = menu.findItem(R.id.registrar_search);

        searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        final SearchView.OnQueryTextListener queryListener = new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String arg0) {
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String input) {
                searchView.clearFocus();
                hideKeyboard = true;
                getActivity().findViewById(R.id.no_results).setVisibility(View.GONE);
                getActivity().findViewById(R.id.registrar_instructions).setVisibility(View.GONE);
                getActivity().findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                searchCourses(input);
                return true;
            }
        };
        searchView.setOnQueryTextListener(queryListener);
    }

    private void searchCourses(String query) {
        mLabs.courses(query)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<List<Course>>() {
            @Override
            public void call(List<Course> courses) {
                if (courses == null || courses.size() == 0) {
                    getActivity().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    getActivity().findViewById(R.id.no_results).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.registrar_fragment).setVisibility(View.GONE);
                } else {
                    getActivity().findViewById(R.id.registrar_fragment).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.no_results).setVisibility(View.GONE);
                    RegistrarListFragment listFragment = new RegistrarListFragment();
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.add(R.id.registrar_fragment, listFragment, "LIST")
                            .addToBackStack(null)
                            .commit();
                    mAdapter = new RegistrarAdapter(mActivity.getApplicationContext(),
                            R.layout.registrar_list_item, courses);
                    listFragment.setListAdapter(mAdapter);
                }
            }});
    }
}


