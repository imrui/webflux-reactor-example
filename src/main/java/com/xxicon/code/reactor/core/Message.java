package com.xxicon.code.reactor.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class Message {
    protected int cmdId;

    @JsonIgnore
    public String unique() {
        return String.valueOf(cmdId);
    }
}
