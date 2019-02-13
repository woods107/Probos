package app.probos.probos;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sys1yagi.mastodon4j.api.method.Timelines;

public class Tab3Federated extends Fragment {
    String instanceName;
    String accessToken;
    RecyclerView personalRecycler;
    Timelines timelines;
    //RVAdapter rvAdapter;

    public void setInfo(String instanceName, String accessToken){
        this.accessToken = accessToken;
        this.instanceName = instanceName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab1personal, container, false);
        //timelines = InstanceChoiceActivity.
        personalRecycler = (RecyclerView) rootView.findViewById(R.id.personal_recycler);
        return rootView;
    }
}