package com.recaudia.master.service;

import com.recaudia.master.repository.UsuarioMaestroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MasterUserBootstrap implements ApplicationRunner {

    private final UsuarioMaestroRepository repository;
    private final MasterAuthService service;

    @Value("${app.master.bootstrap.email:}")
    private String email;

    @Value("${app.master.bootstrap.password:}")
    private String password;

    @Value("${app.master.bootstrap.name:Recaud.IA Master}")
    private String name;

    @Override
    public void run(ApplicationArguments args) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return;
        }

        if (!repository.existsByEmail(email)) {
            service.create(name, email, password);
        }
    }
}
