package org.apache.cayenne.modeler.dialog.datamap;

import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.modeler.ModelerPreferences;
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
        boolean isEntities = ((PackagePreferencesView)getView()).getUpdateEntities().isSelected();
        ModelerPreferences.ENTITIES = isEntities;
        return isEntities;
    }

    public boolean isEmbeddables() {
        boolean isEmbeddables = ((PackagePreferencesView)getView()).getUpdateEmbeddables().isSelected();
        ModelerPreferences.EMBEDDABLES = isEmbeddables;
        return isEmbeddables;
    }
}
