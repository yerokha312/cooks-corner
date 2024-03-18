package dev.yerokha.cookscorner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yerokha.cookscorner.dto.LoginRequest;
import dev.yerokha.cookscorner.service.MailService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static dev.yerokha.cookscorner.controller.AuthenticationControllerTest.accessToken;
import static dev.yerokha.cookscorner.controller.AuthenticationControllerTest.extractToken;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ActionControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    MailService mailService;

    final String APP_JSON = "application/json";
    private static final String EMAIL = "existing@example.com";
    private static final String PASSWORD = "P@ssw0rd";

    @Test
    @Order(1)
    void like_Authorized() throws Exception {
        login(EMAIL, PASSWORD);

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(put("/v1/actions/1/2/1")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Action success"));
        }

    }

    @Test
    @Order(2)
    void like_Comment_Unauthorized() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(put("/v1/actions/1/1/10"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @Order(2)
    void like_Comment_Authorized() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(put("/v1/actions/1/1/10")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @Order(3)
    void getComments() throws Exception {
        mockMvc.perform(get("/v1/comments/1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].parentCommentId").value(nullValue()))
                .andExpect(jsonPath("$.content[0].authorId").value(1L))
                .andExpect(jsonPath("$.content[0].author").value("Existing User"))
                .andExpect(jsonPath("$.content[0].replyCount").value(0))
                .andExpect(jsonPath("$.content[0].likeCount").value(1))
                .andExpect(jsonPath("$.content[0].text").value("Some comment for tests"))
                .andExpect(jsonPath("$.content[0].isLiked").value(true));
    }

    @Test
    @Order(4)
    void dislikeComment() throws Exception {
        mockMvc.perform(put("/v1/actions/10/1/10")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Action success"));
    }

    @Test
    @Order(5)
    void getComments_Disliked() throws Exception {
        mockMvc.perform(get("/v1/comments/1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].parentCommentId").value(nullValue()))
                .andExpect(jsonPath("$.content[0].authorId").value(1L))
                .andExpect(jsonPath("$.content[0].author").value("Existing User"))
                .andExpect(jsonPath("$.content[0].replyCount").value(0))
                .andExpect(jsonPath("$.content[0].likeCount").value(0))
                .andExpect(jsonPath("$.content[0].text").value("Some comment for tests"))
                .andExpect(jsonPath("$.content[0].isLiked").value(false));
    }

    @Test
    @Order(6)
    void like_Unauthorized() throws Exception {
            mockMvc.perform(put("/v1/actions/1/2/1"))
                    .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(7)
    void getRecipeById_Authorized_Liked() throws Exception {
        mockMvc.perform(get("/v1/recipes/1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(jsonPath("$.title").value("Test dish"))
                .andExpect(jsonPath("$.likes").value(1))
                .andExpect(jsonPath("$.isLiked").value(true));
    }

    @Test
    @Order(8)
    void dislikeRecipe() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(put("/v1/actions/10/2/1")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Action success"));
        }
    }

    /*
    *
    private static final byte LIKE = 1;
    private static final byte DISLIKE = 10;
    private static final byte SAVE = 2;
    private static final byte REMOVE = 20;
    *
    private static final byte COMMENT = 1;
    private static final byte RECIPE = 2;
    * */
    @Test
    @Order(9)
    void getRecipeById_Authorized_Disliked() throws Exception {
        mockMvc.perform(get("/v1/recipes/1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(jsonPath("$.title").value("Test dish"))
                .andExpect(jsonPath("$.likes").value(0))
                .andExpect(jsonPath("$.isLiked").value(false));
    }

    @Test
    @Order(10)
    void bookmarkRecipe() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(put("/v1/actions/2/2/1")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Action success"));
        }
    }

    @Test
    @Order(10)
    void getRecipeById_Authorized_Bookmarked() throws Exception {
        mockMvc.perform(get("/v1/recipes/1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(content().string(containsString("Test dish")))
                .andExpect(jsonPath("$.bookmarks").value(1))
                .andExpect(jsonPath("$.isBookmarked").value(true));
    }

    @Test
    @Order(11)
    void removeBookmarkRecipe() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(put("/v1/actions/20/2/1")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Action success"));

        }
    }

    @Test
    @Order(12)
    void getRecipeById_Authorized_Bookmark_Removed() throws Exception {
        mockMvc.perform(get("/v1/recipes/1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(content().string(containsString("Test dish")))
                .andExpect(jsonPath("$.bookmarks").value(0))
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