
package com.vaadin.starter.bakery.ui.views.orderedit;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class OrderHistoryDesign extends Panel {

    protected CssLayout items;
    protected TextField newCommentInput;
    protected Button commitNewComment;

    public final void init() {
        setCaption("History");
        setStyleName("well");
        VerticalLayout historyLayout = new VerticalLayout();
        historyLayout.setStyleName("history");
        historyLayout.setMargin(true);
        items = new CssLayout();
        items.setId("items");
        items.setWidth("100%");
        items.setId("items");
        historyLayout.addComponent(items);
        historyLayout.setComponentAlignment(items, Alignment.TOP_LEFT);
        HorizontalLayout commentLayout = createCommentLayout();
        historyLayout.addComponent(commentLayout);
        historyLayout.setComponentAlignment(commentLayout,
                Alignment.TOP_LEFT);
        this.setContent(historyLayout);
    }

    private HorizontalLayout createCommentLayout() {
        HorizontalLayout commentLayout = new HorizontalLayout();
        commentLayout.setSpacing(false);
        commentLayout.setWidth("100%");
        commentLayout.setMargin(false);

        newCommentInput = new TextField();
        newCommentInput.setId("newCommentInput");
        newCommentInput.setPlaceholder("Message");
        newCommentInput.setWidth("100%");
        newCommentInput.setId("newCommentInput");
        commentLayout.addComponent(newCommentInput);
        commentLayout.setComponentAlignment(newCommentInput,
                Alignment.TOP_LEFT);
        commentLayout.setExpandRatio(newCommentInput, 1.0F);

        commitNewComment = new Button();
        commitNewComment.setIcon(VaadinIcons.ENTER_ARROW);
        commitNewComment.setStyleName("quiet");
        commitNewComment.setId("commitNewComment");
        commitNewComment.setCaption("");
        commitNewComment.setId("commitNewComment");
        commentLayout.addComponent(commitNewComment);
        commentLayout.setComponentAlignment(commitNewComment,
                Alignment.TOP_LEFT);

        return commentLayout;
    }

}
