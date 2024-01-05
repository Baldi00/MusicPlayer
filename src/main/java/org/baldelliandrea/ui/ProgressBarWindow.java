package org.baldelliandrea.ui;

import javax.swing.*;
import java.awt.*;

public class ProgressBarWindow extends JFrame {

    private JLabel label;
    private JProgressBar progressBar;

    public ProgressBarWindow() {
        setLayout(new GridLayout(2, 1));

        label = new JLabel();
        label.setHorizontalAlignment(JLabel.CENTER);

        progressBar = new JProgressBar();

        add(label);
        add(progressBar);

        setSize(400, 150);
        setUndecorated(true);
        setType(Type.UTILITY);
        setLocationRelativeTo(null);

    }

    public void setProgressBarMax(int max) {
        progressBar.setMinimum(0);
        progressBar.setMaximum(max);
        progressBar.setValue(0);
    }

    public void incrementProgressBar() {
        progressBar.setValue(progressBar.getValue() + 1);
    }

    public void setLabel(String label) {
        this.label.setText(label);
    }
}
