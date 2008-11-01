package net.sourceforge.cruisecontrol.builders;

import java.io.File;

import net.sourceforge.cruisecontrol.CruiseControlException;
import net.sourceforge.cruisecontrol.testutil.TestUtil;
import net.sourceforge.cruisecontrol.testutil.TestUtil.FilesToDelete;
import net.sourceforge.cruisecontrol.util.BuildOutputLogger;

import org.jdom.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AntBuilderCreatingBuilderLoggerTest {
    private final FilesToDelete filesToDelete = new FilesToDelete();

    private TestAntBuilder builder;
    private BuildOutputLogger buildOutputLogger;

    @Before
    public void setUp() throws Exception {
        builder = new TestAntBuilder();

        File buildFile = File.createTempFile("build", ".xml", TestUtil.getTargetDir());
        filesToDelete.add(buildFile);
        builder.setBuildFile(buildFile.getAbsolutePath());
    }

    @After
    public void tearDown() throws Exception {
        builder = null;
        buildOutputLogger = null;
        filesToDelete.delete();
    }

    @Test
    public void testShouldCreateBuildOutputLoggerByDefault() throws CruiseControlException {
        builder.validate();
        builder.build(null, null);
        Assert.assertNotNull(buildOutputLogger);
    }

    @Test
    public void testShouldCreateBuildOutputLoggerWithShowAntOutputTrue() throws CruiseControlException {
        builder.setShowAntOutput(true);
        builder.validate();
        builder.build(null, null);
        Assert.assertNotNull(buildOutputLogger);
    }

    @Test
    public void testShouldNotCreateBuildOutputLoggerWithShowAntOutputFalse() throws CruiseControlException {
        builder.setShowAntOutput(false);
        builder.validate();
        builder.build(null, null);
        Assert.assertNull(buildOutputLogger);
    }

    private class TestAntBuilder extends AntBuilder {
        @Override
        boolean runScript(AntScript script, File workingDir, BuildOutputLogger outputLogger)
                throws CruiseControlException {
            buildOutputLogger = outputLogger;
            return true;
        }

        @Override
        protected Element getAntLogAsElement(File file) throws CruiseControlException {
            return null;
        }
    }
}
