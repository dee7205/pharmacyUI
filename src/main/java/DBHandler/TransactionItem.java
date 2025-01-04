package DBHandler;

public class TransactionItem {
    private String itemName;
    private int sellQuantity;
    private double unitCost;

    public TransactionItem(String itemName, int sellQuantity, double unitCost) {
        this.itemName = itemName;
        this.sellQuantity = sellQuantity;
        this.unitCost = unitCost;
    }

    public String getItemName() {
        return itemName;
    }

    public int getSellQuantity() {
        return sellQuantity;
    }

    public double getUnitCost() {
        return unitCost;
    }
}


