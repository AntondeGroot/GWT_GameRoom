package ADG;

import ADG.Lobby.RoomService;
import ADG.Lobby.RoomServiceAsync;
import ADG.Lobby.Room;
import ADG.Utils.Cookie;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class App implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side GameRoom service.
	 */

	private final RoomServiceAsync roomService = GWT.create(RoomService.class);
	private final PresenterManager presenterManager = new PresenterManager();

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		// create a cookie for each player
		Cookie.createPlayerIdCookie();

		String token = History.getToken();
		if (token.startsWith("room=")) {
			String roomId = token.substring("room=".length());
			roomService.getRoomById(roomId, new AsyncCallback<Room>() {
				public void onFailure(Throwable caught) {
					GWT.log("failed to retrieve room");
					presenterManager.switchToLobby();
				}
				public void onSuccess(Room room) {
					if (room == null) { presenterManager.switchToLobby(); return; }
					presenterManager.switchToGameRoom(room);
				}
			});
		} else if (token.startsWith("joining=")) {
			String roomId = token.substring("joining=".length());
			roomService.getRoomById(roomId, new AsyncCallback<Room>() {
				public void onFailure(Throwable caught) {
					presenterManager.switchToLobby();
				}
				public void onSuccess(Room room) {
					if (room != null && room.getPlayerIds().contains(Cookie.getPlayerId())) {
						presenterManager.switchToCharacterSelection(room);
					} else {
						presenterManager.switchToLobby();
					}
				}
			});
		} else {
			presenterManager.switchToLobby();
		}
	}
}
