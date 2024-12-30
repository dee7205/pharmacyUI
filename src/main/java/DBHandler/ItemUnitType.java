package DBHandler;

public class ItemUnitType {
    private int itemUnitTypeID, itemTypeID, unitTypeID;
    private String itemTypeName, unitTypeName;

    public ItemUnitType(){ this(0, 0, 0, "", ""); }
    public ItemUnitType(int itemUnitTypeID, int itemTypeID, int unitTypeID, String itemTypeName, String unitTypeName){
        setItemUnitTypeID(itemUnitTypeID);
        setItemTypeID(itemTypeID);
        setUnitTypeID(unitTypeID);
        setUnitTypeName(unitTypeName);
        setItemTypeName(itemTypeName);
    }

    public int getItemUnitTypeID() {
        return itemUnitTypeID;
    }

    public void setItemUnitTypeID(int itemUnitTypeID) {
        this.itemUnitTypeID = itemUnitTypeID;
    }

    public int getItemTypeID() {
        return itemTypeID;
    }

    public void setItemTypeID(int itemTypeID) {
        this.itemTypeID = itemTypeID;
    }

    public int getUnitTypeID() {
        return unitTypeID;
    }

    public void setUnitTypeID(int unitTypeID) {
        this.unitTypeID = unitTypeID;
    }

    public String getItemTypeName() {
        return itemTypeName;
    }

    public void setItemTypeName(String itemTypeName) {
        this.itemTypeName = itemTypeName;
    }

    public String getUnitTypeName() {
        return unitTypeName;
    }

    public void setUnitTypeName(String unitTypeName) {
        this.unitTypeName = unitTypeName;
    }

    public String [] getInfo(){
        return new String [] {Integer.toString(getItemUnitTypeID()), Integer.toString(getItemTypeID()), Integer.toString(getUnitTypeID()),
                              getItemTypeName(), getUnitTypeName()};
    }

    public static String [][] generateInfoTable(ItemUnitType [] list){
        if (list == null || list.length == 0)
            return null;

        //Item ID, Item Name, Unit Cost
        String [][] infoTable = new String[list.length][5];

        for (int i = 0; i < list.length; i++)
            infoTable[i] = list[i].getInfo();

        return infoTable;
    }
}
