package assignment4;

// import java.rmi.*;
import java.nio.channels.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;


public interface IntMaster extends Remote {
	IntReducer[] getReducers(String[] keys) throws RemoteException, AlreadyBoundException;
	void receiveOutput(String key, int value) throws RemoteException;
}