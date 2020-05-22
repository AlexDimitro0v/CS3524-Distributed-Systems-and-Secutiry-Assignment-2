package cs3524.solutions.mud;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * A class that can be used to represent a MUD; essentially, this is a graph.
*/
public class MUD {

	// ----------------------------------------------PRIVATE STUFF------------------------------------------------------
	/*
	 * All the private stuff. These methods are hidden (encapsulation applied).
	 */


	// Start location of the player
    private String _startLocation = "";
	// HashMaps are not synchronized, but we don't really need this to be synchronised.
    // A record of all the vertices in the MUD graph.
    private Map<String,Vertex> vertexMap = new HashMap<String,Vertex>();
	// All active clients' usernames will be stored here:
	private ArrayList<String> usersList = new ArrayList<String>();
	// Inventory Map that stores clients inventories (username -> inventory list):
	private Map<String, ArrayList<String>> inventory = new HashMap<String, ArrayList<String>>();
	// Logs Map that stores clients logins:
	private Map<String, Boolean> logMap = new HashMap<String, Boolean>();
	// Location Map of all the client's locations
	private Map<String, String> locationsMap = new HashMap<String ,String>();


    /**
     * Add a new edge to the graph.
     */
    private void addEdge(String sourceName, String destName, String direction, String view) {
        Vertex v = getOrCreateVertex(sourceName);
        Vertex w = getOrCreateVertex(destName);
        v._routes.put(direction, new Edge(w, view));
    }


    /**
     * Create a new thing at a location.
     */
    private void createThing(String loc, String thing){
		Vertex v = getOrCreateVertex(loc);
		v._things.add(thing);
    }


    /**
     * Change the message associated with a location.
     */
    private void changeMessage(String loc, String msg){
		Vertex v = getOrCreateVertex(loc);
		v._msg = msg;
    }


	/**
	 * Get a vertex by trying to retrieve it from the vertexMap
	 */
	private Vertex getVertex(String vertexName) {
		return vertexMap.get(vertexName);
	}


    /**
     * If vertexName is not present, add it to vertexMap.  In either
     * case, return the Vertex. Used only for creating the MUD.
     */
    private Vertex getOrCreateVertex(String vertexName) {
        Vertex v = vertexMap.get(vertexName);
        if (v == null) {
            v = new Vertex(vertexName);
            vertexMap.put(vertexName, v);
        }
        return v;
    }


    /**
     * Creates the edges of the graph on the basis of a file with the following format:
     * source direction destination message
     */
    private void createEdges(String edgesfile) {
		try {
			FileReader fin = new FileReader(edgesfile);
			BufferedReader edges = new BufferedReader(fin);
			String line;
			while((line = edges.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line);
				if(st.countTokens() < 3) {
					System.err.println("Skipping ill-formatted line " + line);
					continue;
				}
				String source 		= st.nextToken();
				String direction    = st.nextToken();
				String dest   		= st.nextToken();
				String msg 			= "";
				while (st.hasMoreTokens()) {
					msg = msg + st.nextToken() + " ";		// Add the rest to the message separating the words with a space
				}
				addEdge(source, dest, direction, msg);
			}
		}
		catch(IOException e) {
			// Catch an I/O error and display it to the user
			System.err.println("Graph.createEdges(String " + edgesfile + ")\n" + e.getMessage());
		}
    }


    /**
     * Records the messages associated with vertices in the graph on the basis of a file with the following format:
     * location message
     * The first location is assumed to be the starting point for users joining the MUD.
     */
    private void recordMessages(String messagesfile) {
		try {
			FileReader fin = new FileReader(messagesfile);
			BufferedReader messages = new BufferedReader(fin);
			String line;
			boolean first = true; 								// For recording the start location.
			while((line = messages.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line);
				if(st.countTokens() < 2) {
					System.err.println("Skipping ill-formatted line " + line);
					continue;
				}
				String loc = st.nextToken();
				String msg = "";
				while (st.hasMoreTokens()) {
					msg = msg + st.nextToken() + " ";			// Add the rest to the message separating the words with a space
				}
				changeMessage(loc, msg);
				if (first) {      								// Record the start location.
					_startLocation = loc;
					first = false;
				}
			}
		}
		catch( IOException e ) {
			// Catch an I/O error and display it to the user
			System.err.println("Graph.recordMessages(String " + messagesfile + ")\n" + e.getMessage());
		}
    }


    /**
     * Records the things associated with vertices in the graph on the basis of a file with the following format:
     * location thing1 thing2 ...
     */
    private void recordThings(String thingsfile) {
		try {
			FileReader fin = new FileReader(thingsfile);
			BufferedReader things = new BufferedReader(fin);
			String line;
			while((line = things.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line);
				if(st.countTokens() < 2) {
					System.err.println("Skipping ill-formatted line " + line);
					continue;
				}
				String loc = st.nextToken();
				while (st.hasMoreTokens()) {
					addThing(loc, st.nextToken());				// Add all the things one by one
				}
			}
		}
		catch( IOException e ) {
			// Catch an I/O error and display it to the user
			System.err.println("Graph.recordThings(String " + thingsfile + ")\n" + e.getMessage());
		}
    }

	// -----------------------------------------------------------------------------------------------------------------


