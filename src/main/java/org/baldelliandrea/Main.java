package org.baldelliandrea;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.melloware.jintellitype.JIntellitype;

import java.awt.*;
import javax.swing.*;
public class Main {
    private static JFrame mediaControlWindow;
    private static HideWindowDelayedThread hideWindowDelayedThread;

    private static MusicPlayer musicPlayer;

    public static void main(String[] args) {
        registerHotkeys();
        setLookAndFeel();
        createMediaControlWindow();
        musicPlayer = new MusicPlayer();
        musicPlayer.setMusicFilePath("Path to music");
        musicPlayer.play();
    }

    private static void createMediaControlWindow() {
        EventQueue.invokeLater(() -> {
            mediaControlWindow = new JFrame();
            mediaControlWindow.setSize(600, 200);
            mediaControlWindow.setUndecorated(true);
            mediaControlWindow.setLocation(50, 50);
            mediaControlWindow.setType(JFrame.Type.UTILITY);
//            com.sun.awt.AWTUtilities.setWindowOpacity(mediaControlWindow, 0.9f);
        });
    }

    private static void showMediaControlWindow() {
        EventQueue.invokeLater(() -> {
            if (hideWindowDelayedThread != null)
                hideWindowDelayedThread.interrupt();

            mediaControlWindow.setVisible(true);
            hideWindowDelayedThread = new HideWindowDelayedThread(mediaControlWindow, 2);
            hideWindowDelayedThread.start();
        });
    }

    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerHotkeys() {
        JIntellitype.getInstance().addIntellitypeListener(command -> {
            switch (command) {
                case JIntellitype.APPCOMMAND_MEDIA_PLAY_PAUSE:
                    System.out.println("Play/Pause message received");
                    musicPlayer.togglePlayPause();
                    showMediaControlWindow();
                    break;
                case JIntellitype.APPCOMMAND_MEDIA_NEXTTRACK:
                    System.out.println("Next message received");
                    showMediaControlWindow();
                    break;
                case JIntellitype.APPCOMMAND_MEDIA_PREVIOUSTRACK:
                    System.out.println("Previous message received");
                    showMediaControlWindow();
                    break;
                case JIntellitype.APPCOMMAND_VOLUME_UP:
                    System.out.println("Volume up message received");
                    showMediaControlWindow();
                    break;
                case JIntellitype.APPCOMMAND_VOLUME_DOWN:
                    System.out.println("Volume down message received");
                    showMediaControlWindow();
                    break;
                default:
                    break;
            }
        });
    }
}