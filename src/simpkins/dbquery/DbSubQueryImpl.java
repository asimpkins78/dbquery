package simpkins.dbquery;

import simpkins.query.Query;
import simpkins.query.QueryMap;
import org.hibernate.criterion.*;
import org.hibernate.sql.JoinType;
import org.hibernate.type.Type;

import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("Convert2Diamond")
public class DbSubQueryImpl<TOuterEntity, TInnerEntity, TSource extends DbSubQueryable> implements DbSubQuery<TOuterEntity, TInnerEntity, TSource> {
    private TSource source;
    private Class<TInnerEntity> type;
    private EntityRelationship<TOuterEntity, TInnerEntity> entityRelationship;
    private Consumer<Criterion> sourceAddCriterion;
    private Consumer<EntityExpression<TOuterEntity, ?>> sourcePrepareJoinedDefinitions;
    private DetachedCriteria detachedCriteria;
    private Stack<Junction> junctions = new Stack<Junction>();
    private QueryMap<String, String> aliases = new QueryMap<String, String>();
    private boolean isLimited = false;
    private Integer firstResult;
    private Integer maxResults;

    public DbSubQueryImpl(TSource source, EntityRelationship<TOuterEntity, TInnerEntity> entityRelationship, Consumer<Criterion> sourceAddCriterion, Consumer<EntityExpression<TOuterEntity, ?>> sourcePrepareJoinedDefinitions) {
        this.source = source;
        this.type = entityRelationship.getColumnType();
        this.entityRelationship = entityRelationship;
        this.sourceAddCriterion = sourceAddCriterion;
        this.sourcePrepareJoinedDefinitions = sourcePrepareJoinedDefinitions;

        // TODO can we support this?
        if (entityRelationship.isJoined())
            throw new RuntimeException("Joined relationships not supported for subqueries!");
    }

    public DbSubQueryImpl(TSource source, Class<TInnerEntity> type, Consumer<Criterion> sourceAddCriterion, Consumer<EntityExpression<TOuterEntity, ?>> sourcePrepareJoinedDefinitions) {
        this.source = source;
        this.type = type;
        this.sourceAddCriterion = sourceAddCriterion;
        this.sourcePrepareJoinedDefinitions = sourcePrepareJoinedDefinitions;
    }

    private DetachedCriteria getDetachedCriteria() {
        if (detachedCriteria == null) {
            boolean isRelated = entityRelationship != null;
            String subTableAlias = isRelated && !entityRelationship.isCollection()
                    ? entityRelationship.getFieldMapping()
                    : type.getSimpleName().toLowerCase();
            detachedCriteria = DetachedCriteria.forClass(type, subTableAlias);
            if (isRelated) {
                String subTableColumn = entityRelationship.isCollection()
                        ? subTableAlias + "." + entityRelationship.getFkSourceFieldMapping()
                        : subTableAlias + "." + entityRelationship.getFkTargetFieldMapping();
                String outerTableColumn = entityRelationship.isCollection()
                        ? source.getCriteriaAlias() + "." + entityRelationship.getFkTargetFieldMapping()
                        : source.getCriteriaAlias() + "." + entityRelationship.getFkSourceFieldMapping();
                detachedCriteria.add(Restrictions.eqProperty(subTableColumn, outerTableColumn));
            }
        }
        return detachedCriteria;
    }

    @Override
    public DetachedCriteria getCriteria() {
        return getDetachedCriteria();
    }

    @Override
    public String getCriteriaAlias() {
        return getCriteria().getAlias();
    }

    @Override
    public DbSubQuery<TOuterEntity, TInnerEntity, TSource> addCriterion(Criterion criterion) {
        if (junctions.empty())
            getDetachedCriteria().add(criterion);
        else
            junctions.peek().add(criterion);
        return this;
    }

    @Override
    public <TRelation> DbSubQuery<TInnerEntity, TRelation, DbSubQuery<TOuterEntity, TInnerEntity, TSource>> subQuery(RelationDefinition<TInnerEntity, TRelation> relationDefinition) {
        return new DbSubQueryImpl<TInnerEntity, TRelation, DbSubQuery<TOuterEntity, TInnerEntity, TSource>>(this, relationDefinition, this::addCriterion, this::prepareJoinedDefinitions);
    }

    @Override
    public <TRelation> DbSubQuery<TInnerEntity, TRelation, DbSubQuery<TOuterEntity, TInnerEntity, TSource>> subQuery(CollectionRelationDefinition<TInnerEntity, TRelation> collectionRelationDefinition) {
        return new DbSubQueryImpl<TInnerEntity, TRelation, DbSubQuery<TOuterEntity, TInnerEntity, TSource>>(this, collectionRelationDefinition, this::addCriterion, this::prepareJoinedDefinitions);
    }

