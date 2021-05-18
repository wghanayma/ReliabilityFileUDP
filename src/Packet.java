import java.nio.ByteBuffer;

/* Written By Dr. Raed Alqadi to get you started with Assignment 3
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author raed
 */
public class Packet {
    static final int MAX_DATA_LENGTH = 1024;// Change this to 1024 or 512
    static final int HEADER_LENGTH = 4 + 4 + 4 + 4 + 4;// Constat, addjust to your case
    /*
     * Similar to structure,I will make them public but that is a Very Bad idea
     * These should be made private and use Accessors, Successors
     * These are not actual Packet fields, but will be similar
     */
    public int checkSum = 0; // 4 bytes = b.getChar(0); // index 0, 2 bytes
    public int sequenceNumber = 0; //4 bytes  b.getInt(0 + 2 + 4);// stored at index 2
    public int ackSequence = 0; // 4 bytes = ; //4byte b.getFloat(0 + 2 + 4);//offset 6
    public int dataLength = 0; //4 bytes; = b.getInt(0 + 2 + 4+4);
    public int FIN = 0;
    public ByteBuffer buffer = null;
    int error = 0;
    byte[] data = new byte[MAX_DATA_LENGTH];

    // You can add constructor to allocate the same as the data length instead of max
    public Packet() {
        init(0, 0, 0);
    }

    public Packet(int cs, int sn, int as) {
        this.init(cs, sn, as);
    }

    static void printBufferHex(ByteBuffer b, int limit) {
        //int limit = b.limit();
        String S = "[";
        for (int i = 0; i < limit; i++) {
            S += String.format("%02X", b.array()[i] & 0x00ff);
            if (i != (limit - 1)) {
                S += ", ";
            }
        }
        S += "]\n";
        S += "Position: " + b.position()
                + "\nLimit: " + b.limit();
        System.out.println(S);
    }

    private void init(int cs, int sn, int as) {
        //  System.out.println("HEADER_LENGTH " + HEADER_LENGTH);
        checkSum = cs; // 2 bytes = b.getChar(0); // index 0, 2 bytes
        sequenceNumber = sn; //4 bytes  b.getInt(0 + 2 + 4);// stored at index 2
        ackSequence = as; // 4 bytes = ; //4byte b.getFloat(0 + 2 + 4);//offset 6
        dataLength = 0; //4 bytes; = b.getInt(0 + 2 + 4+4);
        buffer = null;
    }

    // You can read a chunck of data from a file and add it by using this functio
    public boolean addData(byte[] data, int n) {// n is the number of bytes to add
        // n should be more the data.length
        dataLength = n;
        if (n > MAX_DATA_LENGTH) {
            System.out.println("Data too big");
            return false;
        }
        for (int i = 0; i < n; i++) {
            this.data[i] = data[i];
        }
        return true;
    }

    public ByteBuffer toByteBuffer() {
        buffer = ByteBuffer.allocate(HEADER_LENGTH + MAX_DATA_LENGTH);
        buffer.clear();
        buffer.putInt(checkSum); // 4 bytes;
        buffer.putInt(sequenceNumber);//4 bytes
        buffer.putInt(ackSequence);//4 bytes
        buffer.putInt(dataLength);  // Save this, it is important to save it
        buffer.putInt(FIN);  // Save this, it is important to save it
        buffer.put(data, 0, dataLength);//copy ony the vailable bytes
        return buffer;
    }

    public void printPacketAsArray() {
        if (buffer != null) {
            Packet.printBufferHex(buffer, this.getPacketLength());
        } else {
            System.out.println("Buffer is , Execute to Buffer");
        }
    }

    public void printPacket() {
        String S = "";
        S += ("Packet Contents= { ");
        S += ("\n\tcheckSum: " + this.checkSum);
        S += ("\n\tsequenceNumber: " + this.sequenceNumber);
        S += ("\n\tackSequence: " + this.ackSequence);
        S += ("\n\tdataLength: " + this.dataLength);
        S += ("\n\tFIN: " + this.FIN);
        int limit = dataLength;
        S += "\n\tdata =[\n\t";
        for (int i = 0; i < limit; i++) {
            S += String.format("%02X", data[i] & 0x00ff);
            if (i != (limit - 1)) {
                S += ", ";
            }
        }
        S += "]";
        S += "\n}\n";
        System.out.println(S); // instead of this use next
        // return S; // use this to return a String to print it in GUI
        // change function type to String
    }

    public int getPacketLength() {
        return HEADER_LENGTH + dataLength;
    }

    public void extractPacketfromByteBuffer(ByteBuffer buf) {
        try {
            int cs = buf.getInt(0); // index 0, 2 bytes
            int sn = buf.getInt(0 + 4);// stored at index 2
            int as = buf.getInt(0 + 4 + 4);//offset 6
            int dataLen = buf.getInt(0 + 4 + 4 + 4);
            int fin = buf.getInt(0 + 4 + 4 + 4 + 4);
            //  System.out.println(">> BaLength:= "+dataLen);
            byte[] ba = new byte[dataLen];
            for (int i = 0; i < dataLen; i++) {
                byte bt = buf.get(0 + 4 + 4 + 4 + 4 + 4 + i);
                ba[i] = bt;
            }
            this.checkSum = cs;
            this.data = ba;
            this.dataLength = dataLen;
            this.ackSequence = as;
            this.sequenceNumber = sn;
            this.FIN = fin;
            //    this.printPacket(); // to show it works
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public int generateSum(int errorRate) {
        //this.toByteBuffer();
        int sum = 0;
        int i = 0;
        for (byte num : this.toByteBuffer().array()) {
            //System.out.println(i + " num : " + String.format("%02X", num & 0x00ff));
            sum += num;
            //  System.out.println(i + " sum : " + String.format("%02X", sum & 0x00ff));
            i++;
            if (sum >>> 16 > 0) {
                sum = (sum >>> 16) + (sum & 0xffff);
            }
        }
        sum = sum + errorRate;
        //    System.out.println("Sum : " + sum);
        //   System.out.println("Integer.toBinaryString(sum) : " + Integer.toBinaryString(sum));
        //    System.out.println("using 1s complement : " + Integer.toHexString(sum));
        //using 1s complement
        //   System.out.println("Sum : " + ~sum);
        // System.out.println("using 1s complement : " + Integer.toHexString(~sum).substring(4));
        return sum;
    }

    public void generateChecksum(int errorRate) {
        this.checkSum = ~generateSum(errorRate);
    }

    public void ReceiverGenerateChecksum(int checkSum) {
        System.out.println("<<<<<<<<<<<<<<<< Generate Checksum <<<<<<<<<<<<<<<<<<<<<");
        generateChecksum(0);
        System.out.println("<<<<<<<<<<<<<<<< Receiver Generate Checksum <<<<<<<<<<<<<<<<<<<<<");
        System.out.println("checkSum : " + ~checkSum);
        System.out.println("using 1s complement : " + Integer.toHexString(~checkSum));
        System.out.println("this.checkSum : " + this.checkSum);
        System.out.println("using 1s complement : " + Integer.toHexString(this.checkSum));
        toByteBuffer();
        int syndrome = ~(this.checkSum + ~checkSum);
        System.out.println("syndrome : " + syndrome);
        if (syndrome == 0)
            System.out.println("Data is received without error.");
        else {
            System.out.println("There is an error in the received data.");
        }
    }
}
