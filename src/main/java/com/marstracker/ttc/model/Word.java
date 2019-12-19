package com.marstracker.ttc.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name="words")
@NoArgsConstructor
@EqualsAndHashCode
public class Word {
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    @Version
    private Long version;
    @Column(nullable = false)
    private String wordToLearn;
    @Column(nullable = false)
    private String meaning;
    @Column
    private String note;
    @ManyToOne(optional = false)
    private AppUser appUser;
}
