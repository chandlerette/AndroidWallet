package com.cocos.module_mine.account_manage;

import android.databinding.Observable;
import android.os.Bundle;
import android.util.Log;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.cocos.bcx_sdk.bcx_api.CocosBcxApiWrapper;
import com.cocos.bcx_sdk.bcx_callback.IBcxCallBack;
import com.cocos.library_base.base.BaseActivity;
import com.cocos.library_base.entity.PrivateKeyModel;
import com.cocos.library_base.global.IntentKeyGlobal;
import com.cocos.library_base.router.RouterActivityPath;
import com.cocos.library_base.utils.StatusBarUtils;
import com.cocos.library_base.utils.ToastUtils;
import com.cocos.library_base.utils.Utils;
import com.cocos.library_base.utils.singleton.GsonSingleInstance;
import com.cocos.library_base.utils.singleton.MainHandler;
import com.cocos.library_base.base.BaseVerifyPasswordDialog;
import com.cocos.library_base.widget.DialogFragment;
import com.cocos.module_mine.BR;
import com.cocos.module_mine.R;
import com.cocos.module_mine.databinding.ActivityAccountManageBinding;

/**
 * @author ningkang.guo
 * @Date 2019/2/20
 */
@Route(path = RouterActivityPath.ACTIVITY_ACCOUNT_MANAGE)
public class AccountManageActivity extends BaseActivity<ActivityAccountManageBinding, AccountManageViewModel> {

    private String accountName;

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_account_manage;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initParam() {
        try {
            StatusBarUtils.with(AccountManageActivity.this).init();
            accountName = (String) getIntent().getExtras().get(IntentKeyGlobal.ACCOUNT_NAME);
        } catch (Exception e) {
        }
    }

    @Override
    public void initData() {
        try {
            viewModel.setAccountName(accountName);
            String accountId = CocosBcxApiWrapper.getBcxInstance().get_account_id_by_name(accountName);
            viewModel.requestAccountManagerData(accountId);
        } catch (Exception e) {
        }
    }

    @Override
    public void initViewObservable() {
        viewModel.uc.logOutObservable.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                DialogFragment dialogFragment = new DialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString(IntentKeyGlobal.DIALOG_CONTENT, Utils.getString(R.string.logout_warning_text));
                bundle.putInt(IntentKeyGlobal.DIALOG_TYPE, 1);
                bundle.putString(IntentKeyGlobal.ACCOUNT_NAME, accountName);
                dialogFragment.setArguments(bundle);
                dialogFragment.show(getSupportFragmentManager(), "dialogFragment");
            }
        });

        viewModel.uc.exportKeyObservable.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                final BaseVerifyPasswordDialog passwordVerifyDialog = new BaseVerifyPasswordDialog();
                passwordVerifyDialog.show(getSupportFragmentManager(), "passwordVerifyDialog");
                passwordVerifyDialog.setPasswordListener(new BaseVerifyPasswordDialog.IPasswordListener() {
                    @Override
                    public void onFinish(String password) {
                        CocosBcxApiWrapper.getBcxInstance().export_private_key(accountName, password, new IBcxCallBack() {
                            @Override
                            public void onReceiveValue(final String private_key) {
                                Log.d("export_private_key", private_key);
                                MainHandler.getInstance().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        final PrivateKeyModel keyModel = GsonSingleInstance.getGsonInstance().fromJson(private_key, PrivateKeyModel.class);
                                        if (keyModel.code == 105) {
                                            ToastUtils.showShort(R.string.module_mine_wrong_password);
                                            return;
                                        }
                                        if (!keyModel.isSuccess()) {
                                            return;
                                        }
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable(IntentKeyGlobal.KEY_MODEL, keyModel);
                                        ARouter.getInstance().build(RouterActivityPath.ACTIVITY_KEY_EXPORT).with(bundle).navigation();
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void cancel() {

                    }
                });

            }
        });
    }
}
