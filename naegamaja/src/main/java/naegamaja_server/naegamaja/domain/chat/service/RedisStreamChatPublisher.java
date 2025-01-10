package naegamaja_server.naegamaja.domain.chat.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import naegamaja_server.naegamaja.domain.chat.entity.ChatLog;
import naegamaja_server.naegamaja.system.websocket.dto.Message;
import naegamaja_server.naegamaja.system.websocket.model.MessageType;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisStreamChatPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final String ROOM_CHAT_STREAM_KEY = "stream:chat:room:";

    public void publishChatMessage(String roomId, Message.Request message) {
        try {
            if(!message.getType().equals(MessageType.CHAT)) return;
            Map<String, Object> messageMap = objectMapper.convertValue(message, new TypeReference<Map<String, Object>>() {});
            redisTemplate.opsForStream().add(ROOM_CHAT_STREAM_KEY + roomId, messageMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
