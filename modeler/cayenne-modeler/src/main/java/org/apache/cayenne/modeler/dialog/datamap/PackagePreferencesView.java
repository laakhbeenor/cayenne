package org.apache.cayenne.modeler.dialog.datamap;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 */
public class PackagePreferencesView extends JDialog {
    
    protected JCheckBox updateEntities;
    protected JCheckBox updateEmbeddables;
    protected JButton updateButton;
    protected JButton cancelButton;
    protected JPanel buttonPanel;

    public PackagePreferencesView(String ControlEntities, String ControlEmbeddables) {
        initView(ControlEntities, ControlEmbeddables);
    }

    protected void initView(String ControlEntities, String ControlEmbeddables) {
        updateEntities = new JCheckBox(ControlEntities);
        updateEntities.setSelected(true);

        updateEmbeddables = new JCheckBox(ControlEmbeddables);
        updateEmbeddables.setSelected(true);

        JCheckBoxMenuItem checkBoxMenu = new JCheckBoxMenuItem();
        checkBoxMenu.add(updateEntities);
        checkBoxMenu.add(updateEmbeddables);

        updateButton = new JButton("Update");
        cancelButton = new JButton("Cancel");

        // assemble
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);

        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout("left:max(180dlu;pref)", "p, 3dlu, p, 3dlu");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.add(updateEntities, cc.xy(1, 1));
        builder.add(updateEmbeddables, cc.xy(1, 3));

        setLayout(new BorderLayout());
        add(builder.getPanel(), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public JCheckBox getUpdateEntities() {
        return updateEntities;
    }

    public JCheckBox getUpdateEmbeddables() {
        return updateEmbeddables;
    }

    public JButton getUpdateButton() {
        return updateButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }
}
