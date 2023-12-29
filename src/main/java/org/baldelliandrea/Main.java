package org.baldelliandrea;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.melloware.jintellitype.JIntellitype;
import org.baldelliandrea.musicplayer.MusicPlayer;
import org.baldelliandrea.song.Song;
import org.baldelliandrea.song.SongCacheManager;
import org.baldelliandrea.ui.MediaControlFrame;
import org.baldelliandrea.ui.MusicPlayerFrame;
import org.baldelliandrea.utils.MusicPlayerUtils;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

public class Main {

    private static final int DOUBLE_CLICK_MAX_DELAY = 250;

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
        songsList = new TreeMap<>();

        List<String> songsPaths = getSongsPaths();
        for (String songsPath : songsPaths)
            loadSongList(songsPath);

        musicPlayer = new MusicPlayer();
        musicPlayerFrame = new MusicPlayerFrame(songsList, musicPlayer);
        mediaControlFrame = new MediaControlFrame(musicPlayer);
        musicPlayer.setMusicPlayerFrame(musicPlayerFrame);
        musicPlayer.setMediaControlFrame(mediaControlFrame);
    }

    private static List<String> getSongsPaths() {
        List<String> songsPaths = new ArrayList<>();
        File songsPathsFile = new File(MusicPlayerUtils.SONGS_PATHS_FILE);

        // First songs paths file creation
        if (!songsPathsFile.exists()) {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(songsPathsFile));
                bw.write(System.getenv("USERPROFILE") + "\\Music");
                bw.newLine();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Read file
        try {
            BufferedReader br = new BufferedReader(new FileReader(songsPathsFile));
            String line = br.readLine();
            while (line != null) {
                songsPaths.add(line);
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return songsPaths;
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
                    if (System.currentTimeMillis() - startShowWindowTime <= DOUBLE_CLICK_MAX_DELAY) {
                        musicPlayerFrame.toggleVisibility();
                        mediaControlFrame.customHide();
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
        loadSongListRecursive(new File(startFolderPath));
    }

    private static void loadSongListRecursive(File startFolder) {
        if (startFolder.exists() && startFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(startFolder.listFiles())) {
                if (file.isDirectory())
                    loadSongListRecursive(file);
                else if (file.isFile() && file.getName().toLowerCase().endsWith(".mp3"))
                    songsList.put(file.getName(), songCacheManager.getSong(file.getAbsolutePath(), file.getName()));
            }
        }
    }
}