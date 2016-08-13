package simpkins.dbquery;

import simpkins.query.QueryGroup;
import simpkins.query.QueryList;
import simpkins.query.QueryMap;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.*;

import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings({"JavaDoc", "UnusedDeclaration"})
public interface DbQuery<TEntity> extends DbSubQueryable<DbQuery<TEntity>> {

    /**
     * Static constructor to create a DbQuery for the provided type and session.
     */
    public static <TEntity> DbQuery<TEntity> from(Class<TEntity> type, Session session) {
        return new DbQueryImpl<>(type, session);
    }

    /**
     * This should not normally be used.
     */
    Criteria getCriteria();

    /**
     * This should not normally be used.
     */
    DbQuery<TEntity> addCriterion(Criterion criterion);

    /**
     * Instructs the provided relationships to be eagerly loaded with any results.
     */
    DbQuery<TEntity> loadWith(EntityRelationship<TEntity, ?>... entityRelationships);

    /**
     * Instructs the provided relationships to be eagerly loaded with any results.
     */
    DbQuery<TEntity> loadWith(Iterable<EntityRelationship<TEntity, ?>> entityRelationships);

    /**
     * Instructs the provided relationships to be eagerly loaded in separate queries.
     */
    DbQuery<TEntity> loadAfter(EntityRelationship<TEntity, ?>... entityRelationships);

    /**
     * Instructs the provided relationships to be eagerly loaded in separate queries.
     */
    DbQuery<TEntity> loadAfter(Iterable<EntityRelationship<TEntity, ?>> entityRelationships);

    /**
     * Starts a related sub-query criterion as defined by the provided relationDefinition.  All subsequent criteria
     * will apply towards the sub-query until it is finalized.
     */
    <TSubEntity> DbSubQuery<TEntity, TSubEntity, DbQuery<TEntity>> subQuery(RelationDefinition<TEntity, TSubEntity> relationDefinition);

    /**
     * Starts a related sub-query criterion as defined by the provided collectionRelationDefinition.  All subsequent
     * criteria will apply towards the sub-query until it is finalized.
     */
    <TSubEntity> DbSubQuery<TEntity, TSubEntity, DbQuery<TEntity>> subQuery(CollectionRelationDefinition<TEntity, TSubEntity> collectionRelationDefinition);

    /**
     * Starts an unrelated sub-query criterion as defined by the provided type.  All subsequent criteria will apply
     * towards the sub-query until it is finalized.
     */
    <TSubEntity> DbSubQuery<TEntity, TSubEntity, DbQuery<TEntity>> subQuery(Class<TSubEntity> subQueryType);

    /**
     * Begins the creation of a criterion for the provided entityExpression.  The subsequent call will define the rest
     * of the criterion.
     */
    <TValue> DbWhereCriteria<DbQuery<TEntity>, TEntity, TValue> where(EntityExpression<TEntity, TValue> entityExpression);

    /**
     * Adds custom sql to the query's where clause.  The entityExpression provided must resolve to a boolean.
     */
    DbQuery<TEntity> whereTrue(EntityExpression<TEntity, ?> entityExpression);

    /**
     * Adds custom sql to the query's where clause.  The entityExpression provided must resolve to a boolean.
     */
    DbQuery<TEntity> whereNotTrue(EntityExpression<TEntity, ?> entityExpression);

    /**
     * Specifies that all subsequent criteria be part of a new disjunction (A or B or C...) until the disjunction
     * is closed by calling close().
     */
    DbQuery<TEntity> openDisjunction();

    /**
     * Specifies that all subsequent criteria be part of a new conjunction (A and B and C...) until the conjunction
     * is closed by calling close().  This is the default query criteria behavior unless you are nested
     * inside a disjunction.
     */
    DbQuery<TEntity> openConjunction();

    /**
     * Specifies that the current disjunction or conjunction should be closed.
     */
    DbQuery<TEntity> close();

    /**
     * Specifies that the provided doThis Consumer (which is provided the DbQuery) will only be executed if the
     * provided isTrue parameter is true.
     */
    DbQuery<TEntity> when(boolean isTrue, Consumer<DbQuery<TEntity>> doThis);

