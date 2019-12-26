package com.marstracker.ttc.dev;

import com.marstracker.ttc.model.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

@Component
public class Initializer implements CommandLineRunner {
    private final AppUserRepository appUserRepository;
    private final WordRepository wordRepository;
    private final WordSetRepository wordSetRepository;

    public Initializer(AppUserRepository appUserRepository, WordRepository wordRepository, WordSetRepository wordSetRepository) {
        this.appUserRepository = appUserRepository;
        this.wordRepository = wordRepository;
        this.wordSetRepository = wordSetRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Stream.of("Admin", "Test").forEach(email -> {
            AppUser appUser = new AppUser();
            appUser.setEmail(email);
            appUser.setPassword("1234");
            appUser.setName(email);
            appUser.setRoles(new String[]{"WORDSET_OWNER"});
            appUserRepository.save(appUser);
            });

        AppUser appUserIt = new AppUser();
        appUserIt.setEmail("testit@gmail.com");
        appUserIt.setName("МаксимIt");
        appUserIt.setPassword("serene");
        appUserIt.setRoles(new String[]{"WORDSET_OWNER"});
        appUserRepository.save(appUserIt);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testit@gmail.com", "doesn't matter",
                        AuthorityUtils.createAuthorityList("WORDSET_OWNER")));

        AppUser appUserEng = new AppUser();
        appUserEng.setEmail("testen@gmail.com");
        appUserEng.setName("МаксимEng");
        appUserEng.setPassword("serene");
        appUserEng.setRoles(new String[]{"WORDSET_OWNER"});
        appUserRepository.save(appUserEng);

        WordSet wordSet = new WordSet();
        wordSet.setName("Ru-It");
        wordSet.setAppUser(appUserRepository.findByEmail("testit@gmail.com"));
        wordSetRepository.save(wordSet);

        WordSet wordSetEn = new WordSet();
        wordSetEn.setName("Ru-En");
        wordSetEn.setAppUser(appUserRepository.findByEmail("testen@gmail.com"));
        wordSetRepository.save(wordSetEn);

        Word word1 = new Word();
        word1.setMeaning("идти");
        word1.setWordToLearn("andare");
        //word1.setWordSet(wordSet);
        word1.setNote("Неправильный глагол");
        word1.setAppUser(appUserIt);
        wordRepository.save(word1);
        Word word2 = new Word();
        word2.setMeaning("Искать");
        word2.setWordToLearn("cercare");
        word2.setAppUser(appUserIt);
        //word2.setWordSet(wordSet);
        wordRepository.save(word2);
        Word word3 = new Word();
        word3.setMeaning("Находить");
        word3.setWordToLearn("trovare");
        word3.setAppUser(appUserIt);
        //word3.setWordSet(wordSet);
        wordRepository.save(word3);

        // Eng
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testen@gmail.com", "doesn't matter",
                        AuthorityUtils.createAuthorityList("WORDSET_OWNER")));

        Word word10 = new Word();
        word10.setMeaning("Прыгать");
        word10.setWordToLearn("jump");
        word10.setAppUser(appUserEng);
        //word3.setWordSet(wordSet);
        wordRepository.save(word10);

        SecurityContextHolder.clearContext();
    }
}
