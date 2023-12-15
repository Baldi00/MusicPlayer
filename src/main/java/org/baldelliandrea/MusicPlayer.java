package org.baldelliandrea;

import javazoom.jlgui.basicplayer.*;

import java.io.File;
import java.util.Map;

public class MusicPlayer {
    private final BasicPlayer musicPlayer;
    private boolean isPlaying;

    private long songLengthMicroseconds;
    private int songLengthBytes;
    private long currentSongMicroseconds;
    private int totalMinutes;
    private int totalAdditionalSeconds;

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
        } catch (BasicPlayerException e) {
            throw new RuntimeException(e);
        }
    }

    private void addListener() {
        musicPlayer.addBasicPlayerListener(new BasicPlayerListener() {
            @Override
            public void opened(Object o, Map map) {
                songLengthMicroseconds = (long) map.get("duration");
                totalMinutes = (int) (songLengthMicroseconds / 1000000 / 60);
                totalAdditionalSeconds = (int) (songLengthMicroseconds / 1000000) % 60;
                songLengthBytes = (int) map.get("mp3.length.bytes");
            }

            @Override
            public void progress(int i, long l, byte[] bytes, Map map) {
                currentSongMicroseconds = (long) map.get("mp3.position.microseconds");
                long currentMinutes = currentSongMicroseconds / 1000000 / 60;
                long currentSeconds = (currentSongMicroseconds / 1000000) % 60;
                System.out.println(currentMinutes + ":" + currentSeconds + "/" + totalMinutes + ":" + totalAdditionalSeconds);
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
