
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class RoomChat extends UnicastRemoteObject implements IRoomChat {
    private String roomName;
    private Map<String, IUserChat> userList;

    public RoomChat(String roomName) throws RemoteException {
        super();
        this.roomName = roomName;
        this.userList = new HashMap<>();
    }

    @Override
    public void sendMsg(String usrName, String msg) throws RemoteException {
        for (IUserChat user : userList.values()) {
            user.deliverMsg(usrName, msg);
        }
    }

    @Override
    public void joinRoom(String userName, IUserChat user) throws RemoteException {
        synchronized(userList) {
            userList.put(userName, user);
        }
        sendMsg("SISTEMA", userName + " entrou na sala.");
    }

    @Override
    public void leaveRoom(String usrName) throws RemoteException {
        synchronized(userList) {
            if (userList.containsKey(usrName)) {
                userList.remove(usrName);
            }
        }
        sendMsg("SISTEMA", usrName + " saiu da sala.");
    }

    @Override
    public String getRoomName() throws RemoteException {
        return roomName;
    }

    @Override
    public void closeRoom() throws RemoteException {
        if (!userList.isEmpty()) {
            sendMsg("SISTEMA", "Sala fechada pelo servidor.");
        }
        userList.clear();
    }
}