package com.lody.virtual.client.hook.base;

import android.text.TextUtils;
import android.util.Log;

import com.lody.virtual.VALog;
import com.lody.virtual.client.hook.utils.MethodParameterUtils;
import com.lody.virtual.helper.utils.VLog;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lody
 *         <p>
 *         HookHandler uses Java's {@link Proxy} to create a wrapper for existing services.
 *         <p>
 *         When any method is called on the wrapper, it checks if there is any {@link MethodProxy} registered
 *         and enabled for that method. If so, it calls the startUniformer instead of the wrapped implementation.
 *         <p>
 *         The whole thing is managed by a {@link MethodInvocationProxy} subclass
 */
@SuppressWarnings("unchecked")
public class MethodInvocationStub<T> {

    private static final String TAG = MethodInvocationStub.class.getSimpleName();

    //所有需要代理的方法，在初始化对应的xxxMangerStub（示例看ActivityManagerStub）的时候就添加到了这个map
    //mProxyInterface的 HookInvocationHandler 里面会从这个map获取对应MethodProxy，然后调用的对应call方法
    private Map<String, MethodProxy> mInternalMethodProxies = new HashMap<>();
    //系统的真实manager对象
    private T mBaseInterface;
    private T mProxyInterface;
    private String mIdentityName;
    private LogInvocation.Condition mInvocationLoggingCondition = LogInvocation.Condition.NEVER;


    public Map<String, MethodProxy> getAllHooks() {
        return mInternalMethodProxies;
    }

    //利用动态代理hook在调用系统managerService的时候执行自定义实现（MethodProxy）
    /*
    * MethodProxy类是Hook后的代理接口，动态代理中的 call。
    * MethodProxies类是一个个具体的hook系统点
    * 通过本类的addMethodProxy将这些hook点添加过来
    * 传过来的baseInterface其实是一个具体的系统manager，然后通过本类的动态代理，实现调用（就是binder hook，不懂去分析系统的原生getService过程）
    * baseInterface是系统filed或者method的value
    * 更新，ActivityManagerStub为例，baseInterface，传过来的是getDefault这个字段，看源码可以发现，这里就是系统的IActivityManager对象
    *
    * VALog: zzm:android.app.ActivityManagerProxy
    * VALog: zzms:android.app.IActivityManager
    * VALog: zzm... super  com.lody.virtual.client.hook.proxies.am.ActivityManagerStub
    * VALog: zzm... proxiesClass  com.lody.virtual.client.hook.proxies.am.MethodProxies
    * VALog: zzm...  com.lody.virtual.client.hook.proxies.am.MethodProxies$ServiceDoneExecuting
    * VALog: zzm...  com.lody.virtual.client.hook.proxies.am.MethodProxies$CheckGrantUriPermission
    * VALog: zzm... ********还有很多，就看加了什么
    * */
    public MethodInvocationStub(T baseInterface, Class<?>... proxyInterfaces) {
        this.mBaseInterface = baseInterface;
        if (baseInterface != null) {
            if (proxyInterfaces == null) {
                proxyInterfaces = MethodParameterUtils.getAllInterface(baseInterface.getClass());
                VALog.e("zzm:"+baseInterface.getClass().getName());
                for (Class c : proxyInterfaces) {
                    VALog.e("zzms:"+c.getName());
                }
            }
            //这里发生了什么，这里发生了把系统的XXXManager替换为了HookInvocationHandler
            //HookInvocationHandler 又调用了自己实现的methodProxy的call（methodProxy初始化见上面的 mInternalMethodProxies ），call里面又调用了VxxxManager
            //VxxxManager 又IPC调用了VxxxManagerService
            mProxyInterface = (T) Proxy.newProxyInstance(baseInterface.getClass().getClassLoader(), proxyInterfaces, new HookInvocationHandler());
        } else {
            VLog.d(TAG, "Unable to build HookDelegate: %s.", getIdentityName());
        }
    }

    public LogInvocation.Condition getInvocationLoggingCondition() {
        return mInvocationLoggingCondition;
    }

    public void setInvocationLoggingCondition(LogInvocation.Condition invocationLoggingCondition) {
        mInvocationLoggingCondition = invocationLoggingCondition;
    }

    public void setIdentityName(String identityName) {
        this.mIdentityName = identityName;
    }

    public String getIdentityName() {
        if (mIdentityName != null) {
            return mIdentityName;
        }
        return getClass().getSimpleName();
    }

    public MethodInvocationStub(T baseInterface) {
        this(baseInterface, (Class[]) null);
    }

