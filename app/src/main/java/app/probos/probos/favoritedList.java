package app.probos.probos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.entity.Attachment;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.method.Accounts;
import com.sys1yagi.mastodon4j.api.method.Public;
import com.sys1yagi.mastodon4j.api.method.Statuses;
import com.sys1yagi.mastodon4j.api.method.Timelines;
import com.sys1yagi.mastodon4j.api.method.Favourites;
import com.sys1yagi.mastodon4j.api.entity.Relationship;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static app.probos.probos.TimelineActivity.accessTokenStr;
import static app.probos.probos.TimelineActivity.instanceName;

public class favoritedList  extends AppCompatActivity {
    String instanceName;
    String accessToken;
    RecyclerView personalRecycler;
    Timelines timelines;
    Favourites favs;
    boolean loaded = false;
    Pageable<com.sys1yagi.mastodon4j.api.entity.Status> statuses;

    CoordinatorLayout tLayout;
    Toolbar tool;
    int defaultColor;
    int sDefaultColor;

    public void setInfo(){
        Intent currIntent = getIntent();
        instanceName = currIntent.getStringExtra("instancename");
        accessToken = currIntent.getStringExtra("accesstoken");

    }

    List<Status> statusList;
    TimelineAdapter timelineAdapter;
    SwipeRefreshLayout swipeLayout;



    private class grabTimeline extends AsyncTask<Void, Void, Void>{


        protected Void doInBackground(Void... param) {
            try {
                //com.sys1yagi.mastodon4j.api.entity.Status status =new com.sys1yagi.mastodon4j.api.entity.Status();
                MastodonClient authClient = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken).build();
                favs = new Favourites(authClient);
                Range range = new Range(null, null, 50);
                statuses= favs.getFavourites(range).execute();
                statusList = statuses.getPart();

            } catch (Exception e) {
                throw new IllegalArgumentException();
            }

            timelineAdapter = new TimelineAdapter(statusList, accessToken, instanceName);
            loaded = true;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            personalRecycler.setAdapter(timelineAdapter);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorited_list);
        setInfo();
        MastodonClient authClient = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken).build();
        timelines = new Timelines(authClient);
        SharedPreferences sp2=this.getSharedPreferences("Color", MODE_PRIVATE);
        tLayout=(CoordinatorLayout) findViewById(R.id.activity_favorited_list);
        tool= (Toolbar) findViewById(R.id.toolbar);
        defaultColor = sp2.getInt("BackgroundColor", 0);
        sDefaultColor=sp2.getInt("SecondaryColor",0);
        if(defaultColor==0) {
            defaultColor= ContextCompat.getColor(favoritedList.this, R.color.colorPrimaryDark);
            tLayout.setBackgroundColor(defaultColor);
        }else{
            tLayout.setBackgroundColor(defaultColor);
        }
        if(sDefaultColor==0){
            sDefaultColor= ContextCompat.getColor(favoritedList.this,R.color.colorPrimary);
        }
        tool.setBackgroundColor(sDefaultColor);

        //new Tab1Personal.grabTimeline().execute();
        new favoritedList.grabTimeline().execute();




/*
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_favorited_list, container, false);
        swipeLayout = rootView.findViewById(R.id.swipe_container_personal);
        // Adding Listener
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code here
                new favoritedList.grabTimeline().execute();
                personalRecycler.setAdapter(timelineAdapter);
                // To keep animation for 4 seconds
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        // Stop animation (This will be after 3 seconds)
                        swipeLayout.setRefreshing(false);
                    }
                }, 4000); // Delay in millis
            }
        });*/

        personalRecycler = (RecyclerView) findViewById(R.id.personal_recycler2);
        personalRecycler.setAdapter(timelineAdapter);
        personalRecycler.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        personalRecycler.getLayoutManager().setMeasurementCacheEnabled(true);
    }


}







   /* String instanceName;
    String accessToken;
    RecyclerView personalRecycler;
    Timelines timelines;
    boolean loaded = false;

    CoordinatorLayout tLayout;
    int defaultColor;



    List<Status> statusList;
    favoritedList favoritedList;
    SwipeRefreshLayout swipeLayout;


    MastodonClient client = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson())
            .accessToken(accessToken)
            .useStreamingApi()
            .build();



    @Override
    public void onCreate(Bundle savedInstanceState) {
        Intent currIntent = getIntent();
        instanceName = currIntent.getStringExtra("instancename");
        accessToken = currIntent.getStringExtra("accesstoken");
        super.onCreate(savedInstanceState);
        MastodonClient authClient = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken).build();
        timelines = new Timelines(authClient);
        //new grabTimeline().execute();

        tLayout= (CoordinatorLayout) findViewById(R.id.content_favorited_list);
        SharedPreferences sp2=this.getSharedPreferences("Color", MODE_PRIVATE);
        defaultColor = sp2.getInt("BackgroundColor", 0);
        if(defaultColor==0) {
            defaultColor= ContextCompat.getColor(favoritedList.this, R.color.colorPrimaryDark);
            tLayout.setBackgroundColor(defaultColor);
        }else{
            tLayout.setBackgroundColor(defaultColor);
        }


    }*/
   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //it works first try!
        if (id == R.id.action_settings) {
            SharedPreferences sp1 = this.getSharedPreferences("Login", MODE_PRIVATE);
            sp1.edit().clear().commit();
            try {
                Intent intent = new Intent(this, InstanceChoiceActivity.class);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                throw new IllegalArgumentException();
            }
            return true;
        } else if (id == R.id.notifications) {
            try {
                Intent intent = new Intent(this, NotificationsPicker.class);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                throw new IllegalArgumentException();
            }
            return true;

        } else if (id == R.id.action_displayName) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Display Name");

            final EditText in = new EditText(this);
            in.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(in);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Accounts acct = new Accounts(client);

                    Thread nameThr = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                acct.updateCredential(in.getText().toString(), null, null, null).execute();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    nameThr.start();

                    try {
                        nameThr.join();
                    } catch (Exception e) {
                        //literally do nothing plz
                        e.printStackTrace();
                    }

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

        } else if (id == R.id.action_bio) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Update Bio");

            final EditText in = new EditText(this);
            in.setInputType(InputType.TYPE_CLASS_TEXT);

            builder.setView(in);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Accounts acct = new Accounts(client);

                    Thread bioThr = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                acct.updateCredential(null, in.getText().toString(), null, null).execute();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    bioThr.start();

                    try {
                        bioThr.join();
                    } catch (Exception e) {
                        //literally do nothing plz
                        e.printStackTrace();
                    }

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

        }
        return super.onOptionsItemSelected(item);
    }*/







//}
