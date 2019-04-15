package app.probos.probos;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.method.Accounts;
import com.sys1yagi.mastodon4j.api.method.Public;
import com.sys1yagi.mastodon4j.api.method.Timelines;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
public class favoritedList extends AppCompatActivity {

    String instanceName;
    String accessToken;
    RecyclerView personalRecycler;
    Timelines timelines;
    boolean loaded = false;

    CoordinatorLayout tLayout;
    int defaultColor;



    List<Status> statusList;
    TimelineAdapter timelineAdapter;
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


    }
    @Override
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
    }







}
