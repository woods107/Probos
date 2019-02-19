package app.probos.probos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.MastodonRequest;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.method.Favourites;
import com.sys1yagi.mastodon4j.api.method.Statuses;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;

public class TimelineAdapter extends
        RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

    MastodonClient client;
    Statuses statusesAPI;

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



        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            messageUser = (TextView) itemView.findViewById(R.id.msgUser);
            messageMessage = (TextView) itemView.findViewById(R.id.msgMessage);
            profilePicture = (CircleImageView) itemView.findViewById(R.id.profile_picture);
            messageFullUser = (TextView) itemView.findViewById(R.id.msgFullUser);
            messageTime = (TextView) itemView.findViewById(R.id.msgTime);
            favoriteButton = (ImageButton) itemView.findViewById(R.id.favorite_button);
            boostButton = (ImageButton) itemView.findViewById(R.id.boost_button);
        }
    }

    private List<Status> mStatuses;
    private ArrayList<Bitmap> profilePictures = new ArrayList<>();
    private ArrayList<Boolean> isFavoritedList = new ArrayList<>();



    // Pass in the contact array into the constructor
    public TimelineAdapter(List<Status> statuses, String accessToken, String instanceName) {
        mStatuses = statuses;
        client = new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken).build();
        statusesAPI = new Statuses(client);
        Thread getProfPics = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < statuses.size(); i++) {
                        URL newurl = new URL(statuses.get(i).getAccount().getAvatar());
                        Bitmap ppBitmap = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
                        profilePictures.add(ppBitmap);
                        isFavoritedList.add(statuses.get(i).isFavourited());
                    }
                } catch (Exception e) {
                    //do nothing
                }
            }
        });

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
        if (rawContent.length()>0) {
             content = rawContent.substring(3, rawContent.length() - 4);
        }
        msgMsgText.setText(Html.fromHtml(rawContent,Html.FROM_HTML_MODE_COMPACT));

        TextView msgFullUserText = viewHolder.messageFullUser;
        msgFullUserText.setText("@" + status.getAccount().getAcct());

        TextView msgTimeText = viewHolder.messageTime;
        String rawTime = status.getCreatedAt();
        String time = rawTime.substring(0,10) + " " + rawTime.substring(12,19) + " UTC";

        msgTimeText.setText(time);


        CircleImageView imageView = viewHolder.profilePicture;
        imageView.setImageBitmap(profilePictures.get(position));
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ImageButton favoriteButton = viewHolder.favoriteButton;
        if (status.isFavourited()) {
            favoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
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
                            throw new IndexOutOfBoundsException();
                        }
                    }
                });

                favoritePoss.start();
            }
        });
        ImageButton boostButton = viewHolder.boostButton;
        if(status.isReblogged()){
            boostButton.setImageResource(android.R.drawable.checkbox_on_background);//what da image
        }
        boostButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Thread favoritePoss = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(status.isReblogged()){
                                statusesAPI.postUnreblog(id).execute();
                                boostButton.setImageResource(android.R.drawable.ic_menu_rotate);
                                //add turning button on/off
                            }else{
                                statusesAPI.postReblog(id).execute();
                                boostButton.setImageResource(android.R.drawable.checkbox_on_background);
                            }
                        }catch (Exception e) {
                            throw new IndexOutOfBoundsException();
                        }
                    }
                });
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
}