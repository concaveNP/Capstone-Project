


package com.concavenp.artistrymuse.generatedata

import groovy.json.JsonOutput

import com.concavenp.artistrymuse.model.Data
import com.concavenp.artistrymuse.model.User
import com.concavenp.artistrymuse.model.Project
import com.concavenp.artistrymuse.model.Inspiration
import com.concavenp.artistrymuse.model.Favorite
import com.concavenp.artistrymuse.model.Following

import org.apache.batik.transcoder.image.JPEGTranscoder
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput

import com.thedeanda.lorem.Lorem
import com.thedeanda.lorem.LoremIpsum

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

/**
 * Created by dave on 1/8/2017.
 */
class Generate {

    private static final String usersDirectoryName = "users"
    private static final String projectsDirectoryName = "projects"
    private static final String jsonTestFilename = "testData.json"

    private static final int numberOfNewUsers = 30
    private static final int numberOfProjectsPerUser = numberOfNewUsers * 0.15
    private static final int numberOfFavoritesPerUser = numberOfNewUsers * 0.25
    private static final int numberOfFollowingsPerUser = numberOfNewUsers
    private static final int numberOfInspirations = numberOfNewUsers * 0.10

    private static File usersDirectory
    private static File projectsDirectory
    private static File jsonTestFile
    private static Data jsonTestData

    private static JPEGTranscoder transcoder

    // Flag to use the Flickr service to generate the images or not
    private static boolean useFlickr = true;

    // The key and secret that will be used to interact with the Flickr service
    private static String flickrApiKey;
    private static String flickrSecret;

    // The "page" (equivalent to web pagination) from which to group data together in API calls
    private static int flickrPage = 0;

    static private Flickr flickr;
    static private SearchParameters flickrSearchParameters;
    static private PhotoList<Photo> flickrPhotosList;
    static private Iterator flickrIterator;
    static private com.flickr4java.flickr.people.User flickrPhotoOwner;

    private static String baseSvg =
            "<svg height=\"HEIGHT\" viewBox=\"0 0 WIDTH HEIGHT\" width=\"WIDTH\" xmlns=\"http://www.w3.org/2000/svg\">" +
                    "<title>BaseTestDataIlustration</title>" +
                    "<text style=\"fill: #231f20; font-family: MyriadPro-Regular, Myriad Pro\" transform=\"translate(10 30)\">" +
                    "<tspan style=\"font-size: 26px\" x=\"0\" y=\"0\">FIRST</tspan>" +
                    "<tspan style=\"font-size: 16px\" x=\"0\" y=\"21\">SECOND</tspan>" +
                    "<tspan style=\"font-size: 12px\" x=\"0\" y=\"42\">THIRD</tspan>" +
                    "</text>" +
                    "</svg>"

    public static final String FLICKR_PROP_FILE = "FLICKR_KEYS_and_SECRETS.properties"
    public static final String FLICKR_KEY = "flickr_key"
    public static final String FLICKR_SECRET = "flickr_secret"
    public static final String FLICKR_TAGS = "sketch"
    public static final int FLICKR_ITEMS_PER_PAGE = 500

    static void main(String[] args) {

        // Get Flickr API properties
        if (useFlickr) {
            if (!getFlickrProperties()) {

                // Things have gone bad for interfacing with Flickr, BAIL from the app
                println("ERROR: unable to extract Flickr properties!")
                return;

            } else {

                // Create and set the Flickr interfacing object
                flickr = new Flickr(flickrApiKey, flickrSecret, new REST());
                flickrSearchParameters = new SearchParameters();
                flickrSearchParameters.setAccuracy(1);
                flickrSearchParameters.setTags(FLICKR_TAGS);

            }
        }

        // Create a JPEG transcoder
        transcoder = new JPEGTranscoder()

        // Set the transcoding hints.
        transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(0.8))

        // Get the data directories
        usersDirectory = getDirectory(usersDirectoryName, true)
        projectsDirectory = getDirectory(projectsDirectoryName, true)

        // Get the JSON file that will be written/created
        jsonTestFile = getFile(jsonTestFilename)

        // The JSON data to be written to the JSON file
        jsonTestData = new Data()

        // Get the next page of flickr data
        nextFlickrPage();

