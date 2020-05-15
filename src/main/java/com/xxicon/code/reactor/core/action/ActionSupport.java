package com.xxicon.code.reactor.core.action;

import com.xxicon.code.reactor.core.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ActionSupport<M extends Message> implements Action {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Message execute(ActionContext context) {
        try {
            return execute(context, (M) context.getMessage());
        } catch (Exception e) {
            this.logger.error("", e);
        }
        return null;
    }

    public abstract Message execute(ActionContext context, M req);
}
