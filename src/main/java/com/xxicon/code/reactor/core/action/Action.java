package com.xxicon.code.reactor.core.action;

import com.xxicon.code.reactor.core.Message;

public interface Action {
    Message execute(ActionContext context);
}
