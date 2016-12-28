package com.v5kf.client.ui.keyboard;


public interface IView {
    void onItemClick(EmoticonBean bean);
    void onItemDisplay(EmoticonBean bean);
    void onPageChangeTo(int position);
}
