package com.marstracker.ttc.service;

import com.marstracker.ttc.model.AppUser;
import com.marstracker.ttc.model.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SpringDataJpaUserDetailsService implements UserDetailsService {
    public static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    private final AppUserRepository repository;

    @Autowired
    public SpringDataJpaUserDetailsService(AppUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        AppUser appUser = repository.findByEmail(name);
        return new User(appUser.getEmail(), appUser.getPassword(),
                AuthorityUtils.createAuthorityList(appUser.getRoles()));
    }

}