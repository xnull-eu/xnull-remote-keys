package com.xnull.remotekeys.network;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

public class ServerDiscovery {
    private static final String SERVICE_TYPE = "_remotekeys._tcp.";
    private final NsdManager nsdManager;
    private final DiscoveryListener listener;
    private NsdManager.DiscoveryListener discoveryListener;

    public interface DiscoveryListener {
        void onServerFound(NsdServiceInfo serviceInfo);
        void onServerLost(NsdServiceInfo serviceInfo);
        void onDiscoveryStarted();
        void onDiscoveryStopped();
        void onError(String error);
    }

    public ServerDiscovery(Context context, DiscoveryListener listener) {
        this.nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        this.listener = listener;
        initializeDiscoveryListener();
    }

    private void initializeDiscoveryListener() {
        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                listener.onError("Discovery start failed with code: " + errorCode);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                listener.onError("Discovery stop failed with code: " + errorCode);
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                listener.onDiscoveryStarted();
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                listener.onDiscoveryStopped();
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                if (serviceInfo.getServiceType().equals(SERVICE_TYPE)) {
                    listener.onServerFound(serviceInfo);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                listener.onServerLost(serviceInfo);
            }
        };
    }

    public void startDiscovery() {
        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        } catch (Exception e) {
            listener.onError("Failed to start discovery: " + e.getMessage());
        }
    }

    public void stopDiscovery() {
        try {
            nsdManager.stopServiceDiscovery(discoveryListener);
        } catch (Exception e) {
            listener.onError("Failed to stop discovery: " + e.getMessage());
        }
    }
} 