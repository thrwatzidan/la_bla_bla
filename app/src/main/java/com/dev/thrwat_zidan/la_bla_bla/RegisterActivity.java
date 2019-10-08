package com.dev.thrwat_zidan.la_bla_bla;

import android.app.ProgressDialog;
import android.content.Context;
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

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_register;
    private EditText edt_reg_email, edt_reg_password;
    private TextView txt_alaread_have_account;
    private FirebaseAuth auth;
    private ProgressDialog loadingBar;
    private DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initViews();
        auth = FirebaseAuth.getInstance();
        reference= FirebaseDatabase.getInstance().getReference();

    }

    private void initViews() {
        btn_register = findViewById(R.id.btn_register);
        edt_reg_email = findViewById(R.id.edt_reg_email);
        edt_reg_password = findViewById(R.id.edt_reg_password);
        txt_alaread_have_account = findViewById(R.id.txt_alaread_have_account);

        Context context;
        loadingBar=new ProgressDialog(this);

        btn_register.setOnClickListener(this);
        edt_reg_email.setOnClickListener(this);
        edt_reg_password.setOnClickListener(this);
        txt_alaread_have_account.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.txt_alaread_have_account:
                goToLoginPage();
                break;
            case R.id.btn_register:
                createNewAccount();
                break;
        }
    }

    private void createNewAccount() {
        String email = edt_reg_email.getText().toString();
        String pass = edt_reg_password.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please Enter email", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Please Enter Password", Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please Wait while we create account for you");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            auth.createUserWithEmailAndPassword(email,pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                String currentUser_ID = auth.getCurrentUser().getUid();
                                String device_Token = FirebaseInstanceId.getInstance().getToken();

                                String currentUser = auth.getCurrentUser().getUid();
                                reference.child("Users").child(currentUser).setValue("");

                                reference.child("Users").child(currentUser_ID).child("device_token")
                                        .setValue(device_Token);

                                goToMainActivity();
                                Toast.makeText(RegisterActivity.this, "Register Successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }else{
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error"+message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void goToMainActivity() {
        startActivity(new Intent(RegisterActivity.this,MainActivity.class));

    }

    private void goToLoginPage() {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
