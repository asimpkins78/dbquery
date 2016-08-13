package simpkins.dbquery;

@SuppressWarnings("UnusedDeclaration")
public interface DbSubQuerySingleResolution<TQuery, TEntity, TValue> {
    TQuery isEqualTo(TValue value);
    TQuery isEqualTo(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery isGreaterThan(TValue value);
    TQuery isGreaterThan(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery isGreaterThanOrEqualTo(TValue value);
    TQuery isGreaterThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery isLessThan(TValue value);
    TQuery isLessThan(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery isLessThanOrEqualTo(TValue value);
    TQuery isLessThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions);

    TQuery isNotEqualTo(TValue value);
    TQuery isNotEqualTo(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery isNotGreaterThan(TValue value);
    TQuery isNotGreaterThan(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery isNotGreaterThanOrEqualTo(TValue value);
    TQuery isNotGreaterThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery isNotLessThan(TValue value);
    TQuery isNotLessThan(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery isNotLessThanOrEqualTo(TValue value);
    TQuery isNotLessThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions);
}
