package app.probos.probos;

import android.app.AlertDialog;
import android.app.AlertDialog.*;
import android.content.DialogInterface;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        client = new MastodonClient.Builder(intent.getStringExtra("instanceName"), new OkHttpClient.Builder(), new Gson()).accessToken(intent.getStringExtra("access")).build();


        if(intent.getLongExtra("replyID",0)>0){
            id = intent.getLongExtra("replyID",0);

        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //set visibility variable
        String[] settings = {"visible","private","test","DanielSmeels"};
        Status.Visibility visibility = Status.Visibility.Public;

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //Send message and back out of message screen
                Statuses postUse = new Statuses(client);
                EditText draft_body = findViewById(R.id.draft_body);

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
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

        FloatingActionButton privacy_settings = findViewById(R.id.privacy_settings);
        privacy_settings.setOnClickListener(new View.OnClickListener() {
                //do stuff for privacy settings


            AlertDialog.Builder settingsMenu = new AlertDialog.Builder(DraftActivity.this);
            settingsMenu.setTitle("Choose visibility settings");
            settingsMenu.setItems(settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    // the user clicked on colors[which]
                    switch(which){
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

        });

}
