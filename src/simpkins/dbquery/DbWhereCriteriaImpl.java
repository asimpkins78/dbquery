package simpkins.dbquery;

import simpkins.query.Query;

import org.hibernate.*;
import org.hibernate.criterion.*;
import org.hibernate.engine.spi.*;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.type.*;

import java.sql.Types;
import java.util.*;
import java.util.function.*;

@SuppressWarnings("UnusedDeclaration")
public class DbWhereCriteriaImpl<TQuery, TEntity, TValue> implements DbWhereCriteria<TQuery, TEntity, TValue> {
    private TQuery query;
    private EntityExpression<?, TValue> entityExpression;
    private Consumer<Criterion> addCriterion;
    private Consumer<EntityExpression<TEntity, ?>> prepareJoinedDefinitions;
    private boolean ignoreCase = false;
    private boolean sqlNulls = false;

    protected DbWhereCriteriaImpl(TQuery query, EntityExpression<?, TValue> entityExpression, Consumer<Criterion> addCriterion, Consumer<EntityExpression<TEntity, ?>> prepareJoinedDefinitions) {
        this.query = query;
        this.entityExpression = entityExpression;
        this.addCriterion = addCriterion;
        this.prepareJoinedDefinitions = prepareJoinedDefinitions;
    }

    @Override
    public DbWhereCriteria<TQuery, TEntity, TValue> sqlNulls(boolean useSqlNulls) {
        this.sqlNulls = useSqlNulls;
        return this;
    }

    @Override
    public DbWhereCriteria<TQuery, TEntity, TValue> ignoreCase(boolean isIgnoreCase) {
        this.ignoreCase = isIgnoreCase;
        return this;
    }

    @Override
    public TQuery isNull() {
        addCriterion.accept(entityExpression.isCustom() ? custom("is null")
                : Restrictions.isNull(entityExpression.getEntityDefinitionFieldMapping()));
        return query;
    }

    @Override
    public TQuery isEqualTo(TValue value) {
        if (!sqlNulls && value == null) return isNull();
        if (ignoreCase) return isEqualToIgnoreCase(value);
        addCriterion.accept(entityExpression.isCustom() ? custom("=", value)
                : Restrictions.eq(entityExpression.getEntityDefinitionFieldMapping(), value));
        return query;
    }

    @Override
    public TQuery isEqualTo(EntityExpression<TEntity, TValue> otherEntityExpression) {
        if (!sqlNulls && otherEntityExpression == null) return isNull();
        if (ignoreCase) return isEqualToIgnoreCase(otherEntityExpression);
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterion.accept(entityExpression.isCustom() || otherEntityExpression.isCustom() ? custom("=", otherEntityExpression)
                : Restrictions.eqProperty(entityExpression.getEntityDefinitionFieldMapping(), otherEntityExpression.getEntityDefinitionFieldMapping()));
        return query;
    }

    @Override
    public TQuery isEqualToIgnoreCase(TValue value) {
        if (!sqlNulls && value == null) return isNull();
        addCriterion.accept(entityExpression.isCustom()
                ? new EntityCustomExpression<>("lower(%s) = %s", entityExpression, value.toString().toLowerCase()).toHibernateSql()
                : Restrictions.eq(entityExpression.getEntityDefinitionFieldMapping(), value).ignoreCase());
        return query;
    }

    @Override
    public TQuery isEqualToIgnoreCase(EntityExpression<TEntity, TValue> otherEntityExpression) {
        if (!sqlNulls && otherEntityExpression == null) return isNull();
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterion.accept(new EntityCustomExpression<>("lower(%s) = lower(%s)", entityExpression, otherEntityExpression).toHibernateSql());
        return query;
    }

    @Override
    public TQuery isLike(TValue value) {
        if (!sqlNulls && value == null) return isNull();
        if (ignoreCase) return isLikeIgnoreCase(value);
        addCriterion.accept(entityExpression.isCustom() ? custom("like", value)
                : Restrictions.like(entityExpression.getEntityDefinitionFieldMapping(), value));
        return query;
    }

