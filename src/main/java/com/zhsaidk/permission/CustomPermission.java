package com.zhsaidk.permission;

import org.springframework.security.acls.domain.BasePermission;

public class CustomPermission extends BasePermission {
    public static final CustomPermission READER = new CustomPermission(1 << 0, 'R'); // 1
    public static final CustomPermission WRITER = new CustomPermission(1 << 1, 'W'); // 2

    protected CustomPermission(int mask, char code) {
        super(mask, code);
    }
}