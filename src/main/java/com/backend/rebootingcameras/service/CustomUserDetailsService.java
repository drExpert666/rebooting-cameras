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
        System.out.println("Метод: loadUserByUsername" + user);
        return build(user); // возвращаем юзера с листом authorities
    }

    public User loadUserById(Long id) {
        User user = userRepository.findUserById(id).orElse(null);
        if (user != null) {
            user.setAuthorities(user.getRoles().stream() // преобразуем сет ролей юзера в стрим
                    .map(role -> new SimpleGrantedAuthority(role.name())) // для каждой роли создаём новый элемент SimpleGrantedAuthority
                    .collect(Collectors.toList()));
        }
        return user;
    }

    public static User build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream() // преобразуем сет ролей юзера в стрим
                .map(role -> new SimpleGrantedAuthority(role.name())) // для каждой роли создаём новый элемент SimpleGrantedAuthority
                .collect(Collectors.toList()); // и собираём всё в лист
        System.out.println("Роли юзера: " + authorities);
        return new User(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities); /* теперь юзер содержит лист authorities вместо листа ролей */
    }

}