    @Override
    public TQuery isLike(EntityExpression<TEntity, TValue> otherEntityExpression) {
        if (!sqlNulls && otherEntityExpression == null) return isNull();
        if (ignoreCase) return isLikeIgnoreCase(otherEntityExpression);
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterion.accept(custom("like", otherEntityExpression));
        return query;
    }

    @Override
    public TQuery isLikeIgnoreCase(TValue value) {
        if (!sqlNulls && value == null) return isNull();
        addCriterion.accept(entityExpression.isCustom() ? custom("ilike", value)
                : Restrictions.ilike(entityExpression.getEntityDefinitionFieldMapping(), value));
        return query;
    }

    @Override
    public TQuery isLikeIgnoreCase(EntityExpression<TEntity, TValue> otherEntityExpression) {
        if (!sqlNulls && otherEntityExpression == null) return isNull();
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterion.accept(custom("ilike", otherEntityExpression));
        return query;
    }

    @Override
    public TQuery isRegexMatch(TValue value) {
        if (!sqlNulls && value == null) return isNull();
        if (ignoreCase) return isRegexMatchIgnoreCase(value);
        addCriterion.accept(entityExpression.isCustom() ? custom("~", value)
                : new SimpleExpressionOverride(entityExpression.getEntityDefinitionFieldMapping(), value, " ~ ", false));
        return query;
    }

    @Override
    public TQuery isRegexMatch(EntityExpression<TEntity, TValue> otherEntityExpression) {
        if (!sqlNulls && otherEntityExpression == null) return isNull();
        if (ignoreCase) return isRegexMatchIgnoreCase(otherEntityExpression);
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterion.accept(custom("~", otherEntityExpression));
        return query;
    }

    @Override
    public TQuery isRegexMatchIgnoreCase(TValue value) {
        if (!sqlNulls && value == null) return isNull();
        addCriterion.accept(entityExpression.isCustom() ? custom("~*", value)
                : new SimpleExpressionOverride(entityExpression.getEntityDefinitionFieldMapping(), value, " ~* ", false));
        return query;
    }

    @Override
    public TQuery isRegexMatchIgnoreCase(EntityExpression<TEntity, TValue> otherEntityExpression) {
        if (!sqlNulls && otherEntityExpression == null) return isNull();
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterion.accept(custom("~*", otherEntityExpression));
        return query;
    }

    @Override
    public TQuery isGreaterThan(TValue value) {
        addCriterion.accept(entityExpression.isCustom() ? custom(">", value, true)
                : Restrictions.gt(entityExpression.getEntityDefinitionFieldMapping(), value));
        return query;
    }

    @Override
    public TQuery isGreaterThan(EntityExpression<TEntity, TValue> otherEntityExpression) {
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterion.accept(entityExpression.isCustom() || otherEntityExpression.isCustom() ? custom(">", otherEntityExpression, true)
                : Restrictions.gtProperty(entityExpression.getEntityDefinitionFieldMapping(), otherEntityExpression.getEntityDefinitionFieldMapping()));
        return query;
    }

    @Override
    public TQuery isGreaterThanOrEqualTo(TValue value) {
        addCriterion.accept(entityExpression.isCustom() ? custom(">=", value, true)
                : Restrictions.ge(entityExpression.getEntityDefinitionFieldMapping(), value));
        return query;
    }

    @Override
    public TQuery isGreaterThanOrEqualTo(EntityExpression<TEntity, TValue> otherEntityExpression) {
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterion.accept(entityExpression.isCustom() || otherEntityExpression.isCustom() ? custom(">=", otherEntityExpression, true)
                : Restrictions.geProperty(entityExpression.getEntityDefinitionFieldMapping(), otherEntityExpression.getEntityDefinitionFieldMapping()));
        return query;
    }

    @Override
    public TQuery isLessThan(TValue value) {
        addCriterion.accept(entityExpression.isCustom() ? custom("<", value, true)
                : Restrictions.lt(entityExpression.getEntityDefinitionFieldMapping(), value));
        return query;
    }

