import javax.swing.*;

import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class ClientGUI extends JFrame {
    private String userName;
    private String serverIP;
    private IUserChat userStub;
    private IRoomChat currentRoom;
    private IServerChat serverStub;

    private JTextArea chatArea;
    private JTextField messageField;
    private JComboBox<String> roomList;
    private JButton joinButton, leaveButton, sendButton, createButton;

    public ClientGUI(String userName, String serverIP) {
        this.userName = userName;
        this.serverIP = serverIP;
        initializeGUI();
        connectToServer();
    }

    private void initializeGUI() {
        setTitle("Chat RMI - " + userName);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Seleção de Salas
        JPanel roomPanel = new JPanel();
        roomList = new JComboBox<>();
        roomPanel.add(new JLabel("Salas:"));
        roomPanel.add(roomList);

        // Entrada na Sala
        joinButton = new JButton("Entrar");
        joinButton.addActionListener(e -> joinRoom());
        roomPanel.add(joinButton);

        // Saida da Sala
        leaveButton = new JButton("Sair");
        leaveButton.addActionListener(e -> leaveRoom());
        roomPanel.add(leaveButton);

        // Criação de Sala
        createButton = new JButton("Criar Sala");
        createButton.addActionListener(e -> createRoom());
        roomPanel.add(createButton);

        // Atualiza as Salas Disponíveis
        JButton refreshButton = new JButton("Atualizar");
        refreshButton.addActionListener(e -> updateRoomList());
        roomPanel.add(refreshButton);

        bottomPanel.add(roomPanel, BorderLayout.NORTH);

        // Mensagem
        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("Enviar");
        sendButton.addActionListener(e -> sendMessage());

        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        bottomPanel.add(messagePanel, BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry(serverIP, 2020);
            serverStub = (IServerChat) registry.lookup("Servidor");
            userStub = new UserChat(this);
            updateRoomList();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao conectar ao servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateRoomList() {
        try {
            ArrayList<String> rooms = serverStub.getRooms();
            roomList.removeAllItems();
            for (String room : rooms) roomList.addItem(room);   
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar lista de salas: " + e.getMessage());
        }
    }

    private void joinRoom() {
        if (currentRoom != null) {
            JOptionPane.showMessageDialog(this, "Voce ja esta em uma sala.\nSaia da sala atual antes de entrar em outra.");
            return;
        }
    
        String selectedRoom = (String) roomList.getSelectedItem();

        if (selectedRoom == null || selectedRoom.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione uma sala valida.");
            return;
        }
    
        try {
            Registry registry = LocateRegistry.getRegistry(serverIP, 2020);
            currentRoom = (IRoomChat) registry.lookup(selectedRoom);
            currentRoom.joinRoom(userName, userStub);
            displayMessage("SISTEMA", "Voce entrou na sala " + selectedRoom);
            
            joinButton.setEnabled(false);
            leaveButton.setEnabled(true);
        } catch (Exception e) {
            currentRoom = null;
            JOptionPane.showMessageDialog(this, "Erro ao entrar na sala: " + e.getMessage());
        }
    }

    private void leaveRoom() {
        if (currentRoom == null) return;
    
        try {
            currentRoom.leaveRoom(userName);
            displayMessage("SISTEMA", "Voce saiu da sala.");
            currentRoom = null;
            
            // Atualiza estado dos botões
            joinButton.setEnabled(true);
            leaveButton.setEnabled(false);
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Erro ao sair da sala: " + e.getMessage());
        }
    }

    private void createRoom() {
        String newRoomName = JOptionPane.showInputDialog(this, "Digite o nome da nova sala:");
        if (newRoomName == null || newRoomName.trim().isEmpty()) return;

        try {
            serverStub.createRoom(newRoomName);
            updateRoomList();
            roomList.setSelectedItem(newRoomName);
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Erro ao criar sala: " + e.getMessage());
        }
    }

    private void sendMessage() {
        if (currentRoom == null) {
            JOptionPane.showMessageDialog(this, "Voce nao esta em nenhuma sala!");
            return;
        }

        String message = messageField.getText();
        if (message == null || message.trim().isEmpty()) return;

        try {
            currentRoom.sendMsg(userName, message);
            messageField.setText("");
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Erro ao enviar mensagem: " + e.getMessage());
        }
    }

    public void displayMessage(String sender, String message) {
        chatArea.append(sender + ": " + message + "\n");
    }

    public static void main(String[] args) {
        String serverIP = args[0];
        
        String userName = JOptionPane.showInputDialog("Digite seu nome de usuario:");
        if (userName != null && !userName.trim().isEmpty()) {
            new ClientGUI(userName.trim(), serverIP);
        }
    }
}