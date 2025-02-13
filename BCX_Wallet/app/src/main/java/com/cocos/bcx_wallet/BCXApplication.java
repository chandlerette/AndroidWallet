package com.cocos.bcx_wallet;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.cocos.bcx_sdk.bcx_api.CocosBcxApiWrapper;
import com.cocos.bcx_sdk.bcx_callback.IBcxCallBack;
import com.cocos.bcx_wallet.launch.WelcomeActivity;
import com.cocos.library_base.base.BaseApplication;
import com.cocos.library_base.config.ModuleLifecycleConfig;
import com.cocos.library_base.crash.CaocConfig;
import com.cocos.library_base.crash.DefaultErrorActivity;
import com.cocos.library_base.entity.BaseResult;
import com.cocos.library_base.entity.NodeInfoModel;
import com.cocos.library_base.global.SPKeyGlobal;
import com.cocos.library_base.http.api.BaseUrlApi;
import com.cocos.library_base.http.callback.BaseObserver;
import com.cocos.library_base.http.http.HttpMethods;
import com.cocos.library_base.utils.KLog;
import com.cocos.library_base.utils.SPUtils;
import com.cocos.library_base.utils.Utils;
import com.cocos.library_base.utils.multi_language.LocalManageUtil;
import com.cocos.library_base.utils.singleton.GsonSingleInstance;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;


/**
 * @author ningkang.guo
 * @Date 2019/1/28
 */
public class BCXApplication extends BaseApplication {


    @Override
    protected void attachBaseContext(Context base) {
        //保存系统选择语言
        LocalManageUtil.saveSystemCurrentLanguage(base);
        super.attachBaseContext(base);
    }

    /**
     * 改变系统语言时会调用到
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //保存系统选择语言
        LocalManageUtil.onConfigurationChanged(getApplicationContext());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //是否开启日志打印
        KLog.init(BuildConfig.IS_TEST_ENV);
        //初始化 ARouter
        if (BuildConfig.DEBUG) { //开启InstantRun之后，一定要在ARouter.init之前调用openDebug
            ARouter.openDebug();
            ARouter.openLog();
        }
        ARouter.init(this);
        ModuleLifecycleConfig.getInstance().initModuleAhead(this); //初始化组件(靠前)
        ModuleLifecycleConfig.getInstance().initModuleLow(this);     //初始化组件(靠后)
        // 初始化sdk
        CocosBcxApiWrapper.getBcxInstance().init(this);
        requestNodeListData();
        //配置全局异常崩溃操作
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
                .enabled(true) //是否启动全局异常捕获
                .showRestartButton(true) //是否显示重启按钮
                .showErrorDetails(BuildConfig.IS_TEST_ENV)
                .trackActivities(true) //是否跟踪Activity
                .restartActivity(WelcomeActivity.class) //重新启动后的activity
                .errorActivity(DefaultErrorActivity.class) //崩溃后的错误activity
                .apply();
    }


    /**
     * 请求并加载节点数据
     */
    public void requestNodeListData() {
        try {
            Observable<NodeInfoModel> observable = BaseUrlApi.getApiBaseService().getNodeInfo();
            HttpMethods.toSubscribe(observable, new BaseObserver<NodeInfoModel>() {
                @Override
                protected void onBaseNext(NodeInfoModel data) {
                    NodeInfoModel.DataBean selectedNodeModel = SPUtils.getObject(Utils.getContext(), SPKeyGlobal.NODE_WORK_MODEL_SELECTED);
                    if (data.status != 200) {
                        onErrorInit(selectedNodeModel);
                        return;
                    }
                    for (NodeInfoModel.DataBean dataBean : data.data) {
                        // 之前无选中的节点
                        if (null == selectedNodeModel) {
                            init(dataBean, s -> {
                                BaseResult resultEntity = GsonSingleInstance.getGsonInstance().fromJson(s, BaseResult.class);
                                if (resultEntity.isSuccess()) {
                                    SPUtils.putObject(BCXApplication.this, SPKeyGlobal.NODE_WORK_MODEL_SELECTED, dataBean);
                                }
                            });
                            return;
                        }
                        // 之前有选中的节点
                        if (TextUtils.equals(selectedNodeModel.toString(), dataBean.toString())) {
                            init(selectedNodeModel, s -> {
                                BaseResult resultEntity = GsonSingleInstance.getGsonInstance().fromJson(s, BaseResult.class);
                                resultEntity.isSuccess();
                            });
                            return;
                        }
                        //之前选中的节点不为空并且不等于请求到的节点
                        init(dataBean, s -> {
                            BaseResult resultEntity = GsonSingleInstance.getGsonInstance().fromJson(s, BaseResult.class);
                            resultEntity.isSuccess();
                        });
                        return;
                    }
                }

                @Override
                protected void onBaseError(Throwable t) {
                    NodeInfoModel.DataBean selectedNodeModel = SPUtils.getObject(Utils.getContext(), SPKeyGlobal.NODE_WORK_MODEL_SELECTED);
                    onErrorInit(selectedNodeModel);
                }
            });
        } catch (Exception e) {
            NodeInfoModel.DataBean selectedNodeModel = SPUtils.getObject(Utils.getContext(), SPKeyGlobal.NODE_WORK_MODEL_SELECTED);
            onErrorInit(selectedNodeModel);
        }
    }

    /**
     * 连接错误/失败/出现异常时的初始化方法
     */
    private void onErrorInit(NodeInfoModel.DataBean selectedNodeModel) {
        if (null == selectedNodeModel) {
            init(new NodeInfoModel.DataBean(), s -> {
                BaseResult resultEntity = GsonSingleInstance.getGsonInstance().fromJson(s, BaseResult.class);
                resultEntity.isSuccess();
            });
            return;
        }
        init(selectedNodeModel, s -> {
            BaseResult resultEntity = GsonSingleInstance.getGsonInstance().fromJson(s, BaseResult.class);
            resultEntity.isSuccess();
        });
    }


    /**
     * 切换节点
     */
    private void init(final NodeInfoModel.DataBean dataBean, IBcxCallBack iBcxCallBack) {
        // 初始化bcx节点连接
        List<String> nodeUrls = new ArrayList<>();
        nodeUrls.add(dataBean.ws);
        nodeUrls.add(dataBean.ws);
        nodeUrls.add(dataBean.ws);
        CocosBcxApiWrapper.getBcxInstance().connect(this, dataBean.chainId, nodeUrls, dataBean.faucetUrl, dataBean.coreAsset, true, iBcxCallBack);
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        ARouter.getInstance().destroy();
    }
}
