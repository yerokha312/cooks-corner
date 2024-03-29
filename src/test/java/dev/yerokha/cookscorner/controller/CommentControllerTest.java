package dev.yerokha.cookscorner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yerokha.cookscorner.dto.CreateCommentRequest;
import dev.yerokha.cookscorner.dto.LoginRequest;
import dev.yerokha.cookscorner.dto.UpdateCommentRequest;
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
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommentControllerTest {

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
    void addComment() throws Exception {
        Thread.sleep(1000);
        login(EMAIL, PASSWORD);
        CreateCommentRequest request = new CreateCommentRequest(
                1L,
                "Test comment",
                false
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/comments")
                        .header("Authorization", "Bearer " + accessToken)
                        .content(json)
                        .contentType(APP_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("Test comment"))
                .andExpect(jsonPath("$.parentCommentId").value(nullValue()))
                .andExpect(jsonPath("$.commentId").value(1L))
                .andExpect(jsonPath("$.author").value("Existing User"))
                .andExpect(jsonPath("$.authorId").value(1L));
    }

    @Test
    @Order(1)
    void addComment_Unauthorized() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest(
                1L,
                "Test comment",
                false
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/comments")
                        .content(json)
                        .contentType(APP_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    void addComment_Reply() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest(
                1L,
                "Reply comment",
                true
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/comments")
                        .header("Authorization", "Bearer " + accessToken)
                        .content(json)
                        .contentType(APP_JSON))
                .andExpect(jsonPath("$.text").value("Reply comment"))
                .andExpect(jsonPath("$.parentCommentId").value(1L))
                .andExpect(jsonPath("$.commentId").value(2L))
                .andExpect(jsonPath("$.author").value("Existing User"))
                .andExpect(jsonPath("$.authorId").value(1L));

    }

    @Test
    @Order(3)
    void addComment_Reply_Reply() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest(
                2L,
                "Reply for Reply comment",
                true
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/comments")
                        .header("Authorization", "Bearer " + accessToken)
                        .content(json)
                        .contentType(APP_JSON))
                .andExpect(jsonPath("$.text").value("Reply for Reply comment"))
                .andExpect(jsonPath("$.parentCommentId").value(2L))
                .andExpect(jsonPath("$.commentId").value(3L))
                .andExpect(jsonPath("$.author").value("Existing User"))
                .andExpect(jsonPath("$.authorId").value(1L));

    }

    @Test
    @Order(4)
    void getComments() throws Exception {
        mockMvc.perform(get("/v1/comments/1")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].commentId").value(1L))
                .andExpect(jsonPath("$.content[0].parentCommentId").value(nullValue()))
                .andExpect(jsonPath("$.content[0].authorId").value(1L))
                .andExpect(jsonPath("$.content[0].author").value("Existing User"))
                .andExpect(jsonPath("$.content[0].replyCount").value(1))
                .andExpect(jsonPath("$.content[0].likeCount").value(0))
                .andExpect(jsonPath("$.content[0].text").value("Test comment"))
                .andExpect(jsonPath("$.content[0].isLiked").value(false));
    }

    @Test
    @Order(4)
    void getComments_DeletedUser() throws Exception {
        mockMvc.perform(get("/v1/comments/1")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[2].commentId").value(11L))
                .andExpect(jsonPath("$.content[2].parentCommentId").value(nullValue()))
                .andExpect(jsonPath("$.content[2].authorId").value(nullValue()))
                .andExpect(jsonPath("$.content[2].author").value("Deleted User"))
                .andExpect(jsonPath("$.content[2].imageUrl").value(nullValue()))
                .andExpect(jsonPath("$.content[2].replyCount").value(0))
                .andExpect(jsonPath("$.content[2].likeCount").value(0))
                .andExpect(jsonPath("$.content[2].text").value("Comment of deleted user"))
                .andExpect(jsonPath("$.content[2].isLiked").value(false));
    }

    @Test
    @Order(5)
    void getReplies() throws Exception {
        mockMvc.perform(get("/v1/comments/1/replies")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].commentId").value(2L))
                .andExpect(jsonPath("$.content[0].parentCommentId").value(1L))
                .andExpect(jsonPath("$.content[0].authorId").value(1L))
                .andExpect(jsonPath("$.content[0].author").value("Existing User"))
                .andExpect(jsonPath("$.content[0].replyCount").value(1))
                .andExpect(jsonPath("$.content[0].likeCount").value(0))
                .andExpect(jsonPath("$.content[0].isLiked").value(false))
                .andExpect(jsonPath("$.content[0].text").value("Reply comment"));

    }

    @Test
    @Order(6)
    void getRecipe() throws Exception {
        mockMvc.perform(get("/v1/recipes/1"))
                .andExpect(jsonPath("$.recipeId").value(1))
                .andExpect(jsonPath("$.title").value("Test dish"))
                .andExpect(jsonPath("$.comments").value(4));
    }

    @Test
    @Order(7)
    void deleteComment() throws Exception {
        mockMvc.perform(delete("/v1/comments/1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Comment deleted"));
    }

    @Test
    @Order(8)
    void getReplies_AfterDeleted() throws Exception {
        mockMvc.perform(get("/v1/comments/1/replies")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].commentId").value(2L))
                .andExpect(jsonPath("$.content[0].parentCommentId").value(1L))
                .andExpect(jsonPath("$.content[0].authorId").value(1L))
                .andExpect(jsonPath("$.content[0].author").value("Existing User"))
                .andExpect(jsonPath("$.content[0].replyCount").value(1))
                .andExpect(jsonPath("$.content[0].likeCount").value(0))
                .andExpect(jsonPath("$.content[0].isLiked").value(false))
                .andExpect(jsonPath("$.content[0].text").value("Reply comment"));
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

    @Test
    @Order(9)
    void updateComment() throws Exception {
        UpdateCommentRequest request = new UpdateCommentRequest(
                3L,
                "Updated comment"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/v1/comments")
                        .content(json)
                        .contentType(APP_JSON)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(jsonPath("$.commentId").value(3L))
                .andExpect(jsonPath("$.isUpdated").value(true))
                .andExpect(jsonPath("$.text").value("Updated comment"));
    }

    @Test
    @Order(10)
    void getComments_AfterUpdate() throws Exception {
        mockMvc.perform(get("/v1/comments/2/replies")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].commentId").value(3L))
                .andExpect(jsonPath("$.content[0].parentCommentId").value(2L))
                .andExpect(jsonPath("$.content[0].authorId").value(1))
                .andExpect(jsonPath("$.content[0].author").value("Existing User"))
                .andExpect(jsonPath("$.content[0].text").value("Updated comment"))
                .andExpect(jsonPath("$.content[0].isUpdated").value(true))
                .andExpect(jsonPath("$.content[0].isLiked").value(false));
    }
}