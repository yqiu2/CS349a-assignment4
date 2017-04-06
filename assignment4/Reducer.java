package assignment4;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

//import java.rmi.AlreadyBoundException;
//import java.rmi.RemoteException;

public class Reducer implements IntReducer {
	String key;
	IntMaster masterStub;
	int count; 

	public Reducer() { // starting reducerMangaer
		this.key = "";
		this.masterStub = null;
		count = 0;
	}

	public Reducer(String key, IntMaster master) {
		this.key = key;
		this.masterStub = master;
		count = 0;
	}

	public IntReducer createReduceTask(String key, IntMaster master) {
		Reducer reducerTask = new Reducer(key, master);
		// add to registry
		try {
			IntReducer reducerStub = (IntReducer) UnicastRemoteObject.exportObject(reducerTask, 0);
			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry();
			registry.bind("R" + key, reducerStub);
			System.out.println("");
			return reducerStub;
		} catch (Exception e) {
			System.err.println("Client exception(could not register Reducer task " + key + "): \n" + e.toString());
			e.printStackTrace();
			return null;
		}

	}
	public void receiveValues(int value) {
		count += value;
	}

	public int terminate() {
		// 9.once the reducer is done, it sends its results to the master, and
		// terminates
		try {
			masterStub.receiveOutput(key, count);
			return 1;
		} catch (Exception e) {
			System.err.println("Master Exception - Cannot read file" + e.toString());
			e.printStackTrace();
			return -1;
		}
	}

	public static void main(String[] args) {
		// 6. when a reducer task is started, it's in charge of counting the
		// frequency of occurrence of a specific key word
		// 8. the reducer task keeps adding to the frequency count of the key
		// word,
		// until all mapper tasks are done
		// 9.once the reducer is done, it sends its results to the master, and
		// terminates
		//
	}

}
