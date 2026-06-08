package org.shanoir.ng.shared.repository;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Subgraph;
import org.shanoir.ng.dataset.model.DatasetExpression;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShanoirRepositoryImpl<T> {

    @PersistenceContext
    private EntityManager em;

    private Class<T> entityClass;

    public ShanoirRepositoryImpl() {

        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();

        entityClass = (Class<T>) type.getActualTypeArguments()[0];
    }

    public T findWithSpecificRelations(Long id, List<String> relationNames) {
        EntityGraph<T> graph = em.createEntityGraph(entityClass);
        addRelationsToGraph(graph, relationNames);

        Map<String, Object> hints = new HashMap<>();
        hints.put("jakarta.persistence.fetchgraph", graph);

        return em.find(entityClass, id, hints);
    }

    public T findWithSpecificSubRelations(Long id, List<String> relationNames) {
        EntityGraph<T> graph = em.createEntityGraph(entityClass);
        addSubRelationsToGraph(graph, relationNames);

        Map<String, Object> hints = new HashMap<>();
        hints.put("jakarta.persistence.fetchgraph", graph);

        return em.find(entityClass, id, hints);
    }

    public List<T> findListWithSpecificRelations(List<Long> ids, List<String> relationNames) {
        EntityGraph<T> graph = em.createEntityGraph(entityClass);
        addRelationsToGraph(graph, relationNames);

        return em.createQuery(
                        "select d from " + entityClass + " d where d.id in :ids",
                        entityClass
                )
                .setParameter("ids", ids)
                .setHint("jakarta.persistence.fetchgraph", graph)
                .getResultList();
    }

    public List<T> findListWithSpecificSubRelation(List<Long> ids, List<String> relationNames) {
        EntityGraph<DatasetExpression> graph = em.createEntityGraph(DatasetExpression.class);
        addSubRelationsToGraph(graph, relationNames);

        return em.createQuery(
                        "select d from " + entityClass + " d where d.id in :ids",
                        entityClass
                )
                .setParameter("ids", ids)
                .setHint("jakarta.persistence.fetchgraph", graph)
                .getResultList();
    }

    protected void addRelationsToGraph(EntityGraph graph, List<String> relationNames) {
        for (String relationName : relationNames) {
            graph.addAttributeNodes(relationName);
        }
    }

    protected void addSubRelationsToGraph(EntityGraph graph, List<String> relationNames) {
        for (String relationName : relationNames) {
            if (relationName.contains(".")) {
                Subgraph<?> subGraph = graph.addSubgraph(relationName.substring(0, relationName.indexOf(".")));
                subGraph.addAttributeNodes(relationName.substring(relationName.indexOf(".") + 1));
            } else {
                Subgraph<?> subGraph = graph.addSubgraph(relationName);
                subGraph.addAttributeNodes("id");
            }
        }
    }
}
