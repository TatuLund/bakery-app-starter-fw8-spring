
package com.vaadin.starter.bakery.ui.views.dashboard;

import com.vaadin.board.Board;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings({ "java:S2160", "java:S110" })
public class DashboardViewDesign extends VerticalLayout {

    protected Board board;

    public final void init() {
        setStyleName("dashboard-view");
        setResponsive(true);
        setWidth("100%");
        setHeight("100%");
        setMargin(false);
        board = new Board();
        board.setWidth("100%");
        board.setHeight("100%");
        board.setId("board");
        addComponent(board);
        setComponentAlignment(board, Alignment.TOP_LEFT);
        setExpandRatio(board, 1.0F);
    }

}
