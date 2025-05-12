package com.zhsaidk.config;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.convert.converter.Converter;

import java.util.UUID;

public class CustomAclConversionService {

    public static ConversionService createConversionService() {
        GenericConversionService conversionService = new GenericConversionService();

        conversionService.addConverter(new Converter<UUID, String>() {
            @Override
            public String convert(UUID source) {
                return source.toString();
            }
        });

        conversionService.addConverter(new Converter<Integer, String>() {
            @Override
            public String convert(Integer source) {
                return source.toString();
            }
        });

        conversionService.addConverter(new Converter<String, String>() {
            @Override
            public String convert(String source) {
                return source;
            }
        });

        return conversionService;
    }
}