package org.baldelliandrea.musicplayer;

import javazoom.jlgui.basicplayer.*;
import org.baldelliandrea.song.Song;
import org.baldelliandrea.ui.MusicPlayerFrame;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MusicPlayer {
    private final BasicPlayer musicPlayer;
    private boolean isPlaying;

    private long songLengthMicroseconds;
    private int songLengthBytes;
    private long currentSongMicroseconds;

    private MusicPlayerFrame musicPlayerFrame;

    private List<Song> songsQueue;
    private int songsQueuePosition;

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

    public void setSongsQueue(List<Song> songsQueue) {
        this.songsQueue = new ArrayList<>(songsQueue);
    }

    public void setPositionInSongQueue(int position) {
        songsQueuePosition = position;
        if (position < 0)
            songsQueuePosition = Math.max(0, songsQueue.size() - 1 + position);
        if (position >= songsQueue.size())
            songsQueuePosition = Math.min(songsQueue.size() - 1, position - songsQueue.size());
        musicPlayerFrame.updateCurrentSong(songsQueue.get(songsQueuePosition));
        setMusicFilePath(songsQueue.get(songsQueuePosition).getPath());
        play();
    }

    public void nextPositionInSongQueue() {
        setPositionInSongQueue(songsQueuePosition + 1);
    }

    public void prevPositionInSongQueue() {
        setPositionInSongQueue(songsQueuePosition - 1);
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
                if (basicPlayerEvent.getCode() == BasicPlayerEvent.EOM)
                    nextPositionInSongQueue();
            }

            @Override
            public void setController(BasicController basicController) {
            }
        });
    }
}
