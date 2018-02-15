package com.example.wee.membership_visionapi_app.Models;

/**
 * Created by andong-won on 2018. 2. 15..
 */

public class Component {

    private String name;
    private Boolean isAllergy;

    public Component(String name, Boolean isAllergy){
        this.name = name;
        this.isAllergy = isAllergy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isAllergy() {
        return isAllergy;
    }
}
