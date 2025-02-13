package com.cocos.bcx_wallet.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.cocos.bcx_wallet.BR;
import com.cocos.bcx_wallet.R;
import com.cocos.bcx_wallet.adapter.MainViewPagerAdapter;
import com.cocos.bcx_wallet.databinding.ActivityMainBinding;
import com.cocos.library_base.base.BaseActivity;
import com.cocos.library_base.router.RouterActivityPath;
import com.cocos.library_base.utils.ActivityContainer;
import com.cocos.library_base.utils.StatusBarUtils;
import com.cocos.library_base.utils.VersionUtil;
import com.cocos.module_asset.ui.asset.AssetFragment;
import com.cocos.module_found.fragment.FoundFragment;
import com.cocos.module_mine.mine_fragment.MineFragment;

import java.util.ArrayList;

/**
 * @author ningkang.guo
 * @Date 2019/1/29
 */
@Route(path = RouterActivityPath.ACTIVITY_MAIN_PATH)
public class MainActivity extends BaseActivity<ActivityMainBinding, MainViewModel> implements View.OnClickListener {

    private long mExitTime;

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_main;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initData() {
        //初始化Fragment
        initFragment();
        binding.assetRb.setOnClickListener(this);
        binding.foundRb.setOnClickListener(this);
        binding.mineRb.setOnClickListener(this);
    }

    private void initFragment() {
        ArrayList mFragments = new ArrayList<>();
        mFragments.add(new AssetFragment());
        mFragments.add(new FoundFragment());
        mFragments.add(new MineFragment());
        MainViewPagerAdapter mainVPAdapter = new MainViewPagerAdapter(getSupportFragmentManager(), mFragments);
        binding.vpMain.setAdapter(mainVPAdapter);
        binding.vpMain.setOffscreenPageLimit(mFragments.size());
        StatusBarUtils.with(MainActivity.this).init();
        VersionUtil.updateVersion(viewModel, MainActivity.this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.asset_rb:
                StatusBarUtils.with(MainActivity.this).init();
                binding.vpMain.setCurrentItem(0, false);
                break;
            case R.id.found_rb:
                initWidow();
                binding.vpMain.setCurrentItem(1, false);
                break;
            case R.id.mine_rb:
                initWidow();
                binding.vpMain.setCurrentItem(2, false);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.system_exit), Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            ActivityContainer.finishAllActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10086) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                boolean hasInstallPermission = getPackageManager().canRequestPackageInstalls();
                if (hasInstallPermission && !TextUtils.isEmpty(VersionUtil.getDestFileDir(MainActivity.this))) {
                    VersionUtil.installApkO(MainActivity.this, VersionUtil.getDestFileDir(MainActivity.this));
                }
            }
        }
    }


}
