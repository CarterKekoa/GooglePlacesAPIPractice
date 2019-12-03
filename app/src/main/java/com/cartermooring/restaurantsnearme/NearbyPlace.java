package com.cartermooring.restaurantsnearme;

import androidx.annotation.NonNull;

public class NearbyPlace {
    String name;
    String rating;
    String formatted_address;

    public NearbyPlace(String name, String rating, String address) {
        this.name = name;
        this.rating = rating;
        this.formatted_address = address;
    }

    @Override
    public String toString() {
        return "NearbyPlace{" +
                "name='" + name + '\'' +
                ", rating='" + rating + '\'' +
                ", address='" + formatted_address + '\'' +
                '}';
    }

    public String getName(){ return name;}
    public void setName(String name){this.name = name;}

    public String getRating(){ return rating;}
    public void setRating(String rating){this.rating = rating;}

    public String getAddress(){return formatted_address;}
    public void setAddress(String address){this.formatted_address = address;}
}
