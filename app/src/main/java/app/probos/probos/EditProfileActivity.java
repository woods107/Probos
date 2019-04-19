package app.probos.probos;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.entity.Relationship;
import com.sys1yagi.mastodon4j.api.method.Accounts;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static app.probos.probos.DraftActivity.REQUEST_GET_SINGLE_FILE;

//import android.view.LayoutInflater;

public class EditProfileActivity extends AppCompatActivity {

    // Get these three things before starting anything

    String instanceName;
    String accessToken;
    Long acctId;

    Accounts tmpAcct;
    Account currAcct;
    Bitmap ppBitmap;
    Bitmap bannerBitmap;
    Relationship relationship;
    Boolean follow;
    MastodonClient userClient;

    File prof = null;
    File head = null;
    /*String profImg = null;
    String headImg = null;*/

    RequestBody requestBodyAv = null;
    RequestBody requestBodyHead = null;

    public static final int REQUEST_GET_PROFILE_FILE = 13;
    public static final int REQUEST_GET_HEADER_FILE = 23;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // This line here should enable a back button on the action bar
        // Once the UI is more developed, it will be useful to have
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent currIntent = getIntent();
        acctId = currIntent.getLongExtra("id", 0);
        instanceName = currIntent.getStringExtra("name");
        accessToken = currIntent.getStringExtra("token");

        userClient = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken).build();
        tmpAcct = new Accounts(userClient);

        new setUpViews().execute();

    }

    private class setUpViews extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... param) {
            try {
                currAcct = tmpAcct.getAccount(acctId).execute();
                URL newPP = new URL(currAcct.getAvatar());
                URL newBanner = new URL(currAcct.getHeader());
                ppBitmap = BitmapFactory.decodeStream(newPP.openConnection().getInputStream());
                bannerBitmap = BitmapFactory.decodeStream(newBanner.openConnection().getInputStream());
                ArrayList<Long> accounts = new ArrayList<>();
                accounts.add(acctId);
                relationship = tmpAcct.getRelationships(accounts).execute().get(0);
                follow = relationship.isFollowing();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void param) {
            EditText displayName = findViewById(R.id.displayNameEditText);
            String displayNameText = currAcct.getDisplayName();
            if (!displayNameText.equals("")) {
                displayName.setText(currAcct.getDisplayName());
            } else {
                displayName.setText(currAcct.getUserName());
            }

            TextView userName = findViewById(R.id.fullUserName);
            userName.setText("@" + currAcct.getAcct());


            EditText profileBio = findViewById(R.id.profileBioEditText);
            profileBio.setText(Html.fromHtml(currAcct.getNote(), Html.FROM_HTML_MODE_COMPACT));

            ImageView profBanner = findViewById(R.id.profile_banner);
            profBanner.setImageBitmap(bannerBitmap);

            profBanner.setOnClickListener(new View.OnClickListener() {

                // Grab Files based on user selection in order to prepare to send them

                public void onClick(View view) {

                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GET_HEADER_FILE);

                }

            });// End profBanner onClickListener

            CircleImageView imageView = findViewById(R.id.profile_full_picture);
            imageView.setImageBitmap(ppBitmap);
            imageView.bringToFront();

            imageView.setOnClickListener(new View.OnClickListener() {

                // Grab Files based on user selection in order to prepare to send them

                public void onClick(View view) {

                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GET_PROFILE_FILE);

                }

            });// End imageView onClickListener


            /*
            *

            TODO create a String to hold base64 encoded image selected for both profile icon and banner, both default to NULL
            TODO substitute those strings into the updateCredential call

            FloatingActionButton attach_media = findViewById(R.id.media_attachment);
            attach_media.setOnClickListener(new View.OnClickListener() {

            // Grab Files based on user selection in order to prepare to send them

            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GET_SINGLE_FILE);

            }

            });// End attach_media onClickListener

            *
            * */


        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {



        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_menu_done:

                EditText displayName = (EditText) findViewById(R.id.displayNameEditText);
                EditText profileBio = (EditText) findViewById(R.id.profileBioEditText);




                try {

                    if (prof != null) {
                        requestBodyAv = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("avatar", prof.getName(), RequestBody.create(MediaType.parse("*/*"), prof))
                            .build();

                        //byte[] profBytes = FileUtils.readFileToByteArray(prof);
                        //profImg = "avatar:image/*;base64, " + enc.encodeToString(profBytes);
                    }

                    if (head != null) {
                        requestBodyHead = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("header", head.getName(), RequestBody.create(MediaType.parse("*/*"), head))
                                .build();
                        /*byte[] headBytes = FileUtils.readFileToByteArray(head);
                        headImg = "header:image/*;base64, " + enc.encodeToString(headBytes);*/
                    }

                } catch (Exception e) { e.printStackTrace(); }


                Thread profileThr = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            if (requestBodyAv != null && requestBodyHead != null) {
                                userClient.patch("accounts/update_credentials", requestBodyAv);
                                userClient.patch("accounts/update_credentials", requestBodyHead);
                                //tmpAcct.updateCredential(displayName.getText().toString(), profileBio.getText().toString(), profImg, headImg).execute();
                            } else if (requestBodyAv != null) {
                                userClient.patch("accounts/update_credentials", requestBodyAv);
                                //tmpAcct.updateCredential(displayName.getText().toString(), profileBio.getText().toString(), profImg, null).execute();
                            } else if (requestBodyHead != null) {
                                userClient.patch("accounts/update_credentials", requestBodyHead);
                                //tmpAcct.updateCredential(displayName.getText().toString(), profileBio.getText().toString(), null, headImg).execute();
                            } //else {

                            tmpAcct.updateCredential(displayName.getText().toString(), profileBio.getText().toString(), null, null).execute();

                            //}

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                profileThr.start();

                try {
                    profileThr.join();
                } catch (Exception e) {
                    //literally do nothing plz
                    e.printStackTrace();
                }

                onBackPressed();
                return true;

        }// End switch

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {

            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_GET_PROFILE_FILE || requestCode == REQUEST_GET_HEADER_FILE) {

                    Uri selectedImageUri = data.getData();


                    String filePath = "";
                    String fileId = DocumentsContract.getDocumentId(selectedImageUri);
                    // Split at colon, use second item in the array
                    String id = fileId.split(":")[1];
                    String[] column = {MediaStore.Images.Media.DATA};
                    String selector = MediaStore.Images.Media._ID + "=?";
                    Cursor cursor = this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            column, selector, new String[]{id}, null);
                    int columnIndex = cursor.getColumnIndex(column[0]);
                    if (cursor.moveToFirst()) {
                        filePath = cursor.getString(columnIndex);
                    }
                    cursor.close();

                    if (requestCode == REQUEST_GET_PROFILE_FILE) { prof = new File(filePath); }
                    else { head = new File(filePath); }

                    //TODO determine how exactly to convert a file into a base64 encoded String to submit for user icon and banner

                    // This line is the most "correct" but not necessarily for this context
                    //MultipartBody.Part limb = MultipartBody.Part.createFormData("file", f.getName(), RequestBody.create(MediaType.parse("*/*"), f));

                }// End requestCode check
            }// End RESULT_OK if

        } catch (Exception e) {
            e.printStackTrace();
        }
    }// End onActivityResult

}
