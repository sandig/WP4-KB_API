package org.ul.asap.webapp.entry;


public class ASAPEntryFactory {

    public static <T extends MyEntry> T getInstance(Class<T> clazz, String id) {
        if (clazz.getSimpleName().equals("ApplicationComponent")) return (T) new ApplicationComponent(id);
        return null;
    }
}
