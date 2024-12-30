package DBHandler;

public class Item {
    private int itemID, itemTypeID, unitTypeID;
    private double unitCost;
    private String itemName, itemType;

    public static final String MEDICINE = "Medicine";
    public static final String EQUIPMENT = "Equipment";
    public static final String SUPPLIES = "Supplies";

    public Item(){ this(0, "", "", 0); }

    public Item(Item otherItem){
        this(otherItem.getItemID(), otherItem.getItemName(), otherItem.getItemType(), otherItem.getUnitCost());
    }

    public Item(String [] info){
        this(Integer.parseInt(info[0]), info[1], info[2], Double.parseDouble(info[3]));
    }

    public Item(int itemID, String itemName, String itemType, double unitCost){
        if (!itemType.equals(MEDICINE) && !itemType.equals(EQUIPMENT) && !itemType.equals(SUPPLIES)){
            System.out.println("Unable to create new pharmacist. \nIllegal item type used: " + itemType);
            setItemID(0);
            setItemName("");
            setItemType("");
            setUnitCost(0);
            return;
        }

        setItemID(itemID);
        setItemName(itemName);
        setItemType(itemName);
        setUnitCost(unitCost);
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String [] getItemInfo(){
        return new String [] {Integer.toString(getItemID()), getItemName(), getItemType(), Double.toString(getUnitCost())};
    }

    public static String [][] generateItemTable(Item [] items){
        if (items == null || items.length == 0)
            return null;

        //Item ID, Item Name, Unit Cost
        String [][] infoTable = new String[items.length][4];

        for (int i = 0; i < items.length; i++){
            infoTable[i][0] = Integer.toString(items[i].getItemID());
            infoTable[i][1] = items[i].getItemName();
            infoTable[i][2] = items[i].getItemType();
            infoTable[i][3] = Double.toString(items[i].getUnitCost());
        }

        return infoTable;
    }

    //Finds an item given an array of items
    public static Item findItem(Item [] items, int itemID){
        for (Item item : items) {
            if (item.getItemID() == itemID)
                return item;
        }
        return null;
    }

    public static Item findItem(Item [] items, String itemName){
        for (Item item : items){
            if (item.getItemName().equals(itemName))
                return item;
        }
        return null;
    }
}
