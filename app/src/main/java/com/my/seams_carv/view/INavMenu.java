package com.my.seams_carv.view;

public interface INavMenu {
    void setOnMenuStateChangeListener(OnMenuStateChange listener);

    interface OnMenuStateChange {
        void onShowMenu();

        void onHideMenu();
    }
}
