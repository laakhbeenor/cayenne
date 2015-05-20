package org.apache.cayenne.modeler.dialog;

import java.util.ArrayList;
import java.util.List;

import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.modeler.editor.dbentity.DbRelationshipTableModel;
import org.apache.cayenne.modeler.util.CayenneTable;

public class HolderRealtionshipChanges {
	
	List<InfoAboutRelationship> list;
	DbRelationshipTableModel model;
	CayenneTable table;
	
	public HolderRealtionshipChanges(CayenneTable table,int startRow){
		this.table = table;
		this.list = new ArrayList<InfoAboutRelationship>();
		this.model = ((DbRelationshipTableModel)table.getModel());
		for(int i=0;i<model.getRowCount();i++){
			list.add(new InfoAboutRelationship(model.getRelationship(i)));
		}
		ResolveDbRelationshipDialogNextPrev dialog = new ResolveDbRelationshipDialogNextPrev(this,startRow); //tu ma byc list
		dialog.setVisible(true);
        dialog.dispose();
		
	}
	
	public List<InfoAboutRelationship> getList(){
		return list;
	}
	
	public InfoAboutRelationship getRelationshipInfo(int row){
		return list.get(row);
	}
	
	public DbRelationship getRelationship(int row){
		return model.getRelationship(row);
	}
	
	public int getRowCount(){
		return table.getRowCount();
	}
	
	public DbRelationshipTableModel getModel(){
		return model;
	}
}
