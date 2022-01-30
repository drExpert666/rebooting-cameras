package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.entity.User;
import com.backend.rebootingcameras.service.UserService;
import com.backend.rebootingcameras.validations.ResponseErrorValidations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@RequestMapping("/api/user")
public class UserController {

    private UserService userService;
    private ResponseErrorValidations responseErrorValidations;
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    @Autowired
    public void setResponseErrorValidations(ResponseErrorValidations responseErrorValidations) {
        this.responseErrorValidations = responseErrorValidations;
    }

    @GetMapping("/")
    public ResponseEntity<User> getCurrentUser(Principal principal) {
        User user = userService.getCurrentUser(principal);
        return new ResponseEntity<>(user, HttpStatus.OK) ;
    }

}
