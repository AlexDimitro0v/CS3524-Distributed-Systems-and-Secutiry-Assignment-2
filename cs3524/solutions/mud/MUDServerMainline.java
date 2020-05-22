package cs3524.solutions.mud;

import java.rmi.Naming;
import java.lang.SecurityManager;
import java.rmi.server.UnicastRemoteObject;
import java.net.InetAddress;

/**
 CS3524: DISTRIBUTED SYSTEMS AND SECURITY
 @author Alexandar Dimitrov <a.dimitrov.17@abdn.ac.uk>
 STUDENT ID: 51769669
 The game server mainline that generates and registers an instance of MudServerImpl.
 */
public class MUDServerMainline {
    public static void main(String args[]){
        if (args.length < 2) {
            // Inform client for the correct usage and stop the server.
            System.err.println("Usage: \njava MUDServerMainline <registryPort> <serverPort>");
            return;
        }

        try {
            String hostname = (InetAddress.getLocalHost()).getCanonicalHostName();
            int registryPort = Integer.parseInt(args[0]);
            int serverPort = Integer.parseInt(args[1]);

            // Security Policy
            System.setProperty("java.security.policy", "mud.policy");
            System.setSecurityManager(new SecurityManager());

            // Display Information
            String regURL = "rmi://" + hostname + ":" + registryPort + "/MUDServer";
            System.out.println("Registering " + regURL + '\n');

            // Generate the remote objects that will reside on this server.
            MUDServer mudServer = new MUDServer();
            MUDServerInterface mudStub = (MUDServerInterface)UnicastRemoteObject.exportObject(mudServer, serverPort);

            Naming.rebind(regURL, mudStub);
            // Note the server will not shut down!
        }

        // Catch the errors if any
        catch(java.net.UnknownHostException e) {
            System.err.println("Cannot find localhost name.");
            System.exit(0);
        }
        catch (java.io.IOException e) {
            System.err.println("Failed to register.");
            System.err.println("Possible reason: invalid port.");
            System.exit(0);
        }
    }
}
