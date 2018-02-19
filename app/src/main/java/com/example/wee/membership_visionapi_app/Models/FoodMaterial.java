package com.example.wee.membership_visionapi_app.Models;

import java.io.Serializable;

/**
 * Created by youngzz on 2018. 2. 18..
 */

public class FoodMaterial implements Serializable {

    private String materialName;
    private String materialStructure;
    private boolean isMyAllergy;

    public FoodMaterial() {
    }

    public FoodMaterial(String materialName, String materialStructure) {
        this.materialName = materialName;
        this.materialStructure = materialStructure;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getMaterialStructure() {
        return materialStructure;
    }

    public void setMaterialStructure(String materialStructure) {
        this.materialStructure = materialStructure;
    }

    public boolean isMyAllergy() {
        return isMyAllergy;
    }

    public void setMyAllergy(boolean myAllergy) {
        isMyAllergy = myAllergy;
    }
}
