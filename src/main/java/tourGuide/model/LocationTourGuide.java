package tourGuide.model;

import lombok.Data;

/**
 * Location POJO
 */
@Data
public class LocationTourGuide {

    private double longitude;
    private double latitude;

    public LocationTourGuide()
    {

    }
    public LocationTourGuide(double latitude, double longitude) {
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }
}
