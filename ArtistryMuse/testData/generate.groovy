


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

/**
 * Created by dave on 1/8/2017.
 */
class Generate {

    private static final String usersDirectoryName = "users"
    private static final String projectsDirectoryName = "projects"
    private static final String jsonFilename = "testData.json"

    private static final int numberOfNewUsers = 100
    private static final int numberofProjectsPerUser = numberOfNewUsers * 0.15
    private static final int numberofFavoritesPerUser = numberOfNewUsers * 0.25
    private static final int numberofFollowingsPerUser = numberOfNewUsers
    private static final int numberofInspirations = numberOfNewUsers * 0.10

    private static final int startingAuthUid = 1000

    private static File usersDirectory
    private static File projectsDirectory
    private static File jsonFile
    private static Data jsonData

    private static JPEGTranscoder transcoder

    private static String baseSvg =
            "<svg height=\"HEIGHT\" viewBox=\"0 0 WIDTH HEIGHT\" width=\"WIDTH\" xmlns=\"http://www.w3.org/2000/svg\">" +
                    "<title>BaseTestDataIlustration</title>" +
                    "<text style=\"fill: #231f20; font-family: MyriadPro-Regular, Myriad Pro\" transform=\"translate(10 30)\">" +
                    "<tspan style=\"font-size: 26px\" x=\"0\" y=\"0\">FIRST</tspan>" +
                    "<tspan style=\"font-size: 16px\" x=\"0\" y=\"21\">SECOND</tspan>" +
                    "<tspan style=\"font-size: 12px\" x=\"0\" y=\"42\">THIRD</tspan>" +
                    "</text>" +
                    "</svg>"

    static void main(String[] args) {

        // Create a JPEG transcoder
        transcoder = new JPEGTranscoder()

        // Set the transcoding hints.
        transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(0.8))

        // Get the data directories
        usersDirectory = getDirectory(usersDirectoryName, true)
        projectsDirectory = getDirectory(projectsDirectoryName, true)

        // Get the JSON file that will be written to
        jsonFile = getFile(jsonFilename)

        // The JSON data to be written to the JSON file
        jsonData = new Data()

        // Loop over the number of new users and populate the JSON file along with creating images files
        for (int userIndex = 0; userIndex < numberOfNewUsers; userIndex++) {

            // Create new user object and pre-populate it
            def user = createUser(userIndex)

            // Loop over number of projects per user
            for (int projectIndex = 0; projectIndex < numberofProjectsPerUser; projectIndex++) {

                // Create new project object and pre-populate it
                def project = createProject(projectIndex, user.uid)

                // Loop over number of projects per user
                for (int inspirationIndex = 0; inspirationIndex < numberofInspirations; inspirationIndex++) {

                    def inspiration = createInspiration(inspirationIndex)

                    // Add the inspiration to the project
                    project.inspirations.put(inspiration.uid, inspiration)

                    // Create the image file associated with this inspiration
                    createInspirationJpg(projectsDirectory, project.uid, inspiration.imageUid, "Inspiration", inspiration.name, inspiration.uid)

                }

                // Add the project UID to list of user's projects
                // NOTE: assumption about the future needed an actual object behind this (just use a string for now)
                user.projects.put(project.uid, project.uid)

                // Add the project to the overall data
                jsonData.projects.put(project.uid, project)

                // Create the image file associated with this project
                createProjectJpg(projectsDirectory, project.uid, project.mainImageUid, "Project", project.name, project.uid)

            }

            // Add the user to the overall data
            jsonData.users.put(user.uid,user)

            // Create the image file associated with this user
            createHeaderJpg(usersDirectory, user.uid, user.headerImageUid, "Header", user.name, user.uid)
            createProfileJpg(usersDirectory, user.uid, user.profileImageUid, "Profile", user.name, user.uid)

        }

        // Loop over the number of new users and perform some "favoriting" and "following" of others and their work
        for (User userObject : jsonData.users.values()) {

            // Keep track of the users we are following
            def userMap = [:]
            def arrayUsers = jsonData.users.values().toArray()

            // Randomly follow other users
            for (int followingIndex = 0; followingIndex < numberofFollowingsPerUser; followingIndex++) {

                User userRandom = arrayUsers[new Random().nextInt(arrayUsers.size()-1)]

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
            def arrayProjects = jsonData.projects.values().toArray()

            // Randomly favorite other user's projects
            for (int favoriteIndex = 0; favoriteIndex < numberofFavoritesPerUser; favoriteIndex++) {

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
        def json = JsonOutput.toJson(jsonData)
        jsonFile.write(JsonOutput.prettyPrint(json))

    }

    static User createUser(int userIndex) {

        // Lorem library to generate text, names and such...
        Lorem lorem = LoremIpsum.getInstance()

        def result = new User()

        result.authUid = startingAuthUid++
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

    static Inspiration createInspiration(int inspirationIndex) {

        // Lorem library to generate text, names and such...
        Lorem lorem = LoremIpsum.getInstance()

        def result = new Inspiration()

        result.creationDate = new Date().getTime()
        result.description = lorem.getParagraphs(1,3)
        result.imageUid = UUID.randomUUID()
        result.lastUpdateDate = new Date().getTime()
        result.name = lorem.getTitle(1,10)
        result.uid = UUID.randomUUID()

        return result

    }

    static void createHeaderJpg(File baseDirectory, String directoryUid, String imageUid, String first, String second, String third) {
        createJpg(baseDirectory, directoryUid, imageUid, first, second, third, 300, 200)
    }

    static void createProfileJpg(File baseDirectory, String directoryUid, String imageUid, String first, String second, String third) {
        createJpg(baseDirectory, directoryUid, imageUid, first, second, third, 20, 20)
    }

    static void createProjectJpg(File baseDirectory, String directoryUid, String imageUid, String first, String second, String third) {
        createJpg(baseDirectory, directoryUid, imageUid, first, second, third, 300, 200)
    }

    static void createInspirationJpg(File baseDirectory, String directoryUid, String imageUid, String first, String second, String third) {
        createJpg(baseDirectory, directoryUid, imageUid, first, second, third, 300, 200)
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

}
