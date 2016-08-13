package simpkins.dbquery;

import simpkins.query.*;
import simpkins.query.Query;
import org.hibernate.*;
import org.hibernate.criterion.*;
import org.hibernate.sql.JoinType;

import java.util.*;
import java.lang.reflect.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings({"Convert2Diamond", "RedundantCast"})
public class DbQueryImpl<TEntity> implements DbQuery<TEntity> {
    private Class<TEntity> type;
    private Session session;
    private Criteria criteria = null;
    private Stack<Junction> junctions = new Stack<Junction>();
    private QuerySet<String> loadWiths = new QuerySet<String>();
    private QueryMap<String, EntityRelationship<TEntity, ?>> loadAfters = new QueryMap<String, EntityRelationship<TEntity, ?>>();
    private QuerySet<String> fetchJoins = new QuerySet<String>();
    private QueryMap<String, String> aliasJoinsByAlias = new QueryMap<String, String>();
    private QueryList<String> orderings = new QueryList<String>();
    private boolean isLimited = false;
    private boolean isDenormalized = false;
    private RuntimeException noResultException = null;
    private RuntimeException multipleResultsException = null;
    private boolean loadWithAllJoins = false;

    public DbQueryImpl(Class<TEntity> type, Session session) {
        this.type = type;
        this.session = session;
        this.criteria = session.createCriteria(type);
    }

    @Override
    public Criteria getCriteria() {
        return criteria;
    }

    @Override
    public String getCriteriaAlias() {
        return getCriteria().getAlias();
    }

    @Override
    public DbQuery<TEntity> addCriterion(Criterion criterion) {
        if (junctions.empty())
            criteria.add(criterion);
        else
            junctions.peek().add(criterion);
        return this;
    }

    @Override
    public DbQuery<TEntity> loadWith(EntityRelationship<TEntity, ?>... entityRelationships) {
        return loadWith(Arrays.asList(entityRelationships));
    }

    @Override
    public DbQuery<TEntity> loadWith(Iterable<EntityRelationship<TEntity, ?>> entityRelationships) {
        for (EntityRelationship<TEntity, ?> entityRelationship : entityRelationships) {
            loadWith(entityRelationship.getFullPath().select(x -> x.getFieldMapping()).toString("."));
            if (entityRelationship.isCollection())
                isDenormalized = true;
        }
        return this;
    }

    private void loadWith(String joinPath) {
        loadWiths.add(joinPath);
        QueryList<String> joins = QueryList.of(joinPath.split("\\."));
        for (int i = 1; i <= joins.size(); i++) {
            String fetchJoin = joins.take(i).toString(".");
            if (!fetchJoins.contains(fetchJoin)) {
                criteria.setFetchMode(fetchJoin, FetchMode.JOIN);
                fetchJoins.add(fetchJoin);
            }
        }
    }

    @Override
    public DbQuery<TEntity> loadAfter(EntityRelationship<TEntity, ?>... entityRelationships) {
        return loadAfter(Arrays.asList(entityRelationships));
    }

    @Override
    public DbQuery<TEntity> loadAfter(Iterable<EntityRelationship<TEntity, ?>> entityRelationships) {
        for (EntityRelationship<TEntity, ?> entityRelationship : entityRelationships) {
            String key = entityRelationship.getFullPath().select(x -> x.getFieldMapping()).toString(".");
            if (!loadAfters.containsKey(key))
                loadAfters.put(key, entityRelationship);
        }
        return this;
    }

    @Override
    public <TSubEntity> DbSubQuery<TEntity, TSubEntity, DbQuery<TEntity>> subQuery(RelationDefinition<TEntity, TSubEntity> relationDefinition) {
        return new DbSubQueryImpl<TEntity, TSubEntity, DbQuery<TEntity>>(this, relationDefinition, this::addCriterion, this::prepareJoinedDefinitions);
    }

    @Override
    public <TSubEntity> DbSubQuery<TEntity, TSubEntity, DbQuery<TEntity>> subQuery(CollectionRelationDefinition<TEntity, TSubEntity> collectionRelationDefinition) {
        return new DbSubQueryImpl<TEntity, TSubEntity, DbQuery<TEntity>>(this, collectionRelationDefinition, this::addCriterion, this::prepareJoinedDefinitions);
    }

    @Override
    public <TSubEntity> DbSubQuery<TEntity, TSubEntity, DbQuery<TEntity>> subQuery(Class<TSubEntity> subQueryType) {
        return new DbSubQueryImpl<TEntity, TSubEntity, DbQuery<TEntity>>(this, subQueryType, this::addCriterion, this::prepareJoinedDefinitions);
    }

    @Override
    public <TValue> DbWhereCriteria<DbQuery<TEntity>, TEntity, TValue> where(EntityExpression<TEntity, TValue> entityExpression) {
        prepareJoinedDefinitions(entityExpression);
        return new DbWhereCriteriaImpl<DbQuery<TEntity>, TEntity, TValue>(this, entityExpression, this::addCriterion, this::prepareJoinedDefinitions);
    }

    @Override
    public DbQuery<TEntity> whereTrue(EntityExpression<TEntity, ?> entityExpression) {
        prepareJoinedDefinitions(entityExpression);
        addCriterion(entityExpression.asEntityCustomExpression().toHibernateSql());
        return this;
    }

    @Override
    public DbQuery<TEntity> whereNotTrue(EntityExpression<TEntity, ?> entityExpression) {
        prepareJoinedDefinitions(entityExpression);
        addCriterion(new EntityCustomExpression<>("not %s", entityExpression).toHibernateSql());
        return this;
    }

    @Override
    public DbQuery<TEntity> openDisjunction() {
        Junction disjunction = Restrictions.disjunction();
        addCriterion(disjunction);
        junctions.push(disjunction);
        return this;
    }

