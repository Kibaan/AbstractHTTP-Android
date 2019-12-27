package kibaan.android.abstracthttp.entity

import kibaan.android.abstracthttp.enum_type.ConnectionErrorType

/**
 * 通信エラーの情報
 */
data class ConnectionError(
    val type: ConnectionErrorType,
    val nativeError: Error?
)
