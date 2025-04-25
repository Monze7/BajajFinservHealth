// filepath: src/main/java/com/example/Bajajhealth/dto/User.java

package com.example.Bajajhealth.dto;

import java.util.List;

public class User {
    private int id;
    private String name;
    private List<Integer> follows;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Integer> getFollows() { return follows; }
    public void setFollows(List<Integer> follows) { this.follows = follows; }
}
