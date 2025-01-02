package home;

public class ItemType {

    private int itemTypeID;
    private String itemTypeName;

    public ItemType(int itemTypeID, String itemTypeName) {
        this.itemTypeID = itemTypeID;
        this.itemTypeName = itemTypeName;
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
