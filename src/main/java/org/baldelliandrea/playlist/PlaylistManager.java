package org.baldelliandrea.playlist;

import org.baldelliandrea.song.Song;
import org.baldelliandrea.song.SongCacheManager;
import org.baldelliandrea.utils.MusicPlayerUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlaylistManager {
    public static Map<String, List<Song>> getPlaylists() {
        Map<String, List<Song>> playlists = new TreeMap<>();
        SongCacheManager songCacheManager = new SongCacheManager();

        for (String playlistPath : getPlaylistsPaths()) {
            String playlistName = new File(playlistPath).getName().replace(".zpl", "");
            List<Song> songsList = new ArrayList<>();
            for (String songPath : getPlaylistSongsPaths(playlistPath)) {
                File songFile = new File(songPath);
                if (!songFile.getName().toLowerCase().endsWith(".mp3"))
                    continue;
                songsList.add(songCacheManager.getSong(songFile.getAbsolutePath(), songFile.getName()));
            }
            playlists.put(playlistName, songsList);
        }

        return playlists;
    }

    private static List<String> getPlaylistsPaths() {
        List<String> playlistsPaths = new ArrayList<>();
        for (String songsPath : MusicPlayerUtils.getSongsPaths())
            getPlaylistsPathsRecursive(new File(songsPath), playlistsPaths);
        return playlistsPaths;
    }

    private static void getPlaylistsPathsRecursive(File startFolder, List<String> playlistsPaths) {
        if (startFolder.exists() && startFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(startFolder.listFiles())) {
                if (file.isDirectory())
                    getPlaylistsPathsRecursive(file, playlistsPaths);
                else if (file.isFile() && file.getName().endsWith(".zpl"))
                    playlistsPaths.add(file.getAbsolutePath());
            }
        }
    }

    private static List<String> getPlaylistSongsPaths(String playlistPath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = factory.newDocumentBuilder();
            Document doc = parser.parse(new File(playlistPath));
            NodeList playlistSongsElements = doc.getElementsByTagName("media");
            List<String> playlistSongsPaths = new ArrayList<>();

            for (int i = 0; i < playlistSongsElements.getLength(); i++) {
                Element e = (Element) playlistSongsElements.item(i);
                String src = e.getAttribute("src");
                playlistSongsPaths.add(src);
            }
            return playlistSongsPaths;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
