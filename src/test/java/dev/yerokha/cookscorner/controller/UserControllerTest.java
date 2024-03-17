package dev.yerokha.cookscorner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yerokha.cookscorner.dto.LoginRequest;
import dev.yerokha.cookscorner.dto.UpdateProfileRequest;
import dev.yerokha.cookscorner.repository.UserRepository;
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

import static dev.yerokha.cookscorner.controller.AuthenticationControllerTest.accessToken;
import static dev.yerokha.cookscorner.controller.AuthenticationControllerTest.extractToken;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    UserRepository userRepository;
    @MockBean
    MailService mailService;
    @MockBean
    ImageService imageService;

    final String APP_JSON = "application/json";
    private static final String EMAIL = "existing@example.com";
    private static final String PASSWORD = "P@ssw0rd";

    @Test
    @Order(1)
    void showProfile_NotAuthorized() throws Exception {
        long initialViewCount = userRepository.getViewCount(1L);

        mockMvc.perform(get("/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowed").value(nullValue()))
                .andExpect(jsonPath("$.followers").value(0))
                .andExpect(jsonPath("$.following").value(0));

        long updatedViewCount = userRepository.getViewCount(1L);

        Assertions.assertTrue(updatedViewCount > initialViewCount,
                "View count should be incremented after user was retrieved");
    }

    @Test
    @Order(2)
    void showProfile_Authorized() throws Exception {
        login(EMAIL, PASSWORD);
        mockMvc.perform(get("/v1/users/2")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowed").value(false))
                .andExpect(jsonPath("$.followers").value(0))
                .andExpect(jsonPath("$.following").value(0));
    }

    @Test
    @Order(3)
    void showProfile_Authorized_Self() throws Exception {
        mockMvc.perform(get("/v1/users/1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowed").value(nullValue()))
                .andExpect(jsonPath("$.followers").value(0))
                .andExpect(jsonPath("$.following").value(0));
    }

    @Test
    @Order(4)
    void follow() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/v1/users/follow/2")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(content().string("You followed the user"));
        }
    }

    @Test
    @Order(5)
    void follow_Unauthorized() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/v1/users/follow/2"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @Order(6)
    void showProfile_Authorized_Followers() throws Exception {
        mockMvc.perform(get("/v1/users/2")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowed").value(true))
                .andExpect(jsonPath("$.followers").value(1))
                .andExpect(jsonPath("$.following").value(0));
    }

    @Test
    @Order(7)
    void showProfile_Authorized_Following() throws Exception {
        mockMvc.perform(get("/v1/users/1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowed").value(nullValue()))
                .andExpect(jsonPath("$.followers").value(0))
                .andExpect(jsonPath("$.following").value(1));
    }

    @Test
    @Order(8)
    void unfollow() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/v1/users/unfollow/2")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(content().string("You unfollowed the user"));
        }
    }

    @Test
    @Order(9)
    void showProfile_Authorized_Unfollowed() throws Exception {
        mockMvc.perform(get("/v1/users/2")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowed").value(false))
                .andExpect(jsonPath("$.followers").value(0))
                .andExpect(jsonPath("$.following").value(0));
    }

    @Test
    @Order(9)
    void showProfile_Authorized_Self_Unfollowed() throws Exception {
        mockMvc.perform(get("/v1/users/1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowed").value(nullValue()))
                .andExpect(jsonPath("$.followers").value(0))
                .andExpect(jsonPath("$.following").value(0));
    }

    @Test
    @Order(10)
    void updateProfile() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
                1L,
                "The First Test User",
                "I am the star!"
        );

        MockMultipartFile image = new MockMultipartFile(
                "image", "image.jpg", "image/jpeg", "image data".getBytes());

        MockPart part = new MockPart("dto", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart(PUT, "/v1/users")
                        .file(image)
                        .part(part)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(11)
    void showProfile() throws Exception {
        mockMvc.perform(get("/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("The First Test User"))
                .andExpect(jsonPath("$.isFollowed").value(nullValue()))
                .andExpect(jsonPath("$.followers").value(0))
                .andExpect(jsonPath("$.following").value(0));
    }

    @Test
    @Order(12)
    void search() throws Exception {
        mockMvc.perform(get("/v1/users/search")
                        .param("query", "first"))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("The First Test User"));
    }

    @Test
    @Order(13)
    void showUserRecipes() throws Exception {
        mockMvc.perform(get("/v1/users/recipes/2")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].isLiked").value(false))
                .andExpect(jsonPath("$.content[0].title").value("Test dish"));

    }

    @Test
    @Order(14)
    void follow_Order_13() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/v1/users/follow/2")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(content().string("You followed the user"));
        }
    }

    @Test
    @Order(15)
    void showFollowers() throws Exception {
        mockMvc.perform(get("/v1/users/2/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("The First Test User"));
    }

    @Test
    @Order(16)
    void showFollowing() throws Exception {
        mockMvc.perform(get("/v1/users/1/following"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Second User"));
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