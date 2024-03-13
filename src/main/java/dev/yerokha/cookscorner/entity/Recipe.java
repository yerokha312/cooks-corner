package dev.yerokha.cookscorner.entity;

import dev.yerokha.cookscorner.enums.Difficulty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "recipe")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_id")
    private Long recipeId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "title", nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "image_id")
    private Image image;

    @Column(name = "description", length = 1000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "recipe")
    private Set<RecipeIngredient> recipeIngredients;

    @Column(name = "difficulty", nullable = false)
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Column
    private int cookingTimeMinutes;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "recipe_like_junction",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> likes;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "recipe_bookmark_junction",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> bookmarks;

    @Column(name = "view_count")
    private Long viewCount;

}
