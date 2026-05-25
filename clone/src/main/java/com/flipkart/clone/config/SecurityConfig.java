//package com.flipkart.clone.config;
//
//import com.flipkart.clone.security.JwtAuthFilter;
//import com.flipkart.clone.security.UserDetailsServiceImpl;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity          // enables @PreAuthorize on controllers
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final JwtAuthFilter jwtAuthFilter;
//    private final UserDetailsServiceImpl userDetailsService;
//
//    // ── Public routes — no JWT needed ────────────────────────────
//    private static final String[] PUBLIC_URLS = {
//            "/api/auth/**",
//            "/api/products/**",
//            "/api/categories/**",
//            "/api/banners/**",
//            "/api/deals/**",
//            "/api/reviews/**",           // ← add
//            "/api/questions/**",         // ← add
//            "/api/price-history/**",     // ← add
//            "/api/shipments/track/**",   // ← add (public tracking)
//            "/swagger-ui/**",
//            "/v3/api-docs/**"
//    };
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                // Disable CSRF — we use JWT, not cookies
//                .csrf(csrf -> csrf.disable())
//
//                // Define route permissions
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(PUBLIC_URLS).permitAll()
//
//                        // Admin-only routes
//                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
//
//                        // Vendor-only routes
//                        .requestMatchers("/api/vendor/**").hasRole("VENDOR")
//
//                        // Everything else needs authentication
//                        .anyRequest().authenticated()
//                )
//
//                // Stateless — no sessions, JWT only
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//
//                // Use our custom UserDetailsService + BCrypt
//                .authenticationProvider(authenticationProvider())
//
//                // Add JWT filter BEFORE Spring's default auth filter
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    // ── BCrypt password encoder ──────────────────────────────────
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    // ── Wire UserDetailsService + PasswordEncoder together ───────
////    @Bean
////    public AuthenticationProvider authenticationProvider() {
////        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
////        provider.setUserDetailsService(userDetailsService);
////        provider.setPasswordEncoder(passwordEncoder());
////        return provider;
////    }
//
//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//
//        DaoAuthenticationProvider provider =
//                new DaoAuthenticationProvider(userDetailsService);
//
//        provider.setPasswordEncoder(passwordEncoder());
//
//        return provider;
//    }
//
//    // ── AuthenticationManager — used in AuthService.login() ──────
//    @Bean
//    public AuthenticationManager authenticationManager(
//            AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//}




//new//













//new Second//

package com.flipkart.clone.config;

import com.flipkart.clone.security.JwtAuthFilter;
import com.flipkart.clone.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    // ── Public routes — no JWT needed ────────────────────────────
    private static final String[] PUBLIC_URLS = {
            "/api/auth/**",
            "/api/products/**",
            "/api/categories/**",
            "/api/banners/**",
            "/api/deals/**",
            "/api/reviews/**",
            "/api/questions/**",
            "/api/price-history/**",
            "/api/shipments/track/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/upload/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http

                // ENABLE CORS
                .cors(cors -> {})

                // Disable CSRF for JWT APIs
                .csrf(csrf -> csrf.disable())

                // Route permissions
//                .authorizeHttpRequests(auth -> auth
//
//                        // Allow preflight OPTIONS requests
//                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//
//                        // Public routes
//                        .requestMatchers(PUBLIC_URLS).permitAll()
//
//                        // Admin-only routes
//                        .requestMatchers("/api/admin/**")
//                        .hasRole("ADMIN")
//
//                        // Vendor-only routes
//                        .requestMatchers("/api/vendor/**")
//                        .hasRole("VENDOR")
//
//                        // Everything else requires authentication
//                        .anyRequest().authenticated()
//                )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(PUBLIC_URLS).permitAll()

                        // ✅ allow vendor registration without login
                        .requestMatchers("/api/vendor/register").permitAll()
                        .requestMatchers("/api/tts/**").permitAll()

                        // Admin routes
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Vendor routes (after login)
                        .requestMatchers("/api/vendor/**").hasRole("VENDOR")



                        .anyRequest().authenticated()
                )
                // Stateless session management
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS)
                )

                // Authentication provider
                .authenticationProvider(authenticationProvider())

                // JWT filter
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // ── Password Encoder ──────────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ── Authentication Provider ──────────────────────────────────
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    // ── Authentication Manager ───────────────────────────────────
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config)
            throws Exception {

        return config.getAuthenticationManager();
   }
}

























