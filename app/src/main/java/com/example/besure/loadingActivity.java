package com.example.besure;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class loadingActivity extends AppCompatActivity implements View.OnClickListener {
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference, paysureRef;
    MaterialButton button1, button2, button3, button4, button5, button6, button7, button8, button9, button0, buttonback;
    TextView inputloading;
    Button buttonLogin;
    String input, rfid;

    @Override
    public void onClick(View v) {
        MaterialButton button = (MaterialButton) v;
        String buttonText = button.getText().toString();
        String datatoinput = inputloading.getText().toString();

        // Clear input if 'B' is pressed
        if (buttonText.equals("B")) {
            inputloading.setText("");
        } else {
            datatoinput = datatoinput + buttonText;
            inputloading.setText(datatoinput);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading);

        inputloading = findViewById(R.id.inputloading);
        buttonLogin = findViewById(R.id.buttonLogin);

        assignId(button0, R.id.button0);
        assignId(button1, R.id.button1);
        assignId(button2, R.id.button2);
        assignId(button3, R.id.button3);
        assignId(button4, R.id.button4);
        assignId(button5, R.id.button5);
        assignId(button6, R.id.button6);
        assignId(button7, R.id.button7);
        assignId(button8, R.id.button8);
        assignId(button9, R.id.button9);
        assignId(buttonback, R.id.buttonback);

        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference("RFIDUsersFirebase");
        paysureRef = firebaseDatabase.getReference("paysure");

        // Retrieve the RFID from ProfileActivity
        rfid = getIntent().getStringExtra("rfid");

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserAmount();
            }
        });
    }

    private void updateUserAmount() {
        final String inputAmountStr = inputloading.getText().toString().trim();

        if (inputAmountStr.isEmpty()) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        final int inputAmount = Integer.parseInt(inputAmountStr);

        // Get the current amount for the user from RFIDUsersFirebase
        reference.child(rfid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int currentAmount = snapshot.child("amount").getValue(Integer.class);

                    // Add inputAmount to currentAmount in RFIDUsersFirebase
                    int newRFIDAmount = currentAmount + inputAmount;

                    // Update the amount in RFIDUsersFirebase
                    reference.child(rfid).child("amount").setValue(newRFIDAmount);

                    // Now fetch the current balance in Paysure for this user
                    paysureRef.child(rfid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot paysureSnapshot) {
                            if (paysureSnapshot.exists()) {
                                int currentPaysureBalance = paysureSnapshot.child("balance").getValue(Integer.class);

                                // Subtract the inputAmount from the current Paysure balance
                                int newPaysureBalance = currentPaysureBalance - inputAmount;

                                // Update the balance in Paysure
                                paysureRef.child(rfid).child("balance").setValue(newPaysureBalance);

                                Toast.makeText(loadingActivity.this, "Transaction successful!", Toast.LENGTH_SHORT).show();
                                inputloading.setText(""); // Clear the input field
                            } else {
                                Toast.makeText(loadingActivity.this, "Paysure balance not found!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(loadingActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(loadingActivity.this, "RFID not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(loadingActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void assignId(MaterialButton btn, int id) {
        btn = findViewById(id);
        btn.setOnClickListener(this);
    }
}
