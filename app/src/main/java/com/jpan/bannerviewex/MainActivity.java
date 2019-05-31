package com.jpan.bannerviewex;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.jpan.bannerview.BannerView;
import com.jpan.bannerview.entry.BannerEntry;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<BannerEntry> mEntryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BannerView bannerView = findViewById(R.id.banner_view);

        BannerEntry entry = new BannerEntry();
        entry.setResId(R.drawable.logo);
        entry.setName("测试");
        mEntryList.add(entry);

        BannerEntry entry1 = new BannerEntry();
        entry1.setResId(R.drawable.logo1);
        entry1.setName("测试1");
        mEntryList.add(entry1);

        BannerEntry entry2 = new BannerEntry();
        entry2.setResId(R.drawable.logo_retrofit);
        mEntryList.add(entry2);

        BannerEntry entry3 = new BannerEntry();
        entry3.setName("测试3");
        entry3.setResId(R.drawable.github);
        mEntryList.add(entry3);

        bannerView.setEntryList(mEntryList);
        bannerView.setOnBannerItemClickListener(new BannerView.OnBannerItemClickListener() {
            @Override
            public void onClick(int position) {
                Toast.makeText(MainActivity.this, mEntryList.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
