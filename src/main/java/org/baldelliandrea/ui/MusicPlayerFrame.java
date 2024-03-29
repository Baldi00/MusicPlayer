package org.baldelliandrea.ui;

import org.baldelliandrea.musicplayer.MusicPlayer;
import org.baldelliandrea.musicplayer.RepeatMode;
import org.baldelliandrea.playlist.PlaylistManager;
import org.baldelliandrea.song.Song;
import org.baldelliandrea.song.SongCreationTimeComparator;
import org.baldelliandrea.song.SongLastModifiedComparator;
import org.baldelliandrea.song.SongTitleComparator;
import org.baldelliandrea.utils.MusicPlayerUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

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
    private Map<Song, JButton> queueButtons;

    private ImageIcon playIcon;
    private ImageIcon pauseIcon;
    private ImageIcon prevIcon;
    private ImageIcon nextIcon;
    private ImageIcon shuffleIcon;
    private ImageIcon shuffleOffIcon;
    private ImageIcon repeatIcon;
    private ImageIcon repeat1Icon;
    private ImageIcon repeatOffIcon;
    private ImageIcon settingsIcon;

    private JButton playButton;
    private JButton prevButton;
    private JButton nextButton;
    private JButton shuffleButton;
    private JButton repeatButton;
    private JButton settingsButton;

    private JLabel currentSongInfoLabel;
    private JSlider songSlider;
    private JLabel songCurrentTimeLabel;
    private JLabel songTotalTimeLabel;

    private MenuItem trayCurrentSongTitleArtist;

    private ProgressBarWindow progressBarWindow;

    public MusicPlayerFrame(TreeMap<String, Song> songsList, MusicPlayer musicPlayer) {
        this.songsList = songsList;
        this.musicPlayer = musicPlayer;

        progressBarWindow = new ProgressBarWindow();
        progressBarWindow.setLabel("Creating UI");
        progressBarWindow.setVisible(true);

        playlistButtons = new ArrayList<>();
        songsQueue = new ArrayList<>();
        queueButtons = new HashMap<>();
        loadControlsSprites();
        createTitleArtistAlbumButtons();
        createPlaylistsButtons();
        createControlButtons();

        createAndShowWindow();
        createTrayIcon();
        progressBarWindow.dispose();
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
        songSlider.setEnabled(true);

        for (JButton songButton : queueButtons.values())
            songButton.setForeground(Color.WHITE);

        queueButtons.get(currentSong).setForeground(new Color(0, 128, 255));
        trayCurrentSongTitleArtist.setLabel(currentSong.getTitle() + " - " + currentSong.getArtist());
    }

    public void updatePlayPauseButton(boolean isPlaying) {
        ImageIcon icon = isPlaying ? pauseIcon : playIcon;
        playButton.setIcon(MusicPlayerUtils.scaleIcon(icon, 60));
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
        repeatButton.setIcon(MusicPlayerUtils.scaleIcon(icon, 20));
    }

    public void updateShuffleButton(boolean shuffle) {
        ImageIcon icon = shuffle ? shuffleIcon : shuffleOffIcon;
        shuffleButton.setIcon(MusicPlayerUtils.scaleIcon(icon, 20));
    }

    public void toggleVisibility() {
        if (isVisible())
            setVisible(false);
        else {
            setState(Frame.NORMAL);
            setVisible(true);
            toFront();
            requestFocus();
        }
    }

    private void loadControlsSprites() {
        playIcon = MusicPlayerUtils.getSpriteResource("controls/play-circle.png");
        pauseIcon = MusicPlayerUtils.getSpriteResource("controls/pause-circle.png");
        prevIcon = MusicPlayerUtils.getSpriteResource("controls/rewind-circle.png");
        nextIcon = MusicPlayerUtils.getSpriteResource("controls/fast-forward-circle.png");
        shuffleIcon = MusicPlayerUtils.getSpriteResource("controls/shuffle.png");
        shuffleOffIcon = MusicPlayerUtils.getSpriteResource("controls/shuffle-off.png");
        repeatIcon = MusicPlayerUtils.getSpriteResource("controls/repeat.png");
        repeat1Icon = MusicPlayerUtils.getSpriteResource("controls/repeat-1.png");
        repeatOffIcon = MusicPlayerUtils.getSpriteResource("controls/repeat-off.png");
        settingsIcon = MusicPlayerUtils.getSpriteResource("controls/settings.png");
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

        progressBarWindow.setProgressBarMax(songsByTitle.size() + songsByArtist.size() + songsByAlbum.size());

        for (Song song : songsByTitle) {
            ImageIcon cover45 = new ImageIcon(song.getCoverPath45());
            JButton songButton = createButton(formatSongText(song.getTitle(), song.getArtist(), song.getAlbum(), 3), cover45);
            songButton.addActionListener(actionEvent -> setupSongQueueAndPlay(song, songsQueue));
            songsButtons.put(song, songButton);
            progressBarWindow.incrementProgressBar();
        }

        for (Song song : songsByArtist.values()) {
            JButton artistButton = createButton(song.getArtist(), null);
            artistButton.addActionListener(actionEvent -> {
                List<Song> songsWithArtist = new ArrayList<>();
                for (Song s : songsByTitle)
                    if (s.getArtist().equals(song.getArtist()))
                        songsWithArtist.add(s);
                setupSongQueueAndPlay(null, songsWithArtist);
            });
            artistsButtons.put(song.getArtist(), artistButton);
            progressBarWindow.incrementProgressBar();
        }

        for (Song song : songsByAlbum.values()) {
            ImageIcon cover45 = new ImageIcon(song.getCoverPath45());
            JButton albumButton = createButton(song.getAlbum(), cover45);
            albumButton.addActionListener(actionEvent -> {
                List<Song> songsWithAlbum = new ArrayList<>();
                for (Song s : songsByTitle)
                    if (s.getAlbum().equals(song.getAlbum()))
                        songsWithAlbum.add(s);
                setupSongQueueAndPlay(null, songsWithAlbum);
            });
            albumsButtons.put(song.getAlbum(), albumButton);
            progressBarWindow.incrementProgressBar();
        }
    }

    private void createPlaylistsButtons() {
        playlistButtons = new ArrayList<>();

        // Sort by
        List<Song> creationTimeSongs = new ArrayList<>(songsList.values());
        List<Song> lastModifiedSongs = new ArrayList<>(songsList.values());

        creationTimeSongs.sort(new SongCreationTimeComparator());
        lastModifiedSongs.sort(new SongLastModifiedComparator());

        JButton creationTimeButton = createButton("Sort by: Creation Date", null);
        creationTimeButton.addActionListener(actionEvent -> setupSongQueueAndPlay(null, creationTimeSongs));
        playlistButtons.add(creationTimeButton);

        JButton lastModifiedButton = createButton("Sort by: Last Modified", null);
        lastModifiedButton.addActionListener(actionEvent -> setupSongQueueAndPlay(null, lastModifiedSongs));
        playlistButtons.add(lastModifiedButton);

        // Genres
        Map<String, List<Song>> songsGroupedByGenre = new TreeMap<>();
        for (Song song : songsList.values()) {
            if (!songsGroupedByGenre.containsKey(song.getGenre()))
                songsGroupedByGenre.put(song.getGenre(), new ArrayList<>());
            songsGroupedByGenre.get(song.getGenre()).add(song);
        }

        for (String genre : songsGroupedByGenre.keySet()) {
            JButton genreButton = createButton("Genre: " + genre, null);
            genreButton.addActionListener(actionEvent -> setupSongQueueAndPlay(null, songsGroupedByGenre.get(genre)));
            playlistButtons.add(genreButton);
        }

        // Playlists
        Map<String, List<Song>> songGroupedByPlaylist = PlaylistManager.getPlaylists();

        for (String playlist : songGroupedByPlaylist.keySet()) {
            JButton playlistButton = createButton(playlist, null);
            playlistButton.addActionListener(actionEvent -> setupSongQueueAndPlay(null, songGroupedByPlaylist.get(playlist)));
            playlistButtons.add(playlistButton);
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

        queueButtonsPanel.removeAll();
        queueButtons.clear();
        for (int i = 0; i < currentPlayingQueue.size(); i++) {
            Song s = currentPlayingQueue.get(i);
            JButton queueButton = createButton(formatSongText(s.getTitle(), s.getArtist(), s.getAlbum(), 3),
                    new ImageIcon(s.getCoverPath45()));
            int finalI = i;
            queueButton.addActionListener(actionEvent1 -> {
                musicPlayer.setPositionInSongQueue(finalI);
            });
            queueButtons.put(s, queueButton);
            queueButtonsPanel.add(queueButton);
        }
        validate();

        musicPlayer.setSongsQueue(currentPlayingQueue);
        musicPlayer.setPositionInSongQueue(0);
    }

    private JButton createButton(String innerText, ImageIcon icon) {
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
        settingsButton = createControlButton(settingsIcon, 20);
        settingsButton.addActionListener(actionEvent -> {
            try {
                Desktop.getDesktop().open(new File(MusicPlayerUtils.SONGS_PATHS_FILE));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    private JButton createControlButton(ImageIcon icon, int size) {
        JButton controlButton = new JButton(MusicPlayerUtils.scaleIcon(icon, size));
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

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent e) {
                setVisible(false);
            }
        });

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setIconImage(MusicPlayerUtils.getSpriteResource("musicPlayerIcon64.png").getImage());
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
        currentSongInfoLabel.setIcon(MusicPlayerUtils.scaleIcon(playIcon, 100));
        currentSongInfoLabel.setIconTextGap(10);
        currentSongInfoLabel.setFont(currentSongInfoLabel.getFont().deriveFont(25f));
        currentSongInfoLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        currentSongPanel.add(currentSongInfoLabel, BorderLayout.WEST);

        JPanel rightSouthPanel = new JPanel(new BorderLayout());
        rightSouthPanel.setPreferredSize(new Dimension(700, 0));
        JPanel rightSouthEastPanel = new JPanel();
        rightSouthEastPanel.setLayout(new BoxLayout(rightSouthEastPanel, BoxLayout.X_AXIS));

        JSlider volumeSlider = new JSlider(0, 1000, 1000);

        volumeSlider.addChangeListener(changeEvent ->
                musicPlayer.setVolume((double) volumeSlider.getValue() / volumeSlider.getMaximum()));

        rightSouthEastPanel.add(volumeSlider);
        rightSouthEastPanel.add(settingsButton);
        rightSouthPanel.add(rightSouthEastPanel, BorderLayout.EAST);

        southPanel.add(controlsPanel, BorderLayout.CENTER);
        southPanel.add(sliderPanel, BorderLayout.NORTH);
        southPanel.add(currentSongPanel, BorderLayout.WEST);
        southPanel.add(rightSouthPanel, BorderLayout.EAST);

        return southPanel;
    }

    private void createTrayIcon() {

        //Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        PopupMenu popup = new PopupMenu();
        TrayIcon trayIcon = new TrayIcon(MusicPlayerUtils.scaleIcon(
                MusicPlayerUtils.getSpriteResource("musicPlayerIcon64.png"), 16).getImage());
        SystemTray tray = SystemTray.getSystemTray();

        trayIcon.addActionListener(actionEvent -> toggleVisibility());

        // Create a pop-up menu components
        MenuItem playPause = new MenuItem("Play/Pause");
        MenuItem next = new MenuItem("Next");
        MenuItem previous = new MenuItem("Previous");
        MenuItem showPlayer = new MenuItem("Show Player");
        MenuItem exit = new MenuItem("Exit");
        trayCurrentSongTitleArtist = new MenuItem("Title - Artist");
        trayCurrentSongTitleArtist.setEnabled(false);

        playPause.addActionListener(actionEvent -> musicPlayer.togglePlayPause());
        next.addActionListener(actionEvent -> musicPlayer.nextPositionInSongQueue());
        previous.addActionListener(actionEvent -> musicPlayer.prevPositionInSongQueue());
        showPlayer.addActionListener(actionEvent -> {
            setState(Frame.NORMAL);
            setVisible(true);
            toFront();
            requestFocus();
        });
        exit.addActionListener(actionEvent -> System.exit(0));

        //Add components to pop-up menu
        popup.add(trayCurrentSongTitleArtist);
        popup.addSeparator();
        popup.add(playPause);
        popup.add(next);
        popup.add(previous);
        popup.addSeparator();
        popup.add(showPlayer);
        popup.add(exit);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
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
            if (song.contains(searchInput)) {
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
