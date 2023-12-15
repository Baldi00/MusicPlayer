package org.baldelliandrea.musicplayer;

import javazoom.jlgui.basicplayer.*;
import org.baldelliandrea.ui.MusicPlayerFrame;

import java.io.File;
import java.util.Map;

public class MusicPlayer {
    private final BasicPlayer musicPlayer;
    private boolean isPlaying;

    private long songLengthMicroseconds;
    private int songLengthBytes;
    private long currentSongMicroseconds;

    private MusicPlayerFrame musicPlayerFrame;

    public MusicPlayer() {
        musicPlayer = new BasicPlayer();
        addListener();
    }

    public void setMusicFilePath(String path) {
        try {
            musicPlayer.open(new File(path));
        } catch (BasicPlayerException e) {
            throw new RuntimeException(e);
        }
    }

    public void play() {
        try {
            musicPlayer.stop();
            musicPlayer.play();
            isPlaying = true;
        } catch (BasicPlayerException e) {
            throw new RuntimeException(e);
        }
    }

    public void togglePlayPause() {
        try {
            if (isPlaying)
                musicPlayer.pause();
            else
                musicPlayer.resume();
            isPlaying = !isPlaying;
        } catch (BasicPlayerException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPosition(float percentage) {
        try {
            musicPlayer.stop();
            musicPlayer.seek((long) (songLengthBytes * percentage));
            musicPlayer.play();
        } catch (BasicPlayerException e) {
            throw new RuntimeException(e);
        }
    }

    public void setMusicPlayerFrame(MusicPlayerFrame musicPlayerFrame) {
        this.musicPlayerFrame = musicPlayerFrame;
    }

    private void addListener() {
        musicPlayer.addBasicPlayerListener(new BasicPlayerListener() {
            @Override
            public void opened(Object o, Map map) {
                songLengthMicroseconds = (long) map.get("duration");
                songLengthBytes = (int) map.get("mp3.length.bytes");
            }

            @Override
            public void progress(int i, long l, byte[] bytes, Map map) {
                currentSongMicroseconds = (long) map.get("mp3.position.microseconds");
                musicPlayerFrame.updateSlider((int) currentSongMicroseconds, (int) songLengthMicroseconds);
            }

            @Override
            public void stateUpdated(BasicPlayerEvent basicPlayerEvent) {
            }

            @Override
            public void setController(BasicController basicController) {
            }
        });
    }
}
