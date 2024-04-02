package bll;

import dal.db.ArchivedEvent_DB;

import java.util.List;

public class ArchivedEventManager {

    private final ArchivedEvent_DB archivedEvent_DB;

    public ArchivedEventManager() throws Exception { archivedEvent_DB = new ArchivedEvent_DB();  }

    public List<be.Event> getAllEvents() throws Exception { return archivedEvent_DB.getAllArchivedEvents(); }

    public be.Event archiveEvent(be.Event newEvent) throws Exception { return archivedEvent_DB.ArchiveEvent(newEvent); }

    public void deleteEvent(be.Event selectedEvent) throws Exception { archivedEvent_DB.deleteArchivedEvent(selectedEvent); }
}
