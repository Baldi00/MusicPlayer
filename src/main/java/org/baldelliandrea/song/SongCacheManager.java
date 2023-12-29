package org.baldelliandrea.song;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.baldelliandrea.utils.MusicPlayerUtils;

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

    public SongCacheManager() {
        File programDataFolder = new File(MusicPlayerUtils.PROGRAM_DATA_FOLDER_PATH);
        if (!programDataFolder.exists())
            programDataFolder.mkdir();
        File cacheFolder = new File(MusicPlayerUtils.CACHE_FOLDER_PATH);
        if (!cacheFolder.exists())
            cacheFolder.mkdir();
    }

    public Song getSong(String songPath, String filename) {
        if (isSongCached(filename))
            return getCachedSong(songPath, filename);
        return createCacheForSong(songPath, filename);
    }

    private boolean isSongCached(String filename) {
        return new File(MusicPlayerUtils.CACHE_FOLDER_PATH + "\\" + filename).exists();
    }

    private Song getCachedSong(String songPath, String filename) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(MusicPlayerUtils.CACHE_FOLDER_PATH + "\\" + filename));
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

        // Initialize default values
        String title = filename.substring(0, filename.lastIndexOf("."));
        String artist = "No artist";
        String album = "No album";
        String genre = "No genre";
        ImageIcon cover = new ImageIcon(MusicPlayerUtils.getSpriteResource("controls/play-circle.png").getImage());
        String coverPath100 = MusicPlayerUtils.CACHE_FOLDER_PATH + "\\No%20album" + "100.jpg";
        String coverPath45 = MusicPlayerUtils.CACHE_FOLDER_PATH + "\\No%20album" + "45.jpg";
        long creationTime = 0;
        long lastModified = 0;

        // Read ID3 Tag and metadata if present
        try {
            Mp3File mp3file = new Mp3File(songPath);
            if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                title = id3v2Tag.getTitle() != null ? id3v2Tag.getTitle() : title;
                artist = id3v2Tag.getArtist() != null ? id3v2Tag.getArtist() : artist;
                album = id3v2Tag.getAlbum() != null ? id3v2Tag.getAlbum() : album;
                genre = id3v2Tag.getGenreDescription() != null ? id3v2Tag.getGenreDescription() : genre;

                coverPath100 = MusicPlayerUtils.CACHE_FOLDER_PATH + "\\" + URLEncoder.encode(album, "UTF-8") + "100.jpg";
                coverPath45 = MusicPlayerUtils.CACHE_FOLDER_PATH + "\\" + URLEncoder.encode(album, "UTF-8") + "45.jpg";

                cover = new ImageIcon(id3v2Tag.getAlbumImage());
            }

            creationTime = Files.readAttributes(new File(songPath).toPath(), BasicFileAttributes.class).creationTime().toMillis();
            lastModified = new File(songPath).lastModified();
        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            e.printStackTrace();
        }

        // Write cache file
        try {
            File cover100 = new File(coverPath100);
            File cover45 = new File(coverPath45);

            if (!cover100.exists())
                ImageIO.write(resizeImageSmooth(cover.getImage(), 100, 100), "jpg", Files.newOutputStream(Paths.get(coverPath100)));
            if (!cover45.exists())
                ImageIO.write(resizeImageSmooth(cover.getImage(), 45, 45), "jpg", Files.newOutputStream(Paths.get(coverPath45)));

            File tagCache = new File(MusicPlayerUtils.CACHE_FOLDER_PATH + "\\" + filename);
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
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Song(title, artist, album, genre, filename, songPath, coverPath100, coverPath45, creationTime, lastModified);
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
