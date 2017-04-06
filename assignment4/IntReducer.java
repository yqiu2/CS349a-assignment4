package assignment4;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.RemoteException;
import java.util.*;

public interface IntReducer extends Remote {
	IntReducer createReduceTask(String key, IntMaster master) throws RemoteException, AlreadyBoundException;

	void receiveValues(int value) throws RemoteException;

	int terminate() throws RemoteException;
}
