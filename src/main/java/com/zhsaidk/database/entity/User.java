package com.zhsaidk.database.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Length(min = 2, max = 255)
    private String name;

    @Column(nullable = false, unique = true)
    @Length(min = 2, max = 124)
    private String username;

    @NotNull
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
//
//    @ToString.Exclude
//    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
//    List<Project> projects = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<ApiKey> keys = new ArrayList<>();
}
