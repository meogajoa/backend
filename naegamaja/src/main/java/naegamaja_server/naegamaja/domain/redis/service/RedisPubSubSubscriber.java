package naegamaja_server.naegamaja.domain.redis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import naegamaja_server.naegamaja.domain.chat.entity.ChatLog;
import naegamaja_server.naegamaja.domain.game.entity.Player;
import naegamaja_server.naegamaja.domain.room.dto.RoomUserInfo;
import naegamaja_server.naegamaja.domain.session.repository.CustomRedisSessionRepository;
import naegamaja_server.naegamaja.system.websocket.dto.MeogajoaMessage;
import naegamaja_server.naegamaja.system.websocket.model.MessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisPubSubSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final CustomRedisSessionRepository customRedisSessionRepository;


    public void roomChat(String message, String channel){
        try{
            MeogajoaMessage.ChatPubSubResponse chatPubSubResponse = objectMapper.readValue(message, MeogajoaMessage.ChatPubSubResponse.class);

            ChatLog chatlog = chatPubSubResponse.getChatLog();

            System.out.println("/topic/room/" + chatPubSubResponse.getId() + "로 보냈어");
            Map<String, Object> header = new HashMap<>();
            header.put("x-chat-room", "LOBBY");
            header.put("x-log-type", "SINGLE");

            simpMessagingTemplate.convertAndSend("/topic/room/" + chatPubSubResponse.getId() + "/chat", chatlog, header);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void gameChat(String message, String channel){
        try{
            MeogajoaMessage.ChatPubSubResponse chatPubSubResponse = objectMapper.readValue(message, MeogajoaMessage.ChatPubSubResponse.class);

            ChatLog chatlog = chatPubSubResponse.getChatLog();

            System.out.println("/topic/game/" + chatPubSubResponse.getId() + "로 보냈어");
            Map<String, Object> header = new HashMap<>();
            header.put("x-chat-room", "GENERAL");
            header.put("x-log-type", "SINGLE");
            simpMessagingTemplate.convertAndSend("/topic/game/" + chatPubSubResponse.getId() + "/chat", chatlog, header);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void roomInfo(String message, String channel){
        try{
            RoomUserInfo roomUserInfo = objectMapper.readValue(message, RoomUserInfo.class);

            simpMessagingTemplate.convertAndSend("/topic/room/" + roomUserInfo.getRoomId() + "/notice/users", roomUserInfo.getUsers());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void gameStart(String message, String channel){
        try{
            MeogajoaMessage.GameSystemResponse gameSystemResponse = objectMapper.readValue(message, MeogajoaMessage.GameSystemResponse.class);

            System.out.println("게임 시작 메시지 보냈어");
            simpMessagingTemplate.convertAndSend("/topic/room/" + gameSystemResponse.getId() + "/notice/system", gameSystemResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void gameEnd(String message, String channel){
        try{
            MeogajoaMessage.GameSystemResponse gameEndResponse = objectMapper.readValue(message, MeogajoaMessage.GameSystemResponse.class);
            simpMessagingTemplate.convertAndSend("/topic/game/" + gameEndResponse.getId() + "/notice/system", gameEndResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void userInfo(String message, String channel){
        try{
            List<Player> playerList = objectMapper.readValue(message, objectMapper.getTypeFactory().constructCollectionType(List.class, Player.class));

            for(Player player : playerList){
                MeogajoaMessage.PlayerInfoResponse playerInfoResponse = MeogajoaMessage.PlayerInfoResponse.builder()
                        .type(MessageType.GAME_USER_INFO)
                        .id(UUID.randomUUID().toString())
                        .sendTime(LocalDateTime.now())
                        .sender("SYSTEM")
                        .player(player)
                        .build();
                simpMessagingTemplate.convertAndSend("/topic/user/" + player.getNickname() + "/gameInfo", playerInfoResponse);
            }

            System.out.println("유저 개인 정보 출력");
            System.out.println(playerList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void gameUserInfoPersonal(String message, String channel){
        try{
            Player player = objectMapper.readValue(message, Player.class);
            MeogajoaMessage.PlayerInfoResponse playerInfoResponse = MeogajoaMessage.PlayerInfoResponse.builder()
                    .type(MessageType.GAME_USER_INFO)
                    .id(UUID.randomUUID().toString())
                    .sendTime(LocalDateTime.now())
                    .sender("SYSTEM")
                    .player(player)
                    .build();
            simpMessagingTemplate.convertAndSend("/topic/user/" + player.getNickname() + "/gameInfo", playerInfoResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void gameDayOrNight(String message, String channel){
        try{
            MeogajoaMessage.GameDayOrNightResponse gameDayOrNightResponse = objectMapper.readValue(message, MeogajoaMessage.GameDayOrNightResponse.class);
            simpMessagingTemplate.convertAndSend("/topic/game/" + gameDayOrNightResponse.getGameId() + "/notice/system", gameDayOrNightResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void miniGameNotice(String message, String channel){
        try{
            MeogajoaMessage.MiniGameNoticeResponse gameMiniGameNoticeResponse = objectMapper.readValue(message, MeogajoaMessage.MiniGameNoticeResponse.class);
            simpMessagingTemplate.convertAndSend("/topic/game/" + gameMiniGameNoticeResponse.getId() + "/notice/system", gameMiniGameNoticeResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void buttonGameStatus(String message, String channel){
        try{
            MeogajoaMessage.ButtonGameStatusResponse buttonGameStatusResponse = objectMapper.readValue(message, MeogajoaMessage.ButtonGameStatusResponse.class);
            simpMessagingTemplate.convertAndSend("/topic/game/" + buttonGameStatusResponse.getId() + "/notice/system", buttonGameStatusResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void voteGameStatus(String message, String channel){
        try{
            MeogajoaMessage.VoteGameStatusResponse voteGameStatusResponse = objectMapper.readValue(message, MeogajoaMessage.VoteGameStatusResponse.class);
            simpMessagingTemplate.convertAndSend("/topic/game/" + voteGameStatusResponse.getId() + "/notice/system", voteGameStatusResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void gameChatToUser(String message, String channel){
        try{
            MeogajoaMessage.ChatPubSubResponseToUser chatPubSubResponseToUser = objectMapper.readValue(message, MeogajoaMessage.ChatPubSubResponseToUser.class);

            MeogajoaMessage.PersonalChatLog personalChatLog = chatPubSubResponseToUser.getPersonalChatLog();
            String receiver = chatPubSubResponseToUser.getReceiver();
            String sender = chatPubSubResponseToUser.getSender();
            Map<String, Object> header = new HashMap<>();
            header.put("x-chat-room", "PERSONAL");
            header.put("x-log-type", "PERSONAL_SINGLE");

            System.out.println("개인채팅 보냈어");
            simpMessagingTemplate.convertAndSend("/topic/user/" + receiver + "/gameChat", personalChatLog, header);
            simpMessagingTemplate.convertAndSend("/topic/user/" + sender + "/gameChat", personalChatLog, header);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void blackChat(String message, String channel){
        try{
            MeogajoaMessage.ChatPubSubResponse chatPubSubResponse = objectMapper.readValue(message, MeogajoaMessage.ChatPubSubResponse.class);

            ChatLog chatlog = chatPubSubResponse.getChatLog();

            Map<String, Object> header = new HashMap<>();
            header.put("x-chat-room", "BLACK");
            header.put("x-log-type", "SINGLE");

            simpMessagingTemplate.convertAndSend("/topic/game/" + chatPubSubResponse.getId() + "/chat/black", chatlog, header);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void whiteChat(String message, String channel){
        try{
            MeogajoaMessage.ChatPubSubResponse chatPubSubResponse = objectMapper.readValue(message, MeogajoaMessage.ChatPubSubResponse.class);

            ChatLog chatlog = chatPubSubResponse.getChatLog();

            Map<String, Object> header = new HashMap<>();
            header.put("x-chat-room", "WHITE");
            header.put("x-log-type", "SINGLE");

            simpMessagingTemplate.convertAndSend("/topic/game/" + chatPubSubResponse.getId() + "/chat/white", chatlog, header);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void eliminatedChat(String message, String channel){
        try{
            MeogajoaMessage.ChatPubSubResponse chatPubSubResponse = objectMapper.readValue(message, MeogajoaMessage.ChatPubSubResponse.class);

            ChatLog chatlog = chatPubSubResponse.getChatLog();

            Map<String, Object> header = new HashMap<>();
            header.put("x-chat-room", "ELIMINATED");
            header.put("x-log-type", "SINGLE");

            simpMessagingTemplate.convertAndSend("/topic/game/" + chatPubSubResponse.getId() + "/chat/eliminated", chatlog, header);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void gameUserListInfo(String message, String channel){
        try{
            MeogajoaMessage.GameUserListResponse gameUserListResponse = objectMapper.readValue(message, MeogajoaMessage.GameUserListResponse.class);

            simpMessagingTemplate.convertAndSend("/topic/game/" + gameUserListResponse.getId() + "/notice/users", gameUserListResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void blackChatList(String message, String channel){
        try{
            MeogajoaMessage.ChatLogResponse chatLogResponse = objectMapper.readValue(message, MeogajoaMessage.ChatLogResponse.class);

            Map<String, Object> header = new HashMap<>();
            header.put("x-chat-room", "BLACK");
            header.put("x-log-type", "HISTORY");

            simpMessagingTemplate.convertAndSend("/topic/game/" + chatLogResponse.getId() + "/chat/black", chatLogResponse, header);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void whiteChatList(String message, String channel){
        try{
            MeogajoaMessage.ChatLogResponse chatLogResponse = objectMapper.readValue(message, MeogajoaMessage.ChatLogResponse.class);
            Map<String, Object> header = new HashMap<>();
            header.put("x-chat-room", "WHITE");
            header.put("x-log-type", "HISTORY");

            simpMessagingTemplate.convertAndSend("/topic/game/" + chatLogResponse.getId() + "/chat/white", chatLogResponse, header);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void gameChatList(String message, String channel){
        try{
            MeogajoaMessage.ChatLogResponse chatLogResponse = objectMapper.readValue(message, MeogajoaMessage.ChatLogResponse.class);
            Map<String, Object> header = new HashMap<>();
            header.put("x-chat-room", "GENERAL");
            header.put("x-log-type", "HISTORY");

            simpMessagingTemplate.convertAndSend("/topic/game/" + chatLogResponse.getId() + "/chat", chatLogResponse, header);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void personalChatList(String message, String channel){
        try{
            MeogajoaMessage.PersonalChatLogResponse personalChatLogResponse = objectMapper.readValue(message, MeogajoaMessage.PersonalChatLogResponse.class);

            Map<String, Object> header = new HashMap<>();
            header.put("x-chat-room", "PERSONAL");
            header.put("x-log-type", "PERSONAL_HISTORY");

            simpMessagingTemplate.convertAndSend("/topic/user/" + personalChatLogResponse.getReceiver() + "/gameChat", personalChatLogResponse, header);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void eliminatedChatList(String message, String channel){
        try{
            MeogajoaMessage.ChatLogResponse chatLogResponse = objectMapper.readValue(message, MeogajoaMessage.ChatLogResponse.class);
            Map<String, Object> header = new HashMap<>();
            header.put("x-chat-room", "ELIMINATED");
            header.put("x-log-type", "HISTORY");

            simpMessagingTemplate.convertAndSend("/topic/game/" + chatLogResponse.getId() + "/chat/eliminated", chatLogResponse, header);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void redChat(String message, String channel){
        try{
            MeogajoaMessage.ChatPubSubResponse chatPubSubResponse = objectMapper.readValue(message, MeogajoaMessage.ChatPubSubResponse.class);

            ChatLog chatlog = chatPubSubResponse.getChatLog();

            Map<String, Object> header = new HashMap<>();
            header.put("x-chat-room", "RED");
            header.put("x-log-type", "SINGLE");

            simpMessagingTemplate.convertAndSend("/topic/game/" + chatPubSubResponse.getId() + "/chat/red", chatlog, header);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void redChatList(String message, String channel){
        try{
            MeogajoaMessage.ChatLogResponse chatLogResponse = objectMapper.readValue(message, MeogajoaMessage.ChatLogResponse.class);
            Map<String, Object> header = new HashMap<>();
            header.put("x-chat-room", "RED");
            header.put("x-log-type", "HISTORY");

            simpMessagingTemplate.convertAndSend("/topic/game/" + chatLogResponse.getId() + "/chat/red", chatLogResponse, header);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void voteResult(String message, String channel){
        try{
            MeogajoaMessage.VoteResultResponse voteResultResponse = objectMapper.readValue(message, MeogajoaMessage.VoteResultResponse.class);
            simpMessagingTemplate.convertAndSend("/topic/game/" + voteResultResponse.getId() + "/notice/system", voteResultResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void eliminatedUser(String message, String channel){
        try{
            MeogajoaMessage.EliminatedUserResponse eliminatedUserResponse = objectMapper.readValue(message, MeogajoaMessage.EliminatedUserResponse.class);
            simpMessagingTemplate.convertAndSend("/topic/user/" + eliminatedUserResponse.getNickname() + "/gameInfo", eliminatedUserResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void reVoteNotice(String message, String channel){
        try{
            MeogajoaMessage.ReVoteNoticeResponse reVoteNoticeResponse = objectMapper.readValue(message, MeogajoaMessage.ReVoteNoticeResponse.class);
            simpMessagingTemplate.convertAndSend("/topic/game/" + reVoteNoticeResponse.getId() + "/notice/system", reVoteNoticeResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void availableVoteCount(String message, String channel){
        try{
            MeogajoaMessage.AvailableVoteCountResponse availableVoteCountResponse = objectMapper.readValue(message, MeogajoaMessage.AvailableVoteCountResponse.class);
            simpMessagingTemplate.convertAndSend("/topic/user/" + availableVoteCountResponse.getUserNickname() + "/gameInfo", availableVoteCountResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


}
