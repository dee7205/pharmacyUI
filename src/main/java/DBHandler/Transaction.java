package DBHandler;

import java.time.LocalDate;
import java.sql.Date;

public class Transaction {
    private int transactionID, pharmacistID, soldQty;
    private double totalSales;
    private LocalDate transactionDate;

    public Transaction(){ this(0, 0, 0, 0, null); }

    public Transaction(int transactionID, int pharmacistID, LocalDate transactionDate){
        this(transactionID, pharmacistID, 0, 0, transactionDate);
    }

    public Transaction(int transactionID, int pharmacistID, int soldQty, int totalSales, LocalDate transactionDate){
        setTransactionID(transactionID);
        setPharmacistID(pharmacistID);
        setTotalSales(totalSales);
        setSoldQty(soldQty);
        setDate(transactionDate);
    }

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

    public double getTotalSales(){
        return totalSales;
    }

    public void setTotalSales(double totalSales){
        this.totalSales = totalSales;
    }

    public int getSoldQty(){
        return soldQty;
    }

    public void setSoldQty(int soldQty){
        this.soldQty = soldQty;
    }

    public String [] getInfo(){
        return new String [] {Integer.toString(getTransactionID()), Integer.toString(getPharmacistID()),
                              Integer.toString(getSoldQty()), Double.toString(getTotalSales()),
                              Date.valueOf(getDate()).toString()};
    }

    public static String [][] getInfoTable(Transaction [] trans){
        if (trans == null || trans.length == 0)
            return null;

        String [][] infoList = new String[trans.length][5];
        for (int i = 0; i < trans.length; i++)
            infoList[i] = trans[i].getInfo();
        return infoList;
    }
}
