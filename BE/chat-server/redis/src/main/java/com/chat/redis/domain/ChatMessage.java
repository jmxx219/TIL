package com.chat.redis.domain;

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
        this.message = this.sender + "님이 입장하셨습니다.";
    }
}
