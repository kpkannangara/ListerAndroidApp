package com.listerapp.lister.grocerystore.model;

public class Category {
    String id;
    String categry;
    String cateimg;
    String token;

    public Category(String id, String categry, String cateimg) {
        this.id = id;
        this.categry = categry;
        this.cateimg = cateimg;
    }

    public Category(String categry, String token) {
        this.categry = categry;
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategry() {
        return categry;
    }

    public void setCategry(String categry) {
        this.categry = categry;
    }

    public String getCateimg() {
        return cateimg;
    }

    public void setCateimg(String cateimg) {
        this.cateimg = cateimg;
    }
}
