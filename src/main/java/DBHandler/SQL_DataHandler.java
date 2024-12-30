package DBHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQL_DataHandler {

    public static void main (String [] args){
        SQL_DataHandler handler = new SQL_DataHandler();

        handler.removeAllPharmacists();
        handler.addPharmacist(1, "Allan George", "Cajes", "Tagle");
        handler.addPharmacist(2, "Waken Cean", "Cruz", "Maclang");
        handler.addPharmacist(3, "Dave Shanna", "Marie", "Gigawin");
        handler.addPharmacist(4, "John Marcellin", "Espanyol", "Tan");

        Pharmacist [] pharmacists = handler.getPharmacists(1, 4);
        for (Pharmacist pharmacist : pharmacists)
            System.out.println(pharmacist.getPharmacistID() + " " +  pharmacist.getFirstName() + " " + pharmacist.getMiddleName() + " " + pharmacist.getLastName());

        handler.removePharmacist(3);
        System.out.println(handler.pharmacistExists(3));

//        handler.removeAllItems();
        handler.addItem("Paracetamol", "Medicine", 5.0);
        handler.addItem("Biogesic", "Medicine", 4.0);
        handler.addItem("Neozep", "Medicine", 6.0);

        Item [] items = handler.getItems(10, 12);
        for (Item item : items)
            System.out.println(item.getItemID() + " " + item.getItemName() + " " + item.getUnitCost());

//        handler.removeItem("Biogesic");
//        System.out.println(handler.itemExists("Biogesic"));

        handler.addUnitType("Packet");
        UnitType type = handler.getUnitType(handler.getUnitTypeID("Packet"));
        System.out.println(type.getUnitTypeID() + " " + type.getUnitType());
    }

    //Makes a connection with the SQL server
    //Added to help with code readability
    private Connection prepareConnection(){
        Connection connection = DatabaseConnection.connect();
        if (connection == null)
            System.out.println("ERROR: Unable to prepare connection.");
        return connection;
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
        Connection connection;
        try {
            connection = prepareConnection();
        }   catch (Exception e){
            return false;
        }

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

            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();
            connection.close();
            return rowsAffected > 0;

        }   catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePharmacist(String firstName, String middleName, String lastName){
        return false;
    }

    /**
     * Finds a specific pharmacist based on their whole name based from the parameters @param firstName, @param middleName, and @param lastName.
     */
    public Pharmacist getPharmacist(int pharmaID){
        String query = "SELECT * FROM Pharmacists WHERE pharmacist_ID = " + pharmaID;
        try(Connection conn = DatabaseConnection.connect()) {
            assert conn != null;
            try(Statement stmt = conn.createStatement()){

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

            }
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
        Connection connection;

        try{
            connection = prepareConnection();
        } catch (Exception e){
            return -1;
        }

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
        Connection connection;

        try{
            connection = prepareConnection();
        }   catch (Exception e){
            return false;
        }

        try{
            final String query = "SELECT COUNT(*) AS \"total\" FROM Pharmacists WHERE pharmacist_ID = " + pharmacistID;
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet set = pstmt.executeQuery();

            set.next();
            int count = set.getInt("total");
            connection.close();
            return (count > 0);

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
        Connection connection;

        try{
            connection = prepareConnection();
        }   catch (Exception e){
            return false;
        }

        try{
            final String query = "SELECT COUNT(*) AS \"total\" FROM Pharmacists WHERE fName = ? AND mName = ? AND lName = ? ";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, fName);
            pstmt.setString(2, mName);
            pstmt.setString(3, lName);
            ResultSet set = pstmt.executeQuery();

            set.next();
            int numOfRows = set.getInt("total");
            connection.close();
            return (numOfRows > 0);
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
        Connection connection;
        try {
            connection = prepareConnection();
        } catch (Exception e) {
            return;
        }

        try {
            String query = "SELECT * FROM Pharmacists WHERE pharmacist_ID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, pharmaID);
            ResultSet set = pstmt.executeQuery();

            if (set.next()) {
                System.out.println("Pharma ID: " + pharmaID + "\nPharmacist Name: " + set.getString("fName") + " " + set.getString("mName") +
                        " " + set.getString("lName"));
                connection.close();
            } else {
                System.out.println("Pharmacist with ID: " + pharmaID + ", does not exist.");
                connection.close();
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
        Connection connection;

        try{
            connection = prepareConnection();
        } catch (Exception e) {
            return false;
        }

        try{
            if (!pharmacistExists(pharmaID))
                return false;

            Statement stmt = connection.createStatement();
            final String query = "DELETE FROM Pharmacists WHERE pharmacist_ID = " + pharmaID;
            int count = stmt.executeUpdate(query);
            connection.close();
            return (count > 0);

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

        // Database connection and query execution
        try (Connection conn = DatabaseConnection.connect();
        PreparedStatement stmt = conn.prepareStatement(query)) {
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

        // Database connection and query execution
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement()) {

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
     * @param pharmaID      The primary key or identifier for the pharmacist within the database
     * @param fName         The first name of the pharmacist to be updated
     * @param mName         The middle name of the pharmacist to be updated
     * @param lName         The last name of the pharmacist to be updated
     *
     * @return              true if the pharmacist's details were updated, false if the pharmacist doesn't
     *                      exist or if the update was unable to push through.
     */
    public boolean updatePharmacist(Pharmacist pharmacist, int pharmaID, String fName, String mName, String lName){
        Connection connection;

        try{
            connection = prepareConnection();
        } catch (Exception e) {
            return false;
        }

        try{
            if (!pharmacistExists(pharmacist.getPharmacistID())){
                System.out.println("Pharmacist with the ID " + pharmacist.getPharmacistID() + ", doesn't exist.");
                return false;
            }

            final String firstQuery = "UPDATE Pharmacists SET fName = ? WHERE pharmacist_ID = " + pharmacist.getPharmacistID();
            final String secondQuery = "UPDATE Pharmacists SET mName = ? WHERE pharmacist_ID = " + pharmacist.getPharmacistID();
            final String thirdQuery = "UPDATE Pharmacists SET lName = ? WHERE pharmacist_ID = " + pharmacist.getPharmacistID();
            final String fourthQuery = "UPDATE Pharmacists SET pharmacist_ID = ? WHERE pharmacist_ID = " + pharmacist.getPharmacistID();
            PreparedStatement firstPstmt = connection.prepareStatement(firstQuery);
            PreparedStatement secondPstmt = connection.prepareStatement(secondQuery);
            PreparedStatement thirdPstmt = connection.prepareStatement(thirdQuery);
            PreparedStatement fourthPstmt = connection.prepareStatement(fourthQuery);

            firstPstmt.setString(1, fName);
            secondPstmt.setString(1, mName);
            thirdPstmt.setString(1, lName);
            fourthPstmt.setInt(1, pharmaID);

            int rowsAffected = 0;
            rowsAffected += firstPstmt.executeUpdate();
            rowsAffected += secondPstmt.executeUpdate();
            rowsAffected += thirdPstmt.executeUpdate();
            rowsAffected += fourthPstmt.executeUpdate();

            connection.close();
            return (rowsAffected == 4);

        } catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes all pharmacists from the Pharmacists database
     */
    public boolean removeAllPharmacists(){
        Connection connection;
        try{
            connection = DatabaseConnection.connect();
        } catch (Exception e){
            return false;
        }

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

    //TODO: Confirm the implementation, and is prone to changes due to issues with item unit type and item type
    /**
     * Method used to add an item to the database, together with its item type and unit cost (price)
     *
     * @param itemName      The name of the item to be added to the database
     * @param itemType      The type of item to be added (Limited to the drop-down GUI)
     * @param unitCost      The price for one unit of an item (xxxx.xx)
     * @return              true if the item and item type is added to the database, false if not
     */
    public boolean addItem(String itemName, String itemType, double unitCost){
        //Add the item to the database
        String query = "INSERT INTO Items (item_name, unit_cost) VALUES (?, ?)";

        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){

            //Checks if there is no existing item with this name
            if (itemExists(itemName)){
                System.out.println("ERROR: Unable to add new item as, " + itemName + " " + itemType + " " + unitCost + ", already exists in the database");
                return false;
            }

            pstmt.setString(1, itemName);
            pstmt.setDouble(2, unitCost);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected <= 0)
                return false;

            //Use the item ID to add it to the Item Type table
            String secondQuery = """
                INSERT INTO ItemType (item_ID, item_Type) VALUES 
                    ((SELECT i.item_ID FROM Item AS i WHERE i.item_Name = ?), ?)
                """;
            PreparedStatement secondPstmt = connection.prepareStatement(secondQuery);
            secondPstmt.setString(1, itemName);
            secondPstmt.setString(2, itemType);
            rowsAffected = secondPstmt.executeUpdate();
            connection.close();

            return rowsAffected > 0;

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
        Connection connection;
        try {
            connection = prepareConnection();
        }   catch (Exception e){
            return false;
        }

        try{
            if (connection == null){
                System.out.println("ERROR: Connection is null.");
                return false;
            }

            //Checks if there is no existing item with this name
            if (itemExists(itemName)){
                System.out.println("ERROR: item " + itemName + ", doesn't exist in the database.");
                return false;
            }

            ///Add the item to the database
            String firstQuery = "UPDATE Items SET item_name = ? WHERE item_name = ?";
            String secondQuery = "UPDATE Items SET unit_cost = ? WHERE item_name = ?";
            PreparedStatement firstPstmt = connection.prepareStatement(firstQuery);
            PreparedStatement secondPstmt = connection.prepareStatement(firstQuery);
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
                    i.unit_cost AS "Unit Cost",
                    it.item_type AS "Item Type"
                FROM Items AS i
                JOIN ItemType AS it ON i.item_ID = it.item_ID
                WHERE i.item_ID >= ? AND i.item_ID <= ?
                """;

        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){

            if (itemID1 > itemID2){     //Made to ensure that there will be a set of IDs (ID1, ... ,  ID2)
                int temp = itemID1;       //where ID1 is <= ID2 to form the set pharmacists to be taken
                itemID1 = itemID2;
                itemID2 = temp;
            }

            //The instance where the lesser ID (itemID1) value is null, means that itemID2 is also null.
            if (!itemExists(itemID1)){
                System.out.println("Item ID " + itemID1 + ", doesn't exist");
                return null;
            }

            ArrayList<Item> list = new ArrayList<>();
            boolean isAdded = false;
            pstmt.setInt(1, itemID1);
            pstmt.setInt(2, itemID2);
            ResultSet set = pstmt.executeQuery();

            while(set.next()){
                Item item = new Item(set.getInt("Item ID"), set.getString("Item Name"),
                                     set.getString("Item Type"),  set.getDouble("Unit Cost"));
                list.add(item);
                isAdded = true;
            }

            connection.close();

            if (isAdded){
                Item [] newList = new Item[list.size()];
                return list.toArray(newList);
            } else {
                return null;
            }

        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the detail of an item given its item name
     *
     * @param itemName  The name of the item to be looked up in the Items table
     * @return          An Item object containing the details of the item (ID, Name, Type, and Price)
     */
    public Item getItem(String itemName){
        //Gets the item's details from the Item table
        final String query = """
                SELECT 
                    i.item_ID AS "Item ID", 
                    i.item_name AS "Item Name",
                    i.unit_cost AS "Unit Cost",
                    it.item_type AS "Item Type"
                FROM Items AS i
                JOIN ItemType AS it ON i.item_ID = it.item_ID
                WHERE item_ID >= ? AND item_ID <= ?
                """;

        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){
            if (!itemExists(itemName))
                return null;

            pstmt.setString(1, itemName);
            ResultSet set = pstmt.executeQuery();
            if (set.next()){
                connection.close();
                return null;
            }

            //Gets the item's item_type from the ItemType table
            String otherQuery = "SELECT item_type FROM ItemType WHERE item_id = " + set.getInt("item_ID");
            Statement stmt = connection.createStatement();
            ResultSet otherSet = stmt.executeQuery(otherQuery);

            if (!otherSet.next()){
                connection.close();
                return null;
            }

            //Prepares the item's info into a String array
            Item item = new Item(
                    set.getInt("Item ID"),
                    set.getString("Item Name"),
                    set.getString("Item Type"),
                    set.getDouble("Unit Cost"));

            connection.close();
            return item;

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
        Connection connection;
        try{
            connection = DatabaseConnection.connect();
        } catch (Exception e){
            return -1;
        }

        try {
            final String selectQuery = "SELECT i.item_ID FROM Items AS i WHERE i.item_name = ?";
            assert connection != null;
            PreparedStatement pstmt = connection.prepareStatement(selectQuery);
            pstmt.setString(1, itemName);

            ResultSet set = pstmt.executeQuery();

            if (set.next()){
                int id = set.getInt("item_id");
                connection.close();
                return id;
            }   else
                return -1;

        }   catch (SQLException e){
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Removes one item from the database given its name
     */
    //TODO: Alter the contents by adding the getItemID method.
    public boolean removeItem(String itemName){
        try (Connection connection = prepareConnection();){
            int itemID = getItemId(itemName);

            if (itemID != -1){
                //TODO: Add condition to remove all items in Items_Sold table
                //TODO: Add condition to remove all items in Restock table

                //TODO: Change the method, removing the info from the ItemUnitType Table

            }   else{
                connection.close();
                return false;
            }

            final String query = "DELETE FROM Items WHERE item_ID = " + itemID;
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
        Connection connection;
        try{
            connection = DatabaseConnection.connect();
        } catch (Exception e){
            return false;
        }

        try {
            //TODO: Add condition to remove all items in Items_Sold table
            //TODO: Add condition to remove all items in Restock table

            final String removeInItem = "DELETE FROM ItemType WHERE item_id > 0 ";
            final String removeInUnit = "DELETE FROM UnitType WHERE item_id > 0";

            assert connection != null;
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(removeInItem);
            stmt.executeUpdate(removeInUnit);

            final String query = "DELETE FROM Items WHERE item_ID > 0";
            PreparedStatement pstmt = connection.prepareStatement(query);
            int rowsAffected = pstmt.executeUpdate(query);
            connection.close();

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
        Connection connection;

        try{
            connection = prepareConnection();
        }   catch (Exception e){
            return false;
        }

        try{
            final String query = "SELECT COUNT(*) AS \"total\" FROM Items WHERE item_Name = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, itemName);
            ResultSet set = pstmt.executeQuery();

            set.next();
            int numOfRows = set.getInt("total");
            connection.close();
            return (numOfRows > 0);
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
        Connection connection;

        try{
            connection = prepareConnection();
        }   catch (Exception e){
            return false;
        }

        try{
            final String query = "SELECT COUNT(*) AS \"total\" FROM Items WHERE item_ID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, itemID);
            ResultSet set = pstmt.executeQuery();

            set.next();
            int numOfRows = set.getInt("total");
            connection.close();
            return (numOfRows > 0);
        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

//======================================================================================================================================================================
//Methods for the Item Type.

    //TODO: Add comments for method
    public boolean addItemType(String itemTypeName){
        final String query = "INSERT INTO ItemType (item_Type) VALUES (?)";

        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){
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

        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){
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

        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){
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

        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){
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

    //TODO: Add comments for the method
    public boolean itemTypeExists(String itemTypeName){
        final String query = "SELECT * FROM ItemType WHERE item_Type = ?";

        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){
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

        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){
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

        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){
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
        int itemTypeID = getItemTypeID(itemTypeName);

        if (itemTypeID == -1)
            return false;

        final String query = "DELETE FROM ItemType WHERE itemType_ID = ?";

        if (!removeItemUnitType(itemTypeID, REMOVE_ITEM_TYPE))
            return false;

        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){
            if (!itemTypeExists(itemTypeName))
                return false;

            pstmt.setInt(1, itemTypeID);
            return pstmt.executeUpdate() > 0;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

//======================================================================================================================================================================
//Methods for the Unit Type.

    //TODO: Add comments for this method
    public boolean addUnitType(String unitTypeName){
        final String query = "INSERT INTO UnitType (unit_Type) VALUES (?)";
        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){

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

        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(unitTypeName);){
            pstmt.setString(1, unitTypeName);
            ResultSet set = pstmt.executeQuery(query);
            if (set.next())
                return true;
            else
                return false;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean unitTypeExists(int unitTypeID){
        final String query = "SELECT * FROM UnitType WHERE unitType_ID = ?";

        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setInt(1, unitTypeID);
            ResultSet set = pstmt.executeQuery(query);
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

        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){
            if (!unitTypeExists(unitTypeID))
                return null;

            pstmt.setInt(1, unitTypeID);
            ResultSet set = pstmt.executeQuery();
            if (set.next())
                return new UnitType(set.getInt("Item ID"), set.getString("Unit Name"));
        }   catch (Exception e){
            e.printStackTrace();
            return null;
        }

        return null;
    }

    //TODO: Add comments for this method
    public int getUnitTypeID(String unitTypeName){
        final String query = "SELECT unitType_ID AS \"Unit Type ID\" FROM UnitType WHERE unit_Type = ?";

        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setString(1, unitTypeName);
            ResultSet set = pstmt.executeQuery(query);
            if (set.next())
                return set.getInt("Unit Type ID");
            else
                return -1;

        }   catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public boolean updateUnitType(int unitTypeID, String newUnitTypeName){
        final String query = "UPDATE UnitType SET unit_Type = ? WHERE unitType_ID = ?";

        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setString(1, newUnitTypeName);
            pstmt.setInt(2, unitTypeID);

            return pstmt.executeUpdate() > 0;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //TODO: Add comments for this method
    public boolean removeUnitType(int unitTypeID){
        final String query = "DELETE FROM UnitType WHERE item_id = ?";

        if (!removeItemUnitType(unitTypeID, REMOVE_UNIT_TYPE))
            return false;

        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){
            if (!unitTypeExists(unitTypeID))
                return false;

            return pstmt.executeUpdate(query) > 0;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

//======================================================================================================================================================================
//Methods for the ItemUnitType.

    //TODO: Add comments
    public boolean addItemUnitType(int itemTypeID, int unitTypeID){
        final String query = "INSERT INTO ItemUnitType (itemType_ID, unitType_ID) VALUES (?, ?)";
        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){

            if (!unitTypeExists(unitTypeID) || !itemTypeExists(itemTypeID)){
                System.out.println("ERROR: Unable to add new Item-Unit Type. \n Item-Type or Unit-Type doesn't exist.");
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

        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setInt(1, itemUnitTypeID);
            ResultSet set = pstmt.executeQuery(query);
            return (set.next());

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //TODO: Add comments
    public boolean itemUnitTypeExists(int itemTypeID, int unitTypeID){
        final String query = "SELECT * FROM ItemUnitType WHERE unitType_ID = ? AND itemType_ID = ?";

        try(Connection connection = prepareConnection();
            PreparedStatement pstmt = connection.prepareStatement(query);){
            pstmt.setInt(1, unitTypeID);
            pstmt.setInt(2, itemTypeID);
            ResultSet set = pstmt.executeQuery(query);
            return (set.next());

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //TODO: Finish Implementation
    public ItemUnitType getItemUnitType(){

    }

    //TODO: Finish Implementation
    public int getItemUnitTypeID(int itemTypeID, int unitTypeID){

    }

    //TODO: Add explanation for why these variables are made/used
    public static final int REMOVE_ITEM_TYPE = 6969;
    public static final int REMOVE_UNIT_TYPE = 9696;

    //TODO: Add comments
    public boolean removeItemUnitType(int id, int condition){
        final String first = "DELETE FROM ItemUnitType WHERE itemType_ID = " + id;
        final String second = "DELETE FROM ItemUnitType WHERE unitType_ID = " + id;

        try(Connection connection = prepareConnection();
            Statement stmt = connection.createStatement();){

            if (condition == REMOVE_ITEM_TYPE)
                stmt.executeUpdate(first);
            else if (condition == REMOVE_UNIT_TYPE)
                stmt.executeUpdate(second);
            else
                return false;

        }   catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

//======================================================================================================================================================================
//Methods for the Transactions.

    //TODO: Add methods for transactions (CR & RS)

//======================================================================================================================================================================
//Methods for the Sold Items.
    //TODO: Add methods for Sold Items (CR & RS)

    //TODO: Add methods for Restocks (CR)

    //TODO: Make views for the following...
}
