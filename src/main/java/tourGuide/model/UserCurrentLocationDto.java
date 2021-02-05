package tourGuide.model;

import gpsUtil.location.Location;

import java.util.List;
import java.util.UUID;

/**
 * DTO regroupant les données des localisations récentes d'un utilisateur
 */
public class UserCurrentLocationDto {
    private String userId;

    private Location userLocation;

    /**
     * Construction with properties
     * @param userId user id
     * @param location location of user
     */
    public UserCurrentLocationDto(String userId, Location location) {
        this.userId = userId;
        this.userLocation = location;
    }
}
