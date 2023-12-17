package org.baldelliandrea.song;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class SongCacheManager {
    private final String programDataFolderPath = System.getenv("APPDATA") + "\\BaldelliMusicPlayer";
    private final String cacheFolderPath = System.getenv("APPDATA") + "\\BaldelliMusicPlayer\\cache";

    public SongCacheManager() {
        File programDataFolder = new File(programDataFolderPath);
        if (!programDataFolder.exists())
            programDataFolder.mkdir();
        File cacheFolder = new File(cacheFolderPath);
        if (!cacheFolder.exists())
            cacheFolder.mkdir();
    }

    public Song getSong(String songPath, String filename) {
        if (isSongCached(filename))
            return getCachedSong(songPath, filename);
        return createCacheForSong(songPath, filename);
    }

    private boolean isSongCached(String filename) {
        return new File(cacheFolderPath + "\\" + filename).exists();
    }

    private Song getCachedSong(String songPath, String filename) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(cacheFolderPath + "\\" + filename));
            String title = br.readLine();
            String artist = br.readLine();
            String album = br.readLine();
            String genre = br.readLine();
            String coverPath100 = br.readLine();
            String coverPath45 = br.readLine();
            long creationTime = Files.readAttributes(new File(songPath).toPath(), BasicFileAttributes.class).creationTime().toMillis();
            long lastModified = new File(songPath).lastModified();
            br.close();
            return new Song(title, artist, album, genre, filename, songPath, coverPath100, coverPath45, creationTime, lastModified);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Song createCacheForSong(String songPath, String filename) {
        try {
            Mp3File mp3file = new Mp3File(songPath);
            if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                String title = id3v2Tag.getTitle();
                String artist = id3v2Tag.getArtist();
                String album = id3v2Tag.getAlbum();
                String genre = id3v2Tag.getGenreDescription();

                title = title == null ? "No title" : title;
                artist = artist == null ? "No artist" : artist;
                album = album == null ? "No album" : album;
                genre = genre == null ? "No genre" : genre;

                String coverPath100 = cacheFolderPath + "\\" + URLEncoder.encode(album, "UTF-8") + "100.jpg";
                String coverPath45 = cacheFolderPath + "\\" + URLEncoder.encode(album, "UTF-8") + "45.jpg";

                File cover100 = new File(coverPath100);
                File cover45 = new File(coverPath45);

                ImageIcon cover = new ImageIcon(id3v2Tag.getAlbumImage());
                if (!cover100.exists())
                    ImageIO.write(resizeImageSmooth(cover.getImage(), 100, 100), "jpg", Files.newOutputStream(Paths.get(coverPath100)));
                if (!cover45.exists())
                    ImageIO.write(resizeImageSmooth(cover.getImage(), 45, 45), "jpg", Files.newOutputStream(Paths.get(coverPath45)));

                long creationTime = Files.readAttributes(new File(songPath).toPath(), BasicFileAttributes.class).creationTime().toMillis();
                long lastModified = new File(songPath).lastModified();

                File tagCache = new File(cacheFolderPath + "\\" + filename);
                BufferedWriter bw = new BufferedWriter(new FileWriter(tagCache));
                bw.write(title);
                bw.newLine();
                bw.write(artist);
                bw.newLine();
                bw.write(album);
                bw.newLine();
                bw.write(genre);
                bw.newLine();
                bw.write(coverPath100);
                bw.newLine();
                bw.write(coverPath45);
                bw.newLine();
                bw.close();

                return new Song(title, artist, album, genre, filename, songPath, coverPath100, coverPath45, creationTime, lastModified);
            }
        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private BufferedImage resizeImageSmooth(final Image image, int width, int height) {
        ImageIcon resized = new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.drawImage(resized.getImage(), 0, 0, width, height, null);
        graphics2D.dispose();
        return bufferedImage;
    }
}
