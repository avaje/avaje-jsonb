package org.example.customer.enums;

import io.avaje.jsonb.Json;

public enum StaffStatus {
    NORMAL(1),
    ;
    public final int code;

    StaffStatus(int code) {
        this.code = code;
    }

    @Json.Value
    public int getCode() {
        return code;
    }
}
