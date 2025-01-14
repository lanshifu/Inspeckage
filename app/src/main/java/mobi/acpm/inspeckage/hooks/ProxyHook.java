package mobi.acpm.inspeckage.hooks;

import android.os.Build;
import android.util.Log;

import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

import java.net.URI;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.preferences.InspeckagePreferences;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by acpm on 21/11/15.
 */
public class ProxyHook extends XC_MethodHook {

    public static final String TAG = "Inspeckage_Proxy:";
    private static InspeckagePreferences sPrefs;


    public static void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam,InspeckagePreferences prefs) {
        sPrefs = prefs;

        XC_MethodHook ProxySelectorHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                if (sPrefs.getBoolean("switch_proxy", false)) {

                    System.setProperty("proxyHost", sPrefs.getString("host", ""));
                    System.setProperty("proxyPort", sPrefs.getString("port", ""));

                    System.setProperty("http.proxyHost", sPrefs.getString("host", ""));
                    System.setProperty("http.proxyPort", sPrefs.getString("port", ""));

                    System.setProperty("https.proxyHost", sPrefs.getString("host", ""));
                    System.setProperty("https.proxyPort", sPrefs.getString("port", ""));

                    System.setProperty("socksProxyHost", sPrefs.getString("host", ""));
                    System.setProperty("socksProxyPort", sPrefs.getString("port", ""));


                    URI uri = (URI) param.args[0];

                    XposedBridge.log(TAG + " [P:" + sPrefs.getString("host", "") + ":" + sPrefs.getString("port", "") + "] - URI = " + uri);
                }
            }
        };

        try {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                findAndHookMethod("java.net.ProxySelectorImpl", loadPackageParam.classLoader, "select", URI.class, ProxySelectorHook);
            } else {
                findAndHookMethod("sun.net.spi.DefaultProxySelector", loadPackageParam.classLoader, "select", URI.class, ProxySelectorHook);
            }
        } catch (Error e) {
            Module.logError(e);
        }

//        try {
//            Class<?> DefaultHttpClient = XposedHelpers.findClass("org.apache.http.impl.client.DefaultHttpClient", loadPackageParam.classLoader);
//            hookAllConstructors(DefaultHttpClient, new XC_MethodHook() {
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//
//                    if (sPrefs.getBoolean("switch_proxy", false)) {
//                        String proxyHost = sPrefs.getString("host", null);
//                        int proxyPort;
//                        try {
//                            proxyPort = Integer.parseInt(sPrefs.getString("port", null));
//                        } catch (NumberFormatException ex) {
//                            proxyPort = -1;
//                        }
//
//                        DefaultHttpClient httpClient = (DefaultHttpClient) param.thisObject;
//                        HttpHost proxy = new HttpHost(proxyHost, proxyPort);
//                        httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
//                    }
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e(TAG, "initAllHooks: e=" +e.getMessage());
//        }

    }
}
