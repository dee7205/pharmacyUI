package DBHandler;

import java.time.LocalDate;
import java.sql.Date;

public class Transaction {
    private int transactionID, pharmacistID, soldQty;
    private double income;
    private Date transactionDate;
    private String transactionDateString;
    private String pharmacist;

    public Transaction(){ this(0, 0, "N/A", 0, 0, null); }

    public Transaction(int transactionID, int pharmacistID, String Pharmacist, Date transactionDate){
        this(transactionID, pharmacistID, Pharmacist, 0, 0, transactionDate);
    }

    public Transaction(int transactionID, int pharmacistID, String pharmacist, int soldQty, int income, Date transactionDate){
        setTransactionID(transactionID);
        setPharmacistID(pharmacistID);
        setIncome(income);
        setSoldQty(soldQty);
        setTransactionDate(transactionDate);
        setTransactionDateString(transactionDate.toString());
        setPharmacist(pharmacist);
    }

    public String getPharmacist() {
        return pharmacist;
    }

    public void setPharmacist(String pharmacist) {
        this.pharmacist = pharmacist;
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

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public double getIncome(){
        return income;
    }

    public void setIncome(double income){
        this.income = income;
    }

    public int getSoldQty(){
        return soldQty;
    }

    public void setSoldQty(int soldQty){
        this.soldQty = soldQty;
    }

    public String getTransactionDateString() {
        return transactionDateString;
    }

    public void setTransactionDateString(String transactionDateString) {
        this.transactionDateString = transactionDateString;
    }

    public String [] getInfo(){
        return new String [] {Integer.toString(getTransactionID()), Integer.toString(getPharmacistID()),
                              Integer.toString(getSoldQty()), Double.toString(getIncome()),
                              getTransactionDate().toString()};
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
