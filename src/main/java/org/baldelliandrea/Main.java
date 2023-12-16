package org.baldelliandrea;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.melloware.jintellitype.JIntellitype;
import org.baldelliandrea.musicplayer.MusicPlayer;
import org.baldelliandrea.song.Song;
import org.baldelliandrea.song.SongCacheManager;
import org.baldelliandrea.ui.MediaControlFrame;
import org.baldelliandrea.ui.MusicPlayerFrame;

import java.io.File;
import java.util.Objects;
import java.util.TreeMap;
import javax.swing.*;

public class Main {

    private static MusicPlayer musicPlayer;
    private static MusicPlayerFrame musicPlayerFrame;
    private static MediaControlFrame mediaControlFrame;

    private static TreeMap<String, Song> songsList;
    private static SongCacheManager songCacheManager;

    private static long startShowWindowTime;

    public static void main(String[] args) {
        registerHotkeys();
        setLookAndFeel();
        songCacheManager = new SongCacheManager();
        loadSongList(System.getenv("USERPROFILE") + "\\Music");
        musicPlayer = new MusicPlayer();
        musicPlayerFrame = new MusicPlayerFrame(songsList, musicPlayer);
        mediaControlFrame = new MediaControlFrame(musicPlayer);
        musicPlayer.setMusicPlayerFrame(musicPlayerFrame);
        musicPlayer.setMediaControlFrame(mediaControlFrame);
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
                    mediaControlFrame.customShow();
                    break;
                case JIntellitype.APPCOMMAND_MEDIA_NEXTTRACK:
                    musicPlayer.nextPositionInSongQueue();
                    mediaControlFrame.customShow();
                    break;
                case JIntellitype.APPCOMMAND_MEDIA_PREVIOUSTRACK:
                    musicPlayer.prevPositionInSongQueue();
                    mediaControlFrame.customShow();
                    break;
                case JIntellitype.APPCOMMAND_VOLUME_UP:
                case JIntellitype.APPCOMMAND_VOLUME_DOWN:
                    mediaControlFrame.customShow();
                    break;
                case JIntellitype.APPCOMMAND_MEDIA_STOP:
                    if (System.currentTimeMillis() - startShowWindowTime <= 1000) {
                        musicPlayerFrame.toggleVisibility();
                    } else {
                        startShowWindowTime = System.currentTimeMillis();
                        mediaControlFrame.customShow();
                    }
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