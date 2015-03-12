package distributedwhiteboard.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * The main GUI for the Distributed Whiteboard application. Holds an instance of
 *  {@link WhiteboardCanvas} to allow a user to draw and view drawings from 
 * other networked users.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-03-10
 */
public class WhiteboardGUI extends JFrame implements Runnable 
{
    /** Serialisation ID. */
    private static final long serialVersionUID = -4404291511660285311L;
    /** Singleton instance of this {@link WhiteboardGUI}. */
    private static final WhiteboardGUI INSTANCE = new WhiteboardGUI();
    /** The layout manager for the components. */
    private final SpringLayout layout;
    /** A {@link WhiteboardCanvas} to allow the user to draw. */
    private final WhiteboardCanvas canvas;
    /** A set of {@link WhiteboardControls} for the {@link WhiteboardCanvas}. */
    private final WhiteboardControls controls;
    /** A {@link JScrollPane} to hold the canvas in case it's too large. */
    private final JScrollPane scroller;
    /** Repaints the GUI constantly in the background. */
    private final Thread repainter;
    /** Allows the repainter thread to run. */
    private boolean repaint;
    
    /**
     * Creates a new instance of the {@link WhiteboardGUI}, setting up all the 
     * components and the containing frame. This is private as the class is a 
     * singleton to prevent multiple windows being open simultaneously from a 
     * single client.
     * 
     * @since 1.0
     */
    private WhiteboardGUI()
    {
        // Set the look and feel to the system style if possible.
        String sysFeel = UIManager.getSystemLookAndFeelClassName();
        String crossFeel = UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(sysFeel);
        } catch (ClassNotFoundException 
                | InstantiationException 
                | IllegalAccessException 
                | UnsupportedLookAndFeelException ex) {
            System.err.println("Couldn't load system look and feel. "
                    + "Reverting to cross-platform.");
            try {
                UIManager.setLookAndFeel(crossFeel);
            } catch (ClassNotFoundException 
                    | InstantiationException 
                    | IllegalAccessException 
                    | UnsupportedLookAndFeelException innerEx) {
                System.err.println("Couldn't load cross-platform look and "
                        + "feel.");
            }
        }
        
        this.layout = new SpringLayout();
        Container contentPane = this.getContentPane();
        contentPane.setLayout(layout);
        
        this.canvas = new WhiteboardCanvas(800, 800);
        this.controls = new WhiteboardControls(canvas);
        this.controls.setupLayout();
        this.scroller = new JScrollPane(canvas);
        this.scroller.setBackground(Color.LIGHT_GRAY);
        
        // Add the canvas and controls to the main GUI. Canvas above controls.
        contentPane.add(controls);
        this.layout.putConstraint(SpringLayout.WEST, controls, 5, 
                SpringLayout.WEST, contentPane);
        this.layout.putConstraint(SpringLayout.SOUTH, controls, -5, 
                SpringLayout.SOUTH, contentPane);
        this.layout.putConstraint(SpringLayout.EAST, controls, -5, 
                SpringLayout.EAST, contentPane);
        
        contentPane.add(scroller);
        this.layout.putConstraint(SpringLayout.NORTH, scroller, 5, 
                SpringLayout.NORTH, contentPane);
        this.layout.putConstraint(SpringLayout.WEST, scroller, 5, 
                SpringLayout.WEST, contentPane);
        this.layout.putConstraint(SpringLayout.SOUTH, scroller, -5, 
                SpringLayout.NORTH, controls);
        this.layout.putConstraint(SpringLayout.EAST, scroller, -5, 
                SpringLayout.EAST, contentPane);
        
        // Set up the rest of the JFrame.
        this.setContentPane(contentPane);
        this.setTitle("Distributed Whiteboard");
        this.setSize(828, 893);
        this.setLocationByPlatform(true);
        this.setResizable(true);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setVisible(true);
        
        // Repaint the window constantly due to a weird rendering bug on Win.
        this.repainter = new Thread(this);
        this.repainter.setName("Whiteboard Repainter");
        this.repainter.setPriority(Thread.MIN_PRIORITY);
        this.repainter.start();
    }
    
    /**
     * Gets the singleton instance of {@link WhiteboardGUI} for this application
     *  instance.
     * 
     * @return The {@link WhiteboardGUI} singleton instance.
     * @since 1.0
     */
    public static WhiteboardGUI getInstance()
    {
        return WhiteboardGUI.INSTANCE;
    }

    /**
     * Stop the repainting thread gracefully when the window sends the 
     * {@link WindowEvent#WINDOW_CLOSING} event.
     * 
     * @param e The {@link WindowEvent} sent by this {@link WhiteboardGUI}.
     * @since 1.0
     */
    @Override
    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            WhiteboardGUI.this.repaint = false;
            try {
                WhiteboardGUI.this.repainter.join();
                System.out.println("Repainter stopped.");
            } catch (InterruptedException ex) {
                System.err.println("Failed to stop repainter gracefully.");
            }
            dispose();
        }
    }
    
    /**
     * Repaints the entire window every 10 milliseconds to solve a rendering 
     * error on Windows.
     * 
     * @since 1.0
     */
    @Override
    public void run()
    {
        this.repaint = true;
        while (repaint) {
            repaint();
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                
            }
        }
    }
}
