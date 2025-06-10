package StableMulticast;
import java.io.*;
import java.net.*;
import java.util.*;

public class StableMulticast {
    static final int N = 3;     // máximo de membros 

    public IStableMulticast client;         

    private String ip, ipMult = "230.0.0.0";    // ip local e do multicast
    private Integer port, portMult = 4446;      // porta local e do multicast

    private InetSocketAddress address;  // endereço completo

    private DatagramSocket unicast;     // socket uni
    private MulticastSocket multicast;  // socket multi

    private int[][] clockMatrix;        // matriz de relógios vetoriais

    private Set<InetSocketAddress> members;                 // membros   
    private HashMap<InetSocketAddress, Integer> memberId;   // membro -> id
    private List<Message> messages;                         // buffer
    

    @SuppressWarnings("deprecation")
    public StableMulticast(String ip, Integer port, IStableMulticast client) throws IOException {
        this.ip = ip;
        this.port = port;
        this.client = client;
        address = new InetSocketAddress(ip, port);
        ClockMatrix();

        // Configuração do socket unicast
        unicast = new DatagramSocket(port);
        members = new HashSet<>();
        messages = new ArrayList<>();
        memberId = new HashMap<>(); 
        members.add(address);             // adiciona o próprio endereço a lista de membros
        memberId.put(address, 0);     

        // Configuração do socket multicast
        multicast = new MulticastSocket(portMult);
        multicast.joinGroup(InetAddress.getByName(ipMult));

        // inicializa o relogio vetorial
        Hashtable<InetSocketAddress, Integer> vectorClock = new Hashtable<>();
        vectorClock.put(new InetSocketAddress(ip, port), 0);
        for(int i = 1; i < N; i++)
            vectorClock.put(new InetSocketAddress("0.0.0.0", 0), -1);

        // mensagem de entrada
        Message message = new Message("joined", "", address, vectorClock);
        send(message, true, null);

        // threads de recebimento de mensagens
        new Thread(() -> receive(false)).start(); // unicast
        new Thread(() -> receive(true)).start();  // multicast
        
    }

    private void ClockMatrix() {
        clockMatrix = new int[N][N];
        for(int i = 0; i < clockMatrix.length; i++)
            for(int j = 0; j < clockMatrix[i].length; j++)
                clockMatrix[i][j] = -1;
        clockMatrix[0][0] = 0;
    }

    public void printMatrix() {
        HashMap<Integer, InetSocketAddress> idToMember = new HashMap<>();
        
        // mapeia id para endereço
        for(Map.Entry<InetSocketAddress, Integer> entry : memberId.entrySet())
            idToMember.put(entry.getValue(), entry.getKey());
    
        // Cabeçalho com alinhamento correto
        System.out.println("\nMatriz de Relogios:");
        System.out.print(String.format("%-20s", ""));
        
        // Imprime cabeçalhos das colunas
        for(int i = 0; i < N; i++) {
            if(idToMember.containsKey(i)) {
                InetSocketAddress member = idToMember.get(i);
                String address = member.getAddress().getHostAddress() + ":" + member.getPort();
                System.out.print(String.format("%-20s", address));
            }
        }
        System.out.println();
    
        // imprime linhas da matriz
        for(int i = 0; i < N; i++) {
            if(idToMember.containsKey(i)) {
                InetSocketAddress member = idToMember.get(i);
                String address = member.getAddress().getHostAddress() + ":" + member.getPort();
                System.out.print(String.format("%-20s", address));
            }
    
            // Valores da matriz
            for(int j = 0; j < N; j++)
                System.out.print(String.format("%-20d", clockMatrix[i][j]));
            System.out.println();
        }
        
        // Linha separadora
        System.out.println("-".repeat(20 * (N + 1)));

        printBuffer();
    }

    public void printBuffer(){
        System.out.println("Buffer de mensagens:");
        for(Message m : messages)
            System.out.println(m.getIP() + ":" + m.getPort() + " - " + m.getContent());
    }
    
    // procedure mcsend (msg)
    @SuppressWarnings("resource")
    public void msend(String msg, IStableMulticast client) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enviar mensagem para todos? (s/n): ");
        String input = scanner.nextLine().trim().toLowerCase();

        Hashtable<InetSocketAddress, Integer> VectorClock = new Hashtable<>();

