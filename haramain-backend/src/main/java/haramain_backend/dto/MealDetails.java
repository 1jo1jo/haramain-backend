package haramain_backend.dto;

public interface MealDetails {

    int getMealID();

    int getOrganisationID();

    String getMealName();

    String getDescription();

    double getPrice();

    int getQuantity();

    String getImage();

    String getOrganisationName();

    String getCity();
}