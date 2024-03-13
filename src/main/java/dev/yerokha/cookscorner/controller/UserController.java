package dev.yerokha.cookscorner.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yerokha.cookscorner.dto.UpdateProfileRequest;
import dev.yerokha.cookscorner.dto.UpdateProfileResponse;
import dev.yerokha.cookscorner.dto.User;
import dev.yerokha.cookscorner.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

import static dev.yerokha.cookscorner.service.TokenService.getUserIdFromAuthToken;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final Validator validator;


    public UserController(UserService userService, ObjectMapper objectMapper, Validator validator) {
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.validator = validator;
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

    @PutMapping()
    public ResponseEntity<UpdateProfileResponse> updateProfile(@RequestPart("dto") String dto,
                                                               @RequestPart(value = "image", required = false) MultipartFile image,
                                                               Authentication authentication) {

        UpdateProfileRequest request;
        try {
            request = objectMapper.readValue(dto, UpdateProfileRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        validateRequest(request);

        if (image != null) {
            if (!Objects.requireNonNull(image.getContentType()).startsWith("image/")) {
                throw new IllegalArgumentException("Uploaded file is not an image");
            }
        }

        return ResponseEntity.ok(userService.updateUser(request, getUserIdFromAuthToken(authentication), image));
    }

    private void validateRequest(UpdateProfileRequest request) {
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "updateProfileRequest");
        validator.validate(request, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException("Invalid input " + bindingResult.getAllErrors());
        }
    }

}