    @Override
    public TQuery isLessThan(EntityExpression<TEntity, TValue> otherEntityExpression) {
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterion.accept(entityExpression.isCustom() || otherEntityExpression.isCustom() ? custom("<", otherEntityExpression, true)
                : Restrictions.ltProperty(entityExpression.getEntityDefinitionFieldMapping(), otherEntityExpression.getEntityDefinitionFieldMapping()));
        return query;
    }

    @Override
    public TQuery isLessThanOrEqualTo(TValue value) {
        addCriterion.accept(entityExpression.isCustom() ? custom("<=", value, true)
                : Restrictions.le(entityExpression.getEntityDefinitionFieldMapping(), value));
        return query;
    }

    @Override
    public TQuery isLessThanOrEqualTo(EntityExpression<TEntity, TValue> otherEntityExpression) {
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterion.accept(entityExpression.isCustom() || otherEntityExpression.isCustom() ? custom("<=", otherEntityExpression, true)
                : Restrictions.leProperty(entityExpression.getEntityDefinitionFieldMapping(), otherEntityExpression.getEntityDefinitionFieldMapping()));
        return query;
    }

    @Override
    public TQuery isBetween(TValue lowValue, TValue highValue) {
        String s = ignoreCase ? "lower(%s)" : "%s";
        addCriterion.accept(entityExpression.isCustom()
                ? new EntityCustomExpression<>(s + " between " + s + " and " + s, entityExpression, lowValue, highValue).toHibernateSql()
                : Restrictions.between(entityExpression.getEntityDefinitionFieldMapping(), lowValue, highValue));
        return query;
    }

    @Override
    public TQuery isBetween(TValue lowValue, EntityExpression<TEntity, TValue> highEntityExpression) {
        String s = ignoreCase ? "lower(%s)" : "%s";
        prepareJoinedDefinitions.accept(highEntityExpression);
        addCriterion.accept(new EntityCustomExpression<>(s + " between " + s + " and " + s, entityExpression, lowValue, highEntityExpression).toHibernateSql());
        return query;
    }

    @Override
    public TQuery isBetween(EntityExpression<TEntity, TValue> lowEntityExpression, TValue highValue) {
        String s = ignoreCase ? "lower(%s)" : "%s";
        prepareJoinedDefinitions.accept(lowEntityExpression);
        addCriterion.accept(new EntityCustomExpression<>(s + " between " + s + " and " + s, entityExpression, lowEntityExpression, highValue).toHibernateSql());
        return query;
    }

    @Override
    public TQuery isBetween(EntityExpression<TEntity, TValue> lowEntityExpression, EntityExpression<TEntity, TValue> highEntityExpression) {
        String s = ignoreCase ? "lower(%s)" : "%s";
        prepareJoinedDefinitions.accept(lowEntityExpression);
        prepareJoinedDefinitions.accept(highEntityExpression);
        addCriterion.accept(new EntityCustomExpression<>(s + " between " + s + " and " + s, entityExpression, lowEntityExpression, highEntityExpression).toHibernateSql());
        return query;
    }

    @Override
    @SafeVarargs
    public final TQuery isIn(TValue... values) {
        return isIn(values != null ? Arrays.asList(values) : getEmptyValueIterable());
    }

    @Override
    public TQuery isIn(Iterable<TValue> values) {
        return isIn(values, false);
    }

    @Override
    public TQuery isIn(EntityExpression<TEntity, ?>... otherEntityExpressions) {
        Iterable<EntityExpression<TEntity, ?>> iterable = otherEntityExpressions != null ? Arrays.asList(otherEntityExpressions) : getEmptyExpressionIterable();
        for (EntityExpression<TEntity, ?> otherEntityExpression : iterable)
            prepareJoinedDefinitions.accept(otherEntityExpression);
        return isIn(iterable, false);
    }

    @Override
    @SafeVarargs
    public final TQuery isInIgnoreCase(TValue... values) {
        return isInIgnoreCase(values != null ? Arrays.asList(values) : getEmptyValueIterable());
    }

    @Override
    public TQuery isInIgnoreCase(Iterable<TValue> values) {
        this.ignoreCase = true;
        return isIn(values, false);
    }