    @Override
    public <TRelation> DbSubQuery<TInnerEntity, TRelation, DbSubQuery<TOuterEntity, TInnerEntity, TSource>> subQuery(Class<TRelation> subQueryType) {
        return new DbSubQueryImpl<TInnerEntity, TRelation, DbSubQuery<TOuterEntity, TInnerEntity, TSource>>(this, subQueryType, this::addCriterion, this::prepareJoinedDefinitions);
    }

    private void prepareJoinedDefinitions(EntityExpression<TInnerEntity, ?> entityExpression) {
        for (EntityDefinition<TInnerEntity, ?> entityDefinition : entityExpression.getEntityDefinitions()) {
            if (!entityDefinition.isJoined())
                continue;
            for (int i = 1; i < entityDefinition.getFullPath().size(); i++) {
                String route = entityDefinition.getFullPath().take(i).select(x -> x.getFieldMapping()).toString(".");
                String alias = route.replace('.', '_');
                if (!aliases.containsKey(alias)) {
                    aliases.put(alias, route);
                    getDetachedCriteria().createAlias(route, alias, JoinType.LEFT_OUTER_JOIN);
                }
            }
        }
    }

    @Override
    public <TValue> DbWhereCriteria<DbSubQuery<TOuterEntity, TInnerEntity, TSource>, TInnerEntity, TValue> where(EntityExpression<TInnerEntity, TValue> entityExpression) {
        prepareJoinedDefinitions(entityExpression);
        return new DbWhereCriteriaImpl<DbSubQuery<TOuterEntity, TInnerEntity, TSource>, TInnerEntity, TValue>(this, entityExpression, this::addCriterion, this::prepareJoinedDefinitions);
    }

    public <TValue> DbWhereCriteria<DbSubQuery<TOuterEntity, TInnerEntity, TSource>, TInnerEntity, TValue> whereOuter(ColumnDefinition<TOuterEntity, TValue> columnDefinition) {
        sourcePrepareJoinedDefinitions.accept(columnDefinition);
        return new DbWhereCriteriaImpl<DbSubQuery<TOuterEntity, TInnerEntity, TSource>, TInnerEntity, TValue>(this, new OuterColumnExpression<>(source.getCriteriaAlias(), columnDefinition, type), this::addCriterion, this::prepareJoinedDefinitions);
    }

    @Override
    public DbSubQuery<TOuterEntity, TInnerEntity, TSource> whereTrue(EntityExpression<TInnerEntity, ?> entityExpression) {
        prepareJoinedDefinitions(entityExpression);
        addCriterion(entityExpression.asEntityCustomExpression().toHibernateSql());
        return this;
    }

    @Override
    public DbSubQuery<TOuterEntity, TInnerEntity, TSource> whereNotTrue(EntityExpression<TInnerEntity, ?> entityExpression) {
        prepareJoinedDefinitions(entityExpression);
        addCriterion(new EntityCustomExpression<>("not %s", entityExpression).toHibernateSql());
        return this;
    }

    @Override
    public DbSubQuery<TOuterEntity, TInnerEntity, TSource> openDisjunction() {
        Junction disjunction = Restrictions.disjunction();
        addCriterion(disjunction);
        junctions.push(disjunction);
        return this;
    }

    @Override
    public DbSubQuery<TOuterEntity, TInnerEntity, TSource> openConjunction() {
        Junction conjunction = Restrictions.conjunction();
        addCriterion(conjunction);
        junctions.push(conjunction);
        return this;
    }

    @Override
    public DbSubQuery<TOuterEntity, TInnerEntity, TSource> close() {
        if (junctions.empty())
            throw new RuntimeException("Cannot call close when there is no active junction.");
        junctions.pop();
        return this;
    }

    @Override
    public DbSubQuery<TOuterEntity, TInnerEntity, TSource> when(boolean isTrue, Consumer<DbSubQuery<TOuterEntity, TInnerEntity, TSource>> doThis) {
        if (isTrue)
            doThis.accept(this);
        return this;
    }

    @Override
    public DbSubQuery<TOuterEntity, TInnerEntity, TSource> orderBy(EntityExpression<TInnerEntity, ?> entityExpression) {
        return orderBy(entityExpression, false, false, false);
    }

    @Override
    public DbSubQuery<TOuterEntity, TInnerEntity, TSource> orderByIgnoreCase(EntityExpression<TInnerEntity, ?> entityExpression) {
        return orderBy(entityExpression, false, false, true);
    }

