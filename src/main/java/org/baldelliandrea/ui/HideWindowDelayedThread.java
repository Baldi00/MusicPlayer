package org.baldelliandrea.ui;

import javax.swing.*;

public class HideWindowDelayedThread extends Thread {
    private JFrame window;
    private float delaySeconds;

    public HideWindowDelayedThread(JFrame window, float delaySeconds) {
        this.window = window;
        this.delaySeconds = delaySeconds;
    }

    public void run() {
        try {
            Thread.sleep((long) delaySeconds * 1000);
            window.setVisible(false);
        } catch (InterruptedException e) {

        }
    }
}
