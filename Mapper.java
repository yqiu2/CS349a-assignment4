package a4;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;

public class Mapper implements IntMapper {
	
	public IntMapper createMapTask(String name) {
		IntMapper intMap = new Mapper();

		return intMap;
	}

	public void processInput(String input, IntMaster theMaster) {
		//3. a mapper task counts the frequency of words in the line sent to it,
		//4. then it contacts the master to get the addresses of the reducers in 
		//charge of each key it generated
		
	}
	
	public static void main (String[] args){
		//7. the mapper task directly contacs the corresponding reducer task, and sends to its
		//locally stored word count, and terminates when done
	}

}
