package com.authproject.controller;

import com.authproject.controller.dto.CreateUserRequestDto;
import com.authproject.entities.Role;
import com.authproject.entities.User;
import com.authproject.repository.RoleRepository;
import com.authproject.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Role basicRole;
    private UUID userId;

    @BeforeEach
    public void setup() {
        userId = UUID.randomUUID();

        basicRole = new Role();
        basicRole.setId(1L);
        basicRole.setName(Role.Values.BASIC.name());

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName(Role.Values.BASIC.name())).thenReturn(basicRole);
    }

    @Test
    public void testCreateUser_Success() throws Exception {
        // Arrange
        CreateUserRequestDto requestDto = new CreateUserRequestDto("testUser", "password");

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act & Assert
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    public void testCreateUser_UsernameTaken() throws Exception {
        // Arrange
        CreateUserRequestDto requestDto = new CreateUserRequestDto("existingUser", "password");

        User existingUser = new User();
        existingUser.setUsername("existingUser");

        when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testListUsers_WithAdminRole() throws Exception {
        List<User> users = new ArrayList<>();
        User user = new User();
        user.setUserId(userId);
        user.setUsername("testUser");
        users.add(user);

        when(userRepository.findAll()).thenReturn(users);

        // Act & Assert
        // Note: Testing with JSON path rather than exact JSON due to UUID serialization
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":\"" + userId + "\",\"username\":\"testUser\"}]"));
    }

    @Test
    @WithMockUser(authorities = "BASIC")
    public void testListUsers_WithoutAdminRole() throws Exception {
        // Act & Assert - Expecting 403 Forbidden because user doesn't have ADMIN role
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }
}