	// ----------------------------------------------PUBLIC STUFF------------------------------------------------------
	/*
	   * All the public stuff. These methods are designed to hide the internal structure of the MUD.
	   * Could declare these on an interface and have external objects interact with the MUD via the interface.
 	*/


    /**
     * A constructor that creates the MUD(i.e. the graph).
     */
    public MUD(String edgesfile, String messagesfile, String thingsfile) {
		createEdges(edgesfile);
		recordMessages(messagesfile);
		recordThings(thingsfile);

		System.out.println("Files read...");
		System.out.println(vertexMap.size() + " vertices");
    }


    // This method enables us to display the entire MUD
	// (mostly used for testing purposes so that we can check that the structure defined has been successfully parsed).
    public String toString() {
		String summary = "";
		Iterator iter = vertexMap.keySet().iterator();
		String loc;
		while (iter.hasNext()) {
			loc = (String)iter.next();
			summary = summary + "Node: " + loc;
			summary += vertexMap.get(loc).toString();
		}
		summary += "Start location = " + _startLocation;
		return summary;
    }


    /**
     * A method to provide a string describing a particular location.
     */
    public String locationInfo(String loc) {
		return getVertex(loc).toString();
    }


    /**
     * Get the start location for new MUD users.
     */
    public String startLocation() {
		return _startLocation;
    }


	/**
	 * Save the location of a MUD user.
	 */
	public void saveLocation(String location, String username) {
		locationsMap.put(username, location);
	}


	/**
	 * Retrieve the location of a MUD user.
	 */
	public String retrieveLocation(String username) {
		return locationsMap.get(username);
	}


    /**
     * Add a thing to a location; used to enable us to add new users.
     */
    public void addThing(String loc, String thing) {
		Vertex v = getVertex(loc);
		v._things.add(thing);
    }


    /**
     * Remove a thing from a location.
     */
    public void delThing(String loc, String thing) {
		Vertex v = getVertex(loc);
		v._things.remove(thing);
    }


    /**
     * A method to enable a player to move through the MUD (a player is a thing).
	 * Checks that there is a route to travel on.
	 * Returns the location moved to.
     */
    public String moveThing(String loc, String direction, String thing) {
		Vertex v = getVertex(loc);
		Edge e = v._routes.get(direction);
		if (e == null)   // if there is no route in that direction
			return loc;  // no move is made; return current location.
		v._things.remove(thing);
		e._dest._things.add(thing);
		return e._dest._name;
    }


	/**
	 * Add user to the list of active users and initialize user's inventory list
	 */
	public void addUser(String username) {
		// The function adds the user if not already logged in the current MUD game
		if (!usersList.contains(username) && !(username.length() == 0) && !username.contains(" ")){
			usersList.add(username);
			inventory.put(username, new ArrayList<String>());
		}
	}


	/**
	 * Mark the user as logged in for the corresponding MUD game
	 */
	public void logUser(String username) {
		logMap.put(username, true);
	}


	/**
	 * Check if the user is logged in
	 */
	public boolean isLogged(String username) {
		return logMap.getOrDefault(username, false);
	}


	/**
	 * Remove the user from the list of active users and remove user's inventory list
	 */
	public void removeUser(String username) {
		usersList.remove(username);
		inventory.remove(username);
		logMap.remove(username);
		locationsMap.remove(username);
	}


	/**
	 * User picks up an item at a location
	 */
	public String takeItem(String username, String item, String location) {
		String output;
		ArrayList<String> clientInventoryList = inventory.get(username);
		Vertex v = getVertex(location);
		List<String> items = v._things;
		if(item.endsWith("(client)")){
			// The user can only take item things
			output = ">You can only pick items.";
		}
		else if(!items.contains(item)){
			output = ">There is no such item at this location.";
		}
		else{
			delThing(location, item);                       // Delete the thing from the particular location
			clientInventoryList.add(item);                  // Add the item to the client inventory
			inventory.put(username, clientInventoryList);
			output = ">Item picked up successfully.\n\tYour inventory: "+ clientInventoryList.toString();

		}
		return output;
	}


	/**
	 * Prints the items for a specific user
	 */
	public String printItems(String username) {
		ArrayList<String> clientInventoryList = inventory.get(username);
		if(clientInventoryList.isEmpty()) return "Your inventory is empty.";
		else return ">You have: " + clientInventoryList.toString();
	}


	/**
	 * Prints the online users
	 */
	public String printUsers() {
		return ">Online users: [" + String.join(", ", usersList) +']';
	}


	/**
	 * Returns the number of the online users in a MUD game
	 */
	public int activeUsers() {
		return usersList.size();
	}


	/**
	 * Returns the list of recipient that must receive a live update for a certain event/change.
	 */
	public List<String> callbackUpdate(String username){
		List<String> newUsersList = new ArrayList<>();

		for(String user : usersList){
			if(!user.equals(username)){
				newUsersList.add(user);
			}
		}
		return newUsersList;
	}


    /**
     * A main method that can be used to testing purposes to ensure that the MUD is specified correctly.
     */
    public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: java Graph <edgesfile> <messagesfile> <thingsfile>");
			return;
		}
		MUD m = new MUD(args[0], args[1], args[2]);
		System.out.println(m.toString());
		}
}
