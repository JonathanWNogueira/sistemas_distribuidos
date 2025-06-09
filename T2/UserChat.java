import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class UserChat extends UnicastRemoteObject implements IUserChat {
    private ClientGUI gui;

    public UserChat(ClientGUI gui) throws RemoteException {
        super();
        this.gui = gui;
    }

    @Override
    public void deliverMsg(String senderName, String msg) throws RemoteException {
        gui.displayMessage(senderName, msg);
    }
}