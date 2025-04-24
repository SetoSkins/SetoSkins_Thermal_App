package com.setoskins.thermal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.hchen.himiuix.DialogInterface;
import com.hchen.himiuix.MiuiAlertDialog;
import com.hchen.himiuix.MiuiCardPreference;
import com.hchen.himiuix.MiuiPreference;
import com.hchen.himiuix.MiuiSwitchPreference;

import com.setoskins.thermal.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public abstract class BasePreferenceFragment extends PreferenceFragmentCompat {
    private final static String TAG = "MiuiPreference";
    public boolean isFileExistRoot(String path) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "test -f \"" + path + "\""});
            int result = process.waitFor();
            return result == 0; // 文件存在返回 true
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState,
                                    @Nullable String rootKey) {
        setPreferencesFromResource(id(), rootKey);
        initPrefs();

        // 基础项
        Preference cardHasData = findPreference("prefs_card");
        Preference cardNoData = findPreference("prefs_card2");
        Preference cardLowVer = findPreference("prefs_card3");

// 开关项
        MiuiSwitchPreference prefNormal = findPreference("pref_normal");
        MiuiSwitchPreference prefThermal = findPreference("pref_thermal");
        MiuiSwitchPreference prefMaxCurrent = findPreference("pref_child");
        Preference prefEditDialog = findPreference("prefs_edit_dialog");

// 隐藏无关项
        for (String k : new String[]{"prefs_b", "prefs_c", "pref_intent", "pref_disabld"}) {
            Preference p = findPreference(k);
            if (p != null) p.setVisible(false);
        }

        boolean hasDataDir = false;
        boolean versionTooLow = false;

        try {
            // 检查配置.prop 是否存在
            Process process = Runtime.getRuntime().exec(new String[]{
                    "su", "-c", "test -f /data/adb/modules/SetoSkins/配置.prop"
            });
            hasDataDir = (process.waitFor() == 0);  // 返回码为0说明文件存在

            // 检查 module.prop 的 versionCode
            Process verProcess = Runtime.getRuntime().exec(new String[]{
                    "su", "-c", "cat /data/adb/modules/SetoSkins/module.prop"
            });
            BufferedReader reader = new BufferedReader(new InputStreamReader(verProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("versionCode=")) {
                    int versionCode = Integer.parseInt(line.split("=")[1]);
                    if (versionCode < 1080) {
                        versionTooLow = true;
                    }
                    break;
                }
            }
            verProcess.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

// 控制卡片显示
        if (cardHasData != null) cardHasData.setVisible(hasDataDir && !versionTooLow);
        if (cardNoData != null) cardNoData.setVisible(!hasDataDir && !versionTooLow);
        if (cardLowVer != null) cardLowVer.setVisible(versionTooLow);

// 禁用所有设置项（如果模块没装）
        if (!hasDataDir || versionTooLow) {
            if (prefNormal != null) prefNormal.setEnabled(false);
            if (prefThermal != null) prefThermal.setEnabled(false);
            if (prefMaxCurrent != null) prefMaxCurrent.setEnabled(false);
            if (prefEditDialog != null) prefEditDialog.setEnabled(false);

            Toast.makeText(getContext(),
                    versionTooLow ? "模块版本过低，请更新" : "模块未安装或配置文件缺失",
                    Toast.LENGTH_SHORT).show();
        }

        boolean isExist = isFileExistRoot("/data/adb/modules/SetoSkins/配置.prop");

// 如果文件存在，则执行后续操作
        if (isExist) {
            // 1. 快充模式
            // 检查配置文件中是否已有简洁版配置字段
            new Thread(() -> {
                try {
                    Process checkProcess = Runtime.getRuntime().exec(new String[]{
                            "su", "-c", "grep '^简洁版配置=' /data/adb/modules/SetoSkins/配置.prop"
                    });
                    int exitCode = checkProcess.waitFor();
                    // grep 没找到时退出码为 1
                    if (exitCode != 0) {
                        // 添加默认值为 true
                        String appendCmd = "echo '简洁版配置=true' >> /data/adb/modules/SetoSkins/配置.prop";
                        Runtime.getRuntime().exec(new String[]{"su", "-c", appendCmd}).waitFor();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // 1. 快充模式
            if (prefNormal != null) {
                prefNormal.setOnPreferenceChangeListener((preference, newValue) -> {
                    String v = ((Boolean) newValue) ? "true" : "false";
                    String cmd = "sed -i 's/^快充模式=.*/快充模式=" + v + "/' /data/adb/modules/SetoSkins/配置.prop";
                    try {
                        Runtime.getRuntime().exec(new String[]{"su", "-c", cmd}).waitFor();
                        Toast.makeText(getContext(), "快充模式已更新为: " + v, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "修改快充失败", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });
            }

            // 2. 温控空挂载模式
            if (prefThermal != null) {
                prefThermal.setOnPreferenceChangeListener((preference, newValue) -> {
                    String v = ((Boolean) newValue) ? "true" : "false";
                    String cmd = "sed -i 's/^温控空文件挂载=.*/温控空文件挂载=" + v + "/' /data/adb/modules/SetoSkins/配置.prop";
                    try {
                        Runtime.getRuntime().exec(new String[]{"su", "-c", cmd}).waitFor();
                        Toast.makeText(getContext(), "温控模式已更新为: " + v, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "修改温控失败", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });
            }

            // 3. 最大电流数开关
            if (prefMaxCurrent != null) {
                prefMaxCurrent.setOnPreferenceChangeListener((preference, newValue) -> {
                    String v = ((Boolean) newValue) ? "true" : "false";
                    String cmd = "sed -i 's/^开启修改电流数=.*/开启修改电流数=" + v + "/' /data/adb/modules/SetoSkins/配置.prop";
                    try {
                        Runtime.getRuntime().exec(new String[]{"su", "-c", cmd}).waitFor();
                        Toast.makeText(getContext(), "开关电流设置已更新为: " + v, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "修改电流失败", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });
            }

            if (prefEditDialog != null) {
                prefEditDialog.setOnPreferenceClickListener(preference -> {
                    // 1) 先读取当前配置
                    String currentValue = "";
                    try {
                        // 用 su + cat 读取文件
                        Process p = Runtime.getRuntime().exec(new String[]{
                                "su", "-c",
                                "cat /data/adb/modules/SetoSkins/配置.prop | grep '^最大电流数='"
                        });
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(p.getInputStream())
                        );
                        String line = reader.readLine();
                        if (line != null && line.contains("=")) {
                            currentValue = line.substring(line.indexOf('=') + 1);
                        }
                        reader.close();
                        p.waitFor();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // 2) 弹框并预填
                    MiuiAlertDialog dialog = new MiuiAlertDialog(getContext())
                            .setTitle("最大电流数")
                            .setMessage("22A=22000mA=22000000")
                            .setEnableEditTextView(true)
                            .setEditText(currentValue, new DialogInterface.TextWatcher() {
                                @Override
                                public void onResult(DialogInterface d, CharSequence input) {
                                    String value = input.toString().trim();
                                    if (!value.isEmpty()) {
                                        try {
                                            // 转换成数字，判断是否小于200000
                                            long newValue = Long.parseLong(value);
                                            if (newValue < 2000000) {
                                                // 小于200000时自动填充200000
                                                value = "22000000";
                                                Toast.makeText(getContext(), "已修正，请输入2000000以上的数字", Toast.LENGTH_SHORT).show();
                                            }

                                            // 执行更新命令
                                            String cmd = "sed -i 's/^最大电流数=.*/最大电流数=" + value + "/' /data/adb/modules/SetoSkins/配置.prop";
                                            Runtime.getRuntime().exec(new String[]{"su", "-c", cmd}).waitFor();
                                            Toast.makeText(getContext(), "最大电流数已更新为: " + value, Toast.LENGTH_SHORT).show();
                                        } catch (NumberFormatException e) {
                                            Toast.makeText(getContext(), "无效的数字输入", Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Toast.makeText(getContext(), "更新失败，请检查权限", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(getContext(), "请输入有效的电流数", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setPositiveButton("确定", null)  // 设置为null，不关闭对话框
                            .setNegativeButton("取消", null);
                    dialog.show();
                    return true;
                });
            }

        }
    }


    abstract public int id();

    public void initPrefs() {
    }

    public void showMiuiDialogForPreference(@NonNull Preference preference) {
        MiuiAlertDialog miuiAlertDialog = new MiuiAlertDialog(getContext())
                .setTitle("最大电流数")
                .setMessage("22A=22000mA=22000000")
                .setHapticFeedbackEnabled(true)
                .setCanceledOnTouchOutside(false)
                .setPositiveButton("确定", null)
                .setNegativeButton("拒绝", null);

        switch (preference.getKey()) {
            case "prefs_dialog" -> miuiAlertDialog.show();

            case "prefs_edit_dialog" -> miuiAlertDialog
                    .setEnableEditTextView(true)
                    .setEditText("", new DialogInterface.TextWatcher() {
                        @Override
                        public void onResult(DialogInterface dialog, CharSequence s) {
                            Toast.makeText(getContext(), "你输入了: " + s, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();

            case "prefs_list_dialog" -> miuiAlertDialog
                    .setEnableListSelectView(true)
                    .setEnableMultiSelect(true)
                    .setEnableListSpringBack(true)
                    .setItems(new CharSequence[]{
                            "煥晨0", "小煥晨0", "小煥晨1", "大煥晨0", "大煥晨1", "大煥晨2", "大煥晨3", "大煥晨4",
                            "大煥晨5", "大煥晨6", "大煥晨7", "大煥晨8", "大煥晨9"
                    }, new DialogInterface.OnItemsClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, CharSequence item, int which) {
                        }

                        @Override
                        public void onResult(DialogInterface dialog, ArrayList<CharSequence> items, ArrayList<CharSequence> selectedItems) {
                            Toast.makeText(getContext(), "结果: " + selectedItems, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();

            case "prefs_view_dialog" -> miuiAlertDialog
                    .setEnableCustomView(true)
                    .setCustomView(R.layout.custom_view, new DialogInterface.OnBindView() {
                        @Override
                        public void onBindView(ViewGroup root, View view) {
                            ImageView imageView = view.findViewById(R.id.image);
                            imageView.setOnClickListener(v -> {
                                v.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                                Toast.makeText(getContext(), "是煥晨！", Toast.LENGTH_SHORT).show();
                            });
                        }
                    })
                    .show();
        }
    }

    public static class PrefsHome extends BasePreferenceFragment {

        @Override
        public int id() {
            return R.xml.prefs_home;
        }

        @Override
        public void initPrefs() {
            Handler handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    if (msg.what == 1) {
                    }
                }
            };

            MiuiSwitchPreference miuiSwitchPreference = findPreference("pref_child");
            MiuiPreference intentPreference = findPreference("pref_intent");

            miuiSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                Message message = new Message();
                message.what = 1;
                message.obj = new Object[]{preference, newValue};
                handler.removeMessages(1);
                handler.sendMessageDelayed(message, 0);
                return true;
            });

            intentPreference.setOnPreferenceClickListener(preference -> {
                Toast.makeText(getContext(), "你点击了我！", Toast.LENGTH_SHORT).show();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    ((MiuiPreference) preference).setTipText("哟卡哟卡！");
                }, 500);
                return true;
            });

            String[] dialogKeys = {"prefs_dialog", "prefs_edit_dialog", "prefs_list_dialog", "prefs_view_dialog"};
            for (String key : dialogKeys) {
                Preference dialogPref = findPreference(key);
                if (dialogPref != null) {
                    dialogPref.setOnPreferenceClickListener(preference -> {
                        showMiuiDialogForPreference(preference);
                        return true;
                    });
                }
            }
        }
    }

    public static class PrefsSettings extends BasePreferenceFragment implements Preference.OnPreferenceClickListener {

        @Override
        public int id() {
            return R.xml.prefs_settings;
        }

        @Override
        public void initPrefs() {
            String[] keys = {"prefs_dialog", "prefs_edit_dialog", "prefs_list_dialog", "prefs_view_dialog"};
            for (String key : keys) {
                Preference p = findPreference(key);
                if (p != null) p.setOnPreferenceClickListener(this);
            }
        }

        @Override
        public boolean onPreferenceClick(@NonNull Preference preference) {
            showMiuiDialogForPreference(preference);
            return true;
        }
    }

    public static class PrefsAbout extends BasePreferenceFragment {

        @Override
        public int id() {
            return R.xml.prefs_about;
        }

        @Override
        public void initPrefs() {
            MiuiCardPreference miuiCardPreference2 = findPreference("prefs_card2");
            MiuiCardPreference miuiCardPreference3 = findPreference("prefs_card3");

            if (miuiCardPreference2 != null) {
                miuiCardPreference2.setCustomViewCallBack(view -> {
                    view.setOnClickListener(v -> {
                        // 跳转到 prefs_card2 对应的网页
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com"));
                        v.getContext().startActivity(intent);
                    });
                });
            }

            if (miuiCardPreference3 != null) {
                miuiCardPreference3.setCustomViewCallBack(view -> {
                    view.setOnClickListener(v -> {
                        // 跳转到 prefs_card3 对应的网页
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://example2.com"));
                        v.getContext().startActivity(intent);
                    });
                });
            }

        }

    }
}
