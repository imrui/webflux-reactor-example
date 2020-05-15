package com.xxicon.code.reactor.action;

import com.xxicon.code.reactor.core.Message;
import com.xxicon.code.reactor.core.action.ActionContext;
import com.xxicon.code.reactor.message.request.C002_EchoReqMsg;
import com.xxicon.code.reactor.message.response.C002_EchoRespMsg;

public class C002_EchoAction extends BaseAction<C002_EchoReqMsg> {
    @Override
    public Message execute(ActionContext context, C002_EchoReqMsg req) {
        C002_EchoRespMsg resp = new C002_EchoRespMsg();
        resp.setMessage(req.getMessage());
        return resp;
    }
}
