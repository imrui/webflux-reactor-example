package com.xxicon.code.reactor.http.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class User {
    @Id
    private long id;
    private String name;
    private int age;

    public void update(User u) {
        this.name = u.name;
        this.age = u.age;
    }
}
