package com.backend.rebootingcameras.payload.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/** класс  */
@Data
public class LoginRequest {

    /* аннотация из библиотеки javax.validation */
    @NotEmpty(message = "Username cannot be empty")
    private String username;
    @NotEmpty(message = "Username cannot be empty")
    private String password;

}
