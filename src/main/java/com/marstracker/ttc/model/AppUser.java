package com.marstracker.ttc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static com.marstracker.ttc.service.SpringDataJpaUserDetailsService.PASSWORD_ENCODER;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
@EqualsAndHashCode
public class AppUser {
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String[] roles;

    public void setPassword(String password) {
        this.password = PASSWORD_ENCODER.encode(password);
    }
}
