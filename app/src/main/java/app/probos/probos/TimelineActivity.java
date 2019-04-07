package app.probos.probos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
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
import com.sys1yagi.mastodon4j.api.method.Notifications;
import com.sys1yagi.mastodon4j.api.method.Statuses;
import com.sys1yagi.mastodon4j.api.method.Streaming;

import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import okhttp3.OkHttpClient;

public class TimelineActivity extends AppCompatActivity {

    static String instanceName;
    static String accessTokenStr;
    static boolean staySignedIn;
    static String flag = "PERSONAL";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        Intent currIntent = getIntent();
        instanceName = currIntent.getStringExtra("instancename");
        accessTokenStr = currIntent.getStringExtra("accesstoken");
        staySignedIn = currIntent.getBooleanExtra("staySignedIn",false);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        //getSupportActionBar().setDisplayUseLogoEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        if(staySignedIn==false) {
            SharedPreferences li = getSharedPreferences("Login", MODE_PRIVATE);
            SharedPreferences.Editor Ed = li.edit();

            Ed.putString("accessToken", null);
            Ed.putString("instance", null);
            Ed.commit();
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //draft button has been clicked
                Intent draft = new Intent(TimelineActivity.this , DraftActivity.class);
                try {
                    draft.putExtra("instanceName",instanceName);
                    draft.putExtra("access",accessTokenStr);
                    startActivity(draft);

                } catch (Exception e) {
                    e.printStackTrace();
                }// End try/catch block
                //startActivity(draft);
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                */
            }
        });

        //MastodonClient userClient = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken).build();
        //Notifications tmpNotification = new Notifications(userClient);


        MastodonClient client = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson())
                .accessToken(accessTokenStr)
                .useStreamingApi()
                .build();
        Handler handler = new Handler() {
            @Override
            public void onStatus(@NotNull Status status) {
                System.out.println(status.getContent());
            }

            @Override
            public void onNotification(@NotNull Notification notification) {/* no op */
                NotificationCompat.Builder builder = new NotificationCompat.Builder(TimelineActivity.this, "probos")
                        .setSmallIcon(R.drawable.class.getModifiers())
                        .setContentTitle("Probos")
                        .setContentText("do this work?")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            }
            @Override
            public void onDelete(long id) {/* no op */}
        };

        Streaming streaming = new Streaming(client);
        try {
            Shutdownable shutdownable = streaming.localPublic(handler);
            Thread.sleep(10000L);
            shutdownable.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
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
        }else if(id ==R.id.notifications) {
            try {
                Intent intent = new Intent(this, NotificationsPicker.class);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                throw new IllegalArgumentException();
            }
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    Tab1Personal tab1 = new Tab1Personal();
                    tab1.setInfo(instanceName,accessTokenStr);
                    return tab1;
                case 1:
                    Tab2Local tab2 = new Tab2Local();
                    tab2.setInfo(instanceName,accessTokenStr);
                    return tab2;
                case 2:
                    Tab3Federated tab3 = new Tab3Federated();
                    tab3.setInfo(instanceName,accessTokenStr);
                    return tab3;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle (int position) {
            switch (position) {
                case 0:
                    return "PERSONAL";
                case 1:
                    return "LOCAL";
                case 2:
                    return "FEDERATED";
                default:
                    return null;
            }
        }
    }



}
