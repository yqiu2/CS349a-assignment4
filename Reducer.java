package a4;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;

public class Reducer implements IntReducer {

	public IntReducer createReduceTask(String key, IntMaster master) {
		IntReducer intReduc = new Reducer();

		return intReduc;
	}

	public void receiveValues(int value) {

		
	}

	public int terminate() {
		//9.once the reducer is done, it sends its results to the master, and terminates

		return -1;
	}
	
	public static void main(String[] args){
		//6. when a reducer task is started, it's in charge of counting the 
		//frequency of occurrence of a specific key word
		//8. the reducer task keeps adding to the frequency count of the key word,
		//until all mapper tasks are done
		//9.once the reducer is done, it sends its results to the master, and terminates
		//
	}

}
