package fuck.location.xposed.helpers

import android.util.Log

/**
 * 安全的日志工具类，兼容 Xposed 环境和普通 App 环境
 */
object SafeLog {
    private const val TAG = "FL"

    /**
     * 安全地记录日志，在 Xposed 环境中使用 XposedBridge.log，在普通 App 中使用 Android Log
     */
    fun d(message: String) {
        Log.d(TAG, message)
        try {
            // 尝试使用 XposedBridge.log，如果类不存在会抛出异常
            Class.forName("de.robv.android.xposed.XposedBridge")
            de.robv.android.xposed.XposedBridge.log("$TAG: $message")
        } catch (e: ClassNotFoundException) {
            // 在普通 App 环境中，只使用 Android Log
        } catch (e: NoClassDefFoundError) {
            // 在普通 App 环境中，只使用 Android Log
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(TAG, message, throwable)
        } else {
            Log.e(TAG, message)
        }
        try {
            Class.forName("de.robv.android.xposed.XposedBridge")
            de.robv.android.xposed.XposedBridge.log("$TAG: $message")
            throwable?.printStackTrace()
        } catch (e: ClassNotFoundException) {
            // 在普通 App 环境中，只使用 Android Log
        } catch (e: NoClassDefFoundError) {
            // 在普通 App 环境中，只使用 Android Log
        }
    }
}