    @Override
    public TQuery isInIgnoreCase(EntityExpression<TEntity, ?>... otherEntityExpressions) {
        this.ignoreCase = true;
        Iterable<EntityExpression<TEntity, ?>> iterable = otherEntityExpressions != null ? Arrays.asList(otherEntityExpressions) : getEmptyExpressionIterable();
        for (EntityExpression<TEntity, ?> otherEntityExpression : iterable)
            prepareJoinedDefinitions.accept(otherEntityExpression);
        return isIn(iterable, false);
    }

    @Override
    public TQuery isNotNull() {
        addCriterion.accept(entityExpression.isCustom() ? custom("is not null")
                : Restrictions.isNotNull(entityExpression.getEntityDefinitionFieldMapping()));
        return query;
    }

    @Override
    public TQuery isNotEqualTo(TValue value) {
        if (!sqlNulls && value == null) return isNotNull();
        if (ignoreCase) return isNotEqualToIgnoreCase(value);
        addCriterionWithNullHandling(entityExpression.isCustom() ? custom("<>", value)
                : Restrictions.ne(entityExpression.getEntityDefinitionFieldMapping(), value));
        return query;
    }

    @Override
    public TQuery isNotEqualTo(EntityExpression<TEntity, TValue> otherEntityExpression) {
        if (!sqlNulls && otherEntityExpression == null) return isNotNull();
        if (ignoreCase) return isNotEqualToIgnoreCase(otherEntityExpression);
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterionWithNullHandling(entityExpression.isCustom() || otherEntityExpression.isCustom() ? custom("<>", otherEntityExpression)
                : Restrictions.neProperty(entityExpression.getEntityDefinitionFieldMapping(), otherEntityExpression.getEntityDefinitionFieldMapping()));
        return query;
    }

    @Override
    public TQuery isNotEqualToIgnoreCase(TValue value) {
        if (!sqlNulls && value == null) return isNotNull();
        addCriterionWithNullHandling(entityExpression.isCustom()
                ? new EntityCustomExpression<>("lower(%s) <> %s", entityExpression, value.toString().toLowerCase()).toHibernateSql()
                : Restrictions.ne(entityExpression.getEntityDefinitionFieldMapping(), value).ignoreCase());
        return query;
    }

    @Override
    public TQuery isNotEqualToIgnoreCase(EntityExpression<TEntity, TValue> otherEntityExpression) {
        if (!sqlNulls && otherEntityExpression == null) return isNotNull();
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterionWithNullHandling(new EntityCustomExpression<>("lower(%s) <> lower(%s)", entityExpression, otherEntityExpression).toHibernateSql());
        return query;
    }

    @Override
    public TQuery isNotLike(TValue value) {
        if (!sqlNulls && value == null) return isNotNull();
        if (ignoreCase) return isNotLikeIgnoreCase(value);
        addCriterionWithNullHandling(Restrictions.not(entityExpression.isCustom() ? custom("like", value)
                : Restrictions.like(entityExpression.getEntityDefinitionFieldMapping(), value)));
        return query;
    }

    @Override
    public TQuery isNotLike(EntityExpression<TEntity, TValue> otherEntityExpression) {
        if (!sqlNulls && otherEntityExpression == null) return isNotNull();
        if (ignoreCase) return isNotLikeIgnoreCase(otherEntityExpression);
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterionWithNullHandling(Restrictions.not(custom("like", otherEntityExpression)));
        return query;
    }

    @Override
    public TQuery isNotLikeIgnoreCase(TValue value) {
        if (!sqlNulls && value == null) return isNotNull();
        addCriterionWithNullHandling(Restrictions.not(entityExpression.isCustom() ? custom("ilike", value)
                : Restrictions.ilike(entityExpression.getEntityDefinitionFieldMapping(), value)));
        return query;
    }

    @Override
    public TQuery isNotLikeIgnoreCase(EntityExpression<TEntity, TValue> otherEntityExpression) {
        if (!sqlNulls && otherEntityExpression == null) return isNotNull();
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterionWithNullHandling(Restrictions.not(custom("ilike", otherEntityExpression)));
        return query;
    }

