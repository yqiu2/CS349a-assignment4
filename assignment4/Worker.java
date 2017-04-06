package assignment4;

public class Worker {


	public IntReducer createReduceTask(String key, IntMaster stub) {
		try {
			IntReducer reducer = new Reducer();
			reducer.createReduceTask(key, stub);
			return reducer;
		} catch (Exception e) {
			System.err.println("Client exception(could not find remote Mapper or Reducer): " + e.toString());
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}


}
