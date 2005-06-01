/********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2001-2003, ThoughtWorks, Inc.
 * 651 W Washington Ave. Suite 600
 * Chicago, IL 60661 USA
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
package net.sourceforge.cruisecontrol.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import net.sourceforge.cruisecontrol.CruiseControlException;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public final class Util {

    private Util() {
    }

    public static Element loadConfigFile(File configFile) throws CruiseControlException {
        try {
            SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser");
            return builder.build(configFile).getRootElement();
        } catch (Exception e) {
            throw new CruiseControlException(
                    "failed to load config file [" + (configFile != null
                    ? configFile.getName()
                    : "") + "]",
                    e);
        }
    }

    public static String readFileToString(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuffer result = new StringBuffer();

        String s = reader.readLine();
        while (s != null) {
            result.append(s.trim());
            s = reader.readLine();
        }
        reader.close();

        return result.toString();
    }

    /**
     * Deletes a File instance. If the file represents a directory, all
     * the subdirectories and files within.
     */
    public static void deleteFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (int i = 0; i < children.length; i++) {
                File child = children[i];
                deleteFile(child);
            }
        }
        file.delete();
    }

    public static boolean isWindows() {
        String osName = Util.getOsName();
        boolean isWindows = osName.indexOf("Windows") > -1;
        return isWindows;
    }

    public static String getOsName() {
        String osName = System.getProperty("os.name");
        return osName;
    }

}
