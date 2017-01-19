package com.concavenp.artistrymuse.movedata;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.json.JSONException;

/**
 * Created by dave on 1/12/2017.
 */
public class MoveData {

    public static void main(String[] args) {

        FirebaseOptions options = null;

        try {
            options = new FirebaseOptions.Builder()
                    .setServiceAccount(
                            new FileInputStream("artistrymuseudacity-firebase-adminsdk.json"))
                    .setDatabaseUrl("https://artistrymuseudacity.firebaseio.com")
                    .build();
        } catch (FileNotFoundException e) {
            System.out.println("MoveData caught exception: ");
            e.printStackTrace();
        } catch (JSONException e) {
            System.out.println("MoveData caught exception: ");
            e.printStackTrace();
        }

        FirebaseApp.initializeApp(options);






    }

}
