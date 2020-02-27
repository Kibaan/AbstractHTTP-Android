package kibaan.android.abstracthttp.examples.mock

import abstracthttp.core.HTTPConnector
import abstracthttp.entity.Request
import abstracthttp.entity.Response

class MockHTTPConnector : HTTPConnector {

    var latestBody: ByteArray? = null

    override fun execute(request: Request, complete: (Response?, Exception?) -> Unit) {
        val mockData = "Mock response".toByteArray(Charsets.UTF_8)
        latestBody = request.body
        val response = Response(data = mockData, statusCode = 200, headers = mapOf(), nativeResponse = null)
        complete(response, null)
    }

    override fun cancel() {}
}
