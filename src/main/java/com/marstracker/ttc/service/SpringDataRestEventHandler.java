package com.marstracker.ttc.service;

import com.marstracker.ttc.model.AppUser;
import com.marstracker.ttc.model.AppUserRepository;
import com.marstracker.ttc.model.Word;
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

    @Autowired
    public SpringDataRestEventHandler(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @HandleBeforeCreate
    @HandleBeforeSave
    public void applyUserInformationUsingSecurityContext(Word word) {

        System.out.println("word?.appUser -- " + word.getAppUser());

        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser appUser = userRepository.findByEmail(name);

        System.out.println("appUser -- " + appUser);
        if (appUser == null) {
            System.out.println("Here");
            AppUser newAppUser = new AppUser();
            newAppUser.setEmail(name);
            newAppUser.setRoles(new String[]{"WORDSET_OWNER"});
            newAppUser.setName(name);
            appUser = userRepository.save(newAppUser);
        }
        if (word.getAppUser() == null) {
            word.setAppUser(appUser);
        }
    }}
