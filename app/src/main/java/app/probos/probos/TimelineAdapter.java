package app.probos.probos;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sys1yagi.mastodon4j.api.entity.Status;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class TimelineAdapter extends
        RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

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
        }
    }

    private List<Status> mStatuses;
    private ArrayList<Bitmap> profilePictures = new ArrayList<>();


    // Pass in the contact array into the constructor
    public TimelineAdapter(List<Status> statuses) {
        mStatuses = statuses;
        Thread getProfPics = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < statuses.size(); i++) {
                        URL newurl = new URL(statuses.get(i).getAccount().getAvatar());
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
        imageView.bringToFront();
        imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), UserListActivity.class);
                intent.putExtra("id", status.getAccount().getId());
                intent.putExtra("name", TimelineActivity.accessTokenStr);
                intent.putExtra("token", TimelineActivity.instanceName);
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