package com.doan.WEB_TMDT.config;

import com.doan.WEB_TMDT.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/payment/sepay/webhook").permitAll() // SePay webhook
                        .requestMatchers("/api/payment/{paymentCode}/status").permitAll() // Check status
                        .requestMatchers("/api/employee-registration/apply").permitAll()
                        .requestMatchers("/api/test/**").permitAll()
                        
                        // Public product & category endpoints (for all users)
                        .requestMatchers("/api/categories", "/api/categories/tree", "/api/categories/active").permitAll()
                        .requestMatchers("/api/categories/{id}").permitAll()
                        .requestMatchers("/api/products").permitAll()
                        .requestMatchers("/api/products/{id}").permitAll()
                        .requestMatchers("/api/products/{id}/with-specs").permitAll()
                        .requestMatchers("/api/products/search-by-specs").permitAll()
                        .requestMatchers("/api/products/filter-by-specs").permitAll()
                        .requestMatchers("/api/product/**").permitAll()
                        
                        // Customer endpoints (Cart, Orders, Profile)
                        .requestMatchers("/api/cart/**").hasAnyAuthority("CUSTOMER", "ADMIN")
                        .requestMatchers("/api/orders/**").hasAnyAuthority("CUSTOMER", "ADMIN")
                        .requestMatchers("/api/customer/**").hasAnyAuthority("CUSTOMER", "ADMIN")
                        
                        // Warehouse endpoints (Inventory management)
                        .requestMatchers("/api/inventory/**").hasAnyAuthority("WAREHOUSE", "ADMIN")
                        
                        // Product Manager endpoints (Product & Category management)
                        .requestMatchers("/api/products/warehouse/**").hasAnyAuthority("PRODUCT_MANAGER", "ADMIN")
                        .requestMatchers("/api/products/publish").hasAnyAuthority("PRODUCT_MANAGER", "ADMIN")
                        .requestMatchers("/api/categories").hasAnyAuthority("PRODUCT_MANAGER", "ADMIN")
                        
                        // Admin only endpoints
                        .requestMatchers("/api/employee-registration/list").hasAuthority("ADMIN")
                        .requestMatchers("/api/employee-registration/pending").hasAuthority("ADMIN")
                        .requestMatchers("/api/employee-registration/approve/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ✅ Swagger bypass hoàn toàn
    @Bean
    public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/webjars/**"
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
