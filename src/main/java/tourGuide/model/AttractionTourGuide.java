package tourGuide.model;

import lombok.Data;

import java.util.UUID;

/**
 * Attraction POJO
 */
@Data
public class Attraction extends Location {
    private String attractionName;
    private String city;
    private String state;
    private UUID attractionId;

    public Attraction()
    {

    }

    public Attraction(String attractionName, String city, String state, double latitude, double longitude) {
        super(latitude, longitude);
        this.attractionName = attractionName;
        this.city = city;
        this.state = state;
        this.attractionId = UUID.randomUUID();
    }
}
