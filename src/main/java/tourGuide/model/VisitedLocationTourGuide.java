package tourGuide.model;

import lombok.Data;

import java.util.Date;
import java.util.UUID;
@Data
public class VisitedLocationTourGuide {

    private UUID userId;
    private LocationTourGuide locationTourGuide;
    private Date timeVisited;

    public VisitedLocationTourGuide()
    {

    }

    public VisitedLocationTourGuide(UUID userId, LocationTourGuide locationTourGuide, Date timeVisited) {
        this.setUserId(userId);
        this.setLocationTourGuide(locationTourGuide);
        this.setTimeVisited(timeVisited);
    }


}
