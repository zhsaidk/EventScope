package com.zhsaidk.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Value
public class CachedPage<T> {
    List<T> content;
    long totalElements;

    @JsonCreator
    public CachedPage(@JsonProperty("content") List<T> content,
                      @JsonProperty("totalElements") long totalElements) {
        this.content = content;
        this.totalElements = totalElements;
    }

    public Page<T> toPage(PageRequest pageRequest) {
        return new PageImpl<>(content, pageRequest, totalElements);
    }
}