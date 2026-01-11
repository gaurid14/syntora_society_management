////package com.example.societyhub.config;
////
////import jakarta.servlet.http.HttpServletResponse;
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////import org.springframework.lang.NonNull;
////import org.springframework.lang.NonNullApi;
////import org.springframework.security.config.annotation.web.builders.HttpSecurity;
////import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
////import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
////import org.springframework.security.web.SecurityFilterChain;
////import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
////import org.springframework.security.web.csrf.CsrfTokenRepository;
////import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
////import org.springframework.web.servlet.config.annotation.CorsRegistry;
////import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
////import org.apache.logging.log4j.LogManager;
////import org.apache.logging.log4j.Logger;
////
//////import javax.servlet.http.HttpServletResponse;
////
////import static org.springframework.security.config.Customizer.withDefaults;
////
////@Configuration
////@EnableWebSecurity
////public class SecurityConfig {
////
////    @Bean
////    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
////        http
////                .csrf(AbstractHttpConfigurer::disable)
////                .authorizeHttpRequests(authorizeRequests ->
////                        authorizeRequests
////                                .requestMatchers("/api/**").permitAll()
////                                .requestMatchers("/").permitAll()
////                                .anyRequest().permitAll()
////                )
////                .cors(withDefaults())
////                .exceptionHandling(exceptionHandling ->
////                        exceptionHandling
////                                .authenticationEntryPoint((request, response, authException) ->
////                                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied"))
////                );
////
////        return http.build();
////    }
////    @Bean
////    public WebMvcConfigurer corsConfigurer() {
////        return new WebMvcConfigurer() {
////            @Override
////            public void addCorsMappings(CorsRegistry registry) {
////                registry.addMapping("/api/**")
////                        .allowedOrigins("http://localhost:8080") // Add your frontend URL here
////                        .allowedMethods("GET", "POST", "PUT", "DELETE")
////                        .allowedHeaders("*");
////            }
////        };
////    }
////}
////
////
////
////
////
//package com.example.societyhub.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                // Authorize requests
//                .authorizeHttpRequests((requests) -> requests
//                        // Public routes (anyone can access)
//                        .requestMatchers("/index", "/home", "/login", "/register", "/api/auth/login", "/api/auth/register").permitAll()
//                        // Protected routes (requires authentication)
//                        .requestMatchers("/admin/**").authenticated()
//                )
//                // Configure session management
//                .sessionManagement((session) -> session
//                        .maximumSessions(1) // Limit to 1 session per user
//                )
//                // Form login configuration
//                .formLogin((form) -> form
//                        .loginPage("/login") // Custom login page
//                        .permitAll()
//                )
//                // Logout configuration
//                .logout((logout) -> logout
//                        .logoutUrl("/api/auth/logout")
//                        .logoutSuccessUrl("/login")
//                        .invalidateHttpSession(true) // Invalidate session on logout
//                        .clearAuthentication(true)
//                        .permitAll()
//                )
//                // CSRF protection with cookies
//                .csrf((csrf) -> csrf
//                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//                );
//
//        return http.build();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}
