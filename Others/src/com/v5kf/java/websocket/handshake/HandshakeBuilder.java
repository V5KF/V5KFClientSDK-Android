package com.v5kf.java.websocket.handshake;

public interface HandshakeBuilder extends Handshakedata {
	public abstract void setContent( byte[] content );
	public abstract void put( String name, String value );
}
