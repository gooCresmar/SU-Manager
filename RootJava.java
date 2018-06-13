package eu.chainfire.librootjava;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION;
import android.system.Os;
import dalvik.system.BaseDexClassLoader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import me.zhanghai.android.materialprogressbar.BuildConfig;

public class RootJava {
    @SuppressLint({"SdCardPath"})
    public static String getLibraryPath(Context context, String libname, String defaultValue) {
        if (libname.startsWith("lib")) {
            libname = libname.substring(3);
        }
        String fn = defaultValue;
        if (context.getClassLoader() instanceof BaseDexClassLoader) {
            return ((BaseDexClassLoader) context.getClassLoader()).findLibrary(libname);
        }
        return fn;
    }

    public static boolean haveLinkerNamespaces() {
        return (VERSION.SDK_INT == 23 && VERSION.PREVIEW_SDK_INT != 0) || VERSION.SDK_INT > 23;
    }

    public static boolean guessIfAppProcessIs64Bits(String app_process) {
        String compare = app_process;
        int sep = compare.lastIndexOf(47);
        if (sep >= 0) {
            compare = compare.substring(sep + 1);
        }
        if (compare.contains("32")) {
            return false;
        }
        if (compare.contains("64")) {
            return true;
        }
        try {
            compare = new File(app_process).getCanonicalFile().getName();
            if (compare.contains("32") || !compare.contains("64")) {
                return false;
            }
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    public static String getLaunchString(Context context, Class<?> clazz, String app_process, String[] params) {
        return getLaunchString(context, (Class) clazz, app_process, params, null);
    }

    public static String getLaunchString(Context context, Class<?> clazz, String app_process, String[] params, String niceName) {
        return getLaunchString(context.getPackageCodePath(), clazz.getName(), app_process, params, niceName);
    }

    public static String getLaunchString(String packageCodePath, Class<?> clazz, String app_process, String[] params) {
        return getLaunchString(packageCodePath, (Class) clazz, app_process, params, null);
    }

    public static String getLaunchString(String packageCodePath, Class<?> clazz, String app_process, String[] params, String niceName) {
        return getLaunchString(packageCodePath, clazz.getName(), app_process, params, niceName);
    }

    public static String getAppProcessDefault() {
        if (new File("/system/bin/app_process64_original").exists()) {
            return "/system/bin/app_process64_original";
        }
        if (new File("/system/bin/app_process32_original").exists()) {
            return "/system/bin/app_process32_original";
        }
        if (new File("/system/bin/app_process_original").exists()) {
            return "/system/bin/app_process_original";
        }
        if (new File("/system/bin/app_process").exists()) {
            return "/system/bin/app_process";
        }
        return null;
    }

    public static String getAppProcess32Bit() {
        String app_process = getAppProcessDefault();
        if (new File("/system/bin/app_process32_original").exists()) {
            return "/system/bin/app_process32_original";
        }
        if (new File("/system/bin/app_process32").exists()) {
            return "/system/bin/app_process32";
        }
        return app_process;
    }

    public static String getAppProcess64Bit() {
        if (new File("/system/bin/app_process64_original").exists()) {
            return "/system/bin/app_process64_original";
        }
        if (new File("/system/bin/app_process64").exists()) {
            return "/system/bin/app_process64";
        }
        return null;
    }

    public static String getAppProcessMostBits() {
        String ret = getAppProcess64Bit();
        if (ret == null) {
            return getAppProcess32Bit();
        }
        return ret;
    }

    public static String getAppProcessRelocate(String appProcessBase, List<String> preLaunch, List<String> postLaunch, String suffix) {
        String appProcessCopy;
        if (guessIfAppProcessIs64Bits(appProcessBase)) {
            appProcessCopy = "/dev/.app_process_64_" + suffix;
        } else {
            appProcessCopy = "/dev/.app_process_" + suffix;
        }
        String box = VERSION.SDK_INT < 23 ? "toolbox" : "toybox";
        preLaunch.add(String.format(Locale.ENGLISH, "%s cp %s %s", new Object[]{box, appProcessBase, appProcessCopy}));
        preLaunch.add(String.format(Locale.ENGLISH, "%s chmod 0700 %s", new Object[]{box, appProcessCopy}));
        postLaunch.add(String.format(Locale.ENGLISH, "%s rm %s", new Object[]{box, appProcessCopy}));
        return appProcessCopy;
    }

    @TargetApi(21)
    private static String getenv(String name) {
        if (haveLinkerNamespaces()) {
            return Os.getenv(name);
        }
        return System.getenv(name);
    }

    @TargetApi(21)
    private static void setenv(String name, String value) {
        if (!haveLinkerNamespaces()) {
            return;
        }
        if (value == null) {
            try {
                Os.unsetenv(name);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        Os.setenv(name, value, true);
    }

    public static String getPatchedLdLibraryPath(boolean use64bit, String[] extraPaths) {
        int i = 0;
        String LD_LIBRARY_PATH = getenv("LD_LIBRARY_PATH");
        if (haveLinkerNamespaces()) {
            int length;
            StringBuilder paths = new StringBuilder();
            for (String path : use64bit ? new String[]{"/system/lib64", "/data/lib64", "/vendor/lib64", "/data/vendor/lib64"} : new String[]{"/system/lib", "/data/lib", "/vendor/lib", "/data/vendor/lib"}) {
                File file = new File(path);
                if (file.exists()) {
                    try {
                        paths.append(file.getCanonicalPath());
                        paths.append(':');
                        File[] files = file.listFiles();
                        if (files != null) {
                            for (File dir : files) {
                                if (dir.isDirectory()) {
                                    paths.append(dir.getCanonicalPath());
                                    paths.append(':');
                                }
                            }
                        }
                    } catch (IOException e) {
                    }
                }
            }
            if (extraPaths != null) {
                length = extraPaths.length;
                while (i < length) {
                    paths.append(extraPaths[i]);
                    paths.append(':');
                    i++;
                }
            }
            paths.append("/librootjava");
            if (LD_LIBRARY_PATH != null) {
                paths.append(':');
                paths.append(LD_LIBRARY_PATH);
            }
            return paths.toString();
        } else if (LD_LIBRARY_PATH != null) {
            return LD_LIBRARY_PATH;
        } else {
            return null;
        }
    }

    public static String getOriginalLdLibraryPath() {
        String LD_LIBRARY_PATH = System.getenv("LD_LIBRARY_PATH");
        if (LD_LIBRARY_PATH == null) {
            return null;
        }
        if (LD_LIBRARY_PATH.endsWith(":/librootjava")) {
            return null;
        }
        if (LD_LIBRARY_PATH.contains(":/librootjava:")) {
            return LD_LIBRARY_PATH.substring(LD_LIBRARY_PATH.indexOf(":/librootjava:") + ":/librootjava:".length());
        }
        return LD_LIBRARY_PATH;
    }

    public static void restoreOriginalLdLibraryPath() {
        setenv("LD_LIBRARY_PATH", getOriginalLdLibraryPath());
    }

    public static String getLaunchString(String packageCodePath, String clazz, String app_process, String[] params, String niceName) {
        String ANDROID_ROOT = System.getenv("ANDROID_ROOT");
        StringBuilder prefix = new StringBuilder();
        if (ANDROID_ROOT != null) {
            prefix.append("ANDROID_ROOT=");
            prefix.append(ANDROID_ROOT);
            prefix.append(' ');
        }
        String[] extraPaths = null;
        if (app_process.lastIndexOf(47) >= 0) {
            extraPaths = new String[]{app_process.substring(0, app_process.lastIndexOf(47))};
        }
        String LD_LIBRARY_PATH = getPatchedLdLibraryPath(guessIfAppProcessIs64Bits(app_process), extraPaths);
        if (LD_LIBRARY_PATH != null) {
            prefix.append("LD_LIBRARY_PATH=");
            prefix.append(LD_LIBRARY_PATH);
            prefix.append(' ');
        }
        if (niceName == null) {
            niceName = BuildConfig.FLAVOR;
        } else {
            niceName = " --nice-name=" + niceName;
        }
        String ret = String.format("NO_ADDR_COMPAT_LAYOUT_FIXUP=1 %sCLASSPATH=%s %s /system/bin%s %s", new Object[]{prefix.toString(), packageCodePath, app_process, niceName, clazz});
        if (params == null) {
            return ret;
        }
        StringBuilder full = new StringBuilder(ret);
        for (String param : params) {
            full.append(' ');
            full.append(param);
        }
        return full.toString();
    }
}
