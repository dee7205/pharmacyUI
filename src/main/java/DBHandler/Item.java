package DBHandler;

public class Item {
    private int itemID, itemUnitTypeID, quantity;
    private double unitCost;
    private String itemName, itemUnitType;
    private double movement;

    public Item(){ this(0, "", 0, "", 0, 0, 0); }

    public Item(Item otherItem){
        this(otherItem.getItemID(), otherItem.getItemName(), otherItem.getItemUnitTypeID(), otherItem.getItemUnitType(),
             otherItem.getQuantity(), otherItem.getUnitCost(), otherItem.getMovement());
    }

    public Item(int itemID, String itemName, int itemUnitTypeID, String itemUnitType, int quantity, double unitCost, double movement){
        setItemID(itemID);
        setItemName(itemName);
        setItemUnitTypeID(itemUnitTypeID);
        setItemUnitType(itemUnitType);
        setQuantity(quantity);
        setUnitCost(unitCost);
        setMovement(movement);
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

    public int getItemUnitTypeID() {
        return this.itemUnitTypeID;
    }

    public void setItemUnitTypeID(int itemUnitTypeID) {
        this.itemUnitTypeID = itemUnitTypeID;
    }

    public String getItemUnitType() {
        return itemUnitType;
    }

    public void setItemUnitType(String itemUnitType) {
        this.itemUnitType = itemUnitType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getMovement() {
        return movement;
    }

    public void setMovement(double movement) {
        this.movement = movement;
    }

    public String [] getInfo(){
        return new String [] {Integer.toString(getItemID()), getItemName(), Integer.toString(getItemUnitTypeID()),
                              getItemUnitType(), Integer.toString(getQuantity()), Double.toString(getUnitCost()),
                              Double.toString(getMovement()) };
    }

    public static String [][] generateInfoTable(Item [] items){
        if (items == null || items.length == 0)
            return null;

        //Item ID, Item Name, Unit Cost
        String [][] infoTable = new String[items.length][7];

        for (int i = 0; i < items.length; i++)
            infoTable[i] = items[i].getInfo();

        return infoTable;
    }

    //Finds an item given an array of items and an itemID
    public static Item findItem(Item [] items, int itemID){
        for (Item item : items) {
            if (item.getItemID() == itemID)
                return item;
        }
        return null;
    }

    //Finds an item given an array of items and an itemName
    public static Item findItem(Item [] items, String itemName){
        for (Item item : items){
            if (item.getItemName().equals(itemName))
                return item;
        }
        return null;
    }
}
