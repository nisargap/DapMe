package nisargap.dapme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.github.nkzawa.emitter.Emitter;

import org.json.JSONArray;

public class MembersViewActivity extends AppCompatActivity {

    FirebaseUserAuth mUserAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members_view);

        mUserAuth = new FirebaseUserAuth();
    }

    public void dapMeUp(View v) {

        Intent goToMap = new Intent(this, MapsActivity.class);
        startActivity(goToMap);

    }

    public void logUserOut(View v){


        mUserAuth.logOut();

        SocketUtility.getInstance().closeConnection();

        Intent loginActivity = new Intent(this, LoginActivity.class);

        startActivity(loginActivity);


    }

    @Override
    protected void onResume() {
        super.onResume();
        SocketUtility.getInstance().connect();
        SocketUtility.getInstance().listenOnUserData(new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                try {
                    JSONArray data = (JSONArray) args[0];

                    Log.d("ME", data.toString());

                } catch (NullPointerException e) {

                    Log.d("ME", e.getMessage());
                }

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        SocketUtility.getInstance().closeConnection();
        SocketUtility.getInstance().stopListenOnUserData();

    }



}
