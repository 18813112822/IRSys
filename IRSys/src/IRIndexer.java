import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.ansj.lucene3.AnsjAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class IRIndexer {
    private Analyzer analyzer;
    private IndexWriter indexWriter;
    private HashMap<String, Float> globals = new HashMap<String, Float>();
    
    public IRIndexer(String indexDir) {
    	analyzer = new AnsjAnalysis();
//    	analyzer = new AnsjIndexAnalysis();
//    	analyzer = new MyAnalyzer();
        try {
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35, analyzer);
            iwc.setOpenMode(OpenMode.CREATE);
            Directory dir = FSDirectory.open(new File(indexDir));
            indexWriter = new IndexWriter(dir, iwc);
//          indexWriter.setSimilarity(new SimpleSimilarity());
            
//            Analyzer test = new MyAnalyzer();
//            TokenStream tokenStream  = test.tokenStream("", new StringReader(""));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void saveGlobals(String filename) {
        try {
            PrintWriter pw = new PrintWriter(new File(filename));
            
            for (HashMap.Entry<String, Float> entry: globals.entrySet()){
            	pw.println(entry.getKey()+"="+entry.getValue());
            }
            pw.close();
        } catch (IOException e) {
            
        }
    }

    /**
     * <p>
     * index "CNKI_journal_v2.txt"
     *
     */
    public void indexSpecialFile(String filename) {
    	try(BufferedReader br = new BufferedReader(new FileReader(new File(filename)))) {
    		Document document = null;
    		String fieldname = "";
    		String fieldtext = "";
    	    for(String line; (line = br.readLine()) != null; ) {
    	    	if (!line.startsWith("<")){
    	    		if (fieldname.compareTo("") != 0)
    	    			fieldtext += line;
    	    		continue;
    	    	}
    	    	if (fieldname.compareTo("") != 0){
    	    		Field field = new Field(fieldname, fieldtext, Field.Store.YES, Field.Index.ANALYZED);
    	    	    document.add(field);
    	    		if (globals.containsKey(fieldname))
	    	        	globals.put(fieldname, globals.get(fieldname)+fieldtext.length());
	    	        else
	    	        	globals.put(fieldname, (float)fieldtext.length());
    	    		fieldname = "";
	    	        fieldtext = "";
    	    	}
    	        if (line.compareTo("<REC>") == 0){
    	        	if (document != null)
    	        		indexWriter.addDocument(document);
    	        	document = new Document();
    	        	continue;
    	        }
    	        String[] split = line.split("=");
    	        List<String> spList = Arrays.asList(split);
    	        fieldname = split[0].substring(1, split[0].length()-1);
    	        fieldtext = String.join("=", spList.subList(1, spList.size()));
    	    }
    	    for (HashMap.Entry<String, Float> entry: globals.entrySet()){
            	globals.put(entry.getKey(), entry.getValue()/indexWriter.numDocs());
            }
    	    System.out.println("total " + indexWriter.numDocs() + " documents");
    	    indexWriter.close();
    	} catch (Exception e) {
    		e.printStackTrace();
		}
    }
    public static void main(String[] args) {
        IRIndexer indexer = new IRIndexer("forIndex/index");
        indexer.indexSpecialFile("input/CNKI_journal_v2.txt");
        indexer.saveGlobals("forIndex/global.txt");
    }
}
