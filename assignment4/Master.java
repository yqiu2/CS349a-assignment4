
// package assignment4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Master implements IntMaster {
	private static final ArrayList<String> workerIPs = new ArrayList<String>(
			Arrays.asList("54.172.165.34", "52.91.9.237"));
	// final ArrayList<String> reducerIPs = new
	// ArrayList<String>(Arrays.asList("REDUCER_IP_1", "REDUCER_IP_2"));

	private ArrayList<IntMapper> mapperNodes; // what to call when creating
												// mapper and reducer tasks
	private HashMap<String, IntMapper> mapperTasks; // the actual mapper tasks
	private HashMap<String, Boolean> mapperTasksDone; // whether a Mapper Task
														// is done or not
	private ArrayList<IntReducer> reducerNodes; // what to call when creating
												// mapper and reducer tasks
	private HashMap<String, IntReducer> reducerTasks; // <word, reducerTaskStub>
	private HashMap<IntReducer, Boolean> reducerTasksDone;
	// private HashMap<String, Integer> results; // <word, count>
	private IntMaster stub;
	private File file;
	private boolean currentlyReading;
	private PrintWriter writer;

	public Master() {
		file = new File("wordfrequencies.txt");
		mapperNodes = new ArrayList<IntMapper>();
		reducerNodes = new ArrayList<IntReducer>();
		mapperTasks = new HashMap<String, IntMapper>();
		mapperTasksDone = new HashMap<String, Boolean>();
		reducerTasks = new HashMap<String, IntReducer>();
		reducerTasksDone = new HashMap<IntReducer, Boolean>();
		// results = new HashMap<String, Integer>();
		stub = null;
		currentlyReading = false;
		try {
			writer = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void setStub(IntMaster stub) {
		this.stub = stub;
	}

	public IntReducer[] getReducers(String[] keys) {
		IntReducer[] matchingReducers = new IntReducer[keys.length + 1];
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
					reducerTasksDone.put(newReducerTask, false);
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
		try {
			System.out.println("writing to file: " + key + ":" + value);
			writer.println(key + ":" + value);
			// writer.close();
			reducerTasks.remove(key);
			/*
			 * if (mapperTasksDone.isEmpty() && reducerTasks.isEmpty() &&
			 * !currentlyReading) { writer.close(); }
			 */ } catch (Exception e) {
			System.err.println("Master Exception - Cannot write file" + e.toString());
			e.printStackTrace();
		}
		// results.put(key, value);
		reducerTasks.remove(key);
		/*
		 * if (mapperTasksDone.isEmpty() && reducerTasks.isEmpty() &&
		 * !currentlyReading) { boolean reducersAllFinished = true; for
		 * (IntReducer reducer : reducerTasksDone.keySet()) { Boolean
		 * reducerDone = reducerTasksDone.get(reducer); if (!reducerDone) {
		 * reducersAllFinished = false; } } if (reducersAllFinished) {
		 * writer.close(); } }
		 */
	}

	public void markMapperDone(String mapperName) {
		System.out.println("calling mark Master done, removing " + mapperName + " from " + mapperTasksDone.keySet());
		mapperTasksDone.remove(mapperName);
		System.out.println("calling mark Master done, removed " + mapperName + " from " + mapperTasksDone.keySet());

		// if all mappers are done send terminate message to reducers
		/*
		 * if (mapperTasksDone.isEmpty() && !currentlyReading) { System,out.
		 * println("all mappers are done send terminate message to reducers");
		 * for (String reducerName : reducerTasks.keySet()) { IntReducer
		 * reducerStub = reducerTasks.get(reducerName); try {
		 * reducerStub.terminate(); } catch (Exception e) {
		 * System.err.println("ReducerTask Exception - could not terminate" +
		 * e.toString()); e.printStackTrace(); } } }
		 */
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
			master.currentlyReading = true;
			FileReader fr = new FileReader(filename);
			BufferedReader bufr = new BufferedReader(fr);
			String line = bufr.readLine();
			int count = 0;
			int numMappers = master.mapperNodes.size();
			// 2. for each line, master starts task on one of the mapper nodes
			// ^^use FCFS
			while (line != null) {
				System.out.println("processing: " + line);
				// find a mapper node and start a mapper task on it
				String mapperName = "M" + Integer.toString(count);
				IntMapper newMapTask = master.mapperNodes.get(count % numMappers).createMapTask(mapperName);
				master.mapperTasks.put(mapperName, newMapTask);
				master.mapperTasksDone.put(mapperName, false);
				newMapTask.processInput(line, master.stub);
				count++;
				line = bufr.readLine();
			}
			bufr.close();
			master.currentlyReading = false;

			while (!master.mapperTasksDone.isEmpty()) {
			}
			System.out.println("all mappers are done send terminate message to reducers");
			for (String reducerName : master.reducerTasks.keySet()) {
				IntReducer reducerStub = master.reducerTasks.get(reducerName);
				try {
					reducerStub.terminate();
				} catch (Exception e) {
					System.err.println("ReducerTask Exception - could not terminate" + e.toString());
					e.printStackTrace();
				}
			}

			if (master.mapperTasksDone.isEmpty() && master.reducerTasks.isEmpty() && !master.currentlyReading) {
				master.writer.close();
			}

		} catch (Exception e) {
			System.err.println("Master Exception - Cannot read file" + e.toString());
			e.printStackTrace();
		}

		// 9.the master stores all the results received from all reducers
		// to an output file, and terminates

	}

}