    @Override
    public TQuery isNotRegexMatch(TValue value) {
        if (!sqlNulls && value == null) return isNotNull();
        if (ignoreCase) return isNotRegexMatchIgnoreCase(value);
        addCriterionWithNullHandling(entityExpression.isCustom() ? custom("!~", value)
                : new SimpleExpressionOverride(entityExpression.getEntityDefinitionFieldMapping(), value, " !~ ", false));
        return query;
    }

    @Override
    public TQuery isNotRegexMatch(EntityExpression<TEntity, TValue> otherEntityExpression) {
        if (!sqlNulls && otherEntityExpression == null) return isNotNull();
        if (ignoreCase) return isNotRegexMatchIgnoreCase(otherEntityExpression);
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterionWithNullHandling(custom("!~", otherEntityExpression));
        return query;
    }

    @Override
    public TQuery isNotRegexMatchIgnoreCase(TValue value) {
        if (!sqlNulls && value == null) return isNotNull();
        addCriterionWithNullHandling(entityExpression.isCustom() ? custom("!~*", value)
                : new SimpleExpressionOverride(entityExpression.getEntityDefinitionFieldMapping(), value, " !~* ", false));
        return query;
    }

    @Override
    public TQuery isNotRegexMatchIgnoreCase(EntityExpression<TEntity, TValue> otherEntityExpression) {
        if (!sqlNulls && otherEntityExpression == null) return isNotNull();
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterionWithNullHandling(custom("!~*", otherEntityExpression));
        return query;
    }

    @Override
    public TQuery isNotGreaterThan(TValue value) {
        addCriterionWithNullHandling(Restrictions.not(entityExpression.isCustom() ? custom(">", value, true)
                : Restrictions.gt(entityExpression.getEntityDefinitionFieldMapping(), value)));
        return query;
    }

    @Override
    public TQuery isNotGreaterThan(EntityExpression<TEntity, TValue> otherEntityExpression) {
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterionWithNullHandling(Restrictions.not(entityExpression.isCustom() || otherEntityExpression.isCustom() ? custom(">", otherEntityExpression, true)
                : Restrictions.gtProperty(entityExpression.getEntityDefinitionFieldMapping(), otherEntityExpression.getEntityDefinitionFieldMapping())));
        return query;
    }

    @Override
    public TQuery isNotGreaterThanOrEqualTo(TValue value) {
        addCriterionWithNullHandling(Restrictions.not(entityExpression.isCustom() ? custom(">=", value, true)
                : Restrictions.ge(entityExpression.getEntityDefinitionFieldMapping(), value)));
        return query;
    }

    @Override
    public TQuery isNotGreaterThanOrEqualTo(EntityExpression<TEntity, TValue> otherEntityExpression) {
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterionWithNullHandling(Restrictions.not(entityExpression.isCustom() || otherEntityExpression.isCustom() ? custom(">=", otherEntityExpression, true)
                : Restrictions.geProperty(entityExpression.getEntityDefinitionFieldMapping(), otherEntityExpression.getEntityDefinitionFieldMapping())));
        return query;
    }

    @Override
    public TQuery isNotLessThan(TValue value) {
        addCriterionWithNullHandling(Restrictions.not(entityExpression.isCustom() ? custom("<", value, true)
                : Restrictions.lt(entityExpression.getEntityDefinitionFieldMapping(), value)));
        return query;
    }

    @Override
    public TQuery isNotLessThan(EntityExpression<TEntity, TValue> otherEntityExpression) {
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterionWithNullHandling(Restrictions.not(entityExpression.isCustom() || otherEntityExpression.isCustom() ? custom("<", otherEntityExpression, true)
                : Restrictions.ltProperty(entityExpression.getEntityDefinitionFieldMapping(), otherEntityExpression.getEntityDefinitionFieldMapping())));
        return query;
    }

    @Override
    public TQuery isNotLessThanOrEqualTo(TValue value) {
        addCriterionWithNullHandling(Restrictions.not(entityExpression.isCustom() ? custom("<", value, true)
                : Restrictions.le(entityExpression.getEntityDefinitionFieldMapping(), value)));
        return query;
    }

