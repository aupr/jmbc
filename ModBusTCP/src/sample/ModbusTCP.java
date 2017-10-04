package sample;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ModbusTCP {
    private String host;
    private int port;
    private boolean connected;
    private int transactionID;
    private byte unitID; // Slave ID
    private Socket clientSocket;
    private DataOutputStream outToServer;
    private InputStream inFromServer;

    public ModbusTCP(String host, int port) {
        this.host = host;
        this.port = port;
        this.transactionID=0;
        this.unitID = 1;
        connected = false;
    }

    public byte getUnitID() {
        return unitID;
    }

    public void setUnitID(int unitID) {
        if (connected) this.unitID = (byte) unitID;
    }

    public void connect() {
        try {
            clientSocket = new Socket(this.host, this.port);
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = clientSocket.getInputStream();
            System.out.println("Connected with server!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Byte> readDiscreteInputs(int registerAddress, int numberOfRegisters) {

        byte data[] = new byte[12];
        data[4] = 0x00; //Number of remaining byte1
        data[5] = 0x06; //Number of remaining byte0
        data[7] = 0x02; //Function Code
        data[8] = (byte) (registerAddress >> 8);
        data[9] = (byte) registerAddress;
        data[10] = (byte) (numberOfRegisters >> 8);
        data[11] = (byte) numberOfRegisters;

        return transaction(data);
    }

    public List<Byte> readCoils(int registerAddress, int numberOfRegisters) {
        byte data[] = new byte[12];
        data[4] = 0x00; //Number of remaining byte1
        data[5] = 0x06; //Number of remaining byte0
        data[7] = 0x01; //Function Code
        data[8] = (byte) (registerAddress >> 8);
        data[9] = (byte) registerAddress;
        data[10] = (byte) (numberOfRegisters >> 8);
        data[11] = (byte) numberOfRegisters;

        return transaction(data);
    }

    public List<Byte> writeSingleCoil(int registerAddress, Boolean registerValue) {
        byte data[] = new byte[12];
        data[4] = 0x00; //Number of remaining byte1
        data[5] = 0x06; //Number of remaining byte0
        data[7] = 0x05; //Function Code
        data[8] = (byte) (registerAddress >> 8);
        data[9] = (byte) registerAddress;

        if (registerValue){
            data[10] = (byte) 0xFF;
            data[11] = (byte) 0x00;
        } else {
            data[10] = (byte) 0x00;
            data[11] = (byte) 0x00;
        }

        return transaction(data);
    }

    public boolean writeMultipleCoils() {
        return true;
    }

    public List<Byte> readInputRegisters(int registerAddress, int numberOfRegisters) {
        byte data[] = new byte[12];
        data[4] = 0x00; //Number of remaining byte1
        data[5] = 0x06; //Number of remaining byte0
        data[7] = 0x04; //Function Code
        data[8] = (byte) (registerAddress >> 8);
        data[9] = (byte) registerAddress;
        data[10] = (byte) (numberOfRegisters >> 8);
        data[11] = (byte) numberOfRegisters;

        return transaction(data);
    }

    public List<Byte> readMultipleHoldingRegisters(int registerAddress, int numberOfRegisters)
    {
        byte data[] = new byte[12];
        data[4] = 0x00; //Number of remaining byte1
        data[5] = 0x06; //Number of remaining byte0
        data[7] = 0x03; //Function Code
        data[8] = (byte) (registerAddress >> 8);
        data[9] = (byte) registerAddress;
        data[10] = (byte) (numberOfRegisters >> 8);
        data[11] = (byte) numberOfRegisters;

        return transaction(data);
    }


    public List<Byte> writeSingleHoldingRegister(int registerAddress, int registerValue) {
        byte data[] = new byte[12];
        data[4] = 0x00; //Number of remaining byte1
        data[5] = 0x06; //Number of remaining byte0
        data[7] = 0x06; //Function Code
        data[8] = (byte) (registerAddress >> 8);
        data[9] = (byte) registerAddress;
        data[10] = (byte) (registerValue >> 8);
        data[11] = (byte) registerValue;

        return transaction(data);
    }

    public List<Byte> writeMultipleHoldingRegisters(int registerAddress, List<Integer> RegisterValues) {

        //System.out.println(RegisterValues);

        int numberOfRegisters = RegisterValues.size();
        int numberOfBytesOfRegisterValuesToFollow = 2 * numberOfRegisters;
        int numberOfRemainingBytes = 6 + numberOfBytesOfRegisterValuesToFollow;
        int totalDataSizeInBytes = 7 + numberOfRemainingBytes;



        byte data[] = new byte[totalDataSizeInBytes];
        data[4] = (byte) (numberOfRemainingBytes >> 8); //Number of remaining byte1
        data[5] = (byte) numberOfRemainingBytes; //Number of remaining byte0
        data[7] = 0x10; //Function Code
        data[8] = (byte) (registerAddress >> 8);
        data[9] = (byte) registerAddress;
        data[10] = (byte) (numberOfRegisters >> 8);
        data[11] = (byte) numberOfRegisters;
        data[12] = (byte) numberOfBytesOfRegisterValuesToFollow;

        int indexValue = 0;
        //System.out.println(totalDataSizeInBytes);
        for (int i=13; i<totalDataSizeInBytes; i=i+2) {
            data[i] = (byte) (RegisterValues.get(indexValue) >> 8);
            data[i+1] = (byte) (RegisterValues.get(indexValue++) >> 0);
        }

        return transaction(data);
    }




    public List<Byte> transaction(byte[] data) {
        data[0] = (byte) ((transactionID >> 8) & 0xFF);
        data[1] = (byte) (transactionID++ & 0xFF);
        data[6] = unitID;
        try {
            outToServer.write(data, 0, data.length);

            List<Byte> returnValue = new ArrayList<Byte>();
            returnValue.add((byte) inFromServer.read());
            returnValue.add((byte) inFromServer.read());
            returnValue.add((byte) inFromServer.read());
            returnValue.add((byte) inFromServer.read());

            byte  loopLength_1= (byte) inFromServer.read();
            returnValue.add(loopLength_1);
            byte  loopLength_0= (byte) inFromServer.read();
            returnValue.add(loopLength_0);
            int loopLength = (int) ((loopLength_1 << 8) + loopLength_0);

            for (int i=0; i< loopLength; i++ ) {
                returnValue.add((byte) inFromServer.read());
            }

            return returnValue;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void close(){
        try {
            clientSocket.close();
            this.connected = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
