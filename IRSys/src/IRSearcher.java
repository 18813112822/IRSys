import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.ansj.lucene3.AnsjAnalysis;
import org.ansj.lucene3.AnsjIndexAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class IRSearcher {
	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private HashMap<String, Float> globals = new HashMap<String, Float>();
	public static final Set<String> STOPWORDS = new HashSet<String>(Arrays.asList(
//				new String[] {" ", "的", "是", "."}
				new String[] {}
			));

	public IRSearcher(String indexdir) {
		analyzer = new AnsjAnalysis();
//		analyzer = new MyAnalyzer();
//		analyzer = new AnsjIndexAnalysis(true);
		try {
			reader = IndexReader.open(FSDirectory.open(new File(indexdir)));
			searcher = new IndexSearcher(reader);
//			searcher.setSimilarity(new SimpleSimilarity());
			loadGlobals("forIndex/global.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public TopDocs searchQuery(String queryString, String field, int maxnum) {
		try {
			HashMap<String,Float> boosts = new HashMap<String,Float>();
			boosts.put("题名", 10f);
			boosts.put("英文篇名", 10f);
			boosts.put("摘要", 5f);
			boosts.put("英文摘要", 5f);
			String[] fields = boosts.keySet().toArray(new String[0]);
			MultiFieldQueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_35, fields, analyzer, boosts);
			Query query = queryParser.parse(queryString);
//			TokenStream tokenStream  = analyzer.tokenStream(field, new StringReader(queryString));
//			CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
//			System.out.println(field+" tokens:");
//			BooleanQuery query = new BooleanQuery();
//			
//			ArrayList<String> termstrings = new ArrayList<>();
//			while (tokenStream.incrementToken()) {
//				String termStr = charTermAttribute.toString();
//				if (termstrings.contains(termStr))
//					continue;
//				termstrings.add(termStr);
//			}
//			for (String termStr: termstrings){
//				if (STOPWORDS.contains(termStr))
//					continue;
//				Query subquery = new TermQuery(new Term(field, termStr));
//				float boost = 1.0f/(reader.docFreq(new Term(field, termStr))+1);
//				subquery.setBoost(boost);
//				query.add(subquery, Occur.SHOULD);
//				System.out.println(termStr);
//			}
//			
////			query.setBoost(1.0f);
//			//Weight w=searcher.createNormalizedWeight(query);
//			//System.out.println(w.getClass());
			TopDocs results = searcher.search(query, maxnum);
			return results;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Document getDoc(int docID) {
		try {
			return searcher.doc(docID);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void loadGlobals(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] info = line.split("=");
				globals.put(info[0], Float.parseFloat(info[1]));
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public float getAvg(String field) {
		return globals.get(field);
	}

	public static void main(String[] args) {
		IRSearcher search = new IRSearcher("forIndex/index");
		search.loadGlobals("forIndex/global.txt");
		
		TopDocs results = search.searchQuery("清华", "题名", 100);
		ScoreDoc[] hits = results.scoreDocs;
		for (int i = 0; i < hits.length; i++) {
			Document doc = search.getDoc(hits[i].doc);
			System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score + " title=" + doc.get("题名"));
		}
	}
}