    @Override
    public DbSubQuery<TOuterEntity, TInnerEntity, TSource> orderByNullsFirst(EntityExpression<TInnerEntity, ?> entityExpression) {
        return orderBy(entityExpression, false, true, false);
    }

    @Override
    public DbSubQuery<TOuterEntity, TInnerEntity, TSource> orderByNullsFirstIgnoreCase(EntityExpression<TInnerEntity, ?> entityExpression) {
        return orderBy(entityExpression, false, true, true);
    }

    @Override
    public DbSubQuery<TOuterEntity, TInnerEntity, TSource> orderByDescending(EntityExpression<TInnerEntity, ?> entityExpression) {
        return orderBy(entityExpression, true, true, false);
    }

    @Override
    public DbSubQuery<TOuterEntity, TInnerEntity, TSource> orderByDescendingIgnoreCase(EntityExpression<TInnerEntity, ?> entityExpression) {
        return orderBy(entityExpression, true, true, true);
    }

    @Override
    public DbSubQuery<TOuterEntity, TInnerEntity, TSource> orderByDescendingNullsLast(EntityExpression<TInnerEntity, ?> entityExpression) {
        return orderBy(entityExpression, true, false, false);
    }

    @Override
    public DbSubQuery<TOuterEntity, TInnerEntity, TSource> orderByDescendingNullsLastIgnoreCase(EntityExpression<TInnerEntity, ?> entityExpression) {
        return orderBy(entityExpression, true, false, true);
    }

    private DbSubQuery<TOuterEntity, TInnerEntity, TSource> orderBy(EntityExpression<TInnerEntity, ?> entityExpression, boolean isDescending, boolean isNullsFirst, boolean isIgnoreCase) {
        prepareJoinedDefinitions(entityExpression);
        getDetachedCriteria().addOrder(DbQueryOrder.create(entityExpression, isDescending, isNullsFirst, isIgnoreCase));
        return this;
    }

    @Override
    public DbSubQuery<TOuterEntity, TInnerEntity, TSource> limit(Integer firstResult, Integer maxResults) {
        if (isLimited)
            throw new RuntimeException("Cannot limit sub-query more than once.");
        if (firstResult != null || maxResults != null) {
            isLimited = true;
            if (firstResult != null && firstResult != 0)
                this.firstResult = firstResult;
            if (maxResults != null)
                this.maxResults = maxResults;
        }
        return this;
    }

    @Override
    public TSource exists() {
        source.addCriterion(Subqueries.exists(getDetachedCriteria().setProjection(Projections.sqlProjection("true", new String[0], new Type[0]))));
        return source;
    }

    @Override
    public TSource notExists() {
        source.addCriterion(Subqueries.notExists(getDetachedCriteria().setProjection(Projections.sqlProjection("true", new String[0], new Type[0]))));
        return source;
    }

    @Override
    public <TValue> DbSubQueryMultipleResolution<TSource, TOuterEntity, TValue> select(EntityExpression<TInnerEntity, TValue> entityExpression) {
        prepareJoinedDefinitions(entityExpression);
        getDetachedCriteria().setProjection(entityExpression.isCustom()
                ? entityExpression.asEntityCustomExpression().toHibernateSql()
                : Projections.property(entityExpression.getEntityDefinitionFieldMapping()));
        return new DbSubQueryResolution<>(source, getDetachedCriteria(), sourceAddCriterion, sourcePrepareJoinedDefinitions, firstResult, maxResults);
    }

    @Override
    public DbSubQueryMultipleResolution<TSource, TOuterEntity, ?> select(EntityExpression<TInnerEntity, ?>... selects) {
        ProjectionList projectionList = Projections.projectionList();
        for (EntityExpression<TInnerEntity, ?> select : selects) {
            prepareJoinedDefinitions(select);
            projectionList.add(select.isCustom()
                    ? select.asEntityCustomExpression().toHibernateSql()
                    : Projections.property(select.getEntityDefinitionFieldMapping()));
        }
        getDetachedCriteria().setProjection(projectionList);
        return new DbSubQueryResolution<>(source, getDetachedCriteria(), sourceAddCriterion, sourcePrepareJoinedDefinitions, firstResult, maxResults);
    }

    @Override
    public <TValue> DbSubQuerySingleResolution<TSource, TOuterEntity, TValue> selectSingleOrNull(EntityExpression<TInnerEntity, TValue> select) {
        return (DbSubQuerySingleResolution)select(select);
    }

    @Override
    public DbSubQuerySingleResolution<TSource, TOuterEntity, ?> selectSingleOrNull(EntityExpression<TInnerEntity, ?>... selects) {
        return (DbSubQuerySingleResolution)select(selects);
    }

