package naegamaja_server.naegamaja.domain.redis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import naegamaja_server.naegamaja.domain.chat.entity.ChatLog;
import naegamaja_server.naegamaja.domain.game.entity.Player;
import naegamaja_server.naegamaja.domain.room.dto.RoomUserInfo;
import naegamaja_server.naegamaja.domain.session.repository.CustomRedisSessionRepository;
import naegamaja_server.naegamaja.system.websocket.dto.Message;
import naegamaja_server.naegamaja.system.websocket.model.MessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisPubSubSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final CustomRedisSessionRepository customRedisSessionRepository;


    public void roomChat(String message, String channel){
        try{
            Message.RoomChatPubSubResponse roomChatPubSubResponse = objectMapper.readValue(message, Message.RoomChatPubSubResponse.class);

            ChatLog chatlog = roomChatPubSubResponse.getChatLog();

            System.out.println("/topic/room/" + roomChatPubSubResponse.getId() + "로 보냈어");

            simpMessagingTemplate.convertAndSend("/topic/room/" + roomChatPubSubResponse.getId() + "/chat", chatlog);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void roomInfo(String message, String channel){
        try{
            RoomUserInfo roomUserInfo = objectMapper.readValue(message, RoomUserInfo.class);

            Thread.sleep(500);

            simpMessagingTemplate.convertAndSend("/topic/room/" + roomUserInfo.getRoomId() + "/notice/users", roomUserInfo.getUsers());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void gameStart(String message, String channel){
        try{
            String gameId = objectMapper.readValue(message, String.class);
            Message.GameSystemResponse gameSystemResponse = Message.GameSystemResponse.builder()
                    .type(MessageType.GAME_START)
                    .content(gameId)
                    .build();

            System.out.println("/topic/room/" + gameId + "로 보냈어");

            simpMessagingTemplate.convertAndSend("/topic/room/" + gameId, gameSystemResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void UserInfo(String message, String channel){
        try{
            List<Player> playerList = objectMapper.readValue(message, objectMapper.getTypeFactory().constructCollectionType(List.class, Player.class));

            for(Player player : playerList){
                simpMessagingTemplate.convertAndSend("/topic/user/" + player.getNickname() + "/gameInfo", player);
            }

            System.out.println("유저 개인 정보 출력");
            System.out.println(playerList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


}
