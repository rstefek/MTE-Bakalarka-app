package info.stefkovi.studium.mte_bakalarka.model;

public class EventQueueInfo {
    public long numInDbToProcess;
    public int numInQTotal;
    public int numInQToProcess;


    public EventQueueInfo(long numInDbToProcess, int numInQTotal, int numInQToProcess) {
        this.numInDbToProcess = numInDbToProcess;
        this.numInQTotal = numInQTotal;
        this.numInQToProcess = numInQToProcess;
    }
}
