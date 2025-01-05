package DBHandler;

public class TransactionWindow {
    private String itemName;
    private int sellQty;
    private double unitCost;

    public TransactionWindow(String itemName, int sellQty, double unitCost) {
        this.itemName = itemName;
        this.sellQty = sellQty;
        this.unitCost = unitCost;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getSellQty() {
        return sellQty;
    }

    public void setSellQty(int sellQty) {
        this.sellQty = sellQty;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
    }
}
