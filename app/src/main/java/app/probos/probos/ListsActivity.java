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
import com.sys1yagi.mastodon4j.Parameter;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.entity.MastodonList;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.method.Accounts;
import com.sys1yagi.mastodon4j.api.method.MastodonLists;
import com.sys1yagi.mastodon4j.api.method.Statuses;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
//package app.probos.probos;

//Daniel- May be added to ListActivity later

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

//DVONLINE
public class ListsActivity extends AppCompatActivity {

    // Get these three things before starting anything

    String instanceName;
    String accessToken;
    Long pinAcctId;
    boolean pins = false;

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
    Accounts acctUse;

    RecyclerView listRecycler;
    List<MastodonList> lists;
    Pageable<Status> returnedStatuses;

    private TimelineAdapter listAdapter;
///    int toGet = 0;
    Spinner s;
    ArrayAdapter<String> spinAdapter;
    Account[] returnedAccts;

    Spinner listUserSpinner;

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
        pins = currIntent.getBooleanExtra("pins", false);
        pinAcctId = currIntent.getLongExtra("acctId", -1);

        //toGet = currIntent.getIntExtra("toGet",0);

        // Begin instantiating the listRecycler
        listRecycler = findViewById(R.id.listitem_recycler);

        // Figure out how to set this up better
        //View rootView = getLayoutInflater().inflate

        userClient = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken).build();
        tmpLists = new MastodonLists(userClient);
        acctUse = new Accounts(userClient);




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

        TextView pinsTextView = (TextView) findViewById(R.id.pinTextView);
        pinsTextView.setVisibility(View.INVISIBLE);

        //DANYUL CODE BEGIN HURRR
        ImageButton addList = (ImageButton) findViewById(R.id.addLists);
        if (pins) {
            addList.setVisibility(View.INVISIBLE);
        }
        addList.setOnClickListener(new View.OnClickListener(){
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

        ImageButton deleteList = (ImageButton) findViewById(R.id.deleteLists);
        deleteList.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String[] settings = {"OK","Cancel"};
                //Looper.prepare();

                try {


                    AlertDialog.Builder listMenu = new AlertDialog.Builder(view.getContext());
                    listMenu.setTitle("Are you sure you want to delete this list?");
                    //Looper.prepare();
                    listMenu.setItems(settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // the user clicked on colors[which]
                            switch (which) {
                                case 0:
                                    //create list
                                    //create new list object with
                                    new deleteList(lists.get(s.getSelectedItemPosition()).getId()).execute();
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

        ImageButton viewListUsers = (ImageButton) findViewById(R.id.listUsers);
        viewListUsers.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String[] settings = {"Remove","Cancel"};
                //Looper.prepare();

                try {

                    listUserSpinner = new Spinner(ListsActivity.this);
                    new getListUsers(lists.get(s.getSelectedItemPosition()).getId()).execute();
                    AlertDialog.Builder listMenu = new AlertDialog.Builder(view.getContext());
                    listMenu.setTitle("Users in list:");
                    //Looper.prepare();
                    listMenu.setView(listUserSpinner);
                    listMenu.setItems(settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // the user clicked on colors[which]
                            switch (which) {
                                case 0:
                                    //create list
                                    //create new list object with
                                    new removeUserFromList(lists.get(s.getSelectedItemPosition()).getId(), returnedAccts[listUserSpinner.getSelectedItemPosition()].getId()).execute();
                                    //userClient.delete("lists/" + lists.get(s.getSelectedItemPosition()).getId() + "accounts/?account_ids[]=" + returnedAccts[listUserSpinner.getSelectedItemPosition()].getId());

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

        if (pins) {
            s.setVisibility(View.INVISIBLE);
            addList.setVisibility(View.INVISIBLE);
            deleteList.setVisibility(View.INVISIBLE);
            viewListUsers.setVisibility(View.INVISIBLE);
            pinsTextView.setVisibility(View.VISIBLE);
        }

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

        @Override
        protected void onPostExecute(Void aVoid) {
            new setUpListOfLists().execute();
        }
    }

    private class deleteList extends AsyncTask<Void, Void, Void>{

        Long listId;

        private deleteList(Long id){
            listId = id;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            userClient.delete("lists/" + listId);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new setUpListOfLists().execute();
        }
    }

    private class removeUserFromList extends AsyncTask<Void, Void, Void>{

        Long listId;
        Long acctId;

        private removeUserFromList(Long listId, Long acctId){
            this.listId = listId;
            this.acctId = acctId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            userClient.delete("lists/" + listId + "/accounts?account_ids[]=" + acctId);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new setUpListOfLists().execute();
        }
    }


    private class getListUsers extends AsyncTask<Void, Void, Void>{
        ArrayList<String> userNames = new ArrayList<>();
        Long listId;

        private getListUsers(Long id){
            listId = id;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Parameter limitParam = new Parameter();
            limitParam.append("limit", 0);
            Response response = userClient.get("lists/" + listId + "/accounts", limitParam);
            String responseBody;
            try {
                responseBody = response.body().string();
                returnedAccts = new Gson().fromJson(responseBody, Account[].class);
                for (int i = 0; i < returnedAccts.length; i++) {
                    userNames.add(returnedAccts[i].getUserName());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
        //    new setUpListOfLists().execute();
            try {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ListsActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, userNames);
                listUserSpinner.setAdapter(adapter);
                //add turning button on/off
            }catch (Exception e) {
                e.printStackTrace();
            }
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
                ArrayList<String> listNames = new ArrayList<>();
                if (pins) {
                    try {
                        returnedStatuses = acctUse.getStatuses(pinAcctId,false,false,true).execute();
                        listAdapter = new TimelineAdapter(returnedStatuses.getPart(), accessToken, instanceName, 1);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    lists = tmpLists.getLists().execute().getPart();
                    for (int i = 0; i < lists.size(); i++) {
                        listNames.add(lists.get(i).getTitle());
                    }
                    spinAdapter = new ArrayAdapter<String>(ListsActivity.this,
                            R.layout.list_spinner, listNames);
                }

         //       Pageable<com.sys1yagi.mastodon4j.api.entity.Status> firstStatuses = tmpLists.getListTimeLine(lists.get(0).getId(), range).execute();
          //      listAdapter = new TimelineAdapter(firstStatuses.getPart(),accessToken,instanceName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(Void param) {
            s.setAdapter(spinAdapter);
            listRecycler.setAdapter(listAdapter);
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
