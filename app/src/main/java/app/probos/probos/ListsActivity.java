package app.probos.probos;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.entity.MastodonList;
import com.sys1yagi.mastodon4j.api.method.MastodonLists;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class ListsActivity extends AppCompatActivity {

    // Get these three things before starting anything

    String instanceName;
    String accessToken;
    Long acctId;

    /*
    public void setInfo(String instanceName, String accessToken, Long acctId) {
        this.instanceName = instanceName;
        this.accessToken = accessToken;
        this.acctId = acctId;
    }
    */
    MastodonClient userClient;
    MastodonLists tmpLists;
    int currentListInd = 0;

    RecyclerView listRecycler;
    List<MastodonList> lists;

    private TimelineAdapter listAdapter;
///    int toGet = 0;
    Spinner s;
    ArrayAdapter<String> spinAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);
        Toolbar toolbar = findViewById(R.id.toolbar);
        s = (Spinner) findViewById(R.id.spinner);
        setSupportActionBar(toolbar);
        // This line here should enable a back button on the action bar
        // Once the UI is more developed, it will be useful to have
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent currIntent = getIntent();
        //acctId = currIntent.getLongExtra("id", 0);
        instanceName = currIntent.getStringExtra("instanceName");
        accessToken = currIntent.getStringExtra("accessToken");
        //toGet = currIntent.getIntExtra("toGet",0);

        // Begin instantiating the listRecycler
        listRecycler = findViewById(R.id.listitem_recycler);

        // Figure out how to set this up better
        //View rootView = getLayoutInflater().inflate

        userClient = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken).build();
        tmpLists = new MastodonLists(userClient);




        /*Thread followerRetrieval = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    // Gets the first 50 users from the list of followers acctId has
                    Range range = new Range(null,null,50);
                    Pageable<Account> users = tmpAcct.getFollowers(acctId, range).execute();
                    accounts = users.getPart();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        followerRetrieval.start();

        try {
            followerRetrieval.join();
        } catch (Exception e) {
            //do nothing
        }*/
        new setUpListOfLists().execute();

        //TimelineAdapter userListAdapter = new TimelineAdapter(accounts);
        listRecycler.setAdapter(listAdapter);
        listRecycler.setLayoutManager(new LinearLayoutManager(this));
        listRecycler.getLayoutManager().setMeasurementCacheEnabled(true);

        s.setAdapter(spinAdapter);
        //return rootView;

        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                currentListInd = position;
                new updateList().execute();
                int j = 0;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
    }

    private class updateList extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
          //      listAdapter = null;
                Range range = new Range(null, null, 50);
                Pageable<com.sys1yagi.mastodon4j.api.entity.Status> firstStatuses = tmpLists.getListTimeLine(lists.get(currentListInd).getId(), range).execute();
                listAdapter = new TimelineAdapter(firstStatuses.getPart(), accessToken, instanceName, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //listRecycler.setAdapter(null);
            listRecycler.setAdapter(listAdapter);
            int i = 0;
        }
    }

    private class setUpListOfLists extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... param) {
            Range range = new Range(null,null,50);
            try {

                // Gets the first 50 users from the list of followers acctId has

                //Pageable<MastodonList> listPageable;
                //if (toGet == 1) {
                //listPageable = tmpLists.getLists().execute();
                //} else {
                //    users = tmpAcct.getFollowing(acctId, range).execute();
                //}
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("title","Whatever")
                        .build();
                userClient.post("lists", requestBody);


                lists = tmpLists.getLists().execute().getPart();
                ArrayList<String> listNames = new ArrayList<>();
                for (int i = 0; i < lists.size(); i++) {
                    listNames.add(lists.get(i).getTitle());
                }
                spinAdapter = new ArrayAdapter<String>(ListsActivity.this,
                        android.R.layout.simple_spinner_item, listNames);


         //       Pageable<com.sys1yagi.mastodon4j.api.entity.Status> firstStatuses = tmpLists.getListTimeLine(lists.get(0).getId(), range).execute();
          //      listAdapter = new TimelineAdapter(firstStatuses.getPart(),accessToken,instanceName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(Void param) {
            s.setAdapter(spinAdapter);
        }

        //TimelineAdapter userListAdapter = new TimelineAdapter(accounts);
        //UserListAdapter userListAdapter = new UserListAdapter(accounts);
        //userRecycler.setAdapter(userListAdapter);
        //userRecycler.setLayoutManager(new LinearLayoutManager(this));
        //userRecycler.getLayoutManager().setMeasurementCacheEnabled(true);
        //return rootView;

    }

    // TODO Debug why nested user lists sometimes breaks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);

    }

}
