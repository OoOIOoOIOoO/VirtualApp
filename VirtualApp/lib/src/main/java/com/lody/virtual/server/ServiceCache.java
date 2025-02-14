package com.lody.virtual.server;

import android.os.IBinder;

import com.lody.virtual.helper.collection.ArrayMap;

import java.util.Map;

/**
 * @author Lody
 * 所有 VAService 直接继承与 XXX.Stub，也就是 Binder，
 * 并且直接使用了一个 Map 储存在 VAService 进程空间中，并没有注册到系统 AMS 中，
 * 在 VAService 进程中，每个 Service 都被当作一个普通对象 new 和 初始化。
 * 最终，他们被添加到了 ServiceCache
 *
 * 被添加的时机则在 BinderProvider 的 onCreate()中:
 */

public class ServiceCache {

	private static final Map<String, IBinder> sCache = new ArrayMap<>(5);

	public static void addService(String name, IBinder service) {
		sCache.put(name, service);
	}

	public static IBinder removeService(String name) {
		return sCache.remove(name);
	}

	public static IBinder getService(String name) {
		return sCache.get(name);
	}

}
