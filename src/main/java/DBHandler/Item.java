package DBHandler;

public class Item {
    private int itemID, itemUnitTypeID;
    private double unitCost;
    private String itemName;

    public static final String MEDICINE = "Medicine";
    public static final String EQUIPMENT = "Equipment";
    public static final String SUPPLIES = "Supplies";

    public Item(){ this(0, "", 0, 0); }

    public Item(Item otherItem){
        this(otherItem.getItemID(), otherItem.getItemName(), otherItem.getItemUnitTypeID(), otherItem.getUnitCost());
    }

    public Item(String [] info){
        this(Integer.parseInt(info[0]), info[1], Integer.parseInt(info[2]), Double.parseDouble(info[3]));
    }

    public Item(int itemID, String itemName, int itemUnitTypeID, double unitCost){
        setItemID(itemID);
        setItemName(itemName);
        setItemUnitTypeID(itemUnitTypeID);
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

    public int getItemUnitTypeID() {
        return this.itemUnitTypeID;
    }

    public void setItemUnitTypeID(int itemUnitTypeID) {
        this.itemUnitTypeID = itemUnitTypeID;
    }

    public String [] getInfo(){
        return new String [] {Integer.toString(getItemID()), getItemName(), Integer.toString(getItemUnitTypeID()),
                              Double.toString(getUnitCost())};
    }

    public static String [][] generateInfoTable(Item [] items){
        if (items == null || items.length == 0)
            return null;

        //Item ID, Item Name, Unit Cost
        String [][] infoTable = new String[items.length][4];

        for (int i = 0; i < items.length; i++)
            infoTable[i] = items[i].getInfo();

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
