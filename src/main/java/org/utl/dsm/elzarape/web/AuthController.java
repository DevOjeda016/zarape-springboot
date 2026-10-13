package org.utl.dsm.elzarape.web;

import org.utl.dsm.elzarape.dto.Dtos.*;
import org.utl.dsm.elzarape.model.AppUser;
import org.utl.dsm.elzarape.model.Role;
import org.utl.dsm.elzarape.repository.UserRepository;
import org.utl.dsm.elzarape.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        AppUser user = users.findByUsername(req.username())
                .filter(AppUser::isActive)
                .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña inválidos"));
        return new AuthResponse(jwt.generate(user), UserView.of(user));
    }

    /** Registro público: siempre crea un cliente (el rol no se puede elegir). */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest req) {
        if (users.existsByUsername(req.username())) {
            throw ApiException.conflict("El nombre de usuario ya existe");
        }
        AppUser u = new AppUser();
        u.setUsername(req.username());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setRole(Role.CLIENT);
        u.setName(req.name());
        u.setLastName(req.lastName());
        u.setPhone(req.phone());
        users.save(u);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(jwt.generate(u), UserView.of(u)));
    }

    @GetMapping("/me")
    public UserView me(@AuthenticationPrincipal AppUser user) {
        return UserView.of(user);
    }
}
