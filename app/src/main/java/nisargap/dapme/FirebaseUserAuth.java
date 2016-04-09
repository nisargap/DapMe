package nisargap.dapme;

import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

/**
 * Created by nisargap on 4/9/16.
 */
public class FirebaseUserAuth {

    Firebase ref;

    FirebaseUserAuth(){

        // create the Firebase reference
        ref = new Firebase("https://dapmeusers.firebaseio.com");

    }

    public void signUserIn(String email, String password, Firebase.AuthResultHandler authHandler) {

        // Do the user login
        ref.authWithPassword(email, password, authHandler);

    }

    public void createNewUser(String email, String password, Firebase.ValueResultHandler<java.util.Map<String, Object>> valueHandler) {

        // Do the user registration
        ref.createUser(email, password, valueHandler);

    }

    public void logOut(){

        ref.unauth();
    }

    public boolean checkIfLoggedIn(){

        AuthData authData = ref.getAuth();

        Log.d("nisarga", authData + "");

        return authData != null;

    }

    public String getUuid(){

        if(checkIfLoggedIn()) {

            return ref.getAuth().getUid();
        } else {

            return null;
        }

    }

}
