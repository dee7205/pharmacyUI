package DBHandler;
import java.sql.Date;
import java.time.LocalDate;

public class Statistics {
    double beginningBalance, soldBalance, endingBalance;
    LocalDate date;

    public static void main (String [] args){
        LocalDate date = LocalDate.of(2023, 12, 5);

        System.out.println(date.getDayOfMonth());
    }

    public Statistics(){ this(0, 0, null); }
    public Statistics(double beginningBalance, double soldBalance, LocalDate date){
        setBeginningBalance(beginningBalance);
        setSoldBalance(soldBalance);
        setEndingBalance(getBeginningBalance() + getSoldBalance());
        setDate(date);
    }

    public double getBeginningBalance() {
        return beginningBalance;
    }

    public void setBeginningBalance(double beginningBalance) {
        this.beginningBalance = beginningBalance;
    }

    public double getSoldBalance() {
        return soldBalance;
    }

    public void setSoldBalance(double soldBalance) {
        this.soldBalance = soldBalance;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getEndingBalance() {
        return endingBalance;
    }

    public void setEndingBalance(double endingBalance) {
        this.endingBalance = endingBalance;
    }

    public String [] getInfo(){
        Date tempDate = Date.valueOf(getDate());
            return new String [] {getDate().getMonth().toString(), Integer.toString(getDate().getDayOfMonth()),
                                  Integer.toString(getDate().getYear()), Double.toString(getBeginningBalance()),
                                  Double.toString(getSoldBalance()), Double.toString(getEndingBalance())};
    }

    public static String [][] getInfoTable(Statistics [] stats){
        if (stats == null || stats.length == 0)
            return null;

        String [][] infoList = new String[stats.length][6];
        for (int i = 0; i < stats.length; i++)
            infoList[i] = stats[i].getInfo();
        return infoList;
    }
}
