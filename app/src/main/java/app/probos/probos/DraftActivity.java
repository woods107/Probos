package app.probos.probos;

import android.app.AlertDialog;
import android.app.AlertDialog.*;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.content.Intent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.MastodonRequest;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.method.Statuses;

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

public class DraftActivity extends AppCompatActivity {
    MastodonClient client;
    boolean isReply = false;
    long id = 0;


    //set visibility variable
    String[] settings = {"Public","Direct","Private","Unlisted"};
    Status.Visibility visibility = Status.Visibility.Public;

    String[] draftSettings = {"Save Draft", "Load Draft"};
    String[] draftSaveSettings = {"Draft 1", "Draft 2", "Draft 3"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        client = new MastodonClient.Builder(intent.getStringExtra("instanceName"), new OkHttpClient.Builder(), new Gson()).accessToken(intent.getStringExtra("access")).build();
        String prevStatus = intent.getStringExtra("prevStatus");



        if(intent.getLongExtra("replyID",0)>0){
            id = intent.getLongExtra("replyID",0);

        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

/*        //set visibility variable
        String[] settings = {"visible","private","test","DanielSmeels"};
        Status.Visibility visibility = Status.Visibility.Public;
*/      EditText draft_body = findViewById(R.id.draft_body);
        if(!prevStatus.equals("")){
            draft_body.setText(prevStatus);
        }
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //Send message and back out of message screen
                Statuses postUse = new Statuses(client);

                    Thread postThr = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if(id != 0){
                                    postUse.postStatus(draft_body.getText().toString(), id, null, false, null,visibility).execute();
                                }else {
                                    postUse.postStatus(draft_body.getText().toString(), null, null, false, null,visibility).execute();
                                }
                            }catch(Exception e){
                                //uhhhhhhhhhh
                                int useless = 24;
                            }
                        }
                    });

                    postThr.start();

                    try {
                        postThr.join();
                    } catch (Exception e) {
                        //literally do nothing plz
                    }


                //client.post();
                finish();

            }
        });

        FloatingActionButton privacy_settings = findViewById(R.id.privacy_settings);
        privacy_settings.setOnClickListener(new View.OnClickListener() {
                //do stuff for privacy settings

            public void onClick(View view) {
                AlertDialog.Builder settingsMenu = new AlertDialog.Builder(DraftActivity.this);
                settingsMenu.setTitle("Choose visibility settings");
                settingsMenu.setItems(settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // the user clicked on colors[which]
                        switch (which) {
                            case 0:
                                visibility = Status.Visibility.Public;
                                break;
                            case 1:
                                visibility = Status.Visibility.Direct;
                                break;
                            case 2:
                                visibility = Status.Visibility.Private;
                                break;
                            case 3:
                                visibility = Status.Visibility.Unlisted;
                                break;
                            default:
                                break;
                        }

                    }
                });
                settingsMenu.show();
            }
        });

        FloatingActionButton saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            //do stuff for privacy settings

            public void onClick(View view) {
                AlertDialog.Builder saveButtonMenu = new AlertDialog.Builder(DraftActivity.this);
                saveButtonMenu.setTitle("Choose whether to save or load a draft");
                saveButtonMenu.setItems(draftSettings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // the user clicked on colors[which]
                        switch (which) {
                            case 0:
                                //Save Draft
                                AlertDialog.Builder saveDraftMenu = new AlertDialog.Builder(DraftActivity.this);
                                saveDraftMenu.setTitle("Choose which slot to save your current draft");
                                saveDraftMenu.setItems(draftSaveSettings, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // the user clicked on colors[which]
                                        switch (which) {
                                            case 0:
                                                //Draft Slot 1

                                                //save current draft_body into sharedpreferences draft1
                                                SharedPreferences draft1 = getSharedPreferences("draft1",MODE_PRIVATE);
                                                SharedPreferences.Editor draft1edit = draft1.edit();
                                                draft1edit.putString("message",draft_body.getText().toString());
                                                draft1edit.commit();
                                                //draft_body.setText(draft1.getString("message","default draft 1"));
                                                break;
                                            case 1:
                                                //Draft Slot 2
                                                SharedPreferences draft2 = getSharedPreferences("draft2",MODE_PRIVATE);
                                                SharedPreferences.Editor draft2edit = draft2.edit();
                                                draft2edit.putString("message",draft_body.getText().toString());
                                                draft2edit.commit();
                                                //save current draft_body into sharedpreference draft2

                                                break;
                                            case 2:
                                                //Draft Slot 3
                                                SharedPreferences draft3 = getSharedPreferences("draft3",MODE_PRIVATE);
                                                SharedPreferences.Editor draft3edit = draft3.edit();
                                                draft3edit.putString("message",draft_body.getText().toString());
                                                draft3edit.commit();
                                                //save current draft_body into sharedpreferences draft3


                                                break;
                                            default:
                                                break;
                                        }

                                    }
                                });
                                saveDraftMenu.show();

                                break;
                            case 1:
                                //Load Draft
                                AlertDialog.Builder loadDraftMenu = new AlertDialog.Builder(DraftActivity.this);
                                loadDraftMenu.setTitle("Choose which slot from which to load draft");
                                loadDraftMenu.setItems(draftSaveSettings, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // the user clicked on colors[which]
                                        switch (which) {
                                            case 0:
                                                //Draft Slot 1

                                                //pull from sharedpreferences draft1 into draft_body
                                                SharedPreferences draft1 = getSharedPreferences("draft1",MODE_PRIVATE);
                                                draft_body.setText(draft1.getString("message","default draft 1"));
                                                break;
                                            case 1:
                                                //Draft Slot 2

                                                //pull from sharedpreference draft2 into draft_body
                                                SharedPreferences draft2 = getSharedPreferences("draft2",MODE_PRIVATE);
                                                draft_body.setText(draft2.getString("message","default draft 2"));
                                                break;
                                            case 2:
                                                //Draft Slot 3

                                                //pull from sharedpreferences draft3 into draft_body
                                                SharedPreferences draft3 = getSharedPreferences("draft3",MODE_PRIVATE);
                                                draft_body.setText(draft3.getString("message","default draft 3"));
                                                break;
                                            default:
                                                break;
                                        }

                                    }
                                });
                                loadDraftMenu.show();

                                break;
                            default:
                                break;
                        }

                    }
                });
                saveButtonMenu.show();
            }
        });


    }

}//dear god
