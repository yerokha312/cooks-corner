package dev.yerokha.cookscorner.entity;

import dev.yerokha.cookscorner.enums.Difficulty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "recipe")
public class RecipeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_id")
    private Long recipeId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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

    @OneToMany(mappedBy = "recipeEntity", cascade = CascadeType.ALL)
    private Set<RecipeIngredient> recipeIngredients;

    @Column(name = "difficulty", nullable = false)
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Column
    private int cookingTimeMinutes;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @ManyToMany
    @JoinTable(
            name = "user_recipe_likes",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> likes;

    @ManyToMany
    @JoinTable(
            name = "user_recipe_bookmarks",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> bookmarks;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "recipeEntity")
    private List<CommentEntity> comments = new ArrayList<>();

    @Column(name = "view_count")
    private long viewCount;

    @Override
    public String toString() {
        return "RecipeEntity{" +
                "recipeId=" + recipeId +
                ", createdAt=" + createdAt +
                ", title='" + title + '\'' +
                ", image=" + image +
                ", description='" + description + '\'' +
                ", category=" + category +
                ", recipeIngredients=" + recipeIngredients.size() +
                ", difficulty=" + difficulty +
                ", cookingTimeMinutes=" + cookingTimeMinutes +
                ", userEntity=" + userEntity +
                ", likes=" + likes.size() +
                ", bookmarks=" + bookmarks.size() +
                ", viewCount=" + viewCount +
                '}';
    }
}
