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
package org.apache.cayenne.project2.validate;

import org.apache.cayenne.conf.DriverDataSourceFactory;
import org.apache.cayenne.configuration.DataChannelDescriptor;
import org.apache.cayenne.configuration.DataNodeDescriptor;
import org.apache.cayenne.project.ProjectPath;
import org.apache.cayenne.util.Util;


public class DataNodeValidator implements Validator{

    public void validate(Object object, ConfigurationValidationVisitor configurationValidationVisitor) {
        DataNodeDescriptor node = (DataNodeDescriptor) object;
        ProjectPath path = new ProjectPath(new Object[]{configurationValidationVisitor.getProject(), node});
        validateName(node, path, configurationValidationVisitor);
        validateConnection(node, path, configurationValidationVisitor);
    }

    protected void validateConnection(DataNodeDescriptor node, ProjectPath path, ConfigurationValidationVisitor validator) {
        String factory = node.getDataSourceFactoryType();

        // If direct factory, make sure the location is a valid file name.
        if (Util.isEmptyString(factory)) {
            validator.registerError("No DataSource factory.", path);
        }
        else if(!DriverDataSourceFactory.class.getName().equals(factory)){
            String location = node.getParameters();
            if (Util.isEmptyString(location)) {
                validator.registerError("DataNode has no location parameter.", path);
            }
        }
    }

    protected void validateName(DataNodeDescriptor node, ProjectPath path, ConfigurationValidationVisitor validator) {
        String name = node.getName();

        if (Util.isEmptyString(name)) {
            validator.registerError("Unnamed DataNode.", path);
            return;
        }

        DataChannelDescriptor dataChannelDescriptor = (DataChannelDescriptor)validator.getProject().getRootNode();
        // check for duplicate names in the parent context
        for (final DataNodeDescriptor otherNode : dataChannelDescriptor.getNodeDescriptors()) {
            if (otherNode == node) {
                continue;
            }

            if (name.equals(otherNode.getName())) {
                validator.registerError("Duplicate DataNode name: " + name + ".", path);
                break;
            }
        }
    }
}