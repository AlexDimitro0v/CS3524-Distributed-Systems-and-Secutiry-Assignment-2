package cs3524.solutions.mud;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
CS3524: DISTRIBUTED SYSTEMS AND SECURITY
@author Alexandar Dimitrov <a.dimitrov.17@abdn.ac.uk>
STUDENT ID: 51769669
Remote Interface of the MUD Game that defines the behaviour(functions) of the server.
*/
public interface MUDServerInterface extends Remote{
	String initializeMUD(String mudName) throws RemoteException;
	String joinMUD(String inputMud, String username) throws RemoteException;
	String myStartLocation(String MUDname) throws RemoteException;
	String locationInfo(String location, String MUDname) throws RemoteException;
	void saveLocation(String location, String username, String MUDname) throws RemoteException;
	String retrieveLocation(String username, String MUDname) throws RemoteException;
	String moveThing(String location, String direction, String thing, String MUDname) throws RemoteException;
	void addThing(String location, String thing, String MUDname) throws RemoteException;
	boolean addUser(String username, String MUDname, MUDClientInterface client) throws RemoteException;
	void logUser(String username, String MUDname) throws RemoteException;
	boolean isLogged(String username, String MUDname) throws RemoteException;
	void removeUser(String username, String MUDname) throws RemoteException;
	void deleteUser(String username) throws RemoteException;
	String takeItem(String username, String item, String location, String MUDname) throws RemoteException;
	String printItems(String username, String MUDname) throws RemoteException;
	String printUsers(String MUDname) throws RemoteException;
	String printServers() throws RemoteException;
	String message(String recipient, String text) throws RemoteException;
	void callbackUpdate(String MUDname, String username, String text) throws RemoteException;
}
