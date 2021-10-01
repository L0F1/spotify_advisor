package advisor;

public class QueryResultView {

    public void updatePage(String[] result, int resPerPage, int currentPage, int pages) {
        int startIndex =resPerPage*currentPage-1;
        for (int i = startIndex; i < startIndex+resPerPage; i++) {
            System.out.println(result[i]);
            System.out.println();
        }
        System.out.println(String.format("---PAGE %d OF %d---", currentPage, pages));
    }
}
