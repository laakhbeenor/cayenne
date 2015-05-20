package org.apache.cayenne.modeler.dialog;

import org.apache.cayenne.map.DbRelationship;

public class InfoAboutRelationship {
	
	protected String sourceEntity;
	protected String targetEntity;
	protected String reverseRelationship;
	protected String name;
	
	public InfoAboutRelationship(DbRelationship relationship){
		this.sourceEntity = relationship.getSourceEntity().getName();
		this.targetEntity = relationship.getTargetEntityName();
		this.reverseRelationship = relationship.getReverseRelationship().getName();
		this.name = relationship.getName();
		
	}
	public String getSourceEntity(){
		return sourceEntity;
	}
	
	public String getTargetEntity(){
		return targetEntity;
	}
	
	public String getReverseRelationship(){
		return reverseRelationship;
	}
	public String getName(){
		return name;
	}
	
	public void setSourceEntity(String name){
		this.sourceEntity = name;
	}
	
	public void setTargetEntity(String name){
		this.targetEntity = name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setReverseName(String name){
		this.reverseRelationship = name;
	}

}
