package com.example.beaconscanner;

import android.Manifest;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG = "RangingActivity";
    private BeaconManager beaconManager;
    TextView TDebug;
    androidx.constraintlayout.widget.ConstraintLayout background;
    int init = 0;
    int temp = 0;
    int maj = 0;
    int min = 0;
    int twinkle = 2;
    Identifier myBeaconUUID = Identifier.parse("fda50693a4e24fb1afcfc6eb07647825");
    BeaconParser beaconParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "App started up");
        setContentView(R.layout.activity_main);
        TDebug = findViewById(R.id.TDebug);
        background = findViewById(R.id.back);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }
        Log.d(TAG, "Permission granted");
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.setForegroundScanPeriod(30);
        beaconManager.bind(this);
        beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.removeAllMonitorNotifiers();
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {

            }

            @Override
            public void didExitRegion(Region region) {

            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {

            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", myBeaconUUID, null, null));
        } catch (RemoteException e) {
        }
        /*----------------------------------------------------------------------*/

        beaconManager.removeAllRangeNotifiers();
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                for (Beacon beacon : beacons) {
                    Log.e(TAG, beacon.getId1().toString());
                    if (init == 0) {
                        init = 1;
                        maj = beacon.getId2().toInt();
                        min = beacon.getId3().toInt();
                        final BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
                        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {

                            @Override
                            public void onStartFailure(int errorCode) {
                                Log.e("Class", "Advertisement start failed with code: " + errorCode);
                                TDebug.setText("Advertisement start Failed");
                            }

                            @Override
                            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                                try {
                                    Log.i(TAG, "Advertisement start succeeded.");
                                    TDebug.setText("Advertisement start succeeded");
                                    Thread.sleep(10);
                                    beaconTransmitter.stopAdvertising();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    } else if (beacon.getId2().toInt() >= maj) {
                        Log.d("Beacon", "ENTER Region." + beacon.getId1());
                        maj = beacon.getId2().toInt();
                        min = beacon.getId3().toInt();

                        final BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
                        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {

                            @Override
                            public void onStartFailure(int errorCode) {
                                Log.e("Class", "Advertisement start failed with code: " + errorCode);
                                TDebug.setText("Advertisement start Failed");
                            }

                            @Override
                            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                                try {
                                    Log.i(TAG, "Advertisement start succeeded.");
                                    TDebug.setText("Advertisement start succeeded");
                                    Thread.sleep(10);
                                    beaconTransmitter.stopAdvertising();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    //boardcast
                    if (twinkle % 2 == 1) {
                        switch (min) {
                            case 1:
                                try {
                                    background.setBackgroundColor(Color.parseColor("#FF0000"));
                                    Thread.sleep(250);
                                    background.setBackgroundColor(Color.parseColor("#FFFFFF"));

                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 2:
                                try {
                                    background.setBackgroundColor(Color.parseColor("#FF5809"));
                                    Thread.sleep(250);
                                    background.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 3:
                                try {
                                    background.setBackgroundColor(Color.parseColor("#F9F900"));
                                    Thread.sleep(250);
                                    background.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 4:
                                try {
                                    background.setBackgroundColor(Color.parseColor("#00DB00"));
                                    Thread.sleep(250);
                                    background.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 5:
                                try {
                                    background.setBackgroundColor(Color.parseColor("#2828FF"));
                                    Thread.sleep(250);
                                    background.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 6:
                                try {
                                    background.setBackgroundColor(Color.parseColor("#0072E3"));
                                    Thread.sleep(250);
                                    background.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 7:
                                try {
                                    background.setBackgroundColor(Color.parseColor("#6F00D2"));
                                    Thread.sleep(250);
                                    background.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 8:
                                if (maj > temp) {
                                    temp = maj;
                                    twinkle += 1;
                                    break;
                                }

                        }
                    }
                    if (twinkle % 2 == 0) {
                        switch (min) {
                            case 1:
                                background.setBackgroundColor(Color.parseColor("#FF0000"));
                                break;
                            case 2:
                                background.setBackgroundColor(Color.parseColor("#FF5809"));
                                break;
                            case 3:
                                background.setBackgroundColor(Color.parseColor("#F9F900"));
                                break;
                            case 4:
                                background.setBackgroundColor(Color.parseColor("#00DB00"));
                                break;
                            case 5:
                                background.setBackgroundColor(Color.parseColor("#2828FF"));
                                break;
                            case 6:
                                background.setBackgroundColor(Color.parseColor("#0072E3"));
                                break;
                            case 7:
                                background.setBackgroundColor(Color.parseColor("#6F00D2"));
                                break;
                            case 8:
                                if (maj > temp) {
                                    temp = maj;
                                    twinkle += 1;
                                    break;
                                }

                        }
                    }

                }
            }

        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("FDA50693-A4E2-4FB1-AFCF-C6EB07647825", myBeaconUUID, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

}