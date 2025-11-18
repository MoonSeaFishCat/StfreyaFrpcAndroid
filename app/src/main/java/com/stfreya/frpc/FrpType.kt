package com.stfreya.frpc

import android.content.Context
import java.io.File

enum class FrpType(val typeName: String) {
    FRPC("frpc");

    fun getDir(context: Context): File {
        return File(context.filesDir, this.typeName)
    }

    fun getLibName(): String {
        return BuildConfig.FrpcFileName
    }

    fun getAutoStartPreferencesKey(): String {
        return PreferencesKey.AUTO_START_FRPC_LIST
    }

    fun getConfigAssetsName(): String {
        return BuildConfig.FrpcConfigFileName
    }
}