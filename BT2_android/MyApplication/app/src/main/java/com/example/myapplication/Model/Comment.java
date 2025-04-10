package com.example.myapplication.Model;

public class Comment {
    private String userId;
    private String comment;
    private float rating;
    private String timestamp;

    public Comment() {}

    public Comment(String userId, String comment, float rating, String timestamp) {
        this.userId = userId;
        this.comment = comment;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}