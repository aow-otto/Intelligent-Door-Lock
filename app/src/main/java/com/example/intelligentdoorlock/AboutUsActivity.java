package com.example.intelligentdoorlock;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us);
        Toolbar toolbar = findViewById(R.id.toolbar_about_us);
        setSupportActionBar(toolbar);
        RelativeLayout relativeLayout = findViewById(R.id.relativeLayout);

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.icon)//图片
                .setDescription("本款APP的制作目的是与我们自主设计的自动识别开锁装置配套使用，所有源代码都已上传到了GitHub，您可以点击下方的按钮获取，您也可以点击下方的按钮联系我们，非常感谢您的支持！")//介绍
                .addItem(new Element().setTitle("Version 1.0"))
                .addGroup("与我联系")
                .addEmail("wangao.ball@sjtu.edu.cn", "  通过邮箱联系我们")//邮箱
                //.addWebsite("http://zhaoweihao.me")//网站
                //.addPlayStore("com.example.abouttest")//应用商店
                .addGitHub("f-u-wangao/Intelligent-Door-Lock", "  关注我们的GitHub项目")//github
                .create();
        aboutPage.setBackgroundColor(Color.rgb(0, 0, 0));
        relativeLayout.addView(aboutPage);
    }
}
