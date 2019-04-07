package app.probos.probos;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.entity.Relationship;
import com.sys1yagi.mastodon4j.api.method.Accounts;

import java.net.URL;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;

//import android.view.LayoutInflater;

public class EditProfileActivity extends AppCompatActivity {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
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
            EditText displayName = findViewById(R.id.displayNameEditText);
            String displayNameText = currAcct.getDisplayName();
            if (!displayNameText.equals("")) {
                displayName.setText(currAcct.getDisplayName());
            } else {
                displayName.setText(currAcct.getUserName());
            }

            TextView userName = findViewById(R.id.fullUserName);
            userName.setText("@" + currAcct.getAcct());


            EditText profileBio = findViewById(R.id.profileBioEditText);
            profileBio.setText(Html.fromHtml(currAcct.getNote(), Html.FROM_HTML_MODE_COMPACT));

            ImageView profBanner = findViewById(R.id.profile_banner);
            profBanner.setImageBitmap(bannerBitmap);

            CircleImageView imageView = findViewById(R.id.profile_full_picture);
            imageView.setImageBitmap(ppBitmap);
            imageView.bringToFront();


        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_menu_done:

                EditText displayName = (EditText) findViewById(R.id.displayNameEditText);
                EditText profileBio = (EditText) findViewById(R.id.profileBioEditText);

                Thread profileThr = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            tmpAcct.updateCredential(displayName.getText().toString(), profileBio.getText().toString(), null, null).execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                profileThr.start();

                try {
                    profileThr.join();
                } catch (Exception e) {
                    //literally do nothing plz
                    e.printStackTrace();
                }

                onBackPressed();
                return true;

        }// End switch

        return super.onOptionsItemSelected(item);

    }

}
