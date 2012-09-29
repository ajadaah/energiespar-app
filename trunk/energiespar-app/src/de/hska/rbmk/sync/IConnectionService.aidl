package de.hska.rbmk.sync;

interface IConnectionService {
	void getConnectionStatus();
	void setIPandPort(String ip, String port);
	void sendCharArray(in byte[] charArray);
}