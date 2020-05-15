package com.xxicon.code.reactor.core.action;

import com.xxicon.code.reactor.core.Message;
import com.xxicon.code.reactor.core.util.RTSI;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Slf4j
public class AutoLoadActionFactory implements ActionFactory {
    private Map<String, Action> actionMap = new HashMap<>();
    @Setter
    private List<String> pkgList = new ArrayList<>();
    @Setter
    private boolean searchSubDir = false;

    @PostConstruct
    public void init() {
        Set<Class> actionClass = RTSI.findClass(this.pkgList, Action.class, this.searchSubDir);
        log.info("load action size = {}", actionClass.size());
        for (Class clazz : actionClass) {
            try {
                Action action = (Action) clazz.newInstance();
                Class<Message> msgClazz = findGenericMsgClazz(clazz);
                Message reqMsg = msgClazz.newInstance();
                String unique = reqMsg.unique();
                this.actionMap.put(unique, action);
                log.info("load action: unique = {}, clazz = {}", unique, clazz.getName());
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("", e);
            }
        }
    }

    private static Class<Message> findGenericMsgClazz(Class clazz) {
        if (clazz == null
                || clazz.getName().equals(Object.class.getName())
                || clazz.getName().equals(Class.class.getName())) {
            return null;
        }
        Type type = clazz.getGenericSuperclass();
        if (!(type instanceof ParameterizedType)) {
            return findGenericMsgClazz(clazz.getSuperclass()); //获得父类
        }
        ParameterizedType pt = (ParameterizedType) type;
        return (Class<Message>) pt.getActualTypeArguments()[0];
    }

    @Override
    public boolean register(String cmdId, Action action) {
        if (cmdId == null || cmdId.length() == 0 || action == null) {
            return false;
        }
        this.actionMap.put(cmdId, action);
        return true;
    }

    @Override
    public Action getAction(String cmdId) {
        return this.actionMap.get(cmdId);
    }

    @Override
    public Collection<Action> actionList() {
        return this.actionMap.values();
    }
}
