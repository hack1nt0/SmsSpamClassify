package com.xiaomi.smsspam.Utils;

import java.awt.BorderLayout;
import java.util.Hashtable;

import javax.swing.*;

public class T {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Hashtable labelTable = new Hashtable();
        //
        for (int i = 0; i <= 100; i+=10) {
            labelTable.put(new Integer(i), new JLabel(String.valueOf(i/100.0)));
        }

        JSlider slider = new JSlider();
        slider.setLabelTable(labelTable);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(5);
        //

        JLabel jLabel = new JLabel("-321.3432");
        System.out.println(jLabel.getText());

        JFrame f = new JFrame();
        f.getContentPane().add(slider, BorderLayout.CENTER);
        f.setSize(500, 200);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
