package com.bamboo.core.util;

import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class JsonConvertor {

    private static ObjectMapper mapper = new ObjectMapper();
    private static TypeReference<Map<String, Object>> typeRefStringObject = new TypeReference<Map<String, Object>>() {};


    public static <T> T convertJsonToObject(java.lang.String content, java.lang.Class<T> valueType) throws IOException{
        return mapper.readValue(content,valueType);
    }

    public static Map<String, Object> convertJsonToMap(String jsonString){
        try {
            return JsonConvertor.mapper.readValue(jsonString, typeRefStringObject);
        } catch (Exception e) {
            log.error("can't convert to Map , object:[%s]");
        }
        return null;
    }
}
