package com.backend.rebootingcameras.service;

import com.backend.rebootingcameras.entity.User;
import com.backend.rebootingcameras.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;

/** сервис по созданию нового пользователя */
//todo написать реализацию позже, т.к. сейчас в задаче регистрация пользователей не стоит
@Service
public class UserService {

    /* логгер по этому классу */
    //todo посмотреть, надо ли менять реализацию,
    // так как у меня уже подключен собственный логгер у всего приложения (RebootingCamerasApplication.class)
    public static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    /* бин для энкодера прописали вручную в конфиг-классе, чтобы DI было возможным */
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** SecurityContextHolder, в нем содержится информация о текущем контексте безопасности приложения,
     *  который включает в себя подробную информацию о пользователе(Principal) работающем в настоящее время с приложением. */
    public User getCurrentUser(Principal principal) {
       return getUserByPrincipal(principal);
    }

    public User getUserByPrincipal(Principal principal) {
        String username = principal.getName();
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found with username " + username));
    }

}
