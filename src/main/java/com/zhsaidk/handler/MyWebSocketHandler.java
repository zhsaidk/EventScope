package com.zhsaidk.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhsaidk.database.entity.Catalog;
import com.zhsaidk.database.entity.Project;
import com.zhsaidk.dto.BuildEventDto;
import com.zhsaidk.service.EventService;
import com.zhsaidk.service.CatalogService;
import com.zhsaidk.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class    MyWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;
    private final EventService eventService;
    private Authentication authentication;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String uri = Objects.requireNonNull(session.getUri()).toString();
        String[] pathParts = uri.split("/");

        if (pathParts.length < 4) {
            session.sendMessage(new TextMessage("Invalid URL format"));
            return;
        }

        String projectSlug = pathParts[3];
        String catalogSlug = pathParts[4];

        BuildEventDto eventDto = objectMapper.readValue(message.getPayload(), BuildEventDto.class);

        // Вызываем метод build для создания события
        Object result = eventService.build(eventDto, projectSlug, catalogSlug, authentication);

        // Отправляем результат клиенту
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(result)));
    }

}
