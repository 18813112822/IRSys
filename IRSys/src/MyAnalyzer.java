import java.io.Reader;
import java.util.List;

import javax.xml.bind.ParseConversionEvent;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

public class MyAnalyzer extends Analyzer {

	@Override
	public TokenStream tokenStream(String arg0, Reader arg1) {
		// TODO Auto-generated method stub
		List<Term> parse = IndexAnalysis.parse("上海虹桥机场南路");
		System.out.println("Myanalyzer");
		System.out.println(parse);
		return null;
	}

}
