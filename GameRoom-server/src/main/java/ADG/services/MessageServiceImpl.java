package ADG.services;

import ADG.Lobby.MessageService;
import ADG.Lobby.Message;
import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import jakarta.servlet.annotation.WebServlet;

import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("serial")
@WebServlet("/app/message")
public class MessageServiceImpl extends RemoteServiceServlet implements MessageService {
    private final ArrayList<String> roomIds = new ArrayList<>();
    private final HashMap<String, ArrayList<Message>> messagesMap = new HashMap<>();

    @Override
    public void sendMessage(String roomId, Message message) {
        if (!roomIds.contains(roomId)) {
            roomIds.add(roomId);
            messagesMap.put(roomId, new ArrayList<>());
        }
        messagesMap.get(roomId).add(message);
    }

    @Override
    public ArrayList<Message> getMessages(String roomId) {
        if (!roomIds.contains(roomId)) {
            return new ArrayList<>();
        }

        return messagesMap.get(roomId);
    }
}
