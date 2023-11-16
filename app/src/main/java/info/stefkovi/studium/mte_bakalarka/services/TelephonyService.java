package info.stefkovi.studium.mte_bakalarka.services;

import android.annotation.SuppressLint;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import java.util.ArrayList;
import java.util.List;

import info.stefkovi.studium.mte_bakalarka.model.CellInfoApiModel;

public class TelephonyService {

    private TelephonyManager _manager;
    private CellInfoListener _cellListener;

    private static class CellInfoListener extends PhoneStateListener {
        List<CellInfo> cellInfo;
        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            this.cellInfo = cellInfo;
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
        }
    }

    @SuppressLint("MissingPermission")
    public TelephonyService(TelephonyManager manager) {
        this._manager = manager;
        getAllCellInfo();

         /*
        _cellListener = new CellInfoListener();
        _manager.listen(_cellListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        */
    }

    @SuppressLint("MissingPermission")
    public List<CellInfoApiModel> getAllCellInfo() {
        List<CellInfo> allCells = _manager.getAllCellInfo();
        List<CellInfoApiModel> apiCells = new ArrayList<>();

        for (CellInfo cell : allCells) {
            CellInfoApiModel apiCell = new CellInfoApiModel();

            if(cell instanceof CellInfoGsm) {
                CellInfoGsm cellGsm = (CellInfoGsm) cell;
                CellIdentityGsm identityGsm = cellGsm.getCellIdentity();
                CellSignalStrengthGsm signalGsm = cellGsm.getCellSignalStrength();

                apiCell.registered = cellGsm.isRegistered();
                apiCell.connection_status = cellGsm.getCellConnectionStatus();

                //Identita
                apiCell.identity.mnc = identityGsm.getMncString();
                apiCell.identity.arfcn = identityGsm.getArfcn();
                apiCell.identity.cid = identityGsm.getCid();
                apiCell.identity.lac = identityGsm.getLac();
                apiCell.identity.bs = identityGsm.getBsic();

                //Síla signálu
                apiCell.signal.signal_dbm = signalGsm.getDbm();
                apiCell.signal.signal_asu = signalGsm.getAsuLevel();
                apiCell.signal.timing_advance = signalGsm.getTimingAdvance();
                apiCell.signal.level = signalGsm.getLevel();
            }
            else if(cell instanceof CellInfoLte) {
                CellInfoLte cellLte = (CellInfoLte) cell;
                CellIdentityLte identityLte = cellLte.getCellIdentity();
                CellSignalStrengthLte signalLte = cellLte.getCellSignalStrength();

                apiCell.registered = cellLte.isRegistered();
                apiCell.connection_status = cellLte.getCellConnectionStatus();

                //Identita
                apiCell.identity.mnc = identityLte.getMncString();
                apiCell.identity.arfcn = identityLte.getEarfcn();
                apiCell.identity.cid = identityLte.getCi();
                apiCell.identity.tac = identityLte.getTac();
                apiCell.identity.pci = identityLte.getPci();
                apiCell.identity.bandwidth = identityLte.getBandwidth();

                //Síla signálu
                apiCell.signal.signal_dbm = signalLte.getDbm();
                apiCell.signal.signal_asu = signalLte.getAsuLevel();
                apiCell.signal.channel_quality = signalLte.getCqi();
                apiCell.signal.rsrp_dbm = signalLte.getRsrp();
                apiCell.signal.rsrq_dbm = signalLte.getRsrq();
                apiCell.signal.rssnr_db = signalLte.getRssnr();
                apiCell.signal.timing_advance = signalLte.getTimingAdvance();
                apiCell.signal.level = signalLte.getLevel();
            }
            else if(cell instanceof CellInfoWcdma) {
                CellInfoWcdma cellWCdma = (CellInfoWcdma) cell;
                CellIdentityWcdma identityWCdma = cellWCdma.getCellIdentity();
                CellSignalStrengthWcdma signalWCdma = cellWCdma.getCellSignalStrength();

                apiCell.registered = cellWCdma.isRegistered();
                apiCell.connection_status = cellWCdma.getCellConnectionStatus();

                //Identita
                apiCell.identity.mnc = identityWCdma.getMncString();
                apiCell.identity.arfcn = identityWCdma.getUarfcn();
                apiCell.identity.cid = identityWCdma.getCid();
                apiCell.identity.lac = identityWCdma.getLac();

                //Síla signálu
                apiCell.signal.signal_dbm = signalWCdma.getDbm();
                apiCell.signal.signal_asu = signalWCdma.getAsuLevel();
                apiCell.signal.level = signalWCdma.getLevel();
            }
            apiCells.add(apiCell);
        }
        return apiCells;
    }

    public String GetBasicDeviceInfo() {
        return "";
    }

}
