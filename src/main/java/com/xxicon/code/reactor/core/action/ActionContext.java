package com.xxicon.code.reactor.core.action;

import com.xxicon.code.reactor.core.Message;
import com.xxicon.code.reactor.core.channel.ChannelSession;
import lombok.Data;

@Data
public class ActionContext {
    private String actionKey;
    private Action action;
    private ChannelSession channelSession;
    private Message message;
    private Message result;
}
