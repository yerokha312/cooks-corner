package dev.yerokha.cookscorner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yerokha.cookscorner.dto.AccountRecoveryRequest;
import dev.yerokha.cookscorner.dto.LoginRequest;
import dev.yerokha.cookscorner.dto.RegistrationRequest;
import dev.yerokha.cookscorner.dto.ResetPasswordRequest;
import dev.yerokha.cookscorner.dto.SendEmailRequest;
import dev.yerokha.cookscorner.entity.UserEntity;
import dev.yerokha.cookscorner.repository.UserRepository;
import dev.yerokha.cookscorner.service.MailService;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthenticationControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    PasswordEncoder passwordEncoder;
    @MockBean
    MailService mailService;
    @Autowired
    UserRepository userRepository;
    final String APP_JSON = "application/json";
    private static String confirmationUrl;
    private static String resetPasswordUrl;
    private static String recoveryUrl;
    private static String initialAccessToken;
    public static String accessToken;
    private static String refreshToken;

    @Test
    @Order(1)
    void registerCustomer() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "John Doe",
                "johndoe@example.com",
                "P@ssw0rd",
                "http://localhost:8080/v1/auth/confirmation");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/auth/registration")
                        .contentType(APP_JSON)
                        .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("johndoe@example.com")));

        ArgumentCaptor<String> confirmationUrlCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mailService).sendEmailConfirmation(
                eq("johndoe@example.com"),
                confirmationUrlCaptor.capture(),
                eq("John Doe")
        );

        confirmationUrl = confirmationUrlCaptor.getValue();
    }

    @Test
    @Order(2)
    void login_NotEnabled() throws Exception {
        LoginRequest request = new LoginRequest(
                "johndoe@example.com",
                "P@ssw0rd"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/auth/login")
                        .content(json)
                        .contentType(APP_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    void login_isDeleted() throws Exception {
        LoginRequest request = new LoginRequest(
                "deleted@example.com",
                "P@ssw0rd"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/auth/login")
                        .content(json)
                        .contentType(APP_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User has been deleted"));
    }

    @Test
    @Order(2)
    void registerCustomer_BadRequest_Name() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "John Doe$",
                "johndoe2@example.com",
                "P@ssw0rd",
                "http://localhost:8080/v1/auth/confirmation");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/auth/registration")
                        .contentType(APP_JSON)
                        .content(json)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    void registerCustomer_BadRequest_Password() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "John Doe",
                "johndoe3@example.com",
                "password",
                "http://localhost:8080/v1/auth/confirmation");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/auth/registration")
                        .contentType(APP_JSON)
                        .content(json)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    void confirmEmail() throws Exception {
        mockMvc.perform(put(confirmationUrl))
                .andExpect(status().isOk())
                .andExpect(content().string("Email is confirmed"));
    }

    @Test
    @Order(5)
    void confirmEmail_InvalidToken() throws Exception {
        mockMvc.perform(put("http://localhost:8080/v1/auth/confirmation?ct=sNaG9_s2FY8yv2I--YVbkrD2Qw0QfzubLRpD1sXA4gTYAg9T_JSnEnjtXXyeHeEBaTjL65lodulIB5L8XIMi49nXzCw6-ModEfaUiAD7YsCRlTqQI9BQQrQBxeT2SuAzCykA-iaEdPDT5o6b3y8TrIXUUWsRc3UvhuzibQLLoq0Y6StCkKV0DRZjNL4Dbe07_8CL6B-zchLAeOHGczyYqDxJcB0sb1nfgiKfSffCJXot_MF6gitrKhaXvpSitvaFb-wh6hjVBIR9yJ5QJHir_6dw0T2GK7SloeaLCPIDFRPoneRlcv7Gvum4DcPUU7-5ypBb1GGhPoNxkRcP59NtGV_jF0Z9iMWIpjDkCXClKlyKZyAll7CStz_BjgdDQusR9ADuU3QyV0wFwdqi8WwdwfaAmtz-X8k7R1a1qW85BIa_ZO8E9J6nPg8V67v4yGv4eJC5R6sNeD4VGKtYze2G-8inviBaGOaQNvyQvVyufyPp-nAvuX3Bv6hsfp1McgWpjY7M4sorbLKJcCMEDT5mykPHZ0X3w0N5P4oyMnwtz-rvD6nl59SESBz2XnzkNW7NS7Pq5leEhJUD2Ad-j-eKtUoDoL0VgdygI58KGtOR7FHBfrwf-arrwBfi7s7PIKzxSU6Qul0u3PIoXxWDD6bxJKeNAVmGKqcCmoweNZaa52DG0ly1nungPDdPcMRJpwwSe7uIoC0AcMkR-rfmqCBvyA"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Signed JWT rejected: Invalid signature"));
    }

    @Test
    @Order(6)
    void checkAvailable_True() throws Exception {
        mockMvc.perform(post("/v1/auth/email-available")
                .contentType(APP_JSON)
                .content("available@example.com"))
                .andExpect(content().string("true"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    void checkAvailable_False() throws Exception {
        mockMvc.perform(post("/v1/auth/email-available")
                .contentType(APP_JSON)
                .content("existing@example.com"))
                .andExpect(content().string("false"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(8)
    void resend() throws Exception {
        SendEmailRequest request = new SendEmailRequest(
                "unconfirmed@example.com",
                "http://localhost:8080/v1/auth/confirmation");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/auth/resend-confirmation")
                        .contentType(APP_JSON)
                        .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("unconfirmed@example.com")));

        ArgumentCaptor<String> confirmationUrlCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mailService).sendEmailConfirmation(
                eq("unconfirmed@example.com"),
                confirmationUrlCaptor.capture(),
                eq("Unconfirmed User")
        );

        confirmationUrl = confirmationUrlCaptor.getValue();
    }

    @Test
    @Order(9)
    void confirmEmail_Resend() throws Exception {
        mockMvc.perform(put(confirmationUrl))
                .andExpect(status().isOk())
                .andExpect(content().string("Email is confirmed"));
    }

    @Test
    @Order(10)
    void login() throws Exception{
        LoginRequest request = new LoginRequest(
                "johndoe@example.com",
                "P@ssw0rd"
        );

        String json = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/v1/auth/login")
                .content(json)
                .contentType(APP_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        accessToken = extractToken(responseContent, "accessToken");
        initialAccessToken = accessToken;
        refreshToken = extractToken(responseContent, "refreshToken");
    }

    @Test
    @Order(11)
    void login_NotFound() throws Exception{
        LoginRequest request = new LoginRequest(
                "unexisting@example.com",
                "P@ssw0rd"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/auth/login")
                .content(json)
                .contentType(APP_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid username or password"));
    }

    @Test
    @Order(12)
    void login_InvalidPassword() throws Exception{
        LoginRequest request = new LoginRequest(
                "johndoe@example.com",
                "P@ssw0rd1"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/auth/login")
                .content(json)
                .contentType(APP_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid username or password"));
    }

    @Test
    @Order(13)
    void refreshToken() throws Exception {
        Thread.sleep(1000);

        MvcResult result = mockMvc.perform(post("/v1/auth/refresh-token")
                .contentType(APP_JSON)
                .content("Bearer " + refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andReturn();

        String newAccessToken = extractToken(result.getResponse().getContentAsString(),
                "accessToken");

        Assertions.assertNotEquals(accessToken, newAccessToken,
                "New access token should be different from the initial one");

        accessToken = newAccessToken;
    }

    @Test
    @Order(14)
    void refreshToken_InvalidToken() throws Exception {
        mockMvc.perform(post("/v1/auth/refresh-token")
                .contentType(APP_JSON)
                .content("Bearer " + "eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJzZWxmIiwic3ViIjoiZXJib2xhdHRAbGl2ZS5jb20iLCJzY29wZXMiOiJVU0VSIiwiZXhwIjoxNzEwODQ1NTcyLCJ0b2tlblR5cGUiOiJSRUZSRVNIIiwiaWF0IjoxNzEwMjQwNzcyfQ.kkbdqPjcrut98KUWu2q6ah3LEflUiW7KLIHMjJsw9VLi6HVerIkIYwgm4c0qs4yPhiaW2YOU1e6u5afr18Iw5DsDdivHhLugEW83cC-lskruRrAmJKFbvyplL7bpxNFvKuEowlT_bLrNzjzKmutLr-5eYeEQahFap6YkEwm4XDo7MSeOfNtD3zvhsmZEQ05VKlxFnjL59-JuW_8tc8U4lHXIYIyCt4sJ8xozRYj2p2kco-ojNVZXXqKbEZpJ-81lqExxoC4VTVN7aamjqmpktNE58o2IakiA-IZVEs4riSBg3sB3VWp7fPLXDymqaMvHf2GOExM16KGUAg-K3X2NNA"))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    @Order(15)
    void revoke() throws Exception {
        mockMvc.perform(post("/v1/auth/logout")
                .header("Authorization", "Bearer " + accessToken)
                .content("Bearer " + refreshToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Logout success"));
    }

    @Test
    @Order(16)
    void revoke_OldToken() throws Exception {
        mockMvc.perform(post("/v1/auth/logout")
                .header("Authorization", "Bearer " + initialAccessToken)
                .content("Bearer " + refreshToken))
                .andExpect(status().isUnauthorized());
    }

    // test for forgot-password
    @Test
    @Order(17)
    void findUser() throws Exception {
        SendEmailRequest request = new SendEmailRequest(
                "johndoe@example.com",
                "http://localhost:8080/v1/auth/reset-password");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/auth/forgot-password")
                        .contentType(APP_JSON)
                        .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("johndoe@example.com")));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mailService).sendPasswordReset(
                eq("johndoe@example.com"),
                captor.capture(),
                eq("John Doe")
        );

        resetPasswordUrl = captor.getValue();
    }

    @Test
    @Order(18)
    void resetPassword() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("P@ssw0rd");
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(put(resetPasswordUrl)
                .content(json)
                .contentType(APP_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset"));
    }

    @Test
    @Order(19)
    void findUser_NotExists() throws Exception {
        SendEmailRequest request = new SendEmailRequest(
                "unexisting@example.com",
                "http://localhost:8080/v1/auth/confirmation");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/auth/forgot-password")
                        .contentType(APP_JSON)
                        .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("unexisting@example.com")));

        ArgumentCaptor<String> confirmationUrlCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mailService, Mockito.never()).sendPasswordReset(
                eq("unexisting@example.com"),
                confirmationUrlCaptor.capture(),
                eq("Unexisting User")
        );
    }

    @Test
    @Order(20)
    void recoverAccountSendEmail() throws Exception {
        AccountRecoveryRequest request = new AccountRecoveryRequest(
                "deleted@example.com",
                "P@ssw0rd",
                "http://localhost:8080/v1/auth/recovery"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/auth/recovery")
                .contentType(APP_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Recovery email sent to deleted@example.com"));

        ArgumentCaptor<String> confirmationUrlCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mailService).sendAccountRecoveryEmail(
                eq("deleted@example.com"),
                confirmationUrlCaptor.capture(),
                eq("isDeleted User")
        );

        recoveryUrl = confirmationUrlCaptor.getValue();
    }

    @Test
    @Order(20)
    void recoverAccountSendEmail_NotExists() throws Exception {
        AccountRecoveryRequest request = new AccountRecoveryRequest(
                "not-exists@example.com",
                "P@ssw0rd",
                "http://localhost:8080/v1/auth/recovery"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/auth/recovery")
                .contentType(APP_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Recovery email sent to not-exists@example.com"));

        ArgumentCaptor<String> confirmationUrlCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mailService, Mockito.never()).sendAccountRecoveryEmail(
                eq("not-exists@example.com"),
                confirmationUrlCaptor.capture(),
                eq("Not existing User")
        );
    }

    @Test
    @Order(21)
    void recoverAccount() throws Exception {
        mockMvc.perform(put(recoveryUrl))
                .andExpect(status().isOk())
                .andExpect(content().string("Account recovery success"));

        Long userId = 4L;
        UserEntity user = userRepository.findById(userId).orElse(null);

        assertThat(user).isNotNull();
        assertThat(user.isDeleted()).isFalse();
    }

    static String extractToken(String responseContent, String tokenName) throws JSONException {
        JSONObject jsonResponse = new JSONObject(responseContent);
        return jsonResponse.getString(tokenName);
    }
}