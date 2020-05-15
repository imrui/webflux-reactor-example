package com.xxicon.code.reactor.action;

import com.xxicon.code.reactor.core.Message;
import com.xxicon.code.reactor.core.action.ActionContext;
import com.xxicon.code.reactor.message.request.C001_AliveReqMsg;
import com.xxicon.code.reactor.message.response.C001_AliveRespMsg;

public class C001_AliveAction extends BaseAction<C001_AliveReqMsg> {
    @Override
    public Message execute(ActionContext context, C001_AliveReqMsg req) {
        return new C001_AliveRespMsg();
    }
}
