package dev.yerokha.cookscorner.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yerokha.cookscorner.dto.CreateRecipeRequest;
import dev.yerokha.cookscorner.dto.Recipe;
import dev.yerokha.cookscorner.dto.RecipeDto;
import dev.yerokha.cookscorner.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Recipe", description = "Endpoints for recipe interaction")
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
    @Operation(
            summary = "Add recipe", description = "EP for creating a new recipe. Form-data request body is required",
            tags = {"recipe", "post"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Recipe created successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
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

        return new ResponseEntity<>("CreateRecipeRequest created successfully", HttpStatus.CREATED);
    }

    private void validateRequest(CreateRecipeRequest createRecipeRequest) {
        BindingResult bindingResult = new BeanPropertyBindingResult(createRecipeRequest, "createRecipeRequest");
        validator.validate(createRecipeRequest, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException("Invalid input " + bindingResult.getAllErrors());
        }
    }

    @Operation(
            summary = "Query recipes", description = "EP for querying recipes by \"query\" parameter. " +
            "Query parameter is optional",
            tags = {"recipe", "get"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Query success"),
            },
            parameters = {
                    @Parameter(name = "query", description = "Parameter to search by",
                            examples = {
                                    @ExampleObject(name = "pasta", value = "pasta"),
                                    @ExampleObject(name = "My", value = "my"),
                                    @ExampleObject(name = "Soups", value = "category:soups"),
                            }),
                    @Parameter(name = "page", description = "Page number", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "12")
            }
    )
    @GetMapping
    public ResponseEntity<Page<RecipeDto>> getRecipes(@RequestParam(required = false) Map<String, String> params, Authentication authentication) {
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

    @Operation(
            summary = "Get recipe", description = "Retrieve a recipe. If user is authenticated isLiked equals " +
            "true or false, if not authenticated - null",
            tags = {"recipe", "get"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recipe details"),
                    @ApiResponse(responseCode = "404", description = "Recipe not found", content = @Content)
            }
    )
    @GetMapping("/{recipeId}")
    public ResponseEntity<Recipe> getRecipeById(Authentication authentication, @PathVariable Long recipeId) {
        Long userIdFromAuthToken = null;

        if (authentication != null) {
            userIdFromAuthToken = getUserIdFromAuthToken(authentication);
        }

        return ResponseEntity.ok(recipeService.getRecipeById(recipeId, userIdFromAuthToken));
    }

    @Operation(
            summary = "Like recipe", description = "Authenticated user likes recipe by it's id",
            tags = {"recipe", "put"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recipe liked"),
                    @ApiResponse(responseCode = "404", description = "Recipe not found", content = @Content)
            }
    )
    @PutMapping("/like/{recipeId}")
    public ResponseEntity<String> likeRecipe(Authentication authentication, @PathVariable Long recipeId) {
        Long userIdFromAuthToken = null;

        if (authentication != null) {
            userIdFromAuthToken = getUserIdFromAuthToken(authentication);
        }

        recipeService.likeRecipe(recipeId, userIdFromAuthToken);

        return ResponseEntity.ok("Recipe liked successfully");
    }

    @Operation(
            summary = "Dislike recipe", description = "Authenticated user removes like from the recipe",
            tags = {"recipe", "put"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recipe disliked"),
                    @ApiResponse(responseCode = "404", description = "Recipe not found", content = @Content)
            }
    )
    @PutMapping("/dislike/{recipeId}")
    public ResponseEntity<String> dislikeRecipe(Authentication authentication, @PathVariable Long recipeId) {
        Long userIdFromAuthToken = null;

        if (authentication != null) {
            userIdFromAuthToken = getUserIdFromAuthToken(authentication);
        }

        recipeService.dislikeRecipe(recipeId, userIdFromAuthToken);

        return ResponseEntity.ok("Recipe disliked successfully");
    }

    @Operation(
            summary = "Bookmark recipe", description = "Authenticated user saves recipe to bookmarks",
            tags = {"recipe", "put"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recipe saved"),
                    @ApiResponse(responseCode = "404", description = "Recipe not found", content = @Content)
            }
    )
    @PutMapping("/bookmark/{recipeId}")
    public ResponseEntity<String> bookmarkRecipe(Authentication authentication, @PathVariable Long recipeId) {
        Long userIdFromAuthToken = null;

        if (authentication != null) {
            userIdFromAuthToken = getUserIdFromAuthToken(authentication);
        }

        recipeService.bookmarkRecipe(recipeId, userIdFromAuthToken);

        return ResponseEntity.ok("Recipe bookmarked successfully");
    }

    @Operation(
            summary = "Remove bookmark", description = "Authenticated user removes bookmark from the recipe",
            tags = {"recipe", "put"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recipe removed from bookmarks"),
                    @ApiResponse(responseCode = "404", description = "Recipe not found", content = @Content)
            }
    )
    @PutMapping("/remove-bookmark/{recipeId}")
    public ResponseEntity<String> removeBookmarkRecipe(Authentication authentication, @PathVariable Long recipeId) {
        Long userIdFromAuthToken = null;

        if (authentication != null) {
            userIdFromAuthToken = getUserIdFromAuthToken(authentication);
        }

        recipeService.removeBookmark(recipeId, userIdFromAuthToken);

        return ResponseEntity.ok("Bookmark removed successfully");
    }
}
