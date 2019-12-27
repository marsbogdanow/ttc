package com.marstracker.ttc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name="wordSets")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WordSet {
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private String name;
    @ManyToOne(optional = false)
    private AppUser appUser;
}
