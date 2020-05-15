package com.xxicon.code.reactor.core.codec;

import com.xxicon.code.reactor.core.Message;

import java.util.Collection;

public interface MessageMapping {
    Class<Message> getCommandClass(String unique) throws ClassNotFoundException;
    Collection<Class<Message>> getAllCommandClass();
    boolean register(Class clazz) throws Exception;
}
