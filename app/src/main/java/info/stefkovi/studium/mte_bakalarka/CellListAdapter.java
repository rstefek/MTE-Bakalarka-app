package info.stefkovi.studium.mte_bakalarka;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import info.stefkovi.studium.mte_bakalarka.helpers.DatabaseHelper;
import info.stefkovi.studium.mte_bakalarka.helpers.TypeTokenHelper;
import info.stefkovi.studium.mte_bakalarka.model.CellInfoApiModel;


public class CellListAdapter extends RecyclerView.Adapter<CellListAdapter.ViewHolder> {

    private ArrayList<CellInfoApiModel> cells;
    private DatabaseHelper db;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView cellId;
        private final TextView cellLac;
        private final TextView cellTac;
        private final TextView signalAsu;
        private final TextView signalDbm;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            cellId = (TextView) view.findViewById(R.id.cellId);
            cellLac = (TextView) view.findViewById(R.id.cellLac);
            cellTac = (TextView) view.findViewById(R.id.cellTac);
            signalAsu = (TextView) view.findViewById(R.id.signalAsu);
            signalDbm = (TextView) view.findViewById(R.id.signalDbm);
        }

        public TextView getTextViewCellId() {
            return cellId;
        }
        public TextView getTextViewSignalAsu() {
            return signalAsu;
        }
        public TextView getTextViewSignalDbm() {
            return signalDbm;
        }
        public TextView getTextViewCellLac() {
            return cellLac;
        }

        public TextView getTextViewCellTac() {
            return cellTac;
        }

    }

    private void loadData() {
        Gson gson = new Gson();
        long last = db.getLastEventId();
        cells = db.getEventDataCells(last);
    }

    public CellListAdapter(Context ctx) {
        db = DatabaseHelper.getInstance(ctx);
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

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        CellInfoApiModel cell = cells.get(position);

        viewHolder.getTextViewCellId().setText(String.valueOf(cell.identity.cid));
        viewHolder.getTextViewCellLac().setText(String.valueOf(cell.identity.lac));
        viewHolder.getTextViewCellTac().setText(String.valueOf(cell.identity.tac));
        viewHolder.getTextViewSignalAsu().setText(String.valueOf(cell.signal.signal_asu));
        viewHolder.getTextViewSignalDbm().setText(String.valueOf(cell.signal.signal_dbm));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cells.size();
    }
}

