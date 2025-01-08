package ADG.Lobby;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.ArrayList;

@RemoteServiceRelativePath("message")
public interface MessageService extends RemoteService {
    void sendMessage(String roomId, Message message);
    ArrayList<Message> getMessages(String roomId);
}
