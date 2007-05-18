/********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2007, ThoughtWorks, Inc.
 * 200 E. Randolph, 25th Floor
 * Chicago, IL 60601 USA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     + Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     + Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/
package net.sourceforge.cruisecontrol.dashboard.service;

import java.util.List;
import java.util.Map;
import javax.management.MBeanServerConnection;
import net.sourceforge.cruisecontrol.dashboard.ModificationKey;
import net.sourceforge.cruisecontrol.dashboard.web.SpringBasedControllerTests;
import net.sourceforge.cruisecontrol.dashboard.testhelpers.jdelegator.JDelegator;
import net.sourceforge.cruisecontrol.dashboard.testhelpers.jmxstub.MBeanServerConnectionBuildOutputStub;
import net.sourceforge.cruisecontrol.dashboard.testhelpers.jmxstub.MBeanServerConnectionCommitMessageStub;
import net.sourceforge.cruisecontrol.dashboard.testhelpers.jmxstub.MBeanServerConnectionMBeanConsoleHttpPortStub;
import net.sourceforge.cruisecontrol.dashboard.testhelpers.jmxstub.MBeanServerConnectionProjectsStatusStub;
import net.sourceforge.cruisecontrol.dashboard.testhelpers.jmxstub.MBeanServerConnectionStatusStub;
import net.sourceforge.cruisecontrol.dashboard.testhelpers.jmxstub.MBeanServerErrorConnectionStub;

public class CruiseControlJMXServiceTest extends SpringBasedControllerTests {
    private static final String PROJECT_NAME = "connectfour";

    private CruiseControlJMXService jmxService;

    public void testShouldGetStatusByProjectName() throws Exception {
        MBeanServerConnection connection = (MBeanServerConnection) JDelegator.delegate(MBeanServerConnection.class)
                .to(new MBeanServerConnectionStatusStub());
        jmxService.setJmxConnector(connection);
        String status = jmxService.getBuildStatus(PROJECT_NAME);
        assertEquals("waiting for next time to build", status);
    }

    public void testStatusOfProjectShouldBeChangedAfterForceBuildByRMIConnection() throws Exception {
        MBeanServerConnection connection = (MBeanServerConnection) JDelegator.delegate(MBeanServerConnection.class)
                .to(new MBeanServerConnectionStatusStub());
        jmxService.setJmxConnector(connection);
        String statusBefore = jmxService.getBuildStatus(PROJECT_NAME);
        jmxService.fourceBuild(PROJECT_NAME);
        String statusAfter = jmxService.getBuildStatus(PROJECT_NAME);
        assertFalse(statusBefore.equals(statusAfter));
    }

    public void testStatusUnknownShouldBeReturnedJMXErrorHappens() throws Exception {
        MBeanServerConnection connection = (MBeanServerConnection) JDelegator.delegate(MBeanServerConnection.class)
                .to(new MBeanServerErrorConnectionStub());
        jmxService.setJmxConnector(connection);
        String buildStatus = jmxService.getBuildStatus(PROJECT_NAME);
        assertNull(buildStatus);
    }

    public void testShouldReturnArrayContainsCommiterAndCommitMessageWhenInvokeJMXWithAttributeCommitMessage()
            throws Exception {
        MBeanServerConnection connection = (MBeanServerConnection) JDelegator.delegate(MBeanServerConnection.class)
                .to(new MBeanServerConnectionCommitMessageStub());
        jmxService.setJmxConnector(connection);
        List commitMessages = jmxService.getCommitMessages(PROJECT_NAME);
        ModificationKey message = (ModificationKey) commitMessages.get(0);
        assertEquals("commiter", message.getUser());
        assertEquals("message 1", message.getComment());
    }

    public void testShouldReturnBuildOutput() throws Exception {
        MBeanServerConnection connection = (MBeanServerConnection) JDelegator.delegate(MBeanServerConnection.class)
                .to(new MBeanServerConnectionBuildOutputStub());
        jmxService.setJmxConnector(connection);
        String[] output = jmxService.getBuildOutput(PROJECT_NAME, 0);
        assertEquals("Build Failed", output[0]);
        assertEquals("Build Duration: 10s", output[1]);
    }

    public void testShouldReturnEmptyArrayWhenExceptionOccurs() {
        MBeanServerConnection connection = (MBeanServerConnection) JDelegator.delegate(MBeanServerConnection.class)
                .to(new MBeanServerErrorConnectionStub());
        jmxService.setJmxConnector(connection);
        List commitMessages = jmxService.getCommitMessages(PROJECT_NAME);
        assertNotNull(commitMessages);
        assertEquals(0, commitMessages.size());
    }

    public void testShouldReturnArrayContainsProjectNameAndStatusWhenInvokeJMXWithAttributeAllProjectsStatus() {
        MBeanServerConnection connection = (MBeanServerConnection) JDelegator.delegate(MBeanServerConnection.class)
                .to(new MBeanServerConnectionProjectsStatusStub());
        jmxService.setJmxConnector(connection);
        Map projectsStatus = jmxService.getAllProjectsStatus();
        assertEquals("now building since 20070420174744", projectsStatus.get("project1"));
    }

    public void testShouldReturnEmptyMaoWhenInvokeJMXWithAttributeAllProjectsStatus() {
        MBeanServerConnection connection = (MBeanServerConnection) JDelegator.delegate(MBeanServerConnection.class)
                .to(new MBeanServerErrorConnectionStub());
        jmxService.setJmxConnector(connection);
        Map projectsStatus = jmxService.getAllProjectsStatus();
        assertNotNull(projectsStatus);
        assertEquals(0, projectsStatus.size());
    }

    public void testShouldReturnHttpPortForMBeanConsole() throws Exception {
        MBeanServerConnection connection = (MBeanServerConnection) JDelegator.delegate(MBeanServerConnection.class)
                .to(new MBeanServerConnectionMBeanConsoleHttpPortStub());
        jmxService.setJmxConnector(connection);
        int port = jmxService.getHttpPortForMBeanConsole();
        assertEquals(8000, port);
    }

    public CruiseControlJMXService getjmxService() {
        return jmxService;
    }

    public void setJmxService(CruiseControlJMXService jmxService) {
        this.jmxService = jmxService;
    }

}