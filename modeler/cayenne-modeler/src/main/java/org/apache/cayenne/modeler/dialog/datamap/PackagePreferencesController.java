package org.apache.cayenne.modeler.dialog.datamap;

import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.modeler.ProjectController;
import org.apache.cayenne.modeler.util.CayenneController;

/**
 * An abstract controller for the DataMap package updates.
 * 
 */
public abstract class PackagePreferencesController extends CayenneController {
    
    protected DataMap dataMap;
    protected ProjectController mediator;
    

    public PackagePreferencesController(ProjectController mediator, DataMap dataMap) {
        super(mediator);
        this.dataMap = dataMap;
        this.mediator = mediator;
    }

    public boolean isEntities() {
        return ((PackagePreferencesView)getView()).getUpdateEntities().isSelected();
    }

    public boolean isEmbeddables() {
        return ((PackagePreferencesView)getView()).getUpdateEmbeddables().isSelected();
    }
}
