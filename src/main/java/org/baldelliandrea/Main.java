package org.baldelliandrea;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.melloware.jintellitype.JIntellitype;
import org.baldelliandrea.musicplayer.MusicPlayer;
import org.baldelliandrea.song.Song;
import org.baldelliandrea.song.SongCacheManager;
import org.baldelliandrea.ui.HideWindowDelayedThread;
import org.baldelliandrea.ui.MediaControlFrame;
import org.baldelliandrea.ui.MusicPlayerFrame;

import java.awt.*;
import java.io.File;
import java.util.Objects;
import java.util.TreeMap;
import javax.swing.*;

public class Main {
    private static final float MEDIA_CONTROL_WIDGET_DELAY = 3f;

    private static MediaControlFrame mediaControlFrame;
    private static HideWindowDelayedThread hideWindowDelayedThread;

    private static MusicPlayer musicPlayer;

    private static TreeMap<String, Song> songsList;
    private static SongCacheManager songCacheManager;

    public static void main(String[] args) {
        registerHotkeys();
        setLookAndFeel();
        songCacheManager = new SongCacheManager();
        loadSongList("C:\\Users\\Andrea\\Music\\Andrea");
        musicPlayer = new MusicPlayer();
        MusicPlayerFrame musicPlayerFrame = new MusicPlayerFrame(songsList, musicPlayer);
        mediaControlFrame = new MediaControlFrame(musicPlayer);
        musicPlayer.setMusicPlayerFrame(musicPlayerFrame);
        musicPlayer.setMediaControlFrame(mediaControlFrame);
    }

    private static void showMediaControlWindow() {
        EventQueue.invokeLater(() -> {
            if (hideWindowDelayedThread != null)
                hideWindowDelayedThread.interrupt();

            mediaControlFrame.setVisible(true);
            hideWindowDelayedThread = new HideWindowDelayedThread(mediaControlFrame, MEDIA_CONTROL_WIDGET_DELAY);
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
                    musicPlayer.togglePlayPause();
                    showMediaControlWindow();
                    break;
                case JIntellitype.APPCOMMAND_MEDIA_NEXTTRACK:
                    musicPlayer.nextPositionInSongQueue();
                    showMediaControlWindow();
                    break;
                case JIntellitype.APPCOMMAND_MEDIA_PREVIOUSTRACK:
                    musicPlayer.prevPositionInSongQueue();
                    showMediaControlWindow();
                    break;
                case JIntellitype.APPCOMMAND_VOLUME_UP:
                case JIntellitype.APPCOMMAND_VOLUME_DOWN:
                    showMediaControlWindow();
                    break;
                default:
                    break;
            }
        });
    }

    public static void loadSongList(String startFolderPath) {
        songsList = new TreeMap<>();
        loadSongListRecursive(new File(startFolderPath));
    }

    private static void loadSongListRecursive(File startFolder) {
        if (startFolder.exists() && startFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(startFolder.listFiles())) {
                if (file.isDirectory())
                    loadSongListRecursive(file);
                else if (file.isFile() && file.getName().endsWith(".mp3"))
                    songsList.put(file.getName(), songCacheManager.getSong(file.getAbsolutePath(), file.getName()));
            }
        }
    }
}