    @Override
    public DbQuery<TEntity> openConjunction() {
        Junction conjunction = Restrictions.conjunction();
        addCriterion(conjunction);
        junctions.push(conjunction);
        return this;
    }

    @Override
    public DbQuery<TEntity> close() {
        if (junctions.empty())
            throw new RuntimeException("Cannot call close when there is no active junction.");
        junctions.pop();
        return this;
    }

    @Override
    public DbQuery<TEntity> when(boolean isTrue, Consumer<DbQuery<TEntity>> doThis) {
        if (isTrue)
            doThis.accept(this);
        return this;
    }

    @Override
    public DbQuery<TEntity> orderBy(EntityExpression<TEntity, ?> entityExpression) {
        return orderBy(entityExpression, false, false, false);
    }

    @Override
    public DbQuery<TEntity> orderByIgnoreCase(EntityExpression<TEntity, ?> entityExpression) {
        return orderBy(entityExpression, false, false, true);
    }

    @Override
    public DbQuery<TEntity> orderByNullsFirst(EntityExpression<TEntity, ?> entityExpression) {
        return orderBy(entityExpression, false, true, false);
    }

    @Override
    public DbQuery<TEntity> orderByNullsFirstIgnoreCase(EntityExpression<TEntity, ?> entityExpression) {
        return orderBy(entityExpression, false, true, true);
    }

    @Override
    public DbQuery<TEntity> orderByDescending(EntityExpression<TEntity, ?> entityExpression) {
        return orderBy(entityExpression, true, true, false);
    }

    @Override
    public DbQuery<TEntity> orderByDescendingIgnoreCase(EntityExpression<TEntity, ?> entityExpression) {
        return orderBy(entityExpression, true, true, true);
    }

    @Override
    public DbQuery<TEntity> orderByDescendingNullsLast(EntityExpression<TEntity, ?> entityExpression) {
        return orderBy(entityExpression, true, false, false);
    }

    @Override
    public DbQuery<TEntity> orderByDescendingNullsLastIgnoreCase(EntityExpression<TEntity, ?> entityExpression) {
        return orderBy(entityExpression, true, false, true);
    }

    private DbQuery<TEntity> orderBy(EntityExpression<TEntity, ?> entityExpression, boolean isDescending, boolean isNullsFirst, boolean isIgnoreCase) {
        prepareJoinedDefinitions(entityExpression);
        orderings.add(isIgnoreCase ? "lower(" + entityExpression.getKey() + ")" : entityExpression.getKey());
        criteria.addOrder(DbQueryOrder.create(entityExpression, isDescending, isNullsFirst, isIgnoreCase));
        return this;
    }

    @Override
    public DbQuery<TEntity> limit(Integer firstResult, Integer maxResults) {
        if (isLimited)
            throw new RuntimeException("Cannot limit query more than once.");
        if (firstResult != null || maxResults != null) {
            isLimited = true;
            if (firstResult != null)
                criteria.setFirstResult(firstResult);
            if (maxResults != null)
                criteria.setMaxResults(maxResults);
        }
        return this;
    }

    @Override
    public DbQuery<TEntity> setNoResultException(RuntimeException noResultException) {
        this.noResultException = noResultException;
        return this;
    }

    @Override
    public DbQuery<TEntity> setMultipleResultsException(RuntimeException multipleResultsException) {
        this.multipleResultsException = multipleResultsException;
        return this;
    }

    @Override
    public DbQuery<TEntity> loadWithAllJoins() {
        this.loadWithAllJoins = true;
        // load with any previous alias joins
        for (String aliasJoin : aliasJoinsByAlias.values())
            loadWith(aliasJoin);
        return this;
    }

    @Override
    public QueryList<TEntity> toList() {
        return doEntityQuery(type);
    }

    @Override
    public <T> QueryList<T> toList(Class<T> type) {
        return doEntityQuery(type);
    }

    @Override
    public TEntity[] toArray() {
        return doEntityQuery(type).toArray(type);
    }

    @Override
    public <T> T[] toArray(Class<T> type) {
        return doEntityQuery(type).toArray(type);
    }

    @Override
    public <TKey> QueryMap<TKey, TEntity> map(FieldDefinition<TEntity, TKey> key) {
        if (key instanceof EntityRelationship)
            loadWith((EntityRelationship)key);
        return new QueryMap<TKey, TEntity>(doEntityQuery(type), key::apply);
    }

    @Override
    public <TKey, TValue> QueryMap<TKey, TValue> map(EntityExpression<TEntity, TKey> key, EntityExpression<TEntity, TValue> value) {
        //noinspection unchecked
        return new QueryMap<TKey, TValue>(doProjection(key, value),
                projection -> (TKey) projection[0],
                projection -> (TValue) projection[1]);
    }

    @Override
    public <TKey> QueryMap<TKey, DbResultRow> map(EntityExpression<TEntity, TKey> key, EntityExpression<TEntity, ?>... values) {
        return map(key, Arrays.asList(values));
    }

    @Override
    public <TKey> QueryMap<TKey, DbResultRow> map(EntityExpression<TEntity, TKey> key, Iterable<EntityExpression<TEntity, ?>> values) {
        QueryList<EntityExpression<TEntity, ?>> projections = values instanceof Collection
                ? QueryList.initialCapacity(((Collection)values).size() + 1)
                : new QueryList<EntityExpression<TEntity, ?>>();
        projections.add(key);
        projections.addAll(values);

        //noinspection unchecked
        return new QueryMap<TKey, DbResultRow>(doProjection(projections),
                projection -> (TKey) projection[0],
                projection -> new DbResultRow(Arrays.copyOfRange(projection, 1, projections.size()), values));
    }

