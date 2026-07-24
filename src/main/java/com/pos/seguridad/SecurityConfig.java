package com.pos.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .headers(headers -> headers.frameOptions(frame -> frame.disable())) // Permitir consola H2
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para simplificar las llamadas REST del frontend
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    AntPathRequestMatcher.antMatcher("/mesas/login.html"),
                    AntPathRequestMatcher.antMatcher("/css/**"),
                    AntPathRequestMatcher.antMatcher("/js/**"),
                    AntPathRequestMatcher.antMatcher("/img/**"),
                    AntPathRequestMatcher.antMatcher("/h2-console/**")
                ).permitAll() // Permitir estáticos y consola H2
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/mesas/login.html")
                .loginProcessingUrl("/api/auth/login")
                .defaultSuccessUrl("/mesas/index.html", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api/auth/logout", "GET"))
                .logoutSuccessUrl("/mesas/login.html")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
