package distributedwhiteboard;

import distributedwhiteboard.gui.WhiteboardGUI;
import java.awt.Color;
import java.awt.Point;
import javax.swing.SwingUtilities;

/**
 * Starts the UDP listener {@link Server} and the {@link WhiteboardGUI}.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-03-10
 */
public class Distributedwhiteboard 
{

    /**
     * Main entry point.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        int port = 55551;
        
        for (int i = 0; i < args.length; i++) {
            switch(args[i]) {
                case "-p":
                case "--port":
                    // Allow the port to be specified.
                    try {
                        port = Integer.parseInt(args[i+1]);
                        i++;
                    } catch (NumberFormatException nEx) {
                        System.err.println("Port value must be a number.");
                    }
                    break;
                default:
                    System.err.printf("Unknown argument '%s'%n", args[i]);
            }
        }
        
        // Print the available drawing modes.
        System.out.println("Drawing Modes:");
        for (DrawMode mode : DrawMode.values())
            System.out.println(mode);
        System.out.println();
        
        WhiteboardGUI gui = WhiteboardGUI.getInstance();
        if (gui == null) return;
        
        try {
            Server server = Server.getInstance(port);
            server.startServer();
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
