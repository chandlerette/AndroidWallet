package com.cocos.library_base.entity;

import java.util.List;

/**
 * @author ningkang.guo
 * @Date 2019/5/15
 */
public class AccountNhAssetModel {

    public int code;
    public String message;
    public List<DataBean> data;

    public static class DataBean {
        public String id;
        public String nh_hash;
        public String nh_asset_creator;
        public String nh_asset_owner;
        public String nh_asset_active;
        public String authority_account;
        public String asset_qualifier;
        public String world_view;
        public String base_describe;
        public String create_time;
        public String limit_type;
        public List<Object> parent;
        public List<Object> child;
        public List<Object> describe_with_contract;
        public List<Object> limit_list;
    }
}