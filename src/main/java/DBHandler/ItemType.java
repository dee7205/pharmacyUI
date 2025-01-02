package DBHandler;

public class ItemType {
    int itemTypeID;
    String itemTypeName;

    public ItemType(){ this(0, ""); }
    public ItemType(int itemTypeID, String itemTypeName){
        setItemTypeID(itemTypeID);
        setItemTypeName(itemTypeName);
    }

    public int getItemTypeID() {
        return itemTypeID;
    }

    public void setItemTypeID(int itemTypeID) {
        this.itemTypeID = itemTypeID;
    }

    public String getItemTypeName() {
        return itemTypeName;
    }

    public void setItemTypeName(String itemTypeName) {
        this.itemTypeName = itemTypeName;
    }

    public String [] getInfo(){
        return new String [] {Integer.toString(itemTypeID), itemTypeName};
    }

    public static String [][] generateInfoTable(ItemType [] types){
        if (types == null || types.length == 0)
            return null;

        String [][] infoList = new String[types.length][2];
        for (int i = 0; i < types.length; i++)
            infoList[i] = types[i].getInfo();
        return infoList;
    }
}
