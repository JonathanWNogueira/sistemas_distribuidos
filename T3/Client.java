import StableMulticast.*;
import java.io.IOException;
import java.util.Scanner;

public class Client implements IStableMulticast {
    
    private StableMulticast stableMulticast;

    public Client(String ip, Integer port) throws IOException {
        stableMulticast = new StableMulticast(ip, port, this);  // inicializan o middleware
    }

    @Override
    public void deliver(String msg) {
        System.out.println(msg);                                // lógica para tratar a entrega da mensagem recebida
    }
    
    @SuppressWarnings("resource")
    public static void main(String[] args) {
        String ip = args[0];
        int port = Integer.parseInt(args[1]);
        try {
            Client client = new Client(ip, port);               // cria um novo cliente

            Scanner scanner = new Scanner(System.in);           
            while(true) {
                String msg = scanner.nextLine();                // captura a mensagen
                client.stableMulticast.msend(msg, client);      // envia a mensagem
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}