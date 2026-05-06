package com.example.demo.config;
 
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.Filter; // Add this import

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter; 

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(request -> {
                var opt = new org.springframework.web.cors.CorsConfiguration();
                opt.setAllowedOrigins(java.util.List.of(
                	    "http://localhost:3000", 
                	    "https://vinayin.netlify.app",
                	    "https://vinaygame.netlify.app"
                	));
                opt.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                opt.setAllowedHeaders(java.util.List.of("*"));
                return opt;
            }))
            .authorizeHttpRequests(auth -> auth
            	    .requestMatchers("/", "/auth/**").permitAll() // Added "/" for the Render live link
            	    .requestMatchers("/api/game/**").authenticated()
            	    .anyRequest().authenticated()
            	)
            // Fix: Explicitly cast if the compiler is being stubborn
            .addFilterBefore((Filter) jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}