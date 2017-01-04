

import com.concavenp.artistrymuse.model.User
import java.util.UUID

class Generate {

    private static final String usersDirectoryName = "users";
    private static final String projectsDirectoryName = "projects";
    private static final String jsonFilename = "testData.json";

    private static final int numberOfNewUsers = 100;
    private static final int numberofProjectsPerUser = 15;
    private static final int numberofFavoritesPerUser = 25;
    private static final int numberofFollowingsPerUser = 15;
    
    private static final int startingAuthUid = 1000

    static void main(String[] args) {

        // Get the data directories
        def usersDirectory = getDirectory(usersDirectoryName)
        def projectsDirectory = getDirectory(projectsDirectoryName)

        // Get the JSON file
        def jsonFile = getFile(jsonFilename)

        def jsonData = new Data()

        // Loop over the number of new users
        for (int userIndex : numberOfNewUsers) {

            // Create new user object and pre-populate it
            def user = createUser(userIndex);

            // TODO: checking
            print user

            // Loop over number of projects per user
            for (int projectIndex : numberofProjectsPerUser) {

                // Create new project object and pre-populate it
                def project = createProject(projectIndex);

                // TODO: checking
                print project

                // Add the project UID to list of user's projects
                user.artProjects.add(project.uid)

                // Add the project to the overall data
                jsonData.projects.add(project)

            }

            // Add the user to the overall data
            jsonData.users.add(user)

        }

        // Loop over the number of new users
        for (int userIndex : numberOfNewUsers) {

            // TODO: Randomly follow other users

            // TODO: Randomly favorite other user's projects

        }

    }

    static User createUser(int userIndex) {

        def result = new User()

        result.uid = UUID.randomUUID()
        result.authUid = startingAuthUid++
        result.creationDate = new Date().getTime()
        result.description = "some description"
        result.headerImageUid = generateUserImage(result.uid)
        result.lastUpdatedDate = new Date().getTime()
        result.name = "User" + userIndex
        result.profileImageUid = generateUserImage(result.uid)
        result.summary = "some summary"
        result.username = "Username" + userIndex

        return result
        
    }

    static Project createProject(int projectIndex) {

        def result = new Project()

        // TODO: fill out

        return result

    }

    static String generateUserImage(String uid) {

        String result

        // Generate new UUID
        def uuid = new UUID.randomUUID()
        result = uuid.toString()

        // TODO: Copy the base image into the users directory

        // TODO: rename the image to the UUID name

        return result

    }


    static File getDirectory(String dirName) {

        def result = new File(dirName)

        // If it doesn't exist
        if( !result.exists() ) {

            // Warn the user
            println("WARNING: the \"${dirName}\" did not appear to exist and will be created")

            // Create the directory
            boolean success = result.mkdirs()

            if (success) {

                // Inform the user
                println("INFO: the \"${dirName}\" was created")

            } else {

                // Tell the user about the error
                println("ERROR: the \"${dirName}\" could NOT be created!")

            }

        } else {

            // Inform the user
            println("INFO: found the \"${dirName}\" directory that will be used")

        }

    }

    static File getFile(String fileName) {

        def result = new File(fileName)

        // If it doesn't exist
        if( !result.exists() ) {

            // Warn the user
            println("WARNING: the \"${fileName}\" did not appear to exist and will be created")

            // Create the file
            boolean success = result.createNewFile()

            if (success) {

                // Inform the user
                println("INFO: the \"${fileName}\" was created")

            } else {

                // Tell the user about the error
                println("ERROR: the \"${fileName}\" could NOT be created!")

            }

        } else {

            // Inform the user
            println("INFO: found the \"${fileName}\" file that will be used")

        }

    }

}