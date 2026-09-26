package com.recaudia.security;

import com.recaudia.config.DatabaseTenantGuard;
import com.recaudia.domain.Usuario;
import com.recaudia.domain.UsuarioMaestro;
import com.recaudia.master.repository.UsuarioMaestroRepository;
import com.recaudia.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioMaestroRepository usuarioMaestroRepository;
    private final DatabaseTenantGuard tenantGuard;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            final String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            try {
                final String jwt = authHeader.substring(7);
                final String email = jwtService.extractUsername(jwt);
                final String tipo = jwtService.extractTipo(jwt);

                if (email == null || tipo == null
                        || SecurityContextHolder.getContext().getAuthentication() != null) {
                    filterChain.doFilter(request, response);
                    return;
                }

                if ("MASTER".equals(tipo)) {
                    authenticateMaster(jwt, email, request);
                } else if ("TENANT".equals(tipo)) {
                    authenticateTenant(jwt, email, request);
                }
            } catch (RuntimeException ignored) {
                SecurityContextHolder.clearContext();
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private void authenticateMaster(String jwt, String email, HttpServletRequest request) {
        UsuarioMaestro usuario = usuarioMaestroRepository.findByEmail(email).orElse(null);

        if (usuario != null
                && Boolean.TRUE.equals(usuario.getActivo())
                && jwtService.isTokenValid(jwt, email)) {

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_MASTER"));
            var authToken = new UsernamePasswordAuthenticationToken(usuario, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }

    private void authenticateTenant(String jwt, String email, HttpServletRequest request) {
        final Long empresaId = jwtService.extractEmpresaId(jwt);

        if (empresaId == null) {
            return;
        }

        tenantGuard.select(empresaId);
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario != null
                && Boolean.TRUE.equals(usuario.getActivo())
                && jwtService.isTokenValid(jwt, email)
                && usuario.getEmpresa() != null
                && empresaId.equals(usuario.getEmpresa().getId())) {

            var authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())
            );

            var authToken = new UsernamePasswordAuthenticationToken(usuario, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
}
