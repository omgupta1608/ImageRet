package com.example.imageret;

public class ImageDetails {

    String ImageURL;
    String PostTitle;
    String Description;

    public ImageDetails(String imageURL, String postTitle, String description) {
        ImageURL = imageURL;
        PostTitle = postTitle;
        Description = description;
    }

    public ImageDetails() {
    }

    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }

    public String getPostTitle() {
        return PostTitle;
    }

    public void setPostTitle(String postTitle) {
        PostTitle = postTitle;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }
}
