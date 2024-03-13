package dev.yerokha.cookscorner.controller;

import dev.yerokha.cookscorner.dto.User;
import dev.yerokha.cookscorner.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static dev.yerokha.cookscorner.service.TokenService.getUserIdFromAuthToken;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> showProfile(@PathVariable Long userId, Authentication authentication) {
        Long userIdFromAuthToken = null;
        if (authentication != null) {
            userIdFromAuthToken = getUserIdFromAuthToken(authentication);
        }
        return ResponseEntity.ok(userService.getUser(userId, userIdFromAuthToken));
    }

    @PostMapping("/{userId}/follow")
    public ResponseEntity<String> follow(@PathVariable Long userId, Authentication authentication) {
        userService.follow(userId, getUserIdFromAuthToken(authentication));
        return ResponseEntity.ok("You followed the user");
    }

    @PostMapping("/{userId}/unfollow")
    public ResponseEntity<String> unfollow(@PathVariable Long userId, Authentication authentication) {
        userService.unfollow(userId, getUserIdFromAuthToken(authentication));
        return ResponseEntity.ok("You unfollowed the user");
    }

}
