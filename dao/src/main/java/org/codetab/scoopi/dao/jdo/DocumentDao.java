package org.codetab.scoopi.dao.jdo;

import static org.apache.commons.lang3.Validate.notNull;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.codetab.scoopi.dao.IDocumentDao;
import org.codetab.scoopi.model.Document;

/**
 * <p>
 * JDO DocumentDao implementation.
 * @author Maithilish
 *
 */
public final class DocumentDao implements IDocumentDao {

    /**
     * JDO PMF.
     */
    private PersistenceManagerFactory pmf;

    /**
     * <p>
     * Constructor.
     * @param pmf
     *            JDO PMF
     */
    public DocumentDao(final PersistenceManagerFactory pmf) {
        notNull(pmf, "pmf must not be null");
        this.pmf = pmf;
    }

    /**
     * <p>
     * Get document (detached copy) from id. It fetches document with
     * documentObject (i.e. actual contents).
     * @param id
     * @return document
     */
    @Override
    public Document getDocument(final long id) {
        Document document = null;
        PersistenceManager pm = getPM();
        try {
            Object result = pm.getObjectById(Document.class, id);
            // document with documentObject
            pm.getFetchPlan().addGroup("detachDocumentObject"); //$NON-NLS-1$
            document = (Document) pm.detachCopy(result);
        } finally {
            pm.close();
        }
        return document;
    }

    /**
     * <p>
     * Get persistence manager from PersistenceManagerFactory.
     * @return persistence manager
     */
    private PersistenceManager getPM() {
        PersistenceManager pm = pmf.getPersistenceManager();
        return pm;
    }

}
