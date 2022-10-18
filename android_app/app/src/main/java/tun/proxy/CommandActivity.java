package tun.proxy;

import static tun.proxy.MainActivity.REQUEST_VPN;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import tun.proxy.service.Tun2HttpVpnService;

public class CommandActivity extends AppCompatActivity {

    private static final String ACTION_TUN_PROXY_COMMAND = "action.tun_proxy.COMMAND";
    private static final String EXTRA_ACTION = "action";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            finish();
            return;
        }
        if (!ACTION_TUN_PROXY_COMMAND.equals(intent.getAction())) {
            finish();
            return;
        }
        String action = intent.getStringExtra(EXTRA_ACTION);
        if ("start".equals(action)) {
            if (isSettingsOK()) {
                Intent prepare = VpnService.prepare(this);
                if (prepare != null) {
                    startActivityForResult(prepare, REQUEST_VPN);
                } else {
                    onActivityResult(REQUEST_VPN, RESULT_OK, null);
                }
            } else {
                Log.d(getString(R.string.app_name) + ".Command", "Not Setup");
            }
        } else if ("stop".equals(action)) {
            Tun2HttpVpnService.stop(this);
        }
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_VPN && isSettingsOK()) {
            Tun2HttpVpnService.start(this);
        }
    }

    private boolean isSettingsOK() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String proxyHost = prefs.getString(Tun2HttpVpnService.PREF_PROXY_HOST, "");
        final int proxyPort = prefs.getInt(Tun2HttpVpnService.PREF_PROXY_PORT, 0);
        return !TextUtils.isEmpty(proxyHost) && proxyPort > 0;
    }
}