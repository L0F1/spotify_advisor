package advisor;

import java.util.List;

public class QueryResultView {

    public void updatePage(List<String> result, int resPerPage, int currentPage, int pages) {
        int startIndex =resPerPage*currentPage-1;
        for (int i = startIndex; i < startIndex+resPerPage; i++) {
            System.out.println(result.get(i));
        }
        System.out.println(String.format("---PAGE %d OF %d---", currentPage, pages));
    }
}
