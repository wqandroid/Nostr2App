package nostr.postr

import com.google.gson.JsonElement
import nostr.postr.events.Event
import okhttp3.*
import kotlin.collections.HashSet

class Relay(
    val url: String,
    var read: Boolean = true,
    var write: Boolean = true,
    var enable: Boolean = read && write
) {
    private val httpClient = OkHttpClient()
    private val listeners = HashSet<Listener>()
    private lateinit var socket: WebSocket

    fun register(listener: Listener) {
        listeners.add(listener)
    }

    fun unregister(listener: Listener) = listeners.remove(listener)

    fun requestAndWatch(subscriptionId: String, reconnectTs: Long? = null) {
        val request = Request.Builder().url(url).build()
        val listener = object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                sendFilter(requestId = subscriptionId, reconnectTs = reconnectTs)
                listeners.forEach { it.onRelayStateChange(this@Relay, Type.CONNECT) }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
//                    println("------------------")
//                    println(text)
//                    println("------------------")
                    val msg = Event.gson.fromJson(text, JsonElement::class.java).asJsonArray
                    val type = msg[0].asString
                    val channel = msg[1].asString

                    when (type) {
                        "EVENT" -> {
                            val event = Event.fromJson(msg[2], Client.lenient)
                            listeners.forEach { it.onEvent(this@Relay, subscriptionId, event) }
                        }
                        "EOSE" -> listeners.forEach {
                            it.onRelayStateChange(this@Relay, Type.EOSE)
                        }
                        "NOTICE" -> listeners.forEach {
                            // "channel" being the second string in the string array ...
                            it.onError(
                                this@Relay,
                                subscriptionId,
                                Error("Relay sent notice: $channel")
                            )
                        }
                        else -> listeners.forEach {
                            it.onError(
                                this@Relay,
                                subscriptionId,
                                Error("Unknown type $type on channel $channel. Msg was $text")
                            )
                        }
                    }
                } catch (t: Throwable) {
                    text.chunked(2000) { chunked ->
                        listeners.forEach {
                            it.onError(
                                this@Relay,
                                subscriptionId,
                                Error("Problem with $chunked")
                            )
                        }
                    }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                listeners.forEach { it.onRelayStateChange(this@Relay, Type.DISCONNECT) }

            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                listeners.forEach {
                    it.onError(this@Relay, subscriptionId, Error("WebSocket Failure", t))
                }
            }
        }
        socket = httpClient.newWebSocket(request, listener)

    }

    fun disconnect() {
        httpClient.dispatcher.executorService.shutdown()
        socket.close(1000, "Normal close")
    }

    fun sendFilter(requestId: String, reconnectTs: Long? = null) {
        val filters = if (reconnectTs != null) {
            Client.subscriptions[requestId]?.let {
                it.map { filter ->
                    JsonFilter(
                        filter.ids,
                        filter.authors,
                        filter.kinds,
                        filter.tags,
                        since = reconnectTs
                    )
                }
            } ?: error("No filter(s) found.")
            //Client.filters.map { JsonFilter(it.ids, it.authors, it.kinds, it.tags, since = reconnectTs) }
        } else {
            Client.subscriptions[requestId] ?: error("No filter(s) found.")
        }
        val request = """["REQ","$requestId",${filters.joinToString(",") { it.toJson() }}]"""
        print("ws_send_request_msg----$request")
        socket.send(request)
    }

    fun send(signedEvent: Event) {
        print("ws_send_request_msg----${signedEvent.toJson()}")
        socket.send("""["EVENT",${signedEvent.toJson()}]""")
    }

    fun close(subscriptionId: String) {
        socket.send("""["CLOSE","$subscriptionId"]""")
    }

    enum class Type {
        // Websocket connected
        CONNECT,

        // Websocket disconnected
        DISCONNECT,

        // End Of Stored Events
        EOSE
    }

    interface Listener {
        /**
         * A new message was received
         */
        fun onEvent(relay: Relay, subscriptionId: String, event: Event)

        fun onError(relay: Relay, subscriptionId: String, error: Error)

        /**
         * Connected to or disconnected from a relay
         *
         * @param type is 0 for disconnect and 1 for connect
         */
        fun onRelayStateChange(relay: Relay, type: Type)
    }
}
