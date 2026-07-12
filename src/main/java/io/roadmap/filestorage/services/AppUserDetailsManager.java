package io.roadmap.filestorage.services;

import io.roadmap.filestorage.entities.User;
import io.roadmap.filestorage.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppUserDetailsManager implements UserDetailsManager {

    private final UserRepository userRepository;

    @Override
    public void createUser(UserDetails user) {
        userRepository.save((User) user);
    }

    @Override
    public void updateUser(UserDetails user) {
        userRepository.save((User) user);
    }

    @Override
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        userRepository.delete(user);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        User user = userRepository.findByPassword(oldPassword).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setPassword(newPassword);
        userRepository.save(user);
    }

    @Override
    public boolean userExists(String username) {
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isPresent()) return true;
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
