package assignment4;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.RemoteException;
import java.util.*;

public interface IntMapper extends Remote {
	IntMapper createMapTask(String name) throws RemoteException, AlreadyBoundException;
	void processInput(String input, IntMaster theMaster) throws RemoteException, AlreadyBoundException;
}
