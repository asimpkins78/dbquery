package simpkins.dbquery;

import java.util.*;

@SuppressWarnings("UnusedDeclaration")
public class DbResultRow {
    private Map<String, Object> results;

    protected DbResultRow(Object[] results, DbResultRowKey... selects) {
        this(results, Arrays.asList(selects));
    }

    protected DbResultRow(Object[] results, Iterable<? extends DbResultRowKey> selects) {
        this.results = selects instanceof Collection
                ? new HashMap<>(((Collection)selects).size())
                : new HashMap<>();
        int count = 0;
        for (DbResultRowKey select : selects)
            this.results.put(select.getKey(), results[count++]);
    }

    public <TValue> TValue get(DbResultRowKey<TValue> select) {
        if (!results.containsKey(select.getKey()))
            throw new RuntimeException("No entry found for key: " + select.getKey());
        //noinspection unchecked
        return (TValue)results.get(select.getKey());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DbResultRow that = (DbResultRow) o;

        if (this.results.size() != that.results.size())
            return false;

        for (String key : this.results.keySet()) {
            if (!that.results.containsKey(key))
                return false;
            if (!Objects.equals(this.results.get(key), that.results.get(key)))
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return results != null ? results.hashCode() : 0;
    }
}
