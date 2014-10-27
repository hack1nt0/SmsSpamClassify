package TC;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by root on 14-9-12.
 */
public class Show {
    private JButton helloButton;

    public Show() {
        helloButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                System.out.println("Clicked!");
            }
        });
    }
}
