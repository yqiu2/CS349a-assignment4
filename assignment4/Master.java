package assignment4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Master implements IntMaster {
	private static final ArrayList<String> workerIPs = new ArrayList<String>(
			Arrays.asList("MAPPER_IP_1", "MAPPER_IP_2"));
	final ArrayList<String> reducerIPs = new ArrayList<String>(Arrays.asList("REDUCER_IP_1", "REDUCER_IP_2"));

	private ArrayList<IntMapper> mapperNodes; // what to call when creating
												// mapper and reducer tasks
	private HashMap<String, IntMapper> mapperTasks; // the actual mapper tasks
	private ArrayList<IntReducer> reducerNodes; // what to call when creating
												// mapper and reducer tasks
	private HashMap<String, IntReducer> reducerTasks; // <word, reducerTaskStub>
	private HashMap<String, Integer> results; // <word, count>
	private IntMaster stub;

	public Master() {
		mapperNodes = new ArrayList<IntMapper>();
		reducerNodes = new ArrayList<IntReducer>();
		mapperTasks = new HashMap<String, IntMapper>();
		reducerTasks = new HashMap<String, IntReducer>();
		results = new HashMap<String, Integer>();
		stub = null;
	}

	private void setStub(IntMaster stub) {
		this.stub = stub;
	}

	public IntReducer[] getReducers(String[] keys) {
		IntReducer[] matchingReducers = new IntReducer[keys.length];
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			if (reducerTasks.containsKey(key)) {
				matchingReducers[i] = reducerTasks.get(key);
			} else {
				// create new reducerTask
				IntReducer reducerNode = reducerNodes.get(key.hashCode() % reducerNodes.size());
				IntReducer newReducerTask;
				try {
					newReducerTask = reducerNode.createReduceTask(key, this.stub);
					reducerTasks.put(key, newReducerTask);
					matchingReducers[i] = newReducerTask;
				} catch (Exception e) {
					System.err.println("Client exception(could not add reducerTask): " + e.toString());
					e.printStackTrace();
				}

			}
		}
		return matchingReducers;
	}

	public void receiveOutput(String key, int value) {
		// 5. when the master receives a request from a mapper task for the
		// addresses
		// of the reducer tasks corresponding to its keys,
		// it goes through the mapper task keys and
		// a) if the key is unassigned to a reducer task, it creates a reducer
		// task and the remote object reference is sent to the mapper task
		// b) if the key is assigned, the corresponding object is simply sent to
		// the mapper task
		results.put(key, value);
	}

	public static void main(String[] args) {
		// Initialize master
		Master master = new Master();
		// add to registry
		try {
			IntMaster stub = (IntMaster) UnicastRemoteObject.exportObject(master, 0);
			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry();
			registry.bind("Master", stub);
			master.setStub(stub);
			System.out.println("master has been bound to RMI registry");
		} catch (Exception e) {
			System.err.println("Client exception(could not register Master): \n" + e.toString());
			e.printStackTrace();

		}
		// fill in mapper and reducer stubs
		try {
			for (int i = 0; i < workerIPs.size(); i++) {
				String remoteHost = workerIPs.get(i);
				Registry remoteRegistry = LocateRegistry.getRegistry(remoteHost);
				IntMapper remoteStub = (IntMapper) remoteRegistry.lookup("MapManager");
				master.mapperNodes.add(remoteStub);
			}
			for (int i = 0; i < workerIPs.size(); i++) {
				String remoteHost = workerIPs.get(i);
				Registry remoteRegistry = LocateRegistry.getRegistry(remoteHost);
				IntReducer remoteStub = (IntReducer) remoteRegistry.lookup("ReduceManager");
				master.reducerNodes.add(remoteStub);
			}
		} catch (Exception e) {
			System.err.println("Client exception(could not find remote Mapper or Reducer): " + e.toString());
			e.printStackTrace();
		}

		// 1. master opens the file and reads line by line

		try {
			String filename = args[0];
			FileReader fr = new FileReader(filename);
			BufferedReader bufr = new BufferedReader(fr);
			String line = bufr.readLine();
			int count = 0;
			int numMappers = master.mapperNodes.size();
			// 2. for each line, master starts task on one of the mapper nodes
			// ^^use FCFS
			while (line != null) {
				line = bufr.readLine();
				// find a mapper node and start a mapper task on it
				String mapperName = "M" + Integer.toString(count);
				IntMapper newMapTask = master.mapperNodes.get(count % numMappers).createMapTask(mapperName);
				newMapTask.processInput(line, master.stub);
				master.mapperTasks.put(mapperName, newMapTask);
				count++;
			}
			bufr.close();
		} catch (Exception e) {
			System.err.println("Master Exception - Cannot read file" + e.toString());
			e.printStackTrace();
		}

		// 9.the master stores all the results received from all reducers
		// to an output file, and terminates

	}

}
