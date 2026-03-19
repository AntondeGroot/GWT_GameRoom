package ADG.services;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final Map<String, CopyOnWriteArrayList<Map<String, String>>> messages = new ConcurrentHashMap<>();

    @PostMapping(value = "/{roomId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendMessage(
            @PathVariable("roomId") String roomId,
            @RequestBody Map<String, String> body) {
        Map<String, String> entry = new LinkedHashMap<>();
        entry.put("sender", body.getOrDefault("sender", ""));
        entry.put("message", body.getOrDefault("message", ""));
        entry.put("timestamp", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        messages.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>()).add(entry);
        return ResponseEntity.status(201).build();
    }

    @GetMapping(value = "/{roomId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, String>>> getMessages(@PathVariable("roomId") String roomId) {
        return ResponseEntity.ok(new ArrayList<>(messages.getOrDefault(roomId, new CopyOnWriteArrayList<>())));
    }
}