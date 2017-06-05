package com.company;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.SearchParameters;
import com.flickr4java.flickr.people.User;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 *  - Java 7 is needed
 *  - insert your api- and secretkey
 *
 *  start main with wanted tags as parameter, for example: FlickrCrawler.main(Sunset) and all pics will be saved in original size or large to pics\sunset\...
 */
public class FlickrCrawler {

    private static String path = "";
    private static Preferences userPrefs = Preferences.userNodeForPackage(FlickrCrawler.class);

    // convert filename to clean filename
    public static String convertToFileSystemChar(String name) {
        String erg = "";
        Matcher m = Pattern.compile("[a-z0-9 _#&@\\[\\(\\)\\]\\-\\.]", Pattern.CASE_INSENSITIVE).matcher(name);
        while (m.find()) {
            erg += name.substring(m.start(), m.end());
        }
        if (erg.length() > 200) {
            erg = erg.substring(0, 200);
            System.out.println("cut filename: " + erg);
        }
        return erg;
    }

    public static boolean saveImage(Flickr f, Photo p) {

        String cleanTitle = convertToFileSystemChar(p.getTitle());

        // Only save small named files
        if (cleanTitle.length() > 100) {
            return false;  // too long
        }

        File orgFile = new File(path + File.separator + cleanTitle + "_" + p.getId() + "_o." + p.getOriginalFormat());
        File largeFile = new File(path + File.separator + cleanTitle + "_" + p.getId() + "_b." + p.getOriginalFormat());

        if (orgFile.exists() || largeFile.exists()) {
            System.out.println(p.getTitle() + "\t" + p.getLargeUrl() + " skipped!");
            return false;
        }

        try {
            Photo nfo = f.getPhotosInterface().getInfo(p.getId(), null);
            if (nfo.getOriginalSecret().isEmpty()) {
                ImageIO.write(p.getLargeImage(), p.getOriginalFormat(), largeFile);
                System.out.println(p.getTitle() + "\t" + p.getLargeUrl() + " was written to " + largeFile.getName());
            } else {
                p.setOriginalSecret(nfo.getOriginalSecret());
                ImageIO.write(p.getOriginalImage(), p.getOriginalFormat(), orgFile);
                System.out.println(p.getTitle() + "\t" + p.getOriginalUrl() + " was written to " + orgFile.getName());
            }
        } catch (FlickrException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Parameter are needed as for searching. Example: FlickrCrawler.java sunset");
            return;
        }

        // The key and secret that will be used to interact with the Flickr service
        String apikey;
        String secret;

        // Obtain the properties
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream("FLICKR_KEYS_and_SECRETS.properties");

            // load a properties file
            prop.load(input);

            // get the needed property values
            apikey = prop.getProperty("flickr_key");
            secret = prop.getProperty("flickr_secret");

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        Flickr flickr = new Flickr(apikey, secret, new REST());
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setAccuracy(1);

        StringBuilder tagsBuilder = new StringBuilder();
        for (String tmp : args) {
            tagsBuilder.append(" " + tmp);
        }
        path = "pics" + File.separator + tagsBuilder.toString().substring(1);

        new File(path).mkdirs();
        searchParameters.setTags(args);

        for (int i = userPrefs.getInt(path, 0); true; i++) {
            userPrefs.putInt( path, i );
            System.out.println("\tcurrent page: " + userPrefs.getInt(path, 0));
            try {
                PhotoList<Photo> list = flickr.getPhotosInterface().search(searchParameters, 500, i);
                if (list.isEmpty())
                    break;
                    
                Iterator itr = list.iterator();
                User owner;
                while (itr.hasNext()) {
                    Photo photo = itr.next();
                    User currentOwner = photo.getOwner();

                    // I only want one work per user
                    if (owner == null) {
                        owner = currentOwner;
                        saveImage(flickr, photo);
                    } else if (!owner.getId().equals(currentOwner.getId())) {
                        owner = currentOwner;
                        saveImage(flickr, photo);
                    } else {
                        // skip, we want different peoples work
                    }

                }
            } catch (FlickrException e) {
                e.printStackTrace();
            }
        }
    }
}