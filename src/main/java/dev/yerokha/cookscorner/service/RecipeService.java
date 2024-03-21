package dev.yerokha.cookscorner.service;

import dev.yerokha.cookscorner.dto.CreateRecipeRequest;
import dev.yerokha.cookscorner.dto.Ingredient;
import dev.yerokha.cookscorner.dto.Recipe;
import dev.yerokha.cookscorner.dto.RecipeDto;
import dev.yerokha.cookscorner.dto.UpdateRecipeRequest;
import dev.yerokha.cookscorner.entity.Category;
import dev.yerokha.cookscorner.entity.CommentEntity;
import dev.yerokha.cookscorner.entity.IngredientEntity;
import dev.yerokha.cookscorner.entity.RecipeEntity;
import dev.yerokha.cookscorner.entity.RecipeIngredient;
import dev.yerokha.cookscorner.enums.Difficulty;
import dev.yerokha.cookscorner.exception.ForbiddenException;
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
import java.util.List;
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
    private final UserService userService;

    public RecipeService(RecipeRepository recipeRepository, CategoryRepository categoryRepository, UserRepository userRepository, ImageService imageService, IngredientRepository ingredientRepository, UserService userService) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.ingredientRepository = ingredientRepository;
        this.userService = userService;
    }

    @Transactional
    public void addRecipe(CreateRecipeRequest request, Long userIdFromAuthToken, MultipartFile image) {
        RecipeEntity entity = new RecipeEntity();
        entity.setUserEntity(userService.getUserEntityById(userIdFromAuthToken));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setCategory(getCategory(request.category()));
        setIngredientsToRecipe(entity, request.ingredients());
        entity.setImage(imageService.processImage(image));
        entity.setDifficulty(Difficulty.valueOf(request.difficulty().toUpperCase()));
        entity.setCookingTimeMinutes(request.cookingTimeMinutes());

        recipeRepository.save(entity);
    }

    private Category getCategory(String request) {
        return categoryRepository.
                findByCategoryNameIgnoreCase(request).orElseThrow(
                        () -> new NotFoundException("Category not found"));
    }

    public Recipe getRecipeById(Long recipeId, Long userIdFromAuthToken) {
        RecipeEntity entity = recipeRepository.findById(recipeId).orElseThrow(
                () -> new NotFoundException("Recipe not found"));

        return mapRecipe(userIdFromAuthToken, recipeId, entity);
    }

    private int getTotalComments(List<CommentEntity> comments) {
        int parentComments = comments.size();
        int replyComments = comments.stream()
                .mapToInt(comment -> comment.getReplies().size())
                .sum();
        return parentComments + replyComments;
    }

    @Transactional
    public void incrementViewCount(Long id) {
        recipeRepository.incrementViewCount(id);
    }

    public Page<RecipeDto> getRecipes(Map<String, String> params, Long userIdFromAuthToken) {
        Pageable pageable = getPageable(params);
        String query = params.get("query");

        if (query == null || query.isEmpty()) {
            return getPopularRecipes(userIdFromAuthToken, pageable);
        }

        query = query.toLowerCase();

        if (userIdFromAuthToken != null) {
            return switch (query) {
                case "my" -> getUsersRecipes(userIdFromAuthToken, pageable);
                case "saved" -> getSavedRecipes(userIdFromAuthToken, pageable);
                default -> getRecipesByQuery(userIdFromAuthToken, query, pageable);
            };
        }

        return getRecipesByQuery(null, query, pageable);
    }

    private static Pageable getPageable(Map<String, String> params) {
        return PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "12")),
                Sort.by(Sort.Direction.DESC, "viewCount"));
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

    public Page<RecipeDto> getByCategory(Long categoryId, Long userIdFromAuthToken, Map<String, String> params) {
        Pageable pageable = getPageable(params);
        return recipeRepository.findAllByCategoryCategoryId(categoryId, pageable).map(entity -> {
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


    public RecipeEntity getRecipeById(Long recipeId) {
        return recipeRepository.findById(recipeId).orElseThrow(
                () -> new NotFoundException("Recipe not found")
        );
    }

    public Page<RecipeDto> getUserRecipes(Long userId, Long userIdFromAuthToken, Map<String, String> params) {
        Pageable pageable = PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "12")));
        return recipeRepository.findByUserEntityUserId(userId, pageable).map(recipeEntity -> {
            Boolean isLiked = checkLiked(recipeEntity.getRecipeId(), userIdFromAuthToken);
            Boolean isBookmarked = checkBookmarked(recipeEntity.getRecipeId(), userIdFromAuthToken);
            return getRecipeDto(recipeEntity, isLiked, isBookmarked);
        });

    }

    public Recipe updateRecipe(Long userIdFromAuthToken, UpdateRecipeRequest request, MultipartFile image) {
        boolean exists = userRepository.existsById(userIdFromAuthToken);

        if (!exists) {
            throw new NotFoundException("User not found");
        }

        Long recipeId = request.recipeId();
        RecipeEntity recipe = getRecipeById(recipeId);
        if (!userIdFromAuthToken.equals(recipe.getUserEntity().getUserId())) {
            throw new ForbiddenException("User is not the author of this recipe");
        }

        if (image != null) {
            recipe.setImage(imageService.processImage(image));
        }

        recipe.setTitle(request.title());
        recipe.setCookingTimeMinutes(request.cookingTimeMinutes());
        recipe.setDescription(request.description());
        recipe.setCategory(getCategory(request.category()));
        recipe.setDifficulty(Difficulty.valueOf(request.difficulty().toUpperCase()));
        setIngredientsToRecipe(recipe, request.ingredients());
        recipe.setUpdatedAt(LocalDateTime.now());

        recipeRepository.save(recipe);

        return mapRecipe(userIdFromAuthToken, recipeId, recipe);
    }

    private Recipe mapRecipe(Long userIdFromAuthToken, Long recipeId, RecipeEntity recipe) {
        Boolean isLiked = checkLiked(recipeId, userIdFromAuthToken);
        Boolean isBookmarked = checkBookmarked(recipeId, userIdFromAuthToken);

        return new Recipe(
                recipe.getRecipeId(),
                recipe.getUpdatedAt() == null ? recipe.getCreatedAt() : recipe.getUpdatedAt(),
                recipe.getTitle(),
                recipe.getUserEntity().getName(),
                recipe.getUserEntity().getUserId(),
                recipe.getImage() == null ? null : recipe.getImage().getImageUrl(),
                recipe.getCookingTimeMinutes(),
                recipe.getDifficulty().name(),
                recipe.getDescription(),
                recipe.getLikes().size(),
                recipe.getBookmarks().size(),
                getTotalComments(recipe.getComments()),
                isLiked,
                isBookmarked,
                recipe.getRecipeIngredients().stream()
                        .map(ri -> new Ingredient(ri.getIngredientEntity().getIngredientName(),
                                ri.getAmount(),
                                ri.getMeasureUnit()))
                        .collect(Collectors.toSet())
        );
    }

    private void setIngredientsToRecipe(RecipeEntity recipe, Set<Ingredient> ingredients) {
        Set<RecipeIngredient> recipeIngredients = new HashSet<>();
        for (Ingredient ingredient : ingredients) {
            IngredientEntity ingredientEntity = ingredientRepository.
                    findByIngredientNameIgnoreCase(ingredient.ingredient())
                    .orElse(new IngredientEntity(ingredient.ingredient().toLowerCase()));

            RecipeIngredient recipeIngredient = new RecipeIngredient();
            recipeIngredient.setIngredientEntity(ingredientEntity);
            recipeIngredient.setAmount(ingredient.amount());
            recipeIngredient.setMeasureUnit(ingredient.measureUnit());
            recipeIngredient.setRecipeEntity(recipe);

            recipeIngredients.add(recipeIngredient);
        }
        recipe.setRecipeIngredients(recipeIngredients);
    }
}






























