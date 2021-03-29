package com.listerapp.lister.grocerystore.model;

import java.util.ArrayList;
import java.util.List;

public class Orders {
    String date;
    String userName;
    List<Cart> valueList = new ArrayList<>();

    public Orders() {
    }

    public Orders(String date, String userName, List<Cart> valueList) {
        this.date = date;
        this.userName = userName;
        this.valueList = valueList;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<Cart> getValueList() {
        return valueList;
    }

    public void setValueList(List<Cart> valueList) {
        this.valueList = valueList;
    }
}
