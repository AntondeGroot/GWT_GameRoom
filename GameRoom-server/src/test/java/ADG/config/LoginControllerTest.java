package ADG.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ── GET /login ───────────────────────────────────────────────────────

    @Test
    void getLoginRedirectsToLoginHtml() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login.html"));
    }

    @Test
    void loginRedirectIsTemporary() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isFound());
    }
}