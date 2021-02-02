package tourGuide.model;

import gpsUtil.location.Location;

import java.util.List;

/**
 * DTO regroupant les données à affichées lorsqu'on souhaite afficher les attractions proches d'une personne
 */
public class NearbyAttractionDto {

    private Location userLocation;

    private List<AttractionClosestDto> closestAttractionsList;

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public List<AttractionClosestDto> getClosestAttractionsList() {
        return closestAttractionsList;
    }

    public void setClosestAttractionsList(List<AttractionClosestDto> closestAttractionsList) {
        this.closestAttractionsList = closestAttractionsList;
    }
}
