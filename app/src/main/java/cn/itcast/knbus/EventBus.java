package cn.itcast.knbus;

import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventBus {
    private static EventBus instance = new EventBus();
    private HashMap<Object, List<SubscribeMethod>> cacheMap;
    private final ExecutorService executor;
    private final Handler handler;

    public static EventBus getDefault() {
        return instance;
    }

    private EventBus() {
        this.cacheMap = new HashMap();
        executor = Executors.newCachedThreadPool();
        handler = new Handler();
    }


    public void register(Object activity) {
//        Class<?> aClass = activity.getClass();
        List<SubscribeMethod> list = cacheMap.get(activity);
        //已经注册过不需要重新注册
        if (list == null) {
            list = getSubscribeMethods(activity);
            cacheMap.put(activity, list);
        }
    }

    private List<SubscribeMethod> getSubscribeMethods(Object activity) {

        List<SubscribeMethod> subscribeList = new ArrayList<SubscribeMethod>();
        Class<?> clazz = activity.getClass();
        // 从父类中找
        while (clazz != null) {
            String name = clazz.getName();
            //过滤系统类，Activity java包下的类
            if (name.startsWith("android.") || name.startsWith("java.") || name.startsWith("javax.")) {
                break;
            }
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                //获取带注解的方法
                Subscriber subscriber = method.getAnnotation(Subscriber.class);
                if (subscriber == null)
                    continue;
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length > 1)
                    throw new RuntimeException("EventBus 只能接收一个参数");
                ThreadMode threadMode = subscriber.threadMode();
                SubscribeMethod subscribeMethod = new SubscribeMethod(method, threadMode, parameterTypes[0]);
                subscribeList.add(subscribeMethod);
            }
            //继续查找父类中可以接收事件的方法
            clazz = clazz.getSuperclass();
        }
        return subscribeList;
    }

    /**
     * 发送事件
     */
    public void post(final Object object) {
        //拿到hashMap中键的集合
        Set<Object> objects = cacheMap.keySet();
        //遍历拿到方法集合
        Iterator<Object> iterator = objects.iterator();
        while (iterator.hasNext()) {
            final Object activity = iterator.next();
            List<SubscribeMethod> methods = cacheMap.get(activity);
            for (final SubscribeMethod subscribeMethod :
                    methods) {
//                判断 这个方法是否应该接受事件  要传递的参数和接受的参数是同一个类型，就可以接收事件
                if (subscribeMethod.getEventTypes().isAssignableFrom(object.getClass())) {
                    //是否需要切换线程
                    switch (subscribeMethod.getThreadMode()) {
                        case AsyncThread:
                            /**
                             * Looper.myLopper() 当前post方法所在的线程是否在主线程
                             */
                            if (Looper.myLooper() == Looper.getMainLooper()) {
                                executor.submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        invoke(activity, subscribeMethod, object);
                                    }
                                });
                            }
                        {
                            invoke(activity, subscribeMethod, object);
                        }
                        break;
                        case MainThread:
                            if(Looper.myLooper() == Looper.getMainLooper()){
                                invoke(activity, subscribeMethod, object);
                            }else{
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        invoke(activity, subscribeMethod, object);
                                    }
                                });
                            }
                            break;
                        case PostThread:
                            break;
                    }
                }
            }
        }
    }

    private void invoke(Object activity, SubscribeMethod subscribeMethod, Object object) {
        Method method = subscribeMethod.getMethod();
        try {
            //发送事件
            method.invoke(activity,object);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
