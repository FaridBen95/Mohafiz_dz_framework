package com.MohafizDZ.project.scan_dir;

import android.content.Context;

import com.MohafizDZ.App;
import com.MohafizDZ.project.scan_dir.presenters.AdminScanPresenterImpl;
import com.MohafizDZ.project.scan_dir.presenters.ProductScanPresenterImpl;

import org.json.JSONException;

import ru.bullyboo.encoder.Encoder;
import ru.bullyboo.encoder.methods.AES;

public abstract class ConcreteScanPresenter implements IScanPresenter.Presenter{
    private static final String TAG = ConcreteScanPresenter.class.getSimpleName();
    protected final IScanPresenter.View view;
    protected final Context context;
    protected abstract void onQrCodeScanned(String data) throws JSONException;

    protected ConcreteScanPresenter(IScanPresenter.View view, Context context) {
        this.view = view;
        this.context = context;
    }

    public static ConcreteScanPresenter getInstance(IScanPresenter.PresenterType presenterType, IScanPresenter.View view, Context context){
        switch (presenterType){
            case admin:
                return new AdminScanPresenterImpl(view, context);
            case productScan:
                return new ProductScanPresenterImpl(view, context);
        }
        return null;
    }

    protected String getString(int resId){
        return context.getString(resId);
    }

    @Override
    public void onScan(String result) {
        String clearTxt = App.TEST_MODE? result : Encoder.BuilderAES()
                .message(result)
                .method(AES.Method.AES_CBC_PKCS5PADDING)
                .key("JWYXelshE9saPgnpcRVuJWYXelshE9sa")
                .keySize(AES.Key.SIZE_256)
                .decrypt();
        try {
            onQrCodeScanned(clearTxt);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
