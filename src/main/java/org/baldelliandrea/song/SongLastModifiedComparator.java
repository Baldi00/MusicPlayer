package org.baldelliandrea.song;

import java.util.Comparator;

public class SongLastModifiedComparator implements Comparator<Song> {
    @Override
    public int compare(Song s1, Song s2) {
        return -1 * Long.compare(s1.getLastModified(), s2.getLastModified());
    }
}
