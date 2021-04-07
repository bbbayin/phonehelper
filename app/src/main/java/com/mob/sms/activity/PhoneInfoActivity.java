package com.mob.sms.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms.R;
import com.mob.sms.adapter.PhoneInfoAdapter;
import com.mob.sms.base.BaseActivity;
import com.mob.sms.bean.PhoneInfoBean;
import com.mob.sms.db.CallContactTable;
import com.mob.sms.db.DatabaseBusiness;
import com.mob.sms.db.SmsContactTable;
import com.mob.sms.utils.ToastUtil;

import org.apache.poi.hwpf.extractor.WordExtractor;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// 电话导入页面
public class PhoneInfoActivity extends BaseActivity {
    @BindView(R.id.title)
    TextView mTitleTv;
    @BindView(R.id.num)
    TextView mNumTv;
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private ArrayList<PhoneInfoBean> mDatas = new ArrayList<>();
    private PhoneInfoAdapter mPhoneInfoAdapter;
    private int mType;//0 word 1 excel 2 txt 3复制粘贴
    private String mType2;//sms  call
    private String mFilePath;
    private String mCopyContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phono_info);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
        initView();
    }

    private void initView() {
        mTitleTv.setText(getIntent().getStringExtra("title"));
        mType = getIntent().getIntExtra("type", 0);
        mFilePath = getIntent().getStringExtra("filePath");
        mCopyContent = getIntent().getStringExtra("copyContent");
        mType2 = getIntent().getStringExtra("type2");

        if (mType == 0) {
            readWord(mFilePath);
        } else if (mType == 1) {
            readExcel(mFilePath);
        } else if (mType == 2) {
            readTxtFile(mFilePath);
        } else if (mType == 3) {
            if (mCopyContent.contains("\n")) {
                int length = mCopyContent.split("\n").length;
                for (int i = 0; i < length; i++) {
                    mDatas.add(new PhoneInfoBean(mCopyContent.split("\n")[i], "无姓名", false));
                }
            } else {
                mDatas.add(new PhoneInfoBean(mCopyContent, "无姓名", false));
            }
        }
        mNumTv.setText(mDatas.size() + "个联系人");
        mPhoneInfoAdapter = new PhoneInfoAdapter(this, mDatas);
        mRecyclerView.setAdapter(mPhoneInfoAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mPhoneInfoAdapter.setOnItemClickLsitener(new PhoneInfoAdapter.OnItemClickListener() {
            @Override
            public void onclick(int position) {
                mDatas.get(position).isSelected = !mDatas.get(position).isSelected;
                mPhoneInfoAdapter.notifyDataSetChanged();
            }
        });
    }

    private void readExcel(String strFilePath) {
        File file = new File(strFilePath);
        Log.e("yy", "file=" + file.getAbsolutePath());
        String str = "";
        String v = null;
        boolean flat = false;
        List<String> ls = new ArrayList<String>();
        try {
            ZipFile xlsxFile = new ZipFile(file);
            ZipEntry sharedStringXML = xlsxFile
                    .getEntry("xl/sharedStrings.xml");
            InputStream inputStream = xlsxFile.getInputStream(sharedStringXML);
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setInput(inputStream, "utf-8");
            int evtType = xmlParser.getEventType();
            Log.e("=====>", "==xmlParser====>" + xmlParser.toString());
            while (evtType != XmlPullParser.END_DOCUMENT) {
                switch (evtType) {
                    case XmlPullParser.START_TAG:
                        String tag = xmlParser.getName();
                        if (tag.equalsIgnoreCase("t")) {
                            ls.add(xmlParser.nextText());
                            Log.e("=====>", "===xmlParser===>" + ls.toString());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                    default:
                        break;
                }
                evtType = xmlParser.next();
            }
            ZipEntry sheetXML = xlsxFile.getEntry("xl/worksheets/sheet1.xml");
            InputStream inputStreamsheet = xlsxFile.getInputStream(sheetXML);
            XmlPullParser xmlParsersheet = Xml.newPullParser();
            xmlParsersheet.setInput(inputStreamsheet, "utf-8");
            int evtTypesheet = xmlParsersheet.getEventType();
            while (evtTypesheet != XmlPullParser.END_DOCUMENT) {
                switch (evtTypesheet) {
                    case XmlPullParser.START_TAG:
                        String tag = xmlParsersheet.getName();
                        Log.e("=====>", "===tag222===>" + tag);
                        if (tag.equalsIgnoreCase("row")) {
                        } else if (tag.equalsIgnoreCase("c")) {
                            String t = xmlParsersheet.getAttributeValue(null, "t");
                            if (t != null) {
                                flat = true;
                                System.out.println(flat + "有");
                            } else {
                                System.out.println(flat + "没有");
                                flat = false;
                            }
                        } else if (tag.equalsIgnoreCase("v")) {
                            v = xmlParsersheet.nextText();
                            if (v != null) {
                                if (flat) {
                                    str += ls.get(Integer.parseInt(v)) + " ";
                                } else {
                                    str += v + " ";
                                }
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (xmlParsersheet.getName().equalsIgnoreCase("row")
                                && v != null) {
                            str += "\n";
                        }
                        break;
                }
                evtTypesheet = xmlParsersheet.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (str.contains("\n")) {
            int length = str.split("\n").length;
            for (int i = 0; i < length; i++) {
                mDatas.add(new PhoneInfoBean(str.split("\n")[i], "无姓名", false));
            }
        } else {
            mDatas.add(new PhoneInfoBean(str, "无姓名", false));
        }
    }

    private void readWord(String strFilePath) {
        FileInputStream in;
        String text = null;
        try {
            in = new FileInputStream(new File(strFilePath));
            WordExtractor extractor = null;
            //创建WordExtractor
            extractor = new WordExtractor(in);
            //进行提取对doc文件
            text = extractor.getText();

            if (text.contains("\n")) {
                int length = text.split("\n").length;
                for (int i = 0; i < length; i++) {
                    mDatas.add(new PhoneInfoBean(text.split("\n")[i], "无姓名", false));
                }
            } else {
                mDatas.add(new PhoneInfoBean(text, "无姓名", false));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readTxtFile(String strFilePath) {
        String path = strFilePath;
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        try {
            InputStream instream = new FileInputStream(file);
            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                //分行读取
                while ((line = buffreader.readLine()) != null) {
                    mDatas.add(new PhoneInfoBean(line, "无姓名", false));
                }
                instream.close();
            }
        } catch (Exception e) {
        }
    }

    @OnClick({R.id.back, R.id.delete, R.id.confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.delete:
                delete();
                break;
            case R.id.confirm:
                confirm();
                break;
        }
    }

    private void delete() {
        ArrayList<PhoneInfoBean> datas = new ArrayList<>();
        for (int i = 0; i < mDatas.size(); i++) {
            if (mDatas.get(i).isSelected) {
                datas.add(mDatas.get(i));
            }
        }
        if (datas.size() > 0) {
            mDatas.removeAll(datas);
            mPhoneInfoAdapter.notifyDataSetChanged();
            mNumTv.setText(mDatas.size() + "个联系人");
        } else {
            ToastUtil.show("请选择要删除的联系人电话");
        }
    }

    private void confirm() {
        if (mDatas.size() > 0) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("请稍后...");
            progressDialog.show();
            new Thread(){
                @Override
                public void run() {
                    insertDb();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            finish();
                        }
                    });
                }
            }.start();
        } else {
            ToastUtil.show("请选择联系人电话");
        }
    }

    private void insertDb() {
        if ("call".equals(mType2)) {
            List<CallContactTable> callContactTables = DatabaseBusiness.getCallContacts();
            if (callContactTables.size() > 0) {
                for (CallContactTable callContactTable : callContactTables) {
                    DatabaseBusiness.delCallContact(callContactTable);
                }
            }
            for (int i = 0; i < mDatas.size(); i++) {
                DatabaseBusiness.createCallContact(new CallContactTable(mDatas.get(i).name, mDatas.get(i).phone));
            }
        } else if ("sms".equals(mType2)) {
            List<SmsContactTable> smsContactTables = DatabaseBusiness.getSmsContacts();
            if (smsContactTables.size() > 0) {
                for (SmsContactTable smsContactTable : smsContactTables) {
                    DatabaseBusiness.delSmsContact(smsContactTable);
                }
            }
            for (int i = 0; i < mDatas.size(); i++) {
                DatabaseBusiness.createSmsContact(new SmsContactTable(mDatas.get(i).name, mDatas.get(i).phone));
            }
        }
    }
}
