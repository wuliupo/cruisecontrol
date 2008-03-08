package net.sourceforge.cruisecontrol.dashboard.repository;

import java.util.List;
import java.io.IOException;

import javax.management.MBeanServerConnection;

import net.sourceforge.cruisecontrol.BuildLoopInformation;
import net.sourceforge.cruisecontrol.BuildLoopInformation.ProjectInfo;

public interface BuildInformationRepository {
    void saveOrUpdate(BuildLoopInformation information);

    ProjectInfo getProjectInfo(String projectName);

    MBeanServerConnection getJmxConnection(String projectName) throws IOException;

    List getProjectInfos();

    BuildLoopInformation getBuildLoopInfo(String projectName);

    boolean hasBuildLoopInfoFor(String projectName);

    int size();

    void removeAll();
}