package nisargap.dapme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MembersViewActivity extends AppCompatActivity {

    FirebaseUserAuth mUserAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members_view);

        mUserAuth = new FirebaseUserAuth();
    }

    public void logUserOut(View v){


        mUserAuth.logOut();

        Intent loginActivity = new Intent(this, LoginActivity.class);

        startActivity(loginActivity);


    }
}
