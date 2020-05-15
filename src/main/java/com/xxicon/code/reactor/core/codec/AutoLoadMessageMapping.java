package com.xxicon.code.reactor.core.codec;

import com.xxicon.code.reactor.core.Message;
import com.xxicon.code.reactor.core.util.RTSI;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.*;

@Slf4j
public class AutoLoadMessageMapping implements MessageMapping {
    private Map<String, Class<Message>> classMap = new HashMap<>();
    @Setter
    private List<String> pkgList = new ArrayList<>();
    @Setter
    private boolean searchSubDir = false;

    @PostConstruct
    public void init() throws Exception {
        Set<Class> clazzSet = RTSI.findClass(this.pkgList, Message.class, this.searchSubDir);
        log.info("load message size = {}", clazzSet.size());
        for (Class clazz : clazzSet) {
            Message msg = (Message) clazz.newInstance();
            this.classMap.put(msg.unique(), clazz);
            log.info("load message: unique = {}, clazz = {}", msg.unique(), clazz.getName());
        }
    }

    @Override
    public Class<Message> getCommandClass(String unique) throws ClassNotFoundException {
        Class<Message> clazz = this.classMap.get(unique);
        if (clazz == null) {
            throw new ClassNotFoundException("unique = " + unique);
        }
        return clazz;
    }

    @Override
    public Collection<Class<Message>> getAllCommandClass() {
        return this.classMap.values();
    }

    @Override
    public boolean register(Class clazz) throws Exception {
        if (!Message.class.isAssignableFrom(clazz)) {
            throw new RuntimeException("class must isAssignableFrom Message.class");
        }
        Message msg = (Message) clazz.newInstance();
        this.classMap.put(msg.unique(), clazz);
        return false;
    }
}
