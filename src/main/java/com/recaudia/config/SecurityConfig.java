package com.recaudia.config;

import com.recaudia.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Autenticación pública de usuarios de empresa y plataforma.
                        .requestMatchers("/api/auth/**", "/api/v1/auth/**", "/api/master/auth/**", "/api/v1/master/auth/**").permitAll()

                        // Documentación.
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Operaciones de plataforma.
                        .requestMatchers("/api/master/**", "/api/v1/master/**").hasRole("MASTER")
                        .requestMatchers("/api/provisioning/**", "/api/v1/provisioning/**").hasRole("MASTER")

                        // Administración de usuarios y empresa.
                        .requestMatchers("/api/usuarios/**", "/api/v1/usuarios/**", "/api/empresas/**", "/api/v1/empresas/**")
                            .hasRole("ADMIN")

                        // Configuración de servicios.
                        .requestMatchers(HttpMethod.POST, "/api/servicios/**", "/api/v1/servicios/**")
                            .hasAnyRole("ADMIN", "FACTURACION")
                        .requestMatchers(HttpMethod.GET, "/api/servicios/**", "/api/v1/servicios/**")
                            .hasAnyRole("ADMIN", "FACTURACION", "COBRANZAS", "BI")
                        .requestMatchers(HttpMethod.PUT, "/api/servicios/**", "/api/v1/servicios/**")
                            .hasAnyRole("ADMIN", "FACTURACION")
                        .requestMatchers(HttpMethod.DELETE, "/api/servicios/**", "/api/v1/servicios/**")
                            .hasRole("ADMIN")

                        // Notas de crédito.
                        .requestMatchers(HttpMethod.POST, "/api/notas-credito/**", "/api/v1/notas-credito/**")
                            .hasAnyRole("ADMIN", "FACTURACION")

                        // Cuentas.
                        .requestMatchers(HttpMethod.POST, "/api/cuentas/**", "/api/v1/cuentas/**")
                            .hasAnyRole("ADMIN", "FACTURACION")
                        .requestMatchers(HttpMethod.PUT, "/api/cuentas/**", "/api/v1/cuentas/**")
                            .hasAnyRole("ADMIN", "FACTURACION")
                        .requestMatchers(HttpMethod.DELETE, "/api/cuentas/**", "/api/v1/cuentas/**")
                            .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/cuentas/**", "/api/v1/cuentas/**")
                            .hasAnyRole("ADMIN", "FACTURACION", "COBRANZAS", "BI")
                        .requestMatchers(HttpMethod.POST, "/api/cuentas/*/servicios", "/api/v1/cuentas/*/servicios")
                            .hasAnyRole("ADMIN", "FACTURACION")

                        // Facturación.
                        .requestMatchers(HttpMethod.POST, "/api/facturas/**", "/api/v1/facturas/**")
                            .hasAnyRole("ADMIN", "FACTURACION")
                        .requestMatchers(HttpMethod.GET, "/api/facturas/**", "/api/v1/facturas/**")
                            .hasAnyRole("ADMIN", "FACTURACION", "COBRANZAS", "BI")
                        .requestMatchers(HttpMethod.PATCH, "/api/facturas/**", "/api/v1/facturas/**")
                            .hasAnyRole("ADMIN", "FACTURACION")

                        // Cobranzas.
                        .requestMatchers("/api/pagos/**", "/api/v1/pagos/**")
                            .hasAnyRole("ADMIN", "COBRANZAS")
                        .requestMatchers(HttpMethod.PATCH, "/api/alertas/**", "/api/v1/alertas/**")
                            .hasAnyRole("ADMIN", "COBRANZAS")
                        .requestMatchers(HttpMethod.GET, "/api/alertas/**", "/api/v1/alertas/**")
                            .hasAnyRole("ADMIN", "COBRANZAS", "BI")

                        // Dashboard/BI.
                        .requestMatchers("/api/dashboard/**", "/api/v1/dashboard/**")
                            .hasAnyRole("ADMIN", "COBRANZAS", "BI")

                        // Automatización, chatbot, archivos, reportes y auditoría.
                        .requestMatchers("/api/chatbot/**", "/api/v1/chatbot/**")
                            .hasAnyRole("ADMIN", "COBRANZAS", "BI")
                        .requestMatchers("/api/files/**", "/api/v1/files/**")
                            .hasAnyRole("ADMIN", "FACTURACION")
                        .requestMatchers("/api/operaciones/**", "/api/v1/operaciones/**", "/api/conciliacion/**", "/api/v1/conciliacion/**")
                            .hasAnyRole("ADMIN", "FACTURACION", "COBRANZAS", "BI")
                        .requestMatchers("/api/notificaciones/**", "/api/v1/notificaciones/**", "/api/whatsapp/**", "/api/v1/whatsapp/**")
                            .hasAnyRole("ADMIN", "COBRANZAS")
                        .requestMatchers("/api/reportes/**", "/api/v1/reportes/**", "/api/auditoria/**", "/api/v1/auditoria/**")
                            .hasAnyRole("ADMIN", "BI")

                        // Cualquier otro endpoint de negocio requiere un usuario autenticado.
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
