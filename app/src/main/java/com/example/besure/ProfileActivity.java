package com.example.besure;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private TextView RFID, usernameTextView, Name, amount, email, pnumber;
    private Button buttonRedirect;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Initialize views
        RFID = findViewById(R.id.RFID);
        usernameTextView = findViewById(R.id.username);
        Name = findViewById(R.id.Name);
        amount = findViewById(R.id.amount);
        email = findViewById(R.id.email);
        pnumber = findViewById(R.id.pnumber);
        buttonRedirect = findViewById(R.id.button);  // Add the button

        // Initialize database reference
        databaseReference = FirebaseDatabase.getInstance().getReference().child("RFIDUsersFirebase");

        // Get the username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("USERNAME_KEY", null);

        // Fetch user data if username is available
        if (username != null) {
            fetchUserData(username);
        } else {
            Log.e(TAG, "No username found in SharedPreferences");
        }

        // Set up button listener to navigate to loadingActivity
        buttonRedirect.setOnClickListener(v -> navigateToLoadingActivity());
    }

    private void fetchUserData(String username) {
        // Query Firebase database to fetch user data based on username
        Query query = databaseReference.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String rfidValue = snapshot.getKey(); // Get the RFID key (unique identifier)
                        String nameValue = snapshot.child("name").getValue(String.class);
                        Integer amountValue = snapshot.child("amount").getValue(Integer.class);
                        String emailValue = snapshot.child("email").getValue(String.class);
                        String pnumberValue = snapshot.child("phone_number").getValue(String.class);

                        // Set the TextViews with retrieved data
                        RFID.setText(rfidValue);
                        usernameTextView.setText(username); // Set the username
                        Name.setText(nameValue);
                        if (amountValue != null) {
                            amount.setText(String.valueOf(amountValue)); // Set the amount
                        } else {
                            amount.setText("0"); // Default to 0 if amount is null
                        }
                        email.setText(emailValue);
                        pnumber.setText(pnumberValue);

                        // Log rfid for debugging purposes
                        Log.d(TAG, "RFID Value: " + rfidValue);

                        // Break the loop after the first match
                        break;
                    }
                } else {
                    Log.e(TAG, "No data found for username: " + username);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void navigateToLoadingActivity() {
        // Get RFID value from the TextView (you can also use the rfidValue variable)
        String rfidValue = RFID.getText().toString();

        if (!rfidValue.isEmpty()) {
            // Pass the RFID value to loadingActivity via Intent
            Intent intent = new Intent(ProfileActivity.this, loadingActivity.class);
            intent.putExtra("rfid", rfidValue); // Pass RFID as an extra
            startActivity(intent); // Start the loadingActivity
        } else {
            Log.e(TAG, "RFID value is empty, cannot navigate to loadingActivity");
        }
    }
}