    @Override
    public <TKey> QueryGroup<TKey, TEntity> groupBy(FieldDefinition<TEntity, TKey> groupBy) {
        if (groupBy instanceof EntityRelationship)
            loadWith((EntityRelationship)groupBy);
        return new QueryGroup<TKey, TEntity>(doEntityQuery(type), groupBy::apply);
    }

    @Override
    public <TKey, TValue> QueryGroup<TKey, TValue> groupBy(EntityExpression<TEntity, TKey> groupBy, EntityExpression<TEntity, TValue> value) {
        //noinspection unchecked
        return new QueryGroup<TKey, TValue>(doProjection(groupBy, value),
                projection -> (TKey) projection[0],
                projection -> (TValue) projection[1]);
    }

    @Override
    public <TKey> QueryGroup<TKey, DbResultRow> groupBy(EntityExpression<TEntity, TKey> key, EntityExpression<TEntity, ?>... values) {
        return groupBy(key, Arrays.asList(values));
    }

    @Override
    public <TKey> QueryGroup<TKey, DbResultRow> groupBy(EntityExpression<TEntity, TKey> key, Iterable<EntityExpression<TEntity, ?>> values) {
        QueryList<EntityExpression<TEntity, ?>> projections = values instanceof Collection
                ? QueryList.initialCapacity(((Collection)values).size() + 1)
                : new QueryList<EntityExpression<TEntity, ?>>();
        projections.add(key);
        projections.addAll(values);

        //noinspection unchecked
        return new QueryGroup<TKey, DbResultRow>(doProjection(projections),
                projection -> (TKey) projection[0],
                projection -> new DbResultRow(Arrays.copyOfRange(projection, 1, projections.size()), values));
    }

    @Override
    public <TValue> QueryList<TValue> select(EntityExpression<TEntity, TValue> select) {
        //noinspection unchecked
        return doProjection(select).select(projection -> (TValue)projection[0]).toList();
    }

    @Override
    public <TValue> QueryList<TValue> select(RelationBaseDefinition<TEntity, TValue> select) {
        return loadWith(select).toList().select(select::apply).toList();
    }

    @Override
    public <TValue> QueryList<TValue> select(CollectionRelationBaseDefinition<TEntity, TValue> select) {
        return loadWith(select).toList().selectMany(select::apply).toList();
    }

    @Override
    public QueryList<DbResultRow> select(EntityExpression<TEntity, ?>... selects) {
        return select(Arrays.asList(selects));
    }

    @Override
    public QueryList<DbResultRow> select(Iterable<EntityExpression<TEntity, ?>> selects) {
        return doProjection(selects).select(projection -> new DbResultRow(projection, selects)).toList();
    }

    @Override
    public <TValue> TValue selectSingle(EntityExpression<TEntity, TValue> select) {
        return getOne(false, true, () -> select(select));
    }

    @Override
    public <TValue> TValue selectSingle(RelationBaseDefinition<TEntity, TValue> select) {
        return select.apply(loadWith(select).single());
    }

    @Override
    public <TValue> QueryList<TValue> selectSingle(CollectionRelationBaseDefinition<TEntity, TValue> select) {
        return QueryList.of(select.apply(loadWith(select).single()));
    }

    @Override
    public <TValue> TValue selectSingleOrNull(EntityExpression<TEntity, TValue> select) {
        return getOne(true, true, () -> select(select));
    }

    @Override
    public <TValue> TValue selectSingleOrNull(RelationBaseDefinition<TEntity, TValue> select) {
        return loadWith(select).singleOptional().map(select::apply).orElse(null);
    }

    @Override
    public <TValue> QueryList<TValue> selectSingleOrNull(CollectionRelationBaseDefinition<TEntity, TValue> select) {
        return loadWith(select).singleOptional().map(x -> QueryList.of(select.apply(x))).orElse(null);
    }

    @Override
    public <TValue> Optional<TValue> selectSingleOptional(EntityExpression<TEntity, TValue> select) {
        return Optional.ofNullable(selectSingleOrNull(select));
    }

    @Override
    public <TValue> Optional<TValue> selectSingleOptional(RelationBaseDefinition<TEntity, TValue> select) {
        return loadWith(select).singleOptional().map(select::apply);
    }

    @Override
    public <TValue> Optional<QueryList<TValue>> selectSingleOptional(CollectionRelationBaseDefinition<TEntity, TValue> select) {
        return loadWith(select).singleOptional().map(x -> QueryList.of(select.apply(x)));
    }

    @Override
    public <TValue> TValue selectFirst(EntityExpression<TEntity, TValue> select) {
        return getOne(false, false, () -> select(select));
    }

    @Override
    public <TValue> TValue selectFirst(RelationBaseDefinition<TEntity, TValue> select) {
        return select.apply(loadWith(select).first());
    }

    @Override
    public <TValue> QueryList<TValue> selectFirst(CollectionRelationBaseDefinition<TEntity, TValue> select) {
        return QueryList.of(select.apply(loadWith(select).first()));
    }

    @Override
    public <TValue> TValue selectFirstOrNull(EntityExpression<TEntity, TValue> select) {
        return getOne(true, false, () -> select(select));
    }

    @Override
    public <TValue> TValue selectFirstOrNull(RelationBaseDefinition<TEntity, TValue> select) {
        return loadWith(select).firstOptional().map(select::apply).orElse(null);
    }

    @Override
    public <TValue> QueryList<TValue> selectFirstOrNull(CollectionRelationBaseDefinition<TEntity, TValue> select) {
        return loadWith(select).firstOptional().map(x -> QueryList.of(select.apply(x))).orElse(null);
    }

