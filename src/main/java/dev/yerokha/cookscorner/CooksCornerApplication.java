package dev.yerokha.cookscorner;

import dev.yerokha.cookscorner.entity.Category;
import dev.yerokha.cookscorner.entity.Role;
import dev.yerokha.cookscorner.entity.UserEntity;
import dev.yerokha.cookscorner.repository.CategoryRepository;
import dev.yerokha.cookscorner.repository.RoleRepository;
import dev.yerokha.cookscorner.repository.TokenRepository;
import dev.yerokha.cookscorner.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Set;

@SpringBootApplication
public class CooksCornerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CooksCornerApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(
            RoleRepository roleRepository,
            UserRepository userRepository,
            TokenRepository tokenRepository,
            CategoryRepository categoryRepository) {
        return args -> {
            tokenRepository.deleteAll();
            if (roleRepository.count() > 0) {
                return;
            }

            Role userRole = roleRepository.save(new Role("USER"));
            roleRepository.save(new Role("ADMIN"));

            userRepository.save(
                    new UserEntity(
                            "Test User",
                            "erbolatt@live.com",
                            "$2a$10$NOJ8zIC2mQoSM0ORuRBZH.j/GKMjI9dGQn1rcSTob3A0uo7qSkoBi",
                            true,
                            Set.of(userRole)
                    )
            );

            List<Category> categories = List.of(
                    new Category("breakfasts"),
                    new Category("soups"),
                    new Category("salads"),
                    new Category("main dishes"),
                    new Category("desserts"),
                    new Category("seafoods"),
                    new Category("beverages")
            );

            categoryRepository.saveAll(categories);
        };
    }

}
