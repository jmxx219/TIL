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

    public void setEnterMessage() {
        this.message = "[ " + MessageType.ENTER + " ]" + this.sender;
    }
}
