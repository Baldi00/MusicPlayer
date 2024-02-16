package org.baldelliandrea.musicplayer;

import javazoom.jlgui.basicplayer.*;
import org.baldelliandrea.song.Song;
import org.baldelliandrea.ui.MediaControlFrame;
import org.baldelliandrea.ui.MusicPlayerFrame;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sound.sampled.*;
import java.io.File;

import javax.sound.sampled.AudioInputStream;

public class MusicPlayer {
    private BasicPlayer musicPlayer;
    private boolean isPlaying;
    private boolean isSongSelected;

    private long songLengthMicroseconds;
    private int songLengthBytes;
    private long currentSongMicroseconds;

    private MusicPlayerFrame musicPlayerFrame;
    private MediaControlFrame mediaControlFrame;

    private List<Song> songsQueue;
    private int songsQueuePosition;

    private RepeatMode repeatMode = RepeatMode.REPEAT_OFF;
    private boolean shuffle;

    public MusicPlayer() {
        try {
            File mp3File = new File("C:\\Users\\Andrea\\Music\\Andrea\\Pop\\2U - David Guetta.mp3");
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(mp3File);
            AudioFormat baseFormat = audioInputStream.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
            );
            AudioInputStream decodedAudioInputStream = AudioSystem.getAudioInputStream(decodedFormat, audioInputStream);

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(decodedFormat);
            line.start();

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = decodedAudioInputStream.read(buffer)) != -1) {
                line.write(buffer, 0, bytesRead);
            }

            line.drain();
            line.close();
            decodedAudioInputStream.close();
            audioInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        musicPlayer = new BasicPlayer();
//        addListener();
    }

    public void setMusicFilePath(String path) {
        try {
            musicPlayer.open(new File(path));
            isSongSelected = true;
        } catch (BasicPlayerException e) {
            throw new RuntimeException(e);
        }
    }

    public void togglePlayPause() {
        if (!isSongSelected)
            return;

        try {
            if (isPlaying)
                musicPlayer.pause();
            else
                musicPlayer.resume();
            isPlaying = !isPlaying;
            musicPlayerFrame.updatePlayPauseButton(isPlaying);
            mediaControlFrame.updatePlayPauseButton(isPlaying);
        } catch (BasicPlayerException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPosition(float percentage) {
        if (!isSongSelected)
            return;

        try {
            musicPlayer.stop();
            musicPlayer.seek((long) (songLengthBytes * percentage));
            musicPlayer.play();
            if (!isPlaying)
                musicPlayer.pause();
        } catch (BasicPlayerException e) {
            throw new RuntimeException(e);
        }
    }

    public void setMusicPlayerFrame(MusicPlayerFrame musicPlayerFrame) {
        this.musicPlayerFrame = musicPlayerFrame;
    }

    public void setMediaControlFrame(MediaControlFrame mediaControlFrame) {
        this.mediaControlFrame = mediaControlFrame;
    }

    public void setSongsQueue(List<Song> songsQueue) {
        this.songsQueue = songsQueue;
    }

    public void setPositionInSongQueue(int position) {
        if (songsQueue.isEmpty())
            return;

        songsQueuePosition = position;
        if (position < 0)
            songsQueuePosition = Math.max(0, songsQueue.size() - 1 + position);
        if (position >= songsQueue.size())
            songsQueuePosition = Math.min(songsQueue.size() - 1, position - songsQueue.size());
        musicPlayerFrame.updateCurrentSong(songsQueue.get(songsQueuePosition));
        mediaControlFrame.updateCurrentSong(songsQueue.get(songsQueuePosition));
        setMusicFilePath(songsQueue.get(songsQueuePosition).getPath());
        play();
    }

    public void nextPositionInSongQueue() {
        if (!isSongSelected)
            return;

        switch (repeatMode) {
            case REPEAT:
                setPositionInSongQueue(songsQueuePosition + 1);
                break;
            case REPEAT_ONCE:
                setPositionInSongQueue(songsQueuePosition);
                break;
            case REPEAT_OFF:
                if (songsQueuePosition + 1 >= songsQueue.size()) {
                    try {
                        musicPlayer.stop();
                    } catch (BasicPlayerException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }
                setPositionInSongQueue(songsQueuePosition + 1);
                break;
        }
    }

    public void prevPositionInSongQueue() {
        if (!isSongSelected)
            return;

        setPositionInSongQueue(songsQueuePosition - 1);
    }

    public void setRepeatMode(RepeatMode repeatMode) {
        this.repeatMode = repeatMode;
        musicPlayerFrame.updateRepeatButton(repeatMode);
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
        musicPlayerFrame.updateShuffleButton(shuffle);
    }

    public RepeatMode getRepeatMode() {
        return repeatMode;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public Song getCurrentPlayingSong() {
        if (songsQueue == null || !isSongSelected)
            return null;
        return songsQueue.get(songsQueuePosition);
    }

    private void play() {
        if (!isSongSelected)
            return;

        try {
            musicPlayer.stop();
            musicPlayer.play();
            isPlaying = true;
            musicPlayerFrame.updatePlayPauseButton(true);
            mediaControlFrame.updatePlayPauseButton(true);
        } catch (BasicPlayerException e) {
            throw new RuntimeException(e);
        }
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
                mediaControlFrame.updateSlider((int) currentSongMicroseconds, (int) songLengthMicroseconds);
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