    @Override
    public <TValue> DbSubQuerySingleResolution<TSource, TOuterEntity, TValue> selectFirstOrNull(EntityExpression<TInnerEntity, TValue> select) {
        return (DbSubQuerySingleResolution)limit(0, 1).select(select);
    }

    @Override
    public DbSubQuerySingleResolution<TSource, TOuterEntity, ?> selectFirstOrNull(EntityExpression<TInnerEntity, ?>... selects) {
        return (DbSubQuerySingleResolution)limit(0, 1).select(selects);
    }

    private class OuterColumnExpression<TValue> implements EntityExpression<TInnerEntity, TValue> {
        private String path;
        private ColumnDefinition<TOuterEntity, TValue> columnDefinition;
        private Class<TInnerEntity> innerEntityClass;

        protected OuterColumnExpression(String path, ColumnDefinition<TOuterEntity, TValue> columnDefinition, Class<TInnerEntity> innerEntityClass) {
            this.path = path;
            this.columnDefinition = columnDefinition;
            this.innerEntityClass = innerEntityClass;
        }

        @Override
        public Class<TInnerEntity> getEntityType() {
            return innerEntityClass;
        }

        @Override
        public Class<TValue> getType() {
            return columnDefinition.getType();
        }

        @Override
        public boolean isCustom() {
            return columnDefinition.isCustom();
        }

        @Override
        public boolean isCollection() {
            return columnDefinition.isCollection();
        }

        @Override
        public boolean isNullable() {
            return columnDefinition.isNullable();
        }

        @Override
        public EntityCustomExpression<TInnerEntity, TValue> asEntityCustomExpression() {
            return EntityCustomExpression.from(this);
        }

        @Override
        public String getEntityDefinitionFieldMapping() {
            return path + "." + columnDefinition.getEntityDefinitionFieldMapping();
        }

        @Override
        public Query<EntityDefinition<TInnerEntity, ?>> getEntityDefinitions() {
            throw new RuntimeException("Not supported for OuterColumnExpression!");
        }

        @Override
        public Query<Object> getNonEntityDefinitions() {
            return columnDefinition.getNonEntityDefinitions();
        }

        @Override
        public String toSql(Function<String, String> columnMapper, boolean unmasked) {
            return columnMapper.apply(getEntityDefinitionFieldMapping());
        }

        @Override
        public ColumnAggregate<TInnerEntity, Long> count() {
            throw new RuntimeException("Not supported for OuterColumnExpression!");
        }

        @Override
        public ColumnAggregate<TInnerEntity, Long> countDistinct() {
            throw new RuntimeException("Not supported for OuterColumnExpression!");
        }

        @Override
        public ColumnAggregate<TInnerEntity, TValue> sum() {
            throw new RuntimeException("Not supported for OuterColumnExpression!");
        }

        @Override
        public ColumnAggregate<TInnerEntity, Double> avg() {
            throw new RuntimeException("Not supported for OuterColumnExpression!");
        }

        @Override
        public ColumnAggregate<TInnerEntity, TValue> min() {
            throw new RuntimeException("Not supported for OuterColumnExpression!");
        }

        @Override
        public ColumnAggregate<TInnerEntity, TValue> max() {
            throw new RuntimeException("Not supported for OuterColumnExpression!");
        }

        @Override
        public ColumnAggregate<TInnerEntity, Boolean> any() {
            throw new RuntimeException("Not supported for OuterColumnExpression!");
        }

        @Override
        public ColumnAggregate<TInnerEntity, Boolean> all() {
            throw new RuntimeException("Not supported for OuterColumnExpression!");
        }

        @Override
        public ColumnAggregate<TInnerEntity, Boolean> none() {
            throw new RuntimeException("Not supported for OuterColumnExpression!");
        }

        @Override
        public ColumnAggregate<TInnerEntity, TValue> groupBy() {
            throw new RuntimeException("Not supported for OuterColumnExpression!");
        }

        @Override
        public EntityExpression<TInnerEntity, TValue> lower() {
            throw new RuntimeException("Not supported for OuterColumnExpression!");
        }

        @Override
        public EntityExpression<TInnerEntity, TValue> upper() {
            throw new RuntimeException("Not supported for OuterColumnExpression!");
        }

        @Override
        public EntityExpression<TInnerEntity, TValue> coalesce(Object... others) {
            throw new RuntimeException("Not supported for OuterColumnExpression!");
        }

        @Override
        public String getKey() {
            return columnDefinition.getKey();
        }
    }
}
