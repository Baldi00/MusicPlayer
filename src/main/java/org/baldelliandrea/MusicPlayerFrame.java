package org.baldelliandrea;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

public class MusicPlayerFrame extends JFrame {

    private ImageIcon playIcon;
    private ImageIcon pauseIcon;
    private ImageIcon prevIcon;
    private ImageIcon nextIcon;
    private ImageIcon shuffleIcon;
    private ImageIcon shuffleOffIcon;
    private ImageIcon repeatIcon;
    private ImageIcon repeat1Icon;
    private ImageIcon repeatOffIcon;

    private JButton playButton;
    private JButton prevButton;
    private JButton nextButton;
    private JButton shuffleButton;
    private JButton repeatButton;

    public MusicPlayerFrame() {
        loadControlsSprites();
        setupGraphic();
    }

    private void setupGraphic() {
        createControlButtons();

        setLayout(new BorderLayout());

        JPanel northPanel = createNorthPanel();
        JPanel centralPanel = createCentralPanel();
        JPanel southPanel = createSouthPanel();

        add(northPanel, BorderLayout.NORTH);
        add(centralPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void loadControlsSprites() {
        playIcon = getSpriteResource("controls/play-circle.png");
        pauseIcon = getSpriteResource("controls/pause-circle.png");
        prevIcon = getSpriteResource("controls/rewind-circle.png");
        nextIcon = getSpriteResource("controls/fast-forward-circle.png");
        shuffleIcon = getSpriteResource("controls/shuffle.png");
        shuffleOffIcon = getSpriteResource("controls/shuffle-off.png");
        repeatIcon = getSpriteResource("controls/repeat.png");
        repeat1Icon = getSpriteResource("controls/repeat-1.png");
        repeatOffIcon = getSpriteResource("controls/repeat-off.png");
    }

    private JPanel createSouthPanel() {
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setPreferredSize(new Dimension(10, 150));
        southPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JPanel controlsPanel = new JPanel(new GridBagLayout());
        controlsPanel.add(shuffleButton);
        controlsPanel.add(prevButton);
        controlsPanel.add(playButton);
        controlsPanel.add(nextButton);
        controlsPanel.add(repeatButton);

        JSlider songSlider = new JSlider();

        JPanel currentSongPanel = new JPanel(new BorderLayout());
        currentSongPanel.setPreferredSize(new Dimension(500, 0));
        currentSongPanel.add(new JLabel(scaleIcon(playIcon, 100)), BorderLayout.WEST);

        JLabel currentSongInfoLabel = new JLabel(formatSongText("Title", "Artist", "Album", 5));
        currentSongInfoLabel.setFont(currentSongInfoLabel.getFont().deriveFont(25f));
        currentSongInfoLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        currentSongPanel.add(currentSongInfoLabel, BorderLayout.CENTER);

        JPanel rightSouthFillerPanel = new JPanel();
        rightSouthFillerPanel.setPreferredSize(new Dimension(500, 0));

        southPanel.add(controlsPanel, BorderLayout.CENTER);
        southPanel.add(songSlider, BorderLayout.NORTH);
        southPanel.add(currentSongPanel, BorderLayout.WEST);
        southPanel.add(rightSouthFillerPanel, BorderLayout.EAST);

        return southPanel;
    }

    private JPanel createNorthPanel() {
        JPanel northPanel = new JPanel(new GridLayout());
        JTextField searchField = new JTextField();
        northPanel.setBorder(new EmptyBorder(10, 10, 20, 10));
        northPanel.add(searchField);
        return northPanel;
    }

    private JPanel createCentralPanel() {
        JPanel centralPanel = new JPanel(new GridLayout(1, 5));

        List<JButton> playlistButtons = new ArrayList<>();
        List<JButton> songsButtons = new ArrayList<>();
        List<JButton> artistsButtons = new ArrayList<>();
        List<JButton> albumsButtons = new ArrayList<>();
        List<JButton> queueButtons = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            playlistButtons.add(createScrollPaneButton("Playlist", null));
            songsButtons.add(createScrollPaneButton(formatSongText("Title", "Artist", "Album", 3), playIcon));
            artistsButtons.add(createScrollPaneButton("Artist", null));
            albumsButtons.add(createScrollPaneButton("Album", playIcon));
            queueButtons.add(createScrollPaneButton(formatSongText("Title", "Artist", "Album", 3), playIcon));
        }

        JPanel playlistPanel = createScrollPanePanel("Playlists", playlistButtons);
        JPanel songsPanel = createScrollPanePanel("Songs", songsButtons);
        JPanel artistsPanel = createScrollPanePanel("Artists", artistsButtons);
        JPanel albumsPanel = createScrollPanePanel("Albums", albumsButtons);
        JPanel queuePanel = createScrollPanePanel("Queue", queueButtons);

        centralPanel.add(playlistPanel);
        centralPanel.add(songsPanel);
        centralPanel.add(artistsPanel);
        centralPanel.add(albumsPanel);
        centralPanel.add(queuePanel);
        return centralPanel;
    }

    private JButton createScrollPaneButton(String innerText, ImageIcon icon) {
        JButton button = new JButton(innerText);
        if (icon != null) {
            button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH)));
        }
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setPreferredSize(new Dimension(0, 60));
        return button;
    }

    private JPanel createScrollPanePanel(String title, List<JButton> buttonList) {
        JPanel scrollPanePanel = new JPanel(new BorderLayout());
        JLabel scrollPaneTitle = new JLabel(title);
        JPanel scrollContainer1 = new JPanel(new BorderLayout());
        JPanel scrollContainer2 = new JPanel(new GridLayout(0, 1));
        JScrollPane scrollPane = new JScrollPane();

        for (JButton button : buttonList)
            scrollContainer2.add(button);
        scrollContainer1.add(scrollContainer2, BorderLayout.NORTH);

        scrollPane.setViewportView(scrollContainer1);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        scrollPaneTitle.setFont(scrollPaneTitle.getFont().deriveFont(20f));
        scrollPaneTitle.setHorizontalAlignment(SwingConstants.CENTER);
        scrollPanePanel.add(scrollPaneTitle, BorderLayout.NORTH);
        scrollPanePanel.add(scrollPane, BorderLayout.CENTER);

        return scrollPanePanel;
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

    private ImageIcon scaleIcon(ImageIcon icon, int size) {
        return new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

    private JButton createControlButton(ImageIcon icon, int size) {
        JButton controlButton = new JButton(scaleIcon(icon, size));
        controlButton.setBackground(new Color(0, 0, 0, 0));
        return controlButton;
    }

    private void createControlButtons() {
        playButton = createControlButton(playIcon, 60);
        prevButton = createControlButton(prevIcon, 40);
        nextButton = createControlButton(nextIcon, 40);
        shuffleButton = createControlButton(shuffleIcon, 20);
        repeatButton = createControlButton(repeatIcon, 20);
    }

    private String formatSongText(String title, String artist, String album, int size) {
        return "<html>" + title + "<br><font size=\"" + size + "\" color=\"gray\">" + artist + " • " + album + "</font></html>";
    }
}
