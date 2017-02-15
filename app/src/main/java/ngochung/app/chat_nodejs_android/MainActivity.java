package ngochung.app.chat_nodejs_android;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ngochung.app.Applications.MyApplication;
import ngochung.app.Constants.Constants;
import ngochung.app.Models.Message;
import ngochung.app.Untils.SharedConfig;

public class MainActivity extends AppCompatActivity {
    public static String MAIN_LOG="MainActivity";
    private EditText ed_message;
    private Button bt_send;
    private Socket mSocket;
    {
     try {
            mSocket= IO.socket(Constants.URL_SOCKET);
     }catch (URISyntaxException e){

     }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        showToast(new SharedConfig(getBaseContext()).getValueBoolean(SharedConfig.LOGIN)+"    "+new SharedConfig(getBaseContext()).getValueString(SharedConfig.ACCESS_TOKEN));
        mSocket.on("new message", onNewMessage);
        mSocket.connect();

        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend(ed_message);
            }
        });
    }
    public void init(){
        ed_message=(EditText)findViewById(R.id.ed_content);
        bt_send=(Button)findViewById(R.id.bt_send);

    }
    public void showToast(String msg){
        Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();

    }
    private void attemptSend(EditText mInputMessageView ) {
        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }

        mInputMessageView.setText("");
        SharedConfig share= new SharedConfig(getBaseContext());
        String name=share.getValueString(SharedConfig.ACCESS_TOKEN);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        Message msg= new Message(name,message,date);
        Gson gson = new Gson();
        String jsonInString = gson.toJson(msg);
        mSocket.emit("new message", jsonInString);
    }
    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            JSONObject data = (JSONObject) args[0];
            String username;
            String message;
            try {
                username = data.getString("name");
                message = data.getString("message");
            } catch (JSONException e) {
                return;
            }

            Log.i(MAIN_LOG,username+"   "+message);
            //addMessage(username, message);
        }
    };
    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off("new message", onNewMessage);
    }
}
