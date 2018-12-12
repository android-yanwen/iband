package com.manridy.iband.view.main;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps2d.CoordinateConverter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.manridy.iband.IbandApplication;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.bean.StepModel;
import com.manridy.iband.bean.data.RunLocationModel;
import com.manridy.iband.view.base.BaseActionActivity;


import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GoogleMapPlaybackActivity extends BaseActionActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener  {

    private GoogleMap mMap;

    @BindView(R.id.iv_map_close)
    ImageView ivMapClose;
    @BindView(R.id.tv_exercise_time)
    TextView tv_exercise_time;
    @BindView(R.id.tv_pace)
    TextView tv_pace;
    @BindView(R.id.tv_distance)
    TextView tv_distance;
    @BindView(R.id.ll_run_info)
    LinearLayout ll_run_info;

//    LocationService.LocationServiceBinder locationServiceBinder;
    private String stepDate;
    private List<RunLocationModel> runLocationModels;
    private List<LatLng> latLngs;
    private StepModel runData;

    boolean isGoogleMapReady = false;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_googlemap);
        ButterKnife.bind(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        Intent intent = new Intent(GoogleMapPlaybackActivity.this,LocationService.class);
//        bindService(intent,locationServiceConnection,BIND_AUTO_CREATE);

    }


    Runnable initRunLocationModels = new Runnable() {
        @Override
        public void run() {
            runLocationModels = IbandDB.getInstance().getRunLocationData(stepDate);
            runData = IbandDB.getInstance().getRunData(stepDate);
            latLngs = new ArrayList<LatLng>();
            String locationData;
            String[] locations;
            String[] dots;
            LatLng latLng;
            for(RunLocationModel runLocationModel : runLocationModels) {
                locationData = runLocationModel.getLocationData();
                locations = locationData.split(";");
                for(String location:locations){
                    dots = location.split(",");
                    latLng =new LatLng(Double.parseDouble(dots[0]),Double.parseDouble(dots[1]));
//                    CoordinateConverter converter  = new CoordinateConverter();
//                    converter.from(CoordinateConverter.CoordType.GPS);
//                    converter.coord(latLng);
//                    com.amap.api.maps2d.model.LatLng newLatLng = converter.convert();
//                    latLngs.add(newLatLng);
                    latLngs.add(latLng);
                }

            }
            Message message2 = handler.obtainMessage();
            message2.what = 2;
            handler.sendMessage(message2);

        }
    };

//    private ServiceConnection locationServiceConnection = new  ServiceConnection(){
//
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            locationServiceBinder = (LocationService.LocationServiceBinder)service;
////            locationServiceBinder.startCheckSatellites(true);
////            locationServiceBinder.startLocation();
//            if(locationServiceBinder.getIsRunning()){
//                stepDate = TimeUtil.getYMDHMSTime(locationServiceBinder.getRunRecordDate());
//                if(stepDate!=null&&!"".equals(stepDate)) {
//                    handler.post(initRunLocationModels);
//                }
//            }
//
//
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//
//        }
//    };

    @OnClick({R.id.iv_map_close})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_map_close:
                finish();
                break;
        }
    }

    @Override
    protected void initVariables() {

    }

    @Override
    protected void initListener() {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
//        float latitude = (float) SPUtil.get(App.getInstance().getApplicationContext(), AppGlobal.DATA_LOCATION_LATITUDE,0.0);
//        float longitude = (float) SPUtil.get(App.getInstance().getApplicationContext(), AppGlobal.DATA_LOCATION_LONGITUDE,0.0);

//        googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Marker"));
        isGoogleMapReady = true;
        ll_run_info.setVisibility(View.VISIBLE);
        mMap = googleMap;
        //添加标记
//        googleMap.addMarker(new MarkerOptions().position(new LatLng(App.location_latitude, App.location_longitude))
//                .title("Marker in Googleplex"));
        //移动摄像头
        googleMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(new LatLng(IbandApplication.location_latitude, IbandApplication.location_longitude), 18));
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            mMap.setMyLocationEnabled(false);
//            return;
        }
        mMap.setBuildingsEnabled(true);
        mMap.setMyLocationEnabled(false);
        mMap.setTrafficEnabled(true);
        mMap.setIndoorEnabled(true);
