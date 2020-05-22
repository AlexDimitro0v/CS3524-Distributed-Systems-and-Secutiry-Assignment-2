package cs3524.solutions.mud;


public class MUDClientImpl implements MUDClientInterface{
    public void sendMessage(String message) {
        System.out.print(message + "\n>");
    }
}
