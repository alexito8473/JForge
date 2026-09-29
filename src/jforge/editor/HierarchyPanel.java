package jforge.editor;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

public class HierarchyPanel extends JPanel {

    public HierarchyPanel() {

        setLayout(new BorderLayout());

        DefaultListModel<String> model = new DefaultListModel<>();

        model.addElement("Scene");
        model.addElement("  Player");

        JList<String> objects = new JList<>(model);

        add(new JScrollPane(objects), BorderLayout.CENTER);
    }
}