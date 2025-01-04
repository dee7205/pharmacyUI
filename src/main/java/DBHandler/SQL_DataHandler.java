package DBHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class SQL_DataHandler {

    private static Connection connection;
    public static int balance = 800000;

    public static void main (String [] args) {
        SQL_DataHandler handler = new SQL_DataHandler();

//        handler.removeAllPharmacists();
        handler.addPharmacist(1, "Allan George", "Cajes", "Tagle");
        handler.addPharmacist(2, "Waken Cean", "Cruz", "Maclang");
        handler.addPharmacist(3, "Dave Shanna", "Marie", "Gigawin");
        handler.addPharmacist(4, "John Marcellin", "Espanyol", "Tan");

        Pharmacist[] pharmacists = handler.getPharmacists(1, 4);
        for (Pharmacist pharmacist : pharmacists)
            System.out.println(pharmacist.getPharmacistID() + " " + pharmacist.getFirstName() + " " + pharmacist.getMiddleName() + " " + pharmacist.getLastName());

        handler.removePharmacist(3);
        System.out.println(handler.pharmacistExists(3));

        handler.addItemType("Medicine");
        handler.addUnitType("Pills");
        handler.addItemUnitType("Medicine", "Pills");

        handler.addUnitType("Packet");
        UnitType type = handler.getUnitType(handler.getUnitTypeID("Packet"));
        System.out.println(type.getUnitTypeID() + " " + type.getUnitType());

        System.out.println("Item Unit Type Exists: " + handler.getItemUnitType(1, 1));

//        handler.removeAllItems();
        handler.addItem("Paracetamol", handler.getItemUnitTypeID("Medicine", "Pills"), 5.0);
        handler.addItem("Biogesic", handler.getItemUnitTypeID("Medicine", "Pills"), 4.0);
        handler.addItem("Neozep", handler.getItemUnitTypeID("Medicine", "Pills"), 6.0);

        Item[] items = handler.getItems(1, 3);
        for (Item item : items){
            for (String info : item.getInfo())
                System.out.print(info + " ");
            System.out.println();
        }

        System.out.println(handler.itemExists("Myogesic"));

        System.out.println("Add Restock: " + handler.addRestock(1, 50, 3.0, Date.valueOf(handler.getCurrentDate()), Date.valueOf(LocalDate.of(2025, 1, 15))));
        handler.reduceRestocks(1, 30);

        Restocks [] list = handler.getCurrentStock(1);
        for (Restocks i : list)
            i.printInfo();

        System.out.println("Remaining stock for " + handler.getItem("Paracetamol").getItemName() + ": " + handler.getRemainingStockQuantity(handler.getItemId("Paracetamol")));

//        handler.addTransaction(1);
        handler.addItemsSold(1, 1, 20);
        ItemsSold [] sold = handler.getItemsSold_Item(1);

        if (sold != null && sold.length > 0){
            for (ItemsSold i : sold){
                for (String j : i.getInfo())
                    System.out.print(j + " ");
                System.out.println();
            }
        }
    }

    public SQL_DataHandler(){
        prepareConnection();
    }

//======================================================================================================================================================================
//Methods for the Pharmacist.

    /**
     * Method used to add a pharmacist to the database.
     * Before adding a new pharmacist to the DB., the program double checks if the primary key to a pharmacist exists in the database though the getPharmacist() method
     *
     * @param pharmaID      the primary key used to reference the pharmacist in the database
     * @param firstName     the first name of the Pharmacist
     * @param middleName    the middle name of the Pharmacist
     * @param lastName      the last name of the Pharmacist
     * @return boolean      Helps in identifying if the Pharmacist was successfully added to the database.
     */
    public boolean addPharmacist(int pharmaID, String firstName, String middleName, String lastName){
        if (connection == null)
            prepareConnection();

        try{
            if (connection == null){
                System.out.println("ERROR: Connection is null.");
                return false;
            }

            if (pharmacistExists(pharmaID)){
                System.out.println("ERROR: Unable to add new pharmacist as, " + firstName + " " + middleName + " " + lastName + ", already exists in the database");
                return false;
            }

            //If the pharmacist with the given name parameters doesn't exist, add the pharmacist to the database.
            String query = "INSERT INTO Pharmacists (pharmacist_ID, fName, mName, lName) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, pharmaID);
            pstmt.setString(2, firstName);
            pstmt.setString(3, middleName);
            pstmt.setString(4, lastName);

            return pstmt.executeUpdate() > 0;
        }   catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Finds a specific pharmacist based on their whole name based from the parameters @param firstName, @param middleName, and @param lastName.
     */
    public Pharmacist getPharmacist(int pharmaID){
        String query = "SELECT * FROM Pharmacists WHERE pharmacist_ID = " + pharmaID;

        if (connection == null)
            prepareConnection();

        try(Statement stmt = connection.createStatement()){

            if (!pharmacistExists(pharmaID))
                return null;

            //Checks if the contents of the result set is not null.
            //If not, returns the Pharmacist info array (Contains their name)
            ResultSet set = stmt.executeQuery(query);
            if (set.next()) {
                return new Pharmacist(
                        set.getString("fName"),
                        set.getString("mName"),
                        set.getString("lName"),
                        set.getInt("pharmacist_ID"));
            } else
                return null;


        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }//End of getPharmacist method

    /**
     * Method to get the pharmacist_ID of a pharmacist given their first name, middle name, and last name.
     *
     * @param fName     The first name of the pharmacist
     * @param mName     The middle name of the pharmacist
     * @param lName     The last name of the pharmacist
     * @return          An integer representing the pharmacist_ID given the parameters, however, it may
     *                  return -1 if the pharmacist doesn't exist, or no proper connection to the database
     *                  was established.
     */
    public int getPharmacistID(String fName, String mName, String lName){
        if (connection == null)
            prepareConnection();

        try{
            if (!pharmacistExists(fName, mName, lName))
                return -1;

            final String query = "SELECT pharmacist_ID AS \"total\" FROM Pharmacists WHERE fName = " + fName + " AND mName = " + mName + " AND lName = " + lName;
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet set = pstmt.executeQuery();

            //Checks if the contents of the result set is not null.
            //If not, returns the Pharmacist info array (Contains their name)
            if (set.next())
                return set.getInt("pharmacist_ID");
            else
                return -1;

        }   catch (SQLException e){
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * @param pharmacistID  Identifier or primary key of the Pharmacist Table
     * @return              true, if a pharmacist exists, and false if not.
     * <br>
     * <br> Checks if a specific pharmacist exists in the database given their pharmacistID
     */
    public boolean pharmacistExists(int pharmacistID){
        if (connection == null)
            prepareConnection();

        try{
            final String query = "SELECT COUNT(*) AS \"total\" FROM Pharmacists WHERE pharmacist_ID = " + pharmacistID;
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet set = pstmt.executeQuery();

            set.next();
            return set.getInt("total") > 0;
        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param fName     The first name of the pharmacist (Can consist of two or more words)
     * @param mName     The middle name of the pharmacist
     * @param lName     The last name of the pharmacist
     * @return          true, if a pharmacist exists, and false if not, given the parameters (fName, mName, and lName)
     * <br>
     * <br> Checks if a specific pharmacist exists in the database given their first, middle, and last name
     */
    public boolean pharmacistExists(String fName, String mName, String lName){
        if (connection == null)
            prepareConnection();

        try{
            final String query = "SELECT COUNT(*) AS \"total\" FROM Pharmacists WHERE fName = ? AND mName = ? AND lName = ? ";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, fName);
            pstmt.setString(2, mName);
            pstmt.setString(3, lName);
            ResultSet set = pstmt.executeQuery();

            if (set.next())
                return set.getInt("total") > 0;
            else
                return false;
        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Prints out the details (first name, middle name, and last name) of a pharmacist to the console, given their pharmacist ID
     *
     * @param pharmaID      The primary key of the Pharmacist in the database
     */
    public void printPharmacist(int pharmaID) {
        if (connection == null)
            prepareConnection();

        try {
            String query = "SELECT * FROM Pharmacists WHERE pharmacist_ID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, pharmaID);
            ResultSet set = pstmt.executeQuery();

            if (set.next()) {
                System.out.println("Pharma ID: " + pharmaID + "\nPharmacist Name: " + set.getString("fName") + " " + set.getString("mName") +
                        " " + set.getString("lName"));
            } else {
                System.out.println("Pharmacist with ID: " + pharmaID + ", does not exist.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used to remove a pharmacist based on their pharmacist_ID
     * @param pharmaID  The ID of the pharmacist to be removed (Primary Key)
     * @return          true, if the pharmacist is removed, and false if the pharmacist doesn't exist
     *                  or is not removed successfully
     */
    public boolean removePharmacist(int pharmaID){
        if (connection == null)
            prepareConnection();

        try{
            if (!pharmacistExists(pharmaID))
                return false;

            Statement stmt = connection.createStatement();
            final String query = "DELETE FROM Pharmacists WHERE pharmacist_ID = " + pharmaID;
            return stmt.executeUpdate(query) > 0;

        } catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method used to retrieve a set of pharmacist's details from a list. This is done by creating a set containing two
     * different pharmacist IDs. These IDs serve as the primary key for each pharmacist.
     *
     * @param pharmaID1     The ID of the first pharmacist
     * @param pharmaID2     The ID of the second pharmacist
     * @return              A 2d array of Strings containing the details of the pharmacists given the range of IDs or
     *                      a null array if the result set is empty due to issues in ID range
     */
    public Pharmacist [] getPharmacists(int pharmaID1, int pharmaID2){
        ArrayList<Pharmacist> list = new ArrayList<>();
        boolean isAdded = false;
        String query = """
                SELECT
                    p.fName AS "First Name",
                    p.mName AS "Middle Name",
                    p.lName AS "Last Name",
                    p.pharmacist_ID AS "Pharmacist ID"
                FROM Pharmacists AS p
                WHERE p.pharmacist_ID >= ? AND p.pharmacist_ID <= ?;
                """;

        if (connection == null)
            prepareConnection();

        // Database connection and query execution
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            if (pharmaID1 > pharmaID2){     //Made to ensure that there will be a set of IDs (ID1, ... ,  ID2)
                int temp = pharmaID1;       //where ID1 is <= ID2 to form the set pharmacists to be taken
                pharmaID1 = pharmaID2;
                pharmaID2 = temp;
            }

            //The instance where the lesser ID (pharmaID1) value is null, means that pharmaID2 is also null.
            if (!pharmacistExists(pharmaID1)){
                System.out.println("Pharmacist ID " + pharmaID1 + ", doesn't exist");
                return null;
            }

            stmt.setInt(1, pharmaID1);
            stmt.setInt(2, pharmaID2);
            ResultSet set = stmt.executeQuery();

            // Process the result set
            while (set.next()) {
                String firstName = set.getString("First Name");
                String middleName = set.getString("Middle Name");
                String lastName = set.getString("Last Name");
                int pharmacistID = set.getInt("Pharmacist ID");

                // Add each course to the list
                list.add(new Pharmacist(firstName, middleName, lastName, pharmacistID));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        Pharmacist [] array = new Pharmacist[list.size()];
        return list.toArray(array);
    }

    //TODO: Add comments to the method
    public Pharmacist [] getAllPharmacists(){
        List<Pharmacist> list = new ArrayList<>();

        String query = """
            SELECT
                    p.fName AS "First Name",
                    p.mName AS "Middle Name",
                    p.lName AS "Last Name",
                    p.pharmacist_ID AS "Pharmacist ID"
                FROM Pharmacists AS p
                WHERE p.pharmacist_ID > -1;
            """;

        if (connection == null)
            prepareConnection();

        // Database connection and query execution
        try (Statement stmt = connection.createStatement()) {

            // Execute the query
            ResultSet set = stmt.executeQuery(query);

            // Process the result set
            while (set.next()) {
                String firstName = set.getString("First Name");
                String middleName = set.getString("Middle Name");
                String lastName = set.getString("Last Name");
                int pharmacistID = set.getInt("Pharmacist ID");

                // Add each course to the list
                list.add(new Pharmacist(firstName, middleName, lastName, pharmacistID));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        Pharmacist [] array = new Pharmacist[list.size()];
        return list.toArray(array);
    }

    //TODO: Add an updatePharmacist method
    /**
     * Updates the name of an existing pharmacist
     *
     //* @param pharmaID      The primary key or identifier for the pharmacist within the database
     * @param fName         The first name of the pharmacist to be updated
     * @param mName         The middle name of the pharmacist to be updated
     * @param lName         The last name of the pharmacist to be updated
     *
     * @return              true if the pharmacist's details were updated, false if the pharmacist doesn't
     *                      exist or if the update was unable to push through.
     */
    public boolean updatePharmacist(Pharmacist pharmacist,String fName, String mName, String lName){
        if (connection == null)
            prepareConnection();

        try{
            if (!pharmacistExists(pharmacist.getPharmacistID())){
                System.out.println("ERROR: Unable to update pharmacist. \nThe pharmacist you wish to update doesn't exist in the database: " + pharmacist.getPharmacistID());
                return false;
            }

            if (pharmacistExists(fName, mName, lName)){
                System.out.println("ERROR: Unable to update pharmacist. \nThe pharmacist new name is found existing in the database: " + pharmacist.getPharmacistID());
                return false;
            }

            final String firstQuery = "UPDATE Pharmacists SET fName = ? WHERE pharmacist_ID = " + pharmacist.getPharmacistID();
            final String secondQuery = "UPDATE Pharmacists SET mName = ? WHERE pharmacist_ID = " + pharmacist.getPharmacistID();
            final String thirdQuery = "UPDATE Pharmacists SET lName = ? WHERE pharmacist_ID = " + pharmacist.getPharmacistID();

            PreparedStatement firstPstmt = connection.prepareStatement(firstQuery);
            PreparedStatement secondPstmt = connection.prepareStatement(secondQuery);
            PreparedStatement thirdPstmt = connection.prepareStatement(thirdQuery);


            firstPstmt.setString(1, fName);
            secondPstmt.setString(1, mName);
            thirdPstmt.setString(1, lName);


            int rowsAffected = 0;
            rowsAffected += firstPstmt.executeUpdate();
            rowsAffected += secondPstmt.executeUpdate();
            rowsAffected += thirdPstmt.executeUpdate();


            return (rowsAffected == 3);

        } catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes all pharmacists from the Pharmacists database
     */
    public boolean removeAllPharmacists(){
        if (connection == null)
            prepareConnection();

        try {
            final String query = "DELETE FROM Pharmacists WHERE pharmacist_ID > 0";
            PreparedStatement pstmt = connection.prepareStatement(query);
            return (pstmt.executeUpdate(query) > 0);

        }   catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

//======================================================================================================================================================================
//Methods for the Items.

    //TODO: Add methods for items (Add, Retrieve, Update, Delete & Retrieve Set)

    /**
     * Method used to add an item to the database, together with its item type and unit cost (price)
     *
     * @param itemName              The name of the item to be added to the database
     * @param itemUnitTypeID        The id of the type of item & its unit (Limited to the drop-down GUI)
     * @param unitCost              The price for one unit of an item (xxxx.xx)
     * @return                      true if the item and item type is added to the database, false if not
     */
    public boolean addItem(String itemName, int itemUnitTypeID, double unitCost){
        //Add the item to the database
        String query = "INSERT INTO Items (item_name, item_unit_ID, unit_cost) VALUES (?, ?, ?)";

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){

            //Checks if there is no existing item with this name
            if (itemExists(itemName)){
                System.out.println("ERROR: Unable to add new item as, " + itemName + " " + itemUnitTypeID + " " + unitCost + ", already exists in the database");
                return false;
            }

            pstmt.setString(1, itemName);
            pstmt.setInt(2, itemUnitTypeID);
            pstmt.setDouble(3, unitCost);

            return pstmt.executeUpdate() > 0;

        }   catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method used to add an item to the database, together with its item type and unit cost (price)
     * <br> Checks if the itemType & unitType combination exists, if not, creates a new ItemUnitType and
     *      assigns it to the item to be made
     *
     * @param itemName              The name of the item to be added to the database
     * @param itemTypeID            The id of the item type (Limited to the drop-down GUI)
     * @param unitTypeID            The id of the unit type (Limited to the drop-down GUI)
     * @param unitCost              The price for one unit of an item (xxxx.xx)
     * @return                      true if the item and item type is added to the database, false if not
     */
    public boolean addItem(String itemName, int itemTypeID, int unitTypeID, double unitCost){
        //Add the item to the database
        String query = "INSERT INTO Items (item_name, item_unit_ID, unit_cost) VALUES (?, ?, ?)";

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){

            //Creates a new ItemUnitType in scenarios where an Item Type and Unit Type Combination doesn't exist
            if (!itemUnitTypeExists(itemTypeID, unitTypeID)){
                addItemUnitType(itemTypeID, unitTypeID);
            }

            int itemUnitTypeID = getItemUnitTypeID(itemTypeID, unitTypeID);

            //Checks if there is no existing item with this name
            if (itemExists(itemName)){
                System.out.println("ERROR: Unable to add new item as, " + itemName + " " + itemUnitTypeID + " " + unitCost + ", already exists in the database");
                return false;
            }

            pstmt.setString(1, itemName);
            pstmt.setInt(2, itemUnitTypeID);
            pstmt.setDouble(3, unitCost);
            return pstmt.executeUpdate() > 0;

        }   catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the name and price of a specific item
     *
     * @param itemName      The original name of the item (Serves as its identifier in the database)
     * @param newName       The new name of the item
     * @param unitCost      The new cost of the item
     *
     * @return              true if the item was successfully updated, false if the item doesn't exist or
     *                      was not updated.
     */
    public boolean updateItem(String itemName, String newName, double unitCost){
        if (connection == null)
            prepareConnection();

        try{
            //Checks if there is no existing item with this name
            if (!itemExists(itemName)){
                System.out.println("ERROR: Item to be updated: " + itemName + ", doesn't exist in the database.");
                return false;
            }

            if (itemExists(newName)){
                System.out.println("ERROR: item " + itemName + ", exists in the database.");
                return false;
            }

            ///Add the item to the database
            String firstQuery = "UPDATE Items SET item_name = ? WHERE item_name = ?";
            String secondQuery = "UPDATE Items SET unit_cost = ? WHERE item_name = ?";
            PreparedStatement firstPstmt = connection.prepareStatement(firstQuery);
            PreparedStatement secondPstmt = connection.prepareStatement(secondQuery);
            firstPstmt.setString(1, newName);
            firstPstmt.setString(2, itemName);
            secondPstmt.setDouble(1, unitCost);
            secondPstmt.setString(2, newName);

            int rowsAffected = 0;
            rowsAffected += firstPstmt.executeUpdate();
            rowsAffected += secondPstmt.executeUpdate();

            if (rowsAffected <= 0)
                return false;

            return rowsAffected > 0;

        }   catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method used to get a range of items based from the ids (itemID1, itemID2)
     * Represents a range of items (ID1, ... , ID2) is formed, where ID1 <= ID2
     * <br>
     * <br> It is better to use a method other than getItems directly as once an item is deleted
     * and added back to the list, the auto-increment feature of sql doesn't reuse the
     * spare row and instead uses next succeeding rows to the table
     * <br>
     * <br> (e.g. Item ID: 1, 2, 3, null, 5, 6, null, null, 7, 8)
     * <br> where null values represent deleted rows
     *
     * @param itemID1   The ID of one of the items
     * @param itemID2   The ID of another item
     * @return          An Item array containing the range of items
     */
    public Item [] getItems(int itemID1, int itemID2){
        final String query = """
                SELECT
                	i.item_ID AS "Item ID",
                	i.item_name AS "Item Name",
                	ut.unit_Type AS "Unit Type",
                	it.item_Type AS "Item Type",
                	iut.item_unit_ID AS "Item Unit ID",
                	(SUM(COALESCE(r.start_qty - r.sold_qty, 0))) AS "Item Quantity",
                	i.unit_cost AS "Unit Cost",
                    ((SUM(COALESCE(isd.item_qty * i.unit_cost, 0))) / 800000) * 100 AS "Movement"
                FROM Items AS i
                JOIN ItemUnitType AS iut ON i.item_unit_ID = iut.item_unit_ID
                JOIN ItemType AS it ON it.itemType_ID = iut.itemType_ID
                JOIN UnitType AS ut ON ut.unitType_ID = iut.unitType_ID
                LEFT JOIN Sold_Items AS isd ON isd.item_ID = i.item_ID
                LEFT JOIN Restocks AS r ON r.item_ID = i.item_ID
                	AND r.expiry_Date >= CURRENT_DATE()
                    AND r.start_Qty > r.sold_Qty
                WHERE i.item_ID >= ? AND i.item_ID <= ?
                GROUP BY i.item_ID;
                """;
        boolean isAdded = false;

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){

            if (itemID1 > itemID2){     //Made to ensure that there will be a set of IDs (ID1, ... ,  ID2)
                int temp = itemID1;     //where ID1 is <= ID2 to form the set pharmacists to be taken
                itemID1 = itemID2;
                itemID2 = temp;
            }

            //The instance where the lesser ID (itemID1) value is null, means that itemID2 is also null.
            if (!itemExists(itemID1)){
                System.out.println("Item ID " + itemID1 + ", doesn't exist");
                return null;
            }

            List<Item> list = new ArrayList<>();
            pstmt.setInt(1, itemID1);
            pstmt.setInt(2, itemID2);
            ResultSet set = pstmt.executeQuery();

            while(set.next()){
                Item item = new Item(set.getInt("Item ID"), set.getString("Item Name"),
                                     set.getInt("Item Unit ID"), (set.getString("Item Type") + "-" +
                                     set.getString("Unit Type")), set.getInt("Item Quantity"),
                                     set.getDouble("Unit Cost"), set.getDouble("Movement"));
                list.add(item);
                isAdded = true;
            }

            if (isAdded){
                Item [] newList = new Item[list.size()];
                return list.toArray(newList);
            } else {
                System.out.println("Unable to generate Item list");
                return null;
            }

        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    //TODO: Implement this
    public Item [] getAllItemsFiltered(int condition){
        return null;
    }

    //TODO: Add comments for this method
    public Item [] getAllItems(int limit){
        final String query = """
                SELECT
                	i.item_ID AS "Item ID",
                	i.item_name AS "Item Name",
                	ut.unit_Type AS "Unit Type",
                	it.item_Type AS "Item Type",
                	iut.item_unit_ID AS "Item Unit ID",
                	(SUM(COALESCE(r.start_qty - r.sold_qty, 0))) AS "Item Quantity",
                	i.unit_cost AS "Unit Cost"
                FROM Items AS i
                JOIN ItemUnitType AS iut ON i.item_unit_ID = iut.item_unit_ID
                JOIN ItemType AS it ON it.itemType_ID = iut.itemType_ID
                JOIN UnitType AS ut ON ut.unitType_ID = iut.unitType_ID
                LEFT JOIN Restocks AS r ON r.item_ID = i.item_ID
                	AND r.expiry_Date >= CURRENT_DATE()
                    AND r.start_Qty > r.sold_Qty
                GROUP BY i.item_ID
                ORDER BY i.item_ID ASC
                LIMIT ?;
                """;

        boolean isAdded = false;

        if (connection == null)
            prepareConnection();

        if (limit == -1)
            limit = 1000;

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            List<Item> list = new ArrayList<>();
            pstmt.setInt(1, limit);
            ResultSet set = pstmt.executeQuery();

            while(set.next()){
                Item item = new Item(set.getInt("Item ID"), set.getString("Item Name"),
                        set.getInt("Item Unit ID"), (set.getString("Item Type") + "-" +
                        set.getString("Unit Type")), set.getInt("Item Quantity"),
                        set.getDouble("Unit Cost"), 0);
                list.add(item);
                isAdded = true;
            }

            if (isAdded){
                Item [] newList = new Item[list.size()];
                return list.toArray(newList);
            } else {
                System.out.println("Unable to generate Item list");
                return null;
            }

        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the detail of an item given its item id
     *
     * @param itemID    The id of the item to be looked up in the Items table
     * @return          An Item object containing the details of the item (ID, Name, Type, Quantity, Price, and Movement)
     */
    public Item getItem(int itemID){
        //Gets the item's details from the Item table
        final String query = """
                SELECT
                	i.item_ID AS "Item ID",
                	i.item_name AS "Item Name",
                	ut.unit_Type AS "Unit Type",
                	it.item_Type AS "Item Type",
                	iut.item_unit_ID AS "Item Unit ID",
                	(SUM(COALESCE(r.start_qty - r.sold_qty, 0))) AS "Item Quantity",
                	i.unit_cost AS "Unit Cost",
                    ((SUM(COALESCE(isd.item_qty * i.unit_cost, 0))) / 800000) * 100 AS "Movement"
                FROM Items AS i
                JOIN ItemUnitType AS iut ON i.item_unit_ID = iut.item_unit_ID
                JOIN ItemType AS it ON it.itemType_ID = iut.itemType_ID
                JOIN UnitType AS ut ON ut.unitType_ID = iut.unitType_ID
                LEFT JOIN Sold_Items AS isd ON isd.item_ID = i.item_ID
                LEFT JOIN Restocks AS r ON r.item_ID = i.item_ID
                	AND r.expiry_Date >= CURRENT_DATE()
                    AND r.start_Qty > r.sold_Qty
                WHERE i.item_ID = ?;
                """;

        if (connection == null)
            prepareConnection();

        if (!itemExists(itemID))
            return null;

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setInt(1, itemID);
            ResultSet set = pstmt.executeQuery();

            if (!set.next())
                return null;

            //Prepares the item's info into a String array
            return new Item(
                    set.getInt("Item ID"),
                    set.getString("Item Name"),
                    set.getInt("Item Unit ID"),
                    (set.getString("Item Type") + "-" + set.getString("Unit Type")),
                    set.getInt("Item Quantity"),
                    set.getDouble("Unit Cost"),
                    0);
        }   catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the detail of an item given its item name
     *
     * @param itemName  The name of the item to be looked up in the Items table
     * @return          An Item object containing the details of the item (ID, Name, Type, Quantity, Price, and Movement)
     */
    public Item getItem(String itemName){
        //Gets the item's details from the Item table
        final String query = """
                SELECT
                	i.item_ID AS "Item ID",
                	i.item_name AS "Item Name",
                	ut.unit_Type AS "Unit Type",
                	it.item_Type AS "Item Type",
                	iut.item_unit_ID AS "Item Unit ID",
                	(SUM(COALESCE(r.start_qty - r.sold_qty, 0))) AS "Item Quantity",
                	i.unit_cost AS "Unit Cost",
                    ((SUM(COALESCE(isd.item_qty * i.unit_cost, 0))) / 800000) * 100 AS "Movement"
                FROM Items AS i
                JOIN ItemUnitType AS iut ON i.item_unit_ID = iut.item_unit_ID
                JOIN ItemType AS it ON it.itemType_ID = iut.itemType_ID
                JOIN UnitType AS ut ON ut.unitType_ID = iut.unitType_ID
                LEFT JOIN Sold_Items AS isd ON isd.item_ID = i.item_ID
                LEFT JOIN Restocks AS r ON r.item_ID = i.item_ID
                	AND r.expiry_Date >= CURRENT_DATE()
                    AND r.start_Qty > r.sold_Qty
                WHERE i.item_name = ?
                GROUP BY i.item_ID;
                """;

        if (connection == null)
            prepareConnection();

        if (!itemExists(itemName))
            return null;

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setString(1, itemName);
            ResultSet set = pstmt.executeQuery();

            if (!set.next())
                return null;

            //Prepares the item's info into a String array
            return new Item(
                    set.getInt("Item ID"),
                    set.getString("Item Name"),
                    set.getInt("Item Unit ID"),
                    (set.getString("Item Type") + "-" + set.getString("Unit Type")),
                    set.getInt("Item Quantity"),
                    set.getDouble("Unit Cost"),
                    0);
        }   catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Method that retrieves the ID of an item given its name
     *
     * @param itemName  The name of the item to search for in the Items table
     * @return          int value of the item's primary key or -1 is the item doesn't exist in the table
     */
    public int getItemId(String itemName){
        if (connection == null)
            prepareConnection();

        try {
            final String selectQuery = "SELECT i.item_ID FROM Items AS i WHERE i.item_name = ?";
            PreparedStatement pstmt = connection.prepareStatement(selectQuery);
            pstmt.setString(1, itemName);

            ResultSet set = pstmt.executeQuery();

            if (set.next())
                return set.getInt("item_id");
            else
                return -1;

        }   catch (SQLException e){
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Removes one item from the database given its id
     */
    //TODO: Alter the contents by adding the getItemID method.
    public boolean removeItem(int itemID){
        if (connection == null)
            prepareConnection();

        try{
            Item item = getItem(itemID);

            if (item.getItemID() != -1){
                //Remove rows from tables with item_ID as their foreign keys
                removeItemUnitType(item.getItemUnitTypeID(), REMOVE_ITEM_UNIT_TYPE);
                removeRestock(itemID, REMOVE_BY_ITEM_ID);
                removeItemsSold(itemID);
            }   else
                return false;


            final String query = "DELETE FROM Items WHERE item_ID = " + item.getItemID();
            Statement stmt = connection.createStatement();
            return (stmt.executeUpdate(query) > 0);

        }   catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes all items from the Items database
     */
    public boolean removeAllItems(){
        if (connection == null)
            prepareConnection();

        try {
            //TODO: Add condition to remove all items in Sold_Items table
            //TODO: Add condition to remove all items in Restock table

//            removeAllSoldItems();
//            removeAllRestock();
            final String removeInItem = "DELETE FROM ItemType WHERE item_id > 0 ";
            final String removeInUnit = "DELETE FROM UnitType WHERE item_id > 0";

            assert connection != null;
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(removeInItem);
            stmt.executeUpdate(removeInUnit);

            final String query = "DELETE FROM Items WHERE item_ID > 0";
            PreparedStatement pstmt = connection.prepareStatement(query);
            int rowsAffected = pstmt.executeUpdate(query);

            return (rowsAffected > 0);
        }   catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method used to find an item given its name
     *
     * @param itemName  The name of the item (e.g. Capsule, Tablet)
     * @return          true if an item exist under the given parameters, and false if the item is not found.
     */
    public boolean itemExists(String itemName){
        if (connection == null)
            prepareConnection();

        try{
            final String query = "SELECT * FROM Items WHERE item_name = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, itemName);
            ResultSet set = pstmt.executeQuery();
            return set.next();
        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method used to find an item given its id
     *
     * @param itemID    The id of an item assumed to exist within the Items table
     * @return          true if an item exist under the given parameters, and false if the item is not found.
     */
    public boolean itemExists(int itemID){
        if (connection == null)
            prepareConnection();

        try{
            final String query = "SELECT COUNT(*) AS \"total\" FROM Items WHERE item_ID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, itemID);
            ResultSet set = pstmt.executeQuery();

            if (set.next())
                return true;
            else
                return false;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

//======================================================================================================================================================================
//Methods for Item Type.

    //TODO: Add comments for method
    public boolean addItemType(String itemTypeName){
        final String query = "INSERT INTO ItemType (item_Type) VALUES (?)";

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){

            if (itemTypeExists(itemTypeName)){
                System.out.println("ERROR: Unable to add item-type. \nItem Type already exists: " + itemTypeName);
                return false;
            }

            pstmt.setString(1, itemTypeName);
            return pstmt.executeUpdate() > 0;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public ItemType getItemType(String itemTypeName){
        final String query = """
            SELECT 
                it.itemType_ID AS "Item Type ID",
                it.item_Type AS "Item Type Name"
            FROM ItemType as it
            WHERE item_Type = ?
            """;

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setString(1, itemTypeName);
            ResultSet set = pstmt.executeQuery();

            if (set.next())
                return new ItemType(set.getInt("Item Type ID"), set.getString("Item Type Name"));
            else
                return null;

        }   catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public ItemType getItemType(int itemTypeID){
        final String query = """
            SELECT 
                it.itemType_ID AS "Item Type ID",
                it.item_Type AS "Item Type Name"
            FROM ItemType as it
            WHERE itemType_ID = ?
            """;

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setInt(1, itemTypeID);
            ResultSet set = pstmt.executeQuery();

            if (set.next())
                return new ItemType(set.getInt("Item Type ID"), set.getString("Item Type Name"));
            else
                return null;

        }   catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //TODO: Add comments for method
    public int getItemTypeID(String itemTypeName){
        final String query = "SELECT * FROM ItemType AS it WHERE it.item_Type = ?";

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setString(1, itemTypeName);
            ResultSet set = pstmt.executeQuery();

            if (set.next())
                return set.getInt("itemType_ID");
            else
                return -1;

        }   catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public ItemType [] getAllItemTypes(){
        final String query = """
            SELECT 
                it.itemType_ID AS "Item Type ID",
                it.item_Type AS "Item Type Name"
            FROM ItemType as it
            ORDER BY it.itemType_ID ASC;
            """;

        if (connection == null)
            prepareConnection();

        try(Statement stmt = connection.createStatement();){
            ResultSet set = stmt.executeQuery(query);

            List<ItemType> list = new ArrayList<>();
            boolean isAdded = false;
            while (set.next()){
                list.add(new ItemType(set.getInt("Item Type ID"), set.getString("Item Type Name")));
                isAdded = true;
            }

            if (isAdded){
                ItemType [] array = new ItemType[list.size()];
                return list.toArray(array);
            }   else
                return null;

        }   catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //TODO: Add comments for the method
    public boolean itemTypeExists(String itemTypeName){
        final String query = "SELECT * FROM ItemType WHERE item_Type = ?";

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setString(1, itemTypeName);
            ResultSet set = pstmt.executeQuery();
            return set.next();

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //TODO: Add comments for the method
    public boolean itemTypeExists(int itemTypeID){
        final String query = "SELECT * FROM ItemType WHERE itemType_ID = ?";

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setInt(1, itemTypeID);
            ResultSet set = pstmt.executeQuery();
            return set.next();

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //TODO: Add comments for the method
    public boolean updateItemType(int itemTypeID, String newItemTypeName){
        final String query = "UPDATE ItemType SET item_Type = ? WHERE itemType_ID = ?";

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setString(1, newItemTypeName);
            pstmt.setInt(2, itemTypeID);

            return pstmt.executeUpdate() > 0;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //TODO: Add comments for this method
    public boolean removeItemType(String itemTypeName){
        if (connection == null)
            prepareConnection();

        if (!itemTypeExists(itemTypeName))
            return false;

        int itemTypeID = getItemTypeID(itemTypeName);
        if (itemTypeID == -1)
            return false;

        if (!removeItemUnitType(itemTypeID, REMOVE_ITEM_TYPE))
            return false;

        final String query = "DELETE FROM ItemType WHERE itemType_ID = ?";

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setInt(1, itemTypeID);
            return pstmt.executeUpdate() > 0;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public int getAffectedItems(ItemType item){
        List<ItemType> list = new ArrayList<>();
        list.add(item);
        ItemType [] array = new ItemType[1];
        return getAffectedItems(list.toArray(array));
    }

    /**
     * Gets all the items using a specific itemType
     *
     * @param list  Contains the list of items to be searched for
     * @return      Number of items using the specific ItemType
     */

    public int getAffectedItems(ItemType [] list){
        if (connection == null)
            prepareConnection();

        String query = """
            SELECT 
                COUNT(i.item_ID) AS "Item Count"
            FROM ItemType AS it
            JOIN itemUnitType AS iut ON it.itemType_ID = iut.itemType_ID
            JOIN Items AS i ON i.item_unit_ID = iut.item_unit_ID
            WHERE it.itemType_ID = ?
        """;

        int itemCount = 0;
        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            for (ItemType item : list){
                pstmt.setInt(1, item.getItemTypeID());
                ResultSet set = pstmt.executeQuery();
                if (set.next())
                    itemCount += set.getInt("Item Count");
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return itemCount;
    }

//======================================================================================================================================================================
//Methods for the Unit Type.

    //TODO: Add comments for this method
    public boolean addUnitType(String unitTypeName){
        final String query = "INSERT INTO UnitType (unit_Type) VALUES (?)";

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){

            if (unitTypeExists(unitTypeName)){
                System.out.println("ERROR: Unable to add new Unit Type. \nUnit Type already exists: " + unitTypeName);
                return false;
            }

            pstmt.setString(1, unitTypeName);
            return pstmt.executeUpdate() > 0;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //TODO: Add comments for this method
    public boolean unitTypeExists(String unitTypeName){
        final String query = "SELECT * FROM UnitType WHERE unit_Type = ?";

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setString(1, unitTypeName);
            ResultSet set = pstmt.executeQuery();
            return set.next();

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean unitTypeExists(int unitTypeID){
        final String query = "SELECT * FROM UnitType WHERE unitType_ID = ?";

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setInt(1, unitTypeID);
            ResultSet set = pstmt.executeQuery();
            if (set.next())
                return true;
            else
                return false;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //TODO: Add comments for this method
    public UnitType getUnitType(int unitTypeID){
        final String query = """
                SELECT 
                    u.unitType_ID AS "Unit Type ID",
                    u.unit_Type AS "Unit Type Name"
                FROM UnitType as u
                WHERE u.unitType_ID = ?
                """;

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            if (!unitTypeExists(unitTypeID))
                return null;

            pstmt.setInt(1, unitTypeID);
            ResultSet set = pstmt.executeQuery();
            if (set.next())
                return new UnitType(set.getInt("Unit Type ID"), set.getString("Unit Type Name"));
        }   catch (Exception e){
            e.printStackTrace();
            return null;
        }

        return null;
    }

    //TODO: Add comments for this method
    public int getUnitTypeID(String unitTypeName){
        final String query = "SELECT unitType_ID AS \"Unit Type ID\" FROM UnitType WHERE unit_Type = ?";

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setString(1, unitTypeName);
            ResultSet set = pstmt.executeQuery();
            if (set.next())
                return set.getInt("Unit Type ID");
            else
                return -1;

        }   catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public UnitType[] getAllUnitTypes() {
        final String query = """
            SELECT 
                ut.unitType_ID AS "Unit Type ID",
                ut.unit_Type AS "Unit Type Name"
            FROM UnitType as ut
            ORDER BY ut.unitType_ID ASC;
            """;

        if (connection == null)
            prepareConnection();

        try(Statement stmt = connection.createStatement();){
            ResultSet set = stmt.executeQuery(query);

            List<UnitType> list = new ArrayList<>();
            boolean isAdded = false;
            while (set.next()){
                list.add(new UnitType(set.getInt("Unit Type ID"), set.getString("Unit Type Name")));
                isAdded = true;
            }

            if (isAdded){
                UnitType [] array = new UnitType[list.size()];
                return list.toArray(array);
            }   else
                return null;

        }   catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public UnitType getUnitType(String unitType) {
        final String query = """
                SELECT 
                    ut.unitType_ID AS "Unit Type ID",
                    ut.unit_Type AS "Unit Type Name"
                FROM UnitType as ut
                WHERE unit_Type = ?
                """;

        if (connection == null)
            prepareConnection();

        try (PreparedStatement pstmt = connection.prepareStatement(query);) {
            pstmt.setString(1, unitType);
            ResultSet set = pstmt.executeQuery();

            if (set.next())
                return new UnitType(set.getInt("Unit Type ID"), set.getString("Unit Type Name"));
            else
                return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateUnitType(int unitTypeID, String newUnitTypeName){
        final String query = "UPDATE UnitType SET unit_Type = ? WHERE unitType_ID = ?";

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setString(1, newUnitTypeName);
            pstmt.setInt(2, unitTypeID);

            return pstmt.executeUpdate() > 0;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //TODO: Add comments for this method
    public boolean removeUnitType(String unitTypeName){
        if (connection == null)
            prepareConnection();

        if (!unitTypeExists(unitTypeName))
            return false;

        int unitTypeID = getUnitTypeID(unitTypeName);
        if (unitTypeID == -1)
            return false;

        if (!removeItemUnitType(unitTypeID, REMOVE_UNIT_TYPE))
            return false;

        final String query = "DELETE FROM UnitType WHERE unitType_ID = ?";

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setInt(1, unitTypeID);
            return pstmt.executeUpdate() > 0;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public int getAffectedItemsForUnit(UnitType unit){
        List<UnitType> list = new ArrayList<>();
        list.add(unit);
        UnitType [] array = new UnitType[1];
        return getAffectedItemsForUnit(list.toArray(array));
    }


    //UNIT TYPE LIST
    public int getAffectedItemsForUnit(UnitType [] list){
        if (connection == null)
            prepareConnection();

        String query = """
            SELECT 
                COUNT(i.item_ID) AS "Item Count"
            FROM UnitType AS ut
            JOIN itemUnitType AS iut ON ut.unitType_ID = iut.unitType_ID
            JOIN Items AS i ON i.item_unit_ID = iut.item_unit_ID
            WHERE ut.unitType_ID = ?
        """;

        int itemCount = 0;
        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            for (UnitType unit : list){
                pstmt.setInt(1, unit.getUnitTypeID());
                ResultSet set = pstmt.executeQuery();
                if (set.next())
                    itemCount += set.getInt("Item Count");
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return itemCount;
    }

//======================================================================================================================================================================
//Methods for the ItemUnitType.

    //TODO: Add comments to this method
    public boolean addItemUnitType(String itemTypeName, String unitTypeName){
        int itemTypeID = getItemTypeID(itemTypeName);
        int unitTypeID = getUnitTypeID(unitTypeName);
        return addItemUnitType(itemTypeID, unitTypeID);
    }

    public boolean addItemUnitType(int itemTypeID, int unitTypeID){
        final String query = "INSERT INTO ItemUnitType (itemType_ID, unitType_ID) VALUES (?, ?)";

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){

            if (!unitTypeExists(unitTypeID) || !itemTypeExists(itemTypeID)){
                System.out.println("ERROR: Unable to add new Item-Unit Type. \n Item-Type or Unit-Type doesn't exist.");
                return false;
            }

            if (itemUnitTypeExists(itemTypeID, unitTypeID)){
                System.out.println("ERROR: Unable to add new Item-Unit Type. \n This Item Unit Type already exists.");
                return false;
            }

            pstmt.setInt(1, itemTypeID);
            pstmt.setInt(2, unitTypeID);
            return pstmt.executeUpdate() > 0;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //TODO: Add comments
    public boolean itemUnitTypeExists(int itemUnitTypeID){
        final String query = "SELECT * FROM ItemUnitType WHERE item_unit_ID = ?";

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setInt(1, itemUnitTypeID);
            ResultSet set = pstmt.executeQuery(query);
            return (set.next());

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //TODO: Add comments to this method
    public boolean itemUnitTypeExists(int itemTypeID, int unitTypeID){
        final String query = "SELECT * FROM ItemUnitType WHERE unitType_ID = ? AND itemType_ID = ?";

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setInt(1, unitTypeID);
            pstmt.setInt(2, itemTypeID);
            ResultSet set = pstmt.executeQuery();
            return (set.next());

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //TODO: Add comments to this method
    public boolean itemUnitTypeExists(String itemTypeName, String unitTypeName){
        final String query = """
            SELECT * 
            FROM ItemUnitType AS iut
            JOIN ItemType AS it ON it.itemType_ID = iut.itemType_ID
            JOIN UnitType AS ut ON ut.unitType_ID = iut.unitType_ID 
            WHERE it.item_Type = ? AND ut.unit_Type = ?;
            """;

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setString(1, itemTypeName);
            pstmt.setString(2, unitTypeName);
            ResultSet set = pstmt.executeQuery(query);
            return (set.next());

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //TODO: Add comments to this method
    public ItemUnitType getItemUnitType(int itemTypeID, int unitTypeID){
        final String query = """
            SELECT
                 iut.item_unit_ID AS "Item Unit ID",
                 iut.itemType_ID AS "Item Type ID",
                 iut.unitType_ID AS "Unit Type ID",
                 it.item_Type AS "Item Type Name",
                 ut.unit_Type AS "Unit Type Name"
            FROM ItemUnitType AS iut
            JOIN ItemType AS it ON iut.itemType_ID = it.itemType_ID
            JOIN UnitType AS ut ON iut.unitType_ID = ut.unitType_ID
            WHERE iut.unitType_ID = ? AND iut.itemType_ID = ?;
            """;

        if (connection == null)
            prepareConnection();

        if (!itemUnitTypeExists(itemTypeID, unitTypeID))
            return null;

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setInt(1, unitTypeID);
            pstmt.setInt(2, itemTypeID);
            ResultSet set = pstmt.executeQuery();
            if (set.next())
                return new ItemUnitType(set.getInt("Item Unit ID"), itemTypeID,
                                        unitTypeID, set.getString("Item Type Name"), set.getString("Unit Type Name"));

            return null;
        }   catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //TODO: Add comments to this method
    public int getItemUnitTypeID(String itemTypeName, String unitTypeName){
        int itemTypeID = getItemTypeID(itemTypeName);
        int unitTypeID = getUnitTypeID(unitTypeName);
        return getItemUnitTypeID(itemTypeID, unitTypeID);
    }

    //TODO: Add comments to this method
    public int getItemUnitTypeID(int itemTypeID, int unitTypeID){
        final String query = """
            SELECT
                 iut.item_unit_ID AS "Item Unit ID"
            FROM ItemUnitType AS iut
            WHERE iut.unitType_ID = ? AND iut.itemType_ID = ?
            """;

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setInt(1, unitTypeID);
            pstmt.setInt(2, itemTypeID);
            ResultSet set = pstmt.executeQuery();
            if (set.next())
                return set.getInt("Item Unit ID");

            return -1;
        }   catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public ItemUnitType[] getAllItemUnitTypes() {
        final String query = """
                SELECT
                    iut.item_unit_ID AS "Item Unit Type ID",
                    iut.itemType_ID AS "Item Type ID",
                    it.item_Type AS "Item Type Name",
                    iut.unitType_ID AS "Unit Type ID",
                    ut.unit_Type AS "Unit Type Name"
                FROM ItemType AS it
                JOIN itemUnitType AS iut ON it.itemType_ID = iut.itemType_ID
                JOIN UnitType AS ut ON ut.unitType_ID = iut.unitType_ID
                ORDER BY iut.item_unit_ID;
            """;

        if (connection == null)
            prepareConnection();

        try(Statement stmt = connection.createStatement();){
            ResultSet set = stmt.executeQuery(query);

            List<ItemUnitType> list = new ArrayList<>();
            boolean isAdded = false;
            while (set.next()){
                list.add(new ItemUnitType(set.getInt("Item Unit Type ID"),
                        set.getInt("Item Type ID"),
                        set.getInt("Unit Type ID"),
                        set.getString("Item Type Name"),
                        set.getString("Unit Type Name")));
                isAdded = true;
            }

            if (isAdded){
                ItemUnitType [] array = new ItemUnitType[list.size()];
                return list.toArray(array);
            }   else
                return null;

        }   catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateItemUnitType(int itemUnitTypeID, int itemTypeID, int unitTypeID){
        final String query = """
                UPDATE
                ItemUnitType AS iut
                SET iut.itemType_ID = ?, iut.unitType_ID = ? WHERE iut.itemUnitType_ID = ?

                """;

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setInt(1, itemTypeID);
            pstmt.setInt(2, unitTypeID);
            pstmt.setInt(3, itemUnitTypeID);

            return pstmt.executeUpdate() > 0;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

    //TODO: Add explanation for why these variables are made/used
    public static final int REMOVE_ITEM_TYPE = 6969;
    public static final int REMOVE_UNIT_TYPE = 9696;
    public static final int REMOVE_ITEM_UNIT_TYPE = 9999;

    //TODO: Add comments
    public boolean removeItemUnitType(int id, int condition){
        //First set of queries to delete items who uses the following item_unit_ID or itemType_ID or unitType_ID
        final String firstA = "DELETE FROM Items WHERE (SELECT item_unit_ID FROM ItemUnitType WHERE itemType_ID = " + id + ")";
        final String firstB = "DELETE FROM Items WHERE (SELECT item_unit_ID FROM ItemUnitType WHERE unitType_ID = " + id + ")";
        final String firstC = "DELETE FROM Items WHERE (SELECT item_unit_ID FROM ItemUnitType WHERE item_unit_ID = " + id + ")";

        //Second set of queries to remove the item_unit_type IDs
        final String removeItemType = "DELETE FROM ItemUnitType WHERE itemType_ID = " + id;
        final String removeUnitType = "DELETE FROM ItemUnitType WHERE unitType_ID = " + id;
        final String removeItemUnitType = "DELETE FROM ItemUnitType WHERE item_unit_ID = " + id;

        if (connection == null)
            prepareConnection();

        try(Statement stmt = connection.createStatement();){
            if (condition == REMOVE_ITEM_TYPE){
                stmt.executeUpdate(firstA);
                stmt.executeUpdate(removeItemType);
            } else if (condition == REMOVE_UNIT_TYPE){
                stmt.executeUpdate(firstB);
                stmt.executeUpdate(removeUnitType);
            } else if (condition == REMOVE_ITEM_UNIT_TYPE){
                stmt.executeUpdate(firstC);
                stmt.executeUpdate(removeItemUnitType);
            } else
                return false;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public int getAffectedItemsForItemUnit(ItemUnitType unit){
        List<ItemUnitType> list = new ArrayList<>();
        list.add(unit);
        ItemUnitType [] array = new ItemUnitType[1];
        return getAffectedItemsForItemUnit(list.toArray(array));
    }

    public int getAffectedItemsForItemUnit(ItemUnitType [] list){
        if (connection == null)
            prepareConnection();

        String query = """
            SELECT 
                COUNT(i.item_ID) AS "Item Count"
            FROM ItemUnitType AS iut
            JOIN Items AS i ON i.item_unit_ID = iut.item_unit_ID
            WHERE iut.item_unit_ID = ?
        """;

        int itemCount = 0;
        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            for (ItemUnitType unit : list){
                pstmt.setInt(1, unit.getItemUnitTypeID());
                ResultSet set = pstmt.executeQuery();
                if (set.next())
                    itemCount += set.getInt("Item Count");
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return itemCount;
    }

//======================================================================================================================================================================
//Methods for Restocks.

    //TODO: Add comments to this method
    public boolean addRestock(int itemID, int startQuantity, double wholesaleCost, Date restockDate, Date expiryDate){
        final String query = """
            INSERT INTO Restocks (item_ID, start_Qty, sold_Qty, wholesale_cost, restock_Date, expiry_Date)
            VALUES (?, ?, 0, ?, ?, ?)
            """;

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);
            Statement stmt = connection.createStatement();){

            //Checks if an item exists
            if (!itemTypeExists(itemID)){
                System.out.println("ERROR: Unable to add new Restock. \n Item via Item ID doesn't exist: " + itemID);
                return false;
            }

            //Checks if the expiry date is on or after the current date
            if (!Date.valueOf(getCurrentDate()).before(expiryDate) && !Date.valueOf(getCurrentDate()).equals(expiryDate))
                return false;

            pstmt.setInt(1, itemID);
            pstmt.setInt(2, startQuantity);
            pstmt.setDouble(3, wholesaleCost);
            pstmt.setDate(4, restockDate);
            pstmt.setDate(5, expiryDate);
            return pstmt.executeUpdate() > 0;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //  Make this method private
    public boolean reduceRestocks(int itemID, int soldQuantity){
        if (connection == null)
            prepareConnection();

        int result = getRemainingStockQuantity(itemID);
        if (soldQuantity > result){
            System.out.println("ERROR: Unable to reduce restocks. \nSold quantity is above the current stock: " + soldQuantity);
            return false;
        }   else if (result == -1){
                System.out.println("ERROR: Unable to reduce restocks. \nItem under the item id doesn't exist: " + itemID);
                return false;
        }

        //Collect the totalRestocks through an array (Sort by ID ASC)
        Restocks [] currentStock = getCurrentStock(itemID);

        if (currentStock == null || currentStock.length == 0){
            System.out.println("ERROR: Unable to reduce restocks. \nItem doesn't exist or no stock is found under this item");
            return false;
        }

        final String query = "UPDATE Restocks SET sold_Qty = ? WHERE restock_ID = ?";

        //Slowly reduce-update then propagate along the restocks array
        for (Restocks stock : currentStock){
            try(PreparedStatement pstmt = connection.prepareStatement(query)){
                pstmt.setInt(2, stock.getRestockID());

                if (soldQuantity >= stock.getRemainingQty()){                                           //Sold Quantity is more than the current restock in the array
                    soldQuantity -= stock.getRemainingQty();
                    pstmt.setInt(1, stock.getRemainingQty() + stock.getSoldQty());
                } else {                                                                                //Sold quantity is less than the current restock qty in the array
                    pstmt.setInt(1, soldQuantity + stock.getSoldQty());
                    soldQuantity = 0;
                }

                pstmt.executeUpdate();

                if (soldQuantity <= 0)
                    break;

            }   catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    /**
     * Gets restocks based from a specific restock ID
     */
    public Restocks getRestock(int restockID){
        String query = """
            SELECT
                r.restock_ID AS "Restock ID",
                r.item_ID AS "Item ID",
                r.start_Qty AS "Start Quantity",
                r.sold_Qty AS "Sold Quantity",
                r.wholesale_cost AS "Wholesale Cost",
                r.restock_Date AS "Restock Date",
                r.expiry_Date AS "Expiry Date",
                i.item_Name AS "Item Name"
            FROM Restocks AS r
            JOIN Items AS i ON i.item_ID = r.item_ID
            WHERE r.restock_ID = ?;
        """;

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setInt(1, restockID);
            ResultSet set = pstmt.executeQuery();

            if (set.next()){
                return (new Restocks(set.getInt("Item ID"), set.getString("Item Name"), set.getInt("Restock ID"),
                                     set.getInt("Start Quantity"), set.getInt("Sold Quantity"),
                                     set.getDouble("Wholesale Cost"), set.getDate("Restock Date"),
                                     set.getDate("Expiry Date")));
            } else
                return null;

        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets all valid restock (Not Expired and still has Stock, Start Qty > Sold Qty)
     */
    public Restocks [] getCurrentStock(int itemID){
        String query = """
            SELECT
                r.restock_ID AS "Restock ID",
                r.item_ID AS "Item ID",
                r.start_Qty AS "Start Quantity",
                r.sold_Qty AS "Sold Quantity",
                r.wholesale_cost AS "Wholesale Cost",
                r.restock_Date AS "Restock Date",
                r.expiry_Date AS "Expiry Date",
                i.item_Name AS "Item Name"
            FROM Restocks AS r
            JOIN Items AS i ON i.item_ID = r.item_ID
            WHERE r.expiry_Date >= CURRENT_DATE() AND r.start_Qty > r.sold_Qty AND r.item_ID = ?
            ORDER BY r.restock_ID ASC;
        """;

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setInt(1, itemID);
            ResultSet set = pstmt.executeQuery();
            boolean isAdded = false;

            List<Restocks> list = new ArrayList<>();
            while(set.next()){
                list.add(new Restocks(set.getInt("Item ID"), set.getString("Item Name"), set.getInt("Restock ID"),
                                      set.getInt("Start Quantity"), set.getInt("Sold Quantity"),
                                      set.getDouble("Wholesale Cost"), set.getDate("Restock Date"),
                                      set.getDate("Expiry Date")));
                isAdded = true;
            }

            if (isAdded){
                Restocks [] array = new Restocks[list.size()];
                return list.toArray(array);
            }   else
                return null;


        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets all restocks of an item both valid (Not Expired and still has Stock, Start Qty > Sold Qty) and invalid
     *
     *
     */
    public Restocks [] getItemRestock(int itemID){
        String query = """
            SELECT
                r.restock_ID AS "Restock ID",
                r.item_ID AS "Item ID",
                r.start_Qty AS "Start Quantity",
                r.sold_Qty AS "Sold Quantity",
                r.wholesale_cost AS "Wholesale Cost",
                r.restock_Date AS "Restock Date",
                r.expiry_Date AS "Expiry Date",
                i.item_Name AS "Item Name"
            FROM Restocks AS r
            JOIN Items AS i ON i.item_ID = r.item_ID
            WHERE r.item_ID = ?
            ORDER BY r.restock_ID ASC;
        """;

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setInt(1, itemID);
            ResultSet set = pstmt.executeQuery();
            boolean isAdded = false;

            List<Restocks> list = new ArrayList<>();
            while(set.next()){
                list.add(new Restocks(set.getInt("Item ID"), set.getString("Item Name"), set.getInt("Restock ID"),
                        set.getInt("Start Quantity"), set.getInt("Sold Quantity"),
                        set.getDouble("Wholesale Cost"), set.getDate("Restock Date"),
                        set.getDate("Expiry Date")));
                isAdded = true;
            }

            if (isAdded){
                Restocks [] array = new Restocks[list.size()];
                return list.toArray(array);
            }   else
                return null;


        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets all restocks saved in the database
     */
    public Restocks [] getAllRestocks(){
        String query = """
        SELECT
            r.restock_ID AS "Restock ID",
            r.item_ID AS "Item ID",
            r.start_Qty AS "Start Quantity",
            r.sold_Qty AS "Sold Quantity",
            r.wholesale_cost AS "Wholesale Cost",
            r.restock_Date AS "Restock Date",
            r.expiry_Date AS "Expiry Date",
            i.item_Name AS "Item Name"
        FROM Restocks AS r
        JOIN Items AS i ON i.item_ID = r.item_ID
        ORDER BY r.restock_ID ASC;
    """;

        if (connection == null)
            prepareConnection();

        try(Statement stmt = connection.createStatement()){
            ResultSet set = stmt.executeQuery(query);
            boolean isAdded = false;

            List<Restocks> list = new ArrayList<>();
            while(set.next()){
                list.add(new Restocks(set.getInt("Item ID"), set.getString("Item Name"), set.getInt("Restock ID"),
                        set.getInt("Start Quantity"), set.getInt("Sold Quantity"),
                        set.getDouble("Wholesale Cost"), set.getDate("Restock Date"),
                        set.getDate("Expiry Date")));
                isAdded = true;
            }

            if (isAdded){
                Restocks [] array = new Restocks[list.size()];
                return list.toArray(array);
            }   else
                return null;

        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks for the existence of an item and gets remaining stock for a specific item based from the SQL
     * server
     *
     * @param itemID    integer representing the item_ID (primary key) of an item row found
     *                  in the Items table (SQL)
     * @return          An integer representing the remaining stock for an item
     */
    public int getRemainingStockQuantity(int itemID){
        final String query = """
            SELECT (SUM(r.start_Qty - r.sold_Qty)) AS "Total Quantity"
            FROM Restocks AS r 
            WHERE r.expiry_Date >= CURRENT_DATE() AND r.start_Qty > r.sold_Qty;
        """;

        if (connection == null)
            prepareConnection();

        try(Statement stmt = connection.prepareStatement(query);){

            if (!itemExists(itemID)){
                System.out.println("ERROR: Unable to get total quantity of item. \nItem with this item id doesn't exist: " + itemID);
                return -1;
            }

            ResultSet set = stmt.executeQuery(query);
            if (set.next())
                return set.getInt("Total Quantity");

            return -1;

        }   catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public static final int SEARCH_BY_ITEM_ID = 3000;
    public static final int SEARCH_BY_RESTOCK_ID = 4000;

    //TODO: Add comments for this method
    public boolean restocksExists(int id, int condition){
        if (connection == null)
            prepareConnection();

        String first = "SELECT * FROM Restocks AS r WHERE r.item_ID = ?";
        String second = "SELECT * FROM Restocks AS r WHERE r.restock_ID = ?";
        String finalQuery;

        if (condition == REMOVE_BY_RESTOCK_ID)
            finalQuery = second;
        else if (condition == REMOVE_BY_ITEM_ID)
            finalQuery = first;
        else {
            System.out.println("ERROR: Unable to remove restock. \nInvalid restock removal condition used.");
            return false;
        }

        try(PreparedStatement pstmt = connection.prepareStatement(finalQuery);){
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static final int REMOVE_BY_ITEM_ID = 1000;
    public static final int REMOVE_BY_RESTOCK_ID = 2000;

    /**
     * Method to remove restock based on ItemID or Restock ID
     */
    public boolean removeRestock(int id, int condition){
        if (connection == null)
            prepareConnection();

        String first = "DELETE FROM Restocks AS r WHERE r.item_ID = ?";
        String second = "DELETE FROM Restocks AS r WHERE r.restock_ID = ?";
        String finalQuery;

        if (condition == REMOVE_BY_RESTOCK_ID)
            finalQuery = second;
        else if (condition == REMOVE_BY_ITEM_ID)
            finalQuery = first;
        else {
            System.out.println("ERROR: Unable to remove restock. \nInvalid restock removal condition used.");
            return false;
        }

        try(PreparedStatement pstmt = connection.prepareStatement(finalQuery);
            Statement stmt = connection.createStatement();){

            //Checks if an item exists
            if (condition == REMOVE_BY_ITEM_ID && !itemTypeExists(id)){
                System.out.println("ERROR: Unable to remove from Restock. \n Item via Item ID doesn't exist: " + id);
                return false;
            }   else if (condition == REMOVE_BY_RESTOCK_ID && !restocksExists(id, SEARCH_BY_RESTOCK_ID)){
                System.out.println("ERROR: Unable to remove from Restock. \n Restock ID doesn't exist: " + id);
                return false;
            }

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


//======================================================================================================================================================================
//Methods for the Transactions.

    //TODO: Add methods for transactions (CR & RT)

    public boolean addTransaction(int pharmacistID){
        if (connection == null)
            prepareConnection();

        String query = "INSERT INTO Transactions (pharmacist_ID, transaction_date) VALUES (?, ?)";
        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setInt(1, pharmacistID);
            pstmt.setDate(2, Date.valueOf(getCurrentDate()));
            return pstmt.executeUpdate() > 0;

        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //TODO: Add comments to this method
    public boolean transactionExists(int transactionID){
        String query = "SELECT * FROM Transactions AS t WHERE t.transaction_ID = ?";

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setInt(1, transactionID);
            ResultSet set = pstmt.executeQuery();

            return (set.next());

        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //TODO: Add comments for this method
    public Transaction getTransaction (int transactionID){
        String query = """
            SELECT
                t.transaction_ID AS "Transaction ID",
                t.pharmacist_ID AS "Pharmacist ID",
                t.transaction_date AS "Transaction Date",
                SUM(isd.item_qty * i.unit_cost) AS "Total Sales",
                SUM(isd.item_qty) AS "Sold Quantity"
            FROM Transactions AS t
            LEFT JOIN Sold_Items AS isd ON isd.transaction_ID = t.transaction_ID
            LEFT JOIN Items AS i ON i.item_ID = isd.item_ID
            WHERE t.transaction_ID = ?
            GROUP BY t.transaction_ID;
        """;

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setInt(1, transactionID);
            ResultSet set = pstmt.executeQuery();

            if (set.next()){
                return new Transaction(set.getInt("Transaction ID"), set.getInt("Pharmacist ID"),
                                       set.getInt("Sold Quantity"), set.getInt("Total Sales"), set.getDate("Transaction Date")));
            } else
                return null;

        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets all transactions based on a specific date
     */
    public Transaction [] getTransactions(Date referenceDate, boolean isAscending){
        String first = """
            SELECT
                t.transaction_ID AS "Transaction ID",
                t.pharmacist_ID AS "Pharmacist ID",
                t.transaction_date AS "Transaction Date",
                SUM(isd.item_qty * i.unit_cost) AS "Total Sales",
                SUM(isd.item_qty) AS "Sold Quantity"
            FROM Transactions AS t
            LEFT JOIN Sold_Items AS isd ON isd.transaction_ID = t.transaction_ID
            LEFT JOIN Items AS i ON i.item_ID = isd.item_ID
            WHERE t.transaction_date = ?
            GROUP BY t.transaction_ID
            ORDER BY t.transaction_ID DESC;
        """;

        String second = """
            SELECT
                t.transaction_ID AS "Transaction ID",
                t.pharmacist_ID AS "Pharmacist ID",
                t.transaction_date AS "Transaction Date",
                SUM(isd.item_qty * i.unit_cost) AS "Total Sales",
                SUM(isd.item_qty) AS "Sold Quantity"
            FROM Transactions AS t
            LEFT JOIN Sold_Items AS isd ON isd.transaction_ID = t.transaction_ID
            LEFT JOIN Items AS i ON i.item_ID = isd.item_ID
            WHERE t.transaction_date = ?
            GROUP BY t.transaction_ID
            ORDER BY t.transaction_ID DESC;
        """;

        String finalQuery;

        if (isAscending)
            finalQuery = first;
        else
            finalQuery = second;

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(finalQuery)){
            pstmt.setDate(1, referenceDate);
            ResultSet set = pstmt.executeQuery();
            boolean isAdded = false;

            List<Transaction> list = new ArrayList<>();
            while(set.next()){
                list.add(new Transaction(set.getInt("Transaction ID"), set.getInt("Pharmacist ID"),
                                         set.getInt("Sold Quantity"), set.getInt("Total Sales"), set.getDate("Transaction Date")));
                isAdded = true;
            }

            if (isAdded){
                Transaction [] array = new Transaction[list.size()];
                return list.toArray(array);
            }   else
                return null;


        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets all transactions saved in the database
     *
     * @param isAscending   boolean parameter to determine the order of the transactions taken from
     *                      the database (Ascending if true or Descending if false)
     * @return              An array of Transactions that is ordered in Ascending or Descending manner base on the
     *                      transaction ID
     */
    public Transaction [] getAllTransactions(boolean isAscending){
        String first = """
            SELECT
                t.transaction_ID AS "Transaction ID",
                t.pharmacist_ID AS "Pharmacist ID",
                t.transaction_date AS "Transaction Date",
                SUM(isd.item_qty * i.unit_cost) AS "Total Sales",
                SUM(isd.item_qty) AS "Sold Quantity"
            FROM Transactions AS t
            LEFT JOIN Sold_Items AS isd ON isd.transaction_ID = t.transaction_ID
            LEFT JOIN Items AS i ON i.item_ID = isd.item_ID
            GROUP BY t.transaction_ID
            ORDER BY t.transaction_ID ASC;
        """;

        String second = """
            SELECT
                t.transaction_ID AS "Transaction ID",
                t.pharmacist_ID AS "Pharmacist ID",
                t.transaction_date AS "Transaction Date",
                SUM(isd.item_qty * i.unit_cost) AS "Total Sales",
                SUM(isd.item_qty) AS "Sold Quantity"
            FROM Transactions AS t
            LEFT JOIN Sold_Items AS isd ON isd.transaction_ID = t.transaction_ID
            LEFT JOIN Items AS i ON i.item_ID = isd.item_ID
            GROUP BY t.transaction_ID
            ORDER BY t.transaction_ID DESC;
        """;

        String finalQuery;

        if (isAscending)
            finalQuery = first;
        else
            finalQuery = second;

        if (connection == null)
            prepareConnection();

        try(Statement stmt = connection.createStatement()){
            ResultSet set = stmt.executeQuery(finalQuery);
            boolean isAdded = false;

            List<Transaction> list = new ArrayList<>();
            while(set.next()){
                list.add(new Transaction(set.getInt("Transaction ID"), set.getInt("Pharmacist ID"),
                                         set.getInt("Sold Quantity"), set.getInt("Total Sales"), set.getDate("Transaction Date")));
                isAdded = true;
            }

            if (isAdded){
                Transaction [] array = new Transaction[list.size()];
                return list.toArray(array);
            }   else
                return null;

        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

//======================================================================================================================================================================
//Methods for the Items Sold

    /**
     * Adds an item sold based from a specific transaction made
     *
     * @param transactionID     The ID serving as the reference to which transaction the item is sold to
     * @param itemID            The ID of the item sold. Reference to the Items Table
     * @param itemQty           The amount of units sold
     */
    public boolean addItemsSold(int transactionID, int itemID, int itemQty){
        if (connection == null)
            prepareConnection();

        if (!getTransaction(transactionID).getDate().toLocalDate().isEqual(getCurrentDate())){
            System.out.println("ERROR: Unable to add Item to Item Sold. \nReference Transaction and Item Sold found to have different dates");
            return false;
        }

        if (!reduceRestocks(itemID, itemQty))
            return false;

        String query = """
            INSERT INTO Sold_Items (transaction_ID, item_ID, item_qty, transaction_date) VALUES
            (?, ?, ?, CURRENT_DATE())
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setInt(1, transactionID);
            pstmt.setInt(2, itemID);
            pstmt.setInt(3, itemQty);

            return pstmt.executeUpdate() > 0;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets all the items sold based from a specific transaction
     *
     * @param transactionID     The ID of a specific transaction
     * @return                  An array of Sold_Items under the same transaction ID, may contain
     *                          a combination of different items with different quantities
     */
    public ItemsSold [] getItemsSold_Transaction(int transactionID){
        String query = """
            SELECT
                isd.transaction_ID AS "Transaction ID",
                isd.item_ID AS "Item ID",
                i.item_name AS "Item Name",
                i.unit_cost AS "Unit Cost",
                isd.item_qty AS "Item Quantity",
                (isd.item_qty * i.unit_cost) AS "Income",
                isd.transaction_date AS "Transaction Date"
            FROM Sold_Items AS isd
            JOIN Items AS i ON isd.item_ID = i.item_ID
            WHERE isd.transaction_ID = ?
            ORDER BY i.item_ID DESC;
        """;

        if (connection == null)
            prepareConnection();

        if (!transactionExists(transactionID))
            return null;

        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setInt(1, transactionID);
            ResultSet set = pstmt.executeQuery();
            boolean isAdded = false;

            List<ItemsSold> list = new ArrayList<>();
            while(set.next()){
                list.add(new ItemsSold(set.getInt("Transaction ID"),
                                       set.getInt("Item ID"),
                                       set.getString("Item Name"),
                                       set.getDouble("Unit Cost"),
                                       set.getInt("Item Quantity"),
                                       set.getDouble("Income"),
                                       set.getDate("Transaction Date").toLocalDate()));
                isAdded = true;
            }

            if (isAdded){
                ItemsSold [] array = new ItemsSold[list.size()];
                return list.toArray(array);
            }   else
                return null;

        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets all the items sold based from a specific item
     *
     * @param itemID            The ID of a specific item
     * @return                  An array of Sold_Items under the itemID, used to get the total sales
     *                          of an item for all transactions
     */
    public ItemsSold [] getItemsSold_Item(int itemID){
        String query = """
            SELECT
                isd.transaction_ID AS "Transaction ID",
                isd.item_ID AS "Item ID",
                i.item_name AS "Item Name",
                i.unit_cost AS "Unit Cost",
                isd.item_qty AS "Item Quantity",
                (isd.item_qty * i.unit_cost) AS "Income",
                isd.transaction_date AS "Transaction Date"
            FROM Sold_Items AS isd
            JOIN Items AS i ON isd.item_ID = i.item_ID
            WHERE i.item_ID = ?
            ORDER BY i.item_ID DESC;
        """;

        if (connection == null)
            prepareConnection();

        if (!itemExists(itemID))
            return null;

        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setInt(1, itemID);
            ResultSet set = pstmt.executeQuery();
            boolean isAdded = false;

            List<ItemsSold> list = new ArrayList<>();
            while(set.next()){
                list.add(new ItemsSold(set.getInt("Transaction ID"),
                        set.getInt("Item ID"),
                        set.getString("Item Name"),
                        set.getDouble("Unit Cost"),
                        set.getInt("Item Quantity"),
                        set.getDouble("Income"),
                        set.getDate("Transaction Date").toLocalDate()));
                isAdded = true;
            }

            if (isAdded){
                ItemsSold [] array = new ItemsSold[list.size()];
                return list.toArray(array);
            }   else
                return null;

        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Method used to remove records of sold items based on the itemID
     *
     * @param itemID    ID of the item used as reference to specify which records will be removed form
     *                  the database
     * @return          True if the records are removed successfully, False if the item doesn't exist
     */
    public boolean removeItemsSold(int itemID){
        final String query = "DELETE FROM Sold_Items AS isd WHERE isd.item_ID = ?";

        if (connection == null)
            prepareConnection();

        try(PreparedStatement pstmt = connection.prepareStatement(query);
            Statement stmt = connection.createStatement();){

            //Checks if an item exists
            if (!itemTypeExists(itemID)){
                System.out.println("ERROR: Unable to remove from Item Sold table. \n Item via Item ID doesn't exist: " + itemID);
                return false;
            }

            pstmt.setInt(1, itemID);
            return pstmt.executeUpdate() > 0;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


//======================================================================================================================================================================
//Methods for the View / Statistics.

    //TODO: Make views for the following...



//======================================================================================================================================================================
//Helper Methods

    //TODO: Add comments to this method
    public static String [][] reduceInfoArray(String [][] refArray, boolean isSorted, int... colIncluded){
        //Checks if the columns to be included is within the column size of the refArray
        for (int i : colIncluded){
            if (i >= refArray[0].length){
                System.out.println("Column Index: " + i + ", is larger than the column size of the array");
                return null;
            }
        }

        //Creates the new reduced array based on the specified length
        String [][] reducedArray = new String[refArray.length][colIncluded.length];
        for (int i = 0; i < refArray.length; i++){
            for (int j = 0; j < colIncluded.length; j++){
                reducedArray[i][j] = refArray[i][j];
            }
        }

        return reducedArray;
    }

    //TODO: Add comments to this method
    //Recursive method that establishes a connection with the SQL server
    //Added to help with code readability
    private void prepareConnection(){
        connection = DatabaseConnection.connect();
        if (connection == null){
            System.out.println("ERROR: Unable to prepare connection.");
        }
    }

    //TODO: Add comments for this method
    public LocalDate getCurrentDate(){
        String query = "SELECT CURRENT_DATE() AS \"Current Date\"";
        try(Statement stmt = connection.createStatement()){
            ResultSet set = stmt.executeQuery(query);

            if (set.next()){
                Date tempDate = set.getDate("Current Date");
                return tempDate.toLocalDate();
            } else
                return getCurrentDate();

        } catch (Exception e){
            e.printStackTrace();
            return getCurrentDate();
        }
    }
}
