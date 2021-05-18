import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.InvalidPropertiesFormatException;
import java.util.Random;

public class ClientChat {
    private JPanel jPanelMain;
    private JTextArea textAreaMessage;
    private JTextField textFieldLocalIP;
    private JTextField textFieldLocalPort;
    private JTextField textFieldRemoteIP;
    private JTextField textFieldRemotePort;
    private JButton loginButton;
    private JButton buttonOpenSend;
    private JLabel fieldError;
    private JTextField textFieldError;
    private JLabel fieldRetransmissions;
    static boolean loginBool = false;
    static int packetRetransmit = 0;

    public ClientChat() {
        textFieldLocalIP.setText("127.0.0.1");
        textFieldRemoteIP.setText("127.0.0.1");
        textFieldLocalPort.setText("8888");
        textFieldRemotePort.setText("2222");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (!textFieldRemoteIP.getText().isEmpty()) {
                        if (!textFieldRemotePort.getText().isEmpty()) {
                            if(!loginBool){
                            try {
                                String localIP = getUserDetail().getIpAddress();
                                int localPort = (int) getUserDetail().getPort();
                                CFileMessageEvent messageEventFile = new CFileMessageEvent();
                                ReceiverReliabilityUDP receiverReliabilityUDP = new ReceiverReliabilityUDP(messageEventFile, localPort, String.valueOf(localIP));
                                receiverReliabilityUDP.start();
                                loginBool = true;
                            } catch (InvalidPropertiesFormatException invalidPropertiesFormatException) {
                                JOptionPane.showMessageDialog(null
                                        , "please enter a valid number  ", "IP"
                                        , JOptionPane.ERROR_MESSAGE);
                            } catch (NumberFormatException numberFormatException) {
                                JOptionPane.showMessageDialog(null
                                        , "please enter a valid port number from (1024 - 65536)", "Port"
                                        , JOptionPane.ERROR_MESSAGE);
                            }}
                            else {
                                JOptionPane.showMessageDialog(jPanelMain, "already signed in ", "Singed in ", JOptionPane.ERROR_MESSAGE);

                            }
                        } else
                            JOptionPane.showMessageDialog(jPanelMain, "Please put Local Port", "Local Port", JOptionPane.ERROR_MESSAGE);
                    } else
                        JOptionPane.showMessageDialog(jPanelMain, "Please put Local IP", "Local IP", JOptionPane.ERROR_MESSAGE);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        buttonOpenSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!textFieldRemoteIP.getText().isEmpty()) {
                    if (!textFieldRemotePort.getText().isEmpty()) {
                        String fileName = "";
                        if (loginBool) {
                            try {
                                DatagramSocket socket = new DatagramSocket();
                                InetAddress address = InetAddress.getByName(textFieldLocalIP.getText());
                                JFileChooser jfc = new JFileChooser();
                                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                                if (jfc.isMultiSelectionEnabled()) {
                                    jfc.setMultiSelectionEnabled(false);
                                }
                                int r = jfc.showOpenDialog(null);
                                if (r == JFileChooser.APPROVE_OPTION) {
                                    File f = jfc.getSelectedFile();
                                    fileName = f.getName();
                                    byte[] fileNameBytes = fileName.getBytes();

                                        String remoteIP = getUserDetail().getIpAddressRemote();
                                        int remotePort = (int) getUserDetail().getPortRemote();
                                        DatagramPacket fileStatPacket = new DatagramPacket(fileNameBytes, fileNameBytes.length, InetAddress.getByName(remoteIP), remotePort); // File name packet
                                        socket.send(fileStatPacket);
                                        byte[] fileByteArray = Files.readAllBytes(Paths.get(String.valueOf(f)));
                                        sendFile(socket, fileByteArray, address, Integer.parseInt(textFieldRemotePort.getText()));
                                        packetRetransmit = 0;

                                }
                                socket.close();
                                textAreaMessage.append(textFieldRemoteIP.getText() + " : " + textFieldRemotePort.getText() + " " + fileName + "\n");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else
                            JOptionPane.showMessageDialog(jPanelMain, "Please login ", "Login", JOptionPane.ERROR_MESSAGE);
                    } else
                        JOptionPane.showMessageDialog(jPanelMain, "Please put Remote Port", "Remote Port", JOptionPane.ERROR_MESSAGE);
                } else
                    JOptionPane.showMessageDialog(jPanelMain, "Please put Remote IP", "Remote IP", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public static void main(String[] args) {
        JFrame jFrame = new JFrame("Transfer file");
        jFrame.setMinimumSize(new Dimension(700, 300));
        jFrame.setMaximumSize(new Dimension(700, 300));
        jFrame.setPreferredSize(new Dimension(700, 300));
        jFrame.setContentPane(new ClientChat().jPanelMain);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.pack();
        jFrame.setVisible(true);
        jFrame.setResizable(false);
        jFrame.setLocationRelativeTo(null);
    }

    class CFileMessageEvent implements FileMessageEvent {
        @Override
        public void onMessageReceivedFile(String message) {
            textAreaMessage.append(textFieldRemoteIP.getText() + " : " + textFieldRemotePort.getText() + " " + message + "\n");
        }

        @Override
        public void onError(int error) {
            fieldError.setText(String.valueOf(error));
        }
    }

    private void sendFile(DatagramSocket socket, byte[] fileByteArray, InetAddress address, int port) throws IOException {
        int sequenceNumber = 0;
        boolean flag;
        int ackSequence = 0;
        for (int i = 0; i < fileByteArray.length; i = i + 1004) {
            Packet packetSend = new Packet();
            if (sequenceNumber == 0)
                sequenceNumber = 1;
            else
                sequenceNumber = 0;
            packetSend.sequenceNumber = sequenceNumber;
            if ((i + 1004) >= fileByteArray.length) {
                flag = true;
                packetSend.FIN = 1;
            } else {
                flag = false;
                packetSend.FIN = 0;
            }
            byte[] message = new byte[1024];
            if (!flag) {
                System.arraycopy(fileByteArray, i, message, 0, 1004);
                packetSend.addData(message, 1004);
            } else {
                System.arraycopy(fileByteArray, i, message, 0, fileByteArray.length - i);
                packetSend.addData(message, fileByteArray.length - i);
            }
            packetSend.toByteBuffer();
            int valueError;
            if (textFieldError.getText().isEmpty() || textFieldError.getText().equals("0")) {
                valueError = 0;
            } else {
                valueError = RandomNumber(Integer.parseInt(textFieldError.getText()), 0, 1000);
            }
            packetSend.generateChecksum(valueError);
            ByteBuffer byteBufPacketSend = packetSend.toByteBuffer();//byteBuf is just a reference also packet.buffer
            byte[] bytePacketSend = byteBufPacketSend.array();
            DatagramPacket datagramPacketSend = new DatagramPacket(bytePacketSend, packetSend.getPacketLength(), address, port);
            socket.send(datagramPacketSend);
            while (true) {
                Packet packetAcknowledgement = new Packet();
                ByteBuffer byteBufferPacketAck = packetAcknowledgement.toByteBuffer();
                byte[] bytePacketAck = byteBufferPacketAck.array();
                DatagramPacket ackpack = new DatagramPacket(bytePacketAck, packetAcknowledgement.getPacketLength());
                try {
                    socket.setSoTimeout(100);
                    socket.receive(ackpack);
                    ByteBuffer rcvBufPacketAck = ByteBuffer.wrap(bytePacketAck);
                    packetAcknowledgement.extractPacketfromByteBuffer(rcvBufPacketAck);
                    ackSequence = packetAcknowledgement.sequenceNumber;
                    if ((ackSequence == sequenceNumber)) {
                        System.err.println("Ack received: Sequence Number : " + ackSequence);
                        break;
                    } else {
                        System.err.println("Resending: Sequence Number : " + sequenceNumber);
                        Packet packet = new Packet(0, packetSend.sequenceNumber, packetSend.ackSequence);
                        packet.FIN = packetSend.FIN;
                        packet.addData(packetSend.data, packetSend.getPacketLength() - 20);
                        if (textFieldError.getText().isEmpty() || textFieldError.getText().contentEquals("0")) {
                            packet.generateChecksum(0);
                        } else {
                            valueError = RandomNumber(Integer.parseInt(textFieldError.getText()), 0, 1000);
                            packet.generateChecksum(valueError);
                            fieldRetransmissions.setText(String.valueOf(++packetRetransmit));
                        }
                        ByteBuffer byteBufferPacketResending = packet.toByteBuffer();
                        byte[] bytePacketResending = byteBufferPacketResending.array();
                        DatagramPacket datagramPacketResending = new DatagramPacket(bytePacketResending, packet.getPacketLength(), address, port); // The data to be sent
                        socket.send(datagramPacketResending);
                    }
                } catch (SocketTimeoutException e) {
                    System.err.println("Socket timed out waiting for ack");
                }
            }
        }
    }

    int RandomNumber(int rate, int low, int high) {
        //while(true) {
        int value = (int) (high * (rate / 100.0));
        // System.out.println(value);
        Random r = new Random();
        if (value != 0)
            return r.nextInt(value - low) + low;
        else
            return r.nextInt(high - low) + low;
        // }
    }

    public ClientInfo getUserDetail() throws NumberFormatException, InvalidPropertiesFormatException {
        return new ClientInfo(this.textFieldLocalIP.getText(), Integer.parseInt(this.textFieldLocalPort.getText()), this.textFieldRemoteIP.getText(), Integer.parseInt(this.textFieldRemotePort.getText()));
    }
}
