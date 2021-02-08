package tourGuide.model;

import java.util.List;

/**
 * DTO regroupant les données à affichées lorsqu'on souhaite afficher les attractions proches d'une personne
 */
public class NearbyAttractionDto {

    private LocationTourGuide userLocationTourGuide;

    private List<AttractionClosestDto> closestAttractionsList;

    public LocationTourGuide getUserLocation() {
        return userLocationTourGuide;
    }

    public void setUserLocation(LocationTourGuide userLocationTourGuide) {
        this.userLocationTourGuide = userLocationTourGuide;
    }

    public List<AttractionClosestDto> getClosestAttractionsList() {
        return closestAttractionsList;
    }

    public void setClosestAttractionsList(List<AttractionClosestDto> closestAttractionsList) {
        this.closestAttractionsList = closestAttractionsList;
    }
}