        // Loop over the number of new users and populate the JSON file along with creating images files
        for (int userIndex = 0; userIndex < numberOfNewUsers; userIndex++) {

            // Create new user object and pre-populate it
            def user = createUser(userIndex)

            // Loop over number of projects per user
            for (int projectIndex = 0; projectIndex < numberOfProjectsPerUser; projectIndex++) {

                // Create new project object and pre-populate it
                def project = createProject(projectIndex, user.uid)

                // Loop over number of projects per user
                for (int inspirationIndex = 0; inspirationIndex < numberOfInspirations; inspirationIndex++) {

                    def inspiration = createInspiration(project.uid)

                    // Add the inspiration to the project
                    project.inspirations.put(inspiration.uid, inspiration)

                    // Create the image file associated with this inspiration
                    createInspirationJpg(projectsDirectory, project.uid, inspiration.imageUid, "Inspiration", inspiration.name, inspiration.uid)

                }

                // Add the project UID to list of user's projects
                // NOTE: assumption about the future needed an actual object behind this (just use a string for now)
                user.projects.put(project.uid, project.uid)

                // Add the project to the overall data
                jsonTestData.projects.put(project.uid, project)

                // Create the image file associated with this project
                createProjectJpg(projectsDirectory, project.uid, project.mainImageUid, "Project", project.name, project.uid)

            }

            // Add the user to the overall data
            jsonTestData.users.put(user.uid,user)

            // Add the user to the auth data
            jsonTestData.auth.put(user.authUid, user.uid)

            // Create the image file associated with this user
            createHeaderJpg(usersDirectory, user.uid, user.headerImageUid, "Header", user.name, user.uid)
            createProfileJpg(usersDirectory, user.uid, user.profileImageUid, "Profile", user.name, user.uid)

        }

        // Loop over the number of new users and perform some "favoriting" and "following" of others and their work
        for (com.concavenp.artistrymuse.model.User userObject : jsonTestData.users.values()) {

            // Keep track of the users we are following
            def userMap = [:]
            def arrayUsers = jsonTestData.users.values().toArray()

            // Randomly follow other users
            for (int followingIndex = 0; followingIndex < numberOfFollowingsPerUser; followingIndex++) {

                com.concavenp.artistrymuse.model.User userRandom = arrayUsers[new Random().nextInt(arrayUsers.size()-1)]

                // Check it is not the same user
                if (userRandom.uid == userObject.uid) {
                    continue
                }

                // Check if we already have this user
                if (userMap.containsKey(userRandom.uid)) {
                    continue
                }

                Following following = new Following()
                following.uid = userRandom.uid
                following.lastUpdatedDate = new Date().getTime()

                // Add this user as being followed
                userObject.following.put(following.uid, following)

                // Increment the followed count of the user being followed
                userRandom.followedCount++

                // Keep track of the users being followed
                userMap[userRandom.uid] = true
            }

            // Keep track of the projects we have favorited
            def projectMap = [:]
            def arrayProjects = jsonTestData.projects.values().toArray()

            // Randomly favorite other user's projects
            for (int favoriteIndex = 0; favoriteIndex < numberOfFavoritesPerUser; favoriteIndex++) {

                Project projectRandom = arrayProjects[new Random().nextInt(arrayProjects.size()-1)]

                // Check that the project is not one of our own
                boolean found = false
                for (String uid : userObject.projects) {
                    if (uid == projectRandom.uid) {
                        found = true
                        break
                    }
                }
                if (found) {
                    continue
                }

                // Check if we already have marked this project as a favorite of ours
                if (projectMap.containsKey(projectRandom.uid)) {
                    continue
                }

                // Make sure the project is published before continuing
                if (!projectRandom.published) {
                    continue
                }

                Favorite favorite = new Favorite()
                favorite.uid = projectRandom.uid
                favorite.rating = new Random().nextDouble()*10.0
                favorite.favoritedDate = new Date().getTime()

                // Add this project as a favorite
                userObject.favorites.put(favorite.uid, favorite)

                // Increment the favorited count of this project
                projectRandom.favorited++

                // View the project a random number of times
                projectRandom.views += new Random().nextInt(10)

                // Rate the project
                projectRandom.ratingsCount++
                projectRandom.rating = (projectRandom.rating + (new Random().nextDouble()*10.0)) / projectRandom.ratingsCount

                // Keep track of the project being favorited
                projectMap[projectRandom.uid] = true

            }

        }

