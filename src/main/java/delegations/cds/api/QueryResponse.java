package delegations.cds.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.List;

@JsonInclude(Include.NON_NULL)
public class QueryResponse<T> {

    private List<T> results;

    private int count;
    private int page;

    public static <T> QueryResponse<T> create(final List<T> results) {
        QueryResponse<T> queryResponse = new QueryResponse<>();
        queryResponse.results = results;
        queryResponse.count = results.size();
        return queryResponse;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(final List<T> results) {
        this.results = results;
    }

    public int getCount() {
        return count;
    }

    public void setCount(final int count) {
        this.count = count;
    }

    public int getPage() {
        return page;
    }

    public void setPage(final int page) {
        this.page = page;
    }
}
