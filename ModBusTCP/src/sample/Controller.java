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
        List<Byte> dt= modbus.writeMultipleHoldingRegisters(50045, dtd);
        System.out.println(dt.size());
        System.out.println(dt.get(7));
        for (int i=0; i < dt.size(); i++) {
            System.out.print( String.format("%02X - ", dt.get(i)));
        }
        System.out.println("");

    }
}
