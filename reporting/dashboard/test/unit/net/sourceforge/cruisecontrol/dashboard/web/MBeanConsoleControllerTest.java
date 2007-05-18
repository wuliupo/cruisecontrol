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
package net.sourceforge.cruisecontrol.dashboard.web;

import net.sourceforge.cruisecontrol.dashboard.service.CruiseControlJMXService;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

public class MBeanConsoleControllerTest extends MockObjectTestCase {

    private Mock serviceMock;

    private MBeanConsoleController controller;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    protected void setUp() throws Exception {
        serviceMock = mock(CruiseControlJMXService.class);
        controller = new MBeanConsoleController((CruiseControlJMXService) serviceMock.proxy());
        request = new MockHttpServletRequest();
        request.setMethod("GET");
        response = new MockHttpServletResponse();
        serviceMock.expects(once()).method("getHttpPortForMBeanConsole").will(returnValue(8000));
    }

    private void prepareRequest() {
        request.setRequestURI("/admin/mx4j");
    }

    private void prepareRequest(String pn) {
        request.setRequestURI("/admin/mx4j/" + pn);
    }

    public void testShouldReturnJMXConsoleHttpPort() throws Exception {
        prepareRequest();
        ModelAndView mov = controller.server(request, response);
        assertEquals("8000", mov.getModel().get("port"));
    }

    public void testContextPathShouldBeEmptyIfNoProjectNameSpecified() throws Exception {
        prepareRequest();
        ModelAndView mov = controller.server(request, response);
        assertEquals("", mov.getModel().get("context"));
    }

    public void testShouldReturnContextPathForSpecificProject() throws Exception {
        prepareRequest("project1");
        ModelAndView mov = controller.mbean(request, response);
        assertEquals("mbean?objectname=CruiseControl+Project%3Aname%3Dproject1", mov.getModel()
                .get("context"));
    }

    public void testShouldReplaceSpaceWithPlusInLinkWhenProjectNameContainsSpace() throws Exception {
        prepareRequest("project name with space");
        ModelAndView mov = controller.mbean(request, response);
        assertEquals("mbean?objectname=CruiseControl+Project%3Aname%3Dproject+name+with+space", mov
                .getModel().get("context"));
    }

    public void testShouldReturnProjectName() throws Exception {
        prepareRequest("project1");
        ModelAndView mov = controller.mbean(request, response);
        assertEquals("project1", mov.getModel().get("projectName"));
    }

    public void testShouldReturnIdenticalProjectNameForThoseContainSpace() throws Exception {
        prepareRequest("project name with space");
        ModelAndView mov = controller.mbean(request, response);
        assertEquals("project name with space", mov.getModel().get("projectName"));
    }

    public void testProjectNameShouldBeCruiseControlIfNoProjectNameSpecified() throws Exception {
        prepareRequest();
        ModelAndView mov = controller.server(request, response);
        assertEquals("CruiseControl", mov.getModel().get("projectName"));
    }
}