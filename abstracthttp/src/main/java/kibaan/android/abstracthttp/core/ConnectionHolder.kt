package kibaan.android.abstracthttp.core

/**
 * 実行中の通信オブジェクトを保持するためのコンテナ
 * 通信の一括キャンセルや通信オブジェクトが通信中に解放されないよう保持する役割を持つ
 */
class ConnectionHolder {

    companion object {
        var shared = ConnectionHolder()
    }

    private var connections: MutableList<Connection<*>> = mutableListOf()

    /**
     * 通信オブジェクトを追加する。
     * 既に同じものが保持されている場合は何もしない
     *
     * @param connection 追加する通信オブジェクト
     */
    fun add(connection: Connection<*>) {
        if (!contains(connection = connection)) {
            connections.add(connection)
        }
    }

    /**
     * 通信オブジェクトを削除する。
     *
     * @param connection 削除する通信オブジェクト
     */
    fun remove(connection: Connection<*>?) {
        connections.removeAll { it === connection }
    }

    /**
     * 指定した通信オブジェクトを保持しているか判定する
     *
     * @param connection 判定する通信オブジェクト
     * @return  引数に指定した通信オブエジェクトを保持している場合 `true`
     */
    fun contains(connection: Connection<*>?): Boolean =
        connections.contains(connection)

    /**
     * 保持する全ての通信オブジェクトを削除する。
     */
    fun removeAll() {
        connections.clear()
    }

    /// 保持する全ての通信をキャンセルする
    fun cancelAll() {
        connections.forEach { it.cancel() }
    }
}
