package com.example.wee.membership_visionapi_app.Models;

import java.io.Serializable;

/**
 * Created by youngzz on 2018. 2. 18..
 */

public class AllergyIngredient implements Serializable {
    private String materialId;
    private String materialName;
    private boolean isMyAllergy=false;

    public AllergyIngredient() {
    }

    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialId(String materialId) {
        this.materialId = materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public boolean isMyAllergy() {
        return isMyAllergy;
    }

    public void setMyAllergy(boolean myAllergy) {
        isMyAllergy = myAllergy;
    }
}
