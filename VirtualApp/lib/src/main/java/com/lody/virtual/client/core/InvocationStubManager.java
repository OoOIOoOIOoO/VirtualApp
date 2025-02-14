package com.lody.virtual.client.core;

import android.os.Build;

import com.lody.virtual.client.hook.base.MethodInvocationProxy;
import com.lody.virtual.client.hook.base.MethodInvocationStub;
import com.lody.virtual.client.hook.delegate.AppInstrumentation;
import com.lody.virtual.client.hook.proxies.account.AccountManagerStub;
import com.lody.virtual.client.hook.proxies.alarm.AlarmManagerStub;
import com.lody.virtual.client.hook.proxies.am.ActivityManagerStub;
import com.lody.virtual.client.hook.proxies.am.HCallbackStub;
import com.lody.virtual.client.hook.proxies.appops.AppOpsManagerStub;
import com.lody.virtual.client.hook.proxies.appwidget.AppWidgetManagerStub;
import com.lody.virtual.client.hook.proxies.audio.AudioManagerStub;
import com.lody.virtual.client.hook.proxies.backup.BackupManagerStub;
import com.lody.virtual.client.hook.proxies.bluetooth.BluetoothStub;
import com.lody.virtual.client.hook.proxies.clipboard.ClipBoardStub;
import com.lody.virtual.client.hook.proxies.connectivity.ConnectivityStub;
import com.lody.virtual.client.hook.proxies.content.ContentServiceStub;
import com.lody.virtual.client.hook.proxies.context_hub.ContextHubServiceStub;
import com.lody.virtual.client.hook.proxies.devicepolicy.DevicePolicyManagerStub;
import com.lody.virtual.client.hook.proxies.display.DisplayStub;
import com.lody.virtual.client.hook.proxies.dropbox.DropBoxManagerStub;
import com.lody.virtual.client.hook.proxies.fingerprint.FingerprintManagerStub;
import com.lody.virtual.client.hook.proxies.graphics.GraphicsStatsStub;
import com.lody.virtual.client.hook.proxies.imms.MmsStub;
import com.lody.virtual.client.hook.proxies.input.InputMethodManagerStub;
import com.lody.virtual.client.hook.proxies.isms.ISmsStub;
import com.lody.virtual.client.hook.proxies.isub.ISubStub;
import com.lody.virtual.client.hook.proxies.job.JobServiceStub;
import com.lody.virtual.client.hook.proxies.libcore.LibCoreStub;
import com.lody.virtual.client.hook.proxies.location.LocationManagerStub;
import com.lody.virtual.client.hook.proxies.media.router.MediaRouterServiceStub;
import com.lody.virtual.client.hook.proxies.media.session.SessionManagerStub;
import com.lody.virtual.client.hook.proxies.mount.MountServiceStub;
import com.lody.virtual.client.hook.proxies.network.NetworkManagementStub;
import com.lody.virtual.client.hook.proxies.notification.NotificationManagerStub;
import com.lody.virtual.client.hook.proxies.persistent_data_block.PersistentDataBlockServiceStub;
import com.lody.virtual.client.hook.proxies.phonesubinfo.PhoneSubInfoStub;
import com.lody.virtual.client.hook.proxies.pm.PackageManagerStub;
import com.lody.virtual.client.hook.proxies.power.PowerManagerStub;
import com.lody.virtual.client.hook.proxies.restriction.RestrictionStub;
import com.lody.virtual.client.hook.proxies.search.SearchManagerStub;
import com.lody.virtual.client.hook.proxies.shortcut.ShortcutServiceStub;
import com.lody.virtual.client.hook.proxies.telephony.TelephonyRegistryStub;
import com.lody.virtual.client.hook.proxies.telephony.TelephonyStub;
import com.lody.virtual.client.hook.proxies.usage.UsageStatsManagerStub;
import com.lody.virtual.client.hook.proxies.user.UserManagerStub;
import com.lody.virtual.client.hook.proxies.vibrator.VibratorStub;
import com.lody.virtual.client.hook.proxies.view.AutoFillManagerStub;
import com.lody.virtual.client.hook.proxies.wifi.WifiManagerStub;
import com.lody.virtual.client.hook.proxies.wifi_scanner.WifiScannerStub;
import com.lody.virtual.client.hook.proxies.window.WindowManagerStub;
import com.lody.virtual.client.interfaces.IInjector;
import com.lody.virtual.server.BinderProvider;

import java.util.HashMap;
import java.util.Map;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;

/**
 * @author Lody
 *
 */
public final class InvocationStubManager {

    private static InvocationStubManager sInstance = new InvocationStubManager();
    private static boolean sInit;

	private Map<Class<?>, IInjector> mInjectors = new HashMap<>(13);

	private InvocationStubManager() {
	}

	public static InvocationStubManager getInstance() {
		return sInstance;
	}

	void injectAll() throws Throwable {
		for (IInjector injector : mInjectors.values()) {
			injector.inject();
		}
		// XXX: Lazy inject the Instrumentation,
		addInjector(AppInstrumentation.getDefault());
	}

