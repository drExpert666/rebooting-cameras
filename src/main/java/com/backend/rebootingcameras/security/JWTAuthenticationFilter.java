package com.backend.rebootingcameras.security;

import com.backend.rebootingcameras.entity.User;
import com.backend.rebootingcameras.service.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class JWTAuthenticationFilter extends OncePerRequestFilter {


    /* логгер по этому классу */
    //todo посмотреть, надо ли менять реализацию,
    // так как у меня уже подключен собственный логгер у всего приложения (RebootingCamerasApplication.class)
    public static final Logger LOG = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    private JWTTokenProvider jwtTokenProvider;

    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    public void setJwtTokenProvider(JWTTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Autowired
    public void setCustomUserDetailsService(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJWTFromRequest(request);
            // проверяем, что jwt не null и что при декодировании токена вернулся true (не было поймано эксепшенов)
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(jwt); // получаем id юзера из токена
                User userDetails = customUserDetailsService.loadUserById(userId); // получаем юзера по id из БД

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, Collections.emptyList());

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

        } catch (Exception exception) {
            LOG.error("Could not set user authentication" + exception);
        }
         /* внедрились в цепочку запроса и ответа (получаем запрос, что-то с ним делает и отдаём ответ)*/
        filterChain.doFilter(request, response);

    }

    private String getJWTFromRequest(HttpServletRequest request) {
        // каждый раз, когда мы делаем запрос с ангуляра к бэку, мы передаём токен в хэдере
        String bearToken = request.getHeader(SecurityConstants.HEADER_STRING);
        // проверяем, есть ли в хэдере наш префикс
        if (StringUtils.hasText(bearToken) && bearToken.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            // отделяем префикс и возвращаем строку после него
            return bearToken.split(" ")[1];
        }
        return null;
    }

}
