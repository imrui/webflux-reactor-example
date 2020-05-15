package com.xxicon.code.reactor.core.codec;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMessageCodec<I, O> implements MessageCodec<I, O> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Getter
    @Setter
    protected MessageMapping messageMapping;
}
