package com.example.tasktracker.entities.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    private String role;

    protected UserEntity() {
    }

    public UserEntity(String name,
                      String username,
                      String password,
                      String role
    ) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
