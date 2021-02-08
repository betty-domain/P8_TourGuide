package tourGuide.model;

import lombok.Data;

import java.util.Date;
import java.util.UUID;
@Data
public class VisitedLocation {

    private UUID userId;
    private Location location;
    private Date timeVisited;

    public VisitedLocation()
    {

    }

    public VisitedLocation(UUID userId, Location location, Date timeVisited) {
        this.setUserId(userId);
        this.setLocation(location);
        this.setTimeVisited(timeVisited);
    }


}
