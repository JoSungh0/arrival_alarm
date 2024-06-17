package com.example.arrival_alarm;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.kakao.vectormap.*;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.KakaoMapSdk;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.MapViewInfo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    MapView mapView;
    KakaoMap kakaoMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        KakaoMapSdk.init(this, "6d03d6fb405130b73fd1d337f710def8");

        mapView = findViewById(R.id.map_view);
        mapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {
                // 지도 API가 정상적으로 종료될 때 호출
                Log.d("KakaoMap", "onMapDestroy: ");
            }

            @Override
            public void onMapError(Exception error) {
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출
                Log.e("KakaoMap", "onMapError: ", error);
            }
        }, new KakaoMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull KakaoMap map) {
                // 정상적으로 인증이 완료되었을 때 호출
                // KakaoMap 객체를 얻어 옵니다.
                kakaoMap = map;
            }

            @Override
            public LatLng getPosition() {
                return super.getPosition();
            }

            @Override
            public int getZoomLevel() {
                // 지도 시작 시 확대/축소 줌 레벨 설정
                return 15;
            }

            @Override
            public MapViewInfo getMapViewInfo() {
                // 지도 시작 시 App 및 MapType 설정
                return MapViewInfo.from("openmap", MapType.NORMAL);
            }

            @Override
            public boolean isVisible() {
                // 지도 시작 시 visible 여부를 설정
                return true;
            }


        });

        kakaoMap.setOnMapViewInfoChangeListener(new KakaoMap.OnMapViewInfoChangeListener() {
            @Override
            public void onMapViewInfoChanged(MapViewInfo mapViewInfo) {
                // MapViewInfo 변경 성공 시 호출
            }

            @Override
            public void onMapViewInfoChangeFailed() {
                // MapViewInfo 변경 실패 시 호출
            }
        });
/*
        // URL Scheme
        String url = "kakaomap://open";

        // Intent로 URL Scheme 호출
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        intent.addCategory(Intent.CATEGORY_BROWSABLE);

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        startActivity(intent);

 */
    }
}
