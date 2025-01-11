package naegamaja_server.naegamaja.domain.room.controller;

import lombok.RequiredArgsConstructor;
import naegamaja_server.naegamaja.domain.chat.entity.ChatLog;
import naegamaja_server.naegamaja.domain.chat.service.ChatLogService;
import naegamaja_server.naegamaja.domain.chat.service.RedisStreamChatPublisher;
import naegamaja_server.naegamaja.domain.room.service.RoomService;
import naegamaja_server.naegamaja.system.websocket.dto.Message;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RoomStompController {

    private final RoomService roomService;
    private final ChatLogService chatLogService;
    private final RedisStreamChatPublisher redisStreamChatPublisher;


    @MessageMapping("/room/{roomNumber}")
    public void chat(@DestinationVariable Long roomNumber, @Header("Authorization") String authorization, @Payload Message.Request message) {
        System.out.println(message.getContent());

        //chatLogService.roomChat(roomNumber, message, authorization);
        redisStreamChatPublisher.publishChatMessage(roomNumber.toString(), message);

        //todo
    }
}