        // Convert the generated Data (user and project objects) to JSON
        def json = JsonOutput.toJson(jsonTestData)
        jsonTestFile.write(JsonOutput.prettyPrint(json))

    }

    static com.concavenp.artistrymuse.model.User createUser(int userIndex) {

        // Lorem library to generate text, names and such...
        Lorem lorem = LoremIpsum.getInstance()

        def result = new com.concavenp.artistrymuse.model.User()

        result.authUid = UUID.randomUUID()
        result.creationDate = new Date().getTime()
        result.description = lorem.getParagraphs(2,6)
        result.favorites.clear()
        result.followedCount = 0
        result.following.clear()
        result.headerImageUid = UUID.randomUUID()
        result.lastUpdatedDate = new Date().getTime()
        String firstName = lorem.getFirstName()
        String lastName = lorem.getLastName()
        result.name = firstName + " " + lastName
        result.profileImageUid = UUID.randomUUID()
        result.summary = lorem.getTitle(1,10)
        result.uid = UUID.randomUUID()
        result.username = firstName.substring(0,1).toLowerCase() + lastName.toLowerCase()

        return result

    }

    static Project createProject(int projectIndex, String uid) {

        // Lorem library to generate text, names and such...
        Lorem lorem = LoremIpsum.getInstance()

        def result = new Project()

        result.creationDate = new Date().getTime()
        result.description = lorem.getParagraphs(1,3)
        result.favorited = 0
        result.inspirations.clear()
        result.lastUpdateDate = new Date().getTime()
        result.mainImageUid = UUID.randomUUID()
        result.name = lorem.getTitle(1,10)
        result.ownerUid = uid
        result.published = new Random().nextBoolean()
        result.publishedDate = new Date().getTime()
        result.rating = 0.0
        result.ratingsCount = 0
        result.uid = UUID.randomUUID()
        result.views = 0

        return result

    }

    static Inspiration createInspiration(String projectUid) {

        // Lorem library to generate text, names and such...
        Lorem lorem = LoremIpsum.getInstance()

        def result = new Inspiration()

        result.creationDate = new Date().getTime()
        result.description = lorem.getParagraphs(1,3)
        result.imageUid = UUID.randomUUID()
        result.lastUpdateDate = new Date().getTime()
        result.name = lorem.getTitle(1,10)
        result.uid = UUID.randomUUID()
        result.projectUid = projectUid;

        return result

    }

    static void createHeaderJpg(File baseDirectory, String directoryUid, String imageUid, String first, String second, String third) {
        if (useFlickr) {
            createJpg(baseDirectory, directoryUid, imageUid);
        } else {
            createJpg(baseDirectory, directoryUid, imageUid, first, second, third, 300, 200)
        }
    }

    static void createProfileJpg(File baseDirectory, String directoryUid, String imageUid, String first, String second, String third) {
        if (useFlickr) {
            createJpg(baseDirectory, directoryUid, imageUid);
        } else {
            createJpg(baseDirectory, directoryUid, imageUid, first, second, third, 20, 20)
        }
    }

    static void createProjectJpg(File baseDirectory, String directoryUid, String imageUid, String first, String second, String third) {
        if (useFlickr) {
            createJpg(baseDirectory, directoryUid, imageUid);
        } else {
            createJpg(baseDirectory, directoryUid, imageUid, first, second, third, 300, 200)
        }
    }

    static void createInspirationJpg(File baseDirectory, String directoryUid, String imageUid, String first, String second, String third) {
        if (useFlickr) {
            createJpg(baseDirectory, directoryUid, imageUid);
        } else {
            createJpg(baseDirectory, directoryUid, imageUid, first, second, third, 300, 200)
        }
    }

    // convert filename to clean filename
    private static String convertToFileSystemChar(String name) {
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

    private static boolean saveImage(File baseDirectory, String directoryUid, String imageUid, Photo photo) {

        String cleanTitle = convertToFileSystemChar(photo.getTitle());

        // Only save small named files (less than 100 characters)
        if (cleanTitle.length() > 100) {
            return false;  // too long
        }

        File directory = getDirectory(baseDirectory.getName() + File.separator + directoryUid);
        File outputFile = new File(baseDirectory.getName() + File.separator + directory.getName() + File.separator + imageUid + "." + photo.getOriginalFormat());

        try {

            Photo newPhoto = flickr.getPhotosInterface().getInfo(photo.getId(), null);

            if (newPhoto.getOriginalSecret().isEmpty()) {
                ImageIO.write(photo.getLargeImage(), photo.getOriginalFormat(), outputFile);
                System.out.println(photo.getTitle() + "\t" + photo.getLargeUrl() + " was written to " + outputFile.getName());
            } else {
                photo.setOriginalSecret(newPhoto.getOriginalSecret());
                ImageIO.write(photo.getOriginalImage(), photo.getOriginalFormat(), outputFile);
                System.out.println(photo.getTitle() + "\t" + photo.getOriginalUrl() + " was written to " + outputFile.getName());
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

    static void createJpg(File baseDirectory, String directoryUid, String imageUid) {

        boolean complete = false;

        while (!complete) {

            if (flickrPhotosList.isEmpty() || !flickrIterator.hasNext()) {

                println("-------------------------");
                println("Unable to get a picture, the list is empty.");
                println("-------------------------");
                println("Moving to next page");
                println("-------------------------");

                // Get the next page of flickr data
                nextFlickrPage();

                //System.exit(1);

            } else {

                Photo photo = ++flickrIterator;
                com.flickr4java.flickr.people.User currentOwner = photo.getOwner();

                // I only want one work per user
                if ((flickrPhotoOwner == null) || (!flickrPhotoOwner.getId().equals(currentOwner.getId()))) {
                    flickrPhotoOwner = currentOwner;
                    if (saveImage(baseDirectory, directoryUid, imageUid, photo)) {
                        complete = true;
                    }
                } else {
                    // skip, we want different peoples work
                }

            }

        }

    }
    static void createJpg(File baseDirectory, String directoryUid, String imageUid, String first, String second, String third, int width, int height) {

        File directory = getDirectory(baseDirectory.getName() + File.separator + directoryUid)
        String svgString = baseSvg

        svgString = svgString.replaceAll("WIDTH", width.toString())
        svgString = svgString.replaceAll("HEIGHT", height.toString())
        svgString = svgString.replaceAll("FIRST", first.toString())
        svgString = svgString.replaceAll("SECOND", second.toString())
        svgString = svgString.replaceAll("THIRD", third.toString())

        // Create the transcoder input.
        TranscoderInput input = new TranscoderInput(new StringReader(svgString))

        // Create the transcoder output.
        OutputStream ostream = new FileOutputStream(baseDirectory.getName() + File.separator + directory.getName() + File.separator + imageUid + ".jpg")
        TranscoderOutput output = new TranscoderOutput(ostream)

        // Save the image.
        transcoder.transcode(input, output)

        // Flush and close the stream.
        ostream.flush()
        ostream.close()

    }

    static File getDirectory(String dirName) {

        return getDirectory(dirName, false)

    }

    static File getDirectory(String dirName, boolean delete) {

        def result = new File(dirName)

        if (delete) {
            result.delete()
            sleep(3000)
            result = new File(dirName)
        }

        // If it doesn't exist
        if( !result.exists() ) {

            // Warn the user
            println("WARNING: the \"${dirName}\" directory did not appear to exist and will be created")

            // Create the directory
            boolean success = result.mkdirs()

            if (success) {

                // Inform the user
                println("INFO: the \"${dirName}\" directory was created")

            } else {

                // Tell the user about the error
                println("ERROR: the \"${dirName}\" directory could NOT be created!")

            }

        } else {

            // Inform the user
            println("INFO: found the \"${dirName}\" directory that will be used")

        }

        return result

    }

    static File getFile(String fileName) {

        File result = new File(fileName)

        // If it doesn't exist
        if( !result.exists() ) {

            // Warn the user
            println("WARNING: the \"${fileName}\" file did not appear to exist and will be created")

            // Create the file
            boolean success = result.createNewFile()

            if (success) {

                // Inform the user
                println("INFO: the \"${fileName}\" file was created")

            } else {

                // Tell the user about the error
                println("ERROR: the \"${fileName}\" file could NOT be created!")

            }

        } else {

            // Inform the user
            println("INFO: found the \"${fileName}\" file that will be used")

        }

        return result

    }

    private static boolean getFlickrProperties() {

        // Resulting status defaults to fail (aka false)
        boolean result = false;

        // Obtain the properties
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(com.concavenp.artistrymuse.generatedata.Generate.FLICKR_PROP_FILE);

            // load a properties file
            prop.load(input);

            // get the needed property values
            flickrApiKey = prop.getProperty(com.concavenp.artistrymuse.generatedata.Generate.FLICKR_KEY);
            flickrSecret = prop.getProperty(com.concavenp.artistrymuse.generatedata.Generate.FLICKR_SECRET);

        } catch (IOException ex) {

            ex.printStackTrace();

        } finally {
            if (input != null) {
                try {

                    // Close the properties file
                    input.close();

                    // Success
                    result = true;

                } catch (IOException e) {

                    e.printStackTrace();

                }
            }
        }

        return result;

    }

    private static void nextFlickrPage() {

        // Grab a new page full of pictures from Flickr and get an iterator for it
        flickrPhotosList = flickr.getPhotosInterface().search(flickrSearchParameters, FLICKR_ITEMS_PER_PAGE, flickrPage);
        flickrIterator = flickrPhotosList.iterator();
        flickrPhotoOwner = null;

        // Increment page
        flickrPage++;

    }



}
