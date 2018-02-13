package com.example.wee.membership_visionapi_app;

/**
 * Created by andong-won on 2018. 2. 13..
 */

public class Allergy {
    private String name;
    public String firebaseKey;

    public Allergy(String name){ this.name = name;}
    public Allergy(){ }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