    /**
     * Specifies an ascending ordering for the query (null values last).  If more than one order is specified then
     * orderings are applied in the same order they are specified.
     */
    DbQuery<TEntity> orderBy(EntityExpression<TEntity, ?> entityExpression);

    /**
     * Specifies an ascending ordering for the query (null values last).  If more than one order is specified then
     * orderings are applied in the same order they are specified.  Case is ignored.
     */
    DbQuery<TEntity> orderByIgnoreCase(EntityExpression<TEntity, ?> entityExpression);

    /**
     * Specifies an ascending ordering for the query (null values first).  If more than one order is specified then
     * orderings are applied in the same order they are specified.
     */
    DbQuery<TEntity> orderByNullsFirst(EntityExpression<TEntity, ?> entityExpression);

    /**
     * Specifies an ascending ordering for the query (null values first).  If more than one order is specified then
     * orderings are applied in the same order they are specified.  Case is ignored.
     */
    DbQuery<TEntity> orderByNullsFirstIgnoreCase(EntityExpression<TEntity, ?> entityExpression);

    /**
     * Specifies a descending ordering for the query (null values first).  If more than one order is specified then
     * orderings are applied in the same order they are specified.
     */
    DbQuery<TEntity> orderByDescending(EntityExpression<TEntity, ?> entityExpression);

    /**
     * Specifies a descending ordering for the query (null values first).  If more than one order is specified then
     * orderings are applied in the same order they are specified.  Case is ignored.
     */
    DbQuery<TEntity> orderByDescendingIgnoreCase(EntityExpression<TEntity, ?> entityExpression);

    /**
     * Specifies a descending ordering for the query (null values last).  If more than one order is specified then
     * orderings are applied in the same order they are specified.
     */
    DbQuery<TEntity> orderByDescendingNullsLast(EntityExpression<TEntity, ?> entityExpression);

    /**
     * Specifies a descending ordering for the query (null values last).  If more than one order is specified then
     * orderings are applied in the same order they are specified.  Case is ignored.
     */
    DbQuery<TEntity> orderByDescendingNullsLastIgnoreCase(EntityExpression<TEntity, ?> entityExpression);

    /**
     * Specifies a limit to the number of results returned.  firstResult defines how many initial results should be
     * skipped over and maxResults defines that maximum amount of results that should be returned from that point.
     */
    DbQuery<TEntity> limit(Integer firstResult, Integer maxResults);

    /**
     * Sets a custom exception to be thrown if no results are found when doing single/first style queries.
     */
    DbQuery<TEntity> setNoResultException(RuntimeException noResultException);

    /**
     * Sets a custom exception to be thrown if multiple results are found when doing single/singleOrNull style queries.
     */
    DbQuery<TEntity> setMultipleResultsException(RuntimeException multipleResultsException);

    /**
     * Configures the query to load with any related entities defined by joins in the Where, Order By, or SubQuery
     * clauses.
     */
    DbQuery<TEntity> loadWithAllJoins();

    /**
     * Provides the results to the query as a QueryList.
     */
    QueryList<TEntity> toList();

    /**
     * Provides the results to the query as a QueryList of the provided type.
     */
    <T> QueryList<T> toList(Class<T> type);

    /**
     * Performs the query with toList() but then converts the result to an array.
     */
    TEntity[] toArray();

    /**
     * Performs the query with toList() but then converts the result to an array of the provided type.
     */
    <T> T[] toArray(Class<T> type);

    /**
     * Performs a limited query that selects only the column values required and then transforms the results to a Map
     * based on the provided key and with the entity as the value.  The selected key must be unique.  If the selected
     * key will not be unique then consider using groupBy() instead.
     */
    <TKey> QueryMap<TKey, TEntity> map(FieldDefinition<TEntity, TKey> key);

    /**
     * Performs a limited query that selects only the column values required and then transforms the results to a Map
     * based on the provided key and value parameters.  The selected key must be unique.  If the selected key will not
     * be unique then consider using groupBy() instead.
     */
    <TKey, TValue> QueryMap<TKey, TValue> map(EntityExpression<TEntity, TKey> key, EntityExpression<TEntity, TValue> value);

