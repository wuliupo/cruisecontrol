/********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2001, ThoughtWorks, Inc.
 * 651 W Washington Ave. Suite 500
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
package net.sourceforge.cruisecontrol.element.starteam;

import com.starbase.starteam.*;
import com.starbase.starteam.vts.comm.CommandException;
import java.io.FileNotFoundException;
import com.starbase.util.OLEDate;

import java.util.*;
import org.apache.tools.ant.Project;

/**
 * This class logs into StarTeam checks out any changes that have occurred since
 * the last successful build. It also creates all working directories on the
 * local directory if appropriate. Ant Usage: <taskdef name="starteamcheckout"
 * classname="org.apache.tools.ant.taskdefs.StarTeamCheckout"/>
 * <starteamcheckout username="BuildMaster" password="ant" folder="Source"
 * starteamurl="servername:portnum/project/view"
 * createworkingdirectories="true"/>
 *
 * @author Christopher Charlier, ThoughtWorks, Inc. 2001
 * @author <a href="mailto:jcyip@thoughtworks.com">Jason Yip</a>
 */
public class StarTeamCheckout extends StarTeamTask {
    /**
     * The root folder in the StarTeam directory passed in by Ant.
     */
    private Folder rootFolder;

    /**
     * Set the folder in the Starteam repository to check out the code from. All
     * subdirectories of the folder will be checked out as well.
     */
    private String folder;

    /**
     * Set the boolean value that tells us if we want to create all directories
     * that are in the Starteam repository regardless if they are empty.
     */
    private boolean createDirs;

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public void setCreateWorkingDirectories(String create) {
        createDirs = (create == "true") || (create == "yes");
    }

    /**
     * This method does the work of creating the new view and checking it into
     * Starteam.
     *
     * @exception java.io.FileNotFoundException
     * @throws CommandException
     * @throws FileNotFoundException
     * @throws ServerException
     */
    public void taskExecute() throws CommandException, FileNotFoundException, ServerException {
        // Get view as of the current time?
        View view = StarTeamFinder.openView(getUserName() + ":" + getPassword()
                 + "@" + getURL());
        View snapshot = new View(view, ViewConfiguration.createFromTime(
                new OLEDate()));
        rootFolder = StarTeamFinder.findFolder(snapshot.getRootFolder(),
                this.folder);

        if (rootFolder == null) {
            throw new FileNotFoundException();
        }

        // Inspect everything in the root folder
        visit(rootFolder);
    }

    /**
     * Visits a folder to check on the files and sub folders that exist for
     * changes.
     *
     * @param folder
     */
    private void visit(Folder folder) {
        try {

            Set localFiles = getLocalFiles(folder);

            // If we have been told to create the working folders
            if (createDirs) {
                // Create if it doesn't exist
                java.io.File workingFolder = new java.io.File(folder.getPath());
                if (!workingFolder.exists()) {
                    workingFolder.mkdir();
                }
            }

            // For all Files in this folder, we need to check to see if there have been modifications.
            Item[] files = folder.getItems("File");
            for (int i = 0; i < files.length; i++) {
                File eachFile = (File) files[i];
                localFiles.remove(eachFile.getFullName());
                int fileStatus = (eachFile.getStatus());
                // We try to update the status once to give StarTeam another chance.
                if (fileStatus == Status.MERGE || fileStatus == Status.UNKNOWN) {
                    eachFile.updateStatus(true, true);
                }

                // If the file is current then skip it.
                if (fileStatus == Status.CURRENT) {
                    continue;
                }

                // Checkout anything else.
                // Just a note: StarTeam has a status for NEW which implies that their is
                // Something on your local machine that is not in the repository. These
                // are the items that show up as NOT IN VIEW in the Starteam GUI. One
                // would think that we would want to perhaps checkin the NEW items.
                // Unfortunately, the sdk doesn't really work, and we cant actually see
                // anything with a status of NEW. That is why we can just checkout everything
                // here and we don't need to worry about losing anything.
                log("Checking Out: " + (eachFile.getFullName()), Project.MSG_INFO);
                eachFile.checkout(Item.LockType.UNCHANGED, true, true, true);
            }

            // We also want to recursively call this method on all sub folders in this folder.
            Folder[] subFolders = folder.getSubFolders();
            for (int i = 0; i < subFolders.length; i++) {
                localFiles.remove(subFolders[i].getPath());
                visit(subFolders[i]);
            }

            // Delete all folders or files that are not in Starteam.
            if (!localFiles.isEmpty()) {
                delete(localFiles);
            }
        }
        catch (java.io.IOException e) {
            log("Error occurred while reading file: " + e, Project.MSG_ERR);
        }
    }

    /**
     * Deletes everything on the local machine that is not in Starteam.
     *
     * @param files
     */
    private void delete(Collection files) {
        try {

            Iterator itr = files.iterator();
            while (itr.hasNext()) {
                java.io.File file = new java.io.File(itr.next().toString());
                delete(file);
            }

        }
        catch (SecurityException e) {
            log("Error deleting file: " + e, Project.MSG_ERR);
        }
    }

    /**
     * Deletes the file from the local drive.
     *
     * @param file
     * @return
     */
    private boolean delete(java.io.File file) {
        // If the current file is a Directory, we need to delete all its children as well.
        if (file.isDirectory()) {
            java.io.File[] children = file.listFiles();
            for (int i = 0; i < children.length; i++) {
                delete(children[i]);
            }
        }

        log("Deleting: " + file.getAbsolutePath(), Project.MSG_INFO);
        return file.delete();
    }

    /**
     * Gets the collection of the local file names in the current directory We
     * need to check this collection against what we find in Starteam to
     * understand what we need to delete in order to synch with the repos.
     *
     * @param folder
     * @return
     */
    private static Set getLocalFiles(Folder folder) {
        java.io.File localFolder = new java.io.File(folder.getPath());
        if (localFolder.exists()) {
            String[] localFiles = localFolder.list();
            for (int i = 0; i < localFiles.length; i++) {
                localFiles[i] = folder.getPath() + "\\" + localFiles[i];
            }
            return new HashSet(Arrays.asList(localFiles));
        }
        else {
            return Collections.EMPTY_SET;
        }
    }
}
