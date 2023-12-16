package org.baldelliandrea.ui;

import org.baldelliandrea.musicplayer.MusicPlayer;
import org.baldelliandrea.musicplayer.RepeatMode;
import org.baldelliandrea.song.Song;
import org.baldelliandrea.song.SongTitleComparator;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private List<Song> songsQueue;
    private List<Song> currentPlayingQueueOrdered;

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

    public MusicPlayerFrame(TreeMap<String, Song> songsList, MusicPlayer musicPlayer) {
        this.songsList = songsList;
        this.musicPlayer = musicPlayer;

        playlistButtons = new ArrayList<>();
        songsQueue = new ArrayList<>();
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

    public void updateCurrentSong(Song currentSong) {
        currentSongInfoLabel.setText(formatSongText(currentSong.getTitle(), currentSong.getArtist(), currentSong.getAlbum(), 5));
        currentSongInfoLabel.setIcon(new ImageIcon(currentSong.getCoverPath100()));
    }

    public void updatePlayPauseButton(boolean isPlaying) {
        ImageIcon icon = isPlaying ? pauseIcon : playIcon;
        playButton.setIcon(scaleIcon(icon, 60));
    }

    public void updateRepeatButton(RepeatMode repeatMode) {
        ImageIcon icon = null;
        switch (repeatMode) {
            case REPEAT:
                icon = repeatIcon;
                break;
            case REPEAT_OFF:
                icon = repeatOffIcon;
                break;
            case REPEAT_ONCE:
                icon = repeat1Icon;
                break;
        }
        repeatButton.setIcon(scaleIcon(icon, 20));
    }

    public void updateShuffleButton(boolean shuffle) {
        ImageIcon icon = shuffle ? shuffleIcon : shuffleOffIcon;
        shuffleButton.setIcon(scaleIcon(icon, 20));
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
        songsQueue.addAll(songsByTitle);

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
                songSlider.setEnabled(true);
                setupSongQueueAndPlay(song, songsQueue);
            });
            songsButtons.put(song, songButton);
        }

        for (Song song : songsByArtist.values()) {
            JButton artistButton = createSongButton(song.getArtist(), null);
            artistButton.addActionListener(actionEvent -> {
                List<Song> songsWithArtist = new ArrayList<>();
                for (Song s : songsByTitle)
                    if (s.getArtist().equals(song.getArtist()))
                        songsWithArtist.add(s);
                songSlider.setEnabled(true);
                setupSongQueueAndPlay(null, songsWithArtist);
            });
            artistsButtons.put(song.getArtist(), artistButton);
        }

        for (Song song : songsByAlbum.values()) {
            ImageIcon cover45 = new ImageIcon(song.getCoverPath45());
            JButton albumButton = createSongButton(song.getAlbum(), cover45);
            albumButton.addActionListener(actionEvent -> {
                List<Song> songsWithAlbum = new ArrayList<>();
                for (Song s : songsByTitle)
                    if (s.getAlbum().equals(song.getAlbum()))
                        songsWithAlbum.add(s);
                songSlider.setEnabled(true);
                setupSongQueueAndPlay(null, songsWithAlbum);
            });
            albumsButtons.put(song.getAlbum(), albumButton);
        }
    }

    private void setupSongQueueAndPlay(Song currentSong, List<Song> queue) {
        if (queue == null)
            return;

        currentPlayingQueueOrdered = new ArrayList<>(queue);

        List<Song> currentPlayingQueue = new ArrayList<>(currentPlayingQueueOrdered);
        if (musicPlayer.isShuffle())
            Collections.shuffle(currentPlayingQueue);

        if (currentSong != null)
            while (currentPlayingQueue.get(0) != currentSong) {
                Song s = currentPlayingQueue.remove(0);
                currentPlayingQueue.add(s);
            }

        musicPlayer.setSongsQueue(currentPlayingQueue);
        musicPlayer.setPositionInSongQueue(0);

        queueButtonsPanel.removeAll();
        for (int i = 0; i < currentPlayingQueue.size(); i++) {
            Song s = currentPlayingQueue.get(i);
            JButton queueButton = createSongButton(formatSongText(s.getTitle(), s.getArtist(), s.getAlbum(), 3),
                    new ImageIcon(s.getCoverPath45()));
            int finalI = i;
            queueButton.addActionListener(actionEvent1 -> {
                musicPlayer.setPositionInSongQueue(finalI);
            });
            queueButtonsPanel.add(queueButton);
        }
        validate();
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
        playButton.addActionListener(actionEvent -> musicPlayer.togglePlayPause());
        prevButton = createControlButton(prevIcon, 40);
        prevButton.addActionListener(actionEvent -> musicPlayer.prevPositionInSongQueue());
        nextButton = createControlButton(nextIcon, 40);
        nextButton.addActionListener(actionEvent -> musicPlayer.nextPositionInSongQueue());
        shuffleButton = createControlButton(shuffleOffIcon, 20);
        shuffleButton.addActionListener(actionEvent -> {
            musicPlayer.setShuffle(!musicPlayer.isShuffle());
            setupSongQueueAndPlay(musicPlayer.getCurrentPlayingSong(), currentPlayingQueueOrdered);
        });
        repeatButton = createControlButton(repeatOffIcon, 20);
        repeatButton.addActionListener(actionEvent -> {
            RepeatMode nextRepeatMode = RepeatMode.values()[(musicPlayer.getRepeatMode().ordinal() + 1) % 3];
            musicPlayer.setRepeatMode(nextRepeatMode);
        });

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

        setSize(1920, 1080);
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
        JPanel queuePanelContainer = populateButtonsPanel(queueButtonsPanel, null, "Queue");

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

        if (buttonList != null)
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

                if (slider.isEnabled()) {
                    float percentage = (float) (mouseEvent.getXOnScreen() - slider.getLocationOnScreen().x) / slider.getSize().width;
                    musicPlayer.setPosition(percentage);
                    slider.setValue((int) (percentage * slider.getMaximum()));
                }
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
        currentSongPanel.setPreferredSize(new Dimension(700, 0));

        currentSongInfoLabel = new JLabel();
        currentSongInfoLabel.setText(formatSongText("Title", "Artist", "Album", 5));
        currentSongInfoLabel.setIcon(scaleIcon(playIcon, 100));
        currentSongInfoLabel.setIconTextGap(10);
        currentSongInfoLabel.setFont(currentSongInfoLabel.getFont().deriveFont(25f));
        currentSongInfoLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        currentSongPanel.add(currentSongInfoLabel, BorderLayout.WEST);

        JPanel rightSouthFillerPanel = new JPanel();
        rightSouthFillerPanel.setPreferredSize(new Dimension(700, 0));

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

        songsQueue.clear();
        for (Song song : songsButtons.keySet())
            if (song.getTitle().toLowerCase().contains(searchInput) ||
                    song.getArtist().toLowerCase().contains(searchInput) ||
                    song.getAlbum().toLowerCase().contains(searchInput)) {
                songsButtonsPanel.add(songsButtons.get(song));
                songsQueue.add(song);
            }

        for (String artist : artistsButtons.keySet())
            if (artist.toLowerCase().contains(searchInput))
                artistsButtonsPanel.add(artistsButtons.get(artist));

        for (String album : albumsButtons.keySet())
            if (album.toLowerCase().contains(searchInput))
                albumsButtonsPanel.add(albumsButtons.get(album));

        validate();
    }
}
