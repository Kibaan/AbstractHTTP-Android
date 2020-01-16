package kibaan.android.abstracthttp.core

import kibaan.android.abstracthttp.defaultimpl.DefaultHTTPConnector
import kibaan.android.abstracthttp.defaultimpl.DefaultURLEncoder

/// 通信のコンフィグ
/// `Connection` オブジェクトの各種初期値を決める
class ConnectionConfig {
    companion object {
        /// 共有オブジェクト
        var shared = ConnectionConfig()
    }

    /// ログ出力を行うか
    var isLogEnabled = true

    /// 標準のHTTPConnector
    var httpConnector: () -> HTTPConnector = { DefaultHTTPConnector() }

    /// 標準のURLEncoder
    var urlEncoder: () -> URLEncoder = { DefaultURLEncoder() }
}
