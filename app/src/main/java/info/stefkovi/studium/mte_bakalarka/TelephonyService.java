package info.stefkovi.studium.mte_bakalarka;

import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.util.List;

public class TelephonyService {

    private TelephonyManager _manager;
    private CellInfoListener _cellListener;

    private static class CellInfoListener extends PhoneStateListener {
        List<CellInfo> cellInfo;
        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            this.cellInfo = cellInfo;
        }
    }

    public TelephonyService(TelephonyManager manager) {
        this._manager = manager;
        _cellListener = new CellInfoListener();
        _manager.listen(_cellListener, PhoneStateListener.LISTEN_CELL_INFO);
    }

    public String GetBasicDeviceInfo() {
        return "";
    }

}
