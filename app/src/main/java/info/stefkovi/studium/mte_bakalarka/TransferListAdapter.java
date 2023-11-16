package info.stefkovi.studium.mte_bakalarka;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;

import info.stefkovi.studium.mte_bakalarka.helpers.DatabaseHelper;
import info.stefkovi.studium.mte_bakalarka.model.EventModel;


public class TransferListAdapter extends RecyclerView.Adapter<TransferListAdapter.ViewHolder> {

    private ArrayList<EventModel> events;
    private DatabaseHelper db;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView eventDate;
        private final TextView eventState;
        private final TextView eventUid;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            eventDate = (TextView) view.findViewById(R.id.eventDate);
            eventState = (TextView) view.findViewById(R.id.eventState);
            eventUid = (TextView) view.findViewById(R.id.eventUid);
        }

        public TextView getTextViewEventDate() {
            return eventDate;
        }
        public TextView getTextViewEventState() {
            return eventState;
        }
        public TextView getTextViewEventUid() {
            return eventUid;
        }

    }

    private void loadData() {
        events = db.getAllEvents();
    }

    public void reloadData() {
        loadData();
        this.notifyDataSetChanged();
    }

    public TransferListAdapter(Context ctx) {
        db = DatabaseHelper.getInstance(ctx);
        loadData();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.view_row_transfer_list, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        EventModel event = events.get(position);

        viewHolder.getTextViewEventDate().setText(String.valueOf(event.happened.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))));
        switch (event.sent) {
            case 1:
                viewHolder.getTextViewEventState().setBackgroundColor(Color.GREEN);
                break;
            case 0:
            default:
                viewHolder.getTextViewEventState().setBackgroundColor(Color.YELLOW);
                break;
        }
        viewHolder.getTextViewEventUid().setText(event.uid != null ? event.uid.toString() : "N/A");
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return events.size();
    }
}

