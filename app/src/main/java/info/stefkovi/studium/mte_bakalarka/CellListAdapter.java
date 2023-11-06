package info.stefkovi.studium.mte_bakalarka;


import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import info.stefkovi.studium.mte_bakalarka.helpers.DatabaseHelper;
import info.stefkovi.studium.mte_bakalarka.helpers.DatabaseStructureHelper;
import info.stefkovi.studium.mte_bakalarka.model.CellInfoApiModel;


public class CellListAdapter extends RecyclerView.Adapter<CellListAdapter.ViewHolder> {

    private ArrayList<CellInfoApiModel> cells;
    private DatabaseHelper db;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textView = (TextView) view.findViewById(R.id.textView);
        }

        public TextView getTextView() {
            return textView;
        }
    }

    private void loadData() {
        Gson gson = new Gson();
        long last = db.getLastEventId();
        String data = db.getEventDataCells(last);

        Type cellListType = new TypeToken<ArrayList<CellInfoApiModel>>(){}.getType();
        cells = gson.fromJson(data, cellListType);

    }

    public CellListAdapter(Context ctx) {
        db = new DatabaseHelper(ctx);
        loadData();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.view_row_cell_list, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        /*
        dataCursor.moveToPosition(position);
        String cells = dataCursor.getString(dataCursor.getColumnIndexOrThrow(DatabaseStructureHelper.EVENT_COLUMN_DATA_CELLS));
         */
        viewHolder.getTextView().setText(cells.get(position).toString());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cells.size();
    }
}

