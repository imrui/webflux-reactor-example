package com.xxicon.code.reactor.core.channel;

import com.xxicon.code.reactor.core.Message;

public interface ChannelSession {
    void send(Message message);
}
