package com.your.photograph;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.ocr.sdk.model.IDCardParams;
import com.hjq.bar.TitleBar;
import com.your.photograph.bean.BankCardEntry;
import com.your.photograph.bean.IdCardEntry;
import com.your.photograph.util.Constant;
import com.your.photograph.view.BankCardActivity;
import com.your.photograph.view.IDCardActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {


    @BindView(R.id.title)
    TitleBar title;
    @BindView(R.id.btn_idCard_font)
    Button btnIdCardFont;
    @BindView(R.id.btn_idCard_back)
    Button btnIdCardBack;
    @BindView(R.id.btn_bank_card)
    Button btnBankCard;
    @BindView(R.id.tv_idCard_font_result)
    TextView tvIdCardFontResult;
    @BindView(R.id.tv_idCard_back_result)
    TextView tvIdCardBackResult;
    @BindView(R.id.tv_bank_result)
    TextView tvBankResult;

    private Context mContext;
    private Camera camera;

    public final int REQUESTCODE_FROM_ID_CARD_FONT = 102;

    public final int REQUESTCODE_FROM_ID_CARD_BACK = 103;

    public final int REQUESTCODE_FROM_BANK_CARD = 104;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mContext = this;
        initView();
    }

    private void initView() {
        title.setLeftIcon(R.mipmap.bar_icon_back_white);
    }

    @OnClick({R.id.btn_idCard_font, R.id.btn_idCard_back, R.id.btn_bank_card})
    public void onViewClicked(View view) {

        switch (view.getId()) {
            case R.id.btn_idCard_font:
                idCardFont();
                break;
            case R.id.btn_idCard_back:
                idCardBack();
                break;
            case R.id.btn_bank_card:
                bankCard();
                break;
        }
    }

    //身份证正面扫描
    private void idCardFont() {
        Intent intent = new Intent(mContext, IDCardActivity.class);
        intent.putExtra(Constant.SCANNING_TYPE, IDCardParams.ID_CARD_SIDE_FRONT);
        startActivityForResult(intent, REQUESTCODE_FROM_ID_CARD_FONT);
    }

    //身份证正面扫描
    private void idCardBack() {
        Intent intent = new Intent(mContext, IDCardActivity.class);
        intent.putExtra(Constant.SCANNING_TYPE, IDCardParams.ID_CARD_SIDE_BACK);
        startActivityForResult(intent, REQUESTCODE_FROM_ID_CARD_BACK);
    }

    /**
     * 银行卡扫描
     */
    private void bankCard(){
        Intent intent = new Intent(mContext, BankCardActivity.class);
        intent.putExtra(Constant.SCANNING_TYPE, Constant.BANK_CARD_BG);
        startActivityForResult(intent, REQUESTCODE_FROM_BANK_CARD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null){
            IdCardEntry entryFont;
            switch (requestCode){
                case REQUESTCODE_FROM_ID_CARD_FONT:
                    entryFont = (IdCardEntry) data.getSerializableExtra(Constant.ID_CARD_FONT);
                    tvIdCardFontResult.setText("姓名："+entryFont.getName()+"\n身份证号："+entryFont.getIdNumber()+"\n住址："+entryFont.getAddress());
                    break;
                case REQUESTCODE_FROM_ID_CARD_BACK:
                    entryFont = (IdCardEntry) data.getSerializableExtra(Constant.ID_CARD_BACK);
                    tvIdCardBackResult.setText("开始日期："+entryFont.getSignDate()+"\n截止日期："+entryFont.getExpiryDate()+"\n签发机关："+entryFont.getIssueAuthority());
                    break;
                case REQUESTCODE_FROM_BANK_CARD:
                    BankCardEntry entryBankCard= (BankCardEntry) data.getSerializableExtra(Constant.BANK_CARD_BG);
                    String bankType;
                    if (entryBankCard.getBankType().equals("Debit")){
                        bankType="借记卡";
                    }else if (entryBankCard.getBankType().equals("Credit")){
                        bankType="信用卡";
                    }else {
                        bankType="未知类型";
                    }
                    tvBankResult.setText("银行名称："+entryBankCard.getBankName()+"\n银行卡号："+entryBankCard.getBankCardNumber()+"\n银行卡类型："+bankType);
                    break;
            }
        }
    }


    //打开闪光灯
    private void open() {
        try {
            camera = Camera.open();
            camera.startPreview();
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void close() {
        try {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);
            camera.release();
            camera=null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
