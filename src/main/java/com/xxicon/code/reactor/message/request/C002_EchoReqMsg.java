package com.xxicon.code.reactor.message.request;

import com.xxicon.code.reactor.core.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class C002_EchoReqMsg extends Message {
    public C002_EchoReqMsg() {
        this.cmdId = 2;
    }
    private String message;
}
