package app.probos.probos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.entity.MastodonList;
import com.sys1yagi.mastodon4j.api.method.MastodonLists;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
//package app.probos.probos;

//Daniel- May be added to ListActivity later

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

//List is an object with two main fields: title and an ArrayList of SharedPreferences for users
//Version 2 will outsource this with custom API calls to Mastodon servers directly
class ListClient {
    private String Title = "";
    private ArrayList<SharedPreferences> Users;
    public ListClient(String title, ArrayList<SharedPreferences> users){
        Title = title;
        SharedPreferences temp;
        for(int i =0;i<users.size();i++){
            temp = users.get(i);
            Users.add(temp);
        }

    }
    public ListClient(String title, SharedPreferences user){
        Title = title;
        Users.add(user);
    }
    public ListClient(String title){
        Title = title;

    }
    public void AddUser(SharedPreferences user){
        Users.add(user);
    }
    public void RemoveUser(SharedPreferences user){
        Users.remove(user);
    }
    public void setTitle(String newTitle){
        Title = newTitle;
    }
    public String getTitle(){
        return Title;
    }
    public ArrayList<SharedPreferences> getUsers(){
        return Users;
    }
}

//DVONLINE
public class ListsActivity extends AppCompatActivity {

    // Get these three things before starting anything

    String instanceName;
    String accessToken;
    Long acctId;

    /*
    public void setInfo(String instanceName, String accessToken, Long acctId) {
        this.instanceName = instanceName;
        this.accessToken = accessToken;
        this.acctId = acctId;
    }
    */
    MastodonClient userClient;
    MastodonLists tmpLists;
    int currentListInd = 0;

    RecyclerView listRecycler;
    List<MastodonList> lists;

    private TimelineAdapter listAdapter;
///    int toGet = 0;
    Spinner s;
    ArrayAdapter<String> spinAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);
        Toolbar toolbar = findViewById(R.id.toolbar);
        s = (Spinner) findViewById(R.id.spinner);
        setSupportActionBar(toolbar);
        // This line here should enable a back button on the action bar
        // Once the UI is more developed, it will be useful to have
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent currIntent = getIntent();
        //acctId = currIntent.getLongExtra("id", 0);
        instanceName = currIntent.getStringExtra("instanceName");
        accessToken = currIntent.getStringExtra("accessToken");
        //toGet = currIntent.getIntExtra("toGet",0);

        // Begin instantiating the listRecycler
        listRecycler = findViewById(R.id.listitem_recycler);

        // Figure out how to set this up better
        //View rootView = getLayoutInflater().inflate

        userClient = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken).build();
        tmpLists = new MastodonLists(userClient);




        /*Thread followerRetrieval = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    // Gets the first 50 users from the list of followers acctId has
                    Range range = new Range(null,null,50);
                    Pageable<Account> users = tmpAcct.getFollowers(acctId, range).execute();
                    accounts = users.getPart();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        followerRetrieval.start();

        try {
            followerRetrieval.join();
        } catch (Exception e) {
            //do nothing
        }*/
        new setUpListOfLists().execute();

        //DANYUL CODE BEGIN HURRR
        ImageButton addUser = (ImageButton) findViewById(R.id.addUsers);
        addUser.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String[] settings = {"OK","Cancel"};
                //Looper.prepare();

                try {


                    AlertDialog.Builder listMenu = new AlertDialog.Builder(view.getContext());
                    listMenu.setTitle("New List Title:");
                    final EditText newTitle= new EditText(view.getContext());
                    listMenu.setView(newTitle);
                    //Looper.prepare();
                    listMenu.setItems(settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // the user clicked on colors[which]
                            switch (which) {
                                case 0:
                                    //create list
                                    //create new list object with
                                    new createList(newTitle.getText().toString()).execute();
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

        //DANYUL CODE END HURR
        //TimelineAdapter userListAdapter = new TimelineAdapter(accounts);
        listRecycler.setAdapter(listAdapter);
        listRecycler.setLayoutManager(new LinearLayoutManager(this));
        listRecycler.getLayoutManager().setMeasurementCacheEnabled(true);

        s.setAdapter(spinAdapter);
        //return rootView;

        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                currentListInd = position;
                new updateList().execute();
                int j = 0;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
    }
    private class createList extends AsyncTask<Void, Void, Void>{

        String newTitle;

        private createList(String title){
            newTitle = title;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("title",newTitle)
                    .build();
            userClient.post("lists",requestBody);
            return null;
        }

    }
    private class updateList extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... voids) {
            try {
          //      listAdapter = null;
                Range range = new Range(null, null, 50);
                Pageable<com.sys1yagi.mastodon4j.api.entity.Status> firstStatuses = tmpLists.getListTimeLine(lists.get(currentListInd).getId(), range).execute();
                listAdapter = new TimelineAdapter(firstStatuses.getPart(), accessToken, instanceName, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //listRecycler.setAdapter(null);
            listRecycler.setAdapter(listAdapter);
            int i = 0;
        }
    }

    private class setUpListOfLists extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... param) {
            Range range = new Range(null,null,50);
            try {
                // Gets the first 50 users from the list of followers acctId has

                //Pageable<MastodonList> listPageable;
                //if (toGet == 1) {
                //listPageable = tmpLists.getLists().execute();
                //} else {
                //    users = tmpAcct.getFollowing(acctId, range).execute();
                //}

                lists = tmpLists.getLists().execute().getPart();
                ArrayList<String> listNames = new ArrayList<>();
                for (int i = 0; i < lists.size(); i++) {
                    listNames.add(lists.get(i).getTitle());
                }
                spinAdapter = new ArrayAdapter<String>(ListsActivity.this,
                        android.R.layout.simple_spinner_item, listNames);


         //       Pageable<com.sys1yagi.mastodon4j.api.entity.Status> firstStatuses = tmpLists.getListTimeLine(lists.get(0).getId(), range).execute();
          //      listAdapter = new TimelineAdapter(firstStatuses.getPart(),accessToken,instanceName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(Void param) {
            s.setAdapter(spinAdapter);
        }

        //TimelineAdapter userListAdapter = new TimelineAdapter(accounts);
        //UserListAdapter userListAdapter = new UserListAdapter(accounts);
        //userRecycler.setAdapter(userListAdapter);
        //userRecycler.setLayoutManager(new LinearLayoutManager(this));
        //userRecycler.getLayoutManager().setMeasurementCacheEnabled(true);
        //return rootView;

    }

    // TODO Debug why nested user lists sometimes breaks
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
