package tourGuide.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;
@Getter
@Setter
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
