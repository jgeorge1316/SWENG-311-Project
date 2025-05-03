// BaseFrame.java
import javax.swing.*;
import java.awt.event.ActionListener;

public abstract class BaseFrame extends JFrame implements ActionListener {
    public BaseFrame(String title, int width, int height) {
        super(title);
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
}
