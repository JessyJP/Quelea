/*
 * This file is part of Quelea, free projection software for churches.
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.main.schedule;

import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.Displayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.LivePanel;
import org.quelea.windows.main.PreviewPanel;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.actionhandlers.RemoveScheduleItemActionHandler;

/**
 * The panel displaying the schedule / order of service. Items from here are
 * loaded into the preview panel where they are viewed and then projected live.
 * Items can be added here from the library.
 * <p/>
 *
 * @author Michael
 */
public class SchedulePanel extends BorderPane {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final ScheduleList scheduleList;
    private final Button removeButton;
    private final Button upButton;
    private final Button downButton;
    private final Button themeButton;
    private final ScheduleThemeNode scheduleThemeNode;
    private Stage themePopup;

    /**
     * Create and initialise the schedule panel.
     */
    public SchedulePanel() {
        boolean darkTheme = QueleaProperties.get().getUseDarkTheme();
        ImageView themeButtonIcon = new ImageView(new Image("file:icons/theme.png"));
        themeButtonIcon.setFitWidth(16);
        themeButtonIcon.setFitHeight(16);
        themeButton = new Button("", themeButtonIcon);
        themeButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("theme.button.tooltip")));
        scheduleList = new ScheduleList();
        scheduleList.itemsProperty().get().addListener(new ListChangeListener<Displayable>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Displayable> change) {
                scheduleThemeNode.updateTheme();
            }
        });

        themePopup = new Stage();
        themePopup.setTitle(LabelGrabber.INSTANCE.getLabel("theme.select.text"));
        Utils.addIconsToStage(themePopup);
        themePopup.initStyle(StageStyle.UNDECORATED);
        themePopup.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if (t && !t1) {
                    if (Utils.isMac()) {
                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                themePopup.hide();
                            }
                        });

                    } else {
                        themePopup.hide();
                    }
                }
            }
        });

        scheduleThemeNode = new ScheduleThemeNode(this::updateSongTheme, this::updateBibleTheme, themePopup, themeButton);
        scheduleThemeNode.setStyle("-fx-background-color:WHITE;-fx-border-color: rgb(49, 89, 23);-fx-border-radius: 5;");
        Scene scene = new Scene(scheduleThemeNode);
        if (darkTheme) {
            scene.getStylesheets().add("org/modena_dark.css");
        }
        themePopup.setScene(scene);

        themeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (themePopup.isShowing()) {
                    //fixes a JVM crash
                    if (Utils.isMac()) {
                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                themePopup.hide();
                            }
                        });
                    } else {
                        themePopup.hide();
                    }

                } else {
                    themePopup.setX(themeButton.localToScene(0, 0).getX() + QueleaApp.get().getMainWindow().getX());
                    themePopup.setY(themeButton.localToScene(0, 0).getY() + 45 + QueleaApp.get().getMainWindow().getY());
                    themePopup.show();
                }
            }
        });
//        themeButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("adjust.theme.tooltip")));

        //Needed to initialise theme preview. Without this calls to the theme thumbnail return a blank image
        //before hte theme popup is opened for the first time. TODO: Find a better way of doing this.
        themePopup.show();
        Platform.runLater(() -> themePopup.hide());

        ToolBar toolbar = new ToolBar();
        toolbar.setOrientation(Orientation.VERTICAL);
        ImageView removeIV = new ImageView(new Image(darkTheme ? "file:icons/cross-light.png" : "file:icons/cross.png"));
        removeIV.setFitWidth(16);
        removeIV.setFitHeight(16);
        removeButton = new Button("", removeIV);
        Utils.setToolbarButtonStyle(removeButton);
        removeButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("remove.song.schedule.tooltip")));
        removeButton.setDisable(true);
        removeButton.setOnAction(new RemoveScheduleItemActionHandler());

        ImageView upIV = new ImageView(new Image(darkTheme ? "file:icons/up-light.png" : "file:icons/up.png"));
        upIV.setFitWidth(16);
        upIV.setFitHeight(16);
        upButton = new Button("", upIV);
        Utils.setToolbarButtonStyle(upButton);
        upButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("move.up.schedule.tooltip")));
        upButton.setDisable(true);
        upButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                scheduleList.moveCurrentItem(ScheduleList.Direction.UP);
            }
        });

        ImageView downIV = new ImageView(new Image(darkTheme ? "file:icons/down-light.png" : "file:icons/down.png"));
        downIV.setFitWidth(16);
        downIV.setFitHeight(16);
        downButton = new Button("", downIV);
        Utils.setToolbarButtonStyle(downButton);
        downButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("move.down.schedule.tooltip")));
        downButton.setDisable(true);
        downButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                scheduleList.moveCurrentItem(ScheduleList.Direction.DOWN);
            }
        });

        scheduleList.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                updateScheduleDisplay();
            }
        });
        scheduleList.getListView().focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) updateScheduleDisplay();
        });

        ToolBar header = new ToolBar();
        Label headerLabel = new Label(LabelGrabber.INSTANCE.getLabel("order.service.heading"));
        headerLabel.setStyle("-fx-font-weight: bold;");
        header.getItems().add(headerLabel);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getItems().add(spacer);
        header.getItems().add(themeButton);

        toolbar.getItems().add(removeButton);
        toolbar.getItems().add(upButton);
        toolbar.getItems().add(downButton);

        setTop(header);
        setLeft(toolbar);
        setCenter(scheduleList);
    }

    private void updateSongTheme(ThemeDTO theme) {
        QueleaApp.get().getMainWindow().getGlobalThemeStore().setSongThemeOverride(theme);
    }

    private void updateBibleTheme(ThemeDTO theme) {
        QueleaApp.get().getMainWindow().getGlobalThemeStore().setBibleThemeOverride(theme);
    }

    public void updateScheduleDisplay() {
        if (scheduleList.getItems().isEmpty()) {
            removeButton.setDisable(true);
            upButton.setDisable(true);
            downButton.setDisable(true);
        } else {
            removeButton.setDisable(false);
            upButton.setDisable(false);
            downButton.setDisable(false);
            QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().setDisplayable(scheduleList.getSelectionModel().getSelectedItem(), 0);
        }
    }

    /**
     * Get the schedule list backing this panel.
     * <p/>
     *
     * @return the schedule list.
     */
    public ScheduleList getScheduleList() {
        return scheduleList;
    }

    public Button getThemeButton() {
        return themeButton;
    }

    public ScheduleThemeNode getThemeNode() {
        return scheduleThemeNode;
    }

    public Stage getThemePopup() {
        return themePopup;
    }

    /**
     * Set the last added item as live in the schedule.
     */
    public void goLiveWithLastAddedItem(int index) {
        int lastItemIndex = this.getScheduleList().getItems().size() - 1;
        if (lastItemIndex >= 0) {
//            previewPanel.setDisplayable(schedulePanel.getScheduleList().getItems().get(lastIndex), lastIndex);
            this.getScheduleList().getSelectionModel().select(lastItemIndex);
        }
        if (index > 0) {
            PreviewPanel previewPanel = QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel();
            LivePanel livePanel = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel();
            if (previewPanel.isVisible) {
                previewPanel.setDisplayable(previewPanel.getDisplayable(), index);
            } else{
                livePanel.setDisplayable(this.getScheduleList().getSelectionModel().getSelectedItem(), index);
            }
        }
    }
}