    /**
     * Performs a limited query that selects only the column values required and then transforms the results to a Map
     * based on the provided key and with a DbResultRow object holding the requested values from the entity.  The
     * selected key must be unique.  If the selected key will not be unique then consider using groupBy() instead.
     */
    <TKey> QueryMap<TKey, DbResultRow> map(EntityExpression<TEntity, TKey> key, EntityExpression<TEntity, ?>... values);

    /**
     * Performs a limited query that selects only the column values required and then transforms the results to a Map
     * based on the provided key and with a DbResultRow object holding the requested values from the entity.  The
     * selected key must be unique.  If the selected key will not be unique then consider using groupBy() instead.
     */
    <TKey> QueryMap<TKey, DbResultRow> map(EntityExpression<TEntity, TKey> key, Iterable<EntityExpression<TEntity, ?>> values);

    /**
     * Performs a limited query that selects only the column values required and then transforms the results to a Group
     * where the key is the groupBy parameter and the value is value parameter which groups the values defined by
     * the selects parameter of the entities that share the same groupBy key.
     */
    <TKey> QueryGroup<TKey, TEntity> groupBy(FieldDefinition<TEntity, TKey> key);

    /**
     * Performs a limited query that selects only the column values required and then transforms the results to a Group
     * where the key is the groupBy parameter and the value is value parameter which groups the values defined by
     * the selects parameter of the entities that share the same groupBy key.
     */
    <TKey, TValue> QueryGroup<TKey, TValue> groupBy(EntityExpression<TEntity, TKey> key, EntityExpression<TEntity, TValue> value);

    /**
     * Performs a limited query that selects only the column values required and then transforms the results to a Group
     * where the key is the groupBy parameter and the value is a DbResultRow object which groups the values defined by
     * the values parameter of the entities that share the same groupBy key.
     */
    <TKey> QueryGroup<TKey, DbResultRow> groupBy(EntityExpression<TEntity, TKey> key, EntityExpression<TEntity, ?>... values);

    /**
     * Performs a limited query that selects only the column values required and then transforms the results to a Group
     * where the key is the groupBy parameter and the value is a DbResultRow object which groups the values defined by
     * the values parameter of the entities that share the same groupBy key.
     */
    <TKey> QueryGroup<TKey, DbResultRow> groupBy(EntityExpression<TEntity, TKey> key, Iterable<EntityExpression<TEntity, ?>> values);

    /**
     * Performs a limited query that selects and returns a List of only the column value for each entity which is
     * specified by the select parameter.
     */
    <TValue> QueryList<TValue> select(EntityExpression<TEntity, TValue> select);

    /**
     * Performs a limited query that selects and returns a List of only the column value for each entity which is
     * specified by the select parameter.
     */
    <TValue> QueryList<TValue> select(RelationBaseDefinition<TEntity, TValue> select);

    /**
     * Performs a limited query that selects and returns a List of only the column value for each entity which is
     * specified by the select parameter.
     */
    <TValue> QueryList<TValue> select(CollectionRelationBaseDefinition<TEntity, TValue> select);

    /**
     * Performs a limited query that selects and returns a DbResultRow object which contains only the column values for
     * each entity which are specified by the selects parameter.
     */
    QueryList<DbResultRow> select(EntityExpression<TEntity, ?>... selects);

