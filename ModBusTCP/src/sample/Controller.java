package sample;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

public class Controller {
    @FXML
    TextField ipAddress;
    @FXML
    TextField portNumber;
    @FXML
    TextField request;
    @FXML
    TextArea response;

    ModbusTCP modbus;

    @FXML
    public void connect() {
        System.out.println("Connect Button Pressed!");
        modbus = new ModbusTCP(ipAddress.getText(), Integer.parseInt(portNumber.getText()));
        modbus.connect();
        //modbus2.connect();

    }
    @FXML
    public void sendData() {

        List<Integer> dtd = new ArrayList<Integer>();
        dtd.add(0);
        dtd.add(0);
        dtd.add(0);
        dtd.add(33);
        //System.out.println("Send funciton working!");
        List<Byte> dt= modbus.readInputRegisters(1, 2);
        System.out.println(dt.size());
        //System.out.println(dt.get(7));
        for (int i=0; i < dt.size(); i++) {
            System.out.print( String.format("%02X - ", dt.get(i)));
        }
        //float rv = Float.intBitsToFloat((int) ((dt.get(9)<<24) | (dt.get(10)<<16) | (dt.get(11)<<8) | dt.get(12)));
        float rv = Float.intBitsToFloat((int) Long.parseLong(String.format("%02X%02X%02X%02X", dt.get(9),dt.get(10),dt.get(11),dt.get(12)),16));
        System.out.println("");
        System.out.println(rv);

    }
}
