package com.pennapps.labs.pennmobile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pennapps.labs.pennmobile.adapters.DirectoryAdapter;
import com.pennapps.labs.pennmobile.classes.Person;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Jason on 2/1/2016.
 */
public class DirectoryTab extends SearchFavoriteTab {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_search_favorite_tab, container, false);
        ButterKnife.bind(this, v);
        mListView = (ListView) v.findViewById(android.R.id.list);
        if (fav) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
            Gson gson = new Gson();
            Set<String> starred = sp.getStringSet(getString(R.string.search_dir_star), new HashSet<String>());
            if (starred.isEmpty()) {
                Toast.makeText(mActivity, "No favorites found.", Toast.LENGTH_SHORT).show();
                notFavoriteInit();
            } else {
                if (loadingPanel.getVisibility() == View.VISIBLE) {
                    loadingPanel.setVisibility(View.GONE);
                }
                if (mListView.getVisibility() == View.GONE) {
                    mListView.setVisibility(View.VISIBLE);
                }
                if (no_results.getVisibility() == View.VISIBLE) {
                    no_results.setVisibility(View.GONE);
                }
                if (search_instructions.getVisibility() == View.VISIBLE) {
                    search_instructions.setVisibility(View.GONE);
                }
                List<Person> people = new LinkedList<>();
                for (String s : starred) {
                    String details = sp.getString(s + getString(R.string.search_dir_star), "");
                    if (!details.isEmpty()) {
                        Person person = gson.fromJson(details, Person.class);
                        people.add(person);
                    }
                }
                DirectoryAdapter adapter = new DirectoryAdapter(mActivity, people, this);
                mListView.setAdapter(adapter);
            }
            ((MainActivity) getActivity()).closeKeyboard();
        } else {
            notFavoriteInit();
        }
        return v;
    }

    @Override
    public void processQuery (String query) {
        super.processQuery(query);
        processDirectoryQuery(query);
    }

    private void processDirectoryQuery(String query) {
        mLabs.people(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Person>>() {
                    @Override
                    public void call(final List<Person> people) {
                        if (loadingPanel != null) {
                            loadingPanel.setVisibility(View.GONE);
                            if (people.isEmpty()) {
                                if (no_results != null) {
                                    no_results.setVisibility(View.VISIBLE);
                                }
                            } else {
                                if (mListView != null) {
                                    DirectoryAdapter mAdapter = new DirectoryAdapter(mActivity, people);
                                    mListView.setAdapter(mAdapter);
                                    mListView.setVisibility(View.VISIBLE);

                                }
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        noResults();
                    }
                });
    }
}
