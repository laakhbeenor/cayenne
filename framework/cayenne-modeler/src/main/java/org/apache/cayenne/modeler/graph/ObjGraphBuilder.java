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
package org.apache.cayenne.modeler.graph;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.cayenne.access.DataDomain;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.Entity;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.event.AttributeEvent;
import org.apache.cayenne.map.event.EntityEvent;
import org.apache.cayenne.map.event.ObjAttributeListener;
import org.apache.cayenne.map.event.ObjEntityListener;
import org.apache.cayenne.map.event.ObjRelationshipListener;
import org.apache.cayenne.map.event.RelationshipEvent;
import org.apache.cayenne.modeler.ProjectController;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;

/**
 * Builder of ObjEntity information-based graph (relative to UML class diagram)
 */
class ObjGraphBuilder extends BaseGraphBuilder
    implements ObjEntityListener, ObjAttributeListener, ObjRelationshipListener {
    
    static final Color ENTITY_COLOR = new Color(255, 255, 185);
    
    Map<Entity, DefaultEdge> inheritanceEdges;
    
    public ObjGraphBuilder() {
        inheritanceEdges = new HashMap<Entity, DefaultEdge>();
    }
    
    @Override
    protected Collection<? extends Entity> getEntities(DataMap map) {
        return map.getObjEntities();
    }

    @Override
    protected boolean isIsolated(DataDomain domain, Entity entity) {
        return super.isIsolated(domain, entity) && 
            ((ObjEntity) entity).getSuperEntity() == null && 
             domain.getEntityResolver().lookupInheritanceTree(entity.getName()) == null;
    }
    
    @Override
    protected void postProcessEntity(Entity entity, DefaultGraphCell cell) {
        super.postProcessEntity(entity, cell);
        
        GraphConstants.setBackground(cell.getAttributes(), ENTITY_COLOR);
        GraphConstants.setOpaque(cell.getAttributes(), true);
        
        DefaultEdge edge = createInheritanceEdge((ObjEntity) entity);
        if (edge != null) {
            createdObjects.add(edge);
        }
    }
    
    DefaultEdge createInheritanceEdge(ObjEntity entity) {
        if (!inheritanceEdges.containsKey(entity)) {
            ObjEntity superEntity = entity.getSuperEntity();
            if (superEntity != null) {
                DefaultGraphCell sourceCell = entityCells.get(entity.getName());
                DefaultGraphCell targetCell = entityCells.get(superEntity.getName());
                
                DefaultEdge edge = new DefaultEdge();
                edge.setSource(sourceCell.getChildAt(0));
                edge.setTarget(targetCell.getChildAt(0));
                
                GraphConstants.setDashPattern(edge.getAttributes(), new float[] { 5, 5 });
                GraphConstants.setLineEnd(edge.getAttributes(), GraphConstants.ARROW_TECHNICAL);
                GraphConstants.setSelectable(edge.getAttributes(), false);
                
                inheritanceEdges.put(entity, edge);
                
                return edge;
            }
        }
        return null;
    }

    @Override
    protected EntityCellMetadata getCellMetadata(Entity e) {
        return new ObjEntityCellMetadata(this, e.getName());
    }
    
    @Override
    public void setProjectController(ProjectController mediator) {
        super.setProjectController(mediator);
        
        mediator.addObjEntityListener(this);
        mediator.addObjAttributeListener(this);
        mediator.addObjRelationshipListener(this);
    }

    public void destroy() {
        super.destroy();
        
        mediator.removeObjEntityListener(this);
        mediator.removeObjAttributeListener(this);
        mediator.removeObjRelationshipListener(this);
    }

    public void objEntityAdded(EntityEvent e) {
        if (isInCurrentDomain()) {
            insertEntityCell(e.getEntity());
        }
    }

    public void objEntityChanged(EntityEvent e) {
        if (isInCurrentDomain()) {
            remapEntity(e);
            
            updateEntityCell(e.getEntity());
            
            //maybe super entity was changed
            ObjEntity entity = (ObjEntity) e.getEntity();
            DefaultEdge inheritanceEdge = inheritanceEdges.get(entity);
            if (inheritanceEdge != null) {
                if (entity.getSuperEntity() == null) {
                    graph.getGraphLayoutCache().remove(new Object[] { inheritanceEdge });
                    inheritanceEdges.remove(entity);
                }
                else {
                    inheritanceEdge.setTarget(entityCells.get(entity.getSuperEntity().getName()).getChildAt(0));
                    
                    Map nested = new HashMap();
                    nested.put(inheritanceEdge, inheritanceEdge.getAttributes());
                    
                    graph.getGraphLayoutCache().edit(nested);
                }
            }
            else {
                if (entity.getSuperEntity() != null) {
                    DefaultEdge edge = createInheritanceEdge(entity);
                    graph.getGraphLayoutCache().insert(edge);
                }
            }
        }
    }

    public void objEntityRemoved(EntityEvent e) {
        if (isInCurrentDomain()) {
            removeEntityCell(e.getEntity());
        }
    }

    public void objAttributeAdded(AttributeEvent e) {
        if (isInCurrentDomain()) {
            updateEntityCell(e.getEntity());
        }
    }

    public void objAttributeChanged(AttributeEvent e) {
        if (isInCurrentDomain()) {
            updateEntityCell(e.getEntity());
        }
    }

    public void objAttributeRemoved(AttributeEvent e) {
        if (isInCurrentDomain()) {
            updateEntityCell(e.getEntity());
        }
    }

    public void objRelationshipAdded(RelationshipEvent e) {
        //nothing because relationship does not have target yet
    }

    public void objRelationshipChanged(RelationshipEvent e) {
        if (isInCurrentDomain()) {
            remapRelationship(e);
            updateRelationshipCell(e.getRelationship());
        }
    }

    public void objRelationshipRemoved(RelationshipEvent e) {
        if (isInCurrentDomain()) {
            removeRelationshipCell(e.getRelationship());
        }
    }
    
    @Override
    protected void removeEntityCell(Entity e) {
        super.removeEntityCell(e);
        inheritanceEdges.remove(e);
    }

    public GraphType getType() {
        return GraphType.CLASS;
    }
}