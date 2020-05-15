package com.xxicon.code.reactor.message.response;

import com.xxicon.code.reactor.core.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class C002_EchoRespMsg extends Message {
    public C002_EchoRespMsg() {
        this.cmdId = -2;
    }
    private String message;
}
