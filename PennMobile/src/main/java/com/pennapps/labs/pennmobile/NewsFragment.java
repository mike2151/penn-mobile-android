package com.pennapps.labs.pennmobile;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import butterknife.ButterKnife;

public class NewsFragment extends ListFragment {

    private ListView mListView;
    private CustomTabsIntent customTabsIntent;
    private Intent share;

    class NewsSite {
        String name, url;

        public NewsSite(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        mListView = getListView();
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        builder.addMenuItem("Share", PendingIntent.getActivity(getContext(), 0,
                share, PendingIntent.FLAG_CANCEL_CURRENT));
        builder.setToolbarColor(0x3E50B4);
        builder.setStartAnimations(getContext(),
                android.support.design.R.anim.abc_popup_enter,
                android.support.design.R.anim.abc_popup_exit);
        customTabsIntent = builder.build();
        addNews();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).closeKeyboard();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_news, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    private void addNews() {
        NewsSite dp = new NewsSite("The Daily Pennsylvanian", "http://www.thedp.com/");
        NewsSite thirtyFour = new NewsSite("34th Street", "http://www.34st.com/");
        NewsSite utb = new NewsSite("Under the Button",
                "http://www.thedp.com/blog/under-the-button/");
        NewsSite[] allSites = {dp, thirtyFour, utb};
        ArrayAdapter<NewsSite> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, allSites);

        mListView.setAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String url = ((NewsSite) l.getItemAtPosition(position)).getUrl();
        if (url != null) {
            share.putExtra(Intent.EXTRA_TEXT, url);
            customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.news);
        ((MainActivity) getActivity()).setNav(R.id.nav_news);
    }

    @Override
    public void onDestroyView() {
        ((MainActivity) getActivity()).removeTabs();
        super.onDestroyView();

    }
}