    @Override
    public <TValue> Optional<TValue> selectFirstOptional(EntityExpression<TEntity, TValue> select) {
        return Optional.ofNullable(selectFirstOrNull(select));
    }

    @Override
    public <TValue> Optional<TValue> selectFirstOptional(RelationBaseDefinition<TEntity, TValue> select) {
        return loadWith(select).firstOptional().map(select::apply);
    }

    @Override
    public <TValue> Optional<QueryList<TValue>> selectFirstOptional(CollectionRelationBaseDefinition<TEntity, TValue> select) {
        return loadWith(select).firstOptional().map(x -> QueryList.of(select.apply(x)));
    }

    @Override
    public DbResultRow selectSingle(EntityExpression<TEntity, ?>... selects) {
        return selectSingle(Arrays.asList(selects));
    }

    @Override
    public DbResultRow selectSingle(Iterable<EntityExpression<TEntity, ?>> selects) {
        return getOne(false, true, () -> select(selects));
    }

    @Override
    public DbResultRow selectSingleOrNull(EntityExpression<TEntity, ?>... selects) {
        return selectSingleOrNull(Arrays.asList(selects));
    }

    @Override
    public DbResultRow selectSingleOrNull(Iterable<EntityExpression<TEntity, ?>> selects) {
        return getOne(true, true, () -> select(selects));
    }

    @Override
    public Optional<DbResultRow> selectSingleOptional(EntityExpression<TEntity, ?>... selects) {
        return Optional.ofNullable(selectSingleOrNull(selects));
    }

    @Override
    public Optional<DbResultRow> selectSingleOptional(Iterable<EntityExpression<TEntity, ?>> selects) {
        return Optional.ofNullable(selectSingleOrNull(selects));
    }

    @Override
    public DbResultRow selectFirst(EntityExpression<TEntity, ?>... selects) {
        return selectFirst(Arrays.asList(selects));
    }

    @Override
    public DbResultRow selectFirst(Iterable<EntityExpression<TEntity, ?>> selects) {
        return getOne(false, false, () -> select(selects));
    }

    @Override
    public DbResultRow selectFirstOrNull(EntityExpression<TEntity, ?>... selects) {
        return selectFirstOrNull(Arrays.asList(selects));
    }

    @Override
    public DbResultRow selectFirstOrNull(Iterable<EntityExpression<TEntity, ?>> selects) {
        return getOne(true, false, () -> select(selects));
    }

    @Override
    public Optional<DbResultRow> selectFirstOptional(EntityExpression<TEntity, ?>... selects) {
        return Optional.ofNullable(selectFirstOrNull(selects));
    }

    @Override
    public Optional<DbResultRow> selectFirstOptional(Iterable<EntityExpression<TEntity, ?>> selects) {
        return Optional.ofNullable(selectFirstOrNull(selects));
    }

    @Override
    public <TValue> QueryList<TValue> selectDistinct(EntityExpression<TEntity, TValue> select) {
        return selectDistinct(Collections.singletonList(select)).select(queryResultRow -> queryResultRow.get(select)).toList();
    }

    @Override
    public QueryList<DbResultRow> selectDistinct(EntityExpression<TEntity, ?>... selects) {
        return selectDistinct(Arrays.asList(selects));
    }

    @Override
    public QueryList<DbResultRow> selectDistinct(Iterable<EntityExpression<TEntity, ?>> selects) {
        QueryList<String> unselectedOrderings = orderings.except(Query.from(selects).select(x -> x.getKey())).toList();
        if (!unselectedOrderings.isEmpty())
            throw new RuntimeException("All order by expressions must also be present as select distinct expressions: " + unselectedOrderings.toString(", "));
        return doProjection(selects, true).select(x -> new DbResultRow(x, selects)).toList();
    }

    @Override
    public <TValue> TValue selectDistinctSingle(EntityExpression<TEntity, TValue> select) {
        return getOne(false, true, () -> selectDistinct(select));
    }

    @Override
    public <TValue> TValue selectDistinctSingleOrNull(EntityExpression<TEntity, TValue> select) {
        return getOne(true, true, () -> selectDistinct(select));
    }

    @Override
    public <TValue> Optional<TValue> selectDistinctSingleOptional(EntityExpression<TEntity, TValue> select) {
        return Optional.ofNullable(selectDistinctSingleOrNull(select));
    }

    @Override
    public <TValue> TValue selectDistinctFirst(EntityExpression<TEntity, TValue> select) {
        return getOne(false, false, () -> selectDistinct(select));
    }

    @Override
    public <TValue> TValue selectDistinctFirstOrNull(EntityExpression<TEntity, TValue> select) {
        return getOne(true, false, () -> selectDistinct(select));
    }

    @Override
    public <TValue> Optional<TValue> selectDistinctFirstOptional(EntityExpression<TEntity, TValue> select) {
        return Optional.ofNullable(selectDistinctSingleOrNull(select));
    }

    @Override
    public DbResultRow selectDistinctSingle(EntityExpression<TEntity, ?>... selects) {
        return selectDistinctSingle(Arrays.asList(selects));
    }

    @Override
    public DbResultRow selectDistinctSingle(Iterable<EntityExpression<TEntity, ?>> selects) {
        return getOne(false, true, () -> selectDistinct(selects));
    }

    @Override
    public DbResultRow selectDistinctSingleOrNull(EntityExpression<TEntity, ?>... selects) {
        return selectDistinctSingleOrNull(Arrays.asList(selects));
    }

    @Override
    public DbResultRow selectDistinctSingleOrNull(Iterable<EntityExpression<TEntity, ?>> selects) {
        return getOne(true, true, () -> selectDistinct(selects));
    }

    @Override
    public Optional<DbResultRow> selectDistinctSingleOptional(EntityExpression<TEntity, ?>... selects) {
        return Optional.ofNullable(selectDistinctSingleOrNull(selects));
    }

