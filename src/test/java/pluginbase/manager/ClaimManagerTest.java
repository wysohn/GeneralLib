package pluginbase.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.generallib.pluginbase.PluginBase;
import org.generallib.pluginbase.PluginManager;
import org.generallib.pluginbase.constants.Area;
import org.generallib.pluginbase.constants.ClaimInfo;
import org.generallib.pluginbase.constants.SimpleLocation;
import org.generallib.pluginbase.manager.RegionManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import pluginbase.manager.ElementCachingManagerTest.TestConfig;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ClaimManagerTest.TestPlugin.class, Logger.class })
public class ClaimManagerTest {

    @Test
    public void testAddRemoveClaim() throws Exception {
        TestPlugin mockBase = PowerMock.createMock(TestPlugin.class);
        Logger mockLogger = PowerMock.createMock(Logger.class);

        File file = new File("ClaimManagerTestSuite1");

        EasyMock.expect(mockBase.isEnabled()).andReturn(true).anyTimes();
        EasyMock.expect(mockBase.getDataFolder()).andStubReturn(file);
        EasyMock.expect(mockBase.getPluginConfig()).andReturn(new TestConfig());
        EasyMock.expect(mockBase.getLogger()).andStubReturn(mockLogger);

        mockLogger.info(EasyMock.anyString());
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>() {

            @Override
            public Void answer() throws Throwable {
                System.out.println(EasyMock.getCurrentArguments()[0]);
                return null;
            }

        }).anyTimes();

        PowerMock.replay(mockBase, mockLogger);

        TestManager manager = new TestManager(mockBase, PluginManager.NORM_PRIORITY);
        manager.createFileDB().clear();
        Whitebox.invokeMethod(manager, "onEnable");

        Area area = new Area(new SimpleLocation("world", 0, 0, 0), new SimpleLocation("world", 5, 5, 5));
        TestClaimInfo info = new TestClaimInfo(area);
        info.setName("SomeClaim");

        manager.setAreaInfo(area, info);

        System.out.println("Current caches: " + Whitebox.getField(manager.getClass(), "cachedElements").get(manager));
        System.out.println("Current namemap: " + Whitebox.getField(manager.getClass(), "nameMap").get(manager));

        TestClaimInfo infoOut;

        SimpleLocation sloc = new SimpleLocation("world", 2, 3, 2);
        infoOut = manager.getAreaInfo(sloc);
        assertEquals(info, infoOut);

        infoOut = manager.getAreaInfo("SomeClaim");
        assertEquals(info, infoOut);

        manager.removeAreaInfo(area);
        infoOut = manager.getAreaInfo(sloc);
        assertNull(infoOut);

