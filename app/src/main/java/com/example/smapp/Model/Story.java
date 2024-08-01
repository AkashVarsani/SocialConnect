package com.example.smapp.Model;

import java.util.ArrayList;

public class Story {

    private String storyBy;

    public Story() {
    }

    private long storyAt;
    private ArrayList<UserStories> userStoriesList = new ArrayList<>();

    public String getStoryBy() {
        return storyBy;
    }

    public ArrayList<UserStories> getUserStoriesList() {
        return userStoriesList;
    }

    public void setUserStoriesList(ArrayList<UserStories> userStoriesList) {
        this.userStoriesList = userStoriesList;
    }

    public Story(String storyBy, long storyAt) {
        this.storyBy = storyBy;
        this.storyAt = storyAt;
    }

    public void setStoryBy(String storyBy) {
        this.storyBy = storyBy;
    }

    public long getStoryAt() {
        return storyAt;
    }

    public void setStoryAt(long storyAt) {
        this.storyAt = storyAt;
    }
}
