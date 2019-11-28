package edu.baylor.flarn.controllers;

import edu.baylor.flarn.exceptions.EmailSendingException;
import edu.baylor.flarn.exceptions.InvalidConfirmationCodeException;
import edu.baylor.flarn.exceptions.RecordNotFoundException;
import edu.baylor.flarn.models.User;
import edu.baylor.flarn.repositories.UserRepository;
import edu.baylor.flarn.resources.AuthenticationRequest;
import edu.baylor.flarn.resources.ConfirmUserRequest;
import edu.baylor.flarn.resources.UpdatePasswordRequest;
import edu.baylor.flarn.resources.UserRegistration;
import edu.baylor.flarn.security.JwtTokenProvider;
import edu.baylor.flarn.services.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository users;
    private final UserService userService;

    public AuthenticationController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
                                    UserRepository users, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.users = users;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody AuthenticationRequest data) {
        try {
            String username = data.getUsername();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, data.getPassword()));
            String token = jwtTokenProvider.createToken(username,
                    this.users.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Username " + username +
                            "not found")).getRoles());

            User user = userService.getUserByUsername(username);

            Map<Object, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("token", token);
            model.put("userId", user.getId());
            model.put("enabled", user.isEnabled());
            model.put("userType", user.getUserType());
            return ok(model);
        } catch (AuthenticationException | RecordNotFoundException e) {
            InvalidLogin invalidLogin = new InvalidLogin(data.getUsername(), data.getPassword(), "Invalid username and " +
                    "password");
            return new ResponseEntity<>(invalidLogin, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public User register(@RequestBody UserRegistration data) throws EmailSendingException {
        User user = userService.registerUser(data);
        userService.sendConfirmationCode(user);
        return user;
    }

    @GetMapping("/sendConfirmationCode")
    public void sendConfirmationCode(@RequestParam String username) throws RecordNotFoundException, EmailSendingException {
        User user = userService.getUserByUsername(username);
        userService.sendConfirmationCode(user);
    }

    @PostMapping("/confirm")
    public User confirmAccount(@RequestBody ConfirmUserRequest confirmUserRequest) throws RecordNotFoundException, InvalidConfirmationCodeException {
        return userService.confirmUser(confirmUserRequest);
    }

    @PostMapping("/updatePassword")
    public User updatePassword(@RequestBody UpdatePasswordRequest updatePasswordRequest) throws RecordNotFoundException, InvalidConfirmationCodeException {
        return userService.updatePassword(updatePasswordRequest);
    }

    @AllArgsConstructor
    @Data
    private static class InvalidLogin {
        private String username;
        private String password;
        private String error;
    }

}
