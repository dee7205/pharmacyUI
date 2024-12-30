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
}
