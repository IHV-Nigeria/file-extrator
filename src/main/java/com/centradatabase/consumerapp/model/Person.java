package com.centradatabase.consumerapp.model;

import org.springframework.data.annotation.Id;

import java.io.Serializable;

public class Person implements Serializable {
    @Id
    String id;
    String name;

    public Person(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Person(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
