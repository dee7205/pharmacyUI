package DBHandler;
import java.sql.Date;
import java.time.LocalDate;

public class Restocks {
    int itemID, restockID, startQty, soldQty, remainingQty;
    LocalDate restockDate, expiryDate;

    public Restocks(){ this(0, 0, 0, 0, null, null); }
    public Restocks(int itemID, int restockID, int startQty, int soldQty, LocalDate restockDate, LocalDate expiryDate){
        setItemID(itemID);
        setRestockID(restockID);
        setStartQty(startQty);
        setSoldQty(soldQty);
        setRemainingQty(startQty - soldQty);
        setRestockDate(restockDate);
        setExpiryDate(expiryDate);
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getRestockID() {
        return restockID;
    }

    public void setRestockID(int restockID) {
        this.restockID = restockID;
    }

    public int getStartQty() {
        return startQty;
    }

    public void setStartQty(int startQty) {
        this.startQty = startQty;
    }

    public int getSoldQty() {
        return soldQty;
    }

    public void setSoldQty(int soldQty) {
        this.soldQty = soldQty;
    }

    public LocalDate getRestockDate() {
        return restockDate;
    }

    public void setRestockDate(LocalDate restockDate) {
        this.restockDate = restockDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getRemainingQty(){
        return remainingQty;
    }

    public void setRemainingQty(int remainingQty) {
        this.remainingQty = remainingQty;
    }

    //
    public String [] getInfo(){
        return new String [] {Integer.toString(getRestockID()), Integer.toString(getItemID()), Integer.toString(getStartQty()), Integer.toString(getRemainingQty()),
                              Integer.toString(getSoldQty()), Date.valueOf(getRestockDate()).toString(), Date.valueOf(getExpiryDate()).toString()};
    }


    /**
     * Returns a 2d String array, where each row contains the information of a specific element under the Restock table
     * <br> The Row in the 2d String array contains the following in this order: Restock ID, Item ID, Start Qty, Remaining
     *      Qty, Sold Qty, Restock Date, and Expiry Date
     */
    public static String [][] generateInfoTable(Restocks [] list){
        if (list == null || list.length == 0)
            return null;

        String [][] infoList = new String[list.length][7];
        for (int i = 0; i < list.length; i++)
            infoList[i] = list[i].getInfo();
        return infoList;
    }

    public void printInfo(){
        String [] info = getInfo();
        for (String i : info)
            System.out.print(i + " ");
        System.out.println();
    }
}
