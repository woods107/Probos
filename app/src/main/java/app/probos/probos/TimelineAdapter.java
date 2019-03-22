package app.probos.probos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.method.Accounts;
import com.sys1yagi.mastodon4j.api.method.Public;
import com.sys1yagi.mastodon4j.api.method.Statuses;
import com.sys1yagi.mastodon4j.api.method.Timelines;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;


import okhttp3.OkHttpClient;

import static app.probos.probos.TimelineActivity.accessTokenStr;
import static app.probos.probos.TimelineActivity.instanceName;

public class TimelineAdapter extends
        RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

    MastodonClient client;
    Statuses statusesAPI;
    Accounts accountsAPI;

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView messageUser;
        public TextView messageMessage;
        public CircleImageView profilePicture;
        public TextView messageFullUser;
        public TextView messageTime;
        public ImageButton favoriteButton;
        public ImageButton boostButton;
        public ImageButton replyButton;
        public ImageButton deleteButton;
        public Button moreButton;
        public ImageButton muteButton;


        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            messageUser = (TextView) itemView.findViewById(R.id.displayName);
            messageMessage = (TextView) itemView.findViewById(R.id.msgMessage);
            profilePicture = (CircleImageView) itemView.findViewById(R.id.profile_full_picture);
            messageFullUser = (TextView) itemView.findViewById(R.id.fullUserName);
            messageTime = (TextView) itemView.findViewById(R.id.msgTime);
            favoriteButton = (ImageButton) itemView.findViewById(R.id.favorite_button);
            boostButton = (ImageButton) itemView.findViewById(R.id.boost_button);
            moreButton = (Button) itemView.findViewById(R.id.more_button);
            replyButton = (ImageButton) itemView.findViewById(R.id.reply_button);
            deleteButton = (ImageButton) itemView.findViewById(R.id.delete_button);
            muteButton = (ImageButton) itemView.findViewById(R.id.mute_button);
        }
    }

    private List<Status> mStatuses;
    private ArrayList<Bitmap> profilePictures = new ArrayList<>();
    private ArrayList<Boolean> isFavoritedList = new ArrayList<>();
    private ArrayList<Boolean> isAuthorOfPost = new ArrayList<>();
    private Account me;
    private ArrayList<Boolean> isBoostedList = new ArrayList<>();



    // Pass in the contact array into the constructor
    public TimelineAdapter(List<Status> statuses, String accessToken, String instanceName) {
        mStatuses = statuses;
        client = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken).build();
        statusesAPI = new Statuses(client);
        accountsAPI = new Accounts(client);
        Thread getAcct = new Thread(new Runnable(){
           @Override
            public void run(){
               try {
                   me = accountsAPI.getVerifyCredentials().execute();
               } catch (Exception e) {
                   //do nothing
                   e.printStackTrace();
                   System.out.println("hey it throws an error");
               }
           }


        });
        Thread getProfPics = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < statuses.size(); i++) {
                        URL newurl = new URL(statuses.get(i).getAccount().getAvatar());
                        Bitmap ppBitmap = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
                        profilePictures.add(ppBitmap);
                        isFavoritedList.add(statuses.get(i).isFavourited());
                        isAuthorOfPost.add(statuses.get(i).getAccount().getId() == me.getId());
                        isBoostedList.add(statuses.get(i).isReblogged());
                    }
                } catch (Exception e) {
                    //do nothing
                    System.out.println("here");
                }
            }
        });


        getAcct.start();
        try{
            getAcct.join();
        }catch(Exception e){
            int useless = 24;
        }

        getProfPics.start();
        try {
            getProfPics.join();
        } catch (Exception e) {
            //do nothing
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public TimelineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_timeline, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(TimelineAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position


        Status status = mStatuses.get(position);
        Long id = status.getId();


        if (position == mStatuses.size()-1) {
            Thread olderRetrieval = new Thread(new Runnable() {
                @Override
                public void run() {
                    // This is where older statuses are loaded and added to the end of mStatuses


                    try {
                        Range range = new Range(id, null, 50);
                        Timelines timelines = new Timelines(client);
                        Public pub = new Public(client);
                        // Use flag for which timeline is active
                        //System.out.println(viewHolder.itemView);
                        if (TimelineActivity.flag.equals("PERSONAL")) {
                            Pageable<Status> statuses = timelines.getHome(range).execute();
                            System.out.println(mStatuses.size());
                            mStatuses.addAll(statuses.getPart());
                            System.out.println(mStatuses.size());
                        } else if (TimelineActivity.flag.equals("LOCAL")) {
                            Pageable<Status> statuses = pub.getLocalPublic(range).execute();
                            mStatuses.addAll(statuses.getPart());
                        } else if (TimelineActivity.flag.equals("FEDERATED")) {
                            Pageable<Status> statuses = pub.getFederatedPublic(range).execute();
                            mStatuses.addAll(statuses.getPart());
                        }
                        //mStatuses.addAll()


                        for (int i = position+1; i < mStatuses.size(); i++) {
                            URL newurl = new URL(mStatuses.get(i).getAccount().getAvatar());
                            Bitmap ppBitmap = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
                            profilePictures.add(ppBitmap);
                            isFavoritedList.add(mStatuses.get(i).isFavourited());
                            isAuthorOfPost.add(mStatuses.get(i).getAccount().getId() == me.getId());
                            isBoostedList.add(mStatuses.get(i).isReblogged());
                        }



                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("or here?");
                    }

                    //System.out.println(mStatuses.size());

                }

            });

            olderRetrieval.start();
            try {
                olderRetrieval.join();
            } catch (Exception e) { e.printStackTrace(); }

        }// End position if

        // Set item views based on your views and data model
        TextView msgUserText = viewHolder.messageUser;
        String displayName = status.getAccount().getDisplayName();
        if (!displayName.equals("")) {
            msgUserText.setText(status.getAccount().getDisplayName());
        } else {
            msgUserText.setText(status.getAccount().getUserName());
        }

        TextView msgMsgText = viewHolder.messageMessage;
        String rawContent = status.getContent();
        String content = "";
        /*if (rawContent.length()>0) {
             content = rawContent.substring(3, rawContent.length() - 4);
        }*/
        msgMsgText.setText(Html.fromHtml(rawContent,Html.FROM_HTML_MODE_COMPACT));

        TextView msgFullUserText = viewHolder.messageFullUser;
        msgFullUserText.setText("@" + status.getAccount().getAcct());

        TextView msgTimeText = viewHolder.messageTime;
        String rawTime = status.getCreatedAt();
        String time = rawTime.substring(0,10) + " " + rawTime.substring(12,19) + " UTC";

        msgTimeText.setText(time);

        CircleImageView imageView = viewHolder.profilePicture;
        imageView.setImageBitmap(profilePictures.get(position));
        imageView.bringToFront();
        imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                intent.putExtra("id", status.getAccount().getId());
                intent.putExtra("token", TimelineActivity.accessTokenStr);
                intent.putExtra("name", TimelineActivity.instanceName);
                // Need to add a Context/ContextWrapper startActivity statement here
                try {
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }// End try/catch block
            }
        });

        ImageButton favoriteButton = viewHolder.favoriteButton;
        if (isFavoritedList.get(viewHolder.getAdapterPosition())) {
            favoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            favoriteButton.setImageResource(android.R.drawable.btn_star_big_off);
        }
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread favoritePoss = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (isFavoritedList.get(viewHolder.getAdapterPosition())){
                                statusesAPI.postUnfavourite(id).execute();
                                isFavoritedList.set(viewHolder.getAdapterPosition(), false);
                                favoriteButton.setImageResource(android.R.drawable.btn_star_big_off);
                            } else {
                                statusesAPI.postFavourite(id).execute();
                                isFavoritedList.set(viewHolder.getAdapterPosition(), true);
                                favoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                favoritePoss.start();
                /*try{
                    favoritePoss.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }??*/
            }
        });
        ImageButton deleteButton = viewHolder.deleteButton;

        if(position < isAuthorOfPost.size() && isAuthorOfPost.get(viewHolder.getAdapterPosition())){
            //show delete button
            deleteButton.setVisibility(View.VISIBLE);

        }else{
            deleteButton.setVisibility(View.INVISIBLE);
        }

        deleteButton.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View view){
               String[] settings = {"Delete","Delete and Redraft"};
               try {
                   //do delete button tree
                   AlertDialog.Builder settingsMenu = new AlertDialog.Builder(view.getContext());
                   settingsMenu.setTitle("Press back to cancel");
                   settingsMenu.setItems(settings, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           // the user clicked on colors[which]
                           switch (which) {
                               case 0:
                                   //delete
                                   AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
                                   alert.setTitle("Delete entry");
                                   alert.setMessage("Are you sure you want to delete?");
                                   alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                       public void onClick(DialogInterface dialog, int which) {
                                           // continue with delete
                                            try{
                                                new sendDelete(id).execute();
                                            }catch (Exception e){
                                                //do nothing
                                                int useless = 25;
                                            }
                                       }
                                   });
                                   alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog, int which) {
                                           // close dialog
                                           dialog.cancel();
                                       }
                                   });
                                   alert.show();
                                   break;
                               case 1:
                                   //delete and redraft
                                   //System.out.println("A weapon to surpass metal gear");
                                   String prevStatus = msgMsgText.getText().toString();
                                   Intent draft = new Intent(view.getContext() , DraftActivity.class);
                                   try {
                                       draft.putExtra("instanceName",instanceName);
                                       draft.putExtra("access",accessTokenStr);
                                       draft.putExtra("prevStatus",prevStatus);

                                       view.getContext().startActivity(draft);
                                       //TODO: This is the temporary placement of delete for Sprint 2. This does not cover the case where they select delete and redraft and back out of the activity
                                       try{
                                           new sendDelete(id).execute();
                                       }catch (Exception e){
                                           //do nothing
                                           int useless = 25;
                                       }
                                   } catch (Exception e) {
                                       e.printStackTrace();
                                   }
                                   //take the contents of this status and hand it off into the text field of draft

                                   break;
                               default:
                                   break;
                           }

                       }
                   });
                   settingsMenu.show();
               }catch(Exception e){
                   //uhhhhhhhhhh
                   int useless = 24;
               }
           }
        });
        ImageButton muteButton = viewHolder.muteButton;
        //Looper.prepare();
        muteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String[] muteSettings = {"Mute","Cancel"};
                //Looper.prepare();

                        try {


                            AlertDialog.Builder muteMenu = new AlertDialog.Builder(view.getContext());
                            muteMenu.setTitle("Are you sure you would like to mute this user? This cannot be undone.");
                            //Looper.prepare();
                            muteMenu.setItems(muteSettings, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // the user clicked on colors[which]
                                    switch (which) {
                                        case 0:
                                            //mute
                                            Thread mutePoss = new Thread(new Runnable() {
                                                                @Override
                                                               public void run() {
                                                                        try {
                                                                            accountsAPI.postMute(status.getAccount().getId()).execute();
                                                                        }catch(Exception e){
                                                                            //do nothing or freak out, depending on the mood
                                                                            e.printStackTrace();
                                                                        }
                                                                }
                                            });
                                            mutePoss.start();
                                            break;
                                        case 1:
                                            //do nothing
                                            break;
                                        default:
                                            break;
                                    }

                                }
                            });
                            muteMenu.show();
                                //add turning button on/off

                        }catch (Exception e) {
                            e.printStackTrace();
                        }


            }
        });

        ImageButton boostButton = viewHolder.boostButton;
        if(isBoostedList.get(viewHolder.getAdapterPosition())){
            boostButton.setImageResource(android.R.drawable.checkbox_on_background);//what da image
        } else {
            boostButton.setImageResource(android.R.drawable.ic_menu_rotate);
        }
        boostButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Thread boostPoss = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(isBoostedList.get(viewHolder.getAdapterPosition())){
                                statusesAPI.postUnreblog(id).execute();
                                isBoostedList.set(viewHolder.getAdapterPosition(), false);
                                boostButton.setImageResource(android.R.drawable.ic_menu_rotate);
                                //add turning button on/off
                            }else{
                                statusesAPI.postReblog(id).execute();
                                isBoostedList.set(viewHolder.getAdapterPosition(), true);
                                boostButton.setImageResource(android.R.drawable.checkbox_on_background);
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                boostPoss.start();
                /*try{
                    boostPoss.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
            }
        });

        ImageButton replyButton = viewHolder.replyButton;
        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent draft = new Intent(view.getContext(), DraftActivity.class);
                try {
                    draft.putExtra("instanceName",TimelineActivity.instanceName);
                    draft.putExtra("access",TimelineActivity.accessTokenStr);
                    draft.putExtra("replyID", status.getId());
                    view.getContext().startActivity(draft);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        Button moreBtn = viewHolder.moreButton;
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ExpandedStatusActivity.class);
                intent.putExtra("id", status.getId());
                intent.putExtra("token", TimelineActivity.accessTokenStr);
                intent.putExtra("name", TimelineActivity.instanceName);
                // Need to add a Context/ContextWrapper startActivity statement here
                try {
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }// End try/catch block
            }
        });

    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mStatuses.size();
    }

    public static Calendar toCalendar(final String iso8601string)
            throws ParseException {
        Calendar calendar = GregorianCalendar.getInstance();
        String s = iso8601string.replace("Z", "+00:00");
        try {
            s = s.substring(0, 22) + s.substring(23);  // to get rid of the ":"
        } catch (IndexOutOfBoundsException e) {
            throw new ParseException("Invalid length", 0);
        }
        Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(s);
        calendar.setTime(date);
        return calendar;
    }
    private class sendDelete extends AsyncTask<Void, Void, Void>
    {
        Long id;

        public sendDelete(Long id) {
            this.id = id;
        }

        protected Void doInBackground(Void... param) {
            try {
                statusesAPI.deleteStatus(id);
            } catch (Exception e){
                throw new IllegalArgumentException();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

        }
    }
}