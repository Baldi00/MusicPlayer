package org.baldelliandrea.ui;

import org.baldelliandrea.musicplayer.MusicPlayer;
import org.baldelliandrea.song.Song;
import org.baldelliandrea.song.SongTitleComparator;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

public class MusicPlayerFrame extends JFrame {
    private TreeMap<String, Song> songsList;
    private MusicPlayer musicPlayer;

    private JPanel playlistButtonsPanel;
    private JPanel songsButtonsPanel;
    private JPanel artistsButtonsPanel;
    private JPanel albumsButtonsPanel;
    private JPanel queueButtonsPanel;

    private List<JButton> playlistButtons;
    private List<JButton> queueButtons;

    private Map<Song, JButton> songsButtons;
    private Map<String, JButton> artistsButtons;
    private Map<String, JButton> albumsButtons;

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

    private JLabel currentSongInfoLabel;
    private JSlider songSlider;
    private JLabel songCurrentTimeLabel;
    private JLabel songTotalTimeLabel;

    private Song currentSong;

    public MusicPlayerFrame(TreeMap<String, Song> songsList, MusicPlayer musicPlayer) {
        this.songsList = songsList;
        this.musicPlayer = musicPlayer;

        playlistButtons = new ArrayList<>();
        queueButtons = new ArrayList<>();
        loadControlsSprites();
        createTitleArtistAlbumButtons();
        createControlButtons();
        createAndShowWindow();
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

    private void createTitleArtistAlbumButtons() {
        // Prepare songs, artists and albums lists
        List<Song> songsByTitle = new ArrayList<>(songsList.values());
        songsByTitle.sort(new SongTitleComparator());

        Map<String, Song> songsByArtist = new TreeMap<>();
        for (Song song : songsByTitle)
            if (!songsByArtist.containsKey(song.getArtist()))
                songsByArtist.put(song.getArtist(), song);

        Map<String, Song> songsByAlbum = new TreeMap<>();
        for (Song song : songsByTitle)
            if (!songsByAlbum.containsKey(song.getArtist()))
                songsByAlbum.put(song.getAlbum(), song);

        // Create buttons
        songsButtons = new TreeMap<>();
        artistsButtons = new TreeMap<>();
        albumsButtons = new TreeMap<>();

        for (Song song : songsByTitle) {
            ImageIcon cover45 = new ImageIcon(song.getCoverPath45());
            JButton songButton = createSongButton(formatSongText(song.getTitle(), song.getArtist(), song.getAlbum(), 3), cover45);
            songButton.addActionListener(actionEvent -> {
                currentSong = song;
                currentSongInfoLabel.setText(formatSongText(song.getTitle(), song.getArtist(), song.getAlbum(), 5));
                currentSongInfoLabel.setIcon(new ImageIcon(song.getCoverPath100()));
                musicPlayer.setMusicFilePath(song.getPath());
                musicPlayer.play();
                songSlider.setEnabled(true);
            });
            songsButtons.put(song, songButton);
        }

        for (Song song : songsByArtist.values())
            artistsButtons.put(song.getArtist(), createSongButton(song.getArtist(), null));

        for (Song song : songsByAlbum.values()) {
            ImageIcon cover45 = new ImageIcon(song.getCoverPath45());
            albumsButtons.put(song.getAlbum(), createSongButton(song.getAlbum(), cover45));
        }
    }

    private JButton createSongButton(String innerText, ImageIcon icon) {
        JButton button = new JButton(innerText);
        if (icon != null)
            button.setIcon(icon);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setPreferredSize(new Dimension(0, 60));
        button.setSize(new Dimension(0, 60));
        return button;
    }

    private void createControlButtons() {
        playButton = createControlButton(playIcon, 60);
        prevButton = createControlButton(prevIcon, 40);
        nextButton = createControlButton(nextIcon, 40);
        shuffleButton = createControlButton(shuffleIcon, 20);
        repeatButton = createControlButton(repeatIcon, 20);
    }

    private JButton createControlButton(ImageIcon icon, int size) {
        JButton controlButton = new JButton(scaleIcon(icon, size));
        controlButton.setBackground(new Color(0, 0, 0, 0));
        return controlButton;
    }

    private void createAndShowWindow() {
        setLayout(new BorderLayout());

        JPanel searchBarPanel = createSearchBarPanel();
        JPanel centralPanel = createCentralPanel();
        JPanel southPanel = createBottomPanel();

        add(searchBarPanel, BorderLayout.NORTH);
        add(centralPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private JPanel createSearchBarPanel() {
        JPanel searchBarPanel = new JPanel(new GridLayout());
        JTextField searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
            }

            public void removeUpdate(DocumentEvent e) {
                updateButtonsOnSearch(searchField.getText());
            }

            public void insertUpdate(DocumentEvent e) {
                updateButtonsOnSearch(searchField.getText());
            }
        });
        searchBarPanel.setBorder(new EmptyBorder(10, 10, 20, 10));
        searchBarPanel.add(searchField);
        return searchBarPanel;
    }

