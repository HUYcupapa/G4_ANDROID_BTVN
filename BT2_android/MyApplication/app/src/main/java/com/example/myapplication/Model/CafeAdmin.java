package com.example.myapplication.Model;

import com.google.firebase.firestore.GeoPoint;

public class CafeAdmin {
    private String id;
    private String name;
    private String locationText;
    private double lat;
    private double lng;
    private GeoPoint location;
    private String activity;
    private String description;
    private String image1;
    private String image2;
    private String image3;
    private String video;
    private String createdBy;

    // Constructor mặc định (yêu cầu bởi Firestore)
    public CafeAdmin() {
    }

    // Constructor đầy đủ
    public CafeAdmin(String id, String name, String locationText, double lat, double lng, GeoPoint location,
                     String activity, String description, String image1, String image2, String image3,
                     String video, String createdBy) {
        this.id = id;
        this.name = name;
        this.locationText = locationText;
        this.lat = lat;
        this.lng = lng;
        this.location = location;
        this.activity = activity;
        this.description = description;
        this.image1 = image1;
        this.image2 = image2;
        this.image3 = image3;
        this.video = video;
        this.createdBy = createdBy;
    }

    // Getters và Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getImage2() {
        return image2;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }

    public String getImage3() {
        return image3;
    }

    public void setImage3(String image3) {
        this.image3 = image3;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}