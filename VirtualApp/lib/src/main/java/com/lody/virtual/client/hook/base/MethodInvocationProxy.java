package com.lody.virtual.client.hook.base;

import android.content.Context;

import com.lody.virtual.VALog;
import com.lody.virtual.client.core.InvocationStubManager;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.interfaces.IInjector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * @author Lody
 *         <p>
 *         This class is responsible with:
 *         - Instantiating a {@link MethodInvocationStub.HookInvocationHandler} on {@link #getInvocationStub()} ()}
 *         - Install a bunch of {@link MethodProxy}s, either with a @{@link Inject} annotation or manually
 *         calling {@link #addMethodProxy(MethodProxy)} from {@link #onBindMethods()}
 *         - Install the hooked object on the Runtime via {@link #inject()}
 *         <p>
 *         All {@link MethodInvocationProxy}s (plus a couple of other @{@link IInjector}s are installed by
 *         {@link InvocationStubManager}
 *
 * @see Inject
 */

/**
 * 这个类就是初始化所有要代理的方法（MethodProxy），通过addMethodProxy调用mInvocationStub（MethodInvocationStub）的addMethodProxy，
 * 最后存到了MethodInvocationStub的mInternalMethodProxies
 * @param <T>
 */
public abstract class MethodInvocationProxy<T extends MethodInvocationStub> implements IInjector {

    protected T mInvocationStub;

    public MethodInvocationProxy(T invocationStub) {
        this.mInvocationStub = invocationStub;
        onBindMethods();
        afterHookApply(invocationStub);

        LogInvocation loggingAnnotation = getClass().getAnnotation(LogInvocation.class);
        if (loggingAnnotation != null) {
            invocationStub.setInvocationLoggingCondition(loggingAnnotation.value());
        }
    }

    protected void onBindMethods() {

        if (mInvocationStub == null) {
            return;
        }
//        以ActivityManager举例
//        VALog: zzm... super  com.lody.virtual.client.hook.proxies.am.ActivityManagerStub
//        VALog: zzm... proxiesClass  com.lody.virtual.client.hook.proxies.am.MethodProxies
//        VALog: zzm...  com.lody.virtual.client.hook.proxies.am.MethodProxies$ServiceDoneExecuting
//        VALog: zzm...  com.lody.virtual.client.hook.proxies.am.MethodProxies$CheckGrantUriPermission
//        VALog: zzm...  com.lody.virtual.client.hook.proxies.am.MethodProxies$AGetActivityClassForToken
        Class<? extends MethodInvocationProxy> clazz = getClass();
        VALog.e("zzm... super  " + clazz.getName());
        Inject inject = clazz.getAnnotation(Inject.class);
        if (inject != null) {
            Class<?> proxiesClass = inject.value();
            Class<?>[] innerClasses = proxiesClass.getDeclaredClasses();
            VALog.e("zzm... proxiesClass  " + proxiesClass.getName());
            for (Class<?> innerClass : innerClasses) {
                if (!Modifier.isAbstract(innerClass.getModifiers())
                        && MethodProxy.class.isAssignableFrom(innerClass)
                        && innerClass.getAnnotation(SkipInject.class) == null) {
                    addMethodProxy(innerClass);
                    VALog.e("zzm...  " + innerClass.getName());
                }
            }

        }
    }

    private void addMethodProxy(Class<?> hookType) {
        try {
            Constructor<?> constructor = hookType.getDeclaredConstructors()[0];
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            MethodProxy methodProxy;
            if (constructor.getParameterTypes().length == 0) {
                methodProxy = (MethodProxy) constructor.newInstance();
            } else {
                methodProxy = (MethodProxy) constructor.newInstance(this);
            }
            mInvocationStub.addMethodProxy(methodProxy);
        } catch (Throwable e) {
            throw new RuntimeException("Unable to instance Hook : " + hookType + " : " + e.getMessage());
        }
    }

    public MethodProxy addMethodProxy(MethodProxy methodProxy) {
        return mInvocationStub.addMethodProxy(methodProxy);
    }

    protected void afterHookApply(T delegate) {
    }

    @Override
    public abstract void inject() throws Throwable;

    public Context getContext() {
        return VirtualCore.get().getContext();
    }

    public T getInvocationStub() {
        return mInvocationStub;
    }
}
