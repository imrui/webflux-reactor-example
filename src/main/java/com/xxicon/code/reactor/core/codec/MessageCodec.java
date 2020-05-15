package com.xxicon.code.reactor.core.codec;

import com.xxicon.code.reactor.core.Message;

public interface MessageCodec<I,O> {
    Message decode(I in);
    O encode(Message message);
}
