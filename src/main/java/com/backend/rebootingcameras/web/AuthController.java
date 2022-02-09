package com.backend.rebootingcameras.web;

import com.backend.rebootingcameras.payload.request.LoginRequest;
import com.backend.rebootingcameras.payload.response.JWTTokenSuccessResponse;
import com.backend.rebootingcameras.security.JWTTokenProvider;
import com.backend.rebootingcameras.security.SecurityConstants;
import com.backend.rebootingcameras.service.UserService;
import com.backend.rebootingcameras.validations.ResponseErrorValidations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/** контроллер, отвечающий за авторизацию пользователей */
@CrossOrigin
@RestController
@RequestMapping("/api/auth")
@PreAuthorize("permitAll()")
public class AuthController {

    private ResponseErrorValidations responseErrorValidations;
    private AuthenticationManager authenticationManager;
    private UserService userService;
    private JWTTokenProvider jwtTokenProvider;

    @Autowired
    public void setJwtTokenProvider(JWTTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Autowired
    public void setResponseErrorValidations(ResponseErrorValidations responseErrorValidations) {
        this.responseErrorValidations = responseErrorValidations;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @CrossOrigin
    @PostMapping("/signin")
    public ResponseEntity<Object> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, BindingResult bindingResult) {
        ResponseEntity<Object> errors = responseErrorValidations.mapValidationService(bindingResult);
        if (!ObjectUtils.isEmpty(errors)) return errors; // если есть ошибки, то их вовзращаем

        Authentication authentication  = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword()
        ));

        /* задаём секьюрити контекст нашему приложению */
        SecurityContextHolder.getContext().setAuthentication(authentication);
        /* генерируем токен */
        String jwt = SecurityConstants.TOKEN_PREFIX + jwtTokenProvider.generateToken(authentication); // создаём токен

        return ResponseEntity.ok(new JWTTokenSuccessResponse(true, jwt));
    }

}