    /**
     * Performs a limited query that selects and returns a DbResultRow object which contains only the column values for
     * each entity which are specified by the selects parameter.
     */
    QueryList<DbResultRow> select(Iterable<EntityExpression<TEntity, ?>> selects);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for a single result 
     * that matches the provided query.  Throws an exception if no results are found or if more than one result is 
     * found.
     */
    <TValue> TValue selectSingle(EntityExpression<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for a single result
     * that matches the provided query.  Throws an exception if no results are found or if more than one result is
     * found.
     */
    <TValue> TValue selectSingle(RelationBaseDefinition<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for a single result
     * that matches the provided query.  Throws an exception if no results are found or if more than one result is
     * found.
     */
    <TValue> QueryList<TValue> selectSingle(CollectionRelationBaseDefinition<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for a single result 
     * that matches the provided query or null if no results are found.  Throws an exception if more than one result is 
     * found.
     */
    <TValue> TValue selectSingleOrNull(EntityExpression<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for a single result
     * that matches the provided query or null if no results are found.  Throws an exception if more than one result is
     * found.
     */
    <TValue> TValue selectSingleOrNull(RelationBaseDefinition<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for a single result
     * that matches the provided query or null if no results are found.  Throws an exception if more than one result is
     * found.
     */
    <TValue> QueryList<TValue> selectSingleOrNull(CollectionRelationBaseDefinition<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for a single result 
     * that matches the provided query or null if no results are found, wrapped in an Optional.  Throws an exception 
     * if more than one result is found.
     */
    <TValue> Optional<TValue> selectSingleOptional(EntityExpression<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for a single result
     * that matches the provided query or null if no results are found, wrapped in an Optional.  Throws an exception
     * if more than one result is found.
     */
    <TValue> Optional<TValue> selectSingleOptional(RelationBaseDefinition<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for a single result
     * that matches the provided query or null if no results are found, wrapped in an Optional.  Throws an exception
     * if more than one result is found.
     */
    <TValue> Optional<QueryList<TValue>> selectSingleOptional(CollectionRelationBaseDefinition<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for the first result 
     * that matches the provided query.  Throws an exception if no results are found.
     */
    <TValue> TValue selectFirst(EntityExpression<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for the first result
     * that matches the provided query.  Throws an exception if no results are found.
     */
    <TValue> TValue selectFirst(RelationBaseDefinition<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for the first result
     * that matches the provided query.  Throws an exception if no results are found.
     */
    <TValue> QueryList<TValue> selectFirst(CollectionRelationBaseDefinition<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for the first result 
     * that matches the provided query or null if no results are found.
     */
    <TValue> TValue selectFirstOrNull(EntityExpression<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for the first result
     * that matches the provided query or null if no results are found.
     */
    <TValue> TValue selectFirstOrNull(RelationBaseDefinition<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for the first result
     * that matches the provided query or null if no results are found.
     */
    <TValue> QueryList<TValue> selectFirstOrNull(CollectionRelationBaseDefinition<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for the first result 
     * that matches the provided query or null if no results are found, wrapped in an Optional.
     */
    <TValue> Optional<TValue> selectFirstOptional(EntityExpression<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for the first result
     * that matches the provided query or null if no results are found, wrapped in an Optional.
     */
    <TValue> Optional<TValue> selectFirstOptional(RelationBaseDefinition<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for the first result
     * that matches the provided query or null if no results are found, wrapped in an Optional.
     */
    <TValue> Optional<QueryList<TValue>> selectFirstOptional(CollectionRelationBaseDefinition<TEntity, TValue> select);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for a single result that matches the provided query.  Throws an exception if
     * no results are found or if more than one result is found.
     */
    DbResultRow selectSingle(EntityExpression<TEntity, ?>... selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for a single result that matches the provided query.  Throws an exception if
     * no results are found or if more than one result is found.
     */
    DbResultRow selectSingle(Iterable<EntityExpression<TEntity, ?>> selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for a single result that matches the provided query or null if no results are
     * found.  Throws an exception if more than one result is found.
     */
    DbResultRow selectSingleOrNull(EntityExpression<TEntity, ?>... selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for a single result that matches the provided query or null if no results are
     * found.  Throws an exception if more than one result is found.
     */
    DbResultRow selectSingleOrNull(Iterable<EntityExpression<TEntity, ?>> selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for a single result that matches the provided query or null if no results are
     * found, wrapped in an Optional.  Throws an exception if more than one result is found.
     */
    Optional<DbResultRow> selectSingleOptional(EntityExpression<TEntity, ?>... selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for a single result that matches the provided query or null if no results are
     * found, wrapped in an Optional.  Throws an exception if more than one result is found.
     */
    Optional<DbResultRow> selectSingleOptional(Iterable<EntityExpression<TEntity, ?>> selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for the first result that matches the provided query.  Throws an exception if
     * no results are found.
     */
    DbResultRow selectFirst(EntityExpression<TEntity, ?>... selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for the first result that matches the provided query.  Throws an exception if
     * no results are found.
     */
    DbResultRow selectFirst(Iterable<EntityExpression<TEntity, ?>> selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for the first result that matches the provided query or null if no results
     * are found.
     */
    DbResultRow selectFirstOrNull(EntityExpression<TEntity, ?>... selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for the first result that matches the provided query or null if no results
     * are found.
     */
    DbResultRow selectFirstOrNull(Iterable<EntityExpression<TEntity, ?>> selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for the first result that matches the provided query or null if no results
     * are found, wrapped in an Optional.
     */
    Optional<DbResultRow> selectFirstOptional(EntityExpression<TEntity, ?>... selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for the first result that matches the provided query or null if no results
     * are found, wrapped in an Optional.
     */
    Optional<DbResultRow> selectFirstOptional(Iterable<EntityExpression<TEntity, ?>> selects);

    /**
     * Performs a limited query that selects and returns a List of only the distinct column value for each entity which
     * is specified by the select parameter.
     */
    <TValue> QueryList<TValue> selectDistinct(EntityExpression<TEntity, TValue> select);

    /**
     * Performs a limited query that selects and returns a List of only the distinct column values for each entity which
     * is specified by the select parameters.
     */
    QueryList<DbResultRow> selectDistinct(EntityExpression<TEntity, ?>... selects);

    /**
     * Performs a limited query that selects and returns a List of only the distinct column values for each entity which
     * is specified by the select parameters.
     */
    QueryList<DbResultRow> selectDistinct(Iterable<EntityExpression<TEntity, ?>> selects);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for a single distinct
     * result that matches the provided query.  Throws an exception if no results are found or if more than one result
     * is found.
     */
    <TValue> TValue selectDistinctSingle(EntityExpression<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for a single distinct
     * result that matches the provided query or null if no results are found.  Throws an exception if more than one
     * result is found.
     */
    <TValue> TValue selectDistinctSingleOrNull(EntityExpression<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for a single distinct
     * result that matches the provided query or null if no results are found, wrapped in an Optional.  Throws an
     * exception if more than one result is found
     */
    <TValue> Optional<TValue> selectDistinctSingleOptional(EntityExpression<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for the first distinct
     * result that matches the provided query.  Throws an exception if no results are found.
     */
    <TValue> TValue selectDistinctFirst(EntityExpression<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for the first distinct
     * result that matches the provided query or null if no results are found.
     */
    <TValue> TValue selectDistinctFirstOrNull(EntityExpression<TEntity, TValue> select);

    /**
     * Performs a limited query that returns the column value specified by the select parameter for the first distinct
     * result that matches the provided query or null if no results are found, wrapped in an Optional.
     */
    <TValue> Optional<TValue> selectDistinctFirstOptional(EntityExpression<TEntity, TValue> select);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for a single distinct result that matches the provided query.  Throws an
     * exception if no results are found or if more than one result is found.
     */
    DbResultRow selectDistinctSingle(EntityExpression<TEntity, ?>... selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for a single distinct result that matches the provided query.  Throws an
     * exception if no results are found or if more than one result is found.
     */
    DbResultRow selectDistinctSingle(Iterable<EntityExpression<TEntity, ?>> selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for a single distinct result that matches the provided query or null if no
     * results are found.  Throws an exception if more than one result is found.
     */
    DbResultRow selectDistinctSingleOrNull(EntityExpression<TEntity, ?>... selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for a single distinct result that matches the provided query or null if no
     * results are found.  Throws an exception if more than one result is found.
     */
    DbResultRow selectDistinctSingleOrNull(Iterable<EntityExpression<TEntity, ?>> selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for a single distinct result that matches the provided query or null if no
     * results are found, wrapped in an Optional.  Throws an exception if more than one result is found.
     */
    Optional<DbResultRow> selectDistinctSingleOptional(EntityExpression<TEntity, ?>... selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for a single distinct result that matches the provided query or null if no
     * results are found, wrapped in an Optional.  Throws an exception if more than one result is found.
     */
    Optional<DbResultRow> selectDistinctSingleOptional(Iterable<EntityExpression<TEntity, ?>> selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for the first distinct result that matches the provided query.  Throws an
     * exception if no results are found.
     */
    DbResultRow selectDistinctFirst(EntityExpression<TEntity, ?>... selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for the first distinct result that matches the provided query.  Throws an
     * exception if no results are found.
     */
    DbResultRow selectDistinctFirst(Iterable<EntityExpression<TEntity, ?>> selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for the first distinct result that matches the provided query or null if no
     * results are found.
     */
    DbResultRow selectDistinctFirstOrNull(EntityExpression<TEntity, ?>... selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for the first distinct result that matches the provided query or null if no
     * results are found.
     */
    DbResultRow selectDistinctFirstOrNull(Iterable<EntityExpression<TEntity, ?>> selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for the first distinct result that matches the provided query or null if no
     * results are found, wrapped in an Optional.
     */
    Optional<DbResultRow> selectDistinctFirstOptional(EntityExpression<TEntity, ?>... selects);

    /**
     * Performs a limited query that returns a DbResultRow object which contains only the column values which are
     * specified by the selects parameter for the first distinct result that matches the provided query or null if no
     * results are found, wrapped in an Optional.
     */
    Optional<DbResultRow> selectDistinctFirstOptional(Iterable<EntityExpression<TEntity, ?>> selects);

    /**
     * Returns the single result that matches the provided query.  Throws an exception if no results are found
     * or if more than one result is found.
     */
    TEntity single();

    /**
     * Returns the single result that matches the provided query or null if no results are found.  Throws an
     * exception if more than one result is found.
     */
    TEntity singleOrNull();

    /**
     * Returns the single result that matches the provided query wrapped in an Optional object which is empty if no
     * results are found.  Throws an exception if more than one result is found.
     */
    Optional<TEntity> singleOptional();

    /**
     * Returns the first result that matches the provided query.  Throws an exception if no results are found.
     */
    TEntity first();

    /**
     * Returns the first result that matches the provided query or null if no results are found.
     */
    TEntity firstOrNull();

    /**
     * Returns the first result that matches the provided query wrapped in an Optional object which is empty if no
     * results are found.
     */
    Optional<TEntity> firstOptional();

    /**
     * Returns true if the query has at least one results, otherwise false.
     */
    boolean exists();

    /**
     * Returns false if the query has no results, otherwise true.
     */
    boolean notExists();

    /**
     * Returns the number of results for this query.
     */
    long count();

    /**
     * Returns the number of results for this query where the provided entityExpression value is not null.
     */
    long count(EntityExpression<TEntity, ?> entityExpression);

    /**
     * Returns the number of distinct values of the provided entityExpression for this query where the provided
     * entityExpression value is not null.
     */
    long countDistinct(EntityExpression<TEntity, ?> entityExpression);

    /**
     * Returns the sum of the values for the provided entityExpression for the results of this query.
     */
    <TValue> TValue sum(EntityExpression<TEntity, TValue> entityExpression);

    /**
     * Returns the minimum value of the provided entityExpression for the results of this query.
     */
    <TValue> TValue min(EntityExpression<TEntity, TValue> entityExpression);

    /**
     * Returns the maximum value of the provided entityExpression for the results of this query.
     */
    <TValue> TValue max(EntityExpression<TEntity, TValue> entityExpression);

    /**
     * Returns a boolean if any value of the provided entityExpression for the results of this query is true.
     */
    Boolean any(EntityExpression<TEntity, Boolean> entityExpression);

    /**
     * Returns a boolean if all values of the provided entityExpression for the results of this query are true.
     */
    Boolean all(EntityExpression<TEntity, Boolean> entityExpression);

    /**
     * Returns a boolean if no values of the provided entityExpression for the results of this query are true.
     */
    Boolean none(EntityExpression<TEntity, Boolean> entityExpression);

    /**
     * Returns the average of the values for the provided entityExpression for the results of this query.
     */
    Double average(EntityExpression<TEntity, ?> entityExpression);

    /**
     * Returns a series of aggregates including COUNT, SUM, AVG, MIN, and MAX, or GROUP_BY.
     */
    QueryList<DbResultRow> aggregate(ColumnAggregate<TEntity, ?>... aggregates);

    /**
     * Returns a series of aggregates including COUNT, SUM, AVG, MIN, and MAX, or GROUP_BY.
     */
    QueryList<DbResultRow> aggregate(Iterable<ColumnAggregate<TEntity, ?>> aggregates);

    /**
     * Deletes all rows matched by the query and returns the ids of the deleted rows.
     */
    QueryList<Long> delete();

    /**
     * Deletes the row for the given id.
     */
    void deleteById(long id);

    /**
     * Deletes the rows for the given ids.
     */
    void deleteById(Long... ids);

    /**
     * Deletes the rows for the given ids.
     */
    void deleteById(Iterable<Long> ids);

    /**
     * Deletes a single row matched by the query and returns the id of the deleted row.
     * Throws an exception if no results are found or if more than one result is found.
     */
    long deleteSingle();

    /**
     * Deletes a single row matched by the query and returns the id of the deleted row
     * or null if none is found. Throws an exception if no results are found or if more
     * than one result is found.
     */
    Long deleteSingleOrNull();

    /**
     * Deletes the first row matched by the query and returns the id of the deleted row.
     * Throws an exception if no results are found.
     */
    long deleteFirst();

    /**
     * Deletes the first row matched by the query and returns the id of the deleted row
     * or null if none is found.
     */
    Long deleteFirstOrNull();

    /**
     * Updates all rows matched by the query with the specified updates and returns the
     * ids of the updated rows.
     */
    QueryList<Long> update(ColumnUpdate<TEntity, ?>... updates);

    /**
     * Updates all rows matched by the query with the specified updates and returns the
     * ids of the updated rows.
     */
    QueryList<Long> update(Iterable<ColumnUpdate<TEntity, ?>> updates);

    /**
     * Updates the row for the given id with the specified updates.
     */
    void updateById(long id, ColumnUpdate<TEntity, ?>... updates);

    /**
     * Updates the row for the given id with the specified updates.
     */
    void updateById(long id, Iterable<ColumnUpdate<TEntity, ?>> updates);

    /**
     * Updates the rows for the given ids with the specified updates.
     */
    void updateById(Iterable<Long> ids, ColumnUpdate<TEntity, ?>... updates);

    /**
     * Updates the rows for the given ids with the specified updates.
     */
    void updateById(Iterable<Long> ids, Iterable<ColumnUpdate<TEntity, ?>> updates);

    /**
     * Updates a single row matched by the query with the specified updates and returns
     * the id of the updated row.  Throws an exception if no results are found or if
     * more than one result is found.
     */
    long updateSingle(ColumnUpdate<TEntity, ?>... updates);

    /**
     * Updates a single row matched by the query with the specified updates and returns
     * the id of the updated row.  Throws an exception if no results are found or if
     * more than one result is found.
     */
    long updateSingle(Iterable<ColumnUpdate<TEntity, ?>> updates);

    /**
     * Updates a single row matched by the query with the specified updates and returns
     * the id of the updated row or null if none is found. Throws an exception if no
     * results are found or if more than one result is found.
     */
    Long updateSingleOrNull(ColumnUpdate<TEntity, ?>... updates);

    /**
     * Updates a single row matched by the query with the specified updates and returns
     * the id of the updated row or null if none is found. Throws an exception if no
     * results are found or if more than one result is found.
     */
    Long updateSingleOrNull(Iterable<ColumnUpdate<TEntity, ?>> updates);

    /**
     * Updates the first row matched by the query with the specified updates and returns
     * the id of the updated row.  Throws an exception if no results are found.
     */
    long updateFirst(ColumnUpdate<TEntity, ?>... updates);

    /**
     * Updates the first row matched by the query with the specified updates and returns
     * the id of the updated row.  Throws an exception if no results are found.
     */
    long updateFirst(Iterable<ColumnUpdate<TEntity, ?>> updates);

    /**
     * Updates the first row matched by the query with the specified updates and returns
     * the id of the updated row or null if none is found.
     */
    Long updateFirstOrNull(ColumnUpdate<TEntity, ?>... updates);

    /**
     * Updates the first row matched by the query with the specified updates and returns
     * the id of the updated row or null if none is found.
     */
    Long updateFirstOrNull(Iterable<ColumnUpdate<TEntity, ?>> updates);
}