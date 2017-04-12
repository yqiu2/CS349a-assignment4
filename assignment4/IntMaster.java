import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IntMaster extends Remote {
	IntReducer[] getReducers(String[] keys) throws RemoteException, AlreadyBoundException;

	public void markMapperDone(String mapperName) throws RemoteException;

	void receiveOutput(String key, int value) throws RemoteException;
}
