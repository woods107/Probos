package app.probos.probos;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.sys1yagi.mastodon4j.api.method.Timelines;

import java.util.List;

import okhttp3.OkHttpClient;

public class Tab1Personal extends Fragment {
    String instanceName;
    String accessToken;
    RecyclerView personalRecycler;
    Timelines timelines;

    public void setInfo(String instanceName, String accessToken){
        this.accessToken = accessToken;
        this.instanceName = instanceName;
    }

    List<Status> statusList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab1personal, container, false);
        MastodonClient authClient = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken).build();
        Timelines timelines = new Timelines(authClient);
        personalRecycler = (RecyclerView) rootView.findViewById(R.id.personal_recycler);

        Thread timelineRetrieval = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Range range = new Range(null,null,50);
                    Pageable<Status> statuses = timelines.getHome(range).execute();
                    statusList = statuses.getPart();
                } catch (Exception e) {
                    throw new IllegalArgumentException();
                }
            }
        });

        timelineRetrieval.start();

        try {
            timelineRetrieval.join();
        } catch (Exception e) {
            //do nothing
        }
        TimelineAdapter timelineAdapter = new TimelineAdapter(statusList);
        personalRecycler.setAdapter(timelineAdapter);
        personalRecycler.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        personalRecycler.getLayoutManager().setMeasurementCacheEnabled(true);
        return rootView;
    }


}