    @Override
    public Optional<DbResultRow> selectDistinctSingleOptional(Iterable<EntityExpression<TEntity, ?>> selects) {
        return Optional.ofNullable(selectDistinctSingleOrNull(selects));
    }

    @Override
    public DbResultRow selectDistinctFirst(EntityExpression<TEntity, ?>... selects) {
        return selectDistinctFirst(Arrays.asList(selects));
    }

    @Override
    public DbResultRow selectDistinctFirst(Iterable<EntityExpression<TEntity, ?>> selects) {
        return getOne(false, false, () -> selectDistinct(selects));
    }

    @Override
    public DbResultRow selectDistinctFirstOrNull(EntityExpression<TEntity, ?>... selects) {
        return selectDistinctFirstOrNull(Arrays.asList(selects));
    }

    @Override
    public DbResultRow selectDistinctFirstOrNull(Iterable<EntityExpression<TEntity, ?>> selects) {
        return getOne(true, false, () -> selectDistinct(selects));
    }

    @Override
    public Optional<DbResultRow> selectDistinctFirstOptional(EntityExpression<TEntity, ?>... selects) {
        return Optional.ofNullable(selectDistinctFirstOrNull(selects));
    }

    @Override
    public Optional<DbResultRow> selectDistinctFirstOptional(Iterable<EntityExpression<TEntity, ?>> selects) {
        return Optional.ofNullable(selectDistinctFirstOrNull(selects));
    }

    private Supplier<List<TEntity>> doEntityQuerySupplier = () -> doEntityQuery(type);

    @Override
    public TEntity single() {
        return getOne(false, true, doEntityQuerySupplier);
    }

    @Override
    public TEntity singleOrNull() {
        return getOne(true, true, doEntityQuerySupplier);
    }

    @Override
    public Optional<TEntity> singleOptional() {
        return Optional.ofNullable(singleOrNull());
    }

    @Override
    public TEntity first() {
        return getOne(false, false, doEntityQuerySupplier);
    }

    @Override
    public TEntity firstOrNull() {
        return getOne(true, false, doEntityQuerySupplier);
    }

    @Override
    public Optional<TEntity> firstOptional() {
        return Optional.ofNullable(firstOrNull());
    }

    @Override
    public boolean exists() {
        return selectFirstOrNull(new EntityCustomExpression<TEntity, Boolean>("true")) != null;
    }

    @Override
    public boolean notExists() {
        return !exists();
    }

    @Override
    public long count() {
        return (Long)criteria.setProjection(Projections.rowCount()).uniqueResult();
    }

    @Override
    public long count(EntityExpression<TEntity, ?> entityExpression) {
        prepareJoinedDefinitions(entityExpression);
        return (Long)criteria.setProjection(entityExpression.isCustom()
                ? new EntityCustomExpression<>(Long.class, "count(%s)", entityExpression).toHibernateSql()
                : Projections.count(entityExpression.getEntityDefinitionFieldMapping())).uniqueResult();
    }

    @Override
    public long countDistinct(EntityExpression<TEntity, ?> entityExpression) {
        prepareJoinedDefinitions(entityExpression);
        return (Long) criteria.setProjection(entityExpression.isCustom()
                ? new EntityCustomExpression<>(Long.class, "distinct count(%s)", entityExpression).toHibernateSql()
                : Projections.countDistinct(entityExpression.getEntityDefinitionFieldMapping())).uniqueResult();
    }

    @Override
    public <TValue> TValue sum(EntityExpression<TEntity, TValue> entityExpression) {
        prepareJoinedDefinitions(entityExpression);
        //noinspection unchecked
        return (TValue) criteria.setProjection(entityExpression.isCustom()
                ? new EntityCustomExpression<>("sum(%s)", entityExpression).toHibernateSql()
                : Projections.sum(entityExpression.getEntityDefinitionFieldMapping())).uniqueResult();
    }

    @Override
    public <TValue> TValue min(EntityExpression<TEntity, TValue> entityExpression) {
        prepareJoinedDefinitions(entityExpression);
        //noinspection unchecked
        return (TValue) criteria.setProjection(entityExpression.isCustom()
                ? new EntityCustomExpression<>("min(%s)", entityExpression).toHibernateSql()
                : Projections.min(entityExpression.getEntityDefinitionFieldMapping())).uniqueResult();
    }

    @Override
    public <TValue> TValue max(EntityExpression<TEntity, TValue> entityExpression) {
        prepareJoinedDefinitions(entityExpression);
        //noinspection unchecked
        return (TValue) criteria.setProjection(entityExpression.isCustom()
                ? new EntityCustomExpression<>("max(%s)", entityExpression).toHibernateSql()
                : Projections.max(entityExpression.getEntityDefinitionFieldMapping())).uniqueResult();
    }

    @Override
    public Boolean any(EntityExpression<TEntity, Boolean> entityExpression) {
        prepareJoinedDefinitions(entityExpression);
        //noinspection unchecked
        return (Boolean) criteria.setProjection(new EntityCustomExpression<>("bool_or(%s)", entityExpression).toHibernateSql()).uniqueResult();
    }

    @Override
    public Boolean all(EntityExpression<TEntity, Boolean> entityExpression) {
        prepareJoinedDefinitions(entityExpression);
        //noinspection unchecked
        return (Boolean) criteria.setProjection(new EntityCustomExpression<>("bool_and(%s)", entityExpression).toHibernateSql()).uniqueResult();
    }

    @Override
    public Boolean none(EntityExpression<TEntity, Boolean> entityExpression) {
        prepareJoinedDefinitions(entityExpression);
        //noinspection unchecked
        return (Boolean) criteria.setProjection(new EntityCustomExpression<>("not bool_or(%s)", entityExpression).toHibernateSql()).uniqueResult();
    }

