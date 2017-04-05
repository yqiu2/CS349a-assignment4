package a4;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;

public class Master implements IntMaster {

	public Master() {

	}

	public IntReducer[] getReducers(String[] keys) {
		IntReducer[] intReduc = new IntReducer[4];

		return intReduc;
	}

	public void markMapperDone() {

	}

	public void receiveOutput(String key, int value) {
		//5. when the master recieves a request from a mapper task for the addresses
		//of the reducer tasks corresponding to its keys, 
		//it goes through the mapper task keys and
		// a) if the key is unassigned to a reducer task, it creates a reducer task and the remote object reference is sent to the mapper task
		// b) if the key is assigned, the corresponding object is simply sen to the mapper task
	}

	public static void main(String[] args) {

		//1. master opens the file and reads line by line
		//2. for each line, master starts task on one of the mapper nodes
		// ^^use FCFS
		//9.the master stores all the results received from all reducers 
		//to an output file, and terminates
	}

}
