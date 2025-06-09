
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;


public class ServerChat extends UnicastRemoteObject implements IServerChat {
    private Map<String, IRoomChat> roomList;
    private static Registry registry;

    public ServerChat() throws RemoteException {
        super();
        this.roomList = new HashMap<>();
    }

    @Override
    public ArrayList<String> getRooms() throws RemoteException {
        return new ArrayList<>(roomList.keySet());
    }

    @Override
    public void createRoom(String roomName) throws RemoteException {
        if (!roomList.containsKey(roomName)) {
            RoomChat newRoom = new RoomChat(roomName);
            roomList.put(roomName, newRoom);

            try {
                registry.rebind(roomName, newRoom);
                System.out.println("Sala '" + roomName + "' criada e registrada com sucesso.");
            } catch (Exception e) {
                roomList.remove(roomName);
                throw new RemoteException("Falha ao registrar sala: " + e.getMessage());
            }
        } else {
            throw new RemoteException("Sala já existe.");
        }
    }

    public void closeRoom(String roomName) throws RemoteException {
        if (roomList.containsKey(roomName)) {
            try {
                IRoomChat room = roomList.get(roomName);
                room.closeRoom();
                registry.unbind(roomName);
                roomList.remove(roomName);
                System.out.println("Sala '" + roomName + "' fechada com sucesso.");
            } catch (Exception e) {
                throw new RemoteException("Erro ao fechar sala: " + e.getMessage());
            }
        }
    }

    private static Registry initRMI()
    {
        try {
            return registry = LocateRegistry.createRegistry(2020);
        } catch(Exception e)
        {
            System.out.println("Erro brabo" + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        registry = initRMI();
        try {
            ServerChat server = new ServerChat();
            registry.rebind("Servidor", server);
            
            SwingUtilities.invokeLater(() -> new ServerGUI(server));
            
            System.out.println("Servidor pronto na porta 2020...");
        } catch (Exception e) {
            System.err.println("Erro no servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}