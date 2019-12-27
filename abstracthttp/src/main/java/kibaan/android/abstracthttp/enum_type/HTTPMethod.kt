package kibaan.android.abstracthttp.enum_type

/**
 * HTTPメソッド
 */
enum class HTTPMethod(val rawValue: String) {
    get("get"),
    post("post"),
    put("put"),
    delete("delete"),
    head("head"),
    options("options"),
    trace("trace"),
    connect("connect");

    /**
     * HTTPメソッドの大文字文字列（`GET`など）
     */
    val stringValue: String
        get() = rawValue.toUpperCase()
}
