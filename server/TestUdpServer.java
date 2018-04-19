package win.wilber.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Random;

/**
 * Created by wilber on 18-4-10.
 */
public class TestUdpServer {
    private final int MAX_LEN = 1024;
    private final int PORT = 12345;

    private byte[] receiveMsgs = new byte[MAX_LEN];

    private DatagramSocket datagramSocket;
    private DatagramPacket datagramPacket;

    public TestUdpServer() {
        try {
            datagramSocket = new DatagramSocket(PORT);

            while (true) {
                datagramPacket = new DatagramPacket(receiveMsgs,receiveMsgs.length);
                datagramSocket.receive(datagramPacket);

                String receiverStr = new String(datagramPacket.getData(),0,datagramPacket.getLength());
                System.out.println("server receiver:" + receiverStr);

                System.out.println("server port:" + datagramPacket.getPort());

                byte[] buf = (System.currentTimeMillis() + " I receive the message\n").getBytes();

                DatagramPacket sendPacket = new DatagramPacket(buf,buf.length,datagramPacket.getAddress(),datagramPacket.getPort());
                datagramSocket.send(sendPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null != datagramSocket) {
                datagramSocket.close();
            }
        }
    }

    public static void main(String[] args) {
        new TestUdpServer();
    }
}
