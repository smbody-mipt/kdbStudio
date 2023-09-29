package studio.ui;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import studio.kdb.MockQSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StopQueryTest extends StudioTest {

    @BeforeClass
    public static void init() {
        MockQSession.mock();
        MockQSession.lockResponse(true);
    }

    @AfterClass
    public static void restore() {
        MockQSession.unlockResponse();
        MockQSession.lockResponse(false);
    }

    @Test
    public void stopActionTest() {
        setServerConnectionText("s:1");
        MockQSession.resetAllQueryCount();

        frameFixture.button("toolbarStop").requireDisabled();

        frameFixture.textBox("editor1").enterText("x").selectAll();
        frameFixture.button("toolbarExecute").click();

        frameFixture.button("toolbarExecute").requireDisabled();
        frameFixture.button("toolbarStop").requireEnabled();

        frameFixture.button("toolbarStop").click();
        MockQSession.unlockResponse();

        frameFixture.button("toolbarExecute").requireEnabled();
        frameFixture.button("toolbarStop").requireDisabled();

        int count = getTabCount(frameFixture.tabbedPane("ResultTabbedPane").target());
        assertEquals(0, count);


        MockQSession[] sessions = MockQSession.getLastActiveSessions();
        assertEquals(1, sessions.length);
        assertTrue(sessions[0].isClosed());
        int sessionCount = MockQSession.getAllSessions().size();

        MockQSession.setEchoMode();
        frameFixture.button("toolbarExecute").click();

        assertEquals(sessionCount + 1, MockQSession.getAllSessions().size());

        count = getTabCount(frameFixture.tabbedPane("ResultTabbedPane").target());
        assertEquals(1, count);

    }
}
