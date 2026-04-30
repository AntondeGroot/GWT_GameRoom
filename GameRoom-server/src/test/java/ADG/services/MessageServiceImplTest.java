package ADG.services;

import ADG.Lobby.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MessageServiceImplTest {

    private MessageServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MessageServiceImpl();
    }

    private Message buildMessage(String sender, String text) {
        Message m = new Message();
        m.setNameSender(sender);
        m.setMessage(text);
        return m;
    }

    // ── sendMessage ──────────────────────────────────────────────────────

    @Test
    void sendMessageStoresMessageInRoom() {
        Message msg = buildMessage("Alice", "Hello!");
        service.sendMessage("room-1", msg);

        ArrayList<Message> messages = service.getMessages("room-1");
        assertEquals(1, messages.size());
        assertEquals("Alice", messages.get(0).getNameSender());
        assertEquals("Hello!", messages.get(0).getMessage());
    }

    @Test
    void sendMessageAppendsToExistingMessages() {
        service.sendMessage("room-1", buildMessage("Alice", "First"));
        service.sendMessage("room-1", buildMessage("Bob", "Second"));
        service.sendMessage("room-1", buildMessage("Carol", "Third"));

        ArrayList<Message> messages = service.getMessages("room-1");
        assertEquals(3, messages.size());
        assertEquals("First", messages.get(0).getMessage());
        assertEquals("Second", messages.get(1).getMessage());
        assertEquals("Third", messages.get(2).getMessage());
    }

    @Test
    void sendMessagePreservesOrder() {
        for (int i = 0; i < 10; i++) {
            service.sendMessage("room-1", buildMessage("Sender", "Message " + i));
        }

        ArrayList<Message> messages = service.getMessages("room-1");
        for (int i = 0; i < 10; i++) {
            assertEquals("Message " + i, messages.get(i).getMessage());
        }
    }

    @Test
    void sendMessageToMultipleRoomsKeepsMessagesIsolated() {
        service.sendMessage("room-1", buildMessage("Alice", "Room 1 message"));
        service.sendMessage("room-2", buildMessage("Bob", "Room 2 message"));
        service.sendMessage("room-1", buildMessage("Carol", "Another Room 1 message"));

        ArrayList<Message> room1Messages = service.getMessages("room-1");
        ArrayList<Message> room2Messages = service.getMessages("room-2");

        assertEquals(2, room1Messages.size());
        assertEquals(1, room2Messages.size());
        assertEquals("Alice", room1Messages.get(0).getNameSender());
        assertEquals("Bob", room2Messages.get(0).getNameSender());
    }

    @Test
    void sendMessageHandlesNullSender() {
        Message msg = new Message();
        msg.setNameSender(null);
        msg.setMessage("Message with null sender");

        service.sendMessage("room-1", msg);

        ArrayList<Message> messages = service.getMessages("room-1");
        assertEquals(1, messages.size());
        assertNull(messages.get(0).getNameSender());
    }

    @Test
    void sendMessageHandlesNullMessage() {
        Message msg = new Message();
        msg.setNameSender("Alice");
        msg.setMessage(null);

        service.sendMessage("room-1", msg);

        ArrayList<Message> messages = service.getMessages("room-1");
        assertEquals(1, messages.size());
        assertNull(messages.get(0).getMessage());
    }

    @Test
    void sendMessageHandlesEmptyRoom() {
        Message msg = buildMessage("Alice", "Message in empty room");
        service.sendMessage("", msg);

        ArrayList<Message> messages = service.getMessages("");
        assertEquals(1, messages.size());
    }

    // ── getMessages ──────────────────────────────────────────────────────

    @Test
    void getMessagesReturnsEmptyListForUnknownRoom() {
        ArrayList<Message> messages = service.getMessages("unknown-room");
        assertNotNull(messages);
        assertTrue(messages.isEmpty());
    }

    @Test
    void getMessagesReturnsNewListNotReference() {
        service.sendMessage("room-1", buildMessage("Alice", "Message"));

        ArrayList<Message> messages1 = service.getMessages("room-1");
        messages1.add(buildMessage("Hacker", "Injected"));

        ArrayList<Message> messages2 = service.getMessages("room-1");
        assertEquals(1, messages2.size(), "Modifications should not affect stored messages");
    }

    @Test
    void getMessagesReturnsMessagesInOrder() {
        for (int i = 1; i <= 5; i++) {
            service.sendMessage("room-1", buildMessage("User", "Message " + i));
        }

        ArrayList<Message> messages = service.getMessages("room-1");
        for (int i = 0; i < 5; i++) {
            assertEquals("Message " + (i + 1), messages.get(i).getMessage());
        }
    }

    // ── thread safety ────────────────────────────────────────────────────

    @Test
    void sendMessageIsThreadSafe() throws InterruptedException {
        Thread[] threads = new Thread[10];
        for (int t = 0; t < 10; t++) {
            final int threadNum = t;
            threads[t] = new Thread(() -> {
                for (int i = 0; i < 10; i++) {
                    service.sendMessage("room-1", buildMessage("Thread" + threadNum, "Msg" + i));
                }
            });
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        ArrayList<Message> messages = service.getMessages("room-1");
        assertEquals(100, messages.size());
    }

    @Test
    void getMessagesDoesNotBlockSendMessage() throws InterruptedException {
        service.sendMessage("room-1", buildMessage("Alice", "Message 1"));

        Thread readThread = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                service.getMessages("room-1");
            }
        });

        Thread writeThread = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                service.sendMessage("room-1", buildMessage("Writer", "Message " + i));
            }
        });

        readThread.start();
        writeThread.start();
        readThread.join();
        writeThread.join();

        ArrayList<Message> finalMessages = service.getMessages("room-1");
        assertEquals(101, finalMessages.size());
    }

    // ── concurrent room access ──────────────────────────────────────────

    @Test
    void concurrentWritesToDifferentRoomsAreSafe() throws InterruptedException {
        Thread[] threads = new Thread[5];
        for (int t = 0; t < 5; t++) {
            final int roomNum = t;
            threads[t] = new Thread(() -> {
                for (int i = 0; i < 20; i++) {
                    service.sendMessage("room-" + roomNum, buildMessage("User", "Msg" + i));
                }
            });
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        for (int i = 0; i < 5; i++) {
            ArrayList<Message> messages = service.getMessages("room-" + i);
            assertEquals(20, messages.size());
        }
    }

    @Test
    void messageAttributesPreserved() {
        Message msg = new Message();
        msg.setNameSender("Alice");
        msg.setMessage("Test message");
        msg.setTimestamp("12:34");

        service.sendMessage("room-1", msg);

        ArrayList<Message> messages = service.getMessages("room-1");
        assertEquals("Alice", messages.get(0).getNameSender());
        assertEquals("Test message", messages.get(0).getMessage());
        assertEquals("12:34", messages.get(0).getTimestamp());
    }
}