//        enableMyLocation();


        stepDate = getIntent().getStringExtra("StepDate");
        if(stepDate!=null&&!"".equals(stepDate)) {
            handler.post(initRunLocationModels);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Message message = handler.obtainMessage();
        message.what = 4;
        handler.sendMessageDelayed(message,1000);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**绘制两个坐标点之间的线段,从以前位置到现在位置*/
    private void setUpMap(LatLng oldData, LatLng newData,boolean isInCN ) {
        if(isInCN){
            com.amap.api.maps2d.model.LatLng aMapOldData = new com.amap.api.maps2d.model.LatLng(oldData.latitude,oldData.longitude);
            com.amap.api.maps2d.model.LatLng aMapnewData = new com.amap.api.maps2d.model.LatLng(newData.latitude,newData.longitude);

            CoordinateConverter converter  = new CoordinateConverter();
            converter.from(CoordinateConverter.CoordType.GPS);
            converter.coord(aMapOldData);
            com.amap.api.maps2d.model.LatLng oldDesLatLng = converter.convert();
            LatLng googleOldLatLng = new LatLng(oldDesLatLng.latitude,oldDesLatLng.longitude);

            CoordinateConverter converter1  = new CoordinateConverter();
            converter1.from(CoordinateConverter.CoordType.GPS);
            converter1.coord(aMapnewData);
            com.amap.api.maps2d.model.LatLng newDesLatLng = converter1.convert();
            LatLng googleNewLatLng = new LatLng(newDesLatLng.latitude,newDesLatLng.longitude);

            // 绘制一个大地曲线
            mMap.addPolyline((new PolylineOptions())
                .add(googleOldLatLng, googleNewLatLng)
                .geodesic(true).color(Color.RED));

        }else{
            mMap.addPolyline((new PolylineOptions())
                    .add(oldData, newData)
                    .geodesic(true).color(Color.RED));
        }




    }
    private void setUpMap(List<LatLng> latLngs,boolean isInCN) {
          if(isInCN){
              List<LatLng> googleLatLngs = new ArrayList<>();

              for(LatLng latLng:latLngs){

                  com.amap.api.maps2d.model.LatLng aMapOldData = new com.amap.api.maps2d.model.LatLng(latLng.latitude,latLng.longitude);

                  CoordinateConverter converter  = new CoordinateConverter();
                  converter.from(CoordinateConverter.CoordType.GPS);
                  converter.coord(aMapOldData);
                  com.amap.api.maps2d.model.LatLng oldDesLatLng = converter.convert();
                  LatLng googleOldLatLng = new LatLng(oldDesLatLng.latitude,oldDesLatLng.longitude);
                  googleLatLngs.add(googleOldLatLng);

              }
              if(mMap!=null){
                  mMap.addPolyline((new PolylineOptions()).addAll(googleLatLngs)
                          .geodesic(true).color(Color.RED));

              }
              if(googleLatLngs.size()>0){
                  MarkerOptions markerOptions = new MarkerOptions();

                  com.amap.api.maps2d.model.LatLng aMapData1 = new com.amap.api.maps2d.model.LatLng(googleLatLngs.get(0).latitude,googleLatLngs.get(0).longitude);
                  CoordinateConverter converter1  = new CoordinateConverter();
                  converter1.from(CoordinateConverter.CoordType.GPS);
                  converter1.coord(aMapData1);
                  com.amap.api.maps2d.model.LatLng oldDesLatLng1 = converter1.convert();
                  LatLng googleOldLatLng1 = new LatLng(oldDesLatLng1.latitude,oldDesLatLng1.longitude);
                  markerOptions.position(googleOldLatLng1);
                  markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.map_ic_start)));
                  mMap.addMarker(markerOptions);
                  MarkerOptions markerOptions2 = new MarkerOptions();

                  com.amap.api.maps2d.model.LatLng aMapData2 = new com.amap.api.maps2d.model.LatLng(googleLatLngs.get(googleLatLngs.size()-1).latitude,googleLatLngs.get(googleLatLngs.size()-1).longitude);
                  CoordinateConverter converter2  = new CoordinateConverter();
                  converter2.from(CoordinateConverter.CoordType.GPS);
                  converter2.coord(aMapData2);
                  com.amap.api.maps2d.model.LatLng oldDesLatLng2 = converter2.convert();
                  LatLng googleOldLatLng2 = new LatLng(oldDesLatLng2.latitude,oldDesLatLng2.longitude);
                  markerOptions.position(googleOldLatLng2);

                  markerOptions2.position(googleOldLatLng2);
                  markerOptions2.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.map_ic_end)));
                  mMap.addMarker(markerOptions2);
                  mMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(googleLatLngs.get(0), mMap.getCameraPosition().zoom));
              }

          }else{
              if(mMap!=null){
                  mMap.addPolyline((new PolylineOptions()).addAll(latLngs)
                          .geodesic(true).color(Color.RED));



                  if(latLngs.size()>0){
                      MarkerOptions markerOptions = new MarkerOptions();
                      markerOptions.position(latLngs.get(0));
                      markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.map_ic_start)));
                      mMap.addMarker(markerOptions);
                      MarkerOptions markerOptions2 = new MarkerOptions();
                      markerOptions2.position(latLngs.get(latLngs.size()-1));
                      markerOptions2.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.map_ic_end)));
                      mMap.addMarker(markerOptions2);
                      mMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(latLngs.get(0), mMap.getCameraPosition().zoom));
                  }
              }
          }

//        CoordinateConverter converter  = new CoordinateConverter();
//        converter.from(CoordinateConverter.CoordType.GPS);
//        converter.coord(oldData);
//        LatLng oldDesLatLng = converter.convert();

//        CoordinateConverter converter1  = new CoordinateConverter();
//        converter1.from(CoordinateConverter.CoordType.GPS);
//        converter1.coord(newData);
//        LatLng newDesLatLng = converter1.convert();




    }




    DecimalFormat df=new DecimalFormat("0.00");
    SimpleDateFormat sdf= new SimpleDateFormat("HH:mm:ss");
    Location oldLocation = null;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    break;
                case 2:
                    if(runData!=null){
                    boolean isInCN = runData.isInCN();
                        setUpMap(latLngs, isInCN);

                        tv_exercise_time.setText(runData.getRunTime());
                        String str_distance_km = df.format((float) runData.getStepMileage() / 1000);
                        tv_distance.setText(str_distance_km);
                        tv_pace.setText(runData.getPace());
                    }
                    break;
                case 4:
                    if(!isGoogleMapReady) {
                        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(GoogleMapPlaybackActivity.this);
                        builder.setTitle(R.string.notifyTitle).setMessage(R.string.hint_not_support_google_map);
                        builder.setPositiveButton(R.string.hint_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                        builder.show();
                    }
                    break;
            }
        }
    };


    @Override
    public void scrollToFinishActivity() {
    }

    @Override
    protected void initBack() {
    }
}
