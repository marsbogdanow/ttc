package com.marstracker.ttc.service;

import com.marstracker.ttc.model.AppUser;
import com.marstracker.ttc.model.AppUserRepository;
import com.marstracker.ttc.model.Word;
import com.marstracker.ttc.model.WordSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler(Word.class)
public class SpringDataRestEventHandler {
    private final AppUserRepository userRepository;
    private final WordSetRepository wordSetRepository;

    @Autowired
    public SpringDataRestEventHandler(AppUserRepository userRepository, WordSetRepository wordSetRepository) {
        this.userRepository = userRepository;
        this.wordSetRepository = wordSetRepository;
    }

    @HandleBeforeCreate
    @HandleBeforeSave
    public void applyUserInformationUsingSecurityContext(Word word) {
        System.out.println("word?.appUser -- " + word.getAppUser());
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        if (word.getAppUser() == null) {
            AppUser appUser = userRepository.findByEmail(name);
            if (appUser == null) {
                System.out.println("Here");
                AppUser newAppUser = new AppUser();
                newAppUser.setEmail(name);
                newAppUser.setRoles(new String[]{"WORDSET_OWNER"});
                newAppUser.setName(name);
                appUser = userRepository.save(newAppUser);
                word.setAppUser(appUser);
            }
        } else {
            if (word.getAppUser().getId() == null) {
                AppUser appUser = userRepository.findByEmail(word.getAppUser().getEmail());
                word.setAppUser(appUser);
            }
        }

        if (word.getWordSet().getId() == null) {
            word.setWordSet(wordSetRepository.findByName(word.getWordSet().getName()));
        }
    }}
