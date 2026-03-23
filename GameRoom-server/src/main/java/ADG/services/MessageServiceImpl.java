package ADG.services;

import ADG.Lobby.MessageService;
import ADG.Lobby.Message;
import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import jakarta.servlet.annotation.WebServlet;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("serial")
@WebServlet("/app/message")
public class MessageServiceImpl extends RemoteServiceServlet implements MessageService {
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Message>> messagesMap = new ConcurrentHashMap<>();

    @Override
    public void sendMessage(String roomId, Message message) {
        messagesMap.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>()).add(message);
    }

    @Override
    public ArrayList<Message> getMessages(String roomId) {
        CopyOnWriteArrayList<Message> messages = messagesMap.get(roomId);
        if (messages == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(messages);
    }
}
