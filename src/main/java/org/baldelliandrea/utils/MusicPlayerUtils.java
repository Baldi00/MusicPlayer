package org.baldelliandrea.utils;

import org.baldelliandrea.ui.MusicPlayerFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

public class MusicPlayerUtils {

    public static final String PROGRAM_DATA_FOLDER_PATH = System.getenv("APPDATA") + "\\BaldelliMusicPlayer";
    public static final String CACHE_FOLDER_PATH = System.getenv("APPDATA") + "\\BaldelliMusicPlayer\\cache";
    public static final String SONGS_PATHS_FILE = MusicPlayerUtils.PROGRAM_DATA_FOLDER_PATH + "\\songsPath.txt";

    public static ImageIcon getSpriteResource(String path) {
        try (InputStream stream = MusicPlayerFrame.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null)
                throw new MissingResourceException("Resource cannot be found", MusicPlayerFrame.class.getName(), path);
            BufferedImage bufferedImage = ImageIO.read(stream);
            return new ImageIcon(bufferedImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ImageIcon scaleIcon(ImageIcon icon, int size) {
        return new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

    public static List<String> getSongsPaths() {
        List<String> songsPaths = new ArrayList<>();
        File songsPathsFile = new File(MusicPlayerUtils.SONGS_PATHS_FILE);

        // First songs paths file creation
        if (!songsPathsFile.exists()) {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(songsPathsFile));
                bw.write(System.getenv("USERPROFILE") + "\\Music");
                bw.newLine();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Read file
        try {
            BufferedReader br = new BufferedReader(new FileReader(songsPathsFile));
            String line = br.readLine();
            while (line != null) {
                songsPaths.add(line);
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return songsPaths;
    }

    private MusicPlayerUtils() {
    }
}
