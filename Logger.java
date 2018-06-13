package eu.chainfire.librootjava;

import android.util.Log;
import java.util.Locale;
import me.zhanghai.android.materialprogressbar.BuildConfig;

public class Logger {
    private static String LOG_TAG = getDefaultLogTag();
    private static boolean log = false;

    private static String getDefaultLogTag() {
        String tag = BuildConfig.APPLICATION_ID;
        while (true) {
            int p = tag.indexOf(46);
            if (p < 0) {
                return tag;
            }
            tag = tag.substring(p + 1);
        }
    }

    public static void setLogTag(String logTag) {
        LOG_TAG = logTag;
    }

    public static String getLogTag() {
        return LOG_TAG;
    }

    public static void setDebugLogging(boolean enabled) {
        log = enabled;
    }

    public static void d(String message, Object... args) {
        if (log) {
            if (args != null && args.length > 0) {
                message = String.format(Locale.ENGLISH, message, args);
            }
            Log.d(LOG_TAG, message);
        }
    }

    public static void dp(String prefix, String message, Object... args) {
        if (log) {
            if (args != null && args.length > 0) {
                message = String.format(Locale.ENGLISH, message, args);
            }
            String str = LOG_TAG;
            Locale locale = Locale.ENGLISH;
            String str2 = "[%s]%s%s";
            Object[] objArr = new Object[3];
            objArr[0] = prefix;
            String str3 = (message.startsWith("[") || message.startsWith(" ")) ? BuildConfig.FLAVOR : " ";
            objArr[1] = str3;
            objArr[2] = message;
            Log.d(str, String.format(locale, str2, objArr));
        }
    }

    public static void ex(Exception e) {
        if (log) {
            dp("EXCEPTION", "%s: %s", e.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
        }
    }

    public static void v(String message, Object... args) {
        Log.v(LOG_TAG, String.format(Locale.ENGLISH, message, args));
    }

    public static void vp(String prefix, String message, Object... args) {
        message = String.format(Locale.ENGLISH, message, args);
        String str = LOG_TAG;
        Locale locale = Locale.ENGLISH;
        String str2 = "[%s]%s%s";
        Object[] objArr = new Object[3];
        objArr[0] = prefix;
        String str3 = (message.startsWith("[") || message.startsWith(" ")) ? BuildConfig.FLAVOR : " ";
        objArr[1] = str3;
        objArr[2] = message;
        Log.v(str, String.format(locale, str2, objArr));
    }

    public static void i(String message, Object... args) {
        Log.i(LOG_TAG, String.format(Locale.ENGLISH, message, args));
    }

    public static void ip(String prefix, String message, Object... args) {
        message = String.format(Locale.ENGLISH, message, args);
        String str = LOG_TAG;
        Locale locale = Locale.ENGLISH;
        String str2 = "[%s]%s%s";
        Object[] objArr = new Object[3];
        objArr[0] = prefix;
        String str3 = (message.startsWith("[") || message.startsWith(" ")) ? BuildConfig.FLAVOR : " ";
        objArr[1] = str3;
        objArr[2] = message;
        Log.i(str, String.format(locale, str2, objArr));
    }

    public static void w(String message, Object... args) {
        Log.w(LOG_TAG, String.format(Locale.ENGLISH, message, args));
    }

    public static void wp(String prefix, String message, Object... args) {
        message = String.format(Locale.ENGLISH, message, args);
        String str = LOG_TAG;
        Locale locale = Locale.ENGLISH;
        String str2 = "[%s]%s%s";
        Object[] objArr = new Object[3];
        objArr[0] = prefix;
        String str3 = (message.startsWith("[") || message.startsWith(" ")) ? BuildConfig.FLAVOR : " ";
        objArr[1] = str3;
        objArr[2] = message;
        Log.w(str, String.format(locale, str2, objArr));
    }

    public static void e(String message, Object... args) {
        Log.e(LOG_TAG, String.format(Locale.ENGLISH, message, args));
    }

    public static void ep(String prefix, String message, Object... args) {
        message = String.format(Locale.ENGLISH, message, args);
        String str = LOG_TAG;
        Locale locale = Locale.ENGLISH;
        String str2 = "[%s]%s%s";
        Object[] objArr = new Object[3];
        objArr[0] = prefix;
        String str3 = (message.startsWith("[") || message.startsWith(" ")) ? BuildConfig.FLAVOR : " ";
        objArr[1] = str3;
        objArr[2] = message;
        Log.e(str, String.format(locale, str2, objArr));
    }
}
