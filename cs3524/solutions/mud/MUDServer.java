package cs3524.solutions.mud;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 CS3524: DISTRIBUTED SYSTEMS AND SECURITY
 @author Alexandar Dimitrov <a.dimitrov.17@abdn.ac.uk>
 STUDENT ID: 51769669
 */
public class MUDServer implements MUDServerInterface {
    private Integer maxPlayers = 2;	                                       // Restrict the number of logged players in a single game
    private Integer maxGames = 5;	                                       // Restrict the number of running MUD games that can be created
    private MUD mudInstance;                                               // A single MUDWorld instance
    private Map<String, MUD> mudsMap = new HashMap<String, MUD>();         // HashMap to store all the muds (MUD name -> MUD instance)
    // HashMap of all users in the server mapped to the corresponding MUD games they are taking part in (all users across all games):
    private static Map<String, List<MUD>>activeUsers = new HashMap<String, List<MUD>>();
    // HashMap of all users used as a messageBook where the key is the client's username and the value is ClientInterface
    private static Map<String, MUDClientInterface> messageBook = new HashMap<String, MUDClientInterface>();

    /**
     * The constructor.
     */
    public MUDServer() {
        // Create 3 MUD Games initially in order to give clients an option to choose which they want to connect to
        for (int i = 1; i <= 3; i++) {
            initializeMUD("MUD" + i);
        }
    }

    public String initializeMUD(String MUDname) {
        try {
            if (mudsMap.size() >= maxGames){
                return "serverLimit";
            }

            if (!mudsMap.containsKey(MUDname)){
                // Initialize the MUD with the provided files:
                mudsMap.put(MUDname, new MUD("mymud.edg","mymud.msg","mymud.thg"));
                System.out.println('>' + MUDname + " MUD has been created!\n");
                return "true";
            }

            else return "false";
        }

        catch(Exception ex) {
            // Catch errors if the provided files are faulty
            System.err.println(">Error - faulty files: " + ex.getMessage());
            System.exit(0);
            return "false";
        }
    }

    public String joinMUD(String MUDname, String username) throws RemoteException {
        mudInstance = mudsMap.getOrDefault(MUDname, null);
        if (mudInstance != null) {
            boolean loggedUser = mudInstance.isLogged(username);
            if (!loggedUser) {              // ensure the user can log back in an abandoned game
                if (mudInstance.activeUsers() >= maxPlayers) {
                    return "playersLimit";
                }
            }
        }

        if(mudsMap.containsKey(MUDname)){
            return "true";
        }

        return "false";
    }

    public String myStartLocation(String MUDname) {
        mudInstance = mudsMap.get(MUDname);
        return mudInstance.startLocation();
    }

    public String locationInfo(String location, String MUDname) {
        mudInstance = mudsMap.get(MUDname);
        return mudInstance.locationInfo(location);
    }

    public void saveLocation(String location, String username, String MUDname) {
        mudInstance = mudsMap.get(MUDname);
        mudInstance.saveLocation(location, username);
    }

    public String retrieveLocation(String username, String MUDname) {
        mudInstance = mudsMap.get(MUDname);
        return mudInstance.retrieveLocation(username);
    }

    public String moveThing(String location, String direction, String thing, String MUDname) {
        mudInstance = mudsMap.get(MUDname);
        return mudInstance.moveThing(location, direction, thing);
    }

    public void addThing(String location, String thing, String MUDname) {
        mudInstance = mudsMap.get(MUDname);
        mudInstance.addThing(location, thing);
    }

    public boolean addUser(String username, String MUDname, MUDClientInterface client) {
        // The function returns a boolean value (true if user added successfully, false otherwise)
        mudInstance = mudsMap.get(MUDname);
        mudInstance.addUser(username);                           // Adds the user locally to the specific mudInstance (if not already logged in)
        if (!activeUsers.containsKey(username)) {
            activeUsers.put(username, new ArrayList<MUD>());     // Each client is added to a Hashmap
            activeUsers.get(username).add(mudInstance);          // Add the mudInstance to the list of games the player take part in
            messageBook.put(username, client);                   // Add the client to the messageBook
            return true;
        }
        else {
            if(!activeUsers.get(username).contains(mudInstance)) {
                activeUsers.get(username).add(mudInstance);     // Add the mudInstance to the list of games the player take part in
            }
            return false;
        }
    }

    public void logUser(String username, String MUDname) {
        mudInstance = mudsMap.get(MUDname);
        mudInstance.logUser(username);
    }

    public boolean isLogged(String username, String MUDname) {
        mudInstance = mudsMap.get(MUDname);
        return mudInstance.isLogged(username);
    }

    public void removeUser(String username, String MUDname) {
        // Removes a user from a single MUD game (user quits a game)
        mudInstance = mudsMap.get(MUDname);
        String location = mudInstance.retrieveLocation(username);
        mudInstance.delThing(location, username + "(client)");
        mudInstance.removeUser(username);
        activeUsers.get(username).remove(mudInstance);          // Remove the game that the user took part in from his list
    }

    public void deleteUser(String username) throws RemoteException {
        // Removes the user from all the MUD games (user aborts the server)
        for (MUD mudInstance : activeUsers.get(username)) {
            // All user's logins are being deleted.
            String location = mudInstance.retrieveLocation(username);
            mudInstance.delThing(location, username + "(client)");
            String mudName = getKeyFromValue(mudsMap, mudInstance).toString();                   // get the mudName
            callbackUpdate(mudName, username, username + " has left " + mudName + '.');     // send live updates to relevant clients
            mudInstance.removeUser(username);
        }
        activeUsers.remove(username);
        messageBook.remove(username);
    }

    public String takeItem(String username, String item, String location, String MUDname) {
        mudInstance = mudsMap.get(MUDname);
        return mudInstance.takeItem(username, item, location);
    }

    public String printItems(String username, String MUDname) {
        mudInstance = mudsMap.get(MUDname);
        return mudInstance.printItems(username);
    }

    public String printUsers(String MUDname) {
        mudInstance = mudsMap.get(MUDname);
        return mudInstance.printUsers();
    }

    public String printServers() {
        return ">Available MUD games:\n\t-" + String.join("\n\t-", mudsMap.keySet());
    }


    public String message(String recipient, String text) throws RemoteException {
        String output;
        if (activeUsers.containsKey(recipient)) {
            MUDClientInterface sendTo = messageBook.get(recipient);
            sendTo.sendMessage(text);
            output = ">Message sent.";
        }
        else {
            output = ">Recipient not found.";
        }
        return output;
    }

    public void callbackUpdate(String MUDname, String username, String text) throws RemoteException {
        mudInstance = mudsMap.get(MUDname);
        List<String> recipientsList = mudInstance.callbackUpdate(username);
        for (String recipient : recipientsList) {
            MUDClientInterface sendTo = messageBook.get(recipient);
            String location = retrieveLocation(recipient, MUDname);
            sendTo.sendMessage(text + "\n>Refreshing game...\n" + locationInfo(location, MUDname));
        }
    }

    public Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }

}
