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
package net.sourceforge.cruisecontrol.dashboard.jwebunittests;

import java.io.IOException;
import java.net.URL;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import junit.framework.TestCase;
import net.sourceforge.cruisecontrol.dashboard.testhelpers.DataUtils;
import net.sourceforge.jwebunit.junit.WebTester;

public abstract class BaseFunctionalTest extends TestCase {

    public static final String BASE_URL = "http://localhost:9090/dashboard/";
    protected static final String CONFIG_FILE_LOCATION_FIELD_NAME = "configFileLocation";
    protected static final String SPECIFY_CONFIG_FILE_URL = "/admin/config";
    protected static final String SET_CONFIG_FILE_LOCATION_FORM = "specifyConfigLocation";
    public static final String SET_CONFIGRATION_FORM = "setConfigration";
    public static final String BUILD_LOG_LOCATION = "cruiseloglocation";

    protected WebTester tester;

    public final void setUp() throws Exception {
        tester = new WebTester();
        tester.setScriptingEnabled(false);
        tester.getTestContext().setBaseUrl(BASE_URL);
        onSetUp();
    }

    protected void onSetUp() throws Exception {

    }

    protected final String getJSONWithAjaxInvocation(final String path) throws Exception {
        final WebClient webClient = new WebClient();
        WebRequestSettings settings = new WebRequestSettings(new URL(BASE_URL + path));
        WebResponse response = webClient.getWebConnection().getResponse(settings);
        return response.getResponseHeaderValue("X-JSON");
    }

    public void setConfigFileAndSubmitForm(String configFilePath) throws IOException {
        tester.beginAt(SPECIFY_CONFIG_FILE_URL);
        tester.setWorkingForm(SET_CONFIG_FILE_LOCATION_FORM);
        tester.setTextField(CONFIG_FILE_LOCATION_FIELD_NAME, configFilePath);
        tester.submit();
    }

    protected void typeLogLocation() throws Exception {
        tester.setTextField(CONFIG_FILE_LOCATION_FIELD_NAME, DataUtils.getLogDirAsFile()
                .getAbsolutePath());
    }
}