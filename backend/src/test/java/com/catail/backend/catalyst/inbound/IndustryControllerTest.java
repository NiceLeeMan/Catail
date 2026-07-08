package com.catail.backend.catalyst.inbound;

import com.catail.backend.catalyst.DB.Industry;
import com.catail.backend.catalyst.DB.IndustryRepository;
import com.catail.backend.global.jwt.JwtAuthenticationFilter;
import com.catail.backend.global.web.CurrentUserIdArgumentResolver;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IndustryController.class)
class IndustryControllerTest {

    private MockMvc mockMvc;

    @Autowired private WebApplicationContext context;

    @MockitoBean private IndustryRepository industryRepository;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean private CurrentUserIdArgumentResolver currentUserIdArgumentResolver;

    @BeforeEach
    void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        given(currentUserIdArgumentResolver.supportsParameter(any())).willReturn(true);
        given(currentUserIdArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(1L);
    }

    @Test
    @DisplayName("GET /api/industries는 200과 산업 목록을 반환한다")
    void getList_returns200() throws Exception {
        given(industryRepository.findAll()).willReturn(List.of(
                Industry.create("반도체"),
                Industry.create("2차전지")
        ));

        mockMvc.perform(get("/api/industries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("반도체"))
                .andExpect(jsonPath("$.data[1].name").value("2차전지"));
    }
}
