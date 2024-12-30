package DBHandler;

public class ItemsSold {
    private int transactionID, itemID, itemQty;

    public ItemsSold(){ this(0, 0, 0); }
    public ItemsSold(int transactionID, int itemID, int itemQty){
        setTransactionID(transactionID);
        setItemID(itemID);
        setItemQty(itemQty);
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

    public String [] getInfo(){
        return new String []{Integer.toString(getTransactionID()), Integer.toString(getItemID()),
                             Integer.toString(getItemQty())};
    }

    public static String [][] generateInfoTable(ItemsSold [] soldItems){
        if (soldItems == null || soldItems.length == 0)
            return null;

        String [][] infoList = new String[soldItems.length][3];
        for (int i = 0; i < soldItems.length; i++)
            infoList[i] = soldItems[i].getInfo();
        return infoList;
    }
}
