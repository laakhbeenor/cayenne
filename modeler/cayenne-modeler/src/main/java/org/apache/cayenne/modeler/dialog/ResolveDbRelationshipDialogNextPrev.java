/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/

package org.apache.cayenne.modeler.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.map.DbEntity;
import org.apache.cayenne.map.DbJoin;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.Entity;
import org.apache.cayenne.map.Relationship;
import org.apache.cayenne.map.event.MapEvent;
import org.apache.cayenne.map.event.RelationshipEvent;
import org.apache.cayenne.map.naming.DefaultUniqueNameGenerator;
import org.apache.cayenne.map.naming.NameCheckers;
import org.apache.cayenne.modeler.Application;
import org.apache.cayenne.modeler.editor.dbentity.DbRelationshipTableModel;
import org.apache.cayenne.modeler.pref.TableColumnPreferences;
import org.apache.cayenne.modeler.undo.RelationshipUndoableEdit;
import org.apache.cayenne.modeler.util.CayenneDialog;
import org.apache.cayenne.modeler.util.CayenneTable;
import org.apache.cayenne.modeler.util.ModelerUtil;
import org.apache.cayenne.modeler.util.PanelFactory;
import org.apache.cayenne.modeler.util.combo.AutoCompletion;
import org.apache.cayenne.util.Util;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Editor of DbRelationship joins.
 */
public class ResolveDbRelationshipDialogNextPrev extends CayenneDialog {

    protected InfoAboutRelationship relationshipInfo;
    protected DbRelationship relationship;
    protected DbRelationship reverseRelationship;

    protected JTextField name;
    protected JTextField reverseName;
    protected CayenneTable table;
    protected HolderRealtionshipChanges holder;
    protected TableColumnPreferences tablePreferences;
    protected JButton addButton;
    protected JButton removeButton;
    protected JButton prevButton;
    protected JButton nextButton;
    protected JButton saveButton;
    protected JButton cancelButton;
    protected int row;

    private boolean cancelPressed;

    private RelationshipUndoableEdit undo;

    private boolean editable = true;

    public ResolveDbRelationshipDialogNextPrev(HolderRealtionshipChanges holder, int row) {
        this(holder, row, true);
    }

    public ResolveDbRelationshipDialogNextPrev(HolderRealtionshipChanges holder, int row, boolean editable) {
        super(Application.getFrame(), "", true);
        this.editable = editable;
        this.row = row;
        this.holder = holder;
        this.relationshipInfo = holder.getRelationshipInfo(row);
        this.relationship = holder.getRelationship(row);
        this.reverseRelationship = relationship.getReverseRelationship();
      
        initView();
        initController();
        initWithModel();

        //this.undo = new RelationshipUndoableEdit(relationship);

        this.pack();
        this.centerWindow();

    }

