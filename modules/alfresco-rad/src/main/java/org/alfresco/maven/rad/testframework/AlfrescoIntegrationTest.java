/**
 * Copyright (C) 2016 Alfresco Software Limited.
 * <p/>
 * This file is part of the Alfresco SDK project.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.alfresco.maven.rad.testframework;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * This is a base class that can be extended in order to get the AlfrescoTestRunner,
 * Spring Context and Alfresco Service registry setup for a test class.
 * <p/>
 * Integration testing framework donated by Zia Consulting
 *
 * @author Bindu Wavell <bindu@ziaconsulting.com>
 */
@RunWith(value = AlfrescoTestRunner.class)
public abstract class AlfrescoIntegrationTest {
    @Autowired
    ApplicationContext applicationContext;

    /**
     * Main access point to all Alfresco Services
     */
    private ServiceRegistry serviceRegistry;

    /**
     * Get the Alfresco Spring application context.
     *
     * @return
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Get the Service Registry with access to all the
     * public Alfresco Services.
     *
     * @return
     */
    public ServiceRegistry getServiceRegistry() {
        if (null == serviceRegistry) {
            if (null != applicationContext) {
                Object bean = applicationContext.getBean("ServiceRegistry");
                if (null != bean && bean instanceof ServiceRegistry) {
                    serviceRegistry = (ServiceRegistry) bean;
                }
            } else {
                throw new AlfrescoRuntimeException("Cannot get ServiceRegistry, applicationContext is null");
            }
        }

        return serviceRegistry;
    }
}
