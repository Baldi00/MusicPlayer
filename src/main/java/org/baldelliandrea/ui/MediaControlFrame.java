package org.baldelliandrea.ui;

import org.baldelliandrea.musicplayer.MusicPlayer;
import org.baldelliandrea.song.Song;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;

public class MediaControlFrame extends JFrame {

    private MusicPlayer musicPlayer;

    private ImageIcon playIcon;
    private ImageIcon pauseIcon;
    private ImageIcon prevIcon;
    private ImageIcon nextIcon;

    private JButton playButton;
    private JButton prevButton;
    private JButton nextButton;

    private JLabel currentSongInfoLabel;
    private JSlider songSlider;
    private JLabel songCurrentTimeLabel;
    private JLabel songTotalTimeLabel;

    public MediaControlFrame(MusicPlayer musicPlayer) {
        this.musicPlayer = musicPlayer;

        loadControlsSprites();
        createControlButtons();
        setSize(800, 175);
        setUndecorated(true);
        setLocation(150, 75);
        setType(JFrame.Type.UTILITY);
        setAlwaysOnTop(true);
        add(createMediaPanel());
    }

    public void updateSlider(int value, int max) {
        int totalMinutes = max / 1000000 / 60;
        int totalSeconds = (max / 1000000) % 60;
        long currentMinutes = value / 1000000 / 60;
        long currentSeconds = (value / 1000000) % 60;

        songCurrentTimeLabel.setText(String.format("%02d:%02d", currentMinutes, currentSeconds));
        songTotalTimeLabel.setText(String.format("%02d:%02d", totalMinutes, totalSeconds));

        songSlider.setMinimum(0);
        songSlider.setMaximum(max);
        songSlider.setValue(value);
    }

    public void updateCurrentSong(Song currentSong) {
        currentSongInfoLabel.setText(formatSongText(currentSong.getTitle(), currentSong.getArtist(), currentSong.getAlbum(), 5));
        currentSongInfoLabel.setIcon(new ImageIcon(currentSong.getCoverPath100()));
    }

    public void updatePlayPauseButton(boolean isPlaying) {
        ImageIcon icon = isPlaying ? pauseIcon : playIcon;
        playButton.setIcon(scaleIcon(icon, 60));
    }

    private void loadControlsSprites() {
        playIcon = getSpriteResource("controls/play-circle.png");
        pauseIcon = getSpriteResource("controls/pause-circle.png");
        prevIcon = getSpriteResource("controls/rewind-circle.png");
        nextIcon = getSpriteResource("controls/fast-forward-circle.png");
    }

    private ImageIcon getSpriteResource(String path) {
        try (InputStream stream = MusicPlayerFrame.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null)
                throw new MissingResourceException("Resource cannot be found", MusicPlayerFrame.class.getName(), path);
            BufferedImage bufferedImage = ImageIO.read(stream);
            return new ImageIcon(bufferedImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createControlButtons() {
        playButton = createControlButton(playIcon, 60);
        playButton.addActionListener(actionEvent -> musicPlayer.togglePlayPause());
        prevButton = createControlButton(prevIcon, 40);
        prevButton.addActionListener(actionEvent -> musicPlayer.prevPositionInSongQueue());
        nextButton = createControlButton(nextIcon, 40);
        nextButton.addActionListener(actionEvent -> musicPlayer.nextPositionInSongQueue());
    }

    private JButton createControlButton(ImageIcon icon, int size) {
        JButton controlButton = new JButton(scaleIcon(icon, size));
        controlButton.setBackground(new Color(0, 0, 0, 0));
        return controlButton;
    }

    private JPanel createMediaPanel() {
        JPanel mediaPanel = new JPanel(new BorderLayout());
        mediaPanel.setPreferredSize(new Dimension(10, 150));
        mediaPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JPanel controlsPanel = new JPanel(new GridBagLayout());
        controlsPanel.add(prevButton);
        controlsPanel.add(playButton);
        controlsPanel.add(nextButton);

        JPanel sliderPanel = new JPanel(new BorderLayout());

        songSlider = new JSlider();
        songSlider.addMouseListener(new MouseInputListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                JSlider slider = (JSlider) mouseEvent.getSource();
                if (slider.isEnabled())
                    musicPlayer.setPosition((float) slider.getValue() / slider.getMaximum());
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
            }
        });
        songSlider.setMinimum(0);
        songSlider.setMinimum(100);
        songSlider.setValue(0);

        songCurrentTimeLabel = new JLabel("00:00");
        songTotalTimeLabel = new JLabel("00:00");

        sliderPanel.add(songCurrentTimeLabel, BorderLayout.WEST);
        sliderPanel.add(songSlider, BorderLayout.CENTER);
        sliderPanel.add(songTotalTimeLabel, BorderLayout.EAST);

        JPanel currentSongPanel = new JPanel(new BorderLayout());
        currentSongPanel.setPreferredSize(new Dimension(600, 0));

        currentSongInfoLabel = new JLabel();
        currentSongInfoLabel.setText(formatSongText("Title", "Artist", "Album", 5));
        currentSongInfoLabel.setIcon(scaleIcon(playIcon, 100));
        currentSongInfoLabel.setIconTextGap(10);
        currentSongInfoLabel.setFont(currentSongInfoLabel.getFont().deriveFont(25f));
        currentSongInfoLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        currentSongPanel.add(currentSongInfoLabel, BorderLayout.WEST);

        mediaPanel.add(currentSongPanel, BorderLayout.WEST);
        mediaPanel.add(controlsPanel, BorderLayout.EAST);
        mediaPanel.add(sliderPanel, BorderLayout.SOUTH);

        return mediaPanel;
    }

    private ImageIcon scaleIcon(ImageIcon icon, int size) {
        return new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

    private String formatSongText(String title, String artist, String album, int size) {
        return "<html>" + title + "<br><font size=\"" + size + "\" color=\"gray\">" + artist +
                " • " + album + "</font></html>";
    }
}
