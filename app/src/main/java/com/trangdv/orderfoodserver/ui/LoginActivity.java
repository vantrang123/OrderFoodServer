package com.trangdv.orderfoodserver.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.trangdv.orderfoodserver.R;
import com.trangdv.orderfoodserver.common.Common;
import com.trangdv.orderfoodserver.model.User;
import com.trangdv.orderfoodserver.retrofit.IAnNgonAPI;
import com.trangdv.orderfoodserver.retrofit.RetrofitClient;
import com.trangdv.orderfoodserver.utils.DialogUtils;
import com.trangdv.orderfoodserver.utils.SharedPrefs;

import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {

    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    DialogUtils dialogUtils;

    public static final int REQUEST_CODE = 2019;
    public static final String KEY_PHONENUMBER = "key phonenumber address";
    public static final String KEY_PASSWORD = "key password";
    public static final String SAVE_USER = "save user";
    public static final String SAVE_RESTAURANT_OWNER = "save restaurant owner";

    private TextView dispatch_signup;
    private EditText edt_phonenumber;
    private EditText edt_password;
    private FloatingActionButton fab;

    private String phonenumber;
    private String password;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference table_user = database.getReference("User");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
    }

    private void init() {
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        compositeDisposable = new CompositeDisposable();
        dialogUtils = new DialogUtils();

        dispatch_signup = findViewById(R.id.dispatch_signup);
        setClickDispatchSignup();
        fab = findViewById(R.id.fab_login);
        setOnClickFab();
        edt_phonenumber = findViewById(R.id.phonenumber_edt_login);
        edt_password = findViewById(R.id.password_edt_login);

    }

    private void setClickDispatchSignup() {
        dispatch_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DispatchSignup();
            }
        });
    }

    private void DispatchSignup() {
        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            phonenumber = data.getExtras().getString(KEY_PHONENUMBER, "");
            password = data.getExtras().getString(KEY_PASSWORD, "");

            setTextintoEdt();

        }
    }

    private void setOnClickFab() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTextfromEdt();

                if (phonenumber.equals("") == false && password.equals("") == false) {
                    authLogin();
                }

            }
        });
    }

    private void getTextfromEdt() {
        phonenumber = edt_phonenumber.getText().toString();
        password = edt_password.getText().toString();
    }

    private void authLogin() {
        dialogUtils.showProgress(this);
        compositeDisposable.add(
                anNgonAPI.getRestaurantOwner(Common.API_KEY, "k1o2DavRpsY959Mdwwt4ZSFDx7C3")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(restaurantOwnerModel -> {
                                    if (restaurantOwnerModel.isSuccess()) {
                                        // save curreentUser
                                        Common.currentRestaurantOwner = restaurantOwnerModel.getResult().get(0);

                                        SharedPrefs.getInstance().put(SplashActivity.CHECK_ALREADLY_LOGIN, 2);

                                        //save user in share pref
                                        SharedPrefs.getInstance().put(SAVE_RESTAURANT_OWNER, Common.currentRestaurantOwner);

                                        gotoMainActivity();

                                    } else {
                                        Toast.makeText(this, "[GET USER API NOT DATABASE]", Toast.LENGTH_SHORT).show();
                                    }
                                    dialogUtils.dismissProgress();

                                },
                                throwable -> {
                                    Toast.makeText(this, "[GET USER API]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    dialogUtils.dismissProgress();
                                }
                        ));
    }

    private void intoHome(User user) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        Common.currentUser = user;

        startActivity(intent);
        finish();
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        Common.animateStart(this);
    }

    private void setTextintoEdt() {
        edt_phonenumber.setText(phonenumber);
        edt_password.setText(password);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    //
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}
