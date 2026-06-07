package api.pyapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.WebApplicationContext;

import jakarta.servlet.Filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import api.pyapi.Entities.GummyEntity;
import api.pyapi.Entities.UserEntity;
import api.pyapi.Repository.GummyRepository;
import api.pyapi.Repository.UserRepository;
import api.pyapi.Security.JwtService;

@SpringBootTest
class ApiEndpointsTests {

    @Autowired
        private WebApplicationContext webApplicationContext;

        private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GummyRepository gummyRepository;

    @Autowired
    private JwtService jwtService;

        @Autowired
        @Qualifier("springSecurityFilterChain")
        private Filter springSecurityFilterChain;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUpMockMvc() {
                this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                .addFilters(springSecurityFilterChain)
                                .build();
        }

    @BeforeEach
    void cleanDatabase() {
        gummyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void signupLoginRefreshAndGummyCrudWork() throws Exception {
        MockHttpServletResponse signupResponse = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "id", 1,
                                "username", "migue",
                                "password", "secret123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User created"))
                .andExpect(jsonPath("$.id").value(1))
                .andReturn()
                .getResponse();

        Map<String, Object> signupBody = readJson(signupResponse, new TypeReference<Map<String, Object>>() {});
        String signupAccessToken = (String) signupBody.get("accessToken");
        String signupRefreshToken = (String) signupBody.get("refreshToken");

        assertThat(jwtService.isAccessTokenValid(signupAccessToken)).isTrue();
        assertThat(jwtService.isRefreshTokenValid(signupRefreshToken)).isTrue();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "id", 1,
                                "password", "secret123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("migue"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "refreshToken", signupRefreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Token refreshed"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "refreshToken", "invalid-token"))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/gummy/list"))
                .andExpect(status().isForbidden());

        String accessToken = signupAccessToken;
        GummyEntity gummy = new GummyEntity();
        gummy.setName("Bear");
        gummy.setFlavor("Orange");
        gummy.setColor("Yellow");
        gummy.setShape("Bear");
        gummy.setQuantity(10);
        gummy.setPrice(3.5);
        gummy.setVegan(true);
        gummy.setIngredients(java.util.List.of("sugar", "gelatin"));

        mockMvc.perform(post("/gummy/{id}", 1)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gummy)))
                .andExpect(status().isOk())
                .andExpect(content -> assertThat(content.getResponse().getContentAsString()).isEqualTo("Successfull"));

        MockHttpServletResponse listResponse = mockMvc.perform(get("/gummy/list")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        java.util.List<?> gummyList = readJson(listResponse, new TypeReference<java.util.List<?>>() {});
        assertThat(gummyList).hasSize(1);

        MockHttpServletResponse readResponse = mockMvc.perform(get("/gummy/read/{id}", 1)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bear"))
                .andReturn()
                .getResponse();

        Map<String, Object> createdGummy = readJson(readResponse, new TypeReference<Map<String, Object>>() {});
        Number gummyId = (Number) createdGummy.get("id");

        GummyEntity update = new GummyEntity();
        update.setId(gummyId.longValue());
        update.setName("Bear XL");
        update.setFlavor("Orange");
        update.setColor("Yellow");
        update.setShape("Bear");
        update.setQuantity(12);
        update.setPrice(4.0);
        update.setVegan(true);
        update.setIngredients(java.util.List.of("sugar", "pectin"));

        mockMvc.perform(put("/gummy/{id}", 1)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(content -> assertThat(content.getResponse().getContentAsString()).isEqualTo("Successfull"));

        mockMvc.perform(delete("/gummy/delete/{id}", gummyId.longValue())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content -> assertThat(content.getResponse().getContentAsString()).isEqualTo("Successfull"));

        mockMvc.perform(delete("/gummy/delete/{id}", gummyId.longValue())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void signupRejectsDuplicateUserId() throws Exception {
        UserEntity user = new UserEntity();
        user.setId(5);
        user.setUsername("existing");
        user.setPassword(passwordEncoder.encode("secret123"));
        userRepository.save(user);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "id", 5,
                                "username", "existing",
                                "password", "secret123"))))
                .andExpect(status().isConflict());
    }

    @SuppressWarnings("unchecked")
    private <T> T readJson(MockHttpServletResponse response, TypeReference<T> typeReference) throws Exception {
        return objectMapper.readValue(response.getContentAsString(), typeReference);
    }
}