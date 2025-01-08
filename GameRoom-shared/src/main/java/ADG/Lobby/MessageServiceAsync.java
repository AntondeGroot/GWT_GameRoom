package ADG.Lobby;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;

public interface MessageServiceAsync {
    void sendMessage(String roomId, Message message, AsyncCallback<Void> callback) throws IllegalArgumentException;
    void getMessages(String roomId, AsyncCallback<ArrayList<Message>> asyncCallback) throws IllegalArgumentException;
}
