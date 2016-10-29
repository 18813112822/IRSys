
import java.io.IOException;
import java.util.Base64.Decoder;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;

/**
 * Expert: A <code>Scorer</code> for documents matching a <code>Term</code>.
 */
final class SimpleScorer extends Scorer {

	private float idf;
	private final TermDocs termDocs;
	private final byte[] norms;
	private float weightValue;
	private int doc = -1;
	private int freq;

	private final int[] docs = new int[32]; // buffered doc numbers
	private final int[] freqs = new int[32]; // buffered term freqs
	private int pointer;
	private int pointerMax;

	private static final int SCORE_CACHE_SIZE = 32;
	private final float[] scoreCache = new float[SCORE_CACHE_SIZE];

	private float avgLength;
	private float K1 = 2.0f;
	private float b = 0.75f;

	private Searcher search;
	private String field;

	public void setBM25Params(float aveParam) {
		avgLength = aveParam;
	}

	public void setBM25Params(float aveParam, float kParam, float bParam) {
		avgLength = aveParam;
		K1 = kParam;
		b = bParam;
	}

	/**
	 * Construct a <code>SimpleScorer</code>.
	 *
	 * @param weight
	 *            The weight of the <code>Term</code> in the query.
	 * @param td
	 *            An iterator over the documents matching the <code>Term</code>.
	 * @param similarity
	 *            The </code>Similarity</code> implementation to be used for
	 *            score computations.
	 * @param norms
	 *            The field norms of the document fields for the
	 *            <code>Term</code>.
	 */
	SimpleScorer(Weight weight, TermDocs td, Similarity similarity,
	             byte[] norms, float idfValue, float avg, Searcher search, String field) {
		super(similarity, weight);

		this.termDocs = td;
		this.norms = norms;
		this.weightValue = weight.getValue();
		this.idf = idfValue;
		this.avgLength = avg;

		this.search = search;
		this.field = field;
		for (int i = 0; i < SCORE_CACHE_SIZE; i++)
			scoreCache[i] = getSimilarity().tf(i) * weightValue;
	}

	@Override
	public void score(Collector c) throws IOException {
		score(c, Integer.MAX_VALUE, nextDoc());
	}

	// firstDocID is ignored since nextDoc() sets 'doc'
	@Override
	protected boolean score(Collector c, int end, int firstDocID)
	throws IOException {
		c.setScorer(this);
		while (doc < end) { // for docs in window
			c.collect(doc); // collect score

			if (++pointer >= pointerMax) {
				pointerMax = termDocs.read(docs, freqs); // refill buffers
				if (pointerMax != 0) {
					pointer = 0;
				} else {
					termDocs.close(); // close stream
					doc = Integer.MAX_VALUE; // set to sentinel value
					return false;
				}
			}
			doc = docs[pointer];
			freq = freqs[pointer];
		}
		return true;
	}

	@Override
	public int docID() {
		return doc;
	}

	@Override
	public float freq() {
		return freq;
	}

	/**
	 * Advances to the next document matching the query. <br>
	 * The iterator over the matching documents is buffered using
	 * {@link TermDocs#read(int[],int[])}.
	 *
	 * @return the document matching the query or NO_MORE_DOCS if there are no
	 *         more documents.
	 */
	@Override
	public int nextDoc() throws IOException {
		pointer++;
		if (pointer >= pointerMax) {
			pointerMax = termDocs.read(docs, freqs); // refill buffer
			if (pointerMax != 0) {
				pointer = 0;
			} else {
				termDocs.close(); // close stream
				return doc = NO_MORE_DOCS;
			}
		}
		doc = docs[pointer];
		freq = freqs[pointer];
		return doc;
	}

	@Override
	public float score() {
		assert doc != -1;

//		float temp = new DefaultSimilarity().decodeNormValue(norms[doc]);
//		float length = 1 / (temp * temp);
//
// 		try {
// 			Document docu = search.doc(doc);
//// 			System.out.println(docu.get(field));
// 			length = docu.get(field).length();
// 			float TF =  (K1 + 1) * this.freq / (K1 * (1 - this.b + this.b * length / this.avgLength) + this.freq);
// 			if (doc == 51397 || doc == 25320){
// 				System.out.println(TF*idf);
// 				System.out.println(docu.get(field));
// 			}
// 				
// 		} catch (CorruptIndexException e) {
// 			e.printStackTrace();
// 		} catch (IOException e) {
// 			e.printStackTrace();
// 		}
// 		
//// 		System.out.println(doc);
//// 		System.out.println(freq); 		
////		System.out.println(length);
// 		
//		float TF =  (K1 + 1) * this.freq / (K1 * (1 - this.b + this.b * length / this.avgLength) + this.freq);
////		System.out.println(TF*idf);
////		System.out.println(TF);
////		System.out.println(idf);
//		return Math.abs(idf * TF);
		
//		
////		if (doc == 51397 || doc == 25320){
////			System.out.println(doc);
////		}
		
		float raw;
		if (freq < SCORE_CACHE_SIZE)
			raw = scoreCache[freq];
		else
			raw = getSimilarity().tf(freq)*weightValue;        

		return norms == null ? raw : raw * getSimilarity().decodeNormValue(norms[doc]); // normalize for field
	}

	/**
	 * Advances to the first match beyond the current whose document number is
	 * greater than or equal to a given target. <br>
	 * The implementation uses {@link TermDocs#skipTo(int)}.
	 *
	 * @param target
	 *            The target document number.
	 * @return the matching document or NO_MORE_DOCS if none exist.
	 */
	@Override
	public int advance(int target) throws IOException {
		// first scan in cache
		for (pointer++; pointer < pointerMax; pointer++) {
			if (docs[pointer] >= target) {
				freq = freqs[pointer];
				return doc = docs[pointer];
			}
		}

		// not found in cache, seek underlying stream
		boolean result = termDocs.skipTo(target);
		if (result) {
			pointerMax = 1;
			pointer = 0;
			docs[pointer] = doc = termDocs.doc();
			freqs[pointer] = freq = termDocs.freq();
		} else {
			doc = NO_MORE_DOCS;
		}
		return doc;
	}

	/** Returns a string representation of this <code>SimpleScorer</code>. */
	@Override
	public String toString() {
		return "scorer(" + weight + ")";
	}

}
