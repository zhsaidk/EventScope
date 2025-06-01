package com.zhsaidk.serializers;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.List;

public class PageImplDeserializer extends StdDeserializer<PageImpl> {

    public PageImplDeserializer(){
        super(PageImpl.class);
    }

    @Override
    public PageImpl deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        // Извлекаем content
        JsonNode contentNode = node.get("content");
        List<Object> content = ctxt.readValue(contentNode.traverse(p.getCodec()), List.class);

        // Извлекаем page
        JsonNode pageNode = node.get("page");
        int pageNumber = pageNode.get("number").asInt(0);
        int pageSize = pageNode.get("size").asInt(10);
        long totalElements = pageNode.get("totalElements").asLong(0);

        // Создаём PageRequest (без сортировки для простоты)
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, Sort.unsorted());

        return new PageImpl<>(content, pageRequest, totalElements);
    }
}
