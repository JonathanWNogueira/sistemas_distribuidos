package StableMulticast;
import java.net.InetSocketAddress;
import java.util.Hashtable;

public class Message {
    public String prefix, content;          // tipo e conteúdo da mensagem
    private InetSocketAddress address;      // endereço do remetente
    private Hashtable<InetSocketAddress, Integer> vectorClock = new Hashtable<>();   // relógio vetorial
    
    public Message(String prefix, String content, InetSocketAddress address, Hashtable<InetSocketAddress, Integer> vectorClock) {
        this.prefix = prefix;
        this.content = content;
        this.address = address;
        this.vectorClock = vectorClock;
    }

    // getters
    public String getContent() { return content; }
    public String getPrefix() { return prefix; }
    public InetSocketAddress getAddress() { return address; }
    public Hashtable<InetSocketAddress, Integer> getVectorClock() { return vectorClock; }
    public String getIP() { return address.getAddress().getHostAddress(); }
    public int getPort() { return address.getPort(); }
    
    // setters
    public void setvectorClock(Hashtable<InetSocketAddress, Integer> vectorClock) { this.vectorClock = vectorClock; }

    // processo de serialização da mensagem
    
    // converte o relogio vetorial em uma string
    public String vectorClockToString() {
        String vectorClockString = "";
        for (InetSocketAddress key : vectorClock.keySet())
            vectorClockString += key.getAddress().getHostAddress() + "_" + key.getPort() + "=" + vectorClock.get(key) + ",";
        return vectorClockString;
    }

    // "compacta" tudo na mensagem
    public String generateStringMessage() {
        return prefix + ":" + content + ":" + address.getAddress().getHostAddress() + ":" + address.getPort() + ":" + vectorClockToString();
    }

    // recosntroi a mensagem original
    public static Message parseStringMessage(String msg) {
        String[] parts = msg.split(":");
        String prefix = parts[0], content = parts[1];
        InetSocketAddress address = new InetSocketAddress(parts[2], Integer.parseInt(parts[3]));

        Hashtable<InetSocketAddress, Integer> vectorClock = new Hashtable<>();
        String[] vectorClockParts = parts[4].split(",");
        for (String vectorClockPart : vectorClockParts) {
            String[] vectorClockPair = vectorClockPart.split("=");
            InetSocketAddress vectorClockAddress = new InetSocketAddress(vectorClockPair[0].split("_")[0], Integer.parseInt(vectorClockPair[0].split("_")[1]));
            vectorClock.put(vectorClockAddress, Integer.parseInt(vectorClockPair[1]));
        }
        return new Message(prefix, content, address, vectorClock);
    }

    // formata a mensagem para o print
    public String getMessageText() {
        String ip = address.getAddress().getHostAddress();
        String port = String.valueOf(address.getPort());
        
        if(prefix.equals("joined") | prefix.equals("already joined"))
            return ip + ":" + port + "  " + prefix;

        return "\n"  + ip + ":" + port + " ===== enviou ===== "  + content;
    }
}