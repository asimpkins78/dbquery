package simpkins.dbquery;

import org.hibernate.criterion.Criterion;

public interface DbSubQueryable<T> {
    String getCriteriaAlias();
    T addCriterion(Criterion criterion);
}
