/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.DataHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.uva.lucenefacility.IndexInfo;
import static nl.uva.settings.Config.configFile;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Mostafa Dehghani
 */
public class IndexHandler {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IndexHandler.class.getName());
    public static String indexPathString;
    public static Path ipath;
    public static IndexReader ireader;
    public static IndexInfo iInfo;
    public static final Boolean stopWordsRemoving = configFile.getProperty("IF_STOPWORDS_REMOVING").equals("1");
    public static final Boolean stemming = configFile.getProperty("IF_STEMMING").equals("1");
    public static ArrayList<String> stoplist = new ArrayList<>();
    
    
    static{
        indexPathString = configFile.getProperty("INDEX_PATH");
        ipath = FileSystems.getDefault().getPath(IndexHandler.indexPathString);
        try {
            ireader = DirectoryReader.open(FSDirectory.open(ipath));
        } catch (IOException ex) {
            Logger.getLogger(IndexHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        iInfo = new IndexInfo(ireader);
    }
    
    public static ArrayList<String> getStopwords() throws FileNotFoundException, IOException {
        if(stoplist.isEmpty()){
            File stopfile = new File(configFile.getProperty("STOPWORDS_PATH"));
            try (BufferedReader br = new BufferedReader(new FileReader(stopfile))) {
                for (String line; (line = br.readLine()) != null;) {
                    stoplist.add(line);
                }
            }
            log.info("Stopwords file is loaded....");
        }
        return stoplist;
    }
    
    public static String getAnalyzedText(String docID) throws IOException, Exception, Throwable{
        return iInfo.getDocText(iInfo.getIndexId(docID));
        
//        MyAnalyzer myAnalyzer;
//        if (!IndexHandler.stopWordsRemoving) {
//                myAnalyzer = new MyAnalyzer(IndexHandler.stemming);
//        }
//        else{
//            myAnalyzer = new MyAnalyzer(IndexHandler.stemming, IndexHandler.getStopwords());
//        }
//        String str = IndexHandler.iInfo.getDocAnalyezedText(IndexHandler.iInfo.getIndexId(docID));
//        TokenStream stream = myAnalyzer.getAnalyzer(configFile.getProperty("CORPUS_LANGUAGE"))
//                                    .tokenStream(null, new StringReader(str));
//        CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
//        stream.reset();
//        String text = "";
//        while (stream.incrementToken()) {
//          text += cattr.toString() + " ";
//        }
//        stream.end();
//        stream.close();
//        return text;
    }
    
}
