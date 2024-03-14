package dev.yerokha.cookscorner.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yerokha.cookscorner.dto.CreateRecipeRequest;
import dev.yerokha.cookscorner.dto.Recipe;
import dev.yerokha.cookscorner.dto.RecipeDto;
import dev.yerokha.cookscorner.service.RecipeService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/v1/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public RecipeController(RecipeService recipeService, ObjectMapper objectMapper, Validator validator) {
        this.recipeService = recipeService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping
    public ResponseEntity<String> createRecipe(@RequestPart String dto,
                                               @RequestPart MultipartFile image,
                                               Authentication authentication) {
        CreateRecipeRequest createRecipeRequest;
        try {
            createRecipeRequest = objectMapper.readValue(dto, CreateRecipeRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        validateRequest(createRecipeRequest);

        if (image == null) {
            throw new IllegalArgumentException("Uploading image is mandatory");
        }

        if (!Objects.requireNonNull(image.getContentType()).startsWith("image/")) {
            throw new IllegalArgumentException("Uploaded file is not an image");
        }

        recipeService.addRecipe(createRecipeRequest, getUserIdFromAuthToken(authentication), image);

        return ResponseEntity.ok("CreateRecipeRequest created successfully");
    }

    private void validateRequest(CreateRecipeRequest createRecipeRequest) {
        BindingResult bindingResult = new BeanPropertyBindingResult(createRecipeRequest, "createRecipeRequest");
        validator.validate(createRecipeRequest, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException("Invalid input " + bindingResult.getAllErrors());
        }
    }

    @GetMapping
    public ResponseEntity<Page<RecipeDto>> getRecipes(@RequestParam Map<String, String> params, Authentication authentication) {
        Long userIdFromAuthToken = null;
        if (authentication != null) {
            userIdFromAuthToken = getUserIdFromAuthToken(authentication);
        }

        String query = params.get("query");

        if (query != null && (query.equals("my") || query.equals("saved")))
            if (userIdFromAuthToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        return ResponseEntity.ok(recipeService.getRecipes(params, userIdFromAuthToken));
    }

    @GetMapping("/{recipeId}")
    public ResponseEntity<Recipe> getRecipeById(Authentication authentication, @PathVariable Long recipeId) {
        Long userIdFromAuthToken = null;

        if (authentication != null) {
            userIdFromAuthToken = getUserIdFromAuthToken(authentication);
        }

        return ResponseEntity.ok(recipeService.getRecipeById(recipeId, userIdFromAuthToken));
    }

    @PutMapping("/{recipeId}/like")
    public ResponseEntity<String> likeRecipe(Authentication authentication, @PathVariable Long recipeId) {
        Long userIdFromAuthToken = null;

        if (authentication != null) {
            userIdFromAuthToken = getUserIdFromAuthToken(authentication);
        }

        recipeService.likeRecipe(recipeId, userIdFromAuthToken);

        return ResponseEntity.ok("Recipe liked successfully");
    }

    @PutMapping("/{recipeId}/dislike")
    public ResponseEntity<String> dislikeRecipe(Authentication authentication, @PathVariable Long recipeId) {
        Long userIdFromAuthToken = null;

        if (authentication != null) {
            userIdFromAuthToken = getUserIdFromAuthToken(authentication);
        }

        recipeService.dislikeRecipe(recipeId, userIdFromAuthToken);

        return ResponseEntity.ok("Recipe disliked successfully");
    }

    @PutMapping("/{recipeId}/bookmark")
    public ResponseEntity<String> bookmarkRecipe(Authentication authentication, @PathVariable Long recipeId) {
        Long userIdFromAuthToken = null;

        if (authentication != null) {
            userIdFromAuthToken = getUserIdFromAuthToken(authentication);
        }

        recipeService.bookmarkRecipe(recipeId, userIdFromAuthToken);

        return ResponseEntity.ok("Recipe bookmarked successfully");
    }

    @PutMapping("/{recipeId}/remove-bookmark")
    public ResponseEntity<String> removeBookmarkRecipe(Authentication authentication, @PathVariable Long recipeId) {
        Long userIdFromAuthToken = null;

        if (authentication != null) {
            userIdFromAuthToken = getUserIdFromAuthToken(authentication);
        }

        recipeService.removeBookmark(recipeId, userIdFromAuthToken);

        return ResponseEntity.ok("Bookmark removed successfully");
    }
}
