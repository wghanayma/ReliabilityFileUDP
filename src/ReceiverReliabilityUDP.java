import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class ReceiverReliabilityUDP {
    static int NumberOfPackets;
    private final FileMessageEvent fileMessageEvent;
    private DatagramSocket datagramSocket;
    private final int port;
    private final String SIPAddress;
    private int mCountError = 0;

    public ReceiverReliabilityUDP(FileMessageEvent fileMessageEvent, int port, String SIPAddress) {
        if (fileMessageEvent == null) {
            throw new RuntimeException("Message Event can't be null");
        }
        this.fileMessageEvent = fileMessageEvent;
        this.port = port;
        this.SIPAddress = SIPAddress;
    }

    private static void sendAck(int foundLast, DatagramSocket socket, InetAddress address, int port) throws IOException {
        Packet packet = new Packet();
        packet.sequenceNumber = foundLast;
        ByteBuffer byteBuf = packet.toByteBuffer();//byteBuf is just a reference also packet.buffer
        byte[] sendData = byteBuf.array();
        DatagramPacket acknowledgement = new DatagramPacket(sendData, packet.getPacketLength(), address, port);
        socket.send(acknowledgement);
    }

    public void start() throws SocketException {
        InetAddress IPAddress = null;
        try {
            IPAddress = InetAddress.getByName(SIPAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println(SIPAddress + ":" + this.port);
        String filePath = "C:\\Users\\wasim\\IdeaProjects\\ReliabilityFileUDP\\src\\receiveFile";
        datagramSocket = new DatagramSocket(this.port, IPAddress);
        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        byte[] byteReceived = new byte[4096];
                        DatagramPacket datagramPacket = new DatagramPacket(byteReceived, byteReceived.length);
                        datagramSocket.receive(datagramPacket);
                        byte[] data = datagramPacket.getData();
                        String fileName = new String(data, 0, datagramPacket.getLength());
                        fileMessageEvent.onMessageReceivedFile(fileName);
                        File f = new File(filePath + "\\" + fileName);
                        FileOutputStream outToFile = new FileOutputStream(f);
                        receiveFile(outToFile, datagramSocket);
                        fileMessageEvent.onError(mCountError);
                        mCountError = 0;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void receiveFile(FileOutputStream outToFile, DatagramSocket socket) throws IOException {
          int sequenceNumber = 0;
        int lastSequenceNumber = 0;
        while (true) {
            byte[] message = new byte[1024];
            byte[] fileByteArray = new byte[1004];
            // Receive packet and retrieve the data
            DatagramPacket receivedPacket = new DatagramPacket(message, message.length);
            socket.receive(receivedPacket);
            message = receivedPacket.getData();
            Packet packet = new Packet();
            ByteBuffer rcvBuf = ByteBuffer.wrap(message);
            packet.extractPacketfromByteBuffer(rcvBuf);
            packet.toByteBuffer();
            int packetReceivedCheckSum = packet.checkSum;
            Packet packetNew = new Packet(0, packet.sequenceNumber, packet.ackSequence);
            packetNew.FIN = packet.FIN;
            packetNew.addData(packet.data, packet.data.length);
            packetNew.toByteBuffer();
            packetNew.generateChecksum(0);
            int packetReceiverCheckSum = packetNew.checkSum;
            InetAddress address = receivedPacket.getAddress();
            int port = receivedPacket.getPort();
            sequenceNumber = packetNew.sequenceNumber;
            if (packetReceiverCheckSum != packetReceivedCheckSum) {
                System.err.println("Received: Sequence number:" + sequenceNumber + ",but Packet Received CheckSum :" + packetReceivedCheckSum + " is not equal  Receiver CheckSum : " + packetReceiverCheckSum);
                ++mCountError;
            }
            if (sequenceNumber == (lastSequenceNumber )) {
                System.err.println("Expected sequence number: " + (lastSequenceNumber ) + " but received " + sequenceNumber + ". DISCARDING");
            }
            if (sequenceNumber == (lastSequenceNumber ) || packetReceiverCheckSum != packetReceivedCheckSum) {
                sendAck(lastSequenceNumber, socket, address, port);
            } else {
                lastSequenceNumber = sequenceNumber;

                System.err.println("Received: Sequence number:" + lastSequenceNumber);
                System.arraycopy(message, 20, fileByteArray, 0, packet.getPacketLength() - 20);
                outToFile.write(fileByteArray);
                sendAck(lastSequenceNumber, socket, address, port);
                if (packet.FIN == 1) {
                    outToFile.close();
                    break;
                }
            }

        }
    }
}
