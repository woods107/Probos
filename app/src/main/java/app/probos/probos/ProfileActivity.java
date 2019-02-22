package app.probos.probos;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.method.Accounts;

import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;

//import android.view.LayoutInflater;

public class ProfileActivity extends AppCompatActivity {

    // Get these three things before starting anything

    String instanceName;
    String accessToken;
    Long acctId;

    Account currAcct;
    Bitmap ppBitmap;
    Bitmap bannerBitmap;

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
        Accounts tmpAcct = new Accounts(userClient);

        Thread infoRetrieval = new Thread(new Runnable() {
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

        infoRetrieval.start();

        try {
            infoRetrieval.join();
        } catch (Exception e) {
            //do nothing
        }

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
