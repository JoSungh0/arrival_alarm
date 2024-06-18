package com.example.arrival_alarm;

import static android.content.ContentValues.TAG;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class going extends AppCompatActivity implements OnMapReadyCallback {
    private LatLng destinationLocation, currentLocation;

    TextView distance;
    Button reset, nowlocation, howdistance;

    private GoogleMap nmap;

    private long updateInterval = 60000; // 1 minute default

    private FusedLocationProviderClient fusedLocationProviderClient;

    private LocationCallback locationCallback;

    private LocationRequest locationRequest =  LocationRequest.create();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_going);

        double latitude = getIntent().getDoubleExtra("dlatitude", 0.0);
        double longitude = getIntent().getDoubleExtra("dlongitude", 0.0);

        // LatLng 객체로 복원
        destinationLocation = new LatLng(latitude, longitude);

        latitude = getIntent().getDoubleExtra("clatitude", 0.0);
        longitude = getIntent().getDoubleExtra("clongitude", 0.0);

        currentLocation = new LatLng(latitude, longitude);

        checkDangerousPermissions();

        distance = findViewById(R.id.distance);
        reset = findViewById(R.id.reset);
        nowlocation = findViewById(R.id.nowlocation);
        howdistance = findViewById(R.id.howdistance);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.nmap);
        mapFragment.getMapAsync(this);

        //위치 확인 버튼 기능 추가
        nowlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLocationUpdates(updateInterval);
            }
        });

        // 목적지 재설정 버튼
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setReset();
            }
        });

        // 남은 거리를 확인하는 버튼
        howdistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDistance(currentLocation, destinationLocation);
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                // 위치 결과 처리
            }
        };

    }

    @Override
    public void onMapReady(GoogleMap nmap) {
        this.nmap = nmap;
        nmap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f));
        MarkerOptions markerOptions1 = new MarkerOptions().position(destinationLocation).title("목적지");
        MarkerOptions markerOptions2 = new MarkerOptions().position(currentLocation).title("현재 위치").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        nmap.addMarker(markerOptions1);
        nmap.addMarker(markerOptions2);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            nmap.setMyLocationEnabled(true);
        }
    }

    // 두 지점간 거리 확인
    private void checkDistance(LatLng currentLocation, LatLng destinationLocation) {
        if (fusedLocationProviderClient == null) {
            Log.e(TAG, "FusedLocationProviderClient is null");
            return;
        }
        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        } else {
            Log.e(TAG, "locationCallback is null");
            return;
            // 처리할 작업이나 예외 상황에 대한 처리를 여기에 추가할 수 있습니다.
        }

        float[] distanceResults = new float[1];
        Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                destinationLocation.latitude, destinationLocation.longitude, distanceResults);
        float dist = distanceResults[0];
        int value = (int) dist;
        Log.d("거리", String.valueOf(value));
        distance.setText("목적지까지 남은 거리는 :" + String.valueOf(value));


        // 각 거리마다 헌위치 업데이트 주기가 달라짐
        if (value <= 500) {
            Intent intent = new Intent(going.this, alarm.class);
            startActivity(intent);

            Log.d("요청", "곧 도착!!");
            fusedLocationProviderClient.removeLocationUpdates(locationCallback); // Stop updates
        } else if (value > 3000) {
            updateInterval = 60000; // 1
            Log.d("요청", "1분 요청");
        } else if (value > 1500) {
            updateInterval = 30000; // 30 seconds
            Log.d("요청", "30초 요청");
        } else if (value > 800) {
            updateInterval = 10000; // 10 seconds
            Log.d("요청", "10초 요청");
        }

        // Request location updates with the new interval
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        requestLocationUpdates(updateInterval);
    }

    // 현위치를 업데이트 함
    private void requestLocationUpdates(long interval) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            /*
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(going.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                nmap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLocation, 15.0f));
                            }
                        }
                    });

             */

            locationRequest = new LocationRequest();
            locationRequest.setInterval(interval); // 업데이트 간격 (밀리초)
            locationRequest.setFastestInterval(interval / 2); // 최고 업데이트 간격 (밀리초)
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // 고정밀 위치 요청

            // LocationCallback 초기화
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                }
            };
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }

    }

    // 목적지를 재설정함
    public void setReset() {
        destinationLocation = currentLocation;
        Intent outIntent = new Intent(getApplicationContext(),
                MainActivity.class);
        outIntent.putExtra("dlatitude", destinationLocation.latitude);
        outIntent.putExtra("dlongitude", destinationLocation.longitude);
        setResult(RESULT_OK, outIntent);
        finish();
    }

    // 지도 권한 설정
    private void checkDangerousPermissions() {
        String[] permissions = {
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + " 권한이 승인됨.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, permissions[i] + " 권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}