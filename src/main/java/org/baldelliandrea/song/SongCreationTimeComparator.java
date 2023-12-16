package org.baldelliandrea.song;

import java.util.Comparator;

public class SongCreationTimeComparator implements Comparator<Song> {
    @Override
    public int compare(Song s1, Song s2) {
        return -1 * Long.compare(s1.getCreationTime(), s2.getCreationTime());
    }
}
