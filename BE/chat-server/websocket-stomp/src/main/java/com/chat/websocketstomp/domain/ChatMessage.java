package com.chat.websocketstomp.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessage {
    private MessageType type;
    private String sender;
    private String roomId;
    private String message;

    public void setEnterMessage() {
        this.message = "[ " + MessageType.ENTER + " ] " + this.sender;
    }
}
