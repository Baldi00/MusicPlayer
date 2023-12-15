package org.baldelliandrea.song;

import java.util.Comparator;

public class SongArtistComparator implements Comparator<Song> {
    @Override
    public int compare(Song s1, Song s2) {
        return s1.getArtist().compareTo(s2.getArtist());
    }
}