    @Override
    public Double average(EntityExpression<TEntity, ?> entityExpression) {
        prepareJoinedDefinitions(entityExpression);
        return (Double) criteria.setProjection(entityExpression.isCustom()
                ? new EntityCustomExpression<>(Double.class, "avg(%s)", entityExpression).toHibernateSql()
                : Projections.avg(entityExpression.getEntityDefinitionFieldMapping())).uniqueResult();
    }

    @Override
    public QueryList<DbResultRow> aggregate(ColumnAggregate<TEntity, ?>... aggregates) {
        return aggregate(Arrays.asList(aggregates));
    }

    @Override
    public QueryList<DbResultRow> aggregate(Iterable<ColumnAggregate<TEntity, ?>> aggregates) {
        Query<EntityExpression<TEntity, ?>> groupByColumns = Query.from(aggregates)
                .where(item -> item.getType() == ColumnAggregate.Type.GROUP_BY)
                .select(columnAggregate -> columnAggregate.getEntityExpression());

        QueryList<String> unselectedOrderings = orderings.except(Query.from(groupByColumns).select(x -> x.getKey())).toList();
        if (!unselectedOrderings.isEmpty())
            throw new RuntimeException("All order by expressions must also be present as group by expressions: " + unselectedOrderings.toString(", "));

        ProjectionList projectionList = Projections.projectionList();
        for (ColumnAggregate<TEntity, ?> aggregate : aggregates) {
            EntityExpression<TEntity, ?> entityExpression = aggregate.getEntityExpression();
            prepareJoinedDefinitions(entityExpression);
            Projection projection;
            switch (aggregate.getType()) {
                case COUNT:
                    projection = entityExpression.isCustom()
                            ? new EntityCustomExpression<>(Long.class, "count(%s)", entityExpression).toHibernateSql()
                            : Projections.count(entityExpression.getEntityDefinitionFieldMapping());
                    break;
                case COUNT_DISTINCT:
                    projection = entityExpression.isCustom()
                            ? new EntityCustomExpression<>(Long.class, "distinct count(%s)", entityExpression).toHibernateSql()
                            : Projections.countDistinct(entityExpression.getEntityDefinitionFieldMapping());
                    break;
                case SUM:
                    projection = entityExpression.isCustom()
                            ? new EntityCustomExpression<>("sum(%s)", entityExpression).toHibernateSql()
                            : Projections.sum(entityExpression.getEntityDefinitionFieldMapping());
                    break;
                case AVG:
                    projection = entityExpression.isCustom()
                            ? new EntityCustomExpression<>(Double.class, "avg(%s)", entityExpression).toHibernateSql()
                            : Projections.avg(entityExpression.getEntityDefinitionFieldMapping());
                    break;
                case MIN:
                    projection = entityExpression.isCustom()
                            ? new EntityCustomExpression<>("min(%s)", entityExpression).toHibernateSql()
                            : Projections.min(entityExpression.getEntityDefinitionFieldMapping());
                    break;
                case MAX:
                    projection = entityExpression.isCustom()
                            ? new EntityCustomExpression<>("max(%s)", entityExpression).toHibernateSql()
                            : Projections.max(entityExpression.getEntityDefinitionFieldMapping());
                    break;
                case ANY:
                    projection = new EntityCustomExpression<>("bool_or(%s)", entityExpression).toHibernateSql();
                    break;
                case ALL:
                    projection = new EntityCustomExpression<>("bool_and(%s)", entityExpression).toHibernateSql();
                    break;
                case NONE:
                    projection = new EntityCustomExpression<>("not bool_or(%s)", entityExpression).toHibernateSql();
                    break;
                case GROUP_BY:
                    projection = entityExpression.isCustom()
                            ? entityExpression.asEntityCustomExpression().toHibernateSqlGrouped()
                            : Projections.groupProperty(entityExpression.getEntityDefinitionFieldMapping());
                    break;
                default:
                    throw new RuntimeException("Unsupported type: " + type);
            }
            projectionList.add(projection, aggregate.getKey());
        }

        //noinspection unchecked
        List<Object> results = (List<Object>)criteria.setProjection(projectionList).list();
        return Query.from(results)
                .select(x -> new DbResultRow(x != null && x.getClass().isArray() ? (Object[]) x : new Object[] { x }, aggregates))
                .toList();
    }

    @Override
    public QueryList<Long> delete() {
        return delete(this::getIds);
    }

    @Override
    public void deleteById(long id) {
        delete(Query.from(id));
    }

    @Override
    public void deleteById(Long... ids) {
        delete(Query.from(ids));
    }

    @Override
    public void deleteById(Iterable<Long> ids) {
        delete(Query.from(ids));
    }

    @Override
    public long deleteSingle() {
        return delete(this::singleId).single();
    }

    @Override
    public Long deleteSingleOrNull() {
        return delete(this::singleOrNullId).singleOrNull();
    }

    @Override
    public long deleteFirst() {
        return delete(this::firstId).single();
    }

    @Override
    public Long deleteFirstOrNull() {
        return delete(this::firstOrNullId).singleOrNull();
    }

    @Override
    public QueryList<Long> update(ColumnUpdate<TEntity, ?>... updates) {
        return update(Arrays.asList(updates));
    }

    @Override
    public QueryList<Long> update(Iterable<ColumnUpdate<TEntity, ?>> updates) {
        return update(updates, this::getIds);
    }

    @Override
    public void updateById(long id, ColumnUpdate<TEntity, ?>... updates) {
        update(Arrays.asList(updates), Query.from(id));
    }

