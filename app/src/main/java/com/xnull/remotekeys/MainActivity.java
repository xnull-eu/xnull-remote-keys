package com.xnull.remotekeys;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.xnull.remotekeys.network.RemoteKeyClient;
import com.xnull.remotekeys.network.ServerDiscovery;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ServerDiscovery.DiscoveryListener, RemoteKeyClient.WebSocketListener {
    private TextView statusText;
    private LinearLayout deviceList;
    private GridLayout keypadLayout;
    private Button scanButton;
    
    private ServerDiscovery serverDiscovery;
    private RemoteKeyClient webSocketClient;
    private final List<String> keyList = new ArrayList<>();
    private NsdManager nsdManager;
    private MaterialButtonToggleGroup keypadToggle;
    private boolean isNumpadMode = true;
    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        statusText = findViewById(R.id.statusText);
        deviceList = findViewById(R.id.deviceList);
        keypadLayout = findViewById(R.id.keypadLayout);
        scanButton = findViewById(R.id.scanButton);

        nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        serverDiscovery = new ServerDiscovery(this, this);
        initializeKeyList();
        setupKeypad();
        setupScanButton();

        keypadToggle = findViewById(R.id.keypadToggle);
        keypadToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            // Only handle when a button is checked
            if (isChecked) {
                isNumpadMode = checkedId == R.id.numpadToggle;
                setupKeypad();
            }
        });

        // Set single selection mode
        keypadToggle.setSingleSelection(true);
        
        // Check numpad by default
        keypadToggle.check(R.id.numpadToggle);
    }

    private void initializeKeyList() {
        // Function keys
        for (int i = 1; i <= 12; i++) {
            keyList.add("F" + i);
        }
        
        // Numpad keys
        keyList.add("NUM_7");
        keyList.add("NUM_8");
        keyList.add("NUM_9");
        keyList.add("NUM_4");
        keyList.add("NUM_5");
        keyList.add("NUM_6");
        keyList.add("NUM_1");
        keyList.add("NUM_2");
        keyList.add("NUM_3");
        keyList.add("NUM_0");
        keyList.add("NUM_DOT");
        keyList.add("NUM_ENTER");
        keyList.add("NUM_ADD");
        keyList.add("NUM_SUB");
        keyList.add("NUM_MUL");
        keyList.add("NUM_DIV");
    }

    private void setupKeypad() {
        keypadLayout.removeAllViews();
        keypadLayout.setColumnCount(isNumpadMode ? 3 : 4);
        
        if (isNumpadMode) {
            setupNumpad();
        } else {
            setupFunctionKeys();
        }
    }

    private void setupNumpad() {
        String[][] numpadLayout = {
            {"7", "8", "9"},
            {"4", "5", "6"},
            {"1", "2", "3"},
            {".", "0", "ENTER"}
        };

        for (int row = 0; row < numpadLayout.length; row++) {
            for (int col = 0; col < numpadLayout[row].length; col++) {
                String key = numpadLayout[row][col];
                if (key.isEmpty()) continue;

                Button button = createKeyButton(
                    key,
                    "NUM_" + (key.equals(".") ? "DOT" : key)
                );

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                params.columnSpec = GridLayout.spec(col, 1, 1f);
                params.rowSpec = GridLayout.spec(row, 1);
                params.setMargins(4, 4, 4, 4);

                button.setLayoutParams(params);
                keypadLayout.addView(button);
            }
        }
    }

    private void setupFunctionKeys() {
        for (int i = 0; i < 12; i++) {
            Button button = createKeyButton("F" + (i + 1), "F" + (i + 1));
            
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i % 3, 1, 1f);
            params.rowSpec = GridLayout.spec(i / 3, 1);
            params.setMargins(4, 4, 4, 4);
            
            button.setLayoutParams(params);
            keypadLayout.addView(button);
        }
    }

    private Button createKeyButton(String text, String keyCommand) {
        Button button = new Button(this);
        button.setText(text);
        button.setOnClickListener(v -> sendKey(keyCommand));
        button.setTextSize(16);
        button.setMinHeight(getResources().getDimensionPixelSize(R.dimen.key_button_height));
        
        // Make the button expand to fill available space
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 0;
        params.setGravity(Gravity.FILL);
        
        // Set weight for equal distribution
        if (isNumpadMode) {
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
        } else {
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
        }
        
        params.setMargins(8, 8, 8, 8);
        button.setLayoutParams(params);
        
        button.setBackgroundResource(R.drawable.key_button_background);
        return button;
    }

    private void setupScanButton() {
        scanButton.setOnClickListener(v -> {
            if (isScanning) {
                serverDiscovery.stopDiscovery();
                isScanning = false;
                scanButton.setText(R.string.scan_for_devices);
            } else {
                deviceList.removeAllViews();
                serverDiscovery.startDiscovery();
                isScanning = true;
                scanButton.setText(R.string.stop_scanning);
            }
        });
    }

    private void sendKey(String key) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(key);
        } else {
            Toast.makeText(this, "Not connected to any device", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onServerFound(NsdServiceInfo serviceInfo) {
        runOnUiThread(() -> {
            Button deviceButton = new Button(this);
            deviceButton.setText(serviceInfo.getServiceName());
            deviceButton.setOnClickListener(v -> resolveAndConnect(serviceInfo));
            deviceList.addView(deviceButton);
        });
    }

    private void resolveAndConnect(NsdServiceInfo serviceInfo) {
        statusText.setText(R.string.resolving_service);
        
        nsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, 
                        getString(R.string.resolve_error, errorCode), 
                        Toast.LENGTH_SHORT).show();
                    statusText.setText(R.string.status_disconnected);
                });
            }

            @Override
            public void onServiceResolved(NsdServiceInfo resolvedService) {
                runOnUiThread(() -> connectToServer(resolvedService));
            }
        });
    }

    private void connectToServer(NsdServiceInfo serviceInfo) {
        try {
            if (serviceInfo.getHost() == null) {
                Toast.makeText(this, R.string.invalid_host, Toast.LENGTH_SHORT).show();
                return;
            }

            String url = String.format(Locale.US, "ws://%s:%d", 
                serviceInfo.getHost().getHostAddress(), 
                serviceInfo.getPort());
            webSocketClient = new RemoteKeyClient(new URI(url), this);
            webSocketClient.connect();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.connection_error, e.getMessage()), 
                Toast.LENGTH_SHORT).show();
            statusText.setText(R.string.status_disconnected);
        }
    }

    // ServerDiscovery.DiscoveryListener implementations
    @Override
    public void onServerLost(NsdServiceInfo serviceInfo) {
        runOnUiThread(() -> {
            // Remove the lost server from the list
            for (int i = 0; i < deviceList.getChildCount(); i++) {
                Button button = (Button) deviceList.getChildAt(i);
                if (button.getText().equals(serviceInfo.getServiceName())) {
                    deviceList.removeViewAt(i);
                    break;
                }
            }
        });
    }

    @Override
    public void onDiscoveryStarted() {
        runOnUiThread(() -> {
            statusText.setText(R.string.scanning_devices);
            isScanning = true;
            scanButton.setText(R.string.stop_scanning);
        });
    }

    @Override
    public void onDiscoveryStopped() {
        runOnUiThread(() -> {
            isScanning = false;
            scanButton.setText(R.string.scan_for_devices);
            if (webSocketClient == null || !webSocketClient.isOpen()) {
                statusText.setText(getString(R.string.status_disconnected));
            }
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> Toast.makeText(this, error, Toast.LENGTH_SHORT).show());
    }

    // WebSocketListener implementations
    @Override
    public void onConnected() {
        runOnUiThread(() -> statusText.setText(R.string.status_connected));
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(() -> statusText.setText(getString(R.string.status_disconnected)));
    }

    @Override
    public void onError(Exception ex) {
        runOnUiThread(() -> Toast.makeText(this, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onMessage(String message) {
        // Handle incoming messages if needed
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.close();
        }
        if (isScanning) {
            serverDiscovery.stopDiscovery();
        }
    }
}