package com.backend.rebootingcameras.config;

import com.backend.rebootingcameras.entity.enums.ERole;
import com.backend.rebootingcameras.security.JWTAuthenticationEntryPoint;
import com.backend.rebootingcameras.security.JWTAuthenticationFilter;
import com.backend.rebootingcameras.security.SecurityConstants;
import com.backend.rebootingcameras.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * DI
     */
    private JWTAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    public void setJwtAuthenticationEntryPoint(JWTAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }
    @Autowired
    public void setCustomUserDetailsService(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    /* передаём нашего созданного/найденного из БД в классе CustomUserDetailsService */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        System.out.println("Method: configure(AuthenticationManagerBuilder auth) from SecurityConfig");
        auth.userDetailsService(customUserDetailsService).passwordEncoder(bCryptPasswordEncoder());
    }

    //todo разобраться что тут происходит
    /* предположительно описывается конфигурация перехвата http запросов от клиента */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .exceptionHandling() // обработчик ошибок
                .authenticationEntryPoint(jwtAuthenticationEntryPoint) // передаём класс, в задаём поля, которые будут отображаться при ошибке
                .and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(SecurityConstants.SIGN_UP_URLS).permitAll() // разрешаем доступ всем к этим эндпоинтам "/api/auth/**" и всё что за ним
                .antMatchers(SecurityConstants.GET_USERS_URLS).hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated(); // все остальные url должны быть авторизированы
        /* видимо добавляем фильтр перед любыйм запросом (проверка jw-токенов)  */
        System.out.println("Method: configure(HttpSecurity http) from SecurityConfig");
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    /** прописываем бины для spring контейнера */
    @Override
    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JWTAuthenticationFilter jwtAuthenticationFilter() {
        return new JWTAuthenticationFilter();
    }
}
