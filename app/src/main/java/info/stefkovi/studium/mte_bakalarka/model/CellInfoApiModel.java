package info.stefkovi.studium.mte_bakalarka.model;

public class CellInfoApiModel {
    public CellIdentityApiModel identity;
    public CellSignalStrengthApiModel signal;
    public boolean registered;
    public int connection_status;
    public String network_type;

    public CellInfoApiModel() {
        identity = new CellIdentityApiModel();
        signal = new CellSignalStrengthApiModel();
    }
}
