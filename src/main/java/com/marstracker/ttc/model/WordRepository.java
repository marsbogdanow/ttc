package com.marstracker.ttc.model;


import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
@PreAuthorize("hasAuthority('WORDSET_OWNER')")
public interface WordRepository extends PagingAndSortingRepository<Word, Long> {
    @Override
    @PreAuthorize("#word.id == null or #word?.appUser?.email == authentication?.name")
    Word save(@Param("word") Word word);

    @Override
    List<Word> findAll();
}
