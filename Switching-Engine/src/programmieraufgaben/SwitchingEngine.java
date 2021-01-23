package programmieraufgaben;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Eine vereinfachte Switch Engine mit folgenden möglichen Kommandos:
 * frame <Eingangsportnummer> <Absenderadresse> <Zieladresse>,
 * table,
 * statistics,
 * del Xs bzw. del Xmin,
 * exit
 */
public class SwitchingEngine {
    private int port;
    private List<TableItem> table;
    private List<TableItem> deleteItem;
    private int[] portFrameStatistic;

    public SwitchingEngine() {
        port = 0;
        table = new LinkedList<>();
        deleteItem = new LinkedList<>();
        portFrameStatistic = null;
    }

    /**
     * Diese Methode überprüft die Eingabe und erstellt die für den
     * weiteren Funktionsablauf nötige Datenstruktur
     *
     * @param portNumber Anzahl der Ports, die der Switch verwalten soll
     * @return Gibt bei erfolgreicher erstellung TRUE sonst FALSE zurück
     */
    public boolean createSwitch(int portNumber) {
        if (portNumber < 1) {
            System.out.println("Ungültige Eingabe! Die Portnummer soll größer als 1 sein!");
            return false;
        }
        else {
            port = portNumber;
            portFrameStatistic = new int[portNumber];
            System.out.println("\nEin " + port + "-Port-Switch wurde erzeugt.\n");
            return true;
        }
    }

    /**
     * Diese Methode überprüft und interpretiert die Eingaben und führt
     * die geforderten Funktionen aus.
     *
     * @param command Anweisung die der Switch verarbeiten soll
     * @return Gibt an ob der Switch beendet werden soll: TRUE beenden, FALSE weitermachen
     */
    public boolean handleCommand(String command) {
        String[] cmd = command.split("\\s+");
        if (4 == cmd.length & cmd[0].equals("frame")) {
            frame(Integer.valueOf(cmd[1]), Integer.valueOf(cmd[2]), Integer.valueOf(cmd[3]));
        }
        else if (1 == cmd.length & cmd[0].equals("table")) {
            table();
        }
        else if (1 == cmd.length & cmd[0].equals("statistics")) {
            statistics();
        }
        else if (2 == cmd.length & cmd[0].equals("del")) {
            del(cmd[1]);
        }
        else if (1 == cmd.length & cmd[0].equals("exit")) {
            return true;
        }
        else {
            System.out.println("Ungültige Eingabe!");
        }
        System.out.println();
        return false;
    }

    /**
     * Hier wird die angeforderte Zieladresse finden.
     *
     * @param Address ist die angeforderte Adresse
     * @return ist potenzielle gefundene portnummer. Falls keine angeforderte Adresse gefunden, liefert -1 zurück.
     */
    private int findPort(int Address) {
        for (int i = 0; i < table.size(); ++i) {
            if (table.get(i).getAddress() == Address) {
                return table.get(i).getPort();
            }
        }
        return -1;
    }

    /**
     * Hier wird die Switch-Tabelle aktualisieren, falls das Gerät schon in der Tabelle existiert,
     * wenn Portnummer verändert sich, aktualisiert die Port Information,
     * wenn Portnummer und Adresse identisch sein, wird Switch-Tabelle sich nicht verändern
     * sonst direkt einen neuen Datensatz hinzufügen.
     *
     * @param inputPort
     * @param sourceAddress
     */
    private void updateTable(int inputPort, int sourceAddress) {
        boolean isExis = false;
        for (int i = 0; i < table.size(); ++i) {
            if (sourceAddress == table.get(i).getAddress()) {
                if (inputPort == table.get(i).getPort()) {
                    isExis = true;
                }
                else {
                    table.remove(i);
                }
                break;
            }
        }
        if (!isExis) {
            table.add(new TableItem(inputPort, sourceAddress));
        }

    }

    private void frame(int inputPort, int sourceAddress, int destinationAddress) {
        if (inputPort < 1 | inputPort > port | sourceAddress < 1 | sourceAddress > 254 | destinationAddress < 1 | destinationAddress > 255) {
            System.out.println("Ungültige Eingabe!");
            return;
        }
        portFrameStatistic[inputPort - 1]++;
        if (255 == destinationAddress) {
            updateTable(inputPort, sourceAddress);
            for (int i = 0; i < portFrameStatistic.length; ++i) {
                portFrameStatistic[i]++;
            }
            portFrameStatistic[inputPort - 1]--;
            System.out.println("Broadcast: Ausgabe auf allen Ports außer Port " + inputPort + ".");
        }
        else {
            updateTable(inputPort, sourceAddress);
            int destinationPort = findPort(destinationAddress);
            if (-1 == destinationPort) {
                for (int i = 0; i < portFrameStatistic.length; ++i) {
                    portFrameStatistic[i]++;
                }
                portFrameStatistic[inputPort - 1]--;
                System.out.println("Ausgabe auf allen Ports außer Port " + inputPort + ".");
            }
            else if (destinationPort == inputPort) {
                System.out.println("Frame wird gefiltert und verworfen.");
            }
            else {
                portFrameStatistic[destinationPort - 1]++;
                System.out.println("Ausgabe auf Port " + destinationPort + ".");
            }
        }

    }

    private void table() {
        System.out.printf("%7s%5s%5s", "Adresse", "Port", "Zeit");
        System.out.println();
        for (int i = 0; i < table.size(); ++i) {
            Date date = new Date(table.get(i).getCreationTime());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
            System.out.printf("%7s%5s%9s", table.get(i).getAddress(), table.get(i).getPort(), simpleDateFormat.format(date));
            System.out.println();
        }
    }

    private void statistics() {
        System.out.println("Port Frames");
        for (int i = 0; i < portFrameStatistic.length; ++i) {
            System.out.printf("%4s%7s", i + 1, portFrameStatistic[i]);
            System.out.println();
        }
    }

    private void del(String statment) {
        long timeLimit = System.currentTimeMillis();
        if (statment.matches("\\d+min")) {
            statment = statment.replace("min", "");
            timeLimit -= Long.valueOf(statment) * 60 * 1000;
        }
        else if (statment.matches("\\d+s")) {
            statment = statment.replace("s", "");
            timeLimit -= Long.valueOf(statment) * 1000;
        }
        else {
            System.out.println("Ungültige Eingabe!");
            return;
        }
        deleteItem.clear();
        for (int i = 0; i < table.size(); ++i) {
            if (table.get(i).getCreationTime() < timeLimit) {
                deleteItem.add(table.get(i));
                table.remove(i);
                --i;
            }
        }
        if (0 == deleteItem.size()) {
            System.out.println("Keine Adressen wurden aus der Switch-Tabelle gelöscht");
        }
        else {
            System.out.print("Folgende Adressen wurden aus der Switch-Tabelle gelöscht: ");
            for (int i = 0; i < deleteItem.size(); ++i) {
                System.out.print(deleteItem.get(i).getAddress());
                if (i != deleteItem.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
        deleteItem.clear();
    }
}

/**
 * Diese Klasse ist Tabelleelement creationTime ist die erzeugte Zeit
 */
class TableItem {
    private int address;
    private int port;
    private long creationTime;

    public TableItem(int port, int address) {
        this.address = address;
        this.port = port;
        creationTime = System.currentTimeMillis();
    }

    public int getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public long getCreationTime() {
        return creationTime;
    }

}