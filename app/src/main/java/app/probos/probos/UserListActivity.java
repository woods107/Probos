package app.probos.probos;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
//import android.view.LayoutInflater;
import android.view.View;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.MastodonRequest;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.method.Accounts;
import com.sys1yagi.mastodon4j.api.method.Timelines;

import java.util.List;

import okhttp3.OkHttpClient;

public class UserListActivity extends AppCompatActivity {

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

    RecyclerView userRecycler;
    List<Account> accounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // This line here should enable a back button on the action bar
        // Once the UI is more developed, it will be useful to have
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent currIntent = getIntent();
        acctId = currIntent.getLongExtra("id", 0);
        instanceName = currIntent.getStringExtra("name");
        accessToken = currIntent.getStringExtra("token");

        // Begin instantiating the userRecycler
        userRecycler = findViewById(R.id.user_recycler);

        // Figure out how to set this up better
        //View rootView = getLayoutInflater().inflate

        MastodonClient userClient = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken).build();
        Accounts tmpAcct = new Accounts(userClient);


        Thread followerRetrieval = new Thread(new Runnable() {
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
        }


        //TimelineAdapter userListAdapter = new TimelineAdapter(accounts);
        UserListAdapter userListAdapter = new UserListAdapter(accounts);
        userRecycler.setAdapter(userListAdapter);
        userRecycler.setLayoutManager(new LinearLayoutManager(this));
        userRecycler.getLayoutManager().setMeasurementCacheEnabled(true);
        //return rootView;

    }

}
