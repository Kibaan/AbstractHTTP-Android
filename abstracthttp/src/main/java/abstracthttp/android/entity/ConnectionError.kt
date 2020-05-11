package abstracthttp.android.entity

import abstracthttp.android.enumtype.ConnectionErrorType

/**
 * 通信エラーの情報
 */
data class ConnectionError(
    val type: ConnectionErrorType,
    val nativeError: Exception?
)
