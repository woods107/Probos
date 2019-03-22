package app.probos.probos;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.method.Accounts;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.method.Statuses;
import com.sys1yagi.mastodon4j.api.entity.Relationship;
import com.sys1yagi.mastodon4j.api.method.Follows;
import com.sys1yagi.mastodon4j.api.method.FollowRequests;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;

//import android.view.LayoutInflater;

public class ProfileActivity extends AppCompatActivity {

    // Get these three things before starting anything

    String instanceName;
    String accessToken;
    Long acctId;

    Accounts tmpAcct;
    Account currAcct;
    Bitmap ppBitmap;
    Bitmap bannerBitmap;
    Relationship relationship;
    Boolean follow;

    /*
    public void setInfo(String instanceName, String accessToken, Long acctId) {
        this.instanceName = instanceName;
        this.accessToken = accessToken;
        this.acctId = acctId;
    }
    */

    //RecyclerView userRecycler;
    //List<Account> accounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
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
        // userRecycler = findViewById(R.id.user_recycler);

        // Figure out how to set this up better
        //View rootView = getLayoutInflater().inflate

        MastodonClient userClient = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken).build();
        tmpAcct = new Accounts(userClient);

       /* infoRetrieval = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    currAcct = tmpAcct.getAccount(acctId).execute();
                    URL newPP = new URL(currAcct.getAvatar());
                    URL newBanner = new URL(currAcct.getHeader());
                    ppBitmap = BitmapFactory.decodeStream(newPP.openConnection().getInputStream());
                    bannerBitmap = BitmapFactory.decodeStream(newBanner.openConnection().getInputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        infoRetrieval.start();*/
        new setUpViews().execute();

    }

    private class setUpViews extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... param) {
            try {
                currAcct = tmpAcct.getAccount(acctId).execute();
                URL newPP = new URL(currAcct.getAvatar());
                URL newBanner = new URL(currAcct.getHeader());
                ppBitmap = BitmapFactory.decodeStream(newPP.openConnection().getInputStream());
                bannerBitmap = BitmapFactory.decodeStream(newBanner.openConnection().getInputStream());
                ArrayList<Long> accounts = new ArrayList<>();
                accounts.add(acctId);
                relationship = tmpAcct.getRelationships(accounts).execute().get(0);
                follow = relationship.isFollowing();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void param) {
            TextView displayName = findViewById(R.id.displayName);
            String displayNameText = currAcct.getDisplayName();
            if (!displayNameText.equals("")) {
                displayName.setText(currAcct.getDisplayName());
            } else {
                displayName.setText(currAcct.getUserName());
            }

            TextView userName = findViewById(R.id.fullUserName);
            userName.setText("@" + currAcct.getAcct());

            TextView followersCount = findViewById(R.id.followers_count);
            followersCount.setText(String.valueOf(currAcct.getFollowersCount()));
            followersCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), UserListActivity.class);
                    intent.putExtra("id", acctId);
                    intent.putExtra("token", TimelineActivity.accessTokenStr);
                    intent.putExtra("name", TimelineActivity.instanceName);
                    intent.putExtra("toGet", 1);
                    // Need to add a Context/ContextWrapper startActivity statement here
                    try {
                        v.getContext().startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }// End try/catch block
                }
            });

            TextView followingCount = findViewById(R.id.following_count);
            followingCount.setText(String.valueOf(currAcct.getFollowingCount()));
            followingCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), UserListActivity.class);
                    intent.putExtra("id", acctId);
                    intent.putExtra("token", TimelineActivity.accessTokenStr);
                    intent.putExtra("name", TimelineActivity.instanceName);
                    intent.putExtra("toGet", 2);
                    // Need to add a Context/ContextWrapper startActivity statement here
                    try {
                        v.getContext().startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }// End try/catch block
                }
            });

            TextView profileBio = findViewById(R.id.profileBio);
            profileBio.setText(Html.fromHtml(currAcct.getNote(), Html.FROM_HTML_MODE_COMPACT));

            ImageView profBanner = findViewById(R.id.profile_banner);
            profBanner.setImageBitmap(bannerBitmap);

            CircleImageView imageView = findViewById(R.id.profile_full_picture);
            imageView.setImageBitmap(ppBitmap);
            imageView.bringToFront();
            imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                    intent.putExtra("id", acctId);
                    intent.putExtra("token", TimelineActivity.accessTokenStr);
                    intent.putExtra("name", TimelineActivity.instanceName);
                    // Need to add a Context/ContextWrapper startActivity statement here
                    try {
                        v.getContext().startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }// End try/catch block
                }
            });
            ImageButton followButton= findViewById(R.id.followButton);

            if(follow) {
                followButton.setImageResource(android.R.drawable.checkbox_on_background);//what da image
            }else{
                followButton.setImageResource(android.R.drawable.ic_input_add);
            }

            followButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Thread boostPoss = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if(follow){
                                    tmpAcct.postUnFollow(currAcct.getId()).execute();
                                    followButton.setImageResource(android.R.drawable.ic_input_add);
                                    follow = false;
                                    //add turning button on/off
                                }else{
                                    tmpAcct.postFollow(currAcct.getId()).execute();
                                    followButton.setImageResource(android.R.drawable.checkbox_on_background);
                                    follow = true;
                                }
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    boostPoss.start();
                    try{
                        boostPoss.join();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }


        //TimelineAdapter userListAdapter = new TimelineAdapter(accounts);
        //UserListAdapter userListAdapter = new UserListAdapter(accounts);
        //userRecycler.setAdapter(userListAdapter);
        //userRecycler.setLayoutManager(new LinearLayoutManager(this));
        //userRecycler.getLayoutManager().setMeasurementCacheEnabled(true);
        //return rootView;

    }

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
