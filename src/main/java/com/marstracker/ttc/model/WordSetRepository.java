package com.marstracker.ttc.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface WordSetRepository extends JpaRepository<WordSet, Long> {
    WordSet findByName (String name);
}
