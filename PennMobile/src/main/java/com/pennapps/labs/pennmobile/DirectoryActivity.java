package com.pennapps.labs.pennmobile;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DirectoryActivity extends ListActivity {

    private DirectoryAPI mAPI;
    private ListView mListView;
    private DirectoryAdapter mAdapter;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory);

        mContext = getApplicationContext();
        mListView = getListView();

        Intent intent = getIntent();
        mAPI = new DirectoryAPI();
        mAPI.setUrlPath("directory?");
        String mFirstName = intent.getStringExtra(DirectorySearchActivity.FIRST_NAME_INTENT_EXTRA);
        String mLastName = intent.getStringExtra(DirectorySearchActivity.LAST_NAME_INTENT_EXTRA);
        // Log.v("vivlabs", "in directory, " + mFirstName + " " + mLastName);
        new GetRequestTask(mFirstName, mLastName).execute();
    }

    private class GetRequestTask extends AsyncTask<Void, Void, Boolean> {
        private String urlParameter;
        private JSONArray responseArr;

        GetRequestTask(String firstName, String lastName) {
            urlParameter = "first_name=" + firstName + "&last_name=" + lastName;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                JSONObject resultObj = mAPI.getCourse(urlParameter);
                responseArr = (JSONArray) resultObj.get("result_data");
                if (responseArr.length() == 0) {
                    return false;
                }
                // Log.v("vivlabs", "LOL " + responseArr.toString());
                return true;
            } catch(JSONException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean valid) {
            if (!valid) {
                // TODO:
                return;
            }
            try {
                ArrayList<Person> personArr = new ArrayList<Person>();
                JSONObject resp;

                for (int i = 0; i < responseArr.length(); i++) {
                    resp = (JSONObject) responseArr.get(i);
                    Log.v("vivlabs", resp.toString());

                    Person person = new Person.Builder(resp.get("list_name").toString(),
                            resp.get("list_affiliation").toString()).
                            phone(resp.get("list_phone").toString()).
                            email(resp.get("list_email").toString()).
                            build();
                    personArr.add(person);

                }

                /*
                String displayText = person.getName() + "\n" +
                                     person.getAffiliation() + "\n" +
                                     person.getEmail() + "\n" +
                                     person.getPhone();

                mTextView.setText(displayText);
                */

                for (int i = 0; i < personArr.size(); i++) {
                    Log.v("vivlabs", "in array " + i + " " + personArr.get(i).getName());
                }

                mAdapter = new DirectoryAdapter(mContext, personArr);
                mListView.setAdapter(mAdapter);
            } catch (JSONException e) {
                // Log.v("vivlabs", e.toString());
            }
        }
    }

    class DirectoryAdapter extends ArrayAdapter<Person> {

        public DirectoryAdapter(Context context, ArrayList<Person> persons) {
            super(context, R.layout.directory_list_item, persons);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Person person = getItem(position);
            View view = convertView;

            if (view == null) {
                view = LayoutInflater.from(getContext())
                        .inflate(R.layout.directory_list_item, null);
            }

            TextView tvName = (TextView) view.findViewById(R.id.tv_person_name);
            TextView tvAffiliation = (TextView) view.findViewById(R.id.tv_person_affiliation);
            TextView tvEmail = (TextView) view.findViewById(R.id.tv_person_email);
            TextView tvPhone = (TextView) view.findViewById(R.id.tv_person_phone);

            tvName.setText(person.getName());
            tvAffiliation.setText(person.getAffiliation());
            tvEmail.setText(person.getEmail());
            tvPhone.setText(person.getPhone());

            return view;
        }

    }

}