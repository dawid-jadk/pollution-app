package jadkowski.dawid.pollutionapp.helpers;

/**
 * Created by dawid on 07/01/2017.
 */

public class Result {
    private float gas1_value;
    private float gas2_value;
    private float gas3_value;
    private String time_value;
    private double location_lat_value;
    private double location_lng_value;

    public float getGas1_value() {
        return gas1_value;
    }

    public void setGas1_value(float gas1_value) {
        this.gas1_value = gas1_value;
    }

    public float getGas2_value() {
        return gas2_value;
    }

    public void setGas2_value(float gas2_value) {
        this.gas2_value = gas2_value;
    }

    public float getGas3_value() {
        return gas3_value;
    }

    public void setGas3_value(float gas3_value) {
        this.gas3_value = gas3_value;
    }

    public String getTime_value() {
        return time_value;
    }

    public void setTime_value(String time_value) {
        this.time_value = time_value;
    }

    public Double getLocation_lat_value() {
        return location_lat_value;
    }

    public void setLocation_lat_value(double location_lat_value) {
        this.location_lat_value = location_lat_value;
    }

    public Double getLocation_lng_value() {
        return location_lng_value;
    }

    public void setLocation_lng_value(double location_lng_value) {
        this.location_lng_value = location_lng_value;
    }
}
