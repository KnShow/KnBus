package cn.itcast.knbus;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Subscriber {
    //默认接收线程在发送者线程
    ThreadMode threadMode()default ThreadMode.PostThread;
}
