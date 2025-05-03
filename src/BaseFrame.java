// BaseFrame.java
import javax.swing.*;

public abstract class BaseFrame extends JFrame {
    public BaseFrame(String title, int width, int height) {
        super(title);
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
}
