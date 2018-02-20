package com.snackpick.wee.membership_visionapi_app.Models;

public class Allergy {
    private String name;
    private String firebaseKey;

    public Allergy(String name){ this.name = name;}
    public Allergy(){ }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }
}
