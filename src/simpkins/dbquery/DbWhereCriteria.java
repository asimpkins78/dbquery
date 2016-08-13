package simpkins.dbquery;

@SuppressWarnings({"UnusedDeclaration", "unchecked"})
public interface DbWhereCriteria<TQuery, TEntity, TValue> {
    DbWhereCriteria<TQuery, TEntity, TValue> sqlNulls(boolean isNullsUnequal);
    DbWhereCriteria<TQuery, TEntity, TValue> ignoreCase(boolean isIgnoreCase);

    TQuery isNull();
    TQuery isEqualTo(TValue value);
    TQuery isEqualTo(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isEqualToIgnoreCase(TValue value);
    TQuery isEqualToIgnoreCase(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isLike(TValue value);
    TQuery isLike(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isLikeIgnoreCase(TValue value);
    TQuery isLikeIgnoreCase(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isRegexMatch(TValue value);
    TQuery isRegexMatch(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isRegexMatchIgnoreCase(TValue value);
    TQuery isRegexMatchIgnoreCase(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isGreaterThan(TValue value);
    TQuery isGreaterThan(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isGreaterThanOrEqualTo(TValue value);
    TQuery isGreaterThanOrEqualTo(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isLessThan(TValue value);
    TQuery isLessThan(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isLessThanOrEqualTo(TValue value);
    TQuery isLessThanOrEqualTo(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isBetween(TValue lowValue, TValue highValue);
    TQuery isBetween(TValue lowValue, EntityExpression<TEntity, TValue> highEntityExpression);
    TQuery isBetween(EntityExpression<TEntity, TValue> lowEntityExpression, TValue highValue);
    TQuery isBetween(EntityExpression<TEntity, TValue> lowEntityExpression, EntityExpression<TEntity, TValue> highEntityExpression);
    TQuery isIn(TValue... values);
    TQuery isIn(Iterable<TValue> values);
    TQuery isIn(EntityExpression<TEntity, ?>... otherEntityExpressions);
    TQuery isInIgnoreCase(TValue... values);
    TQuery isInIgnoreCase(Iterable<TValue> values);
    TQuery isInIgnoreCase(EntityExpression<TEntity, ?>... otherEntityExpressions);

    TQuery isNotNull();
    TQuery isNotEqualTo(TValue value);
    TQuery isNotEqualTo(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isNotEqualToIgnoreCase(TValue value);
    TQuery isNotEqualToIgnoreCase(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isNotLike(TValue value);
    TQuery isNotLike(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isNotLikeIgnoreCase(TValue value);
    TQuery isNotLikeIgnoreCase(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isNotRegexMatch(TValue value);
    TQuery isNotRegexMatch(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isNotRegexMatchIgnoreCase(TValue value);
    TQuery isNotRegexMatchIgnoreCase(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isNotGreaterThan(TValue value);
    TQuery isNotGreaterThan(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isNotGreaterThanOrEqualTo(TValue value);
    TQuery isNotGreaterThanOrEqualTo(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isNotLessThan(TValue value);
    TQuery isNotLessThan(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isNotLessThanOrEqualTo(TValue value);
    TQuery isNotLessThanOrEqualTo(EntityExpression<TEntity, TValue> otherEntityExpression);
    TQuery isNotBetween(TValue lowValue, TValue highValue);
    TQuery isNotBetween(TValue lowValue, EntityExpression<TEntity, TValue> highEntityExpression);
    TQuery isNotBetween(EntityExpression<TEntity, TValue> lowEntityExpression, TValue highValue);
    TQuery isNotBetween(EntityExpression<TEntity, TValue> lowEntityExpression, EntityExpression<TEntity, TValue> highEntityExpression);
    TQuery isNotIn(TValue... values);
    TQuery isNotIn(Iterable<TValue> values);
    TQuery isNotIn(EntityExpression<TEntity, ?>... otherEntityExpressions);
    TQuery isNotInIgnoreCase(TValue... values);
    TQuery isNotInIgnoreCase(Iterable<TValue> values);
    TQuery isNotInIgnoreCase(EntityExpression<TEntity, ?>... otherEntityExpressions);
}
