package advisor;

import java.util.ArrayList;
import java.util.List;

public class QueryResultModel {

    private ArrayList<String> queryResult;
    private final int resPerPage;
    private int currentPage;
    private int pages;

    public QueryResultModel(int resPerPage) {
        this.resPerPage = resPerPage;
    }

    public List<String> getQueryResult() {
        return List.copyOf(queryResult);
    }

    public void setQueryResult(ArrayList<String> queryResult) {
        this.queryResult = queryResult;
        pages = queryResult.size() / resPerPage;
        currentPage = 1;
    }

    public int getResPerPage() {
        return resPerPage;
    }

    public int getPages() {
        return pages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
