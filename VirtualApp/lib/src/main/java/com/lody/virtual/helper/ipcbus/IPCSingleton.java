package com.lody.virtual.helper.ipcbus;

/**
 * @author Lody
 * 用于IPC通讯的单例类
 * 所有 VXXXManager 都通过这个接口IPC到 VXXXManagerService
 * 说白了，就是所有 com.lody.virtual.client.ipc下面 VXXXManager 的实现都在 com.lody.virtual.server 对应的 VXXXManagerService 里面
 */
public class IPCSingleton<T> {

    private Class<?> ipcClass;
    private T instance;

    public IPCSingleton(Class<?> ipcClass) {
        this.ipcClass = ipcClass;
    }

    public T get() {
        if (instance == null) {
            synchronized (this) {
                if (instance == null) {
                    instance = IPCBus.get(ipcClass);
                }
            }
        }
        return instance;
    }

}
