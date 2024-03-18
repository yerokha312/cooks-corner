package dev.yerokha.cookscorner.service;

import dev.yerokha.cookscorner.entity.CommentEntity;
import dev.yerokha.cookscorner.entity.RecipeEntity;
import dev.yerokha.cookscorner.entity.UserEntity;
import dev.yerokha.cookscorner.repository.CommentRepository;
import dev.yerokha.cookscorner.repository.RecipeRepository;
import dev.yerokha.cookscorner.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ActionService {

    private final RecipeRepository recipeRepository;
    private final RecipeService recipeService;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;

    private static final byte LIKE = 1;
    private static final byte DISLIKE = 10;
    private static final byte SAVE = 2;
    private static final byte REMOVE = 20;
    private static final byte COMMENT = 1;
    private static final byte RECIPE = 2;

    public ActionService(RecipeRepository recipeRepository, RecipeService recipeService, UserRepository userRepository, CommentRepository commentRepository, UserService userService) {
        this.recipeRepository = recipeRepository;
        this.recipeService = recipeService;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.userService = userService;
    }

    public void interact(byte actionId, byte objectTypeId, Long objectId, Long userIdFromAuthToken) {
        switch (actionId) {
            case LIKE -> likeObject(objectTypeId, objectId, userIdFromAuthToken);
            case DISLIKE -> dislikeObject(objectTypeId, objectId, userIdFromAuthToken);
            case SAVE -> saveRecipe(objectId, userIdFromAuthToken);
            case REMOVE -> removeRecipe(objectId, userIdFromAuthToken);
            default -> throw new IllegalArgumentException("Invalid action id");
        }
    }

    private void dislikeObject(byte objectTypeId, Long objectId, Long userIdFromAuthToken) {
        switch (objectTypeId) {
            case COMMENT -> dislikeComment(objectId, userIdFromAuthToken);
            case RECIPE -> dislikeRecipe(objectId, userIdFromAuthToken);
            default -> throw new IllegalArgumentException("Invalid object type id");
        }
    }

    private void likeObject(byte objectTypeId, Long objectId, Long userIdFromAuthToken) {
        switch (objectTypeId) {
            case COMMENT -> likeComment(objectId, userIdFromAuthToken);
            case RECIPE -> likeRecipe(objectId, userIdFromAuthToken);
            default -> throw new IllegalArgumentException("Invalid object id");
        }
    }

    private void likeComment(Long objectId, Long userIdFromAuthToken) {
        UserEntity user = userService.getUserEntityById(userIdFromAuthToken);

        CommentEntity comment = commentRepository.getReferenceById(objectId);

        Set<UserEntity> likedUsers = comment.getLikes();

        likedUsers.add(user);

        comment.setLikes(likedUsers);

        commentRepository.save(comment);
    }

    private void dislikeComment(Long objectId, Long userIdFromAuthToken) {
        UserEntity user = userService.getUserEntityById(userIdFromAuthToken);

        CommentEntity comment = commentRepository.getReferenceById(objectId);

        Set<UserEntity> likedUsers = comment.getLikes();

        likedUsers.remove(user);

        comment.setLikes(likedUsers);

        commentRepository.save(comment);
    }

    public void likeRecipe(Long recipeId, Long userIdFromAuthToken) {
        UserEntity user = userService.getUserEntityById(userIdFromAuthToken);

        RecipeEntity recipe = recipeService.getRecipeById(recipeId);

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
        UserEntity user = userService.getUserEntityById(userIdFromAuthToken);

        RecipeEntity recipe = recipeService.getRecipeById(recipeId);

        Set<RecipeEntity> likedRecipes = user.getLikedRecipes();
        Set<UserEntity> likedUsers = recipe.getLikes();

        likedRecipes.remove(recipe);
        user.setLikedRecipes(likedRecipes);

        likedUsers.remove(user);
        recipe.setLikes(likedUsers);

        userRepository.save(user);
        recipeRepository.save(recipe);
    }

    public void saveRecipe(Long recipeId, Long userIdFromAuthToken) {
        UserEntity user = userService.getUserEntityById(userIdFromAuthToken);

        RecipeEntity recipe = recipeService.getRecipeById(recipeId);

        Set<RecipeEntity> bookmarkedRecipes = user.getBookmarkedRecipes();
        Set<UserEntity> bookmarkedUsers = recipe.getBookmarks();

        bookmarkedRecipes.add(recipe);
        user.setBookmarkedRecipes(bookmarkedRecipes);

        bookmarkedUsers.add(user);
        recipe.setBookmarks(bookmarkedUsers);

        userRepository.save(user);
        recipeRepository.save(recipe);
    }

    public void removeRecipe(Long recipeId, Long userIdFromAuthToken) {
        UserEntity user = userService.getUserEntityById(userIdFromAuthToken);

        RecipeEntity recipe = recipeService.getRecipeById(recipeId);

        Set<RecipeEntity> bookmarkedRecipes = user.getBookmarkedRecipes();
        Set<UserEntity> bookmarkedUsers = recipe.getBookmarks();

        bookmarkedRecipes.remove(recipe);
        user.setBookmarkedRecipes(bookmarkedRecipes);

        bookmarkedUsers.remove(user);
        recipe.setBookmarks(bookmarkedUsers);

        userRepository.save(user);
        recipeRepository.save(recipe);
    }
}
