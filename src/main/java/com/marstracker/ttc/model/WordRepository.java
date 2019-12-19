package com.marstracker.ttc.model;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;

@PreAuthorize("hasRole('WORDSET_OWNER')")
public interface WordRepository extends JpaRepository<Word, Long> {
    @Override
    @PreAuthorize("#word?.appUser == null or #word?.appUser?.email == authentication?.name")
    Word save(@Param("word") Word word);
}
