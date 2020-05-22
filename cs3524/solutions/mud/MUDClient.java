package cs3524.solutions.mud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.lang.SecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


/**
 CS3524: DISTRIBUTED SYSTEMS AND SECURITY
 @author Alexandar Dimitrov <a.dimitrov.17@abdn.ac.uk>
 STUDENT ID: 51769669
 */
public class MUDClient {
    // Initialize the variables to be used:
    private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));    // To read from client
    private static MUDServerInterface serv = null;                                              // Communication with the server - ensured
    private static MUDClientInterface client = null;                                            // Used for the messaging
    private static String username = null;                                                      // To store the username of the client
    private static String location = null;                                                      // To follow the user's current location
    private static String mudName = null;                                                       // To store the name of the current MUD Server
    private static boolean usernameAdded = false;                                               // To control if the username is the client has provided a valid username

    public static void main(String[] args) throws RemoteException {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            /** This handler will be called on Control-C pressed */
            // Source: https://www.tutorialspoint.com/java/lang/runtime_addshutdownhook.htm?fbclid=IwAR1ssLAl9_gIQn4g9eHOueJesr9L3biRYp04Xk0CBWrTFdLAhBxuK2swkaU
            @Override
            public void run() {
                System.out.println("\nSTOPPED !!!");
                if (username != null) {
                    System.out.println("All user's items are being dropped." +
                            "\nAll user's logins are being deleted.");
                }

                // Detect if the user exits the game by pressing Control+C,
                // remove the user and delete him from the corresponding location:
                if (usernameAdded) {
                    try {
                        serv.deleteUser(username);               // delete the user from the active users
                    }
                    catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        if (args.length < 3) {
            System.err.println("Usage:\njava MUDClient <host> <port> <callbackport>") ;
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        int callbackPort = Integer.parseInt(args[2]) ;

        // Specify the security policy and set the security manager.
        System.setProperty("java.security.policy", "mud.policy") ;
        System.setSecurityManager(new SecurityManager()) ;
        try {
            // Connect the client
            MUDClientImpl user = new MUDClientImpl();       // create a client instance
            client = (MUDClientInterface) UnicastRemoteObject.exportObject(user, callbackPort);

            // Obtain the server handle from the RMI registry
            // listening at hostname:port.
            String regURL = "rmi://" + hostname + ":" + port + "/MUDServer";
            System.out.println("Looking up " + regURL + '\n');
            serv = (MUDServerInterface)Naming.lookup(regURL);

            // Start the game
            System.out.println(serv.printServers());
            connect();
        }

        catch (java.rmi.NotBoundException e) {
            System.err.println("Server not found.");
            System.exit(0);
        }

        catch (java.io.IOException e) {
            System.err.println("Failed to connect.");
            System.err.println("Possible reasons: Wrong or conflicting port numbers.");
            System.exit(0);
        }
    }


    static void connect() throws IOException {
        String command;
        String choice = System.console().readLine(">Join a MUD\n" +
                "\t1) Type 'join <MUD name>' to connect to an existing game\n" +
                "\t2) Type 'create <MUD name>' to create and connect to your own\n" +
                "\t3) Type 'view' to see the list of available MUDS\n" +
                ">Command: ").trim();
        while(choice.length() == 0 || ((!(command = choice.split(" ")[0]).equalsIgnoreCase("join")) &&
                (!(command = choice.split(" ")[0]).equalsIgnoreCase("create")) &&
                (!(command = choice.split(" ")[0]).equalsIgnoreCase("view")))){
            // Iterate until a valid command is provided
            System.out.print(">Invalid command.\n");
            choice = System.console().readLine(
                    "\t1) Type 'join <MUD name>' to connect to an existing game\n" +
                         "\t2) Type 'create <MUD name>' to create and connect to your own\n" +
                         "\t3) Type 'view' to see the list of available MUDS\n" +
                            ">Command: ").trim();
        }

        try {
            mudName = choice.split(" ")[1];
            if (!command.equalsIgnoreCase("join")) {   // command == 'create'
                String mudInitialized = serv.initializeMUD(mudName);
                switch (mudInitialized) {
                    case "true":
                        System.out.println(">You've successfully created and joined " + mudName + '!');
                        break;
                    case "false":
                        System.out.println(">***MUD name already exist.***");
                        connect();
                        break;
                    case "serverLimit":
                        System.out.println(">***Games limit on server reached. Try joining one instead.***");
                        connect();
                        break;
                }
            }

            String mudJoined = serv.joinMUD(mudName, username);

            switch (mudJoined) {
                case "true":
                    if (!command.equalsIgnoreCase("create")) {
                        System.out.println(">You successfully joined " + mudName + '!');
                    }
                    break;
                case "false":
                    System.out.println(">***There is no such MUD.***");
                    connect();
                    break;
                case "playersLimit":
                    System.out.println(">***Sorry, the MUD game is full. Try again later or join another one instead.***");
                    connect();
                    break;
            }
        }
        catch (Exception e){
            if(command.equalsIgnoreCase("view")){
                System.out.println(serv.printServers());
            }
            else {
                System.out.println(">***MUD name is not provided.***");
            }
            connect();      // Recursive call - cover any 'tricks' by the user
        }
        gameFunction();
    }


    static void gameFunction() throws RemoteException, IOException {
        String userMove;                                          // To record the user input

        if(!usernameAdded) {

            System.out.print("\n>Provide a username: ");
            username = in.readLine().trim();
            usernameAdded = serv.addUser(username, mudName, client);

            // Validate the username using a while loop:
            while (!usernameAdded || username.length() == 0 || username.contains(" ")) {
                if (username.length() == 0) {
                    System.out.print(">Invalid username. \nProvide another one: ");
                } else if (!usernameAdded) {
                    System.out.print(">The username is already taken. \nProvide another one: ");
                } else if (username.contains(" ")) {
                    System.out.print(">Invalid username. No spaces allowed. \nProvide another one: ");
                }
                username = in.readLine().trim();
                usernameAdded = serv.addUser(username, mudName, client);  // try adding the user to the list of active users if username is valid
            }
        }


        System.out.println("\n============================== WELCOME TO THE MUD WORLD, " + username + '!' + " ==============================");

        serv.addUser(username, mudName, client);                                 // Add the user if not logged in
        if (!serv.isLogged(username, mudName)) {
            location = serv.myStartLocation(mudName);                            // Record the user current location
            serv.saveLocation(location, username, mudName);                      // Saves the location
            serv.addThing(location, username + "(client)", mudName);       // Add the user to the start location node
            serv.callbackUpdate(mudName, username, "A new player has joined the game. You now have company.");
            serv.logUser(username, mudName);                                     // Flag the user as logged in
            helpMenu();
            System.out.println(">Your start location is node: " + location);
        }
        else {
            // The user comes back to play an abandoned game (already has been logged in)
            location = serv.retrieveLocation(username, mudName);
            System.out.println(">Your current location is node: " + location);
        }


        // Game Loop:
        System.out.print('>');
        while(!(userMove = in.readLine()).trim().equals("exit")){
            String command = userMove.split(" ")[0];         // get the user command

            if(command.equalsIgnoreCase("move")){
                try {
                    String direction = userMove.split(" ")[1];
                    if (direction.equalsIgnoreCase("north") || direction.equalsIgnoreCase("east") || direction.equalsIgnoreCase("south") || direction.equalsIgnoreCase("west")) {
                        String oldLocation = location;
                        String newLocation = serv.moveThing(oldLocation, direction.toLowerCase(), username + "(client)", mudName);
                        if (newLocation.equals(oldLocation)) {
                            System.out.println("\n>You cannot move '" + direction + "'.");
                        } else {
                            location = newLocation;                   // update the location
                            serv.saveLocation(location, username, mudName);                      // Saves the location
                            System.out.println("\n>You moved " + direction + ". Your new location is " + "'" + location + "':");
                            System.out.println(serv.locationInfo(location, mudName));
                            serv.callbackUpdate(mudName, username, username + " has moved " + direction + " in game '" + mudName +  "'.");
                        }
                    } else {
                        System.out.println("\n>Invalid direction. Check your spelling.\n");
                    }
                }
                catch (Exception e) {
                    System.out.println(">Direction is not provided.");
                }
            }

            else if(command.equalsIgnoreCase("pick")){
                try {
                    String item = userMove.split(" ")[1];
                    System.out.println(serv.takeItem(username, item, location, mudName));
                }
                catch (Exception e) {
                    System.out.println("Item name not provided.");
                }
            }

            else if(command.equalsIgnoreCase("return")){
                System.out.println(serv.locationInfo(location, mudName));
            }


            else if(command.equalsIgnoreCase("help")){
                helpMenu();
            }

            else if(userMove.equalsIgnoreCase("show items")){
                System.out.println(serv.printItems(username, mudName));
            }

            else if(userMove.equalsIgnoreCase("show users")){
                System.out.println(serv.printUsers(mudName));
            }

            else if(userMove.equalsIgnoreCase("show muds")){
                System.out.println(serv.printServers());
            }

            else if(command.equalsIgnoreCase("change")) {
                serv.saveLocation(location, username, mudName);
                connect();
            }

            else if(command.equalsIgnoreCase("create")){
                try {
                    String newMUDName = userMove.split(" ")[1];
                    String mudInitialized = serv.initializeMUD(newMUDName);
                    switch (mudInitialized) {
                        case "true":
                            System.out.println(">You successfully created " + newMUDName + '!');
                            System.out.println(serv.printServers());
                            System.out.println(">You can now switch to the newly created game by typing 'change'" +
                                    "or continue playing.");
                            break;
                        case "false":
                            System.out.println(">MUD name already exist.");
                            break;
                        case "serverLimit":
                            System.out.println(">Games limit on server reached.");
                            break;
                    }
                }
                catch (Exception e) {
                    System.out.println("MUD name not provided.");
                }
            }

            else if(command.equalsIgnoreCase("message")){
                System.out.println("You can message to one of the following " + serv.printUsers(mudName));
                String sendTo = System.console().readLine( "Who do you want to send the message to: " ).trim();
                if(username.equals(sendTo)){
                    System.out.print(">Sending a message to yourself doesn't make much sense, does it?\n>");
                    continue;
                }
                String text = "Message from " + username + ", playing in '" + mudName + "': ---"  + System.console().readLine( "Write the message: " ).trim();
                System.out.println(serv.message(sendTo, text));
            }

            else{
                System.out.println(">Invalid command. Type 'help' to see the list of available commands.");
            }
            System.out.print('>');
        }

        // Once the user exits the game by pressing 'exit',
        // remove the user and delete him from the corresponding location:
        serv.removeUser(username, mudName);                  // remove the user from the users list and the inventory map
        System.out.println(">***You have successfully left the '" + mudName + "' MUD game.***");
        serv.callbackUpdate(mudName, username, username + " has left " + mudName + '.');
        connect();
    }


    static void helpMenu() throws RemoteException{
        System.out.println(">List of commands that you can use:\n" +
                "\t1.help (to see the list of available commands)\n" +
                "\t2.return (to return information about the location you are currently in)\n" +
                "\t3.move <cardinal direction: {north, east, south, west}> (to move around)\n" +
                "\t4.pick <item> (to pick an item)\n" +
                "\t5.show items (to see the list of picked items)\n" +
                "\t6.show users (to see the list of active users in the current MUD world)\n" +
                "\t7.show muds (to see the list of available MUD games)\n" +
                "\t8.change (to switch to another MUD game)\n" +
                "\t9.create <MUD name> (to create a new MUD game and join it)\n" +
                "\t10.message (to send messages to other users)\n" +
                "\t11.exit (to exit the game)\n"
        );
    }
}
