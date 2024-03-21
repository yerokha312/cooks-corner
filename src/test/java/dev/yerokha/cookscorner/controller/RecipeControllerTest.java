package dev.yerokha.cookscorner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yerokha.cookscorner.dto.CreateRecipeRequest;
import dev.yerokha.cookscorner.dto.Ingredient;
import dev.yerokha.cookscorner.dto.LoginRequest;
import dev.yerokha.cookscorner.repository.RecipeRepository;
import dev.yerokha.cookscorner.service.ImageService;
import dev.yerokha.cookscorner.service.MailService;
import org.junit.jupiter.api.Assertions;
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

import static dev.yerokha.cookscorner.controller.AuthenticationControllerTest.accessToken;
import static dev.yerokha.cookscorner.controller.AuthenticationControllerTest.extractToken;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecipeControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    RecipeRepository recipeRepository;

    @MockBean
    MailService mailService;
    @MockBean
    ImageService imageService;

    final String APP_JSON = "application/json";

    private static final String EMAIL = "existing@example.com";
    private static final String PASSWORD = "P@ssw0rd";

    @Test
    @Order(1)
    void createRecipe() throws Exception {
        login(EMAIL, PASSWORD);

        CreateRecipeRequest recipe = new CreateRecipeRequest(
                "Spaghetti Carbonara",
                20,
                "MEDIUM",
                "A classic Italian pasta dish made with eggs, cheese, pancetta, and black pepper.",
                "main dishes",
                new HashSet<>(Arrays.asList(
                        new Ingredient("spaghetti", "200", "gram"),
                        new Ingredient("pancetta", "100", "gram"),
                        new Ingredient("parmesan cheese", "50", "gram"),
                        new Ingredient("eggs", "2", "pieces"),
                        new Ingredient("black pepper", "0", "to taste")
                ))
        );

        MockMultipartFile image = new MockMultipartFile(
                "image", "image.jpg", "image/jpeg", "image data".getBytes());

        byte[] recipeBytes = objectMapper.writeValueAsBytes(recipe);
        MockPart recipePart = new MockPart("dto", recipeBytes);

        mockMvc.perform(multipart("/v1/recipes")
                        .part(recipePart)
                        .file(image)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    void createRecipe_NotAuthorized() throws Exception {
        CreateRecipeRequest recipe = new CreateRecipeRequest(
                "Spaghetti Carbonara",
                20,
                "MEDIUM",
                "A classic Italian pasta dish made with eggs, cheese, pancetta, and black pepper.",
                "main dishes",
                new HashSet<>(Arrays.asList(
                        new Ingredient("spaghetti", "200", "gram"),
                        new Ingredient("pancetta", "100", "gram"),
                        new Ingredient("parmesan cheese", "50", "gram"),
                        new Ingredient("eggs", "2", "pieces"),
                        new Ingredient("black pepper", "0", "to taste")
                ))
        );

        MockMultipartFile image = new MockMultipartFile(
                "image", "image.jpg", "image/jpeg", "image data".getBytes());

        byte[] recipeBytes = objectMapper.writeValueAsBytes(recipe);
        MockPart recipePart = new MockPart("dto", recipeBytes);

        mockMvc.perform(multipart("/v1/recipes")
                        .part(recipePart)
                        .file(image))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    void getRecipesByCategory_NotAuthorized() throws Exception {
        mockMvc.perform(get("/v1/recipes/category/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].isLiked").value(nullValue()));
    }

    @Test
    @Order(3)
    void getRecipes_UnAuthorized() throws Exception {
        mockMvc.perform(get("/v1/recipes")
                        .param("query", "my"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Order(4)
    void getRecipesByCategory_Authorized() throws Exception {
        mockMvc.perform(get("/v1/recipes/category/4")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].isLiked").value(false));
    }

    @Test
    @Order(5)
    void getRecipeById() throws Exception {
        long initialViewCount = recipeRepository.getViewCount(2);
        mockMvc.perform(get("/v1/recipes/2"))
                .andExpect(content().string(containsString("classic")));

        long updatedViewCount = recipeRepository.getViewCount(2);

        Assertions.assertTrue(updatedViewCount > initialViewCount,
                "View count should be incremented after recipe was retrieved");

    }

    @Test
    @Order(5)
    void getRecipeById_Authorized() throws Exception {
        mockMvc.perform(get("/v1/recipes/2")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(content().string(containsString("classic")))
                .andExpect(jsonPath("$.isBookmarked").value(false));
    }

    public void login(String email, String password) throws Exception {
        LoginRequest request = new LoginRequest(
                email,
                password
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