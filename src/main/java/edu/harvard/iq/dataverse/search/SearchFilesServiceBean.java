package edu.harvard.iq.dataverse.search;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetVersion;
import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.authorization.users.User;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Named;

@Named
@Stateless
public class SearchFilesServiceBean {

    private static final Logger logger = Logger.getLogger(SearchFilesServiceBean.class.getCanonicalName());

    @EJB
    SearchServiceBean searchService;

    public FileView getFileView(DatasetVersion datasetVersion, User user, String userSuppliedQuery) {
        Dataverse dataverse = null;
        List<String> filterQueries = new ArrayList<>();
        filterQueries.add(SearchFields.TYPE + ":" + SearchConstants.FILES);
        filterQueries.add(SearchFields.PARENT_ID + ":" + datasetVersion.getDataset().getId());
        String finalQuery = SearchUtil.determineFinalQuery(userSuppliedQuery);
        SortBy sortBy = getSortBy(finalQuery);
        String sortField = sortBy.getField();
        String sortOrder = sortBy.getOrder();
        int paginationStart = 0;
        boolean onlyDataRelatedToMe = false;
        int numResultsPerPage = 25;
        SolrQueryResponse solrQueryResponse = null;
        try {
            solrQueryResponse = searchService.search(user, dataverse, finalQuery, filterQueries, sortField, sortOrder, paginationStart, onlyDataRelatedToMe, numResultsPerPage);
        } catch (SearchException ex) {
            logger.info(SearchException.class + " searching for files: " + ex);
            return null;
        } catch (Exception ex) {
            logger.info(Exception.class + " searching for files: " + ex);
            return null;
        }

        return new FileView(
                solrQueryResponse.getSolrSearchResults(),
                solrQueryResponse.getFacetCategoryList(),
                solrQueryResponse.getFilterQueriesActual(),
                solrQueryResponse.getSolrQuery().getQuery()
        );
    }

    public static SortBy getSortBy(String query) {
        try {
            if (query != null) {
                return SearchUtil.getSortBy("name", SortBy.ASCENDING);
            } else {
                return SearchUtil.getSortBy(null, null);
            }
        } catch (Exception ex) {
            return null;
        }
    }

}
