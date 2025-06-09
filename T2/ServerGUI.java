
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class ServerGUI extends JFrame {
    private ServerChat server;
    private JList<String> roomList;
    private DefaultListModel<String> listModel;
    private JButton refreshButton, closeRoomButton;

    public ServerGUI(ServerChat server) {
        this.server = server;
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Servidor RMI");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Lista de salas
        listModel = new DefaultListModel<>();
        roomList = new JList<>(listModel);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(roomList);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout());

        refreshButton = new JButton("Atualizar");
        refreshButton.addActionListener(this::refreshRooms);
        buttonPanel.add(refreshButton);

        closeRoomButton = new JButton("Fechar Sala");
        closeRoomButton.addActionListener(this::closeSelectedRoom);
        buttonPanel.add(closeRoomButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);

        // Atualiza a lista inicial
        refreshRooms();
    }

    private void refreshRooms(ActionEvent e) {
        refreshRooms();
    }

    private void refreshRooms() {
        try {
            ArrayList<String> rooms = server.getRooms();
            listModel.clear();
            for (String room : rooms) listModel.addElement(room);
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar lista de salas: " + e.getMessage());
        }
    }

    private void closeSelectedRoom(ActionEvent e) {
        String selectedRoom = roomList.getSelectedValue();
        if (selectedRoom == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma sala para fechar.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Fechar a sala '" + selectedRoom + "'? Todos os usuarios serao desconectados.",
            "Confirmar",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                server.closeRoom(selectedRoom);
                refreshRooms();
                JOptionPane.showMessageDialog(this, "Sala '" + selectedRoom + "' fechada com sucesso.");
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao fechar sala: " + ex.getMessage());
            }
        }
    }
}