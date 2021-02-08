package tourGuide.model;

import lombok.Data;

/**
 * Location POJO
 */
@Data
public class Location {

    private double longitude;
    private double latitude;

    public Location()
    {

    }
    public Location(double latitude, double longitude) {
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }
}
