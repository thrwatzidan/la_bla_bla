package com.dev.thrwat_zidan.la_bla_bla;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class PhoneLoginActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_send_ver_code, btn_verifay;
    private EditText edt_verification_code, edt_phoneNumber;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth auth;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        auth = FirebaseAuth.getInstance();
        initViews();

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredentail(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loading.dismiss();

                Toast.makeText(PhoneLoginActivity.this,
                        "Error" + e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                btn_send_ver_code.setVisibility(View.VISIBLE);
                edt_phoneNumber.setVisibility(View.VISIBLE);

                btn_verifay.setVisibility(View.INVISIBLE);
                edt_verification_code.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(s, token);

                mVerificationId = s;
                mResendToken = token;
                loading.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has Sent Successfully", Toast.LENGTH_SHORT).show();
                btn_send_ver_code.setVisibility(View.VISIBLE);
                edt_phoneNumber.setVisibility(View.VISIBLE);
                btn_verifay.setVisibility(View.INVISIBLE);
                edt_verification_code.setVisibility(View.INVISIBLE);

            }


        };
    }

    private void initViews() {
        btn_send_ver_code = findViewById(R.id.btn_send_ver_code);
        btn_verifay = findViewById(R.id.btn_verifay);
        edt_verification_code = findViewById(R.id.edt_verification_code);
        edt_phoneNumber = findViewById(R.id.edt_phoneNumber_login);


        loading = new ProgressDialog(this);

        btn_send_ver_code.setOnClickListener(this);
        btn_verifay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_send_ver_code:
                visabilty();
                break;
            case R.id.btn_verifay:
                verifayPhoneNumber();
                break;

        }
    }


    private void visabilty() {
//        btn_send_ver_code.setVisibility(View.INVISIBLE);
//        edt_phoneNumber.setVisibility(View.INVISIBLE);
//        btn_verifay.setVisibility(View.VISIBLE);
//        edt_verification_code.setVisibility(View.VISIBLE);

        String phoneNumber = edt_phoneNumber.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Phone number is required", Toast.LENGTH_SHORT).show();
        } else {

            loading.setTitle("Phone Verification");
            loading.setMessage("Please wait ,while we are authentication your phone");
            loading.setCanceledOnTouchOutside(false);
            loading.show();
            String p = ("+2" + phoneNumber);

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    p,
                    60,
                    TimeUnit.SECONDS,
                    PhoneLoginActivity.this,
                    callbacks);

        }

    }


    private void signInWithPhoneAuthCredentail(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loading.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congratulations,you are logged in successfully", Toast.LENGTH_SHORT).show();
                            goToMainActivity();
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void goToMainActivity() {
        startActivity(new Intent(PhoneLoginActivity.this, MainActivity.class));
        finish();
    }

    private void verifayPhoneNumber() {
        btn_send_ver_code.setVisibility(View.INVISIBLE);
        edt_phoneNumber.setVisibility(View.INVISIBLE);
        String verficationCode = edt_verification_code.getText().toString();

        if (TextUtils.isEmpty(verficationCode)) {
            Toast.makeText(this, "Please write verification code first... ", Toast.LENGTH_SHORT).show();
        } else {

            loading.setTitle("Verification Code");
            loading.setMessage("Please wait ,while we are verifying verification code...");
            loading.setCanceledOnTouchOutside(false);
            loading.show();


            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verficationCode);
            signInWithPhoneAuthCredentail(credential);
        }

    }

}
