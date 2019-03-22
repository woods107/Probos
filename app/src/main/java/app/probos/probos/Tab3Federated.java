package app.probos.probos;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.method.Public;
import com.sys1yagi.mastodon4j.api.method.Timelines;

import java.util.List;

import okhttp3.OkHttpClient;

public class Tab3Federated extends Fragment {
    String instanceName;
    String accessToken;
    RecyclerView federatedRecycler;
    Public pub;
    boolean loaded = false;

    public void setInfo(String instanceName, String accessToken){
        this.accessToken = accessToken;
        this.instanceName = instanceName;
    }

    private class grabTimeline extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... param) {
            try {
                Range range = new Range(null, null, 50);
                Pageable<com.sys1yagi.mastodon4j.api.entity.Status> statuses = pub.getFederatedPublic(range).execute();
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
            federatedRecycler.setAdapter(timelineAdapter);
        }
    }

    List<Status> statusList;
    TimelineAdapter timelineAdapter;
    SwipeRefreshLayout swipeLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MastodonClient authClient = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken).build();
        //Timelines timelines = new Timelines(authClient);
        pub = new Public(authClient);
        new grabTimeline().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab3federated, container, false);
        swipeLayout = rootView.findViewById(R.id.swipe_container_personal);
        // Adding Listener
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code here
                new grabTimeline().execute();
                federatedRecycler.setAdapter(timelineAdapter);
                // To keep animation for 4 seconds
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        // Stop animation (This will be after 3 seconds)
                        swipeLayout.setRefreshing(false);
                    }
                }, 4000); // Delay in millis
            }
        });
        federatedRecycler = (RecyclerView) rootView.findViewById(R.id.federated_recycler);
        federatedRecycler.setAdapter(timelineAdapter);
        federatedRecycler.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        federatedRecycler.getLayoutManager().setMeasurementCacheEnabled(true);
        return rootView;
    }
}