/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.DataHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static nl.uva.lm.LanguageModel.sortByValues;
import nl.uva.lucenefacility.MyAnalyzer;
import static nl.uva.settings.Config.configFile;
import org.apache.lucene.queries.TermsFilter;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.util.BytesRef;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Mostafa Dehghani
 */
public class Retrieval {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Retrieval.class.getName());
    private IndexSearcher iSearcher;
    int hitsPerReq = 50;

    public Retrieval() throws IOException {

        this.iSearcher = new IndexSearcher(IndexHandler.ireader);
        this.iSearcher.setSimilarity(new BM25Similarity(1.2F, 0.75F));
    }

    
    
    private Query queryCreator(String qStr) throws IOException, ParseException, Throwable{
        log.info("creating query....");
        MyAnalyzer myAnalyzer;
        if (!IndexHandler.stopWordsRemoving) {
                myAnalyzer = new MyAnalyzer(IndexHandler.stemming);
        }
        else{
            myAnalyzer = new MyAnalyzer(IndexHandler.stemming, IndexHandler.getStopwords());
        }
        QueryParser qp = new QueryParser("TEXT",myAnalyzer.getAnalyzer(configFile.getProperty("CORPUS_LANGUAGE")));
        Query q = qp.parse(QueryParser.escape(qStr));
        return q;
    }
    
      
    private ScoreDoc[] searchIndex(String qText, Filter filter)throws IOException, Throwable{
        log.info("size of query: " +  qText.length());
        boolean retry = true;
        ScoreDoc[] hits = null;
        while (retry)
        {
            try
            {
                retry = false;
                Query q = this.queryCreator(qText);
                TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerReq);
                this.iSearcher.search(q, filter, collector);
//                this.iSearcher.search(q, collector);
                hits = collector.topDocs().scoreDocs;
                return hits;
            }
//            catch (BooleanQuery.TooManyClauses e)
            catch (Exception e)
            {
                // Double the number of boolean queries allowed.
                // The default is in org.apache.lucene.search.BooleanQuery and is 1024.
                String defaultQueries = Integer.toString(BooleanQuery.getMaxClauseCount());
                int oldQueries = Integer.parseInt(System.getProperty("org.apache.lucene.maxClauseCount", defaultQueries));
                int newQueries = oldQueries * 2;
                log.error("Too many hits for query..., changing maxClauseCount to: " + newQueries);
//                log.error("Too many hits for query: " + oldQueries + ".  Increasing to " + newQueries, e);
                System.setProperty("org.apache.lucene.maxClauseCount", Integer.toString(newQueries));
                BooleanQuery.setMaxClauseCount(newQueries);
                retry = true;
            }
        }
        return hits;
    }
     
    
    private void Phase_1_OutputGenerator(String rId, List<Map.Entry<String, Double>> sortedCandidates, String FileName) throws IOException{
            JSONObject obj = new JSONObject();
            obj.put("id", new Integer(rId));
            obj.put("team", "ExPoSe");
            obj.put("run", "run_all");
            JSONArray suggestions = new JSONArray();
            for(Map.Entry<String, Double> e : sortedCandidates){
                suggestions.add(e.getKey());
            }
            obj.put("suggestions", suggestions);
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(FileName, true)));
            out.println(obj);
            out.close();
    //        System.out.print(obj);
     }
    
    
    
    
    public void retrieve() throws IOException, Exception, Throwable {
        String resName = "response_all.json";
        File f = new File(resName);
            f.delete();
        String reqs = configFile.getProperty("REQS");
        String line = null;
        BufferedReader br = new BufferedReader(new FileReader(reqs));
        Integer cnt = 1;
        RequestInfo reqInfo = new RequestInfo();
        while ((line = br.readLine()) != null) {
            log.info("======================================================");
//            log.info("req num: " + cnt++);
            reqInfo.setJson(line);
            Request req = reqInfo.GetRequest();
            log.info("req num: " + req.reqId);
            log.info("request is ready.");
            log.info("query has been created.");
            System.out.println("locationId " + req.locationId);
            Filter cityFilter = new TermsFilter("CITY", new BytesRef(req.locationId));
//        TermRangeFilter("CITY", new BytesRef(req.locationId), null, true, false);
            log.info("start searching...");
//            ScoreDoc[] hits = this.searchIndex(req.getPositiveTags(), cityFilter);
//            ScoreDoc[] hits = this.searchIndex(req.getPositiveQuery(), cityFilter);
            ScoreDoc[] hits = this.searchIndex(req.getPositiveAll(), cityFilter);
            HashMap<String, Double> scores = new HashMap<>();
            for (int i = 0; i < hits.length; i++) {
                String dId = IndexHandler.iInfo.getDocStringId(hits[i].doc);
                Double dScore = (double) hits[i].score;
                scores.put(dId, dScore);
            }
            List<Map.Entry<String, Double>> sortedCandidates = sortByValues(scores, false);
            log.info("writing results to file...");
            this.Phase_1_OutputGenerator(req.reqId, sortedCandidates, resName);
        }
    }
    
    public static void main(String[] args) throws IOException, Throwable {
        Retrieval retrieval = new Retrieval();
        retrieval.retrieve();
    }
    
    
}
