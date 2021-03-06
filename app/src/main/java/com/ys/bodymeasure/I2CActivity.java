package com.ys.bodymeasure;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ys.temperaturelib.device.IPointThermometer;
import com.ys.temperaturelib.device.MeasureDevice;
import com.ys.temperaturelib.device.i2cmatrix.IAMG88XX_8x8_WZHY;
import com.ys.temperaturelib.device.i2cmatrix.IMLX90621_16x4_BAA;
import com.ys.temperaturelib.device.i2cmatrix.IMLX90621_16x4_YS;
import com.ys.temperaturelib.device.i2cmatrix.IMLX90640_32x24_0AB1435407;
import com.ys.temperaturelib.device.i2cmatrix.IMLX90640_32x24_0AB1501502;
import com.ys.temperaturelib.device.i2cmatrix.IMLX90640_32x24_0AB1543001;
import com.ys.temperaturelib.device.i2cmatrix.IMLX90640_32x24_OAA1547511;
import com.ys.temperaturelib.device.i2cmatrix.IMLX90640_32x24_OAA1586901;
import com.ys.temperaturelib.device.i2cmatrix.IMLX90641_16x12;
import com.ys.temperaturelib.device.i2cmatrix.IOMRON_32x32;
import com.ys.temperaturelib.device.i2cmatrix.IOMROND6T_8L_8x1_HG;
import com.ys.temperaturelib.device.i2cmatrix.IOMRON_4x4;
import com.ys.temperaturelib.device.i2cmatrix.IOTPA_16PV4A_16x16;
import com.ys.temperaturelib.device.i2cmatrix.IRTX2080TI_16x16;
import com.ys.temperaturelib.device.i2cpoint.IHM_TSEV01CL55_HLDL;
import com.ys.temperaturelib.device.i2cpoint.IMLX90614;
import com.ys.temperaturelib.range.MesureResult;
import com.ys.temperaturelib.range.RangeFinder;
import com.ys.temperaturelib.range.device.FileImpl;
import com.ys.temperaturelib.temperature.TakeTempEntity;

/**
 * Created by Administrator on 2019/3/12.
 */

public class I2CActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    //所有i2c接口模块型号
    public static final String[] MODE_I2C = new String[]{IHM_TSEV01CL55_HLDL.MODE_NAME, IMLX90614.MODE_NAME,
            IMLX90640_32x24_0AB1543001.MODE_NAME, IMLX90640_32x24_0AB1501502.MODE_NAME, IMLX90640_32x24_0AB1435407.MODE_NAME,
            IMLX90640_32x24_OAA1547511.MODE_NAME, IMLX90640_32x24_OAA1586901.MODE_NAME, IMLX90621_16x4_YS.MODE_NAME, IAMG88XX_8x8_WZHY.MODE_NAME,
            IOMROND6T_8L_8x1_HG.MODE_NAME, IOMRON_4x4.MODE_NAME, IOMRON_32x32.MODE_NAME, IRTX2080TI_16x16.MODE_NAME, IMLX90641_16x12.MODE_NAME
            , IOTPA_16PV4A_16x16.MODE_NAME, IMLX90621_16x4_BAA.MODE_NAME};
    private MeasureDevice[] i2cDevices = new MeasureDevice[]{new IHM_TSEV01CL55_HLDL(), new IMLX90614(),
            new IMLX90640_32x24_0AB1543001(), new IMLX90640_32x24_0AB1501502(), new IMLX90640_32x24_0AB1435407(),
            new IMLX90640_32x24_OAA1547511(), new IMLX90640_32x24_OAA1586901(), new IMLX90621_16x4_YS(), new IAMG88XX_8x8_WZHY(),
            new IOMROND6T_8L_8x1_HG(), new IOMRON_4x4(), new IOMRON_32x32(), new IRTX2080TI_16x16(), new IMLX90641_16x12()
            , new IOTPA_16PV4A_16x16(),new IMLX90621_16x4_BAA()};
    private Button mTakeButton;
    private CheckBox mLightButton;
    private TextView distanceText;
    BaseFragment mSerialFragment;
    TemptakeDialog mTemptakeDialog;
    TakeTempEntity curTempEntity;
    MeasureDevice mCurProduct;
    RangeFinder mRangeFinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int index = intent.getIntExtra("device_index", -1);
        if (index < 0) return;
        setContentView(R.layout.activity_i2c);
        mTakeButton = findViewById(R.id.measure_take);
        mTakeButton.setOnClickListener(this);

        mLightButton = findViewById(R.id.measure_light);
        mLightButton.setOnCheckedChangeListener(this);
        distanceText = findViewById(R.id.measure_distance);
        mRangeFinder = new RangeFinder(new FileImpl());
        mRangeFinder.create();
        mRangeFinder.onMesure(new MesureResult<String>() {
            @Override
            public void onResult(String result) {
                distanceText.setText(String.format("BP : %s cm", result));
                if(mCurProduct != null) {
                    TakeTempEntity takeTempEntity = mCurProduct.getTakeTempEntity();
                    if(takeTempEntity == null){
                        takeTempEntity = new TakeTempEntity();
                    }
                    takeTempEntity.setDistances(Integer.parseInt(result));
                    mCurProduct.setTakeTempEntity(takeTempEntity);
                }
            }
        });
        mCurProduct = i2cDevices[index];
        setTitle(mCurProduct.getMeasureParm().mode);
        setFragmet(mCurProduct.isPoint() ? new PointI2CFragment() : new MatrixI2CFragment());

        setButtonValue(mTakeButton);
    }

    public MeasureDevice getCurProduct() {
        return mCurProduct;
    }

    protected void setFragmet(Fragment fragmet) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.i2c_view, fragmet).commit();
        mSerialFragment = (BaseFragment) fragmet;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCurProduct != null) mCurProduct.destroy();
        mCurProduct = null;
        if (mTemptakeDialog != null) mTemptakeDialog.distroy();
        mTemptakeDialog = null;
        mRangeFinder.release();
    }

    @Override
    public void onClick(View v) {
        if (v == mTakeButton) {
            if (mTemptakeDialog != null) mTemptakeDialog.distroy();
            mTemptakeDialog = null;
            mTemptakeDialog = new TemptakeDialog(this, mCurProduct, new TemptakeDialog.TakeTempCallback() {

                @Override
                public void callback(TakeTempEntity tempEntity) {
                    setButtonValue(mTakeButton);
                }
            });
            mTemptakeDialog.show();
        }
    }

    private void setButtonValue(Button view) {
        curTempEntity = mCurProduct.getTakeTempEntity();
        if (curTempEntity == null) {
            view.setVisibility(View.INVISIBLE);
        } else {
            view.setText("温度补偿(" + curTempEntity.getDistances() + "cm)");
            view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        mCurProduct.getTakeTempEntity().setLight(!b);
    }
}
