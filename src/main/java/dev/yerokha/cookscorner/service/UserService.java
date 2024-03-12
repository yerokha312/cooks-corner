package dev.yerokha.cookscorner.service;

import dev.yerokha.cookscorner.dto.User;
import dev.yerokha.cookscorner.entity.UserEntity;
import dev.yerokha.cookscorner.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("User not found"));
    }

    public User getUser(String email) {
        UserEntity entity = (UserEntity) loadUserByUsername(email);
        return new User(entity.getName(), entity.getEmail());
    }

}

