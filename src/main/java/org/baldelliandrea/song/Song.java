package org.baldelliandrea.song;

public class Song implements Comparable {
    private final String title;
    private final String artist;
    private final String album;
    private final String genre;
    private final String filename;
    private final String path;
    private final String coverPath100;
    private final String coverPath45;
    private final long creationTime;
    private final long lastModified;

    public Song(String title, String artist, String album, String genre, String filename, String path, String coverPath100, String coverPath45, long creationTime, long lastModified) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.filename = filename;
        this.path = path;
        this.coverPath100 = coverPath100;
        this.coverPath45 = coverPath45;
        this.creationTime = creationTime;
        this.lastModified = lastModified;
    }

    public boolean contains(String string) {
        return titleContains(string) || artistContains(string) || albumContains(string);
    }

    public boolean titleContains(String string) {
        return title.toLowerCase().contains(string);
    }

    public boolean artistContains(String string) {
        return artist.toLowerCase().contains(string);
    }

    public boolean albumContains(String string) {
        return album.toLowerCase().contains(string);
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

    public String getGenre() {
        return genre;
    }

    public String getFilename() {
        return filename;
    }

    public String getPath() {
        return path;
    }

    public String getCoverPath100() {
        return coverPath100;
    }

    public String getCoverPath45() {
        return coverPath45;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getLastModified() {
        return lastModified;
    }

    @Override
    public int compareTo(Object o) {
        if (title.startsWith("[") && !((Song) o).title.startsWith("["))
            return 1;
        if (!title.startsWith("[") && ((Song) o).title.startsWith("["))
            return -1;

        int titleCompare = title.toLowerCase().compareTo(((Song) o).title.toLowerCase());
        if (titleCompare != 0)
            return titleCompare;

        int artistCompare = artist.toLowerCase().compareTo(((Song) o).artist.toLowerCase());
        if (artistCompare != 0)
            return artistCompare;

        return album.toLowerCase().compareTo(((Song) o).album.toLowerCase());
    }

    @Override
    public String toString() {
        return "Song{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", genre='" + genre + '\'' +
                ", filename='" + filename + '\'' +
                ", path='" + path + '\'' +
                ", coverPath100='" + coverPath100 + '\'' +
                ", coverPath45='" + coverPath45 + '\'' +
                ", creationTime='" + creationTime + '\'' +
                ", lastModified='" + lastModified + '\'' +
                '}';
    }
}
