package dev.yerokha.cookscorner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "bio", length = 500)
    private String bio;

    @ManyToOne
    @JoinColumn(name = "image_id")
    private Image profilePicture;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @OneToMany(mappedBy = "userEntity")
    private Set<RecipeEntity> recipeEntities = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "following",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "following_id")}
    )
    private Set<UserEntity> following = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "followers",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "follower_id")}
    )
    private Set<UserEntity> followers = new HashSet<>();

    @ManyToMany(mappedBy = "bookmarks")
    private Set<RecipeEntity> bookmarkedRecipes = new HashSet<>();

    @ManyToMany(mappedBy = "likes")
    private Set<RecipeEntity> likedRecipes = new HashSet<>();

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "is_enabled")
    private boolean isEnabled;

    @ManyToMany
    @JoinTable(
            name = "user_role_junction",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> authorities;

    public UserEntity() {
    }

    public UserEntity(String name, String email, String password, Set<Role> authorities) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.registeredAt = LocalDateTime.now();
        this.authorities = authorities;
    }

    public UserEntity(String name, String email, String password, boolean isEnabled, Set<Role> authorities) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.registeredAt = LocalDateTime.now();
        this.isEnabled = isEnabled;
        this.authorities = authorities;
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", bio='" + bio + '\'' +
                ", profilePicture=" + profilePicture +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
//                ", recipeEntities=" + recipeEntities.size() +
//                ", following=" + following.size() +
//                ", followers=" + followers.size() +
                ", registeredAt=" + registeredAt +
                ", isEnabled=" + isEnabled +
                ", authorities=" + authorities +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return
                Objects.equals(userId, that.userId) &&
                        Objects.equals(email, that.email) &&
                        Objects.equals(registeredAt, that.registeredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email, registeredAt);
    }
}

