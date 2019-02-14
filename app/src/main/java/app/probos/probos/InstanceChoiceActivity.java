package app.probos.probos;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.sys1yagi.mastodon4j.api.entity.Attachment;
import com.sys1yagi.mastodon4j.rx.*;
import com.sys1yagi.mastodon4j.*;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.Scope;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.entity.auth.AccessToken;
import com.sys1yagi.mastodon4j.api.entity.auth.AppRegistration;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Apps;
import com.sys1yagi.mastodon4j.api.method.Timelines;

import org.w3c.dom.Text;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class InstanceChoiceActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    //private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    boolean signInSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instance_choice);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        //mPasswordView = (EditText) findViewById(R.id.password);
        /*mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });*/

        Button mEmailSignInButton = (Button) findViewById(R.id.getauth_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (attemptLogin()) {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(InstanceChoiceActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(InstanceChoiceActivity.this);
                    }
                    builder.setTitle("Invalid instance")
                            .setMessage("The instance provided was invalid or unavailable. Please try again.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.signin_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    actualSignIn();
                } catch (Exception e) {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(InstanceChoiceActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(InstanceChoiceActivity.this);
                    }
                    builder.setTitle("Invalid authentication code")
                            .setMessage("The authentication code provided was invalid. Please try again.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    MastodonClient client;
    MastodonClient authClient;
    AppRegistration registration;
    String instanceName;
    String authCode;
    Apps apps;
    String clientId;
    String clientSecret;

    private WebView mWebview ;

    private boolean attemptLogin() {
        //if (mAuthTask != null) {
          //  return;
        //}
        TextView login = findViewById(R.id.email);
        instanceName = login.getText().toString();
        Handler mHandler=new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                client = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson()).build();
                apps = new Apps(client);
                try {
                    registration = apps.createApp(
                            "Probos",
                            "urn:ietf:wg:oauth:2.0:oob",
                            new Scope(Scope.Name.ALL),
                            "https://probos.app"
                    ).execute();

                    System.out.println("instance=" + registration.getInstanceName());
                    Log.d("LoginActivity",registration.getInstanceName());
                    System.out.println("client_id=" + registration.getClientId());
                    Log.d("LoginActivity",registration.getClientId());
                    clientId = registration.getClientId();
                    System.out.println("client_secret=" + registration.getClientSecret());
                    Log.d("LoginActivity",registration.getClientSecret());
                    clientSecret = registration.getClientSecret();
                } catch (Mastodon4jRequestException e) {
                    //int statusCode = e.getResponse().code();
                    // error handling.
                    signInSuccess = true;
                    return;
                }

                String clientId = registration.getClientId();
                String url = apps.getOAuthUrl(clientId, new Scope(Scope.Name.ALL),"urn:ietf:wg:oauth:2.0:oob");

                // url like bellow
                // https://:instance_name/oauth/authorize?client_id=:client_id&redirect_uri=:redirect_uri&response_type=code&scope=read
                // open url and OAuth login and get auth code
                Intent intent = new Intent(InstanceChoiceActivity.this, WebViewActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);

// 	accessToken needs to be saved.

            }
        });

        thread.start();



        // Reset errors.
        mEmailView.setError(null);
        //mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        //String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
      /*  if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }*/

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } /*else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        } */

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } /*else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }*/
        return signInSuccess;
    }
    AccessToken accessToken;

    private void actualSignIn() throws Mastodon4jRequestException {
        TextView authField = findViewById(R.id.auth_text);
        authCode = authField.getText().toString();
        MastodonRequest<AccessToken> accessTokenMastodonRequest = apps.getAccessToken(clientId, clientSecret, "urn:ietf:wg:oauth:2.0:oob", authCode, "authorization_code");

        Thread finalAuthThr = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    accessToken = accessTokenMastodonRequest.execute();
                   /* String accessTokenStr = accessToken.getAccessToken();
                    authClient = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson()).accessToken(accessTokenStr).build();
                    Timelines timelines = new Timelines(authClient);
                    Pageable<Status> statuses = timelines.getHome(new Range()).execute();
                    List<Status> statusList = statuses.getPart();
                    for (int i = 0; i < statusList.size(); i++) {
                        Log.d("statusUser", statusList.get(i).getAccount().getDisplayName());
                        Log.d("statusMSG", statusList.get(i).getContent());
                        Log.d("profilePic", statusList.get(i).getAccount().getAvatar());
                        List<Attachment> attach = statusList.get(i).getMediaAttachments();
                    }*/
                } catch (Mastodon4jRequestException e) {
                    e.printStackTrace();
                }

            }
        });

        finalAuthThr.start();

        try {
            finalAuthThr.join();
            Intent intent = new Intent(this, TimelineActivity.class);
            intent.putExtra("accesstoken", accessToken.getAccessToken());
            intent.putExtra("instancename",instanceName);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(InstanceChoiceActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
  /*  public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

    }
    */

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleOAuthResponse(intent);
    }

    private void handleOAuthResponse(Intent intent) {
        Uri uri = intent.getData();
        if (uri != null && uri.toString().startsWith("https://" + instanceName)) {
            authCode = uri.getQueryParameter("code");
        }
    }

}

