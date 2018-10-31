package com.manridy.iband.view.model.sport;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.manridy.applib.callback.LocationCallBack;
import com.manridy.iband.IbandApplication;
import com.manridy.iband.R;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.map.LocationUtil;
import com.manridy.iband.view.base.BaseEventFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class OutdoorRunGoogleMapFragment extends BaseEventFragment implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {
//    @BindView(R.id.mMapView)
//    MapView mMapView;

    private GoogleMap mMap;

    @BindView(R.id.iv_map_close)
    ImageView ivMapClose;

    @Override
    protected View initView(LayoutInflater inflater, @Nullable ViewGroup container) {
        root = inflater.inflate(R.layout.fragment_sport_outdoor_run_googlemap, container, false);
        ButterKnife.bind(this, root);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//        mMapView.onCreate(getArguments());
//        mMapView.getMapAsync(this);
        return root;
    }

    @Override
    protected void initVariables() {

    }

    @Override
    protected void initListener() {

    }



    @OnClick({R.id.iv_map_close})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_map_close:
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                android.support.v4.app.Fragment mFragment;
                mFragment = new OutdoorRunMainFragment();
                transaction.replace(R.id.fl_content, mFragment);
                transaction.commit();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessage event) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
//        float latitude = (float) SPUtil.get(App.getInstance().getApplicationContext(), AppGlobal.DATA_LOCATION_LATITUDE,0.0);
//        float longitude = (float) SPUtil.get(App.getInstance().getApplicationContext(), AppGlobal.DATA_LOCATION_LONGITUDE,0.0);

//        googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Marker"));

        mMap = googleMap;
        //添加标记
        googleMap.addMarker(new MarkerOptions().position(new LatLng(IbandApplication.location_latitude, IbandApplication.location_longitude))
                .title("Marker in Googleplex"));
        //移动摄像头
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(IbandApplication.location_latitude, IbandApplication.location_longitude), 14));
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        if (ActivityCompat.checkSelfPermission(getAppliaction(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            mMap.setMyLocationEnabled(true);
//            return;
        }
        mMap.setBuildingsEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.setIndoorEnabled(true);
//        enableMyLocation();
    }




    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission to access the location is missing.
//            PermissionUtils.requestPermission(, 1,
//                    Manifest.permission.ACCESS_FINE_LOCATION, true);
//        } else if (mMap != null) {
//            // Access to the location has been granted to the app.
//            mMap.setMyLocationEnabled(true);
//        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public void onResume() {
        super.onResume();
//        EventBus.getDefault().post(new EventMessage(EventGlobal.VIEW_SPORTACTIVITY_HEADBAR_GONE));
        LocationUtil.getInstance(getAppliaction()).init().checkGpsStatus(true);
        LocationUtil.getInstance(getAppliaction()).setCallBack(locationCallBack);
        LocationUtil.getInstance(getAppliaction()).getLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocationUtil.getInstance(getAppliaction()).checkGpsStatus(false);
        LocationUtil.getInstance(getAppliaction()).removeLocationUpdatesListener();
    }


    LocationCallBack locationCallBack = new LocationCallBack(){
        @Override
        public void onGpsStatus(int satellites) {

        }

        @Override
        public void onLocation(Location location) {
            Toast.makeText(getAppliaction(),""+location.getLongitude()+":"+location.getLatitude(),Toast.LENGTH_SHORT).show();

        }
    };

}