    @Override
    public TQuery isNotLessThanOrEqualTo(EntityExpression<TEntity, TValue> otherEntityExpression) {
        prepareJoinedDefinitions.accept(otherEntityExpression);
        addCriterionWithNullHandling(Restrictions.not(entityExpression.isCustom() || otherEntityExpression.isCustom() ? custom("<=", otherEntityExpression, true)
                : Restrictions.leProperty(entityExpression.getEntityDefinitionFieldMapping(), otherEntityExpression.getEntityDefinitionFieldMapping())));
        return query;
    }

    @Override
    public TQuery isNotBetween(TValue lowValue, TValue highValue) {
        String s = ignoreCase ? "lower(%s)" : "%s";
        addCriterionWithNullHandling(entityExpression.isCustom()
                ? new EntityCustomExpression<>("not " + s + " between " + s + " and " + s, entityExpression, lowValue, highValue).toHibernateSql()
                : Restrictions.not(Restrictions.between(entityExpression.getEntityDefinitionFieldMapping(), lowValue, highValue)));
        return query;
    }

    @Override
    public TQuery isNotBetween(TValue lowValue, EntityExpression<TEntity, TValue> highEntityExpression) {
        String s = ignoreCase ? "lower(%s)" : "%s";
        prepareJoinedDefinitions.accept(highEntityExpression);
        addCriterionWithNullHandling(new EntityCustomExpression<>("not " + s + " between " + s + " and " + s, entityExpression, lowValue, highEntityExpression).toHibernateSql());
        return query;
    }

    @Override
    public TQuery isNotBetween(EntityExpression<TEntity, TValue> lowEntityExpression, TValue highValue) {
        String s = ignoreCase ? "lower(%s)" : "%s";
        prepareJoinedDefinitions.accept(lowEntityExpression);
        addCriterionWithNullHandling(new EntityCustomExpression<>("not " + s + " between " + s + " and " + s, entityExpression, lowEntityExpression, highValue).toHibernateSql());
        return query;
    }

    @Override
    public TQuery isNotBetween(EntityExpression<TEntity, TValue> lowEntityExpression, EntityExpression<TEntity, TValue> highEntityExpression) {
        String s = ignoreCase ? "lower(%s)" : "%s";
        prepareJoinedDefinitions.accept(lowEntityExpression);
        prepareJoinedDefinitions.accept(highEntityExpression);
        addCriterionWithNullHandling(new EntityCustomExpression<>("not " + s + " between " + s + " and " + s, entityExpression, lowEntityExpression, highEntityExpression).toHibernateSql());
        return query;
    }

    @Override
    @SafeVarargs
    public final TQuery isNotIn(TValue... values) {
        return isNotIn(values != null ? Arrays.asList(values) : getEmptyValueIterable());
    }

    @Override
    public TQuery isNotIn(Iterable<TValue> values) {
        return isIn(values, true);
    }

    @Override
    public TQuery isNotIn(EntityExpression<TEntity, ?>... otherEntityExpressions) {
        Iterable<EntityExpression<TEntity, ?>> iterable = otherEntityExpressions != null ? Arrays.asList(otherEntityExpressions) : getEmptyExpressionIterable();
        for (EntityExpression<TEntity, ?> otherEntityExpression : iterable)
            prepareJoinedDefinitions.accept(otherEntityExpression);
        return isIn(iterable, true);
    }

    @Override
    @SafeVarargs
    public final TQuery isNotInIgnoreCase(TValue... values) {
        this.ignoreCase = true;
        return isNotInIgnoreCase(values != null ? Arrays.asList(values) : getEmptyValueIterable());
    }

    @Override
    public TQuery isNotInIgnoreCase(Iterable<TValue> values) {
        this.ignoreCase = true;
        return isIn(values, true);
    }

    @Override
    public TQuery isNotInIgnoreCase(EntityExpression<TEntity, ?>... otherEntityExpressions) {
        this.ignoreCase = true;
        Iterable<EntityExpression<TEntity, ?>> iterable = otherEntityExpressions != null ? Arrays.asList(otherEntityExpressions) : getEmptyExpressionIterable();
        for (EntityExpression<TEntity, ?> otherEntityExpression : iterable)
            prepareJoinedDefinitions.accept(otherEntityExpression);
        return isIn(iterable, true);
    }

