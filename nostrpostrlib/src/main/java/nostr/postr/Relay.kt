package nostr.postr

import com.google.gson.JsonElement
import nostr.postr.events.ContactListEvent
import nostr.postr.events.Event
import nostr.postr.events.MetadataEvent
import nostr.postr.events.PrivateDmEvent
import okhttp3.*
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.TimeUnit
import kotlin.collections.HashSet

class Relay(
    val url: String,
    var read: Boolean = true,
    var write: Boolean = true,
    var enable: Boolean = read && write
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(100, TimeUnit.SECONDS)
        .readTimeout(100, TimeUnit.SECONDS)
        .callTimeout(100, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private var isOpen = false


    private val sendFailedMsgSet = linkedSetOf<String>()

    internal val subscriptions: MutableMap<String, MutableList<JsonFilter>> = mutableMapOf()
    private val listeners = mutableSetOf<Client.Listener>()
    private lateinit var socket: WebSocket

    fun register(listener: Client.Listener) {
        listeners.add(listener)
    }


    fun unregister(listener: Client.Listener) = listeners.remove(listener)

    fun connection() {
        justConnection()
    }


    fun requestAndWatch(
        subscriptionId: String,
        filters: MutableList<JsonFilter> = mutableListOf(JsonFilter())
    ) {
        subscriptions[subscriptionId] = filters
        sendFilter(subscriptionId)
    }

    fun justConnection() {
        val request = Request.Builder().url(url)
            .build()
        val listener = object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                isOpen = true
                sendFailedMsgSet.forEach {
                    webSocket.send(it)
                    Thread.sleep(100)
                }
                listeners.forEach { it.onRelayStateChange(Type.CONNECT, this@Relay) }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                synchronized(listeners) {
                    val msg = Event.gson.fromJson(text, JsonElement::class.java).asJsonArray
                    val channel = msg[1].asString
                    parseMessage(text, channel)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                isOpen = false
                listeners.forEach { it.onRelayStateChange(Type.DISCONNECT, this@Relay) }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isOpen = false
                listeners.forEach {
                    it.onError(Error("WebSocket Failure", t), "justConnection", this@Relay)
                }
            }
        }
        socket = httpClient.newWebSocket(request, listener)
    }

    private fun parseMessage(text: String, subscriptionId: String) {
        try {

            val msg = Event.gson.fromJson(text, JsonElement::class.java).asJsonArray
            val type = msg[0].asString
            val channel = msg[1].asString

            when (type) {
                "EVENT" -> {
                    val event = Event.fromJson(msg[2], Client.lenient)
                    if (event is PrivateDmEvent) {
                        println("------------------")
                        println(text)
                        println("------------------")
                    }
                    listeners.forEach { it.onEvent(event, subscriptionId, this@Relay) }
                }
                "EOSE" -> listeners.forEach {
                    it.onRelayStateChange(Type.EOSE, this@Relay)
                }
                "NOTICE" -> listeners.forEach {
                    // "channel" being the second string in the string array ...
                    it.onError(
                        Error("Relay sent notice: $channel"),
                        subscriptionId,
                        this@Relay,
                    )
                }
                "OK" -> {
                    //["OK","c7e8b544ee03b2237f04af7978f3b3897fbcbf81cd30b6175459221ef288cd71",true,""]
                    println("OK--->${msg}")
                    listeners.forEach { it.onOK(this@Relay) }
                }
                else -> listeners.forEach {
                    it.onError(

                        Error("Unknown type $type on channel $channel. Msg was $text"),
                        subscriptionId,
                        this@Relay,

                        )
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            text.chunked(2000) { chunked ->
                listeners.forEach {
                    it.onError(
                        Error("Problem with $chunked"),
                        subscriptionId,
                        this@Relay
                    )
                }
            }
        }
    }


    fun disconnect() {
        sendFailedMsgSet.clear()
        httpClient.dispatcher.executorService.shutdown()
        socket.close(1000, "Normal close")
    }

    private fun sendFilter(requestId: String) {
        val filters =
            subscriptions[requestId] ?: error("No filter(s) found.")

        val request = """["REQ","$requestId",${filters.joinToString(",") { it.toJson() }}]"""
        if (socket != null && isOpen) {
            socket.send(request)
            print("ws_send_request_msg----$request")
            println()
        } else {
            sendFailedMsgSet.add(request)
        }
    }

    fun send(signedEvent: Event) {
        val request = """["EVENT",${signedEvent.toJson()}]"""
        if (socket != null && isOpen) {
            socket.send(request)
            println("${socket}-->ws_send_request_msg----${signedEvent.toJson()}")
            println()
        } else {
            sendFailedMsgSet.add(request)
        }
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

}
