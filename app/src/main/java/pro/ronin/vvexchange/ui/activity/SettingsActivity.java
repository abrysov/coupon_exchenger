package pro.ronin.vvexchange.ui.activity;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import pro.ronin.vvexchange.App;
import pro.ronin.vvexchange.R;

/**
 * Created by ABrysov on 21/07/15
 */
public class SettingsActivity extends Activity {

    private ToggleButton mUsePass;

    private App mApp;

    private Button buttChangePass;
    private EditText editPass;

    private TextView version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mApp = (App) getApplication();

        mUsePass = (ToggleButton) findViewById(R.id.usePass);
        mUsePass.setChecked(mApp.getLocalStorage().getUsePass());
        mUsePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUsePass.setChecked(mUsePass.isChecked());
                mApp.getLocalStorage().setUsePass(mUsePass.isChecked());
            }
        });

        editPass = (EditText) findViewById(R.id.editPass);

        version = (TextView) findViewById(R.id.version);

        try {
            version.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        buttChangePass = (Button) findViewById(R.id.buttChangePass);
        buttChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editPass.getText().length() > 4) {
                    mApp.getLocalStorage().savePass(editPass.getText().toString());
                    Toast.makeText(getApplicationContext(), getString(R.string.message_succes_changed_pass), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_invalid_pass), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
