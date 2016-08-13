package simpkins.dbquery;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;

import java.util.function.Consumer;

@SuppressWarnings({"JavaDoc", "UnusedDeclaration"})
public interface DbSubQuery<TOuterEntity, TInnerEntity, TSource extends DbSubQueryable> extends DbSubQueryable<DbSubQuery<TOuterEntity, TInnerEntity, TSource>> {

    /**
     * This should not normally be used.
     */
    DetachedCriteria getCriteria();

    /**
     * This should not normally be used.
     */
    DbSubQuery<TOuterEntity, TInnerEntity, TSource> addCriterion(Criterion criterion);

    /**
     * Starts a sub-query criterion on the related entity as defined by the provided relationDefinition.  All
     * subsequent criteria will apply towards the sub-query until it is finalized by calling exists() or notExists().
     */
    <TRelation> DbSubQuery<TInnerEntity, TRelation, DbSubQuery<TOuterEntity, TInnerEntity, TSource>> subQuery(RelationDefinition<TInnerEntity, TRelation> relationDefinition);

    /**
     * Starts a sub-query criterion on the related entities as defined by the provided collectionDefinition.  All
     * subsequent criteria will apply towards the sub-query until it is finalized by calling exists() or notExists().
     */
    <TRelation> DbSubQuery<TInnerEntity, TRelation, DbSubQuery<TOuterEntity, TInnerEntity, TSource>> subQuery(CollectionRelationDefinition<TInnerEntity, TRelation> collectionRelationDefinition);

    /**
     * Starts an unrelated sub-query criterion as defined by the provided type.  All subsequent criteria will apply
     * towards the sub-query until it is finalized.
     */
    <TRelation> DbSubQuery<TInnerEntity, TRelation, DbSubQuery<TOuterEntity, TInnerEntity, TSource>> subQuery(Class<TRelation> subQueryType);

    /**
     * Begins the creation of a criterion for the provided entityExpression.  The subsequent call will define the rest
     * of the criterion.
     */
    <TValue> DbWhereCriteria<DbSubQuery<TOuterEntity, TInnerEntity, TSource>, TInnerEntity, TValue> where(EntityExpression<TInnerEntity, TValue> entityExpression);

    /**
     * Begins the creation of a criterion for the provided outer column definition.  The subsequent call will define
     * the rest of the criterion.
     */
    <TValue> DbWhereCriteria<DbSubQuery<TOuterEntity, TInnerEntity, TSource>, TInnerEntity, TValue> whereOuter(ColumnDefinition<TOuterEntity, TValue> columnDefinition);

    /**
     * Adds custom sql to the query's where clause.  The entityExpression provided must resolve to a boolean.
     */
    DbSubQuery<TOuterEntity, TInnerEntity, TSource> whereTrue(EntityExpression<TInnerEntity, ?> entityExpression);

    /**
     * Adds custom sql to the query's where clause.  The entityExpression provided must resolve to a boolean.
     */
    DbSubQuery<TOuterEntity, TInnerEntity, TSource> whereNotTrue(EntityExpression<TInnerEntity, ?> entityExpression);

    /**
     * Specifies that all subsequent criteria be part of a new disjunction (A or B or C...) until the disjunction
     * is closed by calling close().
     */
    DbSubQuery<TOuterEntity, TInnerEntity, TSource> openDisjunction();

    /**
     * Specifies that all subsequent criteria be part of a new conjunction (A and B and C...) until the conjunction
     * is closed by calling close().  This is the default query criteria behavior unless you are nested
     * inside a disjunction.
     */
    DbSubQuery<TOuterEntity, TInnerEntity, TSource> openConjunction();

    /**
     * Specifies that the current disjunction, conjunction, or conditional should be closed.
     */
    DbSubQuery<TOuterEntity, TInnerEntity, TSource> close();

    /**
     * Specifies that the provided doThis Consumer (which is provided the DbSubQuery) will only be executed if the
     * provided isTrue parameter is true.
     */
    DbSubQuery<TOuterEntity, TInnerEntity, TSource> when(boolean isTrue, Consumer<DbSubQuery<TOuterEntity, TInnerEntity, TSource>> doThis);

    /**
     * Specifies an ascending ordering for the sub-query (null values last).  If more than one order is specified then
     * orderings are applied in the same order they are specified.
     */
    DbSubQuery<TOuterEntity, TInnerEntity, TSource> orderBy(EntityExpression<TInnerEntity, ?> entityExpression);

