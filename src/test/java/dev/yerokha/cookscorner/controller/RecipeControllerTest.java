package dev.yerokha.cookscorner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yerokha.cookscorner.dto.CreateRecipeRequest;
import dev.yerokha.cookscorner.dto.Ingredient;
import dev.yerokha.cookscorner.dto.LoginRequest;
import dev.yerokha.cookscorner.service.ImageService;
import dev.yerokha.cookscorner.service.MailService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.HashSet;

import static dev.yerokha.cookscorner.controller.AuthenticationControllerTest.extractToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecipeControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    MailService mailService;
    @MockBean
    ImageService imageService;

    private static String accessToken;
    final String APP_JSON = "application/json";

    @Test
    @Order(1)
    void createRecipe() throws Exception {
        login();

        CreateRecipeRequest recipe = new CreateRecipeRequest(
                "Spaghetti Carbonara",
                20,
                "MEDIUM",
                "A classic Italian pasta dish made with eggs, cheese, pancetta, and black pepper.",
                "main dishes",
                new HashSet<>(Arrays.asList(
                        new Ingredient("spaghetti", 200, "gram"),
                        new Ingredient("pancetta", 100, "gram"),
                        new Ingredient("parmesan cheese", 50, "gram"),
                        new Ingredient("eggs", 2, "pieces"),
                        new Ingredient("black pepper", 0, "to taste")
                ))
        );

        MockMultipartFile image = new MockMultipartFile(
                "image", "image.jpg", "image/jpeg", "image data".getBytes());

        String json = objectMapper.writeValueAsString(recipe);
        byte[] recipeBytes = objectMapper.writeValueAsBytes(recipe);
        MockPart recipePart = new MockPart("dto",recipeBytes);

        mockMvc.perform(multipart("/v1/recipes")
                        .part(recipePart)
                        .file(image)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated());
    }

    @Test
    void getRecipes() {
    }

    @Test
    void getRecipeById() {
    }

    @Test
    void likeRecipe() {
    }

    @Test
    void dislikeRecipe() {
    }

    @Test
    void bookmarkRecipe() {
    }

    @Test
    void removeBookmarkRecipe() {
    }

    private void login() throws Exception {
        LoginRequest request = new LoginRequest(
                "existing@example.com",
                "P@ssw0rd"
        );

        String json = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/v1/auth/login")
                        .content(json)
                        .contentType(APP_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        accessToken = extractToken(responseContent, "accessToken");
    }
}