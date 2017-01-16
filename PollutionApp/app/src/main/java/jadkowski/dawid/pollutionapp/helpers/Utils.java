package jadkowski.dawid.pollutionapp.helpers;

/**
 * Created by dawid on 08/01/2017.
 */

public class Utils {
    public static int getYear(String yr) {
        int yearInt = 0;
        switch (yr) {
            case "2016":
                yearInt = 1;
                break;
            case "2017":
                yearInt = 2;
                break;
            case "2018":
                yearInt = 3;
                break;
            case "2019":
                yearInt = 4;
                break;
            case "2020":
                yearInt = 5;
                break;

        }
        return yearInt;
    }

    public static int getMonth(String mName) {

        int monthInt = 0;
        switch (mName) {
            case "Jan":
                monthInt = 1;
                break;
            case "Feb":
                monthInt = 2;
                break;
            case "Mar":
                monthInt = 3;
                break;
            case "Apr":
                monthInt = 4;
                break;
            case "May":
                monthInt = 5;
                break;
            case "Jun":
                monthInt = 6;
                break;
            case "Jul":
                monthInt = 7;
                break;
            case "Aug":
                monthInt = 8;
                break;
            case "Sep":
                monthInt = 9;
                break;
            case "Oct":
                monthInt = 10;
                break;
            case "Nov":
                monthInt = 11;
                break;
            case "Dec":
                monthInt = 12;
                break;

        }
        return monthInt;
    }

    public static int getDay(String d) {
        int dInt = 0;
        switch (d) {
            case "01":
                dInt = 1;
                break;
            case "02":
                dInt = 2;
                break;
            case "03":
                dInt = 3;
                break;
            case "04":
                dInt = 4;
                break;
            case "05":
                dInt = 5;
                break;
            case "06":
                dInt = 6;
                break;
            case "07":
                dInt = 7;
                break;
            case "08":
                dInt = 8;
                break;
            case "09":
                dInt = 9;
                break;
            case "10":
                dInt = 10;
                break;
            case "11":
                dInt = 11;
                break;
            case "12":
                dInt = 12;
                break;
            case "13":
                dInt = 13;
                break;
            case "14":
                dInt = 14;
                break;
            case "15":
                dInt = 15;
                break;
            case "16":
                dInt = 16;
                break;
            case "17":
                dInt = 17;
                break;
            case "18":
                dInt = 18;
                break;
            case "19":
                dInt = 19;
                break;
            case "20":
                dInt = 20;
                break;
            case "21":
                dInt = 21;
                break;
            case "22":
                dInt = 22;
                break;
            case "23":
                dInt = 23;
                break;
            case "24":
                dInt = 24;
                break;
            case "25":
                dInt = 25;
                break;
            case "26":
                dInt = 26;
                break;
            case "27":
                dInt = 27;
                break;
            case "28":
                dInt = 28;
                break;
            case "29":
                dInt = 29;
                break;
            case "30":
                dInt = 30;
                break;
            case "31":
                dInt = 31;
                break;
        }
        return dInt;
    }
}