    /**
	 * @return if the InvocationStubManager has been initialized.
	 */
	public boolean isInit() {
		return sInit;
	}


	public void init() throws Throwable {
		if (isInit()) {
			throw new IllegalStateException("InvocationStubManager Has been initialized.");
		}
		injectInternal();
		sInit = true;

	}

	/**
	 * 原生manager的hook过程
	 * @throws Throwable
	 */
	private void injectInternal() throws Throwable {
		//va里面存着四种进程，请看VirtualCore的ProcessType
		//MainProcess即io.virtualapp：这是VirtualApp的宿主界面存在的进程。负责显示已安装目标应用，选择及启动需要双开的目标应用等。
		//ServiceProcess即io.virtualapp:x：后台服务进程，va的server端，clinet通过binder和server进行通信。如：BinderProvider、DaemonService等。
		//ClientProcess(VAppProcess)即io.virtualapp:p*：目标应用运行进程，这个进程提供StubActivity/StubDialog/StubContentProvider子类负责启动目标应用对应的组件。
		//但是ProcessType的CHILD，没太明白，理解是va自己的子进程，没看到使用

		// VA自身的App进程不需要 Hook
		// hook的过程见ActivityManagerStub注释
		if (VirtualCore.get().isMainProcess()) {
			return;
		}
		//VAService要Hook AMS和PMS
		if (VirtualCore.get().isServerProcess()) {
			addInjector(new ActivityManagerStub());
			addInjector(new PackageManagerStub());
			return;
		}
		//Client APP要Hook,AMS,PMS,WMS 整个framework使其调用到VA的framework
		if (VirtualCore.get().isVAppProcess()) {
			addInjector(new LibCoreStub());
			addInjector(new ActivityManagerStub());
			addInjector(new PackageManagerStub());
			addInjector(HCallbackStub.getDefault());
			addInjector(new ISmsStub());
			addInjector(new ISubStub());
			addInjector(new DropBoxManagerStub());
			addInjector(new NotificationManagerStub());
			addInjector(new LocationManagerStub());
			addInjector(new WindowManagerStub());
			addInjector(new ClipBoardStub());
			addInjector(new MountServiceStub());
			addInjector(new BackupManagerStub());
			addInjector(new TelephonyStub());
			addInjector(new TelephonyRegistryStub());
			addInjector(new PhoneSubInfoStub());
			addInjector(new PowerManagerStub());
			addInjector(new AppWidgetManagerStub());
			addInjector(new AccountManagerStub());
			addInjector(new AudioManagerStub());
			addInjector(new SearchManagerStub());
			addInjector(new ContentServiceStub());
			addInjector(new ConnectivityStub());

			if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR2) {
				addInjector(new VibratorStub());
				addInjector(new WifiManagerStub());
				addInjector(new BluetoothStub());
				addInjector(new ContextHubServiceStub());
			}
			if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR1) {
				addInjector(new UserManagerStub());
			}

			if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR1) {
				addInjector(new DisplayStub());
			}
			if (Build.VERSION.SDK_INT >= LOLLIPOP) {
				addInjector(new PersistentDataBlockServiceStub());
				addInjector(new InputMethodManagerStub());
				addInjector(new MmsStub());
				addInjector(new SessionManagerStub());
				addInjector(new JobServiceStub());
				addInjector(new RestrictionStub());
			}
			if (Build.VERSION.SDK_INT >= KITKAT) {
				addInjector(new AlarmManagerStub());
				addInjector(new AppOpsManagerStub());
				addInjector(new MediaRouterServiceStub());
			}
			if (Build.VERSION.SDK_INT >= LOLLIPOP_MR1) {
				addInjector(new GraphicsStatsStub());
				addInjector(new UsageStatsManagerStub());
			}
			if (Build.VERSION.SDK_INT >= M) {
				addInjector(new FingerprintManagerStub());
				addInjector(new NetworkManagementStub());
			}
			if (Build.VERSION.SDK_INT >= N) {
                addInjector(new WifiScannerStub());
                addInjector(new ShortcutServiceStub());
                addInjector(new DevicePolicyManagerStub());
            }
            if (Build.VERSION.SDK_INT >= 26) {
				addInjector(new AutoFillManagerStub());
			}
		}
	}

	private void addInjector(IInjector IInjector) {
		mInjectors.put(IInjector.getClass(), IInjector);
	}

	public <T extends IInjector> T findInjector(Class<T> clazz) {
		// noinspection unchecked
		return (T) mInjectors.get(clazz);
	}

	public <T extends IInjector> void checkEnv(Class<T> clazz) {
		IInjector IInjector = findInjector(clazz);
		if (IInjector != null && IInjector.isEnvBad()) {
			try {
				IInjector.inject();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public <T extends IInjector, H extends MethodInvocationStub> H getInvocationStub(Class<T> injectorClass) {
		T injector = findInjector(injectorClass);
		if (injector != null && injector instanceof MethodInvocationProxy) {
			// noinspection unchecked
			return (H) ((MethodInvocationProxy) injector).getInvocationStub();
		}
		return null;
	}

}