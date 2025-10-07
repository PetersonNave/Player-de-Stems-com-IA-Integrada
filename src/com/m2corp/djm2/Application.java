package com.m2corp.djm2;

import com.m2corp.djm2.model.DJTable;
import com.m2corp.djm2.view.DJView;
import javax.swing.*;

public class Application {
    public static void main(String[] args) {
        DJTable table = new DJTable();
        DJView view = new DJView(table);
        SwingUtilities.invokeLater(view::show);
    }
}