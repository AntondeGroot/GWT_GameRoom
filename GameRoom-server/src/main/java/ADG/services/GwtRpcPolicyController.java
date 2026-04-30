package ADG.services;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
public class GwtRpcPolicyController {

    @GetMapping("/app/{filename:.+\\.gwt\\.rpc}")
    public ResponseEntity<String> serveGwtRpcPolicy(@PathVariable("filename") String filename) {
        try {
            ClassPathResource resource = new ClassPathResource("public/app/" + filename);
            if (resource.exists()) {
                String content = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                        .body(content);
            }
        } catch (IOException e) {
            // Fall through to return 404
        }
        return ResponseEntity.notFound().build();
    }
}