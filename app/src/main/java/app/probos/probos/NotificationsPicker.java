package app.probos.probos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.MastodonRequest;
import com.sys1yagi.mastodon4j.api.Handler;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.Shutdownable;
import com.sys1yagi.mastodon4j.api.entity.Notification;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Accounts;
import com.sys1yagi.mastodon4j.api.method.Notifications;
import com.sys1yagi.mastodon4j.api.method.Statuses;
import com.sys1yagi.mastodon4j.api.method.Streaming;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import okhttp3.OkHttpClient;

import android.app.Activity;

public class NotificationsPicker extends AppCompatActivity {

    static String instanceName;
    static String accessTokenStr;
    long longid=0;

    CoordinatorLayout tLayout;
    Button back;
    int defaultColor;
    int sDefaultColor;

    /*MastodonClient client = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson())
            .accessToken(accessTokenStr)
            .useStreamingApi()
            .build();
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_picker);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        Intent currIntent = getIntent();
        instanceName = currIntent.getStringExtra("instancename");
        accessTokenStr = currIntent.getStringExtra("accesstoken");

       /* MastodonClient client = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson())
                .accessToken(accessTokenStr)
                .useStreamingApi()
                .build();*/

        Intent intent = getIntent();

        tLayout= (CoordinatorLayout) findViewById(R.id.activity_notification);


        SharedPreferences sp2=this.getSharedPreferences("Color", MODE_PRIVATE);
        defaultColor = sp2.getInt("BackgroundColor", 0);
        sDefaultColor=sp2.getInt("SecondaryColor",0);
        if(defaultColor==0) {
            defaultColor= ContextCompat.getColor(NotificationsPicker.this, R.color.colorPrimaryDark);
            tLayout.setBackgroundColor(defaultColor);
        }else{
            tLayout.setBackgroundColor(defaultColor);
        }
        if(sDefaultColor==0){
            sDefaultColor= ContextCompat.getColor(NotificationsPicker.this,R.color.colorPrimary);
        }
        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        toolbar.setBackgroundColor(sDefaultColor);
        if(intent.getLongExtra("id",0 )>0){
            longid=intent.getLongExtra("id",0 );
        }



        Switch follow = findViewById(R.id.Follow);
        follow.setBackgroundColor(sDefaultColor);
        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Switch favorite = findViewById(R.id.Favorite);
        favorite.setBackgroundColor(sDefaultColor);
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Switch mention = findViewById(R.id.Mention);
        mention.setBackgroundColor(sDefaultColor);
        mention.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Switch reblog = findViewById(R.id.Reblog);
        reblog.setBackgroundColor(sDefaultColor);
        reblog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });




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


    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //it works first try!
        if (id == R.id.action_settings) {
            SharedPreferences sp1=this.getSharedPreferences("Login", MODE_PRIVATE);
            sp1.edit().clear().commit();
            try {
                Intent intent = new Intent(this, InstanceChoiceActivity.class);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                throw new IllegalArgumentException();
            }
            return true;
        }
        else if (id == R.id.action_displayName) {

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

        }
        else if (id == R.id.action_bio) {

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

        }else if(id == R.id.favorites){
            try {
                Intent intent = new Intent(this, favoritedList.class);
                intent.putExtra("accesstoken",accessTokenStr);
                intent.putExtra("instancename",instanceName);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                throw new IllegalArgumentException();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

}
