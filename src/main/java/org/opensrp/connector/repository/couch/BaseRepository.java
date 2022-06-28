/**
 *
 */
package org.opensrp.connector.repository.couch;

import org.ektorp.BulkDeleteDocument;
import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.GenerateView;
import org.smartregister.domain.BaseDataEntity;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Samuel Githengi created on 02/28/20
 */
public abstract class BaseRepository<T extends BaseDataEntity> extends CouchDbRepositorySupport<T> {

    private Class<T> type;

    protected BaseRepository(Class<T> type, CouchDbConnector db) {
        super(type, db);
        this.type = type;
        initStandardDesignDocument();
    }

    protected void addOrReplace(T entity, String businessFieldName, String businessId) {
        List<T> entities = entities(businessFieldName, businessId);
        if (entities.size() == 0)
            add(entity);
        else if (entities.size() == 1) {
            T entityInDb = entities.get(0);
            entity.setId(entityInDb.getId());
            entity.setRevision(entityInDb.getRevision());
            update(entity);
        } else
            throw new IllegalStateException(String.format("Duplicate entities found for %s:%s", businessFieldName, businessId));
    }

    private List<T> entities(String businessFieldName, String businessId) {
        String viewName = String.format("by_%s", businessFieldName);
        ViewQuery q = createQuery(viewName).key(businessId).includeDocs(true);
        return db.queryView(q, type);
    }

    public void removeAll(String fieldName, String value) {
        List<T> entities = entities(fieldName, value);
        removeAll(entities);
    }

    private void removeAll(List<T> entities) {
        List<BulkDeleteDocument> bulkDeleteQueue = new ArrayList<BulkDeleteDocument>(entities.size());
        for (T entity : entities) {
            bulkDeleteQueue.add(BulkDeleteDocument.of(entity));
        }
        db.executeBulk(bulkDeleteQueue);
    }

    public void removeAll() {
        removeAll(getAll());
    }

    public void safeRemove(T entity) {
        if (contains(entity.getId()))
            remove(entity);
    }

    @Override
    @GenerateView
    public List<T> getAll() {
        return super.getAll();
    }

    protected List<T> getAll(int limit) {
        ViewQuery q = createQuery("all").limit(limit).includeDocs(true);
        return db.queryView(q, type);
    }

    protected T singleResult(List<T> resultSet) {
        return (resultSet == null || resultSet.isEmpty()) ? null : resultSet.get(0);
    }

    public List<T> queryViewWithKeyList(String viewName, List<String> keys) {
        return db.queryView(createQuery(viewName).includeDocs(true).keys(keys), type);
    }
}
