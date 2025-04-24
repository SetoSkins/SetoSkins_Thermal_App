package com.setoskins.thermal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.setoskins.thermal.BasePreferenceFragment;
import com.setoskins.thermal.R;
import com.hchen.himiuix.MiuiAlertDialog;

import java.io.DataOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    protected static String TAG = "MiuiPreference";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("first_run", true);

        if (isFirstRun) {
            // 尝试检测 root 权限
            new Thread(() -> {
                boolean hasRoot = false;
                try {
                    Process su = Runtime.getRuntime().exec("su");
                    DataOutputStream os = new DataOutputStream(su.getOutputStream());
                    os.writeBytes("exit\n");
                    os.flush();
                    int exitValue = su.waitFor();
                    hasRoot = (exitValue == 0);
                } catch (Exception e) {
                    hasRoot = false;
                }

                boolean finalHasRoot = hasRoot;
                runOnUiThread(() -> {
                    if (!finalHasRoot) {
                        // 无 root，弹出对话框
                        new MiuiAlertDialog(MainActivity.this)
                                .setTitle("Root")
                                .setMessage("该系统无 Root 权限")
                                .setHapticFeedbackEnabled(true)
                                .setCanceledOnTouchOutside(false)
                                .setCancelable(false) // 返回键也不能取消
                                .setPositiveButton("确定", (dialog, which) -> {
                                    // 可选：尝试重启，或者直接关闭应用
                                    // Runtime.getRuntime().exec("su -c reboot").waitFor();
                                    finishAffinity(); // 关闭应用
                                })
                                .show();
                    }
                });
            }).start();

            // 标记第一次进入已完成

        }
        // 获取 ImageView 并设置点击事件（原有代码）
        ImageView imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            new MiuiAlertDialog(MainActivity.this)
                    .setTitle("重启")
                    .setMessage("是否重启？")
                    .setHapticFeedbackEnabled(true)
                    .setCanceledOnTouchOutside(false)
                    .setPositiveButton("确定", (dialog, which) -> {
                        try {
                            Runtime.getRuntime().exec("su -c reboot").waitFor();
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "重启失败，请检查Root权限！", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("拒绝", null)
                    .show();
        });

        // 设置 WindowInsetsListener（原有代码）
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 获取 ViewPager2 和 RadioGroup（原有代码）
        Toolbar toolbar = findViewById(R.id.toolbar);
        ViewPager2 viewPager2 = findViewById(R.id.view_pager);
        RadioGroup radioGroup = findViewById(R.id.radio_group);

        // 初始化 Fragment（原有代码）
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new BasePreferenceFragment.PrefsHome());
        fragments.add(new BasePreferenceFragment.PrefsSettings());
        fragments.add(new BasePreferenceFragment.PrefsAbout());

        viewPager2.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return fragments.get(position);
            }

            @Override
            public int getItemCount() {
                return fragments.size();
            }
        });
        viewPager2.setUserInputEnabled(false);

        // 设置 ViewPager2 页面切换监听（原有代码）
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }
        });

        // 获取 RadioButton 并设置点击监听（新增代码）
        AppCompatRadioButton radioButtonSettings = findViewById(R.id.radio_button_settings);
        AppCompatRadioButton radioButtonAbout = findViewById(R.id.radio_button_about);
        RadioButton radioButtonHome = findViewById(R.id.radio_button_home);

// 设置点击监听
        radioButtonSettings.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "还没写好呢！主播正在疯狂赶代码", Toast.LENGTH_SHORT).show();
            v.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
            // 延迟500毫秒后回到主页（让Toast有足够时间显示）
            new Handler().postDelayed(() -> {
                radioButtonHome.setChecked(true); // 选中主页按钮
                viewPager2.setCurrentItem(0);   // 切换到第一个页面
                toolbar.setTitle("温语");        // 更新标题
            }, 0);
        });

        radioButtonAbout.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "还没写好呢！主播正在疯狂赶代码", Toast.LENGTH_SHORT).show();
            v.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
            new Handler().postDelayed(() -> {
                radioButtonHome.setChecked(true);
                viewPager2.setCurrentItem(0);
                toolbar.setTitle("温语");
            }, 0);
        });

// 简化RadioGroup监听（只处理主页按钮的逻辑）
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_button_home) {
                viewPager2.setCurrentItem(0);
                toolbar.setTitle("温语");
            }
        });
    }


    public void onCardClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://example.com")); // 替换为你的实际链接
        startActivity(intent);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}