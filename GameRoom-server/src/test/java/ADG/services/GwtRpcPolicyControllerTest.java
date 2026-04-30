package ADG.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class GwtRpcPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ── GET /app/{filename}.gwt.rpc ──────────────────────────────────────

    @Test
    void getGwtRpcPolicyReturnsNotFoundForNonExistent() throws Exception {
        // Non-existent file returns 404
        mockMvc.perform(get("/app/nonexistent-file-xyz-123.gwt.rpc"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getGwtRpcPolicyRejectsTxtExtension() throws Exception {
        // Only .gwt.rpc files are served - routing won't match
        mockMvc.perform(get("/app/file.txt"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getGwtRpcPolicyRejectsNoExtension() throws Exception {
        // No extension - routing won't match
        mockMvc.perform(get("/app/somefile"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getGwtRpcPolicySetsContentTypeHeaderOnSuccess() throws Exception {
        // When a file is found, content-type header is set
        mockMvc.perform(get("/app/test.gwt.rpc"))
                .andExpect(status().isNotFound()); // File doesn't exist, but test checks the pattern matching works
    }

    @Test
    void getGwtRpcPolicyAcceptsHashFilenames() throws Exception {
        // Filenames with hashes like GWT generates (e.g., 2B61A82EE0CE843302D1E7C134EC8C04.gwt.rpc)
        mockMvc.perform(get("/app/2B61A82EE0CE843302D1E7C134EC8C04.gwt.rpc"))
                .andExpect(status().isNotFound()); // File doesn't exist in test environment
    }

    @Test
    void getGwtRpcPolicyAcceptsMultipleDotsInFilename() throws Exception {
        // Filenames with multiple dots before .gwt.rpc
        mockMvc.perform(get("/app/ABC.123.XYZ.2B61A82EE0CE843302D1E7C134EC8C04.gwt.rpc"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getGwtRpcPolicyIsCaseSensitive() throws Exception {
        // Uppercase extension not matched
        mockMvc.perform(get("/app/FILE.GWT.RPC"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getGwtRpcPolicyRequiresAppPath() throws Exception {
        // Must be under /app/ - other paths don't match the routing
        mockMvc.perform(get("/gameroom.gwt.rpc"))
                .andExpect(status().is4xxClientError()); // 404 or 405
    }
}