    /**
     * Copy all proxies from the input HookDelegate.
     *
     * @param from the HookDelegate we copy from.
     */
    public void copyMethodProxies(MethodInvocationStub from) {
        this.mInternalMethodProxies.putAll(from.getAllHooks());
    }

    /**
     * Add a method proxy.
     *
     * @param methodProxy proxy
     */
    public MethodProxy addMethodProxy(MethodProxy methodProxy) {
        if (methodProxy != null && !TextUtils.isEmpty(methodProxy.getMethodName())) {
            if (mInternalMethodProxies.containsKey(methodProxy.getMethodName())) {
                VLog.w(TAG, "The Hook(%s, %s) you added has been in existence.", methodProxy.getMethodName(),
                        methodProxy.getClass().getName());
                return methodProxy;
            }
            mInternalMethodProxies.put(methodProxy.getMethodName(), methodProxy);
        }
        return methodProxy;
    }

    /**
     * Remove a method proxy.
     *
     * @param hookName proxy
     * @return The proxy you removed
     */
    public MethodProxy removeMethodProxy(String hookName) {
        return mInternalMethodProxies.remove(hookName);
    }

    /**
     * Remove a method proxy.
     *
     * @param methodProxy target proxy
     */
    public void removeMethodProxy(MethodProxy methodProxy) {
        if (methodProxy != null) {
            removeMethodProxy(methodProxy.getMethodName());
        }
    }

    /**
     * Remove all method proxies.
     */
    public void removeAllMethodProxies() {
        mInternalMethodProxies.clear();
    }

    /**
     * Get the startUniformer by its name.
     *
     * @param name name of the Hook
     * @param <H>  Type of the Hook
     * @return target startUniformer
     */
    @SuppressWarnings("unchecked")
    public <H extends MethodProxy> H getMethodProxy(String name) {
        return (H) mInternalMethodProxies.get(name);
    }

    /**
     * @return Proxy interface
     */
    public T getProxyInterface() {
        return mProxyInterface;
    }

    /**
     * @return Origin Interface
     */
    public T getBaseInterface() {
        return mBaseInterface;
    }

    /**
     * @return count of the hooks
     */
    public int getMethodProxiesCount() {
        return mInternalMethodProxies.size();
    }

    private class HookInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            MethodProxy methodProxy = getMethodProxy(method.getName());
            boolean useProxy = (methodProxy != null && methodProxy.isEnable());
            boolean mightLog = (mInvocationLoggingCondition != LogInvocation.Condition.NEVER) ||
                    (methodProxy != null && methodProxy.getInvocationLoggingCondition() != LogInvocation.Condition.NEVER);

            String argStr = null;
            Object res = null;
            Throwable exception = null;
            if (mightLog) {
                // Arguments to string is done before the method is called because the method might actually change it
                argStr = Arrays.toString(args);
                argStr = argStr.substring(1, argStr.length()-1);
            }


            try {
                //调用MethodProxy的方法
                if (useProxy && methodProxy.beforeCall(mBaseInterface, method, args)) {
                    res = methodProxy.call(mBaseInterface, method, args);
                    res = methodProxy.afterCall(mBaseInterface, method, args, res);
                } else {
                    res = method.invoke(mBaseInterface, args);
                }
                return res;

            } catch (Throwable t) {
                exception = t;
                if (exception instanceof InvocationTargetException && ((InvocationTargetException) exception).getTargetException() != null) {
                    exception = ((InvocationTargetException) exception).getTargetException();
                }
                throw exception;

            } finally {
                if (mightLog) {
                    int logPriority = mInvocationLoggingCondition.getLogLevel(useProxy, exception != null);
                    if (methodProxy != null) {
                        logPriority = Math.max(logPriority, methodProxy.getInvocationLoggingCondition().getLogLevel(useProxy, exception != null));
                    }
                    if (logPriority >= 0) {
                        String retString;
                        if (exception != null) {
                            retString = exception.toString();
                        } else if (method.getReturnType().equals(void.class)) {
                            retString = "void";
                        } else {
                            retString = String.valueOf(res);
                        }

                        Log.println(logPriority, TAG, method.getDeclaringClass().getSimpleName() + "." + method.getName() + "(" + argStr + ") => " + retString);
                    }
                }
            }
        }
    }

    private void dumpMethodProxies() {
        StringBuilder sb = new StringBuilder(50);
        sb.append("*********************");
        for (MethodProxy proxy : mInternalMethodProxies.values()) {
            sb.append(proxy.getMethodName()).append("\n");
        }
        sb.append("*********************");
        VLog.e(TAG, sb.toString());
    }

}
