package com.zzg.mybatis.generator.controller;

import com.zzg.mybatis.generator.bridge.MybatisGeneratorBridge;
import com.zzg.mybatis.generator.model.DatabaseConfig;
import com.zzg.mybatis.generator.model.GeneratorConfig;
import com.zzg.mybatis.generator.model.UITableColumnVO;
import com.zzg.mybatis.generator.util.ConfigHelper;
import com.zzg.mybatis.generator.util.DbUtil;
import com.zzg.mybatis.generator.util.MyStringUtils;
import com.zzg.mybatis.generator.view.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.config.ColumnOverride;
import org.mybatis.generator.config.IgnoredColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.sql.SQLRecoverableException;
import java.util.*;

public class MainUIController extends BaseFXController {

    private static final Logger _LOG = LoggerFactory.getLogger(MainUIController.class);
    private static final String FOLDER_NO_EXIST = "Partial of the directories does not exist, whether to create";
    // tool bar buttons
    @FXML
    private Label connectionLabel;
    @FXML
    private Label configsLabel;
    @FXML
    private TextField modelTargetPackage;
    @FXML
    private TextField mapperTargetPackage;
    @FXML
    private TextField daoTargetPackage;
    @FXML
    private TextField tableNameField;
    @FXML
    private TextField domainObjectNameField;
    @FXML
    private TextField generateKeysField;    //添加输入框
    @FXML
    private TextField modelTargetProject;
    @FXML
    private TextField mappingTargetProject;
    @FXML
    private TextField daoTargetProject;
    @FXML
    private TextField mapperName;
    @FXML
    private TextField projectFolderField;
    @FXML
    private CheckBox offsetLimitCheckBox;
    @FXML
    private CheckBox commentCheckBox;
    @FXML
    private CheckBox needToStringHashcodeEquals;
    @FXML
    private CheckBox annotationCheckBox;
    @FXML
    private CheckBox useActualColumnNamesCheckbox;
    @FXML
    private TextField javaTypeConverter;
    @FXML
    private CheckBox useExample;
    @FXML
    private CheckBox ignoreTableSchema;
    @FXML
    private TreeView<String> leftDBTree;
    @FXML
    private Label statusLabel;
    // Current selected databaseConfig
    private DatabaseConfig selectedDatabaseConfig;
    // Current selected tableName
    private String tableName;

    private List<IgnoredColumn> ignoredColumns;

    private List<ColumnOverride> columnOverrides;
    @FXML
    private ChoiceBox<String> encodingChoice;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ImageView dbImage = new ImageView("icons/computer.png");
        dbImage.setFitHeight(40);
        dbImage.setFitWidth(40);
        connectionLabel.setGraphic(dbImage);
        connectionLabel.setOnMouseClicked(event -> {
            DbConnectionController controller = (DbConnectionController) loadFXMLPage("New connection", FXMLPage.NEW_CONNECTION, false);
            controller.setMainUIController(this);
            controller.showDialogStage();
        });
        ImageView configImage = new ImageView("icons/config-list.png");
        configImage.setFitHeight(40);
        configImage.setFitWidth(40);
        configsLabel.setGraphic(configImage);
        configsLabel.setOnMouseClicked(event -> {
            GeneratorConfigController controller = (GeneratorConfigController) loadFXMLPage("Profile", FXMLPage.GENERATOR_CONFIG, false);
            controller.setMainUIController(this);
            controller.showDialogStage();
        });

        CheckBoxTreeItem<String> dbTreeItem = new CheckBoxTreeItem<>();
        dbTreeItem.setExpanded(true);
//        checkBoxTreeItem.setIndependent(true);
        leftDBTree.setShowRoot(false);
        leftDBTree.setRoot(dbTreeItem);
        Callback<TreeView<String>, TreeCell<String>> defaultCellFactory = CheckBoxTreeCell.forTreeView();

