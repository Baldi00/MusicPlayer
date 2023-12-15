package org.baldelliandrea.song;

public class Song implements Comparable {
    private final String title;
    private final String artist;
    private final String album;
    private final String filename;
    private final String path;
    private final String originalCoverPath;
    private final String coverPath100;
    private final String coverPath45;

    public Song(String title, String artist, String album, String filename, String path, String originalCoverPath, String coverPath100, String coverPath45) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.filename = filename;
        this.path = path;
        this.originalCoverPath = originalCoverPath;
        this.coverPath100 = coverPath100;
        this.coverPath45 = coverPath45;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getFilename() {
        return filename;
    }

    public String getPath() {
        return path;
    }

    public String getOriginalCoverPath() {
        return originalCoverPath;
    }

    public String getCoverPath100() {
        return coverPath100;
    }

    public String getCoverPath45() {
        return coverPath45;
    }

    @Override
    public int compareTo(Object o) {
        return title.compareTo(((Song) o).title);
    }
}
