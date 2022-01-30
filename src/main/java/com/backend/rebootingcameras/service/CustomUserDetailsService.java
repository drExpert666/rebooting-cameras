package com.backend.rebootingcameras.service;

import com.backend.rebootingcameras.entity.User;
import com.backend.rebootingcameras.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /* ищем юзера по имени, который передали через клиент */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + username));
        System.out.println(user);
        return build(user); // возвращаем юзера с листом authorities
    }

    public User loadUserById(Long id) {
        return userRepository.findUserById(id).orElse(null);
    }

    public static User build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream() // преобразуем сет ролей юзера в стрим
                .map(role -> new SimpleGrantedAuthority(role.name())) // для каждой роли создаём новый элемент SimpleGrantedAuthority
                .collect(Collectors.toList()); // и собираём всё в лист
        System.out.println(authorities);
        return new User(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities); /* теперь юзер содержит лист authorities вместо листа ролей */
    }

}
