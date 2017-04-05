package a4;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.RemoteException;
import java.util.*;

public interface IntMaster extends Remote {
	IntReducer[] getReducers(String[] keys) throws RemoteException, AlreadyBoundException;
	void markMapperDone() throws RemoteException;
	void receiveOutput(String key, int value) throws RemoteException;
}
