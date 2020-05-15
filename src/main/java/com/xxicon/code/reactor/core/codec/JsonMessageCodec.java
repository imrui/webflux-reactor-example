package com.xxicon.code.reactor.core.codec;

import com.alibaba.fastjson.JSON;
import com.xxicon.code.reactor.core.Message;

public class JsonMessageCodec extends AbstractMessageCodec<String, String> {

    @Override
    public Message decode(String in) {
        try {
            Message m = JSON.parseObject(in, Message.class);
            if (m == null) {
                return null;
            }
            Class<Message> clazz = this.messageMapping.getCommandClass(m.unique());
            return JSON.parseObject(in, clazz);
        } catch (ClassNotFoundException e) {
            this.logger.error("", e);
        }
        return null;
    }

    @Override
    public String encode(Message message) {
        return JSON.toJSONString(message);
    }
}
