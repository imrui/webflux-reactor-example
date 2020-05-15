package com.xxicon.code.reactor.core.action;

import java.util.Collection;

public interface ActionFactory {
    boolean register(String cmdId, Action action);
    Action getAction(String cmdId);
    Collection<Action> actionList();
}
