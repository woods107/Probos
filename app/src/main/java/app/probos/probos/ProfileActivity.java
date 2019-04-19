package app.probos.probos;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.entity.MastodonList;
import com.sys1yagi.mastodon4j.api.method.Accounts;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.method.MastodonLists;
import com.sys1yagi.mastodon4j.api.method.Statuses;
import com.sys1yagi.mastodon4j.api.entity.Relationship;
import com.sys1yagi.mastodon4j.api.method.Follows;
import com.sys1yagi.mastodon4j.api.method.FollowRequests;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

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

    MastodonClient userClient;
    MastodonLists tmpLists;
    List<MastodonList> lists;

    ArrayAdapter<String> choiceSpinAdapter;
    Spinner listSpinner;
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

        userClient = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken).build();
        tmpAcct = new Accounts(userClient);

        //BEGIN EDIT LIST CODE

        ImageButton listButton = (ImageButton) findViewById(R.id.listButton);
        listButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String[] settings = {"OK", "Cancel"};
                //Looper.prepare();

                try {
                    listSpinner = new Spinner(ProfileActivity.this);
                    new findLists().execute();
                    AlertDialog.Builder listMenu = new AlertDialog.Builder(view.getContext());
                    listMenu.setTitle("Add User To List:");
                    //final EditText newTitle= new EditText(view.getContext());
                    listMenu.setView(listSpinner);
                    //Looper.prepare();
                    listMenu.setItems(settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // the user clicked on colors[which]
                            switch (which) {
                                case 0:
                                    //create list
                                    //create new list object with
                                   // new ListsActivity.createList(newTitle.getText().toString()).execute();
                                    Long listId = lists.get(listSpinner.getSelectedItemPosition()).getId();
                                    String path = "lists/" + listId + "/accounts";
                                    String accountForm = acctId.toString();
                                    RequestBody requestBody = new MultipartBody.Builder()
                                            .setType(MultipartBody.FORM)
                                            .addFormDataPart("account_ids[]",accountForm)
                                            .build();
                                    new addToList(path, requestBody).execute();
                                    break;
                                case 1:
                                    //do nothing
                                    break;
                                default:
                                    break;
                            }

                        }
                    });
                    listMenu.show();
                    //add turning button on/off
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button pinsListButton = (Button) findViewById(R.id.pinsViewButton);
        pinsListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ListsActivity.class);
                intent.putExtra("instanceName", instanceName);
                intent.putExtra("accessToken", accessToken);
                intent.putExtra("pins", true);
                intent.putExtra("acctId", acctId);
                startActivity(intent);
            }
        });


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

    private class addToList extends AsyncTask<Void, Void, Void> {
        String path;
        RequestBody requestBody;

        private addToList(String p, RequestBody req) {
            path = p;
            requestBody = req;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Response pls = userClient.post(path, requestBody);
            int code = pls.code();
            return null;
        }
    }

    private class findLists extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                tmpLists = new MastodonLists(userClient);
                lists = tmpLists.getLists().execute().getPart();
                ArrayList<String> listNames = new ArrayList<>();

                for (int i = 0; i < lists.size(); i++) {
                    listNames.add(lists.get(i).getTitle());
                }
                choiceSpinAdapter = new ArrayAdapter<String>(ProfileActivity.this, android.R.layout.simple_spinner_item, listNames);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //listSpinner.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            listSpinner.setAdapter(choiceSpinAdapter);
        }
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
