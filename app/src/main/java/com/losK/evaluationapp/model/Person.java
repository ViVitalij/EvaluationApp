package com.losK.evaluationapp.model;

import java.io.Serializable;

public class Person implements Serializable {

    public static final String TABLE = "people";

    public static final String ID = "_id";

    public static final String NAME = "name";

    public static final String RATING = "rating";

    public static final String PHONE = "phone";

    private String name;

    private Double rating;

    private String phone;

    public Person(String name, Double rating, String phone) {
        this.name = name;
        this.rating = rating;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public Double getRating() {
        return rating;
    }

    public String getPhone() {
        return phone;
    }
}