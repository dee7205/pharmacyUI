package DBHandler;

public class UnitType {
    private int unitTypeID;
    private String unitType;

    public UnitType(){ this(0, ""); }

    public UnitType(int unitTypeID, String unitType){
        setUnitTypeID(unitTypeID);
        setUnitType(unitType);
    }

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public int getUnitTypeID() {
        return unitTypeID;
    }

    public void setUnitTypeID(int unitTypeID) {
        this.unitTypeID = unitTypeID;
    }

    public String [] getInfo(){
        return new String [] {Integer.toString(getUnitTypeID()), getUnitType()};
    }

    public static String [][] generateInfoTable(UnitType [] list){
        if (list == null || list.length == 0)
            return null;

        String [][] info = new String[list.length][2];

        for (int i = 0; i < list.length; i++)
            info[i] = list[i].getInfo();
        return info;
    }
}
