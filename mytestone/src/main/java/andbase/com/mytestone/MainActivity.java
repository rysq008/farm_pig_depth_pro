package andbase.com.mytestone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.xiangchuang.risks.view.LoginFamerActivity;
import com.xiangchuang.risks.view.LoginFamerServer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickView(View view) {
        Toast.makeText(this, "nb", Toast.LENGTH_LONG).show();
        Intent it = new Intent(this, LoginFamerActivity.class);
        startActivity(it);
//        Intent it = new Intent(this, LoginFamerServer.class);
//        startService(it);
    }
}
