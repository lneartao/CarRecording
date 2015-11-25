package net.carslink.activity;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import net.carslink.asynctask.GetLoginCodeTask;
import net.carslink.asynctask.LoginOrRegTask;

public class LoginRegActivity extends Activity {
    EditText phoneText, codeText;
    Button codeButton, submitButton;
    RelativeLayout login_RelativeLayout;
    TimeCount tc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_loginreg);
        tc = new TimeCount(60000, 1000);
        login_RelativeLayout = (RelativeLayout)findViewById(R.id.login_RelativeLayout);
        codeButton = (Button)findViewById(R.id.codeButton);
        submitButton = (Button)findViewById(R.id.submitButton);
        phoneText = (EditText)findViewById(R.id.phoneText);
        codeText = (EditText)findViewById(R.id.codeText);

        login_RelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        codeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneText.getText().toString().trim();
                if(phoneNumber.isEmpty()){
                    Toast.makeText(getApplicationContext(), "请输入手机号码", Toast.LENGTH_SHORT).show();
                    return;
                }
                tc.start();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                
                new GetLoginCodeTask(LoginRegActivity.this, phoneNumber).execute();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneText.getText().toString().trim();
                String code = codeText.getText().toString().trim();
                if(phoneNumber.isEmpty() || code.isEmpty()){
                    Toast.makeText(getApplicationContext(), "请输入手机号码和验证码", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                HashMap<String, String> sendMsg = new HashMap<String, String>();
                sendMsg.put("phoneNumber", phoneNumber);
                sendMsg.put("idCode", code);
                new LoginOrRegTask(LoginRegActivity.this, sendMsg).execute();
            }
        });
    }

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
        }
        @Override
        public void onFinish() {//计时完毕时触发
            codeButton.setText("获取验证码");
            codeButton.setEnabled(true);
        }
        @Override
        public void onTick(long millisUntilFinished){//计时过程显示
            codeButton.setEnabled(false);
            codeButton.setText("稍后获取("+millisUntilFinished /1000+")");
        }
    }
}
