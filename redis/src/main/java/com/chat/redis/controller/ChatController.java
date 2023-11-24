package com.chat.redis.controller;

import com.chat.redis.domain.ChatMessage;
import com.chat.redis.domain.MessageType;
import com.chat.redis.pubsub.RedisPublisher;
import com.chat.redis.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final RedisPublisher redisPublisher;
    private final ChatService chatService;

    @MessageMapping("/chat/message")
    public void message(ChatMessage message) {
        if(message.getType().equals(MessageType.ENTER)) {
            message.setEnterMessage();
            chatService.enterChatRoom(message.getRoomId());
        }
        // 메시지를 redis의 Topic으로 발행함
        redisPublisher.publish(chatService.getTopic(message.getRoomId()), message);
    }

}
