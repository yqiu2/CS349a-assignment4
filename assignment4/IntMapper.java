// package assignment4;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;


public interface IntMapper extends Remote {
	IntMapper createMapTask(String name) throws RemoteException, AlreadyBoundException;
	void processInput(String input, IntMaster theMaster) throws RemoteException, AlreadyBoundException;
}
