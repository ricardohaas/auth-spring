package com.authproject.controller;

import com.authproject.controller.dto.LoginRequestDto;
import com.authproject.controller.dto.LoginResponseDto;
import com.authproject.entities.User;
import com.authproject.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class TokenController {

    private final JwtEncoder jwtEncoder;

    private final BCryptPasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    public TokenController(JwtEncoder jwtEncoder,
                           UserRepository userRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder
                           ){
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.passwordEncoder = bCryptPasswordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto){
        Optional<User> user = userRepository.findByUsername(loginRequestDto.username());
        if(user.isEmpty() || user.get().isLoginIncorrect(loginRequestDto, this.passwordEncoder)){
            throw  new BadCredentialsException("Usuário ou senha inválida");
        }

        var now = Instant.now();
        var expiresIn = 600L;

        var roles = user.get().getRoles()
                .stream()
                .map(role -> "" + role.getName().toUpperCase())
                .collect(Collectors.joining(" "));

        var scopes = user.get().getRoles()
                .stream()
                .map(role -> "" + role.getName().toUpperCase())
                .collect(Collectors.joining(" "));


        var claims = JwtClaimsSet.builder()
                .issuer("auth_spring_training")
                .subject(user.get().getUserId().toString())
                .issuedAt(now)
                .claim("role", roles)
                .claim("scope", scopes)
                .expiresAt(now.plusSeconds(expiresIn))
                .build();

        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return ResponseEntity.ok(new LoginResponseDto(jwtValue, expiresIn));
    }
}
