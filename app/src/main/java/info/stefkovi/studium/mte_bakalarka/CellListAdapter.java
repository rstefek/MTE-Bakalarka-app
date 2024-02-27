package info.stefkovi.studium.mte_bakalarka;

import static info.stefkovi.studium.mte_bakalarka.helpers.ValueHelper.intToStringWithNA;
import static info.stefkovi.studium.mte_bakalarka.helpers.ValueHelper.longToStringWithNA;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import info.stefkovi.studium.mte_bakalarka.helpers.DatabaseHelper;
import info.stefkovi.studium.mte_bakalarka.model.CellInfoApiModel;


public class CellListAdapter extends RecyclerView.Adapter<CellListAdapter.ViewHolder> {

    private ArrayList<CellInfoApiModel> cells;
    private DatabaseHelper db;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView cellGen;
        private final TextView cellId;
        private final TextView cellLac;
        private final TextView cellTac;
        private final TextView signalAsu;
        private final TextView signalDbm;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            cellGen = (TextView) view.findViewById(R.id.cellGen);
            cellId = (TextView) view.findViewById(R.id.cellId);
            cellLac = (TextView) view.findViewById(R.id.cellLac);
            cellTac = (TextView) view.findViewById(R.id.cellTac);
            signalAsu = (TextView) view.findViewById(R.id.signalAsu);
            signalDbm = (TextView) view.findViewById(R.id.signalDbm);
        }

        public TextView getTextViewCellId() {
            return cellId;
        }
        public TextView getTextViewCellGen() {
            return cellGen;
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

    private void loadData(long currId) {
        cells = db.getEventDataCells(currId);
    }

    public CellListAdapter(Context ctx, long currId) {
        db = DatabaseHelper.getInstance(ctx);
        loadData(currId);
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

        viewHolder.getTextViewCellGen().setText(cell.network_type);
        viewHolder.getTextViewCellId().setText(longToStringWithNA(cell.identity.cid));
        viewHolder.getTextViewCellLac().setText(intToStringWithNA(cell.identity.lac));
        viewHolder.getTextViewCellTac().setText(intToStringWithNA(cell.identity.tac));
        viewHolder.getTextViewSignalAsu().setText(String.valueOf(cell.signal.signal_asu));
        viewHolder.getTextViewSignalDbm().setText(String.valueOf(cell.signal.signal_dbm));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cells.size();
    }
}

