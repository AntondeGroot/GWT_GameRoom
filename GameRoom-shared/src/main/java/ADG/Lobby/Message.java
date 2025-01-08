package ADG.Lobby;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Objects;

public class Message implements IsSerializable {
    String nameSender;
    String message;
    String timestamp;

    public Message(){}

    public Message(String timestamp, String nameSender, String message) {
        this.nameSender = nameSender;
        this.message = message;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return timestamp + " - " + nameSender + ": " + "\t" + message;
    }

    public String getNameSender() {
        return nameSender;
    }

    public void setNameSender(String nameSender) {
        this.nameSender = nameSender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message1 = (Message) o;
        return Objects.equals(nameSender, message1.nameSender) && Objects.equals(message, message1.message) && Objects.equals(timestamp, message1.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameSender, message, timestamp);
    }
}