    @Override
    public void updateById(long id, Iterable<ColumnUpdate<TEntity, ?>> updates) {
        update(updates, Query.from(id));
    }

    @Override
    public void updateById(Iterable<Long> ids, ColumnUpdate<TEntity, ?>... updates) {
        update(Arrays.asList(updates), Query.from(ids));
    }

    @Override
    public void updateById(Iterable<Long> ids, Iterable<ColumnUpdate<TEntity, ?>> updates) {
        update(updates, Query.from(ids));
    }

    @Override
    public long updateSingle(ColumnUpdate<TEntity, ?>... updates) {
        return updateSingle(Arrays.asList(updates));
    }

    @Override
    public long updateSingle(Iterable<ColumnUpdate<TEntity, ?>> updates) {
        return update(updates, this::singleId).single();
    }

    @Override
    public Long updateSingleOrNull(ColumnUpdate<TEntity, ?>... updates) {
        return updateSingleOrNull(Arrays.asList(updates));
    }

    @Override
    public Long updateSingleOrNull(Iterable<ColumnUpdate<TEntity, ?>> updates) {
        return update(updates, this::singleOrNullId).singleOrNull();
    }

    @Override
    public long updateFirst(ColumnUpdate<TEntity, ?>... updates) {
        return updateFirst(Arrays.asList(updates));
    }

    @Override
    public long updateFirst(Iterable<ColumnUpdate<TEntity, ?>> updates) {
        return update(updates, this::firstId).single();
    }

    @Override
    public Long updateFirstOrNull(ColumnUpdate<TEntity, ?>... updates) {
        return updateFirstOrNull(Arrays.asList(updates));
    }

    @Override
    public Long updateFirstOrNull(Iterable<ColumnUpdate<TEntity, ?>> updates) {
        return update(updates, this::firstOrNullId).singleOrNull();
    }

    private void prepareJoinedDefinitions(EntityExpression<TEntity, ?> entityExpression) {
        for (EntityDefinition<TEntity, ?> entityDefinition : entityExpression.getEntityDefinitions())
            if (entityDefinition.isJoined())
                prepareJoinedDefinition(entityDefinition);
    }

    private void prepareJoinedDefinition(EntityDefinition<TEntity, ?> entityDefinition) {
        if (entityDefinition.isJoined()) {
            for (int i = 1; i < entityDefinition.getFullPath().size(); i++) {
                String join = entityDefinition.getFullPath().take(i).select(x -> x.getFieldMapping()).toString(".");
                String alias = join.replace('.', '_');
                if (!aliasJoinsByAlias.containsKey(alias)) {
                    aliasJoinsByAlias.put(alias, join);
                    criteria.createAlias(join, alias, JoinType.LEFT_OUTER_JOIN);
                    if (loadWithAllJoins)
                        loadWith(join);
                }
            }
        }
        if (entityDefinition.isCollection())
            isDenormalized = true;
    }

    private <T> QueryList<T> doEntityQuery(Class<T> type) {
        QueryList<T> rootEntities;
        if (isLimited && isDenormalized || aliasJoinsByAlias.valuesQuery().except(fetchJoins).any()) {
            ColumnDefinition<TEntity, Long> idColumnDefinition = getIdColumnDefinition();
            if (idColumnDefinition == null)
                throw new RuntimeException("Cannot split query because " + type.getSimpleName() + " has no id field.");

            // retrieve just the ids for the query and map them with their index in the result set
            QueryMap<Long, Integer> ids = getIds(idColumnDefinition)
                    .selectByIndex((x, i) -> Tuple.create(x, i))
                    .map(x -> x.getItem1(), x -> x.getItem2());
            if (ids.isEmpty())
                return new QueryList<T>();

            // rebuild the query just based on the ids retrieved
            aliasJoinsByAlias.clear();
            fetchJoins.clear();
            criteria = session.createCriteria(type)
                    .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
                    .add(Restrictions.in(idColumnDefinition.getFieldMapping(), ids.keySet()));
            for (String loadWith : loadWiths)
                loadWith(loadWith);

            //noinspection unchecked
            rootEntities = orderings.any()
                    ? Query.from(criteria.list()).orderBy(x -> ids.get(idColumnDefinition.apply((TEntity)x))).toList()
                    : QueryList.of(criteria.list());
        }
        else {
            //noinspection unchecked
            rootEntities = QueryList.of(isDenormalized
                    ? criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list()
                    : criteria.list());
        }

        if (!rootEntities.isEmpty() && !loadAfters.isEmpty()) {
            // setup function to fetch field values with reflection
            TriFunction<Class, String, List, Set> reflectValues = (entityType, fieldName, sources) -> {
                Set<Object> results = new HashSet<Object>();
                try {
                    Field field = entityType.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    for (Object source : sources)
                        results.add(field.get(source));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // do nothing;
                }
                return results;
            };

            // cache results so that we don't repeat any queries
            QueryMap<EntityRelationship<?, ?>, List> resultsByRelationship = new QueryMap<EntityRelationship<?, ?>, List>();
            for (EntityRelationship<TEntity, ?> loadAfter : loadAfters.values()) {
                // all join paths start with the root entities
                List current = rootEntities;
                for (EntityRelationship<?, ?> entityRelationship : loadAfter.getFullPath().cast(EntityRelationship.class)) {
                    // if we've already pulled this relationship, then just fetch it from the cache
                    if (resultsByRelationship.containsKey(entityRelationship)) {
                        current = resultsByRelationship.get(entityRelationship);
                    }
                    // otherwise figure out the FK columns and do the query
                    else {
                        String fkSource = entityRelationship.getFkSourceFieldMapping();
                        String fkTarget = entityRelationship.getFkTargetFieldMapping();
                        Set fkValues = reflectValues.apply(entityRelationship.getEntityType(), entityRelationship.isCollection() ? fkTarget : fkSource, current);
                        current = session.createCriteria(entityRelationship.getColumnType())
                                .add(Restrictions.in(entityRelationship.isCollection() ? fkSource : fkTarget, fkValues))
                                .list();
                        resultsByRelationship.put(entityRelationship, current);
                    }

                    // if the current result is empty then we are done with this join path
                    if (current.isEmpty())
                        break;
                }
            }
        }

        return rootEntities;
    }

