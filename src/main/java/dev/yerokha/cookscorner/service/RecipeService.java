package dev.yerokha.cookscorner.service;

import dev.yerokha.cookscorner.dto.CreateRecipeRequest;
import dev.yerokha.cookscorner.dto.Ingredient;
import dev.yerokha.cookscorner.dto.Recipe;
import dev.yerokha.cookscorner.dto.RecipeDto;
import dev.yerokha.cookscorner.entity.IngredientEntity;
import dev.yerokha.cookscorner.entity.RecipeEntity;
import dev.yerokha.cookscorner.entity.RecipeIngredient;
import dev.yerokha.cookscorner.entity.UserEntity;
import dev.yerokha.cookscorner.enums.Difficulty;
import dev.yerokha.cookscorner.exception.NotFoundException;
import dev.yerokha.cookscorner.repository.CategoryRepository;
import dev.yerokha.cookscorner.repository.IngredientRepository;
import dev.yerokha.cookscorner.repository.RecipeRepository;
import dev.yerokha.cookscorner.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.yerokha.cookscorner.mapper.RecipeMapper.toRecipeDto;
import static java.lang.Integer.parseInt;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final IngredientRepository ingredientRepository;

    public RecipeService(RecipeRepository recipeRepository, CategoryRepository categoryRepository, UserRepository userRepository, ImageService imageService, IngredientRepository ingredientRepository) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.ingredientRepository = ingredientRepository;
    }

    @Transactional
    public void addRecipe(CreateRecipeRequest createRecipeRequest, Long userIdFromAuthToken, MultipartFile image) {
        RecipeEntity entity = new RecipeEntity();
        entity.setCreatedAt(LocalDateTime.now());
        entity.setTitle(createRecipeRequest.title());
        entity.setImage(imageService.processImage(image));
        entity.setDescription(createRecipeRequest.description());
        entity.setCategory(categoryRepository.findByCategoryName(createRecipeRequest.category()).orElseThrow(
                () -> new NotFoundException("Category not found")));
        Set<RecipeIngredient> recipeIngredients = new HashSet<>();
        for (Ingredient ingredient : createRecipeRequest.ingredients()) {
            IngredientEntity ingredientEntity = ingredientRepository.findByIngredientNameIgnoreCase(ingredient.ingredient())
                    .orElse(new IngredientEntity(ingredient.ingredient().toLowerCase()));

            RecipeIngredient recipeIngredient = new RecipeIngredient();
            recipeIngredient.setIngredientEntity(ingredientEntity);
            recipeIngredient.setAmount(ingredient.amount());
            recipeIngredient.setMeasureUnit(ingredient.measureUnit());
            recipeIngredient.setRecipeEntity(entity);

            recipeIngredients.add(recipeIngredient);
        }
        entity.setRecipeIngredients(recipeIngredients);
        entity.setDifficulty(Difficulty.valueOf(createRecipeRequest.difficulty().toUpperCase()));
        entity.setCookingTimeMinutes(createRecipeRequest.cookingTimeMinutes());
        entity.setUserEntity(userRepository.findById(userIdFromAuthToken).orElseThrow(
                () -> new NotFoundException("User not found")));

        recipeRepository.save(entity);
    }

    public Recipe getRecipeById(Long recipeId, Long userIdFromAuthToken) {
        RecipeEntity entity = recipeRepository.findById(recipeId).orElseThrow(
                () -> new NotFoundException("CreateRecipeRequest not found"));

        Boolean isLiked = checkLiked(recipeId, userIdFromAuthToken);
        Boolean isBookmarked = checkBookmarked(recipeId, userIdFromAuthToken);

        return new Recipe(
                entity.getRecipeId(),
                entity.getTitle(),
                entity.getUserEntity().getName(),
                entity.getUserEntity().getUserId(),
                entity.getImage() == null ? null : entity.getImage().getImageUrl(),
                entity.getCookingTimeMinutes(),
                entity.getDifficulty().name(),
                entity.getDescription(),
                entity.getLikes().size(),
                entity.getBookmarks().size(),
                isLiked,
                isBookmarked,
                entity.getRecipeIngredients().stream()
                        .map(ri -> new Ingredient(ri.getIngredientEntity().getIngredientName(),
                                ri.getAmount(),
                                ri.getMeasureUnit()))
                        .collect(Collectors.toSet())
        );
    }

    public Page<RecipeDto> getRecipes(Map<String, String> params, Long userIdFromAuthToken) {
        Pageable pageable = PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "12")),
                Sort.by(Sort.Direction.DESC, "viewCount"));

        String query = params.get("query");

        if (query == null || query.isEmpty()) {
            return getPopularRecipes(userIdFromAuthToken, pageable);
        }

        query = query.toLowerCase();

        if (query.startsWith("category:")) {
            String categoryName = query.substring("category:".length());
            return getByCategory(userIdFromAuthToken, categoryName, pageable);
        }

        if (userIdFromAuthToken != null) {
            return switch (query) {
                case "my" -> getUsersRecipes(userIdFromAuthToken, pageable);
                case "saved" -> getSavedRecipes(userIdFromAuthToken, pageable);
                default -> getRecipesByQuery(userIdFromAuthToken, query, pageable);
            };
        }

        return getRecipesByQuery(null, query, pageable);
    }

    private Page<RecipeDto> getRecipesByQuery(Long userIdFromAuthToken, String query, Pageable pageable) {
        return recipeRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                query, query, pageable).map(entity -> {
            Boolean isLiked = checkLiked(entity.getRecipeId(), userIdFromAuthToken);
            Boolean isBookmarked = checkBookmarked(entity.getRecipeId(), userIdFromAuthToken);
            return getRecipeDto(entity, isLiked, isBookmarked);
        });

    }

    private Page<RecipeDto> getSavedRecipes(Long userIdFromAuthToken, Pageable pageable) {
        return recipeRepository.findByBookmarksUserId(userIdFromAuthToken, pageable).map(entity -> {
            Boolean isLiked = checkLiked(entity.getRecipeId(), userIdFromAuthToken);
            Boolean isBookmarked = true;
            return getRecipeDto(entity, isLiked, isBookmarked);
        });
    }

    private Page<RecipeDto> getUsersRecipes(Long userIdFromAuthToken, Pageable pageable) {
        return recipeRepository.findByUserEntityUserId(userIdFromAuthToken, pageable).map(entity -> {
            Boolean isLiked = checkLiked(entity.getRecipeId(), userIdFromAuthToken);
            Boolean isBookmarked = checkBookmarked(entity.getRecipeId(), userIdFromAuthToken);
            return getRecipeDto(entity, isLiked, isBookmarked);
        });
    }

    private Page<RecipeDto> getPopularRecipes(Long userIdFromAuthToken, Pageable pageable) {
        return recipeRepository.findAll(pageable).map(entity -> {
            Boolean isLiked = checkLiked(entity.getRecipeId(), userIdFromAuthToken);
            Boolean isBookmarked = checkBookmarked(entity.getRecipeId(), userIdFromAuthToken);
            return getRecipeDto(entity, isLiked, isBookmarked);
        });
    }

    private Page<RecipeDto> getByCategory(Long userIdFromAuthToken, String categoryName, Pageable pageable) {
        return recipeRepository.findByCategory_CategoryName(categoryName, pageable).map(entity -> {
            Boolean isLiked = checkLiked(entity.getRecipeId(), userIdFromAuthToken);
            Boolean isBookmarked = checkBookmarked(entity.getRecipeId(), userIdFromAuthToken);
            return getRecipeDto(entity, isLiked, isBookmarked);
        });
    }

    private RecipeDto getRecipeDto(RecipeEntity entity, Boolean isLiked, Boolean isBookmarked) {
        RecipeDto dto = toRecipeDto(entity);
        dto.setIsLiked(isLiked);
        dto.setIsBookmarked(isBookmarked);
        return dto;
    }

    private Boolean checkBookmarked(Long recipeId, Long userIdFromAuthToken) {
        if (userIdFromAuthToken == null) {
            return null;
        }

        Boolean isBookmarked;
        isBookmarked = userRepository.existsByUserIdAndBookmarkedRecipes_RecipeId(userIdFromAuthToken, recipeId);
        return isBookmarked;
    }

    private Boolean checkLiked(Long recipeId, Long userIdFromAuthToken) {
        if (userIdFromAuthToken == null) {
            return null;
        }

        Boolean isLiked;
        isLiked = userRepository.existsByUserIdAndLikedRecipes_RecipeId(userIdFromAuthToken, recipeId);
        return isLiked;
    }

    public void likeRecipe(Long recipeId, Long userIdFromAuthToken) {
        UserEntity user = getUserEntity(userIdFromAuthToken);

        RecipeEntity recipe = getRecipeById(recipeId);

        Set<RecipeEntity> likedRecipes = user.getLikedRecipes();
        Set<UserEntity> likedUsers = recipe.getLikes();

        likedRecipes.add(recipe);
        user.setLikedRecipes(likedRecipes);

        likedUsers.add(user);
        recipe.setLikes(likedUsers);

        userRepository.save(user);
        recipeRepository.save(recipe);
    }

    public void dislikeRecipe(Long recipeId, Long userIdFromAuthToken) {
        UserEntity user = getUserEntity(userIdFromAuthToken);

        RecipeEntity recipe = getRecipeById(recipeId);

        Set<RecipeEntity> likedRecipes = user.getLikedRecipes();
        Set<UserEntity> likedUsers = recipe.getLikes();

        likedRecipes.remove(recipe);
        user.setLikedRecipes(likedRecipes);

        likedUsers.remove(user);
        recipe.setLikes(likedUsers);

        userRepository.save(user);
        recipeRepository.save(recipe);
    }

    public void bookmarkRecipe(Long recipeId, Long userIdFromAuthToken) {
        UserEntity user = getUserEntity(userIdFromAuthToken);

        RecipeEntity recipe = getRecipeById(recipeId);

        Set<RecipeEntity> bookmarkedRecipes = user.getBookmarkedRecipes();
        Set<UserEntity> bookmarkedUsers = recipe.getBookmarks();

        bookmarkedRecipes.add(recipe);
        user.setBookmarkedRecipes(bookmarkedRecipes);

        bookmarkedUsers.add(user);
        recipe.setBookmarks(bookmarkedUsers);

        userRepository.save(user);
        recipeRepository.save(recipe);
    }

    public void removeBookmark(Long recipeId, Long userIdFromAuthToken) {
        UserEntity user = getUserEntity(userIdFromAuthToken);

        RecipeEntity recipe = getRecipeById(recipeId);

        Set<RecipeEntity> bookmarkedRecipes = user.getBookmarkedRecipes();
        Set<UserEntity> bookmarkedUsers = recipe.getBookmarks();

        bookmarkedRecipes.remove(recipe);
        user.setBookmarkedRecipes(bookmarkedRecipes);

        bookmarkedUsers.remove(user);
        recipe.setBookmarks(bookmarkedUsers);

        userRepository.save(user);
        recipeRepository.save(recipe);
    }

    private RecipeEntity getRecipeById(Long recipeId) {
        return recipeRepository.findById(recipeId).orElseThrow(
                () -> new NotFoundException("Recipe not found")
        );
    }

    private UserEntity getUserEntity(Long userIdFromAuthToken) {
        return userRepository.findById(userIdFromAuthToken).orElseThrow(
                () -> new NotFoundException("UserNotFound")
        );
    }
}






























