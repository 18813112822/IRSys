import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ansj.domain.Term;
import org.ansj.splitWord.Analysis;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.pdfbox.contentstream.operator.markedcontent.BeginMarkedContentSequenceWithProperties;
import org.apache.tomcat.jni.OS;
import org.junit.experimental.theories.FromDataPoints;

import com.sun.corba.se.impl.orbutil.ObjectStreamClass_1_3_1;
import com.sun.org.apache.bcel.internal.generic.FLOAD;
import com.sun.org.apache.bcel.internal.generic.INEG;
import com.sun.org.apache.regexp.internal.recompile;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Text;

import javafx.geometry.Pos;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.IntBinaryOperator;
import java.math.*;
import java.net.*;
import java.security.KeyStore.PrivateKeyEntry;
import java.time.chrono.MinguoChronology;
import java.io.*;
import javax.servlet.ServletContext;

public class IRServer extends HttpServlet {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static final int PAGE_RESULT = 10;
    public static final String indexDir = "forIndex";
    public static final String picDir = "";
    private IRSearcher search = null;

    private HashMap<Integer, String> TextCache = new HashMap<>();


    public IRServer() {
        super();
        System.out.println("user.dir:" + (System.getProperty("user.dir")));
        search = new IRSearcher(new String(indexDir + "/index"));
        search.loadGlobals(new String(indexDir + "/global.txt"));
    }

    private ScoreDoc[] showList(ScoreDoc[] results, int page) {
        if (results == null || results.length < (page - 1) * PAGE_RESULT) {
            return null;
        }
        int start = Math.max((page - 1) * PAGE_RESULT, 0);
        int docnum = Math.min(results.length - start, PAGE_RESULT);
        ScoreDoc[] ret = new ScoreDoc[docnum];
        for (int i = 0; i < docnum; i++) {
            ret[i] = results[start + i];
        }
        return ret;
    }

    private void normalizeScore(ScoreDoc[] results) {
        if (results.length == 0)
            return;
        float max = results[0].score;
        for (int i = 0; i < results.length; i++) {
            if (results[i].score > max)
                max = results[i].score;
        }
        float muti = (float)1.0 / max;
        for (int i = 0; i < results.length; i++) {
            results[i].score = muti * (results[i].score);
        }
    }

    private ScoreDoc[] multiQuery(String queryString, int maxnum) {
        HashMap<String, Float> weight = new HashMap<String, Float>() {
            {
                put("title", (float)1.0);
                put("url", (float)0.9);
                put("h1", (float)0.5);
                put("h2", (float)0.2);
                put("h3", (float)0.1);
                put("h4", (float)0.1);
                put("h5", (float)0.1);
                put("h6", (float)0.1);
                put("text", (float)0.05);
            }
        };

        HashMap<Integer, ScoreDoc> docs = new HashMap<>();

        for (HashMap.Entry<String, Float> entry : weight.entrySet()) {
//			System.out.println(entry.getKey());
            TopDocs results = search.searchQuery(queryString, entry.getKey(), 100);
            if (results == null || results.scoreDocs == null)
                continue;
            ScoreDoc[] tmpdocs = results.scoreDocs;
            normalizeScore(tmpdocs);
            for (ScoreDoc document : tmpdocs) {
//				if (document.doc == 51397 || document.doc == 25320){
//					System.out.println(search.getDoc(document.doc).get("title"));
//					System.out.println(document.score);
//				}

//				if (!TextCache.containsKey(document.doc))
//					TextCache.put(document.doc, entry.getKey());
//				else {
//					float w1 = weight.get(TextCache.get(document.doc));
//					float w2 = entry.getValue();
//					if (w2 > w1)
//						TextCache.put(document.doc, entry.getKey());
//				}

                if (docs.containsKey(docs.containsKey(document.doc))) {
                    docs.get(document.doc).score += entry.getValue() * document.score;
                } else {
                    docs.put(document.doc, new ScoreDoc(document.doc, entry.getValue() * document.score));
                }
            }
//			for (HashMap.Entry<Integer, ScoreDoc> entry2: docs.entrySet()){
//				System.out.println(entry.getKey()+":"+entry2.getValue());
//			}
        }


        ScoreDoc[] ans = new ScoreDoc[docs.size()];
        int i = 0;
        for (Map.Entry<Integer, ScoreDoc> entry : docs.entrySet()) {
            ans[i] = entry.getValue();
            i++;
        }
        Arrays.sort(ans, new Comparator<ScoreDoc>() {
            @Override
            public int compare(ScoreDoc o1, ScoreDoc o2) {
                if (o1.score > o2.score)
                    return -1;
                if (o1.score < o2.score)
                    return 1;

                int l1 = search.getDoc(o1.doc).get("url").length();
                int l2 = search.getDoc(o2.doc).get("url").length();
                if (l1 < l2)
                    return -1;
                if (l1 > l2)
                    return 1;

                float r1 = Float.parseFloat(search.getDoc(o1.doc).get("pagerank"));
                float r2 = Float.parseFloat(search.getDoc(o1.doc).get("pagerank"));
                if (r1 > r2)
                    return -1;
                if (r1 < r2)
                    return 1;
                return 0;
            }
        });
        return ans;
    }

