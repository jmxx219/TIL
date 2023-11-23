package com.chat.websocket.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    public MessageType type;
    private String sender;
    private String roomId;
    private String message;

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void newConnect() {
        this.type = MessageType.ENTER;
    }

    public void closeConnect() {
        this.type = MessageType.CLOSE;
    }

    public void setEnterMessage() {
        this.message = "[ " + MessageType.ENTER + " ]" + this.message;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "type=" + type +
                ", sender='" + sender + '\'' +
                ", roomId='" + roomId + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
