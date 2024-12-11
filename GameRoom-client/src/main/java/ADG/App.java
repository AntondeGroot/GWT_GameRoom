package ADG;

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

	private final GameRoomServiceAsync gameRoomService = GWT.create(GameRoomService.class);

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		// create a cookie for each player
		Cookie.createPlayerIdCookie();

		String token = History.getToken();
		if (token.startsWith("room=")) {
			String roomId = token.substring("room=".length());
			gameRoomService.getRoomById(roomId, new AsyncCallback<Room>() {
				public void onFailure(Throwable caught) {
					GWT.log("failed to retrieve room");
					GameLobby gameLobby = new GameLobby();
					gameLobby.onModuleLoad();
				}
				public void onSuccess(Room room) {
					GWT.log("Room from refresh page = "+room);
					GameRoom gameRoom = new GameRoom(room);
					gameRoom.load();

				}
			});

		} else {
			// Initialize the GameLobby when the application starts
			GameLobby gameLobby = new GameLobby();
			gameLobby.onModuleLoad();
		}

	}
}
