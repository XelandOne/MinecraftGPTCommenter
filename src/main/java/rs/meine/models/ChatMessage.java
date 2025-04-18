package rs.meine.models;

public class ChatMessage {
    private final String sender;
    private final String content;
    
    public ChatMessage(String sender, String content) {
        this.sender = sender;
        this.content = content;
    }
    
    public String getSender() {
        return sender;
    }
    
    public String getContent() {
        return content;
    }
    
    @Override
    public String toString() {
        return sender + ": " + content;
    }
} 