        leftDBTree.setCellFactory((TreeView<String> tv) -> {
            TreeCell<String> cell = defaultCellFactory.call(tv);
//            this.cell = cell;
            cell.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                int level = leftDBTree.getTreeItemLevel(cell.getTreeItem());
                TreeCell<String> treeCell = (TreeCell<String>) event.getSource();
                TreeItem<String> treeItem = treeCell.getTreeItem();
                if (event.getClickCount() == 1) {

                    if (level == 1) {
                        final ContextMenu contextMenu = new ContextMenu();
                        MenuItem item1 = new MenuItem("close connection");
                        item1.setOnAction(event1 -> treeItem.getChildren().clear());
                        MenuItem item2 = new MenuItem("edit connection");
                        item2.setOnAction(event1 -> {
                            DatabaseConfig selectedConfig = (DatabaseConfig) treeItem.getGraphic().getUserData();
                            DbConnectionController controller = (DbConnectionController) loadFXMLPage("Edit connection", FXMLPage.NEW_CONNECTION, false);
                            controller.setMainUIController(this);
                            controller.setConfig(selectedConfig);
                            controller.showDialogStage();
                        });
                        MenuItem item3 = new MenuItem("remove connection");
                        item3.setOnAction(event1 -> {
                            DatabaseConfig selectedConfig = (DatabaseConfig) treeItem.getGraphic().getUserData();
                            try {
                                ConfigHelper.deleteDatabaseConfig(selectedConfig);
                                this.loadLeftDBTree();
                            } catch (Exception e) {
                                AlertUtil.showErrorAlert("Delete connection failed! Reason: " + e.getMessage());
                            }
                        });
                        contextMenu.getItems().addAll(item1, item2, item3);
                        cell.setContextMenu(contextMenu);
                    }
                } else if (event.getClickCount() == 2) {
                    if (level == 1) {
                        treeItem.setExpanded(true);
                        System.out.println("index: " + leftDBTree.getSelectionModel().getSelectedIndex());
                        DatabaseConfig selectedConfig = (DatabaseConfig) treeItem.getGraphic().getUserData();
                        try {
                            List<String> tables = DbUtil.getTableNames(selectedConfig);
                            if (tables != null && tables.size() > 0) {
                                ObservableList<TreeItem<String>> children = cell.getTreeItem().getChildren();
                                children.clear();
                                for (String tableName : tables) {
                                    CheckBoxTreeItem<String> newTreeItem = new CheckBoxTreeItem<>();
                                    ImageView imageView = new ImageView("icons/table.png");
                                    imageView.setFitHeight(16);
                                    imageView.setFitWidth(16);
                                    newTreeItem.setGraphic(imageView);
                                    newTreeItem.setValue(tableName);
                                    children.add(newTreeItem);
                                }
                            }
                        } catch (SQLRecoverableException e) {
                            _LOG.error(e.getMessage(), e);
                            AlertUtil.showErrorAlert("connection timeout!");
                        } catch (Exception e) {
                            _LOG.error(e.getMessage(), e);
                            AlertUtil.showErrorAlert(e.getMessage());
                        }
                    } else if (level == 2) { // left DB tree level3
                        String tableName = treeCell.getTreeItem().getValue();
                        selectedDatabaseConfig = (DatabaseConfig) treeItem.getParent().getGraphic().getUserData();
                        if (!tableName.equals(this.tableName)) {
                            generateKeysField.clear();
                        }
                        this.tableName = tableName;
                        tableNameField.setText(tableName);
                        domainObjectNameField.setText(MyStringUtils.dbStringToCamelStyle(tableName));
                    }
                }
            });
            return cell;
        });
        loadLeftDBTree();

        if (encodingChoice != null) {
            encodingChoice.setItems(FXCollections.observableArrayList("UTF-8"));
            encodingChoice.setValue("UTF-8");
        }
    }

    void loadLeftDBTree() {
        TreeItem rootTreeItem = leftDBTree.getRoot();
        rootTreeItem.getChildren().clear();
        try {
            List<DatabaseConfig> dbConfigs = ConfigHelper.loadDatabaseConfig();
            for (DatabaseConfig dbConfig : dbConfigs) {
                CheckBoxTreeItem<String> dbTreeItem = new CheckBoxTreeItem<>();
                dbTreeItem.setValue(dbConfig.getName());
                ImageView dbImage = new ImageView("icons/database.png");
                dbImage.setFitHeight(16);
                dbImage.setFitWidth(16);
                dbImage.setUserData(dbConfig);
                dbTreeItem.setGraphic(dbImage);
                rootTreeItem.getChildren().add(dbTreeItem);

            }
        } catch (Exception e) {
            _LOG.error("connect db failed, reason: {}", e);
            AlertUtil.showErrorAlert(e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
        }
    }

    @FXML
    public void chooseProjectFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedFolder = directoryChooser.showDialog(getPrimaryStage());
        if (selectedFolder != null) {
            projectFolderField.setText(selectedFolder.getAbsolutePath());
        }
    }

    //    TreeCell<String> cell;
    @FXML
    public void generatorSelectedCode() {


        _LOG.info("generatorSelectedCode");

        ObservableList<TreeItem<String>> dbs = leftDBTree.getRoot().getChildren();

        // 支持多个库
        for (TreeItem dbTreeItem : dbs) {
            ObservableList<TreeItem<String>> children = dbTreeItem.getChildren();

            for (TreeItem treeItem : children) {
                CheckBoxTreeItem checkBoxTreeItem = (CheckBoxTreeItem) treeItem;
                if (checkBoxTreeItem.isSelected()) {
                    _LOG.info("process {}.{}", dbTreeItem.getValue(), treeItem.getValue());

                    selectedDatabaseConfig = (DatabaseConfig) treeItem.getParent().getGraphic().getUserData();

                    generateKeysField.clear();
                    this.tableName = treeItem.getValue().toString();

                    tableNameField.setText(tableName);
                    domainObjectNameField.setText(MyStringUtils.dbStringToCamelStyle(tableName));

                    generateCode(treeItem.getValue() + "");

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @FXML
    public void generateCode() {
        generateCode(this.tableName);
    }


    public void generateCode(String tableName) {
        _LOG.info("generateCode tableName: {}", tableName);

        if (tableName == null) {
            AlertUtil.showWarnAlert("please select table in the left first");
            return;
        }
        String result = validateConfig();
        if (result != null) {
            AlertUtil.showErrorAlert(result);
            return;
        }
        GeneratorConfig generatorConfig = getGeneratorConfigFromUI();
        if (!checkDirs(generatorConfig)) {
            return;
        }

        statusLabel.setText("[" + new Date() + "] " + "begin to generator " + tableName + "......");

        MybatisGeneratorBridge bridge = new MybatisGeneratorBridge();
        bridge.setGeneratorConfig(generatorConfig);
        bridge.setDatabaseConfig(selectedDatabaseConfig);

        // add by Benjamin 为ltms增加自动转换数据类型的处理

        if (StringUtils.isNotBlank(javaTypeConverter.getText())) {
            try {
                List<UITableColumnVO> tableColumns = DbUtil.getTableColumns(selectedDatabaseConfig, tableName);

                String className = this.javaTypeConverter.getText();
                Class typeConverterClass = this.getClass().getClassLoader().loadClass(className);

                JavaTypeConverter javaTypeConverter = (JavaTypeConverter) typeConverterClass.newInstance();
                javaTypeConverter.convert(tableColumns, tableName);
                this.columnOverrides = javaTypeConverter.getColumnOverrides();
                this.ignoredColumns = javaTypeConverter.getIgnoredColumns();

            } catch (Exception e) {
                _LOG.error(e.getMessage(), e);
                AlertUtil.showErrorAlert(e.getMessage());
            }
        }

        bridge.setColumnOverrides(columnOverrides);
        bridge.setIgnoredColumns(ignoredColumns);
//		UIProgressCallback alert = new UIProgressCallback(Alert.AlertType.INFORMATION);
//		bridge.setProgressCallback(alert);
//		alert.show();


        StatusLineCallBack statusLineCallBack = new StatusLineCallBack(statusLabel, tableName);
        bridge.setProgressCallback(statusLineCallBack);
        try {
            bridge.generate();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showErrorAlert(e.getMessage());
        }
    }

    private String validateConfig() {
        String projectFolder = projectFolderField.getText();
        if (StringUtils.isEmpty(projectFolder)) {
            return "project path is null";
        }
        if (StringUtils.isEmpty(domainObjectNameField.getText())) {
            return "class name is null";
        }
        if (StringUtils.isAnyEmpty(modelTargetPackage.getText(), mapperTargetPackage.getText(), daoTargetPackage.getText())) {
            return "package name is null";
        }

        return null;
    }

    @FXML
    public void saveGeneratorConfig() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("save profile");
        dialog.setContentText("please enter profile name");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String name = result.get();
            if (StringUtils.isEmpty(name)) {
                AlertUtil.showErrorAlert("name is null");
                return;
            }
            _LOG.info("user choose name: {}", name);
            try {
                GeneratorConfig generatorConfig = getGeneratorConfigFromUI();
                generatorConfig.setName(name);

                if (ConfigHelper.loadGeneratorConfig(name) != null) {
                    ConfigHelper.deleteGeneratorConfig(name);
                }
                ConfigHelper.saveGeneratorConfig(generatorConfig);

            } catch (Exception e) {
                AlertUtil.showErrorAlert("save profile[" + name + "] failed!");
            }
        }
    }

    public GeneratorConfig getGeneratorConfigFromUI() {
        GeneratorConfig generatorConfig = new GeneratorConfig();
        generatorConfig.setProjectFolder(projectFolderField.getText());
        generatorConfig.setModelPackage(modelTargetPackage.getText());
        generatorConfig.setGenerateKeys(generateKeysField.getText());
        generatorConfig.setModelPackageTargetFolder(modelTargetProject.getText());
        generatorConfig.setDaoPackage(daoTargetPackage.getText());
        generatorConfig.setDaoTargetFolder(daoTargetProject.getText());
        generatorConfig.setMapperName(mapperName.getText());
        generatorConfig.setMappingXMLPackage(mapperTargetPackage.getText());
        generatorConfig.setMappingXMLTargetFolder(mappingTargetProject.getText());
        generatorConfig.setTableName(tableNameField.getText());
        generatorConfig.setDomainObjectName(domainObjectNameField.getText());

        generatorConfig.setOffsetLimit(offsetLimitCheckBox.isSelected());
        generatorConfig.setComment(commentCheckBox.isSelected());
        generatorConfig.setNeedToStringHashcodeEquals(needToStringHashcodeEquals.isSelected());
        generatorConfig.setAnnotation(annotationCheckBox.isSelected());
        generatorConfig.setUseActualColumnNames(useActualColumnNamesCheckbox.isSelected());
        generatorConfig.setEncoding(encodingChoice.getValue());
        generatorConfig.setUseExample(useExample.isSelected());
        generatorConfig.setJavaTypeConverter(javaTypeConverter.getText());
        generatorConfig.setIgnoreTableSchema(ignoreTableSchema.isSelected());
        return generatorConfig;
    }

    public void setGeneratorConfigIntoUI(GeneratorConfig generatorConfig) {
        projectFolderField.setText(generatorConfig.getProjectFolder());
        modelTargetPackage.setText(generatorConfig.getModelPackage());
        generateKeysField.setText(generatorConfig.getGenerateKeys());
        modelTargetProject.setText(generatorConfig.getModelPackageTargetFolder());
        daoTargetPackage.setText(generatorConfig.getDaoPackage());
        daoTargetProject.setText(generatorConfig.getDaoTargetFolder());
        mapperTargetPackage.setText(generatorConfig.getMappingXMLPackage());
        mappingTargetProject.setText(generatorConfig.getMappingXMLTargetFolder());
        encodingChoice.setValue(generatorConfig.getEncoding());

        offsetLimitCheckBox.setSelected(generatorConfig.isOffsetLimit());
        commentCheckBox.setSelected(generatorConfig.isComment());
        needToStringHashcodeEquals.setSelected(generatorConfig.isNeedToStringHashcodeEquals());
        annotationCheckBox.setSelected(generatorConfig.isAnnotation());
        useActualColumnNamesCheckbox.setSelected(generatorConfig.isUseActualColumnNames());
        javaTypeConverter.setText(generatorConfig.getJavaTypeConverter());
        ignoreTableSchema.setSelected(generatorConfig.isIgnoreTableSchema());
    }

    @FXML
    public void openTableColumnCustomizationPage() {
        if (tableName == null) {
            AlertUtil.showWarnAlert("please select table in the left first");
            return;
        }
        SelectTableColumnController controller = (SelectTableColumnController) loadFXMLPage("Column Customization", FXMLPage.SELECT_TABLE_COLUMN, false);
        controller.setMainUIController(this);
        try {
            // If select same schema and another table, update table data
            if (!tableName.equals(controller.getTableName())) {
                List<UITableColumnVO> tableColumns = DbUtil.getTableColumns(selectedDatabaseConfig, tableName);
                controller.setColumnList(FXCollections.observableList(tableColumns));
                controller.setTableName(tableName);
            }
            controller.showDialogStage();
        } catch (Exception e) {
            _LOG.error(e.getMessage(), e);
            AlertUtil.showErrorAlert(e.getMessage());
        }
    }

    public void setIgnoredColumns(List<IgnoredColumn> ignoredColumns) {
        this.ignoredColumns = ignoredColumns;
    }

    public void setColumnOverrides(List<ColumnOverride> columnOverrides) {
        this.columnOverrides = columnOverrides;
    }

    /**
     * 检查并创建不存在的文件夹
     *
     * @return
     */
    private boolean checkDirs(GeneratorConfig config) {
        List<String> dirs = new ArrayList<>();
        dirs.add(config.getProjectFolder());
        dirs.add(FilenameUtils.normalize(config.getProjectFolder().concat("/").concat(config.getModelPackageTargetFolder())));
        dirs.add(FilenameUtils.normalize(config.getProjectFolder().concat("/").concat(config.getDaoTargetFolder())));
        dirs.add(FilenameUtils.normalize(config.getProjectFolder().concat("/").concat(config.getMappingXMLTargetFolder())));
        boolean haveNotExistFolder = false;
        for (String dir : dirs) {
            File file = new File(dir);
            if (!file.exists()) {
                haveNotExistFolder = true;
            }
        }
        if (haveNotExistFolder) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText(FOLDER_NO_EXIST);
            Optional<ButtonType> optional = alert.showAndWait();
            if (optional.isPresent()) {
                if (ButtonType.OK == optional.get()) {
                    try {
                        for (String dir : dirs) {
                            FileUtils.forceMkdir(new File(dir));
                        }
                        return true;
                    } catch (Exception e) {
                        AlertUtil.showErrorAlert("mkdir failed, please make sure path is not a file");
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    static class StatusLineCallBack implements ProgressCallback {

        Label statusLabel;

        String tableName;

        public StatusLineCallBack(Label statusLabel, String tableName) {
            this.statusLabel = statusLabel;
            this.tableName = tableName;
        }

        @Override
        public void introspectionStarted(int totalTasks) {
            statusLabel.setText(new Date() + " " + tableName + " introspection Started, totalTasks: " + totalTasks);
        }

        @Override
        public void generationStarted(int totalTasks) {
            statusLabel.setText("[" + new Date() + "] " + tableName + " generation Started, totalTasks: " + totalTasks);
        }

        @Override
        public void saveStarted(int totalTasks) {
//            _LOG.info("saveStarted");
            statusLabel.setText("[" + new Date() + "] " + tableName + " save Started, totalTasks: " + totalTasks);
        }

        @Override
        public void startTask(String taskName) {
//            _LOG.info("startTask");
            statusLabel.setText("[" + new Date() + "] " + "start Task for " + tableName);
        }

        @Override
        public void done() {
//            _LOG.info("done");
            statusLabel.setText("[" + new Date() + "] " + "generator done for " + tableName);
        }

        @Override
        public void checkCancel() throws InterruptedException {
        }
    }

}
