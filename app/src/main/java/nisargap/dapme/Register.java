package nisargap.dapme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Map;

public class Register extends AppCompatActivity {

    FirebaseUserAuth mUserAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mUserAuth = new FirebaseUserAuth();
    }
    public void goToLoginView(View v){

        Intent goToLogin = new Intent(Register.this, LoginActivity.class);
        startActivity(goToLogin);

    }

    public void createUser(View v){

        EditText emailEditText = (EditText)findViewById(R.id.emailField);
        EditText passwordEditText = (EditText) findViewById(R.id.passwordField);

        mUserAuth.createNewUser(emailEditText.getText().toString(), passwordEditText.getText().toString(), new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> stringObjectMap) {

                Intent goToLogin = new Intent(Register.this, LoginActivity.class);
                startActivity(goToLogin);
            }

            @Override
            public void onError(FirebaseError firebaseError) {

            }
        });

    }
}
