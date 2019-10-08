package com.dev.thrwat_zidan.la_bla_bla;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth auth;
    private Button btn_login, btn_phone;
    private EditText edt_email, edt_password;
    private TextView txt_need_new_account, txt_forget_password;
    private ProgressDialog loading;
    private DatabaseReference userRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        InitiFileds();




    }

    private void InitiFileds() {
        btn_login = findViewById(R.id.btn_login);
        btn_phone = findViewById(R.id.btn_phone);
        edt_email = findViewById(R.id.edt_reg_email);
        edt_password = findViewById(R.id.edt_reg_password);
        txt_need_new_account = findViewById(R.id.txt_need_new_account);
        txt_forget_password = findViewById(R.id.txt_forget_password);

        loading = new ProgressDialog(this);

        btn_login.setOnClickListener(this);
        btn_phone.setOnClickListener(this);
        edt_email.setOnClickListener(this);
        edt_password.setOnClickListener(this);
        txt_need_new_account.setOnClickListener(this);
        txt_forget_password.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.txt_need_new_account:
                goToRegisterPage();
                break;
            case R.id.btn_login:
                userLogin();
                break;
            case R.id.btn_phone:
                goToPhoneLoginActivity();
                break;
        }

    }



    private void userLogin() {
        String email = edt_email.getText().toString();
        String pass = edt_password.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please Enter email", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Please Enter Password", Toast.LENGTH_SHORT).show();
        } else {

            loading.setTitle("SignIn");
            loading.setMessage("Please Wait.....");
            loading.setCanceledOnTouchOutside(true);
            loading.show();

auth.signInWithEmailAndPassword(email,pass)
        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {


                    String currentUser_ID = auth.getCurrentUser().getUid();
                    String device_Token = FirebaseInstanceId.getInstance().getToken();
                    userRef.child(currentUser_ID).child("device_token")
                            .setValue(device_Token)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        SendUserMainActivity();
                                        Toast.makeText(LoginActivity.this, "Logged In Successfully...", Toast.LENGTH_SHORT).show();
                                        loading.dismiss();
                                    }
                                }
                            });



                }else{
                    String message = task.getException().toString();
                    Toast.makeText(LoginActivity.this, "Error"+message, Toast.LENGTH_SHORT).show();
                    loading.dismiss();
                }
            }
        });
        }
    }

    private void SendUserMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void goToRegisterPage() {
        startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
    }

    private void goToPhoneLoginActivity() {
        startActivity(new Intent(LoginActivity.this,PhoneLoginActivity.class));
    }
}
