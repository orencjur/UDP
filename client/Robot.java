package client;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class Robot
{

    public static void main(String[] args)
            throws  Exception {
        String init = "";
        DatagramSocket socket;
        DatagramPacket packet;
        InetAddress address, fromAddress;
        String connNum;
        byte[] message;
        int fromPort, port = 4000;
        ByteArrayOutputStream toConvert = new ByteArrayOutputStream();
        Stack<byte[]> queue = new Stack<byte[]>();
        int seq = 0;
        int ack = 0;
        //
        // Send request
        //
        connNum = "00000000000000000401";
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
        message = connNum.getBytes();
        packet = new DatagramPacket(message, message.length,
                address, port);
        socket.send(packet);

        while (true) {
            try {
                packet = new DatagramPacket(message,
                        message.length);
                socket.receive(packet);
                socket.setSoTimeout(1000);
                if (packet.getLength() < 9 || packet.getLength() > 264) {
                    byte[] mess = new byte[264];
                    packet.setData(mess);
                    packet.setLength(264);
                    socket.send(packet);
                    socket.close();
                    break;
                }
                switch (packet.getLength()) {
                    case 11:
                        byte[] mess = new byte[264];
                        packet.setData(mess);
                        packet.setLength(264);
                        socket.send(packet);
                        break;
                }
                byte[] identifier = Arrays.copyOfRange(packet.getData(), 0, 3);
                byte[] sequenceNum = Arrays.copyOfRange(packet.getData(), 4, 5);
                byte[] confirmation = Arrays.copyOfRange(packet.getData(), 6, 7);
                byte sign = packet.getData()[8];
                byte[] data = Arrays.copyOfRange(packet.getData(), 9, packet.getData().length);
                if (Integer.getInteger(Arrays.toString(sequenceNum)) == ack) {
                    toConvert.writeBytes(data);
                    byte[] seqToSend = new byte[2];
                    ack += Integer.getInteger(Arrays.toString(confirmation));
                    byte[] ackToSend = ByteBuffer.allocate(2).putInt(ack).array();
                    byte[] dataToSend = new byte[0];
                    List list = new ArrayList(Arrays.asList(identifier));
                    list.addAll(Arrays.asList(seqToSend));
                    list.addAll(Arrays.asList(ackToSend));
                    list.addAll(Arrays.asList(sign));
                    list.addAll(Arrays.asList(dataToSend));
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(list);
                    byte[] toSend = bos.toByteArray();
                    packet.setData(toSend);
                    socket.send(packet);
                }else if (Integer.getInteger(Arrays.toString(sequenceNum)) > ack)
                    queue.push(packet.getData());

                socket.close();
            }
            catch(SocketException e){
                System.out.println("Socket closed " + e);
            }

            byte[] img = toConvert.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(img);
            BufferedImage bImage2 = ImageIO.read(bis);
            ImageIO.write(bImage2, "png", new File("foto.png") );
            System.out.println("image created");
        }
        }
    }
