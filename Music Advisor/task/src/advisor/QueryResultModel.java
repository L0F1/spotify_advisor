package advisor;

import java.util.Arrays;

public class QueryResultModel {

    private String[] queryResult;
    private final int resPerPage;
    private int currentPage;
    private int pages;

    public QueryResultModel(int resPerPage) {
        this.resPerPage = resPerPage;
    }

    public String[] getQueryResult() {
        return Arrays.copyOf(queryResult, queryResult.length);
    }

    public void setQueryResult(String[] queryResult) {
        this.queryResult = queryResult;
        pages = queryResult.length / resPerPage;
        currentPage = 1;
    }

    public int getPages() {
        return pages;
    }

    public int getCurrentPage() {
        return currentPage;
    }
}
