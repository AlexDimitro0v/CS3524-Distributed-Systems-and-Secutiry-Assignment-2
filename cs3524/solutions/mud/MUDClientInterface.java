package cs3524.solutions.mud;
import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 CS3524: DISTRIBUTED SYSTEMS AND SECURITY
 @author Alexandar Dimitrov <a.dimitrov.17@abdn.ac.uk>
 STUDENT ID: 51769669
 Remote Interface used for the messaging.
 */
public interface MUDClientInterface extends Remote {
    void sendMessage(String message) throws RemoteException;
}
