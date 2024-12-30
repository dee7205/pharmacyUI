package DBHandler;

public class Pharmacist {
    private String firstName, middleName, lastName;
    private int pharmacistID;

    public Pharmacist(){ this("", "", "", 0); }
    public Pharmacist(String [] info){
        if (info.length == 4){
            setFirstName(info[0]);
            setMiddleName(info[1]);
            setLastName(info[2]);
            setPharmacistID(Integer.parseInt(info[3]));
        } else {
            setFirstName("");
            setMiddleName("");
            setLastName("");
            setPharmacistID(0);
        }
    }

    public Pharmacist(Pharmacist otherPharmacist){
        this(otherPharmacist.getFirstName(), otherPharmacist.getMiddleName(),
             otherPharmacist.getLastName(), otherPharmacist.getPharmacistID());
    }

    public Pharmacist(String firstName, String middleName, String lastName, int pharmacistID){
        setFirstName(firstName);
        setMiddleName(middleName);
        setLastName(lastName);
        setPharmacistID(pharmacistID);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getPharmacistID() {
        return pharmacistID;
    }

    public void setPharmacistID(int pharmacistID) {
        this.pharmacistID = pharmacistID;
    }

    @Override
    public String toString(){
        return getPharmacistID() + " " + getFirstName() + " " + getMiddleName() + " " + getLastName();
    }

    public String [] getPharmacistInfo(){
        return new String[]{Integer.toString(getPharmacistID()), getFirstName(), getMiddleName(), getLastName()};
    }

    public static String [][] generatePharmacistTable(Pharmacist [] pharmacists){
        if (pharmacists == null || pharmacists.length == 0)
            return null;

        //Pharmacist ID, First Name, Middle Name, Last Name
        String [][] infoTable = new String[pharmacists.length][4];

        for (int i = 0; i < pharmacists.length; i++){
            infoTable[i][0] = Integer.toString(pharmacists[i].getPharmacistID());
            infoTable[i][1] = pharmacists[i].getFirstName();
            infoTable[i][2] = pharmacists[i].getMiddleName();
            infoTable[i][3] = pharmacists[i].getLastName();
        }

        return infoTable;
    }

    public static Pharmacist findPharmacist(Pharmacist [] pharmacists, String firstName, String middleName, String lastName){
        for (Pharmacist pharmacist : pharmacists){
            if (pharmacist.getFirstName().equals(firstName) && pharmacist.getMiddleName().equals(middleName) &&
                pharmacist.getLastName().equals(lastName))
                return pharmacist;
        }

        return null;
    }

    public static Pharmacist findPharmacist(Pharmacist [] pharmacists, int pharmacistID){
        for (Pharmacist pharmacist : pharmacists){
            if (pharmacist.getPharmacistID() == pharmacistID)
                return pharmacist;
        }

        return null;
    }
}
