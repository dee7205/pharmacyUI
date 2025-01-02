package home;

public class Item {
    private Integer itemNumber;
    private String itemName;
    private String itemType;
    private Integer beginningQty;
    private Double unitCost;

    public Item(Integer itemNumber, String itemName, String itemType, Integer beginningQty, Double unitCost) {
        this.itemNumber = itemNumber;
        this.itemName = itemName;       this.itemType = itemType;
        this.beginningQty = beginningQty;
        this.unitCost = unitCost;
    }

    public String getItemName() {
        return itemName;
    }

    public int getItemNumber() {
        return itemNumber;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public String getItemType() {
        return itemType;
    }

    public int getBeginningQty() {
        return beginningQty;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setItemNo(int itemNumber) {
        this.itemNumber = itemNumber;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public void setBeginningQty(int beginningQty) {
        this.beginningQty = beginningQty;
    }
}
