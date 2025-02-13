package com.cocos.module_asset.ui.transfer;

import android.app.Application;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;

import com.cocos.bcx_sdk.bcx_api.CocosBcxApiWrapper;
import com.cocos.bcx_sdk.bcx_callback.IBcxCallBack;
import com.cocos.library_base.base.BaseViewModel;
import com.cocos.library_base.binding.command.BindingAction;
import com.cocos.library_base.binding.command.BindingCommand;
import com.cocos.library_base.entity.AssetBalanceModel;
import com.cocos.library_base.entity.AssetsModel;
import com.cocos.library_base.utils.Utils;
import com.cocos.library_base.utils.singleton.GsonSingleInstance;
import com.cocos.module_asset.R;

import java.math.BigDecimal;

/**
 * @author ningkang.guo
 * @Date 2019/2/18
 */
public class TransferViewModel extends BaseViewModel {


    public BigDecimal balance;
    private AssetsModel.AssetModel assetModel;

    public TransferViewModel(@NonNull Application application) {
        super(application);
    }


    //收款方帐户名的绑定
    public ObservableField<String> receivablesAccountName = new ObservableField<>();

    //帐户余额的绑定
    public ObservableField<String> accountBalance = new ObservableField<>();

    public ObservableField<String> symbolType = new ObservableField<>(Utils.getString(R.string.module_asset_coin_type_test));

    //转账数量的绑定
    public ObservableField<String> transferAmount = new ObservableField<>();

    //转账备注的绑定
    public ObservableField<String> transferMemo = new ObservableField<>("");

    //封装一个界面发生改变的观察者
    public UIChangeObservable uc = new UIChangeObservable();

    public void setTransferAssetModel(AssetsModel.AssetModel assetModel) {
        this.assetModel = assetModel;
    }


    public class UIChangeObservable {
        public ObservableBoolean transferBtnObservable = new ObservableBoolean(false);
        public ObservableBoolean toContact = new ObservableBoolean(false);
        public ObservableBoolean toCaptureActivity = new ObservableBoolean(false);
    }

    //返回按钮事件
    public BindingCommand backOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            finish();
        }
    });

    //扫描按钮
    public BindingCommand scanOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.toCaptureActivity.set(!uc.toCaptureActivity.get());
        }
    });

    //联系人按钮
    public BindingCommand contactOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.toContact.set(!uc.toContact.get());
        }
    });

    //全部按钮
    public BindingCommand transferAllOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            transferAmount.set(String.valueOf(balance));
        }
    });

    //转账按钮
    public BindingCommand transferNextOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.transferBtnObservable.set(!uc.transferBtnObservable.get());
        }
    });

    /**
     * 计算余额
     *
     * @param accountId
     */
    public void setAccountBalance(String accountId) {
        CocosBcxApiWrapper.getBcxInstance().get_account_balances(accountId, assetModel.id, new IBcxCallBack() {
            @Override
            public void onReceiveValue(final String s) {
                AssetBalanceModel balanceEntity = GsonSingleInstance.getGsonInstance().fromJson(s, AssetBalanceModel.class);
                if (!balanceEntity.isSuccess()) {
                    return;
                }
                final AssetBalanceModel.DataBean dataBean = balanceEntity.data;
                balance = dataBean.amount;
                accountBalance.set(Utils.getString(R.string.module_asset_account_balance_text) + balance.add(BigDecimal.ZERO) + assetModel.symbol);
            }
        });
    }
}
