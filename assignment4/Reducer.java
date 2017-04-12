// package assignment4;

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
			System.out.println("R: created Reducer R"+key);
			return reducerStub;
		} catch (Exception e) {
			System.err.println("R: Client exception(could not register Reducer task " + key + "): \n" + e.toString());
			e.printStackTrace();
			return null;
		}

	}
	
	public void receiveValues(int value) {
		System.out.println(key+" + "+value);
		count += value;
	}

	public int terminate() {
		// 9.once the reducer is done, it sends its results to the master, and
		// terminates
		System.out.println("R: terminating Reducer: "+ key);
		try {
			System.out.println("R: Send to Master: "+ key + " v: "+ count);
			masterStub.receiveOutput(key, count);
			return 1;
		} catch (Exception e) {
			System.err.println("R: Master Exception - Cannot read file" + e.toString());
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
		Reducer reducerManager = new Reducer();
		// add to registry
		try {
			IntReducer reducerManagerStub = (IntReducer) UnicastRemoteObject.exportObject(reducerManager, 0);
			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry();
			registry.bind("ReduceManager", reducerManagerStub);
			System.out.println("R: Started ReduceManager");
		} catch (Exception e) {
			System.err.println("R: Client exception(could not register ReduceManager \n" + e.toString());
			e.printStackTrace();
		}
	}

}