    private class HLPosition {
        public ArrayList<Integer> starts = new ArrayList<>();
        public ArrayList<Integer> ends = new ArrayList<>();

        private int max(int a, int b) {
            return a > b ? a : b;
        }

        private int min(int a, int b) {
            return a < b ? a : b;
        }

        public void add(int start, int end) {
            int length = starts.size();
            for (int i = 0; i < length; i++) {
                if (end < starts.get(i)) {

                }
            }
        }
    }


    private ArrayList<Integer> allIndexOf(String text, String substring) {
        ArrayList<Integer> pos = new ArrayList<>();
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
//			System.out.println(substring + index);
            pos.add(index);
            index += substring.length();
        }
        return pos;
    }

    private ArrayList<Integer> splitIndex(char[] flags, int length) {
        ArrayList<Integer> split = new ArrayList<>();
        if (length == 0) {
            return split;
        }
        int pos = 0;
        char pre = flags[0];

        for (int i = 1; i < length; i++) {
            if (flags[i] != flags[pos]) {
                pos = i;
                pre = flags[i];
                split.add(pos);
            }
        }
        split.add(length);
        return split;
    }

    private String highlightText(String text, String querystring) {
        List<Term> terms = IndexAnalysis.parse(querystring);
        ArrayList<String> arraystrings = new ArrayList<>();
        for (Term term : terms) {
            if (IRSearcher.STOPWORDS.contains(term.toString()))
                continue;
            arraystrings.add(term.toString());
        }
        String[] strings = arraystrings.toArray(new String[0]);

        char[] flags = new char[text.length()];
        for (int i = 0; i < text.length(); i++)
            flags[i] = 0;

        for (String term : strings) {
            ArrayList<Integer> poss = allIndexOf(text, term);
            for (int pos : poss) {
                for (int i = pos; i < pos + term.length(); i++)
                    flags[i] = 1;
            }
        }

        ArrayList<Integer> split = splitIndex(flags, text.length());
        ArrayList<String> substrings = new ArrayList<>();

        String ans = "";
        for (int i = 1; i < split.size(); i++) {
            String substring = text.substring(split.get(i - 1), split.get(i));
            char flag = flags[split.get(i - 1)];
            if (flag == 0) {
                int remain = 7;
                String prex = "<span>";
                String subx = "</span>";
                if (substring.length() > remain * 4) {
                    String prefix = substring.substring(0, remain);
                    String suffix = substring.substring(substring.length() - remain, substring.length());
                    substring = prefix + "..." + suffix;
                }
                substring = prex + substring + subx;
            }
            if (flag == 1) {
                String prefix = "<span class = 'highlight'>";
                String suffix = "</span>";
                substring = prefix + substring + suffix;
            }
            ans += substring;
            if (ans.length() > 400)
                break;
        }
        return ans;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        String queryString = request.getParameter("query");
        String pageString = request.getParameter("page");
        int page = 1;
        if (pageString != null) {
            page = Integer.parseInt(pageString);
        }
        if (queryString == null) {
            System.out.println("null query");
            //request.getRequestDispatcher("/Image.jsp").forward(request, response);
        } else {
            System.out.println(queryString);
            System.out.println(URLDecoder.decode(queryString, "utf-8"));
            System.out.println(URLDecoder.decode(queryString, "gb2312"));


//			IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexDir+"/index")));
//			Dictionary dict = new LuceneDictionary(reader, "text");
//			SpellChecker spellChecker = new SpellChecker();
//			String[] sugs = spellChecker.suggestSimilar(queryString, 5);
//			for (String str: sugs){
//				System.out.println(str);
//			}
//			spellChecker.close();

            String[] titles = null;
            String[] urls = null;
            String[] texts = null;
//			TopDocs results = search.searchQuery(queryString, "title", 100);
            TextCache.clear();
            ScoreDoc[] results = multiQuery(queryString, 100);
            if (results != null) {
                ScoreDoc[] hits = showList(results, page);
                if (hits != null) {
                    System.out.println(hits.length);
                    titles = new String[hits.length];
                    urls = new String[hits.length];
                    texts = new String[hits.length];
                    for (int i = 0; i < hits.length && i < PAGE_RESULT; i++) {
                        Document doc = search.getDoc(hits[i].doc);
                        titles[i] = doc.get("title");
                        urls[i] = picDir + doc.get("url");
                        texts[i] = doc.get("text");
                        texts[i] = highlightText(texts[i], queryString).replaceAll("\n\r", " ").replaceAll("\n", " ");
                        System.out.println(texts[i]);
                        System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score
                                           + " pagerank=" + doc.get("pagerank") + " title=" + doc.get("title") + " url=" + doc.get("url"));
                    }

                } else {
                    System.out.println("page null");
                }
            } else {
                System.out.println("result null");
            }
            request.setAttribute("currentQuery", queryString);
            request.setAttribute("currentPage", page);
            request.setAttribute("titles", titles);
            request.setAttribute("urls", urls);
            request.setAttribute("texts", texts);
            request.getRequestDispatcher("/show.jsp").forward(request,
                    response);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        this.doGet(request, response);
    }
}
