package app.probos.probos;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.method.Accounts;
import com.sys1yagi.mastodon4j.api.method.Notifications;
import com.sys1yagi.mastodon4j.api.method.Statuses;

import org.w3c.dom.Text;

import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;

//import android.view.LayoutInflater;

public class ExpandedStatusActivity extends AppCompatActivity {

    // Get these three things before starting anything

    String instanceName;
    String accessToken;
    Long statusId;

    Status currStatus;
    Bitmap ppBitmap;

    MastodonClient userClient;
    Statuses tmpStats;
    //Bitmap bannerBitmap;

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
        setContentView(R.layout.activity_expanded_status);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // This line here should enable a back button on the action bar
        // Once the UI is more developed, it will be useful to have
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent currIntent = getIntent();
        statusId = currIntent.getLongExtra("id", 0);
        instanceName = currIntent.getStringExtra("name");
        accessToken = currIntent.getStringExtra("token");

        // Begin instantiating the userRecycler
       // userRecycler = findViewById(R.id.user_recycler);

        // Figure out how to set this up better
        //View rootView = getLayoutInflater().inflate

        userClient = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken).build();
        tmpStats = new Statuses(userClient);

        new setUpExpand().execute();
       /* Thread infoRetrieval = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    currStatus = tmpStats.getStatus(statusId).execute();
                    URL newPP = new URL(currStatus.getAccount().getAvatar());
                    ppBitmap = BitmapFactory.decodeStream(newPP.openConnection().getInputStream());
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
        }*/



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

    private class setUpExpand extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... param) {
            try {
                try {
                    currStatus = tmpStats.getStatus(statusId).execute();
                    URL newPP = new URL(currStatus.getAccount().getAvatar());
                    ppBitmap = BitmapFactory.decodeStream(newPP.openConnection().getInputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void param) {
            TextView statusTextFull = findViewById(R.id.statusText);
            String rawContent = currStatus.getContent();
            statusTextFull.setText(Html.fromHtml(rawContent,Html.FROM_HTML_MODE_COMPACT));

            TextView displayName = findViewById(R.id.displayNameExp);
            String displayNameText = currStatus.getAccount().getDisplayName();
            if (!displayNameText.equals("")) {
                displayName.setText(currStatus.getAccount().getDisplayName());
            } else {
                displayName.setText(currStatus.getAccount().getUserName());
            }

            TextView userName = findViewById(R.id.fullUserNameExp);
            userName.setText("@" + currStatus.getAccount().getAcct());

            TextView timeText = findViewById(R.id.msgTimeExp);
            String rawTime = currStatus.getCreatedAt();
            String time = rawTime.substring(0,10) + " " + rawTime.substring(12,19) + " UTC";
            timeText.setText(time);

            TextView reblogCount = findViewById(R.id.reblogs_count);
            reblogCount.setText("Reblogs: " + currStatus.getReblogsCount());

            TextView replyCount = findViewById(R.id.replies_count);
            replyCount.setText("Replies: " + currStatus.getRepliesCount());

        /*TextView replyCount = findViewById(R.id.reply_count);
        replyCount.setText(String.valueOf(currStatus.getRepliesCount()));*/


            CircleImageView imageView = findViewById(R.id.profile_full_picture_exp);
            imageView.setImageBitmap(ppBitmap);
            imageView.bringToFront();
            imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                    intent.putExtra("id", currStatus.getAccount().getId());
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

        //TimelineAdapter userListAdapter = new TimelineAdapter(accounts);
        //UserListAdapter userListAdapter = new UserListAdapter(accounts);
        //userRecycler.setAdapter(userListAdapter);
        //userRecycler.setLayoutManager(new LinearLayoutManager(this));
        //userRecycler.getLayoutManager().setMeasurementCacheEnabled(true);
        //return rootView;

    }

}
