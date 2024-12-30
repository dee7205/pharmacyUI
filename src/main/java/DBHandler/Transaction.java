package DBHandler;

import java.util.Date;

public class Transaction {
    private int transactionID, pharmacistID;
    private Date transactionDate;

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

    public Date getDate() {
        return transactionDate;
    }

    public void setDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDate_String(){
        return transactionDate.getDay() + "-" + transactionDate.getMonth() + "-" + transactionDate.getYear();
    }

    public String [] getInfo(){
        return new String [] {Integer.toString(getTransactionID()), Integer.toString(getPharmacistID()),
                              getDate_String()};
    }

    public static String [][] getInfoTable(Transaction [] trans){
        if (trans == null || trans.length == 0)
            return null;

        String [][] infoList = new String[trans.length][];
        for (int i = 0; i < trans.length; i++)
            infoList[i] = trans[i].getInfo();
        return infoList;
    }
}
