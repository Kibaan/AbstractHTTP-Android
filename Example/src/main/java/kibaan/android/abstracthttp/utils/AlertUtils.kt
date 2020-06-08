package kibaan.android.abstracthttp.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog

/**
 * Alertのユーティリティー
 * Created by yamamoto on 2018/02/09.
 */
object AlertUtils {

    var isEnabled = true
    var defaultErrorTitle = "エラー"
    var defaultCloseLabel = "閉じる"

    private var displayingList: MutableList<AlertDialog> = mutableListOf()

    /**
     * 閉じるだけのアラートを表示する
     */
    fun showNotice(context: Context, title: String, message: String, handler: (() -> Unit)? = null) {
        show(context = context, title = title, message = message)
    }

    /**
     * 閉じるだけのエラーアラートを表示する
     */
    fun showErrorNotice(context: Context, message: String, handler: (() -> Unit)? = null) {
        show(context = context, title = defaultErrorTitle, message = message, handler = handler)
    }

    fun show(
        context: Context, title: String,
        message: String,
        okLabel: String = defaultCloseLabel,
        handler: (() -> Unit)? = null,
        cancelLabel: String? = null,
        cancelHandler: (() -> Unit)? = null
    ): AlertDialog? {

        if (!isEnabled) return null

        val builder = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(okLabel) { _, _ ->
                handler?.invoke()
            }
            .setCancelable(false)

        if (cancelLabel != null) {
            builder.setNegativeButton(cancelLabel) { _, _ ->
                cancelHandler?.invoke()
            }
        }
        val dialog = builder.create()
        displayingList.add(dialog)
        dialog.setOnDismissListener {
            displayingList.remove(dialog)
        }
        dialog.show()
        return dialog
    }

    fun clear() {
        isEnabled = true
    }

    fun dismissAllAlert() {
        displayingList.forEach {
            try {
                it.dismiss()
            } catch (e: Exception) {
                // ダイアログを表示していたアクティビティが終了済みの場合、dismissでExceptionが発生するのでキャッチする
            }
        }
        displayingList.clear()
    }
}