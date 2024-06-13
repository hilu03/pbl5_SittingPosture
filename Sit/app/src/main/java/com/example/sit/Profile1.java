package com.example.sit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Profile1 extends BaseActivity {
    TextView txt_name, txt_username, txt_email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile1);

        txt_name = findViewById(R.id.txt_name);
        txt_email = findViewById(R.id.txt_email);
        HelperClass singleton = HelperClass.getInstance();
        String username = singleton.getUsername();
        String email = singleton.getEmail();
        String password = singleton.getPassword();
        String name = singleton.getName();

//        txt_username.setText(username);
        txt_email.setText(email);
        txt_name.setText(name);

        Back();
        changePass();
        logOut();
    }

    private void Back() {
        ConstraintLayout back = findViewById(R.id.btnBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profile1.this, Home1.class);
                startActivity(intent);
//                finish();
            }
        });
    }

    private void changePass() {
        RelativeLayout changePass = findViewById(R.id.change_pass);
        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profile1.this, ChangePass.class);
                startActivity(intent);
//                finish();
            }
        });
    }

    private void logOut() {
        RelativeLayout logOut = findViewById(R.id.log_out);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profile1.this, Login.class);
                startActivity(intent);
//                finish();
            }
        });
    }
}