        for(InetSocketAddress member : members)
            VectorClock.put(member, clockMatrix[0][memberId.get(member)]);  // timestamp

        Message message = new Message("msg", msg, address, VectorClock);    

        if(input.equals("s"))
            send(message,true, null);
        else
            for(InetSocketAddress member : members) {
                System.out.print("Enviar mensagem para " + member + "?");
                scanner.nextLine();
                send(message, false, member);
            }
        
        clockMatrix[0][0] += 1; // atualiza seu relógio
        printMatrix();
    }

    private void send(Message message, Boolean isMulticast ,InetSocketAddress member) {
        try {
            byte[] buffer = message.generateStringMessage().getBytes();
            if (isMulticast) 
                multicast.send(new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ipMult), portMult));
            else
                unicast.send(new DatagramPacket(buffer, buffer.length, InetAddress.getByName(member.getAddress().getHostAddress()), member.getPort()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // when Pi receives msg from Pj
    private void receive(boolean isMulticast) {
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                if(isMulticast) multicast.receive(packet);
                else unicast.receive(packet);
                
                // processa a msg
                String content = new String(packet.getData(), 0, packet.getLength());
                Message msg = Message.parseStringMessage(content);
    
                if (msg.getPrefix().equals("msg")) {
                    messages.add(msg);  // adiciona a mensagem ao buffer
                    
                    // when (existe msg no buffer)
                    Iterator<Message> iterator = messages.iterator();
                    while(iterator.hasNext()) {
                        Message m = iterator.next();
                        InetSocketAddress messageMember = new InetSocketAddress(m.getIP(), m.getPort());
                        Integer messageMemberIdx = memberId.get(messageMember);
                        if(messageMemberIdx == null) continue;
                        
                        boolean remove = false;
                        
                        // msg.VC[msg.sender] ≤ min1≤x≤n(MCi[x][msg.sender])
                        for(int i = 0; i < N; i++)
                            if(m.getVectorClock().get(messageMember) <= clockMatrix[i][messageMemberIdx]) {
                                System.out.println("VAI DISCARTAR AQUI!!");
                                remove = true;
                                break;
                            }
                        if(remove) iterator.remove(); // elimina a msg do buffer
                    }
                
                    if (!(msg.getIP().equals(ip) && msg.getPort() == port)) {
                        Integer idx = memberId.get(new InetSocketAddress(msg.getIP(), msg.getPort()));
                        if (idx == null) {
                            System.err.println("Unknown sender: " + msg.getIP() + ":" + msg.getPort());
                            return;
                        }
                
                        Enumeration<InetSocketAddress> keys = msg.getVectorClock().keys();
                        while(keys.hasMoreElements()) {
                            InetSocketAddress member = keys.nextElement();
                            Integer memberIdx = memberId.get(member);
                            
                            if(memberIdx != null)
                                clockMatrix[idx][memberIdx] = msg.getVectorClock().get(member); // atualiza visão do Pi com visão de Pj
                        }
                        clockMatrix[0][idx] += 1; // mais 1 msg de Pj entregue
                        printMatrix();
                    }
                }
                
                // a msg foi enviada por si mesmo
                if(msg.getIP().equals(ip) && msg.getPort() == port) continue;
    

                if(isMulticast && msg.getPrefix().equals("joined")) {
                    // detecta e adiciona o novo membro na lista
                    InetSocketAddress member = new InetSocketAddress(msg.getIP(), msg.getPort());
                    members.add(member);
                    memberId.put(member, members.size() - 1);
                    
                    // inclui um relogio vetorial inicial
                    Hashtable<InetSocketAddress, Integer> vectorClock = new Hashtable<>();
                    vectorClock.put(new InetSocketAddress(ip, port), 0);
                    for (int i = 1; i < N; i++)
                        vectorClock.put(new InetSocketAddress("0.0.0.0", 0), -1);

                    // envia confirmação via unicast
                    send(new Message("already joined", "", address, vectorClock), false, member);
                }
    
                if(!isMulticast && msg.getPrefix().equals("already joined")) {
                    // novo membro "conhece" os membros do grupo
                    InetSocketAddress member = new InetSocketAddress(msg.getIP(), msg.getPort());
                    members.add(member);
                    memberId.put(member, members.size() - 1);
                }
                
                client.deliver(msg.getMessageText()); // entrega a mensagem
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}