package simpkins.dbquery;

import simpkins.query.Query;
import simpkins.query.QueryString;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.*;
import org.hibernate.dialect.pagination.LegacyLimitHandler;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.type.IntegerType;

import java.util.function.Consumer;

public class DbSubQueryResolution<TQuery, TEntity, TValue> implements DbSubQueryMultipleResolution<TQuery, TEntity, TValue>, DbSubQuerySingleResolution<TQuery, TEntity, TValue> {
    private TQuery query;
    private DetachedCriteria detachedCriteria;
    private Consumer<Criterion> addCriterion;
    private Consumer<EntityExpression<TEntity, ?>> prepareJoinedDefinitions;
    private Integer firstResult;
    private Integer maxResults;

    public DbSubQueryResolution(TQuery query, DetachedCriteria detachedCriteria, Consumer<Criterion> addCriterion, Consumer<EntityExpression<TEntity, ?>> prepareJoinedDefinitions, Integer firstResult, Integer maxResults) {
        this.query = query;
        this.detachedCriteria = detachedCriteria;
        this.addCriterion = addCriterion;
        this.prepareJoinedDefinitions = prepareJoinedDefinitions;
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    @Override
    public TQuery allEqualTo(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, "=", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery allEqualTo(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, "=", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery allGreaterThan(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, "<", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery allGreaterThan(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, "<", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery allGreaterThanOrEqualTo(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, "<=", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery allGreaterThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, "<=", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery allLessThan(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, ">", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery allLessThan(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, ">", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery allLessThanOrEqualTo(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, ">=", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery allLessThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, ">=", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyEqualTo(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, "=", "some", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyEqualTo(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, "=", "some", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyGreaterThan(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, "<", "some", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyGreaterThan(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, "<", "some", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyGreaterThanOrEqualTo(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, "<=", "some", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyGreaterThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, "<=", "some", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyLessThan(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, ">", "some", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyLessThan(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, ">", "some", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyLessThanOrEqualTo(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, ">=", "some", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyLessThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, ">=", "some", detachedCriteria));
        return query;
    }

    @Override
    public TQuery isEqualTo(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, "=", null, detachedCriteria));
        return query;
    }

    @Override
    public TQuery isEqualTo(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, "=", null, detachedCriteria));
        return query;
    }

    @Override
    public TQuery isGreaterThan(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, "<", null, detachedCriteria));
        return query;
    }

    @Override
    public TQuery isGreaterThan(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, "<", null, detachedCriteria));
        return query;
    }

    @Override
    public TQuery isGreaterThanOrEqualTo(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, "<=", null, detachedCriteria));
        return query;
    }

    @Override
    public TQuery isGreaterThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, "<=", null, detachedCriteria));
        return query;
    }

    @Override
    public TQuery isLessThan(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, ">", null, detachedCriteria));
        return query;
    }

    @Override
    public TQuery isLessThan(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, ">", null, detachedCriteria));
        return query;
    }

    @Override
    public TQuery isLessThanOrEqualTo(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, ">=", null, detachedCriteria));
        return query;
    }

    @Override
    public TQuery isLessThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, ">=", null, detachedCriteria));
        return query;
    }

    @Override
    public TQuery allNotEqualTo(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, "!=", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery allNotEqualTo(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, "!=", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery allNotGreaterThan(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, ">=", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery allNotGreaterThan(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, ">=", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery allNotGreaterThanOrEqualTo(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, ">", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery allNotGreaterThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, ">", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery allNotLessThan(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, "<=", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery allNotLessThan(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, "<=", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery allNotLessThanOrEqualTo(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, "<", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery allNotLessThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, "<", "all", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyNotEqualTo(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, "!=", "any", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyNotEqualTo(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, "!=", "any", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyNotGreaterThan(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, ">=", "any", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyNotGreaterThan(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, ">=", "any", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyNotGreaterThanOrEqualTo(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, ">", "any", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyNotGreaterThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, ">", "any", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyNotLessThan(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, "<=", "any", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyNotLessThan(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, "<=", "any", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyNotLessThanOrEqualTo(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, "<", "any", detachedCriteria));
        return query;
    }

    @Override
    public TQuery anyNotLessThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, "<", "any", detachedCriteria));
        return query;
    }

    @Override
    public TQuery isNotEqualTo(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, "!=", null, detachedCriteria));
        return query;
    }

    @Override
    public TQuery isNotEqualTo(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, "!=", null, detachedCriteria));
        return query;
    }

    @Override
    public TQuery isNotGreaterThan(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, ">=", null, detachedCriteria));
        return query;
    }

    @Override
    public TQuery isNotGreaterThan(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, ">=", null, detachedCriteria));
        return query;
    }

    @Override
    public TQuery isNotGreaterThanOrEqualTo(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, ">", null, detachedCriteria));
        return query;
    }

    @Override
    public TQuery isNotGreaterThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, ">", null, detachedCriteria));
        return query;
    }

    @Override
    public TQuery isNotLessThan(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, "<=", null, detachedCriteria));
        return query;
    }

    @Override
    public TQuery isNotLessThan(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, "<=", null, detachedCriteria));
        return query;
    }

    @Override
    public TQuery isNotLessThanOrEqualTo(TValue value) {
        addCriterion.accept(new CustomSimpleSubqueryExpression(value, "<", null, detachedCriteria));
        return query;
    }

    @Override
    public TQuery isNotLessThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions) {
        for (EntityExpression<TEntity, ?> entityExpression : entityExpressions)
            prepareJoinedDefinitions.accept(entityExpression);
        addCriterion.accept(new CustomSubqueryExpression(entityExpressions, "<", null, detachedCriteria));
        return query;
    }

    private class CustomSimpleSubqueryExpression extends SimpleSubqueryExpression {
        public CustomSimpleSubqueryExpression(Object value, String op, String quantifier, DetachedCriteria dc) {
            super(value, op, quantifier, dc);
        }

        @Override
        public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
            TypedValue[] typedValues = super.getTypedValues(criteria, criteriaQuery);
            if (firstResult == null && maxResults == null)
                return typedValues;
            return Query.from(typedValues)
                    .when(firstResult != null, x -> x.combine(new TypedValue(IntegerType.INSTANCE, firstResult)))
                    .when(maxResults != null, x -> x.combine(new TypedValue(IntegerType.INSTANCE, maxResults)))
                    .toArray(TypedValue.class);
        }

        @Override
        public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
            String sql = super.toSqlString(criteria, criteriaQuery);
            if (firstResult == null && maxResults == null)
                return sql;
            RowSelection rowSelection = new RowSelection();
            rowSelection.setFirstRow(firstResult);
            rowSelection.setMaxRows(maxResults);
            return new LegacyLimitHandler(criteriaQuery.getFactory().getDialect())
                    .processSql(QueryString.of(sql).substringToLast(")").toString(), rowSelection) + ")";
        }
    }

    private class CustomSubqueryExpression extends SubqueryExpression {
        private EntityExpression<?, ?>[] entityExpressions;

        protected CustomSubqueryExpression(EntityExpression<?, ?>[] entityExpressions, String op, String quantifier, DetachedCriteria dc) {
            super(op, quantifier, dc);
            this.entityExpressions = entityExpressions;
        }

        @Override
        public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
            TypedValue[] typedValues = super.getTypedValues(criteria, criteriaQuery);
            if (firstResult == null && maxResults == null)
                return typedValues;
            return Query.from(typedValues)
                    .when(firstResult != null, x -> x.combine(new TypedValue(IntegerType.INSTANCE, firstResult)))
                    .when(maxResults != null, x -> x.combine(new TypedValue(IntegerType.INSTANCE, maxResults)))
                    .toArray(TypedValue.class);
        }

        @Override
        protected String toLeftSqlString(Criteria criteria, CriteriaQuery criteriaQuery) {
            String sql = Query.from(entityExpressions).select(x -> x.toSql(y -> criteriaQuery.getColumn(criteria, y), false)).toString(",");
            if (entityExpressions.length > 1)
                sql = "(" + sql + ")";
            return sql;
        }

        @Override
        public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
            String sql = super.toSqlString(criteria, criteriaQuery);
            if (firstResult == null && maxResults == null)
                return sql;
            RowSelection rowSelection = new RowSelection();
            rowSelection.setFirstRow(firstResult);
            rowSelection.setMaxRows(maxResults);
            return new LegacyLimitHandler(criteriaQuery.getFactory().getDialect())
                    .processSql(QueryString.of(sql).substringToLast(")").toString(), rowSelection) + ")";
        }
    }
}
