package kibaan.android.abstracthttp.examples.retry

import abstracthttp.core.Connection
import abstracthttp.core.ConnectionErrorListener
import abstracthttp.entity.ConnectionError
import abstracthttp.entity.Response
import android.content.Context
import androidx.appcompat.app.AlertDialog

class RetryRunner(val context: Context) : ConnectionErrorListener {

    override fun onNetworkError(connection: Connection<*>, error: Exception?) {
        context.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle("通信エラー")
                setMessage("通信に失敗しました。リトライしますか？")
                setPositiveButton("リトライ") { _, _ ->
                    retry(connection)
                }
                setNegativeButton("キャンセル", null)
            }
            builder.create()
        }.show()
    }

    private fun retry(connection: Connection<*>) {
        connection.restart()
    }

    override fun onResponseError(connection: Connection<*>, response: Response) {}

    override fun onParseError(connection: Connection<*>, response: Response, error: Exception) {}

    override fun onValidationError(connection: Connection<*>, response: Response, responseModel: Any) {}

    override fun afterError(connection: Connection<*>, response: Response?, responseModel: Any?, error: ConnectionError) {}

    override fun onCanceled(connection: Connection<*>) {}
}
