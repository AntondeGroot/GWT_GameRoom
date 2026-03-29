package ADG.Lobby;

import ADG.*;
import ADG.audio.AudioPlayer;
import ADG.Utils.ChatCipher;
import ADG.Utils.Cookie;
import ADG.Utils.PollingService;
import ADG.Utils.TimeUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.*;
import com.google.gwt.json.client.*;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoomPresenter implements Presenter {

    private static final String CHAT_BASE_URL = "";

    private final RoomServiceAsync roomService;
    private final RoomView roomView;
    private int chatMessageCount = 0;
    private final Room room;
    private final PresenterManager presenterManager;
    private HashMap<String, String> userNames = new HashMap<>();
    private HashMap<String, String> userProfiles = new HashMap<>();
    private boolean playerListInitialized = false;
    private final PollingService pollingService = new PollingService();
    private final List<HandlerRegistration> handlerRegistrations = new ArrayList<>();

    public RoomPresenter(RoomView roomView, Room model, PresenterManager presenterManager, RoomServiceAsync roomService, MessageServiceAsync messageService) {
        this.roomView = roomView;
        this.room = model;
        this.presenterManager = presenterManager;
        this.roomService = roomService;
    }

    @Override
    public void start() {
        History.newItem("room=" + room.getId());
        AudioPlayer.play(AudioPlayer.PLAYER_ENTER);
        bind();
        pollingService.startPolling(500, this::pollServerForUpdates);
    }

    @Override
    public void stop() {
        pollingService.stopPolling();
        for (HandlerRegistration reg : handlerRegistrations) reg.removeHandler();
        handlerRegistrations.clear();
    }

    private void bind(){
        roomView.showRoomName(room.getName());
        roomView.refreshPlayerList(new HashMap<>(), new HashMap<>());
        roomView.refreshMessages(new ArrayList<>());
        handlerRegistrations.add(roomView.getLeaveRoomButton().addClickHandler(event -> { AudioPlayer.play(AudioPlayer.BUTTON_CLICK); leaveRoom(); }));
        handlerRegistrations.add(roomView.getDeleteRoomButton().addClickHandler(event -> { AudioPlayer.play(AudioPlayer.BUTTON_CLICK); deleteRoom(); }));
        handlerRegistrations.add(roomView.getStartGameButton().addClickHandler(event -> { AudioPlayer.play(AudioPlayer.BUTTON_CLICK); startGame(); }));
        handlerRegistrations.add(roomView.getSendMessageButton().addClickHandler(event -> { AudioPlayer.play(AudioPlayer.BUTTON_CLICK); sendMessage(); }));
        handlerRegistrations.add(roomView.getMessageInputField().addKeyDownHandler(event -> {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                sendMessage();
            }
        }));
        roomView.updateCreatorControls(room);
    }

    private void startGame() {
        roomService.startGame(room.getId(), new AsyncCallback<Room>() {
            @Override
            public void onFailure(Throwable throwable) {
                Window.alert("Failed to start game. Please try again.");
            }

            @Override
            public void onSuccess(Room room) {
                roomView.updateCreatorControls(room);
            }
        });
    }

    private void sendMessageToServer(String inputText) {
        String encrypted = ChatCipher.encrypt(inputText, room.getId());
        String sender = userNames.getOrDefault(Cookie.getPlayerId(), "?");
        JSONObject body = new JSONObject();
        body.put("sender", new JSONString(sender));
        body.put("message", new JSONString(encrypted));
        try {
            RequestBuilder rb = new RequestBuilder(RequestBuilder.POST,
                    URL.encode(CHAT_BASE_URL + "/chat/" + room.getId()));
            rb.setHeader("Content-Type", "application/json");
            rb.sendRequest(body.toString(), new RequestCallback() {
                @Override public void onResponseReceived(Request req, Response res) {}
                @Override public void onError(Request req, Throwable ex) {}
            });
        } catch (RequestException e) {
            GWT.log("chat send error: " + e.getMessage());
        }
    }

    private void sendMessage() {
        String inputText = roomView.getMessageInputField().getText();
        if(!inputText.isEmpty()){
            sendMessageToServer(inputText);
            roomView.getMessageInputField().setText("");
        }
    }

    private void leaveRoom() {
        removePlayerFromRoom();
        sendLeaveMessage();
        presenterManager.switchToLobby();
    }

    private void sendLeaveMessage() {
        sendMessageToServer("has left the room");
    }

    private void deleteRoom() {
        boolean confirmDelete = Window.confirm("Are you sure you want to delete this room?");
        if (confirmDelete) {
            roomService.deleteRoom(room.getName(), new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable throwable) {
                }

                @Override
                public void onSuccess(Void unused) {
                    presenterManager.switchToLobby();
                }
            });
        }
    }

    public void pollServerForUpdates(){
        pollServerForPlayers();
        pollServerForMessages();
    }

    public void pollServerForPlayers() {
        roomService.getRoomById(room.getId(), new AsyncCallback<Room>() {
            @Override
            public void onFailure(Throwable throwable) {
                GWT.log(throwable.getMessage());
            }

            @Override
            public void onSuccess(Room updatedRoom) {
                if (updatedRoom == null) {
                    stop();
                    presenterManager.switchToLobby();
                    return;
                }
                if (updatedRoom.getStatus() == GameStatus.PLAYING && updatedRoom.getGameSessionId() != null) {
                    stop();
                    String url = updatedRoom.getGameBaseUrl()
                            + "/?sessionid=" + updatedRoom.getGameSessionId()
                            + "&playerid=" + Cookie.getPlayerId();
                    Window.Location.replace(url);
                    return;
                }
                HashMap<String, String> serverUserNames = updatedRoom.getPlayerNames();
                HashMap<String, String> serverUserProfiles = updatedRoom.getPlayerProfiles();
                if (!serverUserNames.equals(userNames) || !serverUserProfiles.equals(userProfiles)) {
                    if (playerListInitialized && serverUserNames.size() > userNames.size()) {
                        AudioPlayer.play(AudioPlayer.PLAYER_ENTER);
                    }
                    playerListInitialized = true;
                    userNames = serverUserNames;
                    userProfiles = serverUserProfiles;
                    roomView.refreshPlayerList(userNames, userProfiles);
                } else {
                    playerListInitialized = true;
                }
                roomView.updateCreatorControls(updatedRoom);
            }
        });
    }

    public void pollServerForMessages() {
        try {
            RequestBuilder rb = new RequestBuilder(RequestBuilder.GET,
                    URL.encode(CHAT_BASE_URL + "/chat/" + room.getId()));
            rb.setHeader("Accept", "application/json");
            rb.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request req, Response res) {
                    if (res.getStatusCode() < 200 || res.getStatusCode() >= 300) return;
                    JSONValue parsed;
                    try {
                        parsed = JSONParser.parseStrict(res.getText());
                    } catch (JSONException e) {
                        GWT.log("chat poll: invalid JSON response: " + e.getMessage());
                        return;
                    }
                    JSONArray arr = parsed.isArray();
                    if (arr == null || arr.size() == chatMessageCount) return;
                    chatMessageCount = arr.size();
                    ArrayList<Message> decrypted = new ArrayList<>();
                    for (int i = 0; i < arr.size(); i++) {
                        JSONObject m = arr.get(i).isObject();
                        if (m == null) continue;
                        String timestampUTC = m.get("timestampUTC").isString().stringValue();
                        String sender    = m.get("sender").isString().stringValue();
                        String encrypted = m.get("message").isString().stringValue();
                        decrypted.add(new Message(TimeUtils.convertUTCToLocal(timestampUTC), sender,
                                ChatCipher.decrypt(encrypted, room.getId())));
                    }
                    roomView.refreshMessages(decrypted);
                }
                @Override public void onError(Request req, Throwable ex) {
                    GWT.log("chat poll error: " + ex.getMessage());
                }
            });
        } catch (RequestException e) {
            GWT.log("chat poll error: " + e.getMessage());
        }
    }

    private void removePlayerFromRoom() {
        roomService.removePlayerFromRoom(Cookie.getPlayerId(), room.getId(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onSuccess(Void v) {
            }
        });
    }
}
