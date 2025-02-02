package DBHandler;
import java.sql.Date;

public class Restocks {
    int itemID;
    int restockID;
    int startQty;
    int soldQty;
    int remainingQty;
    private int daysBeforeExpiry = 0;
    String itemName, restockDateString, expiryDateString;
    String restockDate, expiryDate;
    double wholesaleCost;
    private String status = "";

    public Restocks(){ this(0, "", 0, 0, 0, 0, null, null); }
    public Restocks(int itemID, String itemName, int restockID, int startQty, int soldQty, double wholesaleCost, String restockDate, String expiryDate){
        setItemID(itemID);
        setItemName(itemName);
        setRestockID(restockID);
        setStartQty(startQty);
        setSoldQty(soldQty);
        setWholesaleCost(wholesaleCost);
        setRemainingQty(startQty - soldQty);
        setRestockDate(restockDate);
        setExpiryDate(expiryDate);
        setExpiryDateString(expiryDate.toString());
    }

    public Restocks(int itemID, String itemName, int restockID, int startQty, int soldQty, double wholesaleCost, String restockDate, String expiryDate, int daysBeforeExpiry){
        setItemID(itemID);
        setItemName(itemName);
        setRestockID(restockID);
        setStartQty(startQty);
        setSoldQty(soldQty);
        setWholesaleCost(wholesaleCost);
        setRemainingQty(startQty - soldQty);
        setRestockDate(restockDate);
        setRestockDateString(restockDate.toString());
        setExpiryDate(expiryDate);
        setExpiryDateString(expiryDate.toString());
        setDaysBeforeExpiry(daysBeforeExpiry);
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

    public String getRestockDate() {
        return restockDate;
    }

    public void setRestockDate(String restockDate) {
        this.restockDate = restockDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getRemainingQty(){
        return remainingQty;
    }

    public void setRemainingQty(int remainingQty) {
        this.remainingQty = remainingQty;
    }

    public double getWholesaleCost() {
        return wholesaleCost;
    }

    public void setWholesaleCost(double wholesaleCost) {
        this.wholesaleCost = wholesaleCost;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setRestockDateString(String restockDateString){
        this.restockDateString = restockDateString;
    }

    public String getRestockDateString(){
        return this.restockDateString;
    }

    public void setExpiryDateString(String expiryDateString){
        this.expiryDateString = expiryDateString;
    }

    public String getExpiryDateString(){
        return this.expiryDateString;
    }

    //
    public String [] getInfo(){
        return new String [] {Integer.toString(getRestockID()), Integer.toString(getItemID()), Integer.toString(getStartQty()),
                              Integer.toString(getSoldQty()), Double.toString(getWholesaleCost()), getRestockDate().toString(),
                              getExpiryDate().toString()};
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

    public int getDaysBeforeExpiry() {
        return daysBeforeExpiry;
    }

    public void setDaysBeforeExpiry(int daysBeforeExpiry) {
        this.daysBeforeExpiry = daysBeforeExpiry;
        if (daysBeforeExpiry > 90) status = "Good";
        else if (daysBeforeExpiry > 30) status = "Near Expiry";
        else if (daysBeforeExpiry > 0)status = "Short Expiry";
        else status = "EXPIRED";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
