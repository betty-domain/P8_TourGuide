package tourGuide.model;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;

import java.util.Locale;

/**
 * DTO regroupant les données d'une attraction et sa proximité relative avec un utilisateur et les points qu'il peut gagner en y allant
 */
public class AttractionClosestDto
{
    //  Return a new JSON object that contains:
    // Name of Tourist attraction,
    // Tourist attractions lat/long,
    // The user's location lat/long,
    // The distance in miles between the user's location and each of the attractions.
    // The reward points for visiting each Attraction.
    //    Note: Attraction reward points can be gathered from RewardsCentral
    private String attractionName;
    private Location attractionLocation;
    private double distanceUserToAttraction;
    private int rewardPoints;

    public double getDistanceUserToAttraction() {
        return distanceUserToAttraction;
    }

    public AttractionClosestDto(String attractionName, Location attractionLocation, double distanceUserToAttraction, int rewardPoints)
    {
        this.attractionName = attractionName;
        this.attractionLocation = attractionLocation;
        this.distanceUserToAttraction = distanceUserToAttraction;
        this.rewardPoints =  rewardPoints;
    }
}
