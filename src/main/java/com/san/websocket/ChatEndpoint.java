package com.san.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.san.model.Message;

// Reference : https://docs.oracle.com/javaee/7/tutorial/websocket.htm

// Note : Here we are using Java API for WebSocket 1.1
// For compatible web servers, refer below links
// Apache Tomcat Reference : https://wiki.apache.org/tomcat/Specifications
// Apache Tomcat Reference : http://tomcat.apache.org/whichversion.html
// Jetty Reference : https://www.eclipse.org/jetty/documentation/9.4.x/what-jetty-version.html

@ServerEndpoint(value = "/chat/{username}", decoders = MessageDecoder.class, encoders = MessageEncoder.class)
public class ChatEndpoint {
	private Session session;
	private static final Set<ChatEndpoint> chatEndpoints = new CopyOnWriteArraySet<>();
	private static HashMap<String, String> users = new HashMap<>();

	@OnOpen
	public void onOpen(Session session, @PathParam("username") String username) throws IOException, EncodeException {

		this.session = session;
		chatEndpoints.add(this);
		users.put(session.getId(), username);

		Message message = new Message();
		message.setFrom(username);
		message.setContent("Connected!");
		broadcast(message);
	}

	@OnMessage
	public void onMessage(Session session, Message message) throws IOException, EncodeException {
		message.setFrom(users.get(session.getId()));
		broadcast(message);
	}

	@OnClose
	public void onClose(Session session) throws IOException, EncodeException {
		chatEndpoints.remove(this);
		Message message = new Message();
		message.setFrom(users.get(session.getId()));
		message.setContent("Disconnected!");
		broadcast(message);
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		// Do error handling here
	}

	private static void broadcast(Message message) throws IOException, EncodeException {
		for (ChatEndpoint endpoint : chatEndpoints) {
			synchronized (endpoint) {
				try {
					endpoint.session.getBasicRemote().sendObject(message);
				} catch (IOException | EncodeException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