    /**
     * Specifies an ascending ordering for the sub-query (null values last).  If more than one order is specified then
     * orderings are applied in the same order they are specified.  Case is ignored.
     */
    DbSubQuery<TOuterEntity, TInnerEntity, TSource> orderByIgnoreCase(EntityExpression<TInnerEntity, ?> entityExpression);

    /**
     * Specifies an ascending ordering for the sub-query (null values first).  If more than one order is specified then
     * orderings are applied in the same order they are specified.
     */
    DbSubQuery<TOuterEntity, TInnerEntity, TSource> orderByNullsFirst(EntityExpression<TInnerEntity, ?> entityExpression);

    /**
     * Specifies an ascending ordering for the sub-query (null values first).  If more than one order is specified then
     * orderings are applied in the same order they are specified.  Case is ignored.
     */
    DbSubQuery<TOuterEntity, TInnerEntity, TSource> orderByNullsFirstIgnoreCase(EntityExpression<TInnerEntity, ?> entityExpression);

    /**
     * Specifies a descending ordering for the sub-query (null values first).  If more than one order is specified then
     * orderings are applied in the same order they are specified.
     */
    DbSubQuery<TOuterEntity, TInnerEntity, TSource> orderByDescending(EntityExpression<TInnerEntity, ?> entityExpression);

    /**
     * Specifies a descending ordering for the sub-query (null values first).  If more than one order is specified then
     * orderings are applied in the same order they are specified.  Case is ignored.
     */
    DbSubQuery<TOuterEntity, TInnerEntity, TSource> orderByDescendingIgnoreCase(EntityExpression<TInnerEntity, ?> entityExpression);

    /**
     * Specifies a descending ordering for the sub-query (null values last).  If more than one order is specified then
     * orderings are applied in the same order they are specified.
     */
    DbSubQuery<TOuterEntity, TInnerEntity, TSource> orderByDescendingNullsLast(EntityExpression<TInnerEntity, ?> entityExpression);

    /**
     * Specifies a descending ordering for the sub-query (null values last).  If more than one order is specified then
     * orderings are applied in the same order they are specified.  Case is ignored.
     */
    DbSubQuery<TOuterEntity, TInnerEntity, TSource> orderByDescendingNullsLastIgnoreCase(EntityExpression<TInnerEntity, ?> entityExpression);

    /**
     * Specifies a limit to the number of results returned.  firstResult defines how many initial results should be
     * skipped over and maxResults defines that maximum amount of results that should be returned from that point.
     */
    DbSubQuery<TOuterEntity, TInnerEntity, TSource> limit(Integer firstResult, Integer maxResults);

    /**
     * Closes the sub-query criterion.  The criterion will match if any of the related entities exist that match the
     * criteria provided to the sub-query.
     */
    TSource exists();

    /**
     * Closes the sub-query criterion.  The criterion will match if none of the related entities exist that match the
     * criteria provided to the sub-query.
     */
    TSource notExists();

    /**
     * Begins the close of the sub-query criterion.  A subsequent resolution will be chosen which will identify the
     * outer entity column to compare to and the type of comparison to be made.
     */
    <TValue> DbSubQueryMultipleResolution<TSource, TOuterEntity, TValue> select(EntityExpression<TInnerEntity, TValue> select);

    DbSubQueryMultipleResolution<TSource, TOuterEntity, ?> select(EntityExpression<TInnerEntity, ?>... selects);

    /**
     * Begins the close of the sub-query criterion.  A subsequent resolution will be chosen which will identify the
     * out entity column to compare to and the type of comparison to be made.  The query will fail if the sub-query
     * return more than one row.
     */
    <TValue> DbSubQuerySingleResolution<TSource, TOuterEntity, TValue> selectSingleOrNull(EntityExpression<TInnerEntity, TValue> select);

    DbSubQuerySingleResolution<TSource, TOuterEntity, ?> selectSingleOrNull(EntityExpression<TInnerEntity, ?>... selects);

    /**
     * Begins the close of the sub-query criterion.  A subsequent resolution will be chosen which will identify the
     * outer entity column to compare to and the type of comparison to be made.  The sub-query result will be limited
     * to the first row.
     */
    <TValue> DbSubQuerySingleResolution<TSource, TOuterEntity, TValue> selectFirstOrNull(EntityExpression<TInnerEntity, TValue> select);

    DbSubQuerySingleResolution<TSource, TOuterEntity, ?> selectFirstOrNull(EntityExpression<TInnerEntity, ?>... selects);
}
