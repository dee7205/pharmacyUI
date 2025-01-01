package DBHandler;
import java.sql.Date;
import java.time.LocalDate;

public class ItemsSold {
    private int transactionID, itemID, itemQty;
    String itemName;
    LocalDate transactionDate;
    double unitCost, income;

    public ItemsSold(){ this(0, 0, 0, 0, 0, null); }
    public ItemsSold(int transactionID, int itemID, double unitCost, int itemQty, double income, LocalDate transactionDate){
        setTransactionID(transactionID);
        setItemID(itemID);
        setUnitCost(unitCost);
        setItemQty(itemQty);
        setIncome(income);
        setTransactionDate(transactionDate);
    }

    public int getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getItemQty() {
        return itemQty;
    }

    public void setItemQty(int itemQty) {
        this.itemQty = itemQty;
    }

    public LocalDate getTransactionDate(){
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate){
        this.transactionDate = transactionDate;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getIncome() {
        return income;
    }

    public void setIncome(double income) {
        this.income = income;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
    }

    public String [] getInfo(){
        return new String []{Integer.toString(getTransactionID()), Integer.toString(getItemID()),
                             Integer.toString(getItemQty()), Date.valueOf(transactionDate).toString()};
    }

    public static String [][] generateInfoTable(ItemsSold [] soldItems){
        if (soldItems == null || soldItems.length == 0)
            return null;

        String [][] infoList = new String[soldItems.length][4];
        for (int i = 0; i < soldItems.length; i++)
            infoList[i] = soldItems[i].getInfo();
        return infoList;
    }
}
