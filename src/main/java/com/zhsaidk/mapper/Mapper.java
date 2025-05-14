package com.zhsaidk.mapper;

public interface Mapper<F, T> {
    T map(F object);
}