    private Iterable<TValue> getEmptyValueIterable() {
        return () -> new Iterator<TValue>() {
            @Override public boolean hasNext() {
                return false;
            }
            @Override public TValue next() {
                throw new NoSuchElementException();
            }
        };
    }

    private Iterable<EntityExpression<TEntity, ?>> getEmptyExpressionIterable() {
        return () -> new Iterator<EntityExpression<TEntity, ?>>() {
            @Override public boolean hasNext() {
                return false;
            }
            @Override public EntityExpression<TEntity, ?> next() {
                throw new NoSuchElementException();
            }
        };
    }

    private Criterion custom(String operator) {
        return custom(operator, false);
    }

    private Criterion custom(String operator, boolean checkIgnoreCase) {
        String s = checkIgnoreCase && ignoreCase ? "lower(%s)" : "%s";
        return new EntityCustomExpression<>(s + " " + operator, entityExpression).toHibernateSql();
    }

    private Criterion custom(String operator, Object other) {
        return custom(operator, other, false);
    }

    private Criterion custom(String operator, Object other, boolean checkIgnoreCase) {
        String s = checkIgnoreCase && ignoreCase ? "lower(%s)" : "%s";
        return new EntityCustomExpression<>(s + " " + operator + " " + s, entityExpression, other).toHibernateSql();
    }

    private void addCriterionWithNullHandling(Criterion criterion) {
        if (!sqlNulls && entityExpression.isNullable()) {
            addCriterion.accept(Restrictions.disjunction()
                    .add(entityExpression.isCustom() ? custom("is null") : Restrictions.isNull(entityExpression.getEntityDefinitionFieldMapping()))
                    .add(criterion));
        }
        else {
            addCriterion.accept(criterion);
        }
    }

    private TQuery isIn(Iterable<?> values, boolean isNot) {
        Query<?> valuesQuery = values != null ? Query.from(values) : Query.empty();
        Object[] valuesArray = !sqlNulls ? valuesQuery.where(x -> x != null).toArray() : valuesQuery.toArray();
        boolean hasValues = valuesArray.length > 0;
        boolean hasNonNullValues = !sqlNulls ? hasValues : valuesQuery.any(x -> x != null);
        boolean isCustom = entityExpression.isCollection() || valuesQuery.any(x -> x instanceof EntityExpression);

        Function<Object[], Criterion> buildCriterion = items -> {
            Criterion criterion;
            if (isCustom) {
                String s = ignoreCase ? "lower(%s)" : "%s";
                criterion = new EntityCustomExpression<>(s + (isNot ? " not" : "") + " in (" + Query.generate(items.length, s).toString(",") + ")",
                        Query.from(items).insert(0, entityExpression).toArray()).toHibernateSql();
            }
            else {
                criterion = ignoreCase
                        ? new InExpression(entityExpression.getEntityDefinitionFieldMapping(), items, true)
                        : Restrictions.in(entityExpression.getEntityDefinitionFieldMapping(), items);
                if (isNot)
                    criterion = Restrictions.not(criterion);
            }
            return criterion;
        };

        // if this was just null, then do a simple null check
        boolean hasNulls = valuesQuery.any(x -> x == null);
        if (!sqlNulls && hasNulls && !hasValues)
            return isNot ? isNotNull() : isNull();

        // if not using SQL nulls then do special handling
        // if nullable and either is not in expression or we have nulls
        if (!sqlNulls && entityExpression.isNullable() && (isNot || hasNulls)) {
            // is not null and not in() vs. is null or in()/not in()
            Junction junction = isNot && hasNulls ? Restrictions.conjunction() : Restrictions.disjunction();
            junction.add(isNot && hasNulls ? Restrictions.isNotNull(entityExpression.getEntityDefinitionFieldMapping()) : Restrictions.isNull(entityExpression.getEntityDefinitionFieldMapping()));
            junction.add(buildCriterion.apply(valuesArray));
            addCriterion.accept(junction);
        }
        // otherwise we only care if there are values or we have isIn() with no values
        else if (hasValues || !isNot) {
            // if there are no values then add null to avoid a sql error
            addCriterion.accept(buildCriterion.apply(hasValues ? valuesArray : new Object[] { null }));
        }
        return query;
    }

