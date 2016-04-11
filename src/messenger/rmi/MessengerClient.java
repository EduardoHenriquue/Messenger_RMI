package messenger.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessengerClient extends Remote {
	void receiveMsg(String from, String msg) throws RemoteException;
}