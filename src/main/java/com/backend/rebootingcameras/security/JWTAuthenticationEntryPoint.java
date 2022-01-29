package com.backend.rebootingcameras.security;

import com.backend.rebootingcameras.payload.response.InvalidLoginResponse;
import com.google.gson.Gson;
import org.apache.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/* ловим ошибки, если параметры были заполнены не правильно
* выдаём ошибку авторизации, если юзер пытается получить защищенный ресурс когда не аторизирован  */
@Component
public class JWTAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        InvalidLoginResponse loginResponse = new InvalidLoginResponse();
        // библу Gson внедрили отдельно в градл
        String jsonLoginResponse = new Gson().toJson(loginResponse);
        response.setContentType(SecurityConstants.CONTENT_TYPE); // выставляем тип json-у
        response.setStatus(HttpStatus.SC_UNAUTHORIZED); // ставим статус 401
        response.getWriter().println(jsonLoginResponse);
    }
}