    /**
     * Creates graphical components.
     */
    private void initView() {

        // create widgets
        name = new JTextField(25);
        reverseName = new JTextField(25);

        addButton = new JButton("Add");
        addButton.setEnabled(this.editable);

        removeButton = new JButton("Remove");
        removeButton.setEnabled(this.editable);
        
        prevButton = new JButton("Previous");
        prevButton.setEnabled(this.editable);
        
        nextButton = new JButton("Next");
        nextButton.setEnabled(this.editable);

        saveButton = new JButton("Done");

        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(this.editable);

        table = new AttributeTable();

        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePreferences = new TableColumnPreferences(getClass(), "dbentity/dbjoinTable");

        // assemble
        getContentPane().setLayout(new BorderLayout());

        CellConstraints cc = new CellConstraints();
        PanelBuilder builder = new PanelBuilder(
                new FormLayout(
                        "right:max(50dlu;pref), 3dlu, fill:min(150dlu;pref), 3dlu, fill:min(50dlu;pref)",
                        "p, 3dlu, p, 3dlu, p, 9dlu, p, 3dlu, top:14dlu, 3dlu, top:p:grow"));
        builder.setDefaultDialogBorder();

        builder.addSeparator("DbRelationship Information", cc.xywh(1, 1, 5, 1));
        builder.addLabel("Relationship:", cc.xy(1, 3));
        builder.add(name, cc.xywh(3, 3, 1, 1));
        builder.addLabel("Reverse Relationship", cc.xy(1, 5));
        builder.add(reverseName, cc.xywh(3, 5, 1, 1));

        builder.addSeparator("Joins", cc.xywh(1, 7, 5, 1));
        builder.add(new JScrollPane(table), cc.xywh(1, 9, 3, 3, "fill, fill"));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEADING));

        buttons.add(addButton);
        buttons.add(removeButton);
        
        builder.add(buttons, cc.xywh(5, 9, 1, 3));

        getContentPane().add(builder.getPanel(), BorderLayout.CENTER);
        getContentPane().add(PanelFactory.createButtonPanel(new JButton[] {
                prevButton, nextButton, saveButton, cancelButton
        }), BorderLayout.SOUTH);
    }

    private void initWithModel() {
        // sanity check
        if (relationship.getSourceEntity() == null) {
            throw new CayenneRuntimeException("Null source entity: " + relationship);
        }

        if (relationship.getTargetEntity() == null) {
            throw new CayenneRuntimeException("Null target entity: " + relationship);
        }

        if (relationship.getSourceEntity().getDataMap() == null) {
            throw new CayenneRuntimeException("Null DataMap: "
                    + relationship.getSourceEntity());
        }

        // Once assigned, can reference relationship directly. Would it be
        // OK to assign relationship at the very top of this method?
        //relationship = aRelationship;
        //reverseRelationship = relationship.getReverseRelationship();

        // init UI components
        setTitle("DbRelationship Info: "
                + relationshipInfo.getSourceEntity()
                + " to "
                + relationshipInfo.getTargetEntity());

        table.setModel(new DbJoinTableModel(relationship, getMediator(), this, true));
        TableColumn sourceColumn = table.getColumnModel().getColumn(
                DbJoinTableModel.SOURCE);
        JComboBox comboBox = Application.getWidgetFactory().createComboBox(
                ModelerUtil.getDbAttributeNames(getMediator(), (DbEntity) relationship
                        .getSourceEntity()),
                true);

        AutoCompletion.enable(comboBox);
        sourceColumn.setCellEditor(Application.getWidgetFactory().createCellEditor(
                comboBox));

        TableColumn targetColumn = table.getColumnModel().getColumn(
                DbJoinTableModel.TARGET);
        comboBox = Application.getWidgetFactory().createComboBox(
                ModelerUtil.getDbAttributeNames(getMediator(), (DbEntity) relationship
                        .getTargetEntity()),
                true);
        AutoCompletion.enable(comboBox);

        targetColumn.setCellEditor(Application.getWidgetFactory().createCellEditor(
                comboBox));

        if (reverseRelationship != null) {
            reverseName.setText(relationshipInfo.getReverseRelationship());
        }

        name.setText(relationshipInfo.getName());
        tablePreferences.bind(table, null, null, null, DbJoinTableModel.SOURCE, true);
    }

    private void initController() {
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DbJoinTableModel model = (DbJoinTableModel) table.getModel();

                DbJoin join = new DbJoin(relationship);
                model.addRow(join);

                undo.addDbJoinAddUndo(join);

                table.select(model.getRowCount() - 1);
            }
        });

        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DbJoinTableModel model = (DbJoinTableModel) table.getModel();
                stopEditing();
                int row = table.getSelectedRow();

                DbJoin join = model.getJoin(row);
                undo.addDbJoinRemoveUndo(join);

                model.removeRow(join);
            }
        });

        saveButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cancelPressed = false;

                if (editable) {
                	saveChanges();
                    save();
                }

                dispose();
            }
        });

        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cancelPressed = true;
                setVisible(false);
            }
        });
        
        nextButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            	saveChanges();
            	if((row+1)<holder.getRowCount()){
	            	setVisible(false);
	            	ResolveDbRelationshipDialogNextPrev dialog = new ResolveDbRelationshipDialogNextPrev(holder,row+1);
	                dialog.setVisible(true);
	                dialog.dispose();
            	}
            	else{
            		JOptionPane.showMessageDialog(nextButton.getParent(), "This is the last relationship.");
            	}
            }
        });
        
        prevButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            	saveChanges();
            	if((row-1)>=0){
            		setVisible(false);
	            	table.select(row-1);
	            	ResolveDbRelationshipDialogNextPrev dialog = new ResolveDbRelationshipDialogNextPrev(holder,row-1);
	                dialog.setVisible(true);
	                dialog.dispose();
            	}
            	else{
            		JOptionPane.showMessageDialog(nextButton.getParent(), "This is the first relationship.");
            	}
            }
        });
    }

    public boolean isCancelPressed() {
        return cancelPressed;
    }

    private void stopEditing() {
        // Stop whatever editing may be taking place
        int col_index = table.getEditingColumn();
        if (col_index >= 0) {
            TableColumn col = table.getColumnModel().getColumn(col_index);
            col.getCellEditor().stopCellEditing();
        }
    }

    private void saveChanges(){
    	 stopEditing();
    	 String sourceEntityName = name.getText();
         if (sourceEntityName.length() == 0) {
             sourceEntityName = null;
         }
         
         if (sourceEntityName == null) {
             sourceEntityName = DefaultUniqueNameGenerator.generate(NameCheckers.dbRelationship, relationship.getSourceEntity());
         }
         
         if (!validateName(relationship.getSourceEntity(), relationship, sourceEntityName)) {
             return;
         }
         
         relationshipInfo.setName(sourceEntityName);
         
         String targetEntityName = reverseName.getText().trim();
         if (targetEntityName.length() == 0) {
             targetEntityName = null;
         }

         if (targetEntityName == null) {
             targetEntityName = DefaultUniqueNameGenerator.generate(NameCheckers.dbRelationship, relationship.getTargetEntity());
         }
         
      // check if reverse name is valid
         DbJoinTableModel model = (DbJoinTableModel) table.getModel();
         boolean updatingReverse = model.getObjectList().size() > 0;

         if (updatingReverse
                 && !validateName(
                         relationship.getTargetEntity(),
                         reverseRelationship,
                         targetEntityName)) {
             return;
         }
         
         relationshipInfo.setReverseName(targetEntityName);
    }
    
    private void save() {
    	for(int i=0;i<holder.getRowCount();i++){
    		DbRelationship oldRelationship = holder.getRelationship(i);
    		InfoAboutRelationship relChange = holder.getRelationshipInfo(i);
        // handle name update
        if (!Util.nullSafeEquals(relChange.getSourceEntity(), oldRelationship.getName())) {
            String oldName = oldRelationship.getName();
            
            oldRelationship.setName(relChange.getSourceEntity());

            undo.addNameUndo(oldRelationship, oldName, relChange.getSourceEntity());

            getMediator().fireDbRelationshipEvent(
                    new RelationshipEvent(this, oldRelationship, oldRelationship
                            .getSourceEntity(), oldName));
        }
        
        DbJoinTableModel model = (DbJoinTableModel) table.getModel();
        
        model.commit();

        // check "to dep pk" setting,
        // maybe this is no longer valid
        if (oldRelationship.isToDependentPK() && !oldRelationship.isValidForDepPk()) {
            oldRelationship.setToDependentPK(false);
        }

        // If new reverse DbRelationship was created, add it to the target
        // Don't create reverse with no joins - makes no sense...
        if (model.getObjectList().size() > 0) {

            // If didn't find anything, create reverseDbRel
        	DbRelationship oldReverseRelationship = oldRelationship.getReverseRelationship();
            if (oldReverseRelationship == null) {
                oldReverseRelationship = new DbRelationship(relChange.getTargetEntity());
                oldReverseRelationship.setSourceEntity(oldRelationship.getTargetEntity());
                oldReverseRelationship.setTargetEntityName(oldRelationship.getSourceEntity());
                oldReverseRelationship.setToMany(!oldRelationship.isToMany());
                oldRelationship.getTargetEntity().addRelationship(oldReverseRelationship);

                // fire only if the relationship is to the same entity...
                // this is needed to update entity view...
                if (oldRelationship.getSourceEntity() == oldRelationship.getTargetEntity()) {
                    getMediator().fireDbRelationshipEvent(
                            new RelationshipEvent(
                                    this,
                                    oldReverseRelationship,
                                    oldReverseRelationship.getSourceEntity(),
                                    MapEvent.ADD));
                }
            }
            else if (!Util
                    .nullSafeEquals(relChange.getTargetEntity(), oldReverseRelationship.getName())) {

                String oldName = oldReverseRelationship.getName();
                
                oldReverseRelationship.setName(relChange.getTargetEntity());

                undo.addNameUndo(oldReverseRelationship, oldName, relChange.getTargetEntity());

                getMediator().fireDbRelationshipEvent(
                        new RelationshipEvent(
                                this,
                                oldReverseRelationship,
                                oldReverseRelationship.getSourceEntity(),
                                oldName));
            }

            Collection reverseJoins = getReverseJoins(oldRelationship);
            oldReverseRelationship.setJoins(reverseJoins);

            // check if joins map to a primary key of this entity
            if (!oldRelationship.isToDependentPK() && oldReverseRelationship.isValidForDepPk()) {
                oldReverseRelationship.setToDependentPK(true);
            }
        }

        Application.getInstance().getUndoManager().addEdit(undo);

        getMediator()
                .fireDbRelationshipEvent(
                        new RelationshipEvent(this, oldRelationship, oldRelationship
                                .getSourceEntity()));
    	}
    }

    private boolean validateName(Entity entity, Relationship aRelationship, String newName) {
        Relationship existing = entity.getRelationship(newName);
        if (existing != null && (aRelationship == null || aRelationship != existing)) {
            JOptionPane.showMessageDialog(
                    this,
                    "There is an existing relationship named \""
                            + newName
                            + "\". Select a different name.");
            return false;
        }

        return true;
    }

    private Collection getReverseJoins(DbRelationship relationship) {
        Collection<DbJoin> joins = relationship.getJoins();

        if ((joins == null) || (joins.size() == 0)) {
            return Collections.EMPTY_LIST;
        }

        List reverseJoins = new ArrayList(joins.size());

        // Loop through the list of attribute pairs, create reverse pairs
        // and put them to the reverse list.
        for (DbJoin pair : joins) {
            DbJoin reverseJoin = pair.createReverseJoin();

            // since reverse relationship is not yet initialized,
            // reverse join will not have it set automatically
            reverseJoin.setRelationship(reverseRelationship);
            reverseJoins.add(reverseJoin);
        }

        return reverseJoins;
    }

    final class AttributeTable extends CayenneTable {

        final Dimension preferredSize = new Dimension(203, 100);

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return preferredSize;
        }
    }
}
