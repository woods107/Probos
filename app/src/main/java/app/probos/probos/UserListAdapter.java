package app.probos.probos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.entity.Status;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class UserListAdapter extends
        RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView messageUser;
        public ImageView profilePicture;
        public TextView messageFullUser;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            messageUser = (TextView) itemView.findViewById(R.id.msgUser);
            profilePicture = (ImageView) itemView.findViewById(R.id.profile_picture);
            messageFullUser = (TextView) itemView.findViewById(R.id.msgFullUser);
        }
    }

    private List<Account> mAccounts;
    private ArrayList<Bitmap> profilePictures = new ArrayList<>();


    // Pass in the contact array into the constructor
    public UserListAdapter(List<Account> accts) {
        mAccounts = accts;
        Thread getProfPics = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < accts.size(); i++) {
                        URL newurl = new URL(accts.get(i).getAvatar());
                        Bitmap ppBitmap = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
                        profilePictures.add(ppBitmap);
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
    public UserListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(UserListAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Account gAcct = mAccounts.get(position);

        // Set item views based on your views and data model
        TextView msgUserText = viewHolder.messageUser;
        msgUserText.setText(gAcct.getDisplayName());

        /*
        TextView msgMsgText = viewHolder.messageMessage;
        String rawContent = status.getContent();
        String content = rawContent.substring(3,rawContent.length()-4);
        msgMsgText.setText(Html.fromHtml(content,0));
        */

        TextView msgFullUserText = viewHolder.messageFullUser;
        msgFullUserText.setText("@" + gAcct.getAcct());

        /*
        TextView msgTimeText = viewHolder.messageTime;
        String rawTime = status.getCreatedAt();
        String time = rawTime.substring(0,10) + " " + rawTime.substring(12,19) + " UTC";

        msgTimeText.setText(time);
        */

        ImageView imageView = viewHolder.profilePicture;
        imageView.getLayoutParams().height = 80;
        imageView.getLayoutParams().width = 80;
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setImageBitmap(profilePictures.get(position));

    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mAccounts.size();
    }

    /*
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
    }*/

}