    // subclass hack to get around SimpleExpression's protected constructors
    private class SimpleExpressionOverride extends SimpleExpression {
        private SimpleExpressionOverride(String propertyName, Object value, String op, boolean ignoreCase) {
            super(propertyName, value, op, ignoreCase);
        }
    }

    /**
     * A slightly modified clone of org.hibernate.criterion.InExpression with some logic changes to handle
     * case insensitive comparisons.
     *
     * This should also work when ignoreCase == false, but I haven't tested it.  I just use the original
     * org.hibernate.criterion.InExpression.
     */
    @SuppressWarnings({"ForLoopReplaceableByForEach", "unchecked"})
    private class InExpression implements Criterion {

        private final String propertyName;
        private final Object[] values;
        private final boolean ignoreCase;

        public InExpression(String propertyName, Object[] values, boolean ignoreCase) {
            this.propertyName = propertyName;
            this.values = values;
            this.ignoreCase = ignoreCase;
        }

        public String toSqlString( Criteria criteria, CriteriaQuery criteriaQuery )
                throws HibernateException {
            String[] columns = prepareColumns(criteria, criteriaQuery);
            if ( criteriaQuery.getFactory().getDialect()
                    .supportsRowValueConstructorSyntaxInInList() || columns.length<=1) {

                String singleValueParam = StringHelper.repeat("?, ",
                        columns.length - 1)
                        + "?";
                if ( columns.length > 1 )
                    singleValueParam = '(' + singleValueParam + ')';
                String params = values.length > 0 ? StringHelper.repeat(
                        singleValueParam + ", ", values.length - 1 )
                        + singleValueParam : "";
                String cols = StringHelper.join( ", ", columns );
                if ( columns.length > 1 )
                    cols = '(' + cols + ')';
                return cols + " in (" + params + ')';
            } else {
                String cols = " ( " + StringHelper.join( " = ? and ", columns ) + "= ? ) ";
                cols = values.length > 0 ? StringHelper.repeat( cols
                        + "or ", values.length - 1 )
                        + cols : "";
                cols = " ( " + cols + " ) ";
                return cols;
            }
        }

        public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery)
                throws HibernateException {
            ArrayList list = new ArrayList();
            Type type = criteriaQuery.getTypeUsingProjection(criteria, propertyName);
            if ( type.isComponentType() ) {
                CompositeType actype = (CompositeType) type;
                Type[] types = actype.getSubtypes();
                for ( int j=0; j<values.length; j++ ) {
                    for ( int i=0; i<types.length; i++ ) {
                        Object subval = values[j]==null ?
                                null :
                                actype.getPropertyValues( values[j], EntityMode.POJO )[i];
                        list.add( new TypedValue( types[i], subval ) );
                    }
                }
            }
            else {
                for ( int j=0; j<values.length; j++ ) {
                    Object icValue = values[j] != null && ignoreCase ? values[j].toString().toLowerCase() : values[j];
                    list.add( new TypedValue( type, icValue ) );
                }
            }
            return (TypedValue[]) list.toArray( new TypedValue[ list.size() ] );
        }

        public String toString() {
            return propertyName + " in (" + StringHelper.toString(values) + ')';
        }

        private String[] prepareColumns(Criteria criteria, CriteriaQuery criteriaQuery) {
            String[] columns = criteriaQuery.findColumns( propertyName, criteria );
            if (!ignoreCase)
                return columns;
            Type type = criteriaQuery.getTypeUsingProjection( criteria, propertyName );
            SessionFactoryImplementor factory = criteriaQuery.getFactory();
            int[] sqlTypes = type.sqlTypes( factory );
            String[] formattedColumns = new String[columns.length];
            for ( int i = 0; i < columns.length; i++ ) {
                boolean lower = (sqlTypes[i] == Types.VARCHAR || sqlTypes[i] == Types.CHAR);
                formattedColumns[i] =
                        (lower ? factory.getDialect().getLowercaseFunction() + "(" : "") + columns[i] + (lower ? ")" : "");
            }
            return formattedColumns;
        }
    }
}
