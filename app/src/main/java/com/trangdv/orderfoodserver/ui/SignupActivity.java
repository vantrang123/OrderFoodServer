package com.trangdv.orderfoodserver.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;
import com.trangdv.orderfoodserver.R;
import com.trangdv.orderfoodserver.common.Common;

public class SignupActivity extends AppCompatActivity {
    private EditText edt_username;
    private EditText edt_phonenumber;
    private EditText edt_password;
    private FloatingActionButton fab;
    private CountryCodePicker countryCodePicker;

    private String name;
    private String phoneNumber;
    private String password;
    private String code;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference table_user = database.getReference("User");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        inits();
    }

    private void inits() {
        edt_username = findViewById(R.id.username_edt);
        edt_phonenumber = findViewById(R.id.phonenumber_edt_signup);
        edt_password = findViewById(R.id.password_edt_signup);
        countryCodePicker = findViewById(R.id.contry_code_picker);

        fab = findViewById(R.id.fab_back_login);
        setClickNext();
    }

    private void setClickNext() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTextfromEdt();
                if (name.equals("")==false && phoneNumber.equals("")==false && password.equals("")==false) {
//                    createUser();
                    gotoVerification();
                }
            }
        });
    }

    private void gotoVerification() {
        String phoneFormat = "+" + code + phoneNumber;

        Intent intent = new Intent(SignupActivity.this, VerifyPhoneActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("phoneNumber", phoneNumber);
        bundle.putString("userName", name);
        bundle.putString("password", password);
        bundle.putString("phoneFormat", phoneFormat);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void getTextfromEdt() {
        phoneNumber = edt_phonenumber.getText().toString();
        password = edt_password.getText().toString();
        name = edt_username.getText().toString();
        code = countryCodePicker.getSelectedCountryCode().trim();

    }

    private void sendResult() {

        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(LoginActivity.KEY_PHONENUMBER, phoneNumber);
        bundle.putString(LoginActivity.KEY_PASSWORD, password);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Common.animateFinish(this);
    }
}
