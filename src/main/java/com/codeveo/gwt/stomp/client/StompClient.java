package com.codeveo.gwt.stomp.client;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StompClient {
    private static final Logger logger = Logger.getLogger(StompClient.class.getName());
    private final boolean useSockJs;
    private String wsURL;
    private Callback callback;
    private JavaScriptObject jsoStompClient;
    private boolean isConnected = false;
    private Map<String, Subscription> subscriptions;

    public static interface Callback {
        void onConnect();

        void onError(String cause);

        void onDisconnect();
    }

    public StompClient(String wsURL, Callback callback, boolean useSockJs) {
        this.useSockJs = useSockJs;
        this.wsURL = wsURL;
        this.callback = callback;
        this.subscriptions = new HashMap<String, Subscription>();
    }

    public final void connect() {
        if (isConnected) {
            logger.log(Level.FINE, "Already connected");
            return;
        }

        logger.log(Level.FINE, "Connecting to '" + wsURL + "' ...");
        __connect(wsURL, useSockJs);
    }

    public final void disconnect() {
        for (Entry<String, Subscription> id : subscriptions.entrySet()) {
            unsubscribe(id.getKey());
        }

        logger.log(Level.FINE, "Disconecting from '" + wsURL + "' ...");
        __disconnect();
    }

    public final Subscription subscribe(String destination, MessageListener listener) {
        logger.log(Level.FINE, "Subscribing to destination '" + destination + "' ...");
        Subscription subscription = __subscribe(destination, listener);

        logger.log(Level.FINE, "Subscribed to destination '" + destination + "' with ID '" + subscription.getId() + "'");
        subscriptions.put(destination, subscription);

        return subscription;
    }

    public final void unsubscribe(String destination) {
        Subscription subscription = subscriptions.get(destination);

        if (subscription != null) {
            logger.log(Level.FINE, "Unsubscribing from destination '" + destination + "' ...");
            __unsubscribe(subscription);

            logger.log(Level.FINE, "Unsubscribed from destination '" + destination + "'");
            subscriptions.remove(destination);
        }
    }

    public native final void send(String destination, String jsonString)
    /*-{
        var self = this;
        self.@com.codeveo.gwt.stomp.client.StompClient::jsoStompClient.send(destination, {}, jsonString);
    }-*/;

    public native final boolean isConnected()
    /*-{
        if (self.@com.codeveo.gwt.stomp.client.StompClient::jsoStompClient != null) {
            return false;
        } else {
            return self.@com.codeveo.gwt.stomp.client.StompClient::jsoStompClient.connected;
        }
    }-*/;

    private native final void __connect(String wsURL, boolean overSockJs)
    /*-{
        var self = this;
        var stompClientConfig = {
            onConnect: function () {
                self.@com.codeveo.gwt.stomp.client.StompClient::onConnected()();
            },
            onStompError: function (cause) {
                self.@com.codeveo.gwt.stomp.client.StompClient::onError(Ljava/lang/String;)(cause);
            },
            onDisconnect: function() {
                self.@com.codeveo.gwt.stomp.client.StompClient::onDisconnect()();
            },
            onWebSocketClose: function() {
                self.@com.codeveo.gwt.stomp.client.StompClient::onDisconnect()();
            }
        };

        if (overSockJs === true) {
            stompClientConfig.webSocketFactory = function() {
                return new $wnd.SockJS(wsURL);
            }
        } else {
            stompClientConfig.brokerURL = wsURL;
        }

        self.@com.codeveo.gwt.stomp.client.StompClient::jsoStompClient = new $wnd.StompJs.Client(stompClientConfig);
        self.@com.codeveo.gwt.stomp.client.StompClient::jsoStompClient.activate();
    }-*/;

    private native final void __disconnect()
    /*-{
    	var self = this;
    	self.@com.codeveo.gwt.stomp.client.StompClient::jsoStompClient.deactivate();
    }-*/;

    private native final Subscription __subscribe(String destination, MessageListener listener)
    /*-{
        var self = this;

    	var onMessage = function (message) {
    		listener.@com.codeveo.gwt.stomp.client.MessageListener::onMessage(Lcom/codeveo/gwt/stomp/client/Message;)(message);
    	};

    	var subscription = self.@com.codeveo.gwt.stomp.client.StompClient::jsoStompClient.subscribe(destination, onMessage);

     	return subscription;
    }-*/;

    private native final void __unsubscribe(Subscription subscription)
    /*-{
        subscription.unsubscribe();
    }-*/;

    /* Need to wrap the callbacks */
    private void onConnected() {
        if (callback != null && !isConnected) {
            isConnected = true;
            callback.onConnect();
        }
    }

    private void onDisconnect() {
        if (callback != null) {
            callback.onDisconnect();
            isConnected = false;
        }
    }

    private void onError(String cause) {
        if (callback != null) {
            callback.onError(cause);
        }
    }

}
