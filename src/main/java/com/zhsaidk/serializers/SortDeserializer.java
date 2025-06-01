//package com.zhsaidk.serializers;
//
//import com.fasterxml.jackson.core.JacksonException;
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.core.TreeNode;
//import com.fasterxml.jackson.databind.DeserializationContext;
//import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
//import org.springframework.data.domain.Sort;
//
//import java.io.IOException;
//
//public class SortDeserializer extends StdDeserializer<Sort> {
//    protected SortDeserializer() {
//        super(Sort.class);
//    }
//
//    @Override
//    public Sort deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
//        TreeNode node = jsonParser.getCodec().readTree(jsonParser);
//        if ()
//        return null;
//    }
//}
