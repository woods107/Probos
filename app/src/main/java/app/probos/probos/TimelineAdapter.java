package app.probos.probos;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sys1yagi.mastodon4j.api.entity.Status;

import java.util.List;

public class TimelineAdapter extends
        RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView messageUser;
        public TextView messageMessage;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            messageUser = (TextView) itemView.findViewById(R.id.msgUser);
            messageMessage = (TextView) itemView.findViewById(R.id.msgMessage);
        }
    }

    private List<Status> mStatuses;

    // Pass in the contact array into the constructor
    public TimelineAdapter(List<Status> statuses) {
        mStatuses = statuses;
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
        TextView textView = viewHolder.messageUser;
        textView.setText(status.getAccount().getDisplayName());
        TextView textView1 = viewHolder.messageMessage;
        textView1.setText(status.getContent());
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mStatuses.size();
    }
}