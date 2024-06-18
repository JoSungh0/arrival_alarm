package com.example.arrival_alarm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final double DISTANCE_THRESHOLD = 50; // 거리 임계값 (미터)
    private List<Marker> markerList = new ArrayList<>(); //Marker List
    //객체 선언
    SupportMapFragment mapFragment;
    GoogleMap map;
    Button btnLocation, btnKor2Loc;
    EditText editText;

    private FusedLocationProviderClient fusedLocationProviderClient;

    float zoomLevel = 16.0f;

    private LatLng destinationLocation, tempLocation, currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //권한 설정
        checkDangerousPermissions();

        //객체 초기화
        editText = findViewById(R.id.destination);
        btnLocation = findViewById(R.id.mylocation);
        btnKor2Loc = findViewById(R.id.search);

        //지도 프래그먼트 설정
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d("onMap", "onMapReady: ");
                map = googleMap;
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissionsain
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel));
                                }
                            }
                        });

                map.setMyLocationEnabled(true);
                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {
                        // 마커 클릭 시 다이얼로그
                        Marker marker = markerList.get(0);
                        if (marker != null) {
                            showDestinationDialog(marker);
                        }

                    }
                });
            }
        });
        MapsInitializer.initialize(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //위치 확인 버튼 기능 추가
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestMyLocation();

            }
        });

        btnKor2Loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editText.getText().toString().length() > 0) {
                    Location location = getLocationFromAddress(getApplicationContext(), editText.getText().toString());

                    showCurrentLocation(location);
                }
            }
        });
    }

    // 검색 위치의 경도 위도 가져오기
    private Location getLocationFromAddress(Context context, String address) {
        Geocoder geocoder = new Geocoder(context);
        List<Address> addresses;
        Location resLocation = new Location("");
        try {
            addresses = geocoder.getFromLocationName(address, 5);
            if((addresses == null) || (addresses.size() == 0)) {
                return null;
            }
            Address addressLoc = addresses.get(0);

            resLocation.setLatitude(addressLoc.getLatitude());
            resLocation.setLongitude(addressLoc.getLongitude());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resLocation;
    }

    //내 위치 정보 가져오기
    private void requestMyLocation() {
        try {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel));
                            }
                        }
                    });
            Location clocation = new Location(""); // 더미 제공자 이름을 설정합니다. 실제로는 GPS 또는 네트워크 제공자를 사용할 수 있습니다.
            clocation.setLatitude(currentLocation.latitude);
            clocation.setLongitude(currentLocation.longitude);

            showCurrentLocation(clocation);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    //내 위치 정보 마커로 표시하기
    private void showCurrentLocation(Location location) {
        LatLng curPoint = new LatLng(location.getLatitude(), location.getLongitude());
        String msg = "Latitutde : " + curPoint.latitude
                + "\nLongitude : " + curPoint.longitude;
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        tempLocation = curPoint;
        Log.d("목적", msg.toString());

        //화면 확대, 숫자가 클수록 확대
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, zoomLevel));

        boolean shouldAddMarker = true;

        // 기존 마커들과 비교하여 일정 거리 이내에 있는지 확인
        for (Marker marker : markerList) {
            LatLng markerPosition = marker.getPosition();
            float[] distanceResults = new float[1];
            Location.distanceBetween(markerPosition.latitude, markerPosition.longitude,
                    curPoint.latitude, curPoint.longitude, distanceResults);
            float distance = distanceResults[0];

            if (distance < DISTANCE_THRESHOLD) {
                shouldAddMarker = false;
                break;
            }
        }

        // 일정 거리 이내에 없으면 새로운 마커 추가
        if (shouldAddMarker) {
            clearMarkers();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(curPoint);
            Marker newMarker = map.addMarker(markerOptions);
            markerList.add(newMarker);
        }
    }

    //권한 확인하기
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

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "권한 있음", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "권한 없음", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                Toast.makeText(this, "권한 설명 필요함.", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    //접근 권한들을 확인한다.
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

    // 목적지 설정 다이얼로그 표시
    private void showDestinationDialog(Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("목적지로 설정")
                .setMessage("이곳을 목적지로 설정하시겠습니까?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        destinationLocation = tempLocation;
                        // Yes 버튼 클릭 시 처리할 내용
                        Intent intent = new Intent(MainActivity.this, going.class);
                        intent.putExtra("dlatitude", destinationLocation.latitude);
                        intent.putExtra("dlongitude", destinationLocation.longitude);
                        intent.putExtra("clatitude", currentLocation.latitude);
                        intent.putExtra("clongitude", currentLocation.longitude);
                        startActivityForResult(intent, 0);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // No 버튼 클릭 시 처리할 내용
                        dialog.dismiss(); // 다이얼로그 닫기
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 기존에 추가된 모든 마커 삭제
    public void clearMarkers() {
        for (Marker marker : markerList) {
            marker.remove();
        }
        markerList.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            double latitude = getIntent().getDoubleExtra("dlatitude", 0.0);
            double longitude = getIntent().getDoubleExtra("dlongitude", 0.0);

            // LatLng 객체로 복원
            destinationLocation = new LatLng(latitude, longitude);
        }
    }
}