    private JPanel createCentralPanel() {
        JPanel centralPanel = new JPanel(new GridLayout(1, 5));

        playlistButtonsPanel = new JPanel(new GridLayout(0, 1));
        songsButtonsPanel = new JPanel(new GridLayout(0, 1));
        artistsButtonsPanel = new JPanel(new GridLayout(0, 1));
        albumsButtonsPanel = new JPanel(new GridLayout(0, 1));
        queueButtonsPanel = new JPanel(new GridLayout(0, 1));

        JPanel playlistPanelContainer = populateButtonsPanel(playlistButtonsPanel, playlistButtons, "Playlists");
        JPanel songsPanelContainer = populateButtonsPanel(songsButtonsPanel, songsButtons.values(), "Songs");
        JPanel artistsPanelContainer = populateButtonsPanel(artistsButtonsPanel, artistsButtons.values(), "Artists");
        JPanel albumsPanelContainer = populateButtonsPanel(albumsButtonsPanel, albumsButtons.values(), "Albums");
        JPanel queuePanelContainer = populateButtonsPanel(queueButtonsPanel, queueButtons, "Queue");

        centralPanel.add(playlistPanelContainer);
        centralPanel.add(songsPanelContainer);
        centralPanel.add(artistsPanelContainer);
        centralPanel.add(albumsPanelContainer);
        centralPanel.add(queuePanelContainer);

        return centralPanel;
    }

    private JPanel populateButtonsPanel(JPanel buttonsPanel, Collection<JButton> buttonList, String title) {
        JPanel container = new JPanel(new BorderLayout());
        JLabel scrollPaneTitle = new JLabel(title);
        JPanel scrollContainer1 = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane();

        for (JButton button : buttonList)
            buttonsPanel.add(button);
        scrollContainer1.add(buttonsPanel, BorderLayout.NORTH);

        scrollPane.setViewportView(scrollContainer1);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        scrollPaneTitle.setFont(scrollPaneTitle.getFont().deriveFont(20f));
        scrollPaneTitle.setHorizontalAlignment(SwingConstants.CENTER);

        container.add(scrollPaneTitle, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    private JPanel createBottomPanel() {
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setPreferredSize(new Dimension(10, 150));
        southPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JPanel controlsPanel = new JPanel(new GridBagLayout());
        controlsPanel.add(shuffleButton);
        controlsPanel.add(prevButton);
        controlsPanel.add(playButton);
        controlsPanel.add(nextButton);
        controlsPanel.add(repeatButton);

        JPanel sliderPanel = new JPanel(new BorderLayout());

        songSlider = new JSlider();
        songSlider.setEnabled(false);
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
        currentSongPanel.setPreferredSize(new Dimension(500, 0));

        currentSongInfoLabel = new JLabel();
        currentSongInfoLabel.setText(formatSongText("Title", "Artist", "Album", 5));
        currentSongInfoLabel.setIcon(scaleIcon(playIcon, 100));
        currentSongInfoLabel.setIconTextGap(10);
        currentSongInfoLabel.setFont(currentSongInfoLabel.getFont().deriveFont(25f));
        currentSongInfoLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        currentSongPanel.add(currentSongInfoLabel, BorderLayout.WEST);

        JPanel rightSouthFillerPanel = new JPanel();
        rightSouthFillerPanel.setPreferredSize(new Dimension(500, 0));

        southPanel.add(controlsPanel, BorderLayout.CENTER);
        southPanel.add(sliderPanel, BorderLayout.NORTH);
        southPanel.add(currentSongPanel, BorderLayout.WEST);
        southPanel.add(rightSouthFillerPanel, BorderLayout.EAST);

        return southPanel;
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

    private String formatSongText(String title, String artist, String album, int size) {
        return "<html>" + title + "<br><font size=\"" + size + "\" color=\"gray\">" + artist +
                " • " + album + "</font></html>";
    }

    private void updateButtonsOnSearch(String searchInput) {
        songsButtonsPanel.removeAll();
        artistsButtonsPanel.removeAll();
        albumsButtonsPanel.removeAll();

        searchInput = searchInput.toLowerCase();

        for (Song song : songsButtons.keySet())
            if (song.getTitle().toLowerCase().contains(searchInput) ||
                    song.getArtist().toLowerCase().contains(searchInput) ||
                    song.getAlbum().toLowerCase().contains(searchInput))
                songsButtonsPanel.add(songsButtons.get(song));

        for (String artist : artistsButtons.keySet())
            if (artist.toLowerCase().contains(searchInput))
                artistsButtonsPanel.add(artistsButtons.get(artist));

        for (String album : albumsButtons.keySet())
            if (album.toLowerCase().contains(searchInput))
                albumsButtonsPanel.add(albumsButtons.get(album));

        validate();
    }
}