    private <T> T getOne(boolean isNull, boolean isSingle, Supplier<List<T>> getter) {
        if (isLimited)
            throw new RuntimeException("Cannot limit a first/single style query.");
        if (!isSingle)
            limit(0, 1);
        List<T> queryResult = getter.get();
        if (queryResult.isEmpty()) {
            if (isNull)
                return null;
            throw noResultException != null ? noResultException : new RuntimeException("Sequence contains no elements.");
        }
        if (isSingle && queryResult.size() > 1)
            throw multipleResultsException != null ? multipleResultsException : new NonUniqueResultException(queryResult.size());
        return queryResult.get(0);
    }

    private QueryList<Object[]> doProjection(EntityExpression<TEntity, ?>... entityExpressions) {
        return doProjection(Arrays.asList(entityExpressions));
    }

    private QueryList<Object[]> doProjection(Iterable<EntityExpression<TEntity, ?>> entityExpressions) {
        return doProjection(entityExpressions, false);
    }

    private QueryList<Object[]> doProjection(Iterable<EntityExpression<TEntity, ?>> entityExpressions, boolean isDistinct) {
        ProjectionList projectionList = Projections.projectionList();
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions) {
            prepareJoinedDefinitions(entityExpression);
            projectionList.add(entityExpression.isCustom()
                    ? entityExpression.asEntityCustomExpression().toHibernateSql()
                    : Projections.property(entityExpression.getEntityDefinitionFieldMapping()));
        }

        List projections = isDistinct
                ? criteria.setProjection(Projections.distinct(projectionList)).list()
                : criteria.setProjection(projectionList).list();
        QueryList<Object[]> results = QueryList.initialCapacity(projections.size());
        for (Object projection : projections)
            results.add(projection != null && projection.getClass().isArray() ? (Object[])projection : new Object[] {projection});
        return results;
    }

    private QueryList<Long> delete(Function<ColumnDefinition<TEntity, Long>, Query<Long>> idGetter) {
        return delete(getIds(idGetter));
    }

    private QueryList<Long> delete(Query<Long> ids) {
        QueryList<Long> idList = ids.toList();
        if (!idList.isEmpty())
            session.createQuery("delete " + type.getSimpleName() + " this_ where this_.id in (:ids)")
                    .setParameterList("ids", idList)
                    .executeUpdate();
        return idList;
    }

    private QueryList<Long> update(Iterable<ColumnUpdate<TEntity, ?>> updates, Function<ColumnDefinition<TEntity, Long>, Query<Long>> idGetter) {
        return update(updates, getIds(idGetter));
    }

    private QueryList<Long> update(Iterable<ColumnUpdate<TEntity, ?>> updates, Query<Long> ids) {
        QueryList<Long> idList = ids.toList();
        if (!idList.isEmpty()) {
            String setters = Query.from(updates).select(x -> "this_." + x.getColumnDefinition().getFieldMapping() + " = " +
                    (x.isValueExpression()
                            ? x.getValueExpression().toSql(y -> "this_." + y, false)
                            : ":" + x.getColumnDefinition().getFieldMapping()))
                    .toString(", ");

            org.hibernate.Query query = session.createQuery("update " + type.getSimpleName() + " this_ set " + setters + " where this_.id in (:ids)");
            for (ColumnUpdate<TEntity, ?> update : updates)
                if (!update.isValueExpression())
                    query.setParameter(update.getColumnDefinition().getFieldMapping(), update.getValue());
            query.setParameterList("ids", idList).executeUpdate();
        }
        return idList;
    }

    private ColumnDefinition<TEntity, Long> getIdColumnDefinition() {
        ColumnDefinition<TEntity, Long> idColumnDefinition = null;
        try {
            //noinspection unchecked
            idColumnDefinition = (ColumnDefinition<TEntity, Long>)type.getDeclaredField("Id").get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            // do nothing
        }
        return idColumnDefinition;
    }

    private Query<Long> getIds(ColumnDefinition<TEntity, Long> idColumnDefinition) {
        return doProjection(idColumnDefinition).select(projection -> (Long) projection[0]);
    }

    private Query<Long> singleId(ColumnDefinition<TEntity, Long> idColumnDefinition) {
        return Query.from(selectSingle(idColumnDefinition));
    }

    private Query<Long> singleOrNullId(ColumnDefinition<TEntity, Long> idColumnDefinition) {
        return Query.from(selectSingleOrNull(idColumnDefinition));
    }

    private Query<Long> firstId(ColumnDefinition<TEntity, Long> idColumnDefinition) {
        return Query.from(selectFirst(idColumnDefinition));
    }

    private Query<Long> firstOrNullId(ColumnDefinition<TEntity, Long> idColumnDefinition) {
        return Query.from(selectFirstOrNull(idColumnDefinition));
    }

    private Query<Long> getIds(Function<ColumnDefinition<TEntity, Long>, Query<Long>> idGetter) {
        ColumnDefinition<TEntity, Long> idColumnDefinition = getIdColumnDefinition();
        if (idColumnDefinition == null)
            throw new RuntimeException("Cannot update or delete because " + type.getSimpleName() + " has no id field.");
        return idGetter.apply(idColumnDefinition).where(item -> item != null);
    }
}