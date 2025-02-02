package studio.kdb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import studio.utils.Content;
import studio.utils.LineEnding;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Workspace {

    private final List<TopWindow> windows = new ArrayList<>();
    private int selectedWindow = -1;

    private final static String SELECTED_WINDOW = "selectedWindow";
    private final static String DIVIDER_LOCATION = "dividerLocation";
    private final static String RESULT_DIVIDER_LOCATION = "resultDividerLocation";
    private final static String X = "x";
    private final static String Y = "y";
    private final static String WIDTH = "width";
    private final static String HEIGHT = "height";
    private final static String WINDOW = "window";
    private final static String TAB = "tab";
    private final static String LEFT = "left";
    private final static String RIGHT = "right";
    private final static String VERTICAL_SPLIT = "verticalSplit";
    private final static String SELECTED_TAB = "selectedTab";
    private final static String FILENAME = "filename";
    private final static String SERVER_FULLNAME = "serverFullname";
    private final static String SERVER_CONNECTION = "serverConnection";
    private final static String SERVER_AUTH = "serverAuth";
    private final static String CONTENT = "content";
    private final static String MODIFIED = "modified";
    private final static String LINE_ENDING = "lineEnding";
    private final static String CARET = "caret";


    private final static Logger log = LogManager.getLogger();

    public int getSelectedWindow() {
        return selectedWindow;
    }

    public TopWindow[] getWindows() {
        return windows.toArray(new TopWindow[0]);
    }

    public void save(Properties p) {
        p.setProperty(SELECTED_WINDOW, "" + selectedWindow);
        for (int index = 0; index<windows.size(); index++) {
            Window window = windows.get(index);
            window.save(WINDOW + "." + index + ".", p);
        }
    }

    private static int getInt(Properties p, String key, int defValue) {
        try {
            return Integer.parseInt(p.getProperty(key, "" + defValue));
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    private static double getDouble(Properties p, String key, double defValue) {
        try {
            return Double.parseDouble(p.getProperty(key, "" + defValue));
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    private static boolean hasKeysWithPrefix(Properties p, String prefix) {
        for(Object key : p.keySet() ) {
            if (key.toString().startsWith(prefix)) return true;
        }
        return false;
    }

    public void load(Properties p) {
        windows.clear();
        for (int index = 0; ; index++) {
            String prefix = WINDOW + "." + index + ".";
            if (! hasKeysWithPrefix(p, prefix)) break;

            Workspace.TopWindow window = new Workspace.TopWindow();
            window.load(prefix, p);
            windows.add(window);
        }
        selectedWindow = getInt(p, SELECTED_WINDOW, -1);
    }

    public TopWindow addWindow(boolean selected) {
        TopWindow window = new TopWindow();
        windows.add(window);
        if (selected) {
            selectedWindow = windows.size()-1;
        }
        return window;
    }

    public static class Window {

        private final List<Tab> tabs = new ArrayList<>();
        private int selectedTab = -1;
        private Window left, right;
        private boolean verticalSplit;
        private double dividerLocation = 0.5;

        Window() {}

        public Window(Server server, String filename) {
            tabs.add(new Tab(server,filename));
            selectedTab = 0;
        }

        public int getSelectedTab() {
            return selectedTab;
        }

        public Tab[] getAllTabs() {
            List<Tab> list = new ArrayList<>();
            addAllTabs(list);
            return list.toArray(new Tab[0]);
        }

        private void addAllTabs(List<Tab> list) {
            if (left != null) left.addAllTabs(list);
            if (right != null) right.addAllTabs(list);
            list.addAll(tabs);
        }

        public Tab addTab(boolean selected) {
            Tab tab = new Tab();
            tabs.add(tab);
            if (selected) {
                selectedTab = tabs.size()-1;
            }
            return tab;
        }

        public boolean isSplit() {
            return left != null || right != null;
        }

        public Window getLeft() {
            return left;
        }

        public Window getRight() {
            return right;
        }

        public Window addLeft() {
            return left = new Window();
        }

        public Window addRight() {
            return right = new Window();
        }

        public boolean isVerticalSplit() {
            return verticalSplit;
        }

        public void setVerticalSplit(boolean verticalSplit) {
            this.verticalSplit = verticalSplit;
        }

        public double getDividerLocation() {
            return dividerLocation;
        }

        public void setDividerLocation(double dividerLocation) {
            this.dividerLocation = dividerLocation;
        }

        protected void save(String prefix, Properties p) {
            if (left != null && right != null) {
                p.setProperty(prefix + VERTICAL_SPLIT, Boolean.toString(verticalSplit));
                p.setProperty(prefix + DIVIDER_LOCATION, Double.toString(dividerLocation));
                left.save(prefix + LEFT + ".", p);
                right.save(prefix + RIGHT + ".", p);
            } else {
                p.setProperty(prefix + SELECTED_TAB, "" + selectedTab);
                for (int index = 0; index < tabs.size(); index++) {
                    Tab tab = tabs.get(index);
                    tab.save(prefix + TAB + "." + index + ".", p);
                }
            }
        }

        protected void load(String prefix, Properties p) {
            tabs.clear();
            for (int index = 0; ; index++) {
                Workspace.Tab tab = new Workspace.Tab();
                tab.load(prefix + TAB + "." + index + ".", p);
                if (tab.getContent() == null) break;
                tabs.add(tab);
            }
            selectedTab = getInt(p, prefix + SELECTED_TAB, -1);

            verticalSplit = Boolean.parseBoolean(p.getProperty(prefix + VERTICAL_SPLIT, "false"));
            dividerLocation = Double.parseDouble(p.getProperty(prefix + DIVIDER_LOCATION, "0.5"));
            left = loadChild(prefix + LEFT + ".", p);
            right = loadChild(prefix + RIGHT + ".", p);
        }

        private Window loadChild(String prefix, Properties p) {
            if (! hasKeysWithPrefix(p, prefix)) return null;
            Window window = new Window();
            window.load(prefix, p);
            return window;
        }
    }


    public static class TopWindow extends Window {
        private double resultDividerLocation = 0.5;
        private Rectangle location;

        public TopWindow() {}

        public TopWindow(Server server, String filename) {
            super(server, filename);
            location = new Rectangle(-1,-1, 0,0);
        }

        public double getResultDividerLocation() {
            return resultDividerLocation;
        }

        public void setResultDividerLocation(double resultDividerLocation) {
            this.resultDividerLocation = resultDividerLocation;
        }

        public Rectangle getLocation() {
            return location;
        }

        public void setLocation(Rectangle location) {
            this.location = location;
        }

        protected void load(String prefix, Properties p) {
            resultDividerLocation = Double.parseDouble(p.getProperty(prefix + RESULT_DIVIDER_LOCATION, "0.5"));
            int x = Integer.parseInt(p.getProperty(prefix + X, "-1"));
            int y = Integer.parseInt(p.getProperty(prefix + Y, "-1"));
            int width = Integer.parseInt(p.getProperty(prefix + WIDTH, "0"));
            int height = Integer.parseInt(p.getProperty(prefix + HEIGHT, "0"));
            location = new Rectangle(x,y, width, height);

            super.load(prefix, p);
        }

        protected void save(String prefix, Properties p) {
            p.setProperty(prefix + RESULT_DIVIDER_LOCATION, Double.toString(resultDividerLocation));
            if (location != null) {
                p.setProperty(prefix + X, Integer.toString(location.x));
                p.setProperty(prefix + Y, Integer.toString(location.y));
                p.setProperty(prefix + WIDTH, Integer.toString(location.width));
                p.setProperty(prefix + HEIGHT, Integer.toString(location.height));
            }

            super.save(prefix, p);
        }
    }

    public static class Tab {
        private String filename = null;
        private String serverFullName = null;
        private String serverConnection = null;
        private String serverAuth = null;
        private String content = "";
        private boolean modified = false;
        private LineEnding lineEnding = LineEnding.Unix;
        private int caret = 0;

        Tab() {}

        public Tab(Server server, String filename) {
            this.filename = filename;
            lineEnding = Config.getInstance().getEnum(Config.DEFAULT_LINE_ENDING);
            if (server != Server.NO_SERVER) {
                serverFullName = server.getFullName();
                serverConnection = server.getConnectionStringWithPwd();
                serverAuth = server.getAuthenticationMechanism();
            }
        }

        public String getFilename() {
            return filename;
        }

        public String getServerFullName() {
            return serverFullName;
        }

        public String getServerConnection() {
            return serverConnection;
        }

        public String getServerAuth() {
            return serverAuth;
        }

        public String getContent() {
            return content;
        }

        public boolean isModified() {
            return modified;
        }

        public LineEnding getLineEnding() {
            return lineEnding;
        }

        public int getCaret() {
            return caret;
        }

        public Tab addFilename(String filename) {
            this.filename = filename;
            return this;
        }

        public Tab addServer(Server server) {
            if (server == Server.NO_SERVER) return this;

            if (server.getFolder() == null) {
                serverFullName = null;
            } else {
                serverFullName = server.getFullName();
            }
            serverConnection = server.getConnectionStringWithPwd();
            serverAuth = server.getAuthenticationMechanism();
            return this;
        }

        public Tab addContent(String content) {
            modified = true;
            this.content = content;
            return this;
        }

        public Tab addContent(Content content) {
            return addContent(content.getContent())
                    .setLineEnding(content.getLineEnding())
                    .setModified(content.isModified());
        }

        public Tab setModified(boolean modified) {
            this.modified = modified;
            return this;
        }

        public Tab setLineEnding(LineEnding lineEnding) {
            this.lineEnding = lineEnding;
            return this;
        }

        public Tab setCaret(int caret) {
            this.caret = caret;
            return this;
        }

        private void save(String prefix, Properties p) {
            if (filename != null) p.setProperty(prefix + FILENAME, filename);
            if (serverFullName != null) p.setProperty(prefix + SERVER_FULLNAME, serverFullName);
            if (serverConnection != null) p.setProperty(prefix + SERVER_CONNECTION, serverConnection);
            if (serverAuth != null) p.setProperty(prefix + SERVER_AUTH, serverAuth);
            if (content != null) p.setProperty(prefix + CONTENT, content);
            p.setProperty(prefix + MODIFIED, Boolean.toString(modified));
            p.setProperty(prefix + LINE_ENDING, lineEnding.toString());
            p.setProperty(prefix + CARET, Integer.toString(caret));
        }

        private void load(String prefix, Properties p) {
            filename = p.getProperty(prefix + FILENAME);
            serverFullName = p.getProperty(prefix + SERVER_FULLNAME);
            serverConnection = p.getProperty(prefix + SERVER_CONNECTION);
            serverAuth = p.getProperty(prefix + SERVER_AUTH);
            content = p.getProperty(prefix + CONTENT);
            modified = Boolean.parseBoolean(p.getProperty(prefix + MODIFIED, "false"));

            try {
                lineEnding = LineEnding.valueOf(p.getProperty(prefix + LINE_ENDING, "Unix"));
            } catch (IllegalArgumentException e) {
                log.error("Failed to parse {} of key {}", p.getProperty(prefix + LINE_ENDING, "Unix"), prefix + LINE_ENDING, e);
            }

            try {
                caret = Integer.parseInt(p.getProperty(prefix + CARET, "0"));
            } catch (NumberFormatException e) {
                log.error("Failed to parse {} of key {}", p.getProperty(prefix + CARET, "0"), prefix + CARET, e);
            }
        }
    }
}
