package dev.yerokha.cookscorner.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yerokha.cookscorner.dto.RecipeDto;
import dev.yerokha.cookscorner.dto.UpdateProfileRequest;
import dev.yerokha.cookscorner.dto.UpdateProfileResponse;
import dev.yerokha.cookscorner.dto.User;
import dev.yerokha.cookscorner.dto.UserDto;
import dev.yerokha.cookscorner.service.RecipeService;
import dev.yerokha.cookscorner.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Objects;

import static dev.yerokha.cookscorner.service.TokenService.getUserIdFromAuthToken;

@Tag(name = "User", description = "Endpoints for user interaction")
@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;
    private final RecipeService recipeService;
    private final ObjectMapper objectMapper;
    private final Validator validator;


    public UserController(UserService userService, RecipeService recipeService, ObjectMapper objectMapper, Validator validator) {
        this.userService = userService;
        this.recipeService = recipeService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Operation(
            summary = "Get user profile", description = "Retrieve a user information, if authenticated user follows" +
            "the target user, isFollowed=true, if user is not authenticated, then isFollowed=null", tags = {"user", "get"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "User profile"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
    @GetMapping("/{userId}")
    public ResponseEntity<User> showProfile(@PathVariable Long userId, Authentication authentication) {
        Long userIdFromAuthToken = null;
        if (authentication != null) {
            userIdFromAuthToken = getUserIdFromAuthToken(authentication);
        }
        return ResponseEntity.ok(userService.getUser(userId, userIdFromAuthToken));
    }

    @Operation(
            summary = "Follow", description = "Follow the user", tags = {"user", "post"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Follow success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "User can not follow himself", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
    @PostMapping("/follow/{userId}")
    public ResponseEntity<String> follow(@PathVariable Long userId, Authentication authentication) {
        userService.follow(userId, getUserIdFromAuthToken(authentication));
        return ResponseEntity.ok("You followed the user");
    }

    @Operation(
            summary = "Unfollow", description = "Unfollow the user", tags = {"user", "post"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Unfollow success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
    @PostMapping("/unfollow/{userId}")
    public ResponseEntity<String> unfollow(@PathVariable Long userId, Authentication authentication) {
        userService.unfollow(userId, getUserIdFromAuthToken(authentication));
        return ResponseEntity.ok("You unfollowed the user");
    }

    @Operation(
            summary = "Update profile", description = "Update user information", tags = {"user", "put"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Update success"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or file is not an image", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
    @PutMapping
    public ResponseEntity<UpdateProfileResponse>
    updateProfile(@RequestPart("dto") String dto,
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

    @Operation(
            summary = "User search", description = "Search for cooks by \"query\" param in name or bio." +
            "query is not required, else method returns most popular cooks",
            tags = {"user", "get"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Search success"),
            },
            parameters = {
                    @Parameter(name = "query", description = "Parameter to search by",
                            examples = {
                                    @ExampleObject(name = "Andrew", value = "andrew")
                            }),
                    @Parameter(name = "page", description = "Page number", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "12")
            }
    )
    @GetMapping("/search")
    public ResponseEntity<Page<UserDto>> search(@RequestParam(required = false) Map<String, String> params) {
        return ResponseEntity.ok(userService.search(params));
    }

    @Operation(
            summary = "User search", description = "Search for cooks by \"query\" param in name or bio." +
            "query is not required, else method returns most popular cooks",
            tags = {"user", "get"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Search success"),
            },
            parameters = {
                    @Parameter(name = "page", description = "Page number", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "12")
            }
    )
    @GetMapping("/recipes/{userId}")
    public ResponseEntity<Page<RecipeDto>> showUserRecipes(@RequestParam(required = false) Map<String, String> params,
                                                           @PathVariable Long userId,
                                                           Authentication authentication) {
        Long userIdFromAuthToken = null;
        if (authentication != null) {
            userIdFromAuthToken = getUserIdFromAuthToken(authentication);
        }
        return ResponseEntity.ok(recipeService.getUserRecipes(userId, userIdFromAuthToken, params));
    }

}
