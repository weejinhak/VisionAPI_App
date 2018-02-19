package com.example.wee.membership_visionapi_app.Models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Food implements Serializable {
    private String foodId;//식품 아이디
    private String foodName;//식품명
    private String thumbnailUrl;//썸네일 URL
    private String barcode;//바코드

    private String tags;//등록된 태그(구분자 ,)
    private String foodClassifyId;//식품 분류 아이디
    private String foodClassifyName;//식품 분류 이름
    private List<FoodMaterial> foodMaterials = new ArrayList<>();//원재료 리스트
    private List<AllergyIngredient> allergyIngredients =new ArrayList<>();//알레르기성분

    private int count;//해당하는 알레르기 성분 갯수

    public Food() {
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getFoodClassifyId() {
        return foodClassifyId;
    }

    public void setFoodClassifyId(String foodClassifyId) {
        this.foodClassifyId = foodClassifyId;
    }

    public String getFoodClassifyName() {
        return foodClassifyName;
    }

    public void setFoodClassifyName(String foodClassifyName) {
        this.foodClassifyName = foodClassifyName;
    }

    public List<FoodMaterial> getFoodMaterials() {
        return foodMaterials;
    }

    public void setFoodMaterials(List<FoodMaterial> foodMaterials) {
        this.foodMaterials = foodMaterials;
    }

    public List<AllergyIngredient> getAllergyIngredients() {
        return allergyIngredients;
    }

    public void setAllergyIngredients(List<AllergyIngredient> allergyIngredients) {
        this.allergyIngredients = allergyIngredients;
    }
}