        Whitebox.invokeMethod(manager, "onDisable");
    }

    @Test
    public void testResizeClaim() throws Exception {
        TestPlugin mockBase = PowerMock.createMock(TestPlugin.class);
        Logger mockLogger = PowerMock.createMock(Logger.class);

        File file = new File("ClaimManagerTestSuite2");

        EasyMock.expect(mockBase.isEnabled()).andReturn(true).anyTimes();
        EasyMock.expect(mockBase.getDataFolder()).andStubReturn(file);
        EasyMock.expect(mockBase.getPluginConfig()).andReturn(new TestConfig());
        EasyMock.expect(mockBase.getLogger()).andStubReturn(mockLogger);

        mockLogger.info(EasyMock.anyString());
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>() {

            @Override
            public Void answer() throws Throwable {
                System.out.println(EasyMock.getCurrentArguments()[0]);
                return null;
            }

        }).anyTimes();

        PowerMock.replay(mockBase, mockLogger);

        TestManager manager = new TestManager(mockBase, PluginManager.NORM_PRIORITY);
        manager.createFileDB().clear();
        Whitebox.invokeMethod(manager, "onEnable");

        Area area = new Area(new SimpleLocation("world", -5, -5, -5), new SimpleLocation("world", -1, -1, -1));
        TestClaimInfo info = new TestClaimInfo(area);
        info.setName("TestClaim1");

        manager.setAreaInfo(area, info);

        System.out.println("Init:");
        System.out.println("Current caches: " + Whitebox.getField(manager.getClass(), "cachedElements").get(manager));
        System.out.println("Current namemap: " + Whitebox.getField(manager.getClass(), "nameMap").get(manager));

        TestClaimInfo infoOut;
        Area newArea;

        infoOut = manager.getAreaInfo(new SimpleLocation("world", -3, -3, -2));

        newArea = new Area(new SimpleLocation("world", -5, 0, 0), new SimpleLocation("world", 10, 5, 7));
        System.out.println("Resize -5,-5,-5,-1,-1,-1 -> -5,0,0,10,5,7:");
        manager.resizeArea(infoOut.area, newArea);

        System.out.println("Current caches: " + Whitebox.getField(manager.getClass(), "cachedElements").get(manager));
        System.out.println("Current namemap: " + Whitebox.getField(manager.getClass(), "nameMap").get(manager));
        assertEquals(new TestClaimInfo(newArea), manager.getAreaInfo("TestClaim1"));

        infoOut = manager.getAreaInfo("TestClaim1");

        newArea = new Area(new SimpleLocation("world", -50, -10, -50), new SimpleLocation("world", 0, 0, 0));
        System.out.println("Resize -5,0,0,10,5,7 -> -50,-10,-50,0,0,0:");
        manager.resizeArea(infoOut.area, newArea);

        System.out.println("Current caches: " + Whitebox.getField(manager.getClass(), "cachedElements").get(manager));
        System.out.println("Current namemap: " + Whitebox.getField(manager.getClass(), "nameMap").get(manager));
        assertEquals(new TestClaimInfo(newArea), manager.getAreaInfo("TestClaim1"));

        newArea = new Area(new SimpleLocation("world", -50, -10, -50), new SimpleLocation("world", 0, 0, 0));
        System.out.println("Resize -50,-10,-50,0,0,0 -> -50,-10,-50,0,0,0 (fail test):");

        System.out.println("Current caches: " + Whitebox.getField(manager.getClass(), "cachedElements").get(manager));
        System.out.println("Current namemap: " + Whitebox.getField(manager.getClass(), "nameMap").get(manager));
        assertFalse(manager.resizeArea(infoOut.area, newArea));

        Whitebox.invokeMethod(manager, "onDisable");
    }

    public static class TestPlugin extends PluginBase {

        @Override
        protected void preEnable() {
            // TODO Auto-generated method stub

        }

    }

    public static class TestManager extends RegionManager<TestPlugin, TestClaimInfo> {

        public TestManager(TestPlugin base, int loadPriority) {
            super(base, loadPriority);
        }

        @Override
        protected String getTableName() {
            return "Claims";
        }

        @Override
        protected Type getType() {
            return TestClaimInfo.class;
        }

        @Override
        protected CacheUpdateHandle<Area, TestClaimInfo> getUpdateHandle() {
            return new CacheUpdateHandle<Area, TestClaimInfo>() {

                @Override
                public TestClaimInfo onUpdate(Area area, TestClaimInfo original) {
                    original.area = area;
                    return null;
                }

            };
        }
    }

    public static class TestClaimInfo implements ClaimInfo {
        transient Area area;
        String areaName;

        public TestClaimInfo(Area area) {
            super();
            this.area = area;
        }

        @Override
        public String getName() {
            return areaName;
        }

        public void setName(String areaName) {
            this.areaName = areaName;
        }

        @Override
        public Area getArea() {
            return area;
        }

        @Override
        public boolean isPublic() {
            return false;
        }

        @Override
        public UUID getOwner() {
            return null;
        }

        @Override
        public Set<UUID> getTrusts() {
            return new HashSet<>();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((area == null) ? 0 : area.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TestClaimInfo other = (TestClaimInfo) obj;
            if (area == null) {
                if (other.area != null)
                    return false;
            } else if (!area.equals(other.area))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "TestClaimInfo [area=" + area + "]";
        }

        @Override
        public void setArea(Area area) {
            this.area = area;
        }

        @Override
        public void setOwner(UUID uuid) {
            // TODO Auto-generated method stub

        }

    }
}
