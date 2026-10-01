package jforge.editor;

import jforge.editor.debug.DebugPanel;
import jforge.engine.GameObject;
import jforge.engine.Scene;
import jforge.engine.components.MeshRenderer;
import jforge.engine.math.Vector3;
import jforge.engine.rendering.camera.CameraController;
import jforge.engine.rendering.gpu.GPUInfo;
import jforge.engine.rendering.gpu.GPUManager;
import jforge.engine.rendering.gpu.GPUMultiStrategy;
import jforge.engine.rendering.gpu.GPURenderConfig;
import jforge.engine.rendering.material.LoadedModel;
import jforge.engine.rendering.geometry.Mesh;
import jforge.engine.rendering.RenderBackend;
import jforge.engine.rendering.Renderer;
import jforge.engine.rendering.RendererManager;
import jforge.engine.rendering.material.Material;
import jforge.engine.rendering.material.Texture;
import jforge.engine.rendering.assets.OBJLoader;
import jforge.engine.runtime.SceneExporter;

import javax.swing.*;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.FlowLayout;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditorWindow
        extends JFrame {

    private final Scene scene;

    private final RendererManager rendererManager;

    private final CameraController cameraController;

    private JPanel viewportPanel;

    private DebugPanel debugPanel;

    private JTree hierarchyTree;

    private DefaultTreeModel hierarchyModel;

    private JPanel inspectorPanel;

    private JTextArea console;

    private GameObject selectedObject;

    private JTextField positionX;
    private JTextField positionY;
    private JTextField positionZ;

    private JTextField rotationX;
    private JTextField rotationY;
    private JTextField rotationZ;

    private JTextField scaleX;
    private JTextField scaleY;
    private JTextField scaleZ;

    private int objectCounter;

    /*
     * Timer solamente para actualizar
     * la cámara mediante teclado.
     *
     * El render puede seguir estando
     * controlado por el game loop.
     */


    public EditorWindow(
            Scene scene,
            RendererManager rendererManager) {

        super(
                "JForge Editor"
        );

        this.scene =
                scene;

        this.rendererManager =
                rendererManager;

        this.objectCounter =
                1;

        this.selectedObject =
                null;

        /*
         * Controller de cámara.
         *
         * SOLO TECLADO.
         */
        this.cameraController =
                new CameraController(
                        rendererManager
                                .getRenderer()
                                .getCamera()
                );

        cameraController.setMoveSpeed(
                5.0
        );

        cameraController.setRotationSpeed(
                90.0
        );

        /*
         * Ventana.
         */
        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setSize(
                1400,
                800
        );

        setMinimumSize(
                new Dimension(
                        1000,
                        650
                )
        );

        setLocationRelativeTo(
                null
        );

        setLayout(
                new BorderLayout()
        );

        /*
         * Menu.
         */
        setJMenuBar(
                createMenuBar()
        );

        /*
         * Construir UI.
         */
        buildUI();

        /*
         * Instalar renderer actual.
         */
        installRenderer(
                rendererManager.getRenderer()
        );

        /*
         * Cámara por teclado.
         *
         * 60 actualizaciones por segundo.
         *
         * No utilizamos ratón.
         */


        /*
         * Cleanup.
         */
        addWindowListener(
                new WindowAdapter() {

                    @Override
                    public void windowClosing(
                            WindowEvent event) {


                        if (debugPanel != null) {

                            debugPanel.dispose();
                        }

                        rendererManager.dispose();
                    }
                }
        );
    }

    /*
     * =========================================================
     * UI GENERAL
     * =========================================================
     */

    private void buildUI() {

        /*
         * HIERARCHY
         */
        JPanel hierarchyPanel =
                createHierarchyPanel();

        /*
         * VIEWPORT
         */
        viewportPanel =
                new JPanel(
                        new BorderLayout()
                );

        viewportPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Scene"
                )
        );

        /*
         * INSPECTOR
         */
        inspectorPanel =
                new JPanel(
                        new BorderLayout()
                );

        inspectorPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Inspector"
                )
        );

        showNothingSelected();

        /*
         * CONSOLE
         */
        console =
                new JTextArea();

        console.setEditable(
                false
        );

        console.setRows(
                5
        );

        console.setText(
                "JForge Editor iniciado.\n"
        );

        JScrollPane consoleScroll =
                new JScrollPane(
                        console
                );

        consoleScroll.setBorder(
                BorderFactory.createTitledBorder(
                        "Console"
                )
        );

        /*
         * HIERARCHY + VIEWPORT
         */
        JSplitPane leftCenter =
                new JSplitPane(
                        JSplitPane.HORIZONTAL_SPLIT,
                        hierarchyPanel,
                        viewportPanel
                );

        leftCenter.setDividerLocation(
                220
        );

        leftCenter.setResizeWeight(
                0.0
        );

        /*
         * VIEWPORT + INSPECTOR
         */
        JSplitPane mainSplit =
                new JSplitPane(
                        JSplitPane.HORIZONTAL_SPLIT,
                        leftCenter,
                        inspectorPanel
                );

        mainSplit.setDividerLocation(
                1100
        );

        mainSplit.setResizeWeight(
                0.85
        );

        add(
                mainSplit,
                BorderLayout.CENTER
        );

        add(
                consoleScroll,
                BorderLayout.SOUTH
        );

        /*
         * Hierarchy.
         */
        refreshHierarchy();
    }

    /*
     * =========================================================
     * HIERARCHY
     * =========================================================
     */

    private JPanel createHierarchyPanel() {

        JPanel panel =
                new JPanel(
                        new BorderLayout()
                );

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        "Hierarchy"
                )
        );

        DefaultMutableTreeNode root =
                new DefaultMutableTreeNode(
                        scene.getName()
                );

        hierarchyModel =
                new DefaultTreeModel(
                        root
                );

        hierarchyTree =
                new JTree(
                        hierarchyModel
                );

        hierarchyTree.setRootVisible(
                true
        );

        hierarchyTree.setShowsRootHandles(
                true
        );

        hierarchyTree.addTreeSelectionListener(
                event -> {

                    Object selected =
                            hierarchyTree
                                    .getLastSelectedPathComponent();

                    if (selected
                            instanceof GameObjectNode node) {

                        selectObject(
                                node.getGameObject()
                        );
                    }
                }
        );

        panel.add(
                new JScrollPane(
                        hierarchyTree
                ),
                BorderLayout.CENTER
        );

        /*
         * Botones.
         */
        JPanel buttons =
                new JPanel(
                        new GridLayout(
                                2,
                                2,
                                5,
                                5
                        )
                );

        JButton cube =
                new JButton(
                        "+ Cube"
                );

        JButton empty =
                new JButton(
                        "+ Empty"
                );

        JButton model =
                new JButton(
                        "Add Model"
                );

        JButton delete =
                new JButton(
                        "Delete"
                );

        cube.addActionListener(
                event ->
                        createCube()
        );

        empty.addActionListener(
                event ->
                        createEmpty()
        );

        model.addActionListener(
                event ->
                        loadModel()
        );

        delete.addActionListener(
                event ->
                        deleteSelected()
        );

        buttons.add(
                cube
        );

        buttons.add(
                empty
        );

        buttons.add(
                model
        );

        buttons.add(
                delete
        );

        panel.add(
                buttons,
                BorderLayout.SOUTH
        );

        return panel;
    }

    /*
     * =========================================================
     * RENDERER
     * =========================================================
     */

    private void installRenderer(
            Renderer renderer) {

        if (renderer == null) {
            return;
        }

        /*
         * Eliminar viewport anterior.
         */
        viewportPanel.removeAll();

        /*
         * Actualizar cámara del controller.
         */
        cameraController.setCamera(
                renderer.getCamera()
        );

        /*
         * Componente real:
         *
         * Software -> JPanel
         * OpenGL   -> AWTGLCanvas
         */
        Component component =
                renderer.getViewComponent();

        component.setPreferredSize(
                new Dimension(
                        900,
                        600
                )
        );

        /*
         * Solo teclado.
         *
         * IMPORTANTE:
         * no añadimos MouseListener.
         */
        component.addKeyListener(
                cameraController
        );

        component.setFocusable(
                true
        );

        /*
         * Estadísticas.
         */
        if (debugPanel != null) {

            debugPanel.dispose();
        }

        debugPanel =
                new DebugPanel(
                        renderer.getRenderStats(),
                        renderer.getBackend()
                );

        viewportPanel.add(
                component,
                BorderLayout.CENTER
        );

        viewportPanel.add(
                debugPanel,
                BorderLayout.SOUTH
        );

        viewportPanel.revalidate();

        viewportPanel.repaint();

        /*
         * Foco para WASD.
         */
        component.requestFocusInWindow();
    }

    /*
     * =========================================================
     * CAMBIAR RENDERER
     * =========================================================
     */

    private void switchRenderer(
            RenderBackend backend) {

        if (backend == null) {
            return;
        }

        if (rendererManager.getBackend()
                == backend) {

            return;
        }

        try {

            log(
                    "Cambiando renderer: " +
                            backend
            );

            Renderer renderer =
                    rendererManager.switchBackend(
                            backend
                    );

            installRenderer(
                    renderer
            );

            renderer.requestRender();

            renderer
                    .getViewComponent()
                    .requestFocusInWindow();

            setTitle(
                    "JForge Editor - " +
                            backend
            );

            log(
                    "Renderer activo: " +
                            renderer.getBackend()
            );

        } catch (
                RuntimeException exception
        ) {

            log(
                    "Error cambiando renderer: " +
                            exception.getMessage()
            );

            exception.printStackTrace();

            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "No se pudo cambiar el renderer.\n\n" +
                            exception.getMessage(),
                    "JForge",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /*
     * =========================================================
     * MENU
     * =========================================================
     */

    private JMenuBar createMenuBar() {

        JMenuBar menuBar =
                new JMenuBar();

        /*
         * FILE
         */
        JMenu fileMenu =
                new JMenu(
                        "File"
                );
        JMenuItem exportGameItem =
                new JMenuItem("Exportar juego...");

        exportGameItem.addActionListener(
                e -> exportGame()
        );

        fileMenu.add(
                exportGameItem
        );

        fileMenu.addSeparator();
        JMenuItem exit =
                new JMenuItem(
                        "Exit"
                );

        exit.addActionListener(
                event ->
                        System.exit(0)
        );

        fileMenu.add(
                exit
        );

        /*
         * RENDERER
         */
        JMenu rendererMenu =
                new JMenu(
                        "Renderer"
                );
        JMenuItem gpuSettingsItem =
                new JMenuItem("Configuración GPU...");

        gpuSettingsItem.addActionListener(
                e -> showGPUSettingsDialog()
        );

        rendererMenu.addSeparator();

        rendererMenu.add(
                gpuSettingsItem
        );
        JRadioButtonMenuItem softwareItem =
                new JRadioButtonMenuItem(
                        "Software CPU"
                );

        JRadioButtonMenuItem openGLItem =
                new JRadioButtonMenuItem(
                        "OpenGL GPU"
                );

        ButtonGroup group =
                new ButtonGroup();

        group.add(
                softwareItem
        );

        group.add(
                openGLItem
        );

        softwareItem.setSelected(
                rendererManager.getBackend()
                        ==
                        RenderBackend.SOFTWARE
        );

        openGLItem.setSelected(
                rendererManager.getBackend()
                        ==
                        RenderBackend.OPENGL
        );

        softwareItem.addActionListener(
                event -> {

                    switchRenderer(
                            RenderBackend.SOFTWARE
                    );

                    softwareItem.setSelected(
                            rendererManager
                                    .getBackend()
                                    ==
                                    RenderBackend.SOFTWARE
                    );

                    openGLItem.setSelected(
                            rendererManager
                                    .getBackend()
                                    ==
                                    RenderBackend.OPENGL
                    );
                }
        );

        openGLItem.addActionListener(
                event -> {

                    switchRenderer(
                            RenderBackend.OPENGL
                    );

                    softwareItem.setSelected(
                            rendererManager
                                    .getBackend()
                                    ==
                                    RenderBackend.SOFTWARE
                    );

                    openGLItem.setSelected(
                            rendererManager
                                    .getBackend()
                                    ==
                                    RenderBackend.OPENGL
                    );
                }
        );

        rendererMenu.add(
                softwareItem
        );

        rendererMenu.add(
                openGLItem
        );

        /*
         * TOOLS
         */
        JMenu toolsMenu =
                new JMenu(
                        "Tools"
                );

        JMenuItem refresh =
                new JMenuItem(
                        "Refresh Scene"
                );

        refresh.addActionListener(
                event -> {

                    refreshHierarchy();

                    rendererManager
                            .getRenderer()
                            .requestRender();
                }
        );

        toolsMenu.add(
                refresh
        );

        menuBar.add(
                fileMenu
        );

        menuBar.add(
                rendererMenu
        );

        menuBar.add(
                toolsMenu
        );

        return menuBar;
    }

    /*
     * =========================================================
     * CREATE CUBE
     * =========================================================
     */

    private void createCube() {

        String name =
                "Cube " +
                        objectCounter++;

        GameObject object =
                new GameObject(
                        name
                );

        object.setMeshRenderer(
                new MeshRenderer(
                        Mesh.createCube(),
                        new Material(
                                Color.WHITE
                        )
                )
        );

        /*
         * Ponerlo delante de la cámara.
         */
        positionObjectInFrontOfCamera(
                object,
                8.0
        );

        scene.add(
                object
        );

        refreshHierarchy();

        selectObject(
                object
        );

        log(
                "Creado: " +
                        name
        );

        rendererManager
                .getRenderer()
                .requestRender();
    }

    /*
     * =========================================================
     * CREATE EMPTY
     * =========================================================
     */

    private void createEmpty() {

        String name =
                "GameObject " +
                        objectCounter++;

        GameObject object =
                new GameObject(
                        name
                );

        positionObjectInFrontOfCamera(
                object,
                8.0
        );

        scene.add(
                object
        );

        refreshHierarchy();

        selectObject(
                object
        );

        log(
                "Creado: " +
                        name
        );

        rendererManager
                .getRenderer()
                .requestRender();
    }

    /*
     * =========================================================
     * LOAD MODEL
     * =========================================================
     */

    private void loadModel() {

        JFileChooser chooser =
                new JFileChooser();

        chooser.setDialogTitle(
                "Añadir modelo 3D"
        );

        int result =
                chooser.showOpenDialog(
                        this
                );

        if (result !=
                JFileChooser.APPROVE_OPTION) {

            return;
        }

        File file =
                chooser.getSelectedFile();

        try {

            LoadedModel loaded =
                    OBJLoader.loadModel(
                            file
                    );

            if (loaded == null ||
                    loaded.getMesh() == null) {

                log(
                        "El modelo no se pudo cargar."
                );

                return;
            }

            Mesh mesh =
                    loaded.getMesh();

            String name =
                    file.getName();

            int dot =
                    name.lastIndexOf(
                            '.'
                    );

            if (dot > 0) {

                name =
                        name.substring(
                                0,
                                dot
                        );
            }

            GameObject object =
                    new GameObject(
                            name
                    );

            Material material =
                    new Material(
                            Color.WHITE
                    );

            /*
             * Textura del MTL.
             */
            if (loaded
                    .getDiffuseTextureFile()
                    != null) {

                Texture texture =
                        Texture.load(
                                loaded
                                        .getDiffuseTextureFile()
                        );

                material.setTexture(
                        texture
                );

                log(
                        "Textura: " +
                                loaded
                                        .getDiffuseTextureFile()
                                        .getName()
                );

            } else {

                log(
                        "No se encontró map_Kd."
                );
            }

            object.setMeshRenderer(
                    new MeshRenderer(
                            mesh,
                            material
                    )
            );

            fitImportedModel(
                    object,
                    mesh
            );

            scene.add(
                    object
            );

            refreshHierarchy();

            selectObject(
                    object
            );

            log(
                    "Modelo cargado: " +
                            file.getName()
            );

            log(
                    "Vertices: " +
                            mesh
                                    .getVertices()
                                    .length
            );

            log(
                    "Triangles: " +
                            mesh
                                    .getTriangles()
                                    .length
            );

            rendererManager
                    .getRenderer()
                    .requestRender();

        } catch (
                IOException |
                RuntimeException exception
        ) {

            log(
                    "Error cargando modelo: " +
                            exception.getMessage()
            );

            exception.printStackTrace();
        }
    }

    /*
     * =========================================================
     * AJUSTAR MODELO
     * =========================================================
     */

    private void fitImportedModel(
            GameObject object,
            Mesh mesh) {

        Vector3[] vertices =
                mesh.getVertices();

        if (vertices == null ||
                vertices.length == 0) {

            return;
        }

        double minX =
                Double.POSITIVE_INFINITY;

        double minY =
                Double.POSITIVE_INFINITY;

        double minZ =
                Double.POSITIVE_INFINITY;

        double maxX =
                Double.NEGATIVE_INFINITY;

        double maxY =
                Double.NEGATIVE_INFINITY;

        double maxZ =
                Double.NEGATIVE_INFINITY;

        for (Vector3 vertex :
                vertices) {

            if (vertex == null) {
                continue;
            }

            minX =
                    Math.min(
                            minX,
                            vertex.x
                    );

            minY =
                    Math.min(
                            minY,
                            vertex.y
                    );

            minZ =
                    Math.min(
                            minZ,
                            vertex.z
                    );

            maxX =
                    Math.max(
                            maxX,
                            vertex.x
                    );

            maxY =
                    Math.max(
                            maxY,
                            vertex.y
                    );

            maxZ =
                    Math.max(
                            maxZ,
                            vertex.z
                    );
        }

        double centerX =
                (minX + maxX) *
                        0.5;

        double centerY =
                (minY + maxY) *
                        0.5;

        double centerZ =
                (minZ + maxZ) *
                        0.5;

        double sizeX =
                maxX - minX;

        double sizeY =
                maxY - minY;

        double sizeZ =
                maxZ - minZ;

        double maxSize =
                Math.max(
                        sizeX,
                        Math.max(
                                sizeY,
                                sizeZ
                        )
                );

        if (maxSize <= 0.000001) {

            maxSize =
                    1.0;
        }

        double targetSize =
                4.0;

        double scale =
                targetSize /
                        maxSize;

        scale =
                Math.max(
                        0.0001,
                        Math.min(
                                1000.0,
                                scale
                        )
                );

        Vector3 forward =
                rendererManager
                        .getRenderer()
                        .getCamera()
                        .getForward();

        Vector3 cameraPosition =
                rendererManager
                        .getRenderer()
                        .getCamera()
                        .getPosition();

        double distance =
                8.0;

        double targetX =
                cameraPosition.x +
                        forward.x *
                                distance;

        double targetY =
                cameraPosition.y +
                        forward.y *
                                distance;

        double targetZ =
                cameraPosition.z +
                        forward.z *
                                distance;

        double positionX =
                targetX -
                        centerX *
                                scale;

        double positionY =
                targetY -
                        centerY *
                                scale;

        double positionZ =
                targetZ -
                        centerZ *
                                scale;

        object.getTransform()
                .setScale(
                        scale,
                        scale,
                        scale
                );

        object.getTransform()
                .setPosition(
                        positionX,
                        positionY,
                        positionZ
                );

        object.getTransform()
                .setRotation(
                        0,
                        0,
                        0
                );
    }

    private void positionObjectInFrontOfCamera(
            GameObject object,
            double distance) {

        Vector3 forward =
                rendererManager
                        .getRenderer()
                        .getCamera()
                        .getForward();

        Vector3 cameraPosition =
                rendererManager
                        .getRenderer()
                        .getCamera()
                        .getPosition();

        object.getTransform()
                .setPosition(
                        cameraPosition.x +
                                forward.x *
                                        distance,

                        cameraPosition.y +
                                forward.y *
                                        distance,

                        cameraPosition.z +
                                forward.z *
                                        distance
                );
    }

    /*
     * =========================================================
     * DELETE
     * =========================================================
     */

    private void deleteSelected() {

        if (selectedObject == null) {
            return;
        }

        String name =
                selectedObject.getName();

        if (selectedObject.getParent()
                != null) {

            selectedObject
                    .getParent()
                    .removeChild(
                            selectedObject
                    );

        } else {

            scene.remove(
                    selectedObject
            );
        }

        selectedObject =
                null;

        refreshHierarchy();

        showNothingSelected();

        log(
                "Eliminado: " +
                        name
        );

        rendererManager
                .getRenderer()
                .requestRender();
    }

    /*
     * =========================================================
     * SELECTION
     * =========================================================
     */

    private void selectObject(
            GameObject object) {

        selectedObject =
                object;

        showInspector(
                object
        );
    }

    /*
     * =========================================================
     * INSPECTOR
     * =========================================================
     */

    private void showNothingSelected() {

        inspectorPanel.removeAll();

        JLabel label =
                new JLabel(
                        "Selecciona un GameObject",
                        SwingConstants.CENTER
                );

        inspectorPanel.add(
                label,
                BorderLayout.CENTER
        );

        inspectorPanel.revalidate();

        inspectorPanel.repaint();
    }

    private void showInspector(
            GameObject object) {

        inspectorPanel.removeAll();

        JPanel content =
                new JPanel(
                        new BorderLayout(
                                5,
                                5
                        )
                );

        JLabel title =
                new JLabel(
                        object.getName()
                );

        title.setBorder(
                BorderFactory.createEmptyBorder(
                        8,
                        8,
                        8,
                        8
                )
        );

        content.add(
                title,
                BorderLayout.NORTH
        );

        JPanel transformPanel =
                new JPanel(
                        new GridLayout(
                                4,
                                4,
                                5,
                                5
                        )
                );

        transformPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Transform"
                )
        );

        transformPanel.add(
                new JLabel("")
        );

        transformPanel.add(
                new JLabel(
                        "X"
                )
        );

        transformPanel.add(
                new JLabel(
                        "Y"
                )
        );

        transformPanel.add(
                new JLabel(
                        "Z"
                )
        );

        Vector3 position =
                object
                        .getTransform()
                        .getPosition();

        Vector3 rotation =
                object
                        .getTransform()
                        .getRotation();

        Vector3 scale =
                object
                        .getTransform()
                        .getScale();

        positionX =
                createField(
                        position.x
                );

        positionY =
                createField(
                        position.y
                );

        positionZ =
                createField(
                        position.z
                );

        rotationX =
                createField(
                        rotation.x
                );

        rotationY =
                createField(
                        rotation.y
                );

        rotationZ =
                createField(
                        rotation.z
                );

        scaleX =
                createField(
                        scale.x
                );

        scaleY =
                createField(
                        scale.y
                );

        scaleZ =
                createField(
                        scale.z
                );

        addTransformRow(
                transformPanel,
                "Position",
                positionX,
                positionY,
                positionZ
        );

        addTransformRow(
                transformPanel,
                "Rotation",
                rotationX,
                rotationY,
                rotationZ
        );

        addTransformRow(
                transformPanel,
                "Scale",
                scaleX,
                scaleY,
                scaleZ
        );

        /*
         * Botón Apply.
         */
        JButton apply =
                new JButton(
                        "Apply Transform"
                );

        apply.addActionListener(
                event ->
                        applyTransform()
        );

        transformPanel.add(
                apply
        );

        /*
         * Material.
         */
        JPanel materialPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT
                        )
                );

        materialPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Material"
                )
        );

        JButton colorButton =
                new JButton(
                        "Change Color"
                );

        colorButton.addActionListener(
                event ->
                        changeColor(
                                object
                        )
        );

        materialPanel.add(
                colorButton
        );

        JPanel center =
                new JPanel(
                        new BorderLayout()
                );

        center.add(
                transformPanel,
                BorderLayout.NORTH
        );

        center.add(
                materialPanel,
                BorderLayout.CENTER
        );

        content.add(
                center,
                BorderLayout.CENTER
        );

        inspectorPanel.add(
                new JScrollPane(
                        content
                ),
                BorderLayout.CENTER
        );

        inspectorPanel.revalidate();

        inspectorPanel.repaint();
    }

    private JTextField createField(
            double value) {

        return new JTextField(
                String.valueOf(
                        value
                )
        );
    }

    private void addTransformRow(
            JPanel panel,
            String name,
            JTextField x,
            JTextField y,
            JTextField z) {

        panel.add(
                new JLabel(
                        name
                )
        );

        panel.add(
                x
        );

        panel.add(
                y
        );

        panel.add(
                z
        );
    }

    private void applyTransform() {

        if (selectedObject == null) {
            return;
        }

        try {

            double px =
                    Double.parseDouble(
                            positionX.getText()
                    );

            double py =
                    Double.parseDouble(
                            positionY.getText()
                    );

            double pz =
                    Double.parseDouble(
                            positionZ.getText()
                    );

            double rx =
                    Double.parseDouble(
                            rotationX.getText()
                    );

            double ry =
                    Double.parseDouble(
                            rotationY.getText()
                    );

            double rz =
                    Double.parseDouble(
                            rotationZ.getText()
                    );

            double sx =
                    Double.parseDouble(
                            scaleX.getText()
                    );

            double sy =
                    Double.parseDouble(
                            scaleY.getText()
                    );

            double sz =
                    Double.parseDouble(
                            scaleZ.getText()
                    );

            selectedObject
                    .getTransform()
                    .setPosition(
                            px,
                            py,
                            pz
                    );

            selectedObject
                    .getTransform()
                    .setRotation(
                            rx,
                            ry,
                            rz
                    );

            selectedObject
                    .getTransform()
                    .setScale(
                            sx,
                            sy,
                            sz
                    );

            log(
                    "Transform actualizado: " +
                            selectedObject.getName()
            );

            rendererManager
                    .getRenderer()
                    .requestRender();

        } catch (
                NumberFormatException exception
        ) {

            log(
                    "Valor numérico inválido."
            );
        }
    }

    private void changeColor(
            GameObject object) {

        MeshRenderer meshRenderer =
                object.getMeshRenderer();

        if (meshRenderer == null) {
            return;
        }

        Material material =
                meshRenderer.getMaterial();

        if (material == null) {
            return;
        }

        Color current =
                material.getColor();

        if (current == null) {

            current =
                    Color.WHITE;
        }

        Color selected =
                JColorChooser.showDialog(
                        this,
                        "Seleccionar color",
                        current
                );

        if (selected == null) {
            return;
        }

        material.setColor(
                selected
        );

        log(
                "Color cambiado: " +
                        object.getName()
        );

        rendererManager
                .getRenderer()
                .requestRender();
    }

    /*
     * =========================================================
     * HIERARCHY REFRESH
     * =========================================================
     */

    private void refreshHierarchy() {

        if (hierarchyModel == null) {
            return;
        }

        DefaultMutableTreeNode root =
                new DefaultMutableTreeNode(
                        scene.getName()
                );

        for (GameObject object :
                scene.getObjects()) {

            addObjectNode(
                    root,
                    object
            );
        }

        hierarchyModel.setRoot(
                root
        );

        hierarchyModel.reload();

        if (hierarchyTree != null) {

            hierarchyTree.expandRow(
                    0
            );
        }
    }

    private void addObjectNode(
            DefaultMutableTreeNode parent,
            GameObject object) {

        GameObjectNode node =
                new GameObjectNode(
                        object
                );

        parent.add(
                node
        );

        for (GameObject child :
                object.getChildren()) {

            addObjectNode(
                    node,
                    child
            );
        }
    }

    /*
     * =========================================================
     * CONSOLE
     * =========================================================
     */

    private void log(
            String message) {

        if (console == null) {
            return;
        }

        console.append(
                message +
                        "\n"
        );

        console.setCaretPosition(
                console.getDocument()
                        .getLength()
        );
    }

    /*
     * =========================================================
     * GETTERS
     * =========================================================
     */

    public Renderer getRenderer() {

        return rendererManager
                .getRenderer();
    }

    public RendererManager getRendererManager() {

        return rendererManager;
    }

    public CameraController getCameraController() {

        return cameraController;
    }

    /*
     * =========================================================
     * TREE NODE
     * =========================================================
     */

    private static class GameObjectNode
            extends DefaultMutableTreeNode {

        private final GameObject gameObject;

        public GameObjectNode(
                GameObject gameObject) {

            super(
                    gameObject.getName()
            );

            this.gameObject =
                    gameObject;
        }

        public GameObject getGameObject() {

            return gameObject;
        }

        @Override
        public String toString() {

            return gameObject.getName();
        }
    }
    private void showGPUSettingsDialog() {

        GPUManager gpuManager =
                rendererManager.getGPUManager();

        List<GPUInfo> gpus =
                gpuManager.getGPUs();

        if (gpus.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "JForge no ha podido detectar GPUs " +
                            "mediante el sistema operativo.",
                    "GPUs",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        JPanel mainPanel =
                new JPanel(new BorderLayout(10, 10));

        JPanel modePanel =
                new JPanel(
                        new GridLayout(0, 1, 4, 4)
                );

        modePanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Modo de selección"
                )
        );

        JRadioButton autoButton =
                new JRadioButton(
                        "Automático"
                );

        JRadioButton singleButton =
                new JRadioButton(
                        "Usar una GPU"
                );

        JRadioButton multiButton =
                new JRadioButton(
                        "Usar varias GPUs"
                );

        ButtonGroup modeGroup =
                new ButtonGroup();

        modeGroup.add(autoButton);
        modeGroup.add(singleButton);
        modeGroup.add(multiButton);

        modePanel.add(autoButton);
        modePanel.add(singleButton);

        if (gpus.size() >= 2) {
            modePanel.add(multiButton);
        }

        mainPanel.add(
                modePanel,
                BorderLayout.NORTH
        );

        JPanel gpuPanel =
                new JPanel(
                        new GridLayout(0, 1, 4, 4)
                );

        gpuPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "GPUs disponibles"
                )
        );

        List<JCheckBox> gpuChecks =
                new ArrayList<>();

        for (GPUInfo gpu : gpus) {

            String text =
                    gpu.getDisplayName() +
                            " | " +
                            gpu.getMemoryMB() +
                            " MB" +
                            (gpu.isDiscrete()
                                    ? " | Dedicada"
                                    : " | Integrada");

            JCheckBox checkBox =
                    new JCheckBox(text);

            gpuChecks.add(checkBox);
            gpuPanel.add(checkBox);
        }

        mainPanel.add(
                gpuPanel,
                BorderLayout.CENTER
        );

        JPanel strategyPanel =
                new JPanel(
                        new BorderLayout(5, 5)
                );

        strategyPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Estrategia multi-GPU"
                )
        );

        JComboBox<GPUMultiStrategy>
                strategyCombo =
                new JComboBox<>(
                        GPUMultiStrategy.values()
                );

        strategyPanel.add(
                new JLabel("Estrategia:"),
                BorderLayout.WEST
        );

        strategyPanel.add(
                strategyCombo,
                BorderLayout.CENTER
        );

        mainPanel.add(
                strategyPanel,
                BorderLayout.SOUTH
        );

        GPURenderConfig currentConfig =
                gpuManager.getConfiguration();

        if (currentConfig.isAutomatic()) {

            autoButton.setSelected(true);

        } else if (currentConfig.isSingleGPU()) {

            singleButton.setSelected(true);

        } else {

            multiButton.setSelected(true);
        }

        int[] currentIndices =
                currentConfig.getSelectedGpuIndices();

        for (int index : currentIndices) {

            if (index >= 0 &&
                    index < gpuChecks.size()) {

                gpuChecks.get(index).setSelected(true);
            }
        }

        strategyCombo.setSelectedItem(
                currentConfig.getMultiStrategy()
        );

        if (gpus.size() == 1) {
            autoButton.setSelected(false);
            singleButton.setSelected(true);
            gpuChecks.get(0).setSelected(true);
        }

        Runnable updateState = () -> {

            boolean multi =
                    multiButton.isSelected();

            strategyCombo.setEnabled(multi);

            for (int i = 0; i < gpuChecks.size(); i++) {

                boolean enabled =
                        singleButton.isSelected() ||
                                multi;

                gpuChecks.get(i).setEnabled(enabled);
            }

            if (singleButton.isSelected()) {

                boolean alreadySelected = false;

                for (JCheckBox check : gpuChecks) {

                    if (check.isSelected()) {

                        if (!alreadySelected) {
                            alreadySelected = true;
                        } else {
                            check.setSelected(false);
                        }
                    }
                }
            }

            if (autoButton.isSelected()) {

                for (JCheckBox check : gpuChecks) {
                    check.setEnabled(false);
                }
            }
        };

        autoButton.addActionListener(e -> updateState.run());
        singleButton.addActionListener(e -> updateState.run());
        multiButton.addActionListener(e -> updateState.run());

        updateState.run();

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        mainPanel,
                        "Configuración de GPU",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        GPURenderConfig newConfig;

        if (autoButton.isSelected()) {

            newConfig =
                    GPURenderConfig.auto();

        } else if (singleButton.isSelected()) {

            int selected =
                    -1;

            for (int i = 0;
                 i < gpuChecks.size();
                 i++) {

                if (gpuChecks.get(i).isSelected()) {
                    selected = i;
                    break;
                }
            }

            if (selected < 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Selecciona una GPU.",
                        "Configuración GPU",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            newConfig =
                    GPURenderConfig.single(selected);

        } else {

            List<Integer> selected =
                    new ArrayList<>();

            for (int i = 0;
                 i < gpuChecks.size();
                 i++) {

                if (gpuChecks.get(i).isSelected()) {
                    selected.add(i);
                }
            }

            if (selected.size() < 2) {

                JOptionPane.showMessageDialog(
                        this,
                        "Selecciona al menos 2 GPUs " +
                                "para el modo multi-GPU.",
                        "Configuración GPU",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            int[] selectedArray =
                    new int[selected.size()];

            for (int i = 0;
                 i < selected.size();
                 i++) {

                selectedArray[i] =
                        selected.get(i);
            }

            newConfig =
                    GPURenderConfig.multi(
                            selectedArray,
                            (GPUMultiStrategy)
                                    strategyCombo.getSelectedItem()
                    );
        }

        rendererManager.setGPUConfiguration(
                newConfig
        );

        appendConsole(
                "GPU configurada: " +
                        rendererManager
                                .getGPUConfiguration()
        );

        if (newConfig.isMultiGPU()) {

            appendConsole(
                    "Multi-GPU: " +
                            newConfig.getMultiStrategy()
            );
        }
    }
    private void appendConsole(String message) {
        if (console == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            console.append(message);
            console.append(System.lineSeparator());
            console.setCaretPosition(console.getDocument().getLength());
        });
    }
    private void exportGame() {

        Renderer renderer =
                rendererManager.getRenderer();

        if (renderer == null) {
            return;
        }

        JFileChooser chooser =
                new JFileChooser();

        chooser.setDialogTitle(
                "Seleccionar carpeta de exportación"
        );

        chooser.setFileSelectionMode(
                JFileChooser.DIRECTORIES_ONLY
        );

        chooser.setAcceptAllFileFilterUsed(
                false
        );

        int result =
                chooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File outputDirectory =
                chooser.getSelectedFile();

        String defaultName =
                scene.getName();

        if (defaultName == null ||
                defaultName.isBlank()) {

            defaultName = "JForgeGame";
        }

        String gameName =
                JOptionPane.showInputDialog(
                        this,
                        "Nombre del juego:",
                        defaultName
                );

        if (gameName == null ||
                gameName.isBlank()) {

            return;
        }

        try {

            appendConsole(
                    "[Export] Preparando juego..."
            );

            File executable =
                    SceneExporter.exportExecutable(
                            scene,
                            renderer.getCamera(),
                            rendererManager.getBackend(),
                            outputDirectory,
                            gameName
                    );

            appendConsole(
                    "[Export] Juego creado correctamente."
            );

            appendConsole(
                    "[Export] EXE:"
            );

            appendConsole(
                    executable.getAbsolutePath()
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Juego exportado correctamente.\n\n" +
                            "Ejecutable:\n" +
                            executable.getAbsolutePath(),
                    "JForge",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {

            e.printStackTrace();

            appendConsole(
                    "[Export] ERROR:"
            );

            appendConsole(
                    e.getMessage()
            );

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo crear el ejecutable:\n\n" +
                            e.getMessage(),
                    "Error de exportación",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}