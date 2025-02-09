package com.example.besure;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class statusActivity extends AppCompatActivity {
    private TextView textViewLocationlat, textViewTimer, textViewonoff, textViewLocationlong;
    private Handler handler;
    private Runnable runnable;
    private DatabaseReference databaseReference;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_status);

        textViewLocationlat = findViewById(R.id.locationlat);
        textViewLocationlong = findViewById(R.id.locationlong);
        textViewTimer = findViewById(R.id.timer);
        textViewonoff = findViewById(R.id.onoff);
        TextView textView3 = findViewById(R.id.textView3);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("USERNAME_KEY", "Default Username");
        textView3.setText(username);

        databaseReference = FirebaseDatabase.getInstance().getReference("SCOOTERSTATUS");

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                fetchAndUpdateData();
                handler.postDelayed(this, 1000); // Re-run the Runnable every 1000 milliseconds (1 second)
            }
        };

        // Start the periodic updates
        handler.post(runnable);
        // Find the button and set its onClick listener
        Button nextActivityButton = findViewById(R.id.timer);
        nextActivityButton.setOnClickListener(view -> {
            Intent intent = new Intent(statusActivity.this, LocationActivity.class);
            startActivity(intent);
        });

    }

    private void fetchAndUpdateData() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean userFound = false;
                String user = dataSnapshot.child("USER").getValue(String.class);
                if (username.equals(user)) {
                    String onoff = dataSnapshot.child("STATUS").getValue(String.class);
                    String locationlat = dataSnapshot.child("LOCATION").child("latitude").getValue(String.class);
                    String locationlong = dataSnapshot.child("LOCATION").child("longitude").getValue(String.class);

                    textViewonoff.setText(onoff);
                    textViewLocationlong.setText(locationlat);
                    textViewLocationlat.setText(locationlong);
                    userFound = true;
                }

                if (!userFound) {
                    textViewonoff.setText("-");
                    textViewLocationlong.setText("-");
                    textViewLocationlat.setText("-");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                textViewonoff.setText("Error: " + databaseError.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove any pending posts of the Runnable when the activity is destroyed
        handler.removeCallbacks(runnable);
    }
}
