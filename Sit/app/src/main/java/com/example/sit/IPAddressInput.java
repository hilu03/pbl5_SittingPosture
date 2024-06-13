package com.example.sit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class IPAddressInput extends AppCompatActivity {
    EditText ipAddress;
    Button ok;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ipaddress_input);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ipAddress = findViewById(R.id.edt_IP);
        ok = findViewById(R.id.btn_ok);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IPAddressSingleton singleton = IPAddressSingleton.getInstance();
                singleton.setIPAddress(ipAddress.getText().toString());

                Intent intent = new Intent(IPAddressInput.this, Login.class);
                startActivity(intent);
            }
        });
    }
}