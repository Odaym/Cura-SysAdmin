package com.cura.connection;

interface CommunicationInterface {
	
	String executeCommand(String command);
	void close();
	boolean connected();
}
