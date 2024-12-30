package DBHandler;

import java.time.LocalDate;
import java.sql.Date;

public class Transaction {
    private int transactionID, pharmacistID;
    private LocalDate transactionDate;

    public int getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    public int getPharmacistID() {
        return pharmacistID;
    }

    public void setPharmacistID(int pharmacistID) {
        this.pharmacistID = pharmacistID;
    }

    public LocalDate getDate() {
        return transactionDate;
    }

    public void setDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String [] getInfo(){
        return new String [] {Integer.toString(getTransactionID()), Integer.toString(getPharmacistID()),
                              Date.valueOf(getDate()).toString()};
    }

    public static String [][] getInfoTable(Transaction [] trans){
        if (trans == null || trans.length == 0)
            return null;

        String [][] infoList = new String[trans.length][3];
        for (int i = 0; i < trans.length; i++)
            infoList[i] = trans[i].getInfo();
        return infoList;
    }
}
