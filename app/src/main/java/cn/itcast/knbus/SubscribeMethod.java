package cn.itcast.knbus;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;

public class SubscribeMethod {

    private final Method method;
    private final ThreadMode threadMode;
    private final Class<?> eventTypes;

    public Method getMethod() {
        return method;
    }

    public ThreadMode getThreadMode() {
        return threadMode;
    }

    public Class<?> getEventTypes() {
        return eventTypes;
    }

    public SubscribeMethod(Method method, ThreadMode threadMode, Class<?> eventTypes) {
        this.method = method;
        this.threadMode = threadMode;
        this.eventTypes = eventTypes;
    }
}
