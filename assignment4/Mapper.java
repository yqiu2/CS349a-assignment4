import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class Mapper implements IntMapper {
	private String mapperName;

	public Mapper() {
		this.mapperName = "";
	}

	public IntMapper createMapTask(String name) {
		Mapper mapTask = new Mapper();
		mapTask.mapperName = name;
		// add to registry
		try {
			IntMapper mapStub = (IntMapper) UnicastRemoteObject.exportObject(mapTask, 0);
			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry();
			registry.bind(name, mapStub);
			System.out.println("M: new MapTask for " + name);
			return mapStub;
		} catch (Exception e) {
			System.err.println("M: Client exception(could not register Mapper task " + name + "): \n" + e.toString());
			e.printStackTrace();
			return null;
		}
	}

	public void processInput(String input, IntMaster theMaster) {
		// counts frequency of words
		input = input.replaceAll("[^A-Za-z\\s]", "");
		input = input.toLowerCase();
		String[] tokens = input.split("[ ]+");
		HashMap<String, Integer> outputs = new HashMap<String, Integer>();
		for (String token : tokens) {
			if (outputs.containsKey(token)) {
				int current = outputs.get(token);
				current++;
				outputs.remove(token);
				outputs.put(token, current);
			} else {
				outputs.put(token, 1);
			}
		}
		// get reducers
		try {
			String[] words = outputs.keySet().toArray(new String[outputs.size()]);
			System.out.println("M: getting reducers for " + input);
			IntReducer[] reducers = theMaster.getReducers(words);
			System.out.println("M: got " + reducers.length + " IntReducers from master for" + input);
			for (int i = 0; i < words.length; i++) {
				reducers[i].receiveValues(outputs.get(words[i]));
			}
			// tells master that it is done
			System.out.println("M: calling MarkMapperDone(" + this.mapperName);
			theMaster.markMapperDone(this.mapperName);

		} catch (Exception e) {
			System.err.println("M:Master Exception (could not get reducers from master)" + e.toString());
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		Mapper mapManager = new Mapper();
		// add to registry
		try {
			IntMapper mapManagerStub = (IntMapper) UnicastRemoteObject.exportObject(mapManager, 0);
			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry();
			registry.bind("MapManager", mapManagerStub);
			System.out.println("M: Started MapManager");
		} catch (Exception e) {
			System.err.println("M: Client exception(could not register MapManager \n" + e.toString());
			e.printStackTrace();
		